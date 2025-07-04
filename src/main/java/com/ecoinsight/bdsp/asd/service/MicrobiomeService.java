package com.ecoinsight.bdsp.asd.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.ecoinsight.bdsp.asd.crypto.FileCryptoKeys;
import com.ecoinsight.bdsp.asd.crypto.FileDecryptor;
import com.ecoinsight.bdsp.asd.entity.Microbiome;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.Pupillometry;
import com.ecoinsight.bdsp.asd.repository.IMocrobiomeRepository;
import com.ecoinsight.bdsp.asd.repository.IMocrobiomeRepository.MocrobiomeSqlProvider;
import com.ecoinsight.bdsp.core.util.DateUtil;

@Transactional
@Service(MicrobiomeService.ID)
public class MicrobiomeService implements AsdSubjectService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String ID = "MicrobiomeService";

    @Value("${ecoinsight.object-storage.endpoint}")
    private String _endpoint;
    @Value("${ecoinsight.object-storage.region}")
    private String _region;
    @Value("${ecoinsight.object-storage.access-key}")
    private String _accessKey;
    @Value("${ecoinsight.object-storage.secret-key}")
    private String _secretKey;
    @Value("${ecoinsight.object-storage.microbiome.internal}")
    private String _objectStoageInternal;

    @Value("${ecoinsight.microbiome.crypto.security-path}")
    private String _securityPath;
    @Value("${ecoinsight.microbiome.crypto.password}")
    private String _password;

    @Autowired
    private IMocrobiomeRepository _microbiomeRepository;
    @Autowired
    private DataCommonService _dataCommonService;

    @Transactional(propagation = Propagation.REQUIRED)
    public void publish(File uploadedFile, Microbiome item) {
        final AWSCredentials credentials = new BasicAWSCredentials(this._accessKey, this._secretKey);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(this._endpoint, this._region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        if (!s3.doesBucketExistV2(this._objectStoageInternal)) {
            throw new RuntimeException(
                    "Cannot find object storage in the ncloud. object storage=" + this._objectStoageInternal);
        }

        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zdatetime = now.atZone(ZoneId.systemDefault());
        String folder = DateUtil.getCurrentDate(new Date(zdatetime.toInstant().toEpochMilli()));
        boolean status = false;
        try {
            this.uploadFileToObjectStorage(s3, this._objectStoageInternal, folder, uploadedFile);
            item.setFailed(false);
            item.setPublished(true);
            item.setCompleted(true);
            item.setDateCompleted(now);
            status = true;
            boolean result = uploadedFile.delete();
            if (!result) {
                LOGGER.error("Fail to delete the file. - " + uploadedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            status = false;
            item.setFailed(true);
            item.setPublished(false);
            item.setPublishedError("Failed to publish. - " + e.getMessage());
        } finally {
            item.setDatePublished(now);
            boolean result = this._microbiomeRepository.changeAfterPublishedAndLabelled(item);
            if (result) {
                this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.MICROBIOME, item.getSubjectId(), item.getProjectSeq(), item.getTrialIndex(), status);
                LOGGER.info("Updated published microbiome data. - " + item);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void create(Microbiome microbiome) throws Exception {
        try {
            this._microbiomeRepository.add(microbiome);
        } catch (Exception e) {
            throw new Exception("Fail to create a microbiome resource in database. - " + e.getMessage(), e);
        }
    }

    public List<Microbiome> getMicrobiome(String systemId, String subjectId, long projectSeq, long subjectProjectSeq, int trialIndex) {
        List<Microbiome> list = this._microbiomeRepository.queryMicrobiomeResources(systemId, subjectId, projectSeq, trialIndex);
        boolean state = false;

        for (Microbiome item : list) {
            if (item.getProjectSeq() != subjectProjectSeq) {
                state = this._microbiomeRepository.updateSubjectProjectSeq(item);
            }
        }
        List<Microbiome> updateList = new ArrayList<>();
        if (state) {
            updateList = this._microbiomeRepository.queryMicrobiomeResources(systemId, subjectId, subjectProjectSeq, trialIndex);
        } else {
            updateList = list;
        }

        return updateList;
    }

    public boolean download(File downloadDir, String systemId, String subjectId, long projectSeq, int trialIndex) throws Exception {
        List<Microbiome> resources = this._microbiomeRepository.queryMicrobiomeResources(systemId, subjectId, projectSeq, trialIndex);
        if (resources == null || resources.size() == 0) {
            throw new Exception(String.format("업로드한 장내미생물 파일이 없습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s",
                    systemId, subjectId, projectSeq, trialIndex));
        }

        final AWSCredentials credentials = new BasicAWSCredentials(this._accessKey, this._secretKey);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(this._endpoint, this._region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        int downloadedFileCount = 0;
        for (Microbiome resource : resources) {
            LocalDateTime ptime = resource.getDatePublished();
            ZonedDateTime pzdatetime = ptime.atZone(ZoneId.systemDefault());
            String cpfolder = DateUtil.getCurrentDate(new Date(pzdatetime.toInstant().toEpochMilli()));

            String fname = resource.getTargetFileName();
            String targetFileName = fname;
            String objectPath = String.format("%s/%s", cpfolder, targetFileName);
            if (!s3.doesObjectExist(this._objectStoageInternal, objectPath)) {
                LOGGER.warn("Cannot find the microbiome object from the ncloud. - " + objectPath);
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
            if (targetFileName.endsWith(FileCryptoKeys.ENCRYPTED_FILE_SUFFIX)) {
                String outputFileName = targetFileName.substring(0, targetFileName.length() - FileCryptoKeys.ENCRYPTED_FILE_SUFFIX.length());
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

    private void uploadFileToObjectStorage(AmazonS3 s3, String objectStorage, String folder, File file)
            throws Exception {
        try {
            s3.putObject(objectStorage, folder + "/" + file.getName(), file);
        } catch (Exception e) {
            throw new Exception("Fail to upload a file to object storage. file=" + file.getAbsolutePath() + ", error="
                    + e.getMessage(), e);
        }
    }

    public List<Microbiome> findAll(String systemId, String subjectId, long projectSeq, int trialIndex, String orgId, String gender, int page, int offset) {

        List<Microbiome> list = this._microbiomeRepository.findAll(systemId, subjectId, projectSeq, trialIndex, orgId, gender, page, offset);

        return list;
    }

    public int countAll(String systemId, String subjectId, long projectSeq, int trialIndex, String orgId, String gender) {
        return this._microbiomeRepository.countAll(systemId, subjectId, projectSeq, trialIndex, orgId, gender);
    }

    @Override
    public void doLabelling(String systemId, String subjectId, long projectSeq) throws Exception {

    }

    @Override
    public void changeProjectSeq(String systemId, String subjectId, long fromProjectSeq, long toProjectSeq)
            throws Exception {

    }

}
