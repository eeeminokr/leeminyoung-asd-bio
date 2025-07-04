package com.ecoinsight.bdsp.asd.scheduling;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.FnirsResource;
import com.ecoinsight.bdsp.asd.model.Pupillometry;
import com.ecoinsight.bdsp.asd.repository.IFnirsResourceRepository;
import com.ecoinsight.bdsp.asd.repository.IPupillometryRepository;
import com.ecoinsight.bdsp.asd.service.DataCommonService;
import com.ecoinsight.bdsp.core.util.DateUtil;

@Component
public class FnirsResourceUploadBatch extends AsdBaseScheduler {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private String _hostname;

    @Value("${ecoinsight.object-storage.endpoint}")
    private String _endpoint;
    @Value("${ecoinsight.object-storage.region}")
    private String _region;
    @Value("${ecoinsight.object-storage.access-key}")
    private String _accessKey;
    @Value("${ecoinsight.object-storage.secret-key}")
    private String _secretKey;
    @Value("${ecoinsight.object-storage.fnirs.internal}")
    private String _objectStoageInternal;
    @Value("${ecoinsight.object-storage.to.skt}")
    private String _objectStoageToSkt;
    @Value("${ecoinsight.object-storage.to.eco}")
    private String _objectStoageToEco;

    @Value("${ecoinsight.fnirs.crypto.output-dir}")
    private String _outputDir;
    @Value("${ecoinsight.fnirs.upload-batch-enabled}")
    private Boolean enabled;

    @Autowired
    protected IFnirsResourceRepository _fNIRsResourceRepository;
    @Autowired
    private DataCommonService _dataCommonService;

    @PostConstruct
    public void startup() throws UnknownHostException {
        this.taskName = "FnirsResourceUploadBatch";
        this._hostname = InetAddress.getLocalHost().getHostName();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> startup hostname=" + this._hostname);
        }
    }

    @Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 60)
    @Override
    public void doPerform() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("->FnirsResourceUploadBatch.doPerform.... enabled=" + this.enabled);
        }

        if (this.enabled == null || !this.enabled)
            return;

        final AWSCredentials credentials = new BasicAWSCredentials(this._accessKey, this._secretKey);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(this._endpoint, this._region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        if (!s3.doesBucketExistV2(this._objectStoageInternal)) {
            throw new RuntimeException(
                    "Cannot find object storage in the ncloud. object storage=" + this._objectStoageToEco);
        }

        // String folder = DateUtil.getCurrentDate();
        // if(s3.doesObjectExist(this._objectStoage, folder)) {
        // ObjectMetadata meta = new ObjectMetadata();
        // meta.setContentLength(0L);
        // meta.setContentType("application/x-directory");
        // PutObjectRequest objRequest = new PutObjectRequest(this._objectStoage,
        // folder, new ByteArrayInputStream(new byte[0]), meta);
        // try {
        // s3.putObject(objRequest);
        // } catch(Exception e) {
        // throw new RuntimeException("Cannot create date folder in the object storage.
        // - object storage="+this._objectStoage+", error="+e.getMessage(), e);
        // }
        // }

        List<FnirsResource> resources = this._fNIRsResourceRepository.queryByPublished(this._hostname, Boolean.TRUE);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> queryByPublished. - result=" + resources.size());
        }
        boolean status = true;
        for (FnirsResource vr : resources) {
            String fileName = vr.getTargetFileName();
            File file = new File(this._outputDir, fileName);
            LOGGER.info("completed : {}", vr.isCompleted());
            if (!(file.exists()) && !(vr.isCompleted())) {
                status = false;
                vr.setFailed(true);
                vr.setPublished(false);
                vr.setPublishedError("Cannot find the target file from file system. - " + file.getAbsolutePath());
                vr.setDatePublished(LocalDateTime.now());
                this._fNIRsResourceRepository.changeAfterPublishedAndLabelled(vr);
                this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.FNIRS,
                        vr.getSubjectId(), vr.getProjectSeq(), vr.getTrialIndex(),
                        status);
                continue;
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Publlishing target file. - " + file.getAbsolutePath());
            }
            // publish uploaded file to internal(4) or skt(others) sotrage.
            LocalDateTime now = LocalDateTime.now();
            ZonedDateTime zdatetime = now.atZone(ZoneId.systemDefault());
            String folder = DateUtil.getCurrentDate(new Date(zdatetime.toInstant().toEpochMilli()));
            if (!(vr.isCompleted())) {

                try {
                    // // upload it to repository
                    // if (vr.getProjectSeq() == ProjectSeq.HOLD.getSeq()) {
                    // this.uploadFileToObjectStorage(s3, this._objectStoageInternal, folder, file);
                    // }
                    // else {
                    // this.uploadFileToObjectStorage(s3, this._objectStoageToSkt, folder, file);
                    // vr.setLabelled(true);
                    // vr.setDateLabelled(now);
                    // }
                    this.uploadFileToObjectStorage(s3, this._objectStoageInternal, folder, file);
                    vr.setFailed(false);
                    vr.setPublished(true);
                    vr.setCompleted(true);
                    vr.setDateCompleted(now);
                    status = true;
                    boolean result = file.delete();
                    if (!result) {
                        LOGGER.error("Fail to delete the file. - " + file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    vr.setFailed(true);
                    vr.setPublished(false);
                    vr.setPublishedError("Failed to publish. - " + e.getMessage());
                    status = false;
                } finally {
                    vr.setDatePublished(now);
                    this._fNIRsResourceRepository.changeAfterPublishedAndLabelled(vr);
                    this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.FNIRS,
                            vr.getSubjectId(), vr.getProjectSeq(), vr.getTrialIndex(),
                            status);
                }
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
}
