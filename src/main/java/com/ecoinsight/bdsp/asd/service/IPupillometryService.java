package com.ecoinsight.bdsp.asd.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.FnirsResource;
import com.ecoinsight.bdsp.asd.model.Pupillometry;
import com.ecoinsight.bdsp.asd.model.PupillometryModel;
import com.ecoinsight.bdsp.asd.model.VideoReuploadModel;
import com.ecoinsight.bdsp.asd.repository.IPupillometryRepository;
import com.ecoinsight.bdsp.core.util.DateUtil;

@Transactional
@Service(IPupillometryService.ID)
public class IPupillometryService implements AsdSubjectService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String ID = "PupillometryService";

    @Value("${ecoinsight.pupillometry.crypto.output-dir}")
    private String _outputDir;
    @Value("${ecoinsight.object-storage.endpoint}")
    private String _endpoint;
    @Value("${ecoinsight.object-storage.region}")
    private String _region;
    @Value("${ecoinsight.object-storage.access-key}")
    private String _accessKey;
    @Value("${ecoinsight.object-storage.secret-key}")
    private String _secretKey;
    @Value("${ecoinsight.object-storage.pupillometry.internal}")
    private String _objectStoageInternal;
    @Value("${ecoinsight.object-storage.to.skt}")
    private String _objectStoageToSkt;
    @Value("${ecoinsight.object-storage.to.eco}")
    private String _objectStoageToEco;

    @Value("${ecoinsight.pupillometry.crypto.security-path}")
    private String _securityPath;
    @Value("${ecoinsight.pupillometry.crypto.password}")
    private String _password;

    @Autowired
    private IPupillometryRepository _pupillometryRepository;
    @Autowired
    private DataCommonService _dataCommonService;

    @Transactional(propagation = Propagation.REQUIRED)
    public void create(AsdProjectSubject subject, Pupillometry pupillometry) throws Exception {
        try {
            this._pupillometryRepository.add(pupillometry);
        } catch (Exception e) {
            throw new Exception("Fail to create a pupillometry resource in database. - " + e.getMessage(), e);
        }

        try {
            boolean completed = false;
            List<Pupillometry> resources = this._pupillometryRepository.queryPupillometryResources(
                    pupillometry.getSystemId(), pupillometry.getSubjectId(),
                    pupillometry.getProjectSeq(), pupillometry.getTrialIndex());

            if (resources.size() > 0) {

                completed = true;
            }

            if (!completed) {
                completed = false;

            }
            LOGGER.debug("completedt={}", completed);
            if (completed) {
                this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.PUPILLOMETRY,
                        pupillometry.getSubjectId(), pupillometry.getProjectSeq(), pupillometry.getTrialIndex(),
                        completed);
            }
        } catch (Exception e) {
            throw new Exception("Fail to handle data summary. - " + e.getMessage(), e);
        }
    }

    public boolean download(File downloadDir, String systemId, String subjectId, long projectSeq, int trialIndex)
            throws Exception {

        List<Pupillometry> resources = this._pupillometryRepository.findByProjectSeqAndTrialIndex(systemId, subjectId,
                projectSeq, trialIndex);

        if (resources == null || resources.size() == 0) {
            throw new Exception(String.format(
                    "업로드한 동공측정 파일이 없습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s",
                    systemId, subjectId, projectSeq, trialIndex));
        }

        final AWSCredentials credentials = new BasicAWSCredentials(this._accessKey, this._secretKey);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(this._endpoint, this._region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        int downloadedFileCount = 0;
        for (Pupillometry resource : resources) {
            LocalDateTime ptime = resource.getDatePublished();
            LocalDateTime cptime = resource.getDatePublished();
            ZonedDateTime pzdatetime = ptime.atZone(ZoneId.systemDefault());
            ZonedDateTime cpzdatetime = cptime.atZone(ZoneId.systemDefault());
            String pfolder = DateUtil.getCurrentDate(new Date(pzdatetime.toInstant().toEpochMilli()));
            String cpfolder = DateUtil.getCurrentDate(new Date(pzdatetime.toInstant().toEpochMilli()));
            // if(cpfolder != null)
            // if(!pfolder.equals(cpfolder)){

            // }
            // Filename changed after SKT : IF2001_4_1_1023032001_1.mp4.enc to
            // IF2001_4_1_1023032001_1_deid.mp4.enc
            String fname = resource.getTargetFileName();
            int idx = fname.indexOf(".");
            // String targetFileName = String.format("%s_deid%s", fname.substring(0, idx),
            // fname.substring(idx));
            String targetFileName = fname;
            String objectPath = String.format("%s/%s", cpfolder, targetFileName);
            if (!s3.doesObjectExist(this._objectStoageInternal, objectPath)) {
                LOGGER.warn("Cannot find the pupillometry object from the ncloud. - " + objectPath);
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

    private File downloadFromS3(AmazonS3 s3, String objectPath, File downloadDir, String targetFileName)
            throws Exception {
        S3Object s3Object = s3.getObject(this._objectStoageInternal, objectPath);
        S3ObjectInputStream s3in = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        FileOutputStream fo = null;
        File outputFile = null;
        File downloadFile = new File(downloadDir, targetFileName);
        try {
            s3in = s3Object.getObjectContent();
            bis = new BufferedInputStream(s3in);
            fo = new FileOutputStream(downloadFile);
            bos = new BufferedOutputStream(fo);
            byte[] bytesArray = new byte[4096];
            while (true) {
                int l = bis.read(bytesArray);
                if (l == -1) {
                    break;
                }
                bos.write(bytesArray, 0, l);
            }
            bos.flush();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("download completed from S3. - " + downloadFile.getAbsolutePath());
            }
            // File decryption : IF2001_4_1_1023032001_1_deid.mp4.enc to
            // IF2001_4_1_1023032001_1_deid.mp4
            if (targetFileName.endsWith(FileCryptoKeys.ENCRYPTED_FILE_SUFFIX)) {
                String outputFileName = targetFileName.substring(0,
                        targetFileName.length() - FileCryptoKeys.ENCRYPTED_FILE_SUFFIX.length());
                outputFile = new File(downloadDir, outputFileName);

                File saltFile = new File(this._securityPath, FileCryptoKeys.SALT_FILENAME);
                FileDecryptor fdecryptor = new FileDecryptor(saltFile);
                fdecryptor.decrypt(this._password, downloadFile, outputFile);

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
                if (bis != null) {
                    bis.close();
                }
            } catch (Exception e) {
            }
            try {
                if (s3in != null) {
                    s3in.close();
                }
            } catch (Exception e) {
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
            }
            try {
                if (fo != null) {
                    fo.close();
                }
            } catch (Exception e) {
            }
            try {
                if (s3Object != null) {
                    s3Object.close();
                }
            } catch (Exception e) {
            }
            if (outputFile != null && outputFile.exists()) {
                boolean result = downloadFile.delete();
                LOGGER.info("Deleted download file(" + result + ") - " + downloadFile.getAbsolutePath());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(List<Pupillometry> resources, Pupillometry model, String worker) throws Exception {

        int deletedResourceCount = 0;

        try {
            final AWSCredentials credentials = new BasicAWSCredentials(this._accessKey, this._secretKey);
            final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(this._endpoint, this._region))
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();
            for (Pupillometry resource : resources) {
                resource.setDateUpdated(LocalDateTime.now());
                resource.setUserUpdated(worker);

                String sname = resource.getTargetFileName();
                String tname = toFilename(sname, resource.getProjectSeq());
                LocalDateTime pdate = resource.getDateCompleted();
                ZonedDateTime pzdatetime = pdate.atZone(ZoneId.systemDefault());
                String sfolder = DateUtil.getCurrentDate(new Date(pzdatetime.toInstant().toEpochMilli()));

                // LocalDateTime now = LocalDateTime.now();
                // ZonedDateTime tzdatetime = now.atZone(ZoneId.systemDefault());
                // String tfolder = DateUtil.getCurrentDate(new
                // Date(tzdatetime.toInstant().toEpochMilli()));
                if (this._pupillometryRepository.delete(resource.getSubjectId(), resource.getProjectSeq(),
                        resource.getTrialIndex())) {
                    deletedResourceCount++;
                    deleteS3Object(s3, sfolder, sname);
                }

            }

            if (deletedResourceCount == resources.size()) {
                this._dataCommonService.updateDataSummaryColumn(
                        DataSummaryColumn.PUPILLOMETRY,
                        model.getSubjectId(),
                        model.getProjectSeq(),
                        model.getTrialIndex(),
                        false);

            }

        } catch (DataAccessException ex) {

            throw new Exception("Fail to handle data upate . - ", ex);
        }

    }

    private String toFilename(String fname, long projectSeq) {
        String targetName = fname;
        int idx = fname.indexOf(".");
        String prefix = fname.substring(0, idx);
        String suffix = null;
        String suffix2 = null;
        if (fname.endsWith(FileCryptoKeys.ENCRYPTED_FILE_SUFFIX)) {
            targetName = fname.substring(0,
                    fname.length() - FileCryptoKeys.ENCRYPTED_FILE_SUFFIX.length());
            idx = targetName.indexOf(".");
            prefix = targetName.substring(0, idx);
            suffix = targetName.substring(idx);
            suffix2 = FileCryptoKeys.ENCRYPTED_FILE_SUFFIX;
        }

        String[] items = prefix.split("_");
        LOGGER.info("prefix[0]:{},prefix[2]:{},prefix[3]:{}", items[0], projectSeq, items[2], items[3]);
        LOGGER.info("suffix[pupill]: ", suffix);
        return String.format("%s_%s_%s_%s", items[0], projectSeq, items[2], items[3]) + suffix + suffix2;
    }

    private void deleteS3Object(AmazonS3 s3, String sfolder, String sname)
            throws Exception {
        String sourceKey = toKey(sfolder, sname);
        if (!s3.doesObjectExist(_objectStoageInternal, sourceKey)) {
            throw new Exception(
                    "Cannot find source file from the " + _objectStoageInternal + " bucket. source file=" + sourceKey);
        }

        // String targetKey = toKey(sfolder, tname);
        // CopyObjectRequest copyRequest = new CopyObjectRequest(_objectStoageInternal,
        // sourceKey, _objectStoageToSkt,
        // targetKey);
        try {
            // CopyObjectResult copyResult = s3.copyObject(copyRequest);
            // if (LOGGER.isInfoEnabled()) {
            // LOGGER.info("-> CopyResult=" + copyResult);
            // }
            DeleteObjectRequest deleteRequest = new DeleteObjectRequest(_objectStoageInternal, sourceKey);
            s3.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new Exception("Fail to move a object(" + sourceKey + "). - " + e.getMessage(), e);
        }
    }

    private String toKey(String folder, String file) {
        return String.format("%s/%s", folder, file);
    }

    public List<Pupillometry> getPupillometry(String systemId, String subjectId, long projectSeq, long subjectProjectSeq, int trialIndex) {

        List<Pupillometry> list = this._pupillometryRepository.queryPupillometryResources(systemId, subjectId, projectSeq, trialIndex);
        boolean state = false;

        for (Pupillometry pupillometry : list) {
            if (pupillometry.getProjectSeq() != subjectProjectSeq) {
                pupillometry.setDateUpdated(LocalDateTime.now());
                state = this._pupillometryRepository.updateSubjectProjectSeq(pupillometry);
            }
        }
        List<Pupillometry> updateList = new ArrayList<>();
        if (state) {
            updateList = this._pupillometryRepository.queryPupillometryResources(systemId, subjectId, subjectProjectSeq, trialIndex);
        } else {
            updateList = list;
        }

        return updateList;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void changeProjectSeq(String systemId, String subjectId, long fromProjectSeq, long toProjectSeq) throws Exception {
        List<Pupillometry> result = this._pupillometryRepository.findByProjectIdAndTrialIndex(systemId, subjectId);
        if(result==null || result.isEmpty()) return;

        for(Pupillometry item : result) {
            if(item.getProjectSeq()==toProjectSeq) continue;
            LocalDateTime now = LocalDateTime.now();
            item.setProjectSeq(toProjectSeq);
            item.setDateUpdated(now);
            this._pupillometryRepository.updateSubjectProjectSeq(item);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void doLabelling(String systemId, String subjectId, long projectSeq) throws Exception {
        List<Pupillometry> result = this._pupillometryRepository.findByProjectIdAndTrialIndex(systemId, subjectId);
        if(result==null || result.isEmpty()) return;
        
        for(Pupillometry item : result) {
            if(item.getProjectSeq()==projectSeq) continue;
            LocalDateTime now = LocalDateTime.now();
            item.setProjectSeq(projectSeq);
            item.setDateUpdated(now);
            this._pupillometryRepository.updateSubjectProjectSeq(item);
        }
    }
}
