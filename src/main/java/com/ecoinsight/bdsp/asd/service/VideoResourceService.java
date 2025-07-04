package com.ecoinsight.bdsp.asd.service;

import java.util.Optional;
import java.util.List;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.ecoinsight.bdsp.asd.crypto.FileCryptoKeys;
import com.ecoinsight.bdsp.asd.crypto.FileDecryptor;
import com.ecoinsight.bdsp.asd.entity.VideoResource;
import com.ecoinsight.bdsp.asd.entity.VideoReupload;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.ProjectSeq;
import com.ecoinsight.bdsp.asd.model.VideoReuploadModel;
import com.ecoinsight.bdsp.asd.model.VideoReuploadState;
import com.ecoinsight.bdsp.asd.repository.IVideoResourceRepository;
import com.ecoinsight.bdsp.asd.repository.IVideoReuploadRepository;
import com.ecoinsight.bdsp.asd.web.IFVideo;
import com.ecoinsight.bdsp.core.service.ServiceException;
import com.ecoinsight.bdsp.core.util.DateUtil;

@Transactional
@Service(VideoResourceService.ID)
public class VideoResourceService implements AsdSubjectService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String ID = "VideoResourceService";

    @Value("${ecoinsight.video.crypto.output-dir}")
    private String _outputDir;
    @Value("${ecoinsight.object-storage.endpoint}")
    private String _endpoint;
    @Value("${ecoinsight.object-storage.region}")
    private String _region;
    @Value("${ecoinsight.object-storage.access-key}")
    private String _accessKey;
    @Value("${ecoinsight.object-storage.secret-key}")
    private String _secretKey;
    @Value("${ecoinsight.object-storage.internal}")
    private String _objectStoageInternal;
    @Value("${ecoinsight.object-storage.to.skt}")
    private String _objectStoageToSkt;
    @Value("${ecoinsight.object-storage.to.eco}")
    private String _objectStoageToEco;

    @Value("${ecoinsight.video.crypto.security-path}")
    private String _securityPath;
    @Value("${ecoinsight.video.crypto.password}")
    private String _password;

    @Autowired
    private IVideoResourceRepository _videoRepository;
    @Autowired
    private IVideoReuploadRepository _reuploadRepository;
    @Autowired
    private DataCommonService _dataCommonService;

    @Transactional(propagation = Propagation.REQUIRED)
    public void create(AsdProjectSubject subject, VideoResource resource, VideoReupload reupload) throws Exception {
        try {
            this._videoRepository.add(resource);
            if (reupload != null) {
                reupload.setState(VideoReuploadState.COMPLETED.name());
                this._reuploadRepository.updateState(reupload.getId(), VideoReuploadState.COMPLETED.name(),
                        resource.getUserCreated(), resource.getDateCreated());
            }
        } catch (Exception e) {
            throw new Exception("Fail to create a video resource in database. - " + e.getMessage(), e);
        }

        List<IFVideo> items = null;
        try {
            boolean result = true;
            items = IFVideo.getVideos(subject.getRegistDate(), subject.getBirthDay());
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("-> VideoResourceService - birthday=" + subject.getBirthDay() + ", IFVideoList=" + items);
            }

            LOGGER.debug("itemsize={},items", items.size(), items);

            if (items == null || items.size() == 0) {
                result = false; // there is no video to upload for this age.
                return;
            }

            for (IFVideo item : items) {
                // Completed : 3개 or retryIndex=0
                // @Param("systemId") String systemId, @Param("subjectId") String subjectId,
                // @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex,
                // @Param("interfaceId") String interfaceId
                List<VideoResource> resources = this._videoRepository.queryVideoResources2(resource.getSystemId(),
                        resource.getSubjectId(), resource.getProjectSeq(), resource.getTrialIndex(), item.name());
                if (resources.size() == 3)
                    continue; // completed for this interface
                if (resources.size() == 0) { // no uploaded video
                    result = false;
                    break;
                }
                boolean completed = false;
                for (VideoResource vr : resources) { // 1 or 2 videos
                    if (vr.getRetryIndex() == 0) {
                        completed = true;
                        break;
                    }
                }
                // not completed case for less than 3 videos.
                if (!completed) {
                    result = false;
                    break;
                }
            }
            LOGGER.debug("result={}", result);
            if (result) {
                this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.VideoResource,
                        resource.getSubjectId(), resource.getProjectSeq(), resource.getTrialIndex(), result);
            }
        } catch (Exception e) {
            throw new Exception("Fail to handle data summary. - " + e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void create(VideoReupload reupload) throws ServiceException {
        try {

            this._reuploadRepository.add(reupload);
        } catch (Exception e) {
            throw new ServiceException("Fail to create a video reupload in database. - " + e.getMessage(), e);
        }
        // boolean isValid = false;
        // try {
        // this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.VideoResource,
        // reupload.getSubjectId(),
        // reupload.getProjectSeq(), reupload.getTrialIndex(), isValid);
        // } catch (Exception ex) {
        // throw new ServiceException("Fail to handle data summary. - " +
        // ex.getMessage(), ex);
        // }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateState(VideoReupload reupload) throws Exception {
        try {
            this._reuploadRepository.updateState(reupload.getId(), reupload.getState(), reupload.getUserUpdated(),
                    reupload.getDateUpdated());
        } catch (Exception e) {
            throw new Exception("Fail to update state of a video reupload in database. - " + e.getMessage(), e);
        }
    }

    public Optional<VideoReupload> getActiveVideoReupload(String systemId, String subjectId, long projectSeq,
            int trialIndex, String interfaceId) {
        return this._reuploadRepository.getActiveVideoReupload(systemId, subjectId, projectSeq, trialIndex,
                interfaceId);
    }

    public List<VideoReupload> getRequestedVideoReuploads() {
        return this._reuploadRepository.getRequestedVideoReuploads();
    }

    public List<VideoResource> getVideoResources(String systemId, String subjectId, long projectSeq, int trialIndex,
            String interfaceId) {
        return this._videoRepository.queryVideoResources2(systemId, subjectId, projectSeq, trialIndex, interfaceId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(List<VideoResource> resources, VideoReuploadModel model, String worker) throws Exception {

        int updatedResourceCount = 0;

        try {
            for (VideoResource resource : resources) {
                resource.setDateUpdated(LocalDateTime.now());
                resource.setUserUpdated(worker);

                if (this._videoRepository.delete(resource)) {
                    updatedResourceCount++;
                }
            }

            if (updatedResourceCount == resources.size()) {
                this._dataCommonService.updateDataSummaryColumn(
                        DataSummaryColumn.VideoResource,
                        model.getSubjectId(),
                        model.getProjectSeq(),
                        model.getTrialIndex(),
                        false);
            }

        } catch (DataAccessException ex) {

            throw new Exception("Fail to handle data upate . - ", ex);
        }

    }

    /**
     * 보류(4) to 정상(1), 고위험군(2), 자폐(3)
     * 
     * @param systemId
     * @param subjectId
     * @param projectSeq
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void doLabelling(String systemId, String subjectId, long projectSeq) throws Exception {
        List<VideoResource> result = this._videoRepository.queryVideoResources3(systemId, subjectId,
                ProjectSeq.HOLD.getSeq());
        // List<VideoResource> result =
        // this._videoRepository.queryVideoResources3(systemId, subjectId,
        // 0);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("query count=" + result.size() + ", systemId=" + systemId + ", subjectId=" + subjectId
                    + ", projectSeq=" + ProjectSeq.HOLD.getSeq());
        }
        if (result.size() == 0)
            return;

        final AWSCredentials credentials = new BasicAWSCredentials(this._accessKey, this._secretKey);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(this._endpoint, this._region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        for (VideoResource item : result) {
            if (item.isLabelled() || item.isDeleted())
                continue;

            try {
                // move object storage from internal to skt
                String sname = item.getTargetFileName();
                String tname = toFilename(sname, projectSeq);
                LocalDateTime pdate = item.getDatePublished();
                ZonedDateTime pzdatetime = pdate.atZone(ZoneId.systemDefault());
                String sfolder = DateUtil.getCurrentDate(new Date(pzdatetime.toInstant().toEpochMilli()));

                LocalDateTime now = LocalDateTime.now();
                ZonedDateTime tzdatetime = now.atZone(ZoneId.systemDefault());
                String tfolder = DateUtil.getCurrentDate(new Date(tzdatetime.toInstant().toEpochMilli()));

                if (item.isPublished()) {
                    moveS3Object(s3, sfolder, sname, tfolder, tname);
                }
                item.setProjectSeq(projectSeq);
                item.setLabelled(true);
                item.setDateLabelled(now);
                item.setTargetFileName(tname);
                // ProjectSeq, Labelled, LDate, fileName update required
                this._videoRepository.changeAfterLabelled(item);
            } catch (Exception e) {
                throw new Exception("Fail to do labelling. (VedioId=" + item.getId() + ") - " + e.getMessage(), e);
            }
        }
    }

    public List<VideoResource> findByProjectSeqAndTrialIndex(String systemId, String subjectId, long projectSeq, int trialIndex) throws Exception {
        try {
            return this._videoRepository.findByProjectSeqAndTrialIndex(systemId, subjectId, projectSeq, trialIndex);
        } catch (Exception e) {
            throw new Exception("Fail to find video resources. - " + e.getMessage(), e);
        }
    }

    public boolean download(File downloadDir, String systemId, String subjectId, long projectSeq, int trialIndex)
            throws Exception {
        List<VideoResource> resources = this._videoRepository.findByProjectSeqAndTrialIndex(systemId, subjectId, projectSeq, trialIndex);
        if (resources == null || resources.size() == 0) {
            throw new Exception(
                    String.format("업로드한 상호작용 영상이 없습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s",
                            systemId, subjectId, projectSeq, trialIndex));
        }

        final AWSCredentials credentials = new BasicAWSCredentials(this._accessKey, this._secretKey);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(this._endpoint, this._region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        int downloadedFileCount = 0;
        for (VideoResource resource : resources) {
            if(!resource.isPublished()) continue;
            
            LocalDateTime dtime = resource.getDatePublished();
            ZonedDateTime zdatetime = dtime.atZone(ZoneId.systemDefault());
            String folder = DateUtil.getCurrentDate(new Date(zdatetime.toInstant().toEpochMilli()));

            // Filename changed after SKT : IF2001_4_1_1023032001_1.mp4.enc to
            // deid_IF2001_4_1_1023032001_1.mp4
            String fname = resource.getTargetFileName();
            String targetFileName = this.toFileName(fname);
            String objectPath = String.format("%s/%s", folder, targetFileName);
            if (!s3.doesObjectExist(this._objectStoageToEco, objectPath)) {
                LOGGER.warn("Cannot find the video object from the ncloud. - " + objectPath);
                continue;
            }
            try {
                File resultFile = this.downloadFromS3(s3, objectPath, downloadDir, targetFileName);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("download completed. - " + resultFile.getAbsolutePath());
                }
                ++downloadedFileCount;
            } catch (Exception e) {
                throw e;
            }
        }

        if (downloadedFileCount == 0) {
            LOGGER.warn("There is no downloaded file.");
            downloadDir.delete();
            return false;
        }
        return true;
    }

    public File download(File downloadDir, String systemId, String subjectId, long projectSeq, int trialIndex,
            String ifId) throws Exception {
        List<VideoResource> resources = this._videoRepository.findByProjectSeqAndTrialIndexAndIfId(systemId, subjectId, projectSeq, trialIndex, ifId);
        if (resources == null || resources.size() == 0) {
            throw new Exception(String.format(
                    "업로드한 상호작용 영상이 없습니다. - systemId=%s, subjectId=%, projectSeq=%s, trialIndex=%s, interfaceId=%s",
                    systemId, subjectId, projectSeq, trialIndex, ifId));
        }
        final AWSCredentials credentials = new BasicAWSCredentials(this._accessKey, this._secretKey);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(this._endpoint, this._region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        VideoResource vresource = resources.get(0);
        LocalDateTime dtime = vresource.getDatePublished();
        ZonedDateTime zdatetime = dtime.atZone(ZoneId.systemDefault());
        String folder = DateUtil.getCurrentDate(new Date(zdatetime.toInstant().toEpochMilli()));

        // Filename changed after SKT : IF2001_4_1_1023032001_1.mp4.enc to
        // deid_IF2001_4_1_1023032001_1.mp4
        String fname = vresource.getTargetFileName();
        String targetFileName = toFileName(fname);
        String objectPath = String.format("%s/%s", folder, targetFileName);
        if (!s3.doesObjectExist(this._objectStoageToEco, objectPath)) {
            LOGGER.error("Cannot find the video object in the object storage. - " + objectPath);
            return null;
        }

        try {
            File resultFile = this.downloadFromS3(s3, objectPath, downloadDir, targetFileName);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("download completed. - " + resultFile.getAbsolutePath());
            }
            return resultFile;
        } catch (Exception e) {
            throw e;
        }
    }

    private String toFilename(String fname, long projectSeq) {
        int idx = fname.indexOf(".");
        String prefix = fname.substring(0, idx);
        String suffix = fname.substring(idx);

        String[] items = prefix.split("_");
        return String.format("%s_%s_%s_%s_%s", items[0], projectSeq, items[2], items[3], items[4]) + suffix;
    }

    private void moveS3Object(AmazonS3 s3, String sfolder, String sname, String tfolder, String tname)
            throws Exception {
        String sourceKey = toKey(sfolder, sname);
        if (!s3.doesObjectExist(_objectStoageInternal, sourceKey)) {
            throw new Exception(
                    "Cannot find source file from the " + _objectStoageInternal + " bucket. source file=" + sourceKey);
        }

        String targetKey = toKey(tfolder, tname);
        CopyObjectRequest copyRequest = new CopyObjectRequest(_objectStoageInternal, sourceKey, _objectStoageToSkt,
                targetKey);
        try {
            CopyObjectResult copyResult = s3.copyObject(copyRequest);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("-> CopyResult=" + copyResult);
            }
            DeleteObjectRequest deleteRequest = new DeleteObjectRequest(_objectStoageInternal, sourceKey);
            s3.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new Exception("Fail to move a object(" + sourceKey + "). - " + e.getMessage(), e);
        }
    }

    private String toKey(String folder, String file) {
        return String.format("%s/%s", folder, file);
    }

    private File downloadFromS3(AmazonS3 s3, String objectPath, File downloadDir, String targetFileName)
            throws Exception {
        S3Object s3Object = s3.getObject(this._objectStoageToEco, objectPath);
        S3ObjectInputStream s3in = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        FileOutputStream fo = null;
        File downloadFile = new File(downloadDir, targetFileName);
        try {
            s3in = s3Object.getObjectContent();
            bis = new BufferedInputStream(s3in);
            fo = new FileOutputStream(downloadFile);
            bos = new BufferedOutputStream(fo);
            byte[] bytesArray = new byte[4096];
            while (true) {
                int l = bis.read(bytesArray);
                if (l == -1)
                    break;
                bos.write(bytesArray, 0, l);
            }
            bos.flush();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("download completed from S3. - " + downloadFile.getAbsolutePath());
            }
            // File decryption : deid_IF2001_4_1_1023032001_1.mp4.enc to
            // deid_IF2001_4_1_1023032001_1.mp4
            if (targetFileName.endsWith(FileCryptoKeys.ENCRYPTED_FILE_SUFFIX)) {
                String outputFileName = targetFileName.substring(0,
                        targetFileName.length() - FileCryptoKeys.ENCRYPTED_FILE_SUFFIX.length());
                File outputFile = new File(downloadDir, outputFileName);

                File saltFile = new File(this._securityPath, FileCryptoKeys.SALT_FILENAME);
                FileDecryptor fdecryptor = new FileDecryptor(saltFile);
                fdecryptor.decrypt(this._password, downloadFile, outputFile);
                if (outputFile.exists()) {
                    downloadFile.delete();
                }

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("decrypted a download file. - " + outputFile.getAbsolutePath());
                }
                return outputFile;
            }
            return downloadFile;
        } catch (Exception e) {
            throw new Exception("Fail to download a file from object storage. - " + e.getMessage(), e);
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch (Exception e) {
            }
            try {
                if (s3in != null)
                    s3in.close();
            } catch (Exception e) {
            }
            try {
                if (bos != null)
                    bos.close();
            } catch (Exception e) {
            }
            try {
                if (fo != null)
                    fo.close();
            } catch (Exception e) {
            }
            try {
                if (s3Object != null)
                    s3Object.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 1. Object storage toSkt
     * 2. Object storage toEco
     * 3. T_VideoResource
     * 
     */
    // @Transactional(propagation = Propagation.REQUIRED)
    // public void changeProjectSeq(String systemId, String subjectId, long
    // fromProjectSeq, long toProjectSeq)
    // throws Exception {
    // if (ProjectSeq.HOLD.getSeq() == fromProjectSeq) {
    // throw new Exception("Invalid appfrom projectSeq. - projectSeq=" +
    // fromProjectSeq);
    // }
    // // query VideoResources from T_VideoResource table
    // List<VideoResource> vresources =
    // this._videoRepository.findByProjectSeq(systemId, subjectId, fromProjectSeq);
    // for (VideoResource vresource : vresources) {
    // String origName = vresource.getOriginalFileName();

    // String origExtension = FilenameUtils.getExtension(origName);
    // String fileName = vresource.getTargetFileName();
    // int lastidx = fileName.lastIndexOf(".");
    // String translate = fileName.substring(0, lastidx);
    // String targetExtension = FilenameUtils.getExtension(translate);

    // if (origExtension.equals(FileCryptoKeys.ENCRYPTED_FILE_EXTENSION)) {
    // origExtension = FileCryptoKeys.MP4_FILE_EXTENSION;
    // } else {
    // origExtension = FileCryptoKeys.MP4_FILE_EXTENSION;
    // }

    // if (targetExtension.equals(FileCryptoKeys.MP4_FILE_EXTENSION)) {
    // targetExtension = FileCryptoKeys.ENCRYPTED_FILE_EXTENSION;
    // } else {
    // targetExtension = FileCryptoKeys.ENCRYPTED_FILE_EXTENSION;
    // }

    // String newFileName = this.fname(vresource.getInterfaceId(), toProjectSeq,
    // vresource.getTrialIndex(),
    // subjectId, vresource.getRetryIndex()) + "." + origExtension + "." +
    // targetExtension;

    // // 1. not published
    // if (!vresource.isPublished()) {
    // // VideoResourceUploadBatch run every 10 minutes.
    // File file = new File(this._outputDir, fileName);
    // if (!file.exists())
    // continue;

    // File newFile = new File(this._outputDir, newFileName);
    // file.renameTo(newFile);

    // vresource.setProjectSeq(toProjectSeq);
    // vresource.setOriginalFileName(fileName);
    // vresource.setTargetFileName(newFileName);
    // try {
    // this._videoRepository.updateProjectSeq(vresource);
    // } catch (Exception e) {
    // LOGGER.error("Fail to update projectSeq of the VideoResource" +
    // vresource.getId() + ". - "
    // + e.getMessage(), e);
    // }
    // continue;
    // }

    // // 2. published // published가 되고, labelled가 되었으면 이미 to skt로 디렉토리파일이 넘어갔기때문에,
    // // labelled 유무에 따라
    // // find folder를 다르게(labelled 값에따라) 찾는다.
    // LocalDateTime now = LocalDateTime.now();
    // // String folder = DateUtil.getCurrentDate(new
    // // Date(zdatetime.toInstant().toEpochMilli()));
    // LocalDateTime publishedDateTime = vresource.getDatePublished();
    // LocalDateTime labeledDateTime = vresource.getDateLabelled();
    // boolean labeled = vresource.isLabelled();
    // ZonedDateTime zPublishedDatetime = publishedDateTime != null
    // ? publishedDateTime.atZone(ZoneId.systemDefault())
    // : null;
    // ZonedDateTime zLablledDatetime = labeledDateTime != null ?
    // labeledDateTime.atZone(ZoneId.systemDefault())
    // : null;

    // String folder;

    // if (labeled && labeledDateTime != null) {
    // folder = DateUtil.getCurrentDate(new
    // Date(zLablledDatetime.toInstant().toEpochMilli()));
    // } else {
    // folder = DateUtil.getCurrentDate(new
    // Date(zPublishedDatetime.toInstant().toEpochMilli()));
    // }

    // vresource.setDateChangeProject(now);
    // final AWSCredentials credentials = new BasicAWSCredentials(this._accessKey,
    // this._secretKey);
    // final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
    // .withEndpointConfiguration(new
    // AwsClientBuilder.EndpointConfiguration(this._endpoint, this._region))
    // .withCredentials(new AWSStaticCredentialsProvider(credentials))
    // .build();

    // String objectName = String.format("%s/%s", folder, fileName);
    // String newObjectName = String.format("%s/%s", folder, newFileName);

    // boolean objectExistsInSkt = false;
    // boolean objectExistsInEco = false;

    // // 2-1. check published video : toSkt object storage
    // try {
    // objectExistsInSkt = s3.doesObjectExist(this._objectStoageToSkt, objectName);

    // if (objectExistsInSkt) {
    // s3.copyObject(this._objectStoageToSkt, objectName, this._objectStoageToSkt,
    // newObjectName);
    // s3.deleteObject(this._objectStoageToSkt, objectName);
    // } else {
    // LOGGER.error("Cannot find object in the object storage(" +
    // this._objectStoageToSkt
    // + ") - objectName=" + objectName);
    // // continue;
    // }
    // } catch (Exception e) {
    // LOGGER.error("Fail to change objectName in object storage(" +
    // this._objectStoageToSkt
    // + ") - objectName=" + objectName + ", error=" + e.getMessage(), e);
    // }

    // // 2-2. Check published video: toEco object storage only if objectExistsInSkt
    // is
    // // false

    // if (!objectExistsInSkt) {
    // try {
    // objectExistsInEco = s3.doesObjectExist(this._objectStoageToEco, objectName);
    // if (objectExistsInEco) {
    // s3.copyObject(this._objectStoageToEco, objectName, this._objectStoageToSkt,
    // newObjectName);
    // s3.deleteObject(this._objectStoageToEco, objectName);
    // vresource.setLabelled(true);
    // } else {
    // LOGGER.info("Cannot find object in the object storage(" +
    // this._objectStoageToEco
    // + ") - objectName=" + objectName);
    // }
    // } catch (Exception e) {
    // LOGGER.error("Fail to change objectName in object storage(" +
    // this._objectStoageToEco
    // + ") - objectName=" + objectName + ", error=" + e.getMessage(), e);
    // }
    // }

    // try {
    // if (objectExistsInSkt || objectExistsInEco) {
    // vresource.setProjectSeq(toProjectSeq);
    // vresource.setOriginalFileName(fileName);
    // vresource.setTargetFileName(newFileName);
    // this._videoRepository.updateProjectSeq(vresource);
    // } else {
    // LOGGER.info("No action taken for projectSeq update as neither object exists
    // in Skt nor Eco.");
    // }
    // } catch (Exception e) {
    // LOGGER.error("Failed to update projectSeq of the VideoResource " +
    // vresource.getId() + ". - "
    // + e.getMessage(), e);
    // }

    // }
    // }

    // projectSeq ==4 에서 일경우, projectSeq != 4 아니고 1,2,3 인데 intener -> skt 못간 대상자들 경우

    public void changeProjectSeq(String systemId, String subjectId, long fromProjectSeq, long toProjectSeq)
            throws Exception {

    }

    private String fname(String interfaceId, long projectSeq, int trialIndex, String subjectId, int retryIndex) {
        return String.format("%s_%s_%s_%s_%s", interfaceId, projectSeq, trialIndex, subjectId, retryIndex);
    }

    private String toFileName(String fileName) {
        int idx = fileName.lastIndexOf(".");
        String targetFileName = fileName.substring(0,idx);
        return String.format("deid_%s", targetFileName);
    }
}
