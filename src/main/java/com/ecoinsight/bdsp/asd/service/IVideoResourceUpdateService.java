package com.ecoinsight.bdsp.asd.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.ecoinsight.bdsp.asd.crypto.FileCryptoKeys;
import com.ecoinsight.bdsp.asd.entity.VideoResource;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.Result;
import com.ecoinsight.bdsp.asd.repository.IVideoResourceRepository;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.web.BaseApiController;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

@Transactional
@Service(IVideoResourceUpdateService.ID)
public class IVideoResourceUpdateService implements AsdSubjectService {
    public static final String ID = "IVideoResourceUpdateService";
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private static final String SYSTEM_ID = "ASD";
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

    @Autowired
    protected IVideoResourceRepository _videoResourceRepository;
    @Autowired
    private DataCommonService _dataCommonService;

    @Autowired
    private EyeTrackingService _EyeTrackingService;

    @Autowired
    private MChartService _MChartService;

    // 파일 이름에서 IF, 프로젝트 시퀀스, 시퀀스를 추출하는 정규 표현식
    private static final String FILENAME_PATTERN = "(IF\\d{4})_(\\d{1})_(\\d{1})";

    private static final String SKT_OBJECT_STORAGE = "SKT";

    private static final String INTERNAL_OBJECT_STORAGE = "INTERNAL";

    private static final String ECO_OBJECT_STORAGE = "ECO";

    // @PutMapping(path = "/api/v1/forced/video/result")
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void changeProjectSeq(String systemId, String subjectId, long fromProjectSeq, long toProjectSeq)
            throws Exception {
        // public ResponseEntity<JsonResponseObject> forcedVideoResourceUpdateBatch(
        // @RequestParam(required = false) String subjectId, @RequestParam(required =
        // false) Long fromProjectSeq,
        // @RequestParam(required = false) Long toProjectSeq) {

        // AWS S3 인증 정보 설정
        final AWSCredentials credentials = new BasicAWSCredentials(this._accessKey, this._secretKey);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(this._endpoint, this._region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        // 내부 및 외부 오브젝트 스토리지 버킷 확인
        validateBucketExistence(s3, this._objectStoageInternal);
        validateBucketExistence(s3, this._objectStoageToSkt);
        validateBucketExistence(s3, this._objectStoageToEco);

        // 프로젝트 시퀀스 다른경우 에 해당하는 비디오 자원 목록 가져오기 T.ProjectSeq != V.ProjectSeq 업데이트 전의
        // 대상자정보"
        List<VideoResource> videoResources = fetchVideoResources(subjectId, fromProjectSeq, toProjectSeq);

        // 대상자 정보를 반복 처리
        for (VideoResource videoResource : videoResources) {
            // 대상자 정보 갱신
            if (videoResource != null) {
                videoResource.setSubjectProjectSeq(toProjectSeq);
            } else {
                LOGGER.warn("VideoResource 객체가 null입니다.");
                continue;
            }

            // 파일 처리
            processFile(s3, videoResource);
        }

        Result result = new Result();
        result.setSuccess(true);
        // 처리된 비디오 자원 목록이 없는 경우 에러 응답
        if (videoResources.isEmpty()) {
            result.setSuccess(false);
            LOGGER.info("상호작용 리스트 대상자 정보가 없습니다.");
            return;
            // return errorResponse(result.isSuccess(), "상호작용 정보에서 labelled false 값 데이터를 찾을
            // 수 없습니다.", videoResources);
        }

        // // 정상적으로 처리된 경우 응답
        // return ResponseEntity.ok()
        // .body(new JsonResponseObject(result.isSuccess(), "비디오 자원이 성공적으로 처리되었습니다.",
        // videoResources));
    }

    // ErrorResponse 메소드 수정
    private ResponseEntity<JsonResponseObject> errorResponse(Boolean state, String message, Object data) {
        JsonResponseObject responseBody = new JsonResponseObject(state, message, data);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
    }

    // 내부 및 외부 오브젝트 스토리지 버킷 확인 메서드
    private void validateBucketExistence(AmazonS3 s3, String bucketName) {
        if (!s3.doesBucketExistV2(bucketName)) {
            throw new RuntimeException("Ncloud의 오브젝트 스토리지를 찾을 수 없습니다. 오브젝트 스토리지: " + bucketName);
        }
    }

    // 비디오 자원 가져오는 메서드
    private List<VideoResource> fetchVideoResources(String subjectId, Long fromProjectSeq, Long ToProjectSeq) {
        List<VideoResource> videoResourceList = new ArrayList<>();
        if (fromProjectSeq != null) {
            List<VideoResource> videoResources = new ArrayList<>();
            videoResources = _videoResourceRepository.findByproectSeqLists(fromProjectSeq, subjectId);

            if (!videoResources.isEmpty()) {
                for (VideoResource videoResource : videoResources) {
                    LOGGER.info("기존 대상자정보와 상호작용과 대상군이 상이하였습니다.대상자과제군={},상호작용 과제군={} ",
                            videoResource.getSubjectProjectSeq(), videoResource.getProjectSeq());
                }

            }

            videoResourceList = _videoResourceRepository.findByProjectSeq(SYSTEM_ID, subjectId, fromProjectSeq);
        }
        return videoResourceList;
    }

    private void processFile(AmazonS3 s3, VideoResource videoResource) {
        String fileName = videoResource.getTargetFileName();
        String objectName = getObjectStoragePath(videoResource, fileName);
        LOGGER.info("처리 중인 파일의 객체 이름 => {}", objectName);

        boolean objectExistsInSkt = checkObjectExistence(s3, this._objectStoageToSkt, objectName);

        if (!objectExistsInSkt) {
            objectName = getObjectStoragePahthByLabelledDate(videoResource, fileName);
            LOGGER.info("[ObjectExsitInskt]레이블된 객체 이름으로 변경됨 => {}", objectName);
            if (objectName == null) {
                // 레이블된 객체 이름이 null인 경우 다시 기본 객체 이름을 가져옴
                objectName = getObjectStoragePath(videoResource, fileName);
            } else {
                // 레이블된 객체 이름이 있을 경우 해당 객체가 존재하는지 확인
                objectExistsInSkt = checkObjectExistence(s3, this._objectStoageToSkt, objectName);
            }
        }

        if (objectExistsInSkt) {
            processObjectInSkt(s3, videoResource, fileName, objectName);
        } else {

            processObjectNotInSkt(s3, videoResource, fileName);
        }
    }

    // SKT 오브젝트 스토리지에 파일이 존재하는 경우 처리하는 메서드
    private void processObjectInSkt(AmazonS3 s3, VideoResource videoResource, String fileName, String objectName) {
        String[] parts = getParts(fileName);
        final String TO_SKT_OJBJECTSTORGE = "SKT";

        if (parts != null) {
            processParts(s3, videoResource, parts, fileName, objectName, TO_SKT_OJBJECTSTORGE);
        } else {
            processNoParts(s3, videoResource, fileName, objectName, TO_SKT_OJBJECTSTORGE);
        }
    }

    private void processObjectNotInSkt(AmazonS3 s3, VideoResource videoResource, String fileName) {
        String objectName = getObjectStoragePath(videoResource, fileName);

        // Internal Storage 체크
        boolean objectExistsInternal = checkObjectExistence(s3, this._objectStoageInternal, objectName);
        if (!objectExistsInternal) {
            LOGGER.info("[ObjectExsitInternal] 레이블된 객체 이름으로 변경됨 => {}", objectName);
            objectName = getObjectStoragePahthByLabelledDate(videoResource, fileName);
            if (objectName == null) {
                LOGGER.info("[ObjectExsitInternal] 레이블된 객체 이름 => null값");
                objectName = getObjectStoragePath(videoResource, fileName);
            } else {
                objectExistsInternal = checkObjectExistence(s3, this._objectStoageInternal, objectName);
            }
        }

        // ECO Storage 체크
        String objectNames = null;
        if (objectExistsInternal) {
            processInternalObject(s3, videoResource, fileName, objectName);
        } else {
            LOGGER.info("오브젝트 스토리지(" + this._objectStoageInternal + ")에서 파일을 찾을 수 없습니다. - objectName=" + objectName);
            objectNames = getConvertdeiedObjectStoragePath(videoResource, fileName, objectName);
        }

        boolean objectExistsEco = false;
        if (objectNames != null) {
            objectExistsEco = checkObjectExistence(s3, this._objectStoageToEco, objectNames);
        }
        if (!objectExistsEco) {
            LOGGER.info("[ObjectExsitECO] 레이블된 객체 이름으로 변경됨 => {}", objectName);
            objectName = getObjectStoragePahthByLabelledDate(videoResource, fileName);
            objectNames = getConvertdeiedObjectStoragePath(videoResource, fileName, objectName);
            if (objectName == null && objectNames == null) {
                LOGGER.info("[ObjectExsitInternal] 레이블된 객체 이름 => null값");
                objectName = getObjectStoragePath(videoResource, fileName);
                objectNames = getConvertdeiedObjectStoragePath(videoResource, fileName, objectName);
            } else {
                objectExistsEco = checkObjectExistence(s3, this._objectStoageToEco, objectNames);
            }
        }
        if (objectExistsEco) {
            processToEcoObject(s3, videoResource, fileName, objectName);
        } else {
            LOGGER.info("오브젝트 스토리지(" + this._objectStoageToEco + ")에서 파일을 찾을 수 없습니다. - objectName=" + objectNames);
        }
    }

    // 객체 존재 여부 확인 메소드
    private boolean checkObjectExistence(AmazonS3 s3, String storage, String objectName) {
        boolean objectExists = false;
        if (objectName != null) {
            objectExists = s3.doesObjectExist(storage, objectName);
        }
        return objectExists;
    }

    // 내부 오브젝트 스토리지에 파일이 존재하는 경우 처리하는 메서드
    private void processInternalObject(AmazonS3 s3, VideoResource videoResource, String fileName, String objectName) {
        String[] parts = getParts(fileName);
        final String TO_INTERNAL_OJBJECTSTORGE = "INTERNAL";
        if (parts != null) {
            processParts(s3, videoResource, parts, fileName, objectName, TO_INTERNAL_OJBJECTSTORGE);
        } else {
            processNoParts(s3, videoResource, fileName, objectName, TO_INTERNAL_OJBJECTSTORGE);
        }
    }

    private void processToEcoObject(AmazonS3 s3, VideoResource videoResource, String fileName, String objectName) {
        String[] parts = getParts(fileName);
        String objectNames = getConvertdeiedObjectStoragePath(videoResource, fileName, objectName);
        final String TO_ECO_OJBJECTSTORGE = "ECO"; // for deid_extension
        if (parts != null) {
            processParts(s3, videoResource, parts, fileName, objectNames, TO_ECO_OJBJECTSTORGE);
        } else {
            processNoParts(s3, videoResource, fileName, objectNames, TO_ECO_OJBJECTSTORGE);
        }
    }

    // 파일 이름에서 IF, 프로젝트 시퀀스, 시퀀스를 추출하는 메서드
    private String[] getParts(String filename) {
        Pattern pattern = Pattern.compile(FILENAME_PATTERN);
        Matcher matcher = pattern.matcher(filename);
        if (matcher.find()) {
            String[] parts = new String[3];
            parts[0] = matcher.group(1);
            parts[1] = matcher.group(2);
            parts[2] = matcher.group(3);
            return parts;
        } else {
            return null;
        }
    }

    // 처리할 파일 파트가 있는 경우 처리하는 메서드
    private void processParts(AmazonS3 s3, VideoResource videoResource, String[] parts, String fileName,
            String objectName, String objectStorage) {
        int partTwo = Integer.parseInt(parts[1]);
        int partThree = Integer.parseInt(parts[2]);
        String newFileName = processFileNameAndExtension(videoResource);
        LocalDateTime now = LocalDateTime.now();
        // 새 파일 이름 설정
        // if (partTwo != videoResource.getProjectSeq()) {
        // newFileName = processFileNameAndExtension(videoResource);
        // videoResource.setTargetFileName(newFileName);
        // videoResource.setProjectSeq(videoResource.getProjectSeq());
        // videoResource.setDateChangeProject(now);
        // videoResource.setOriginalFileName(fileName);
        // videoResource.setTargetFileName(newFileName);
        // changeProjectseq = true;

        // updateVideoResource(videoResource);

        // }

        if (partTwo != videoResource.getSubjectProjectSeq() || partTwo != videoResource.getProjectSeq()) {
            LOGGER.info("파일명과제군과 대상자정보과제군 일치하지 않습니다 파일명 과제군: " + partTwo + ", 대상자정보 과제군: "
                    + videoResource.getSubjectProjectSeq());

        }

        String newObjectName = getObjectStoragePath(videoResource, newFileName);
        boolean objectStorageException = false; // objectStorage 예외 여부를 저장하는 변수
        String whichObjectStorage = this._objectStoageToSkt;
        try {
            if (objectStorage.equals(SKT_OBJECT_STORAGE)) {

                if (s3.doesObjectExist(this._objectStoageToSkt, objectName)) {
                    if (!objectName.equals(newObjectName)) {
                        copyAndDeleteObject(s3, this._objectStoageToSkt, this._objectStoageToSkt, objectName,
                                newObjectName);
                    } else {
                        LOGGER.error("Cannot find the video object in the object storage. - " + objectName);

                    }

                }

            } else if (objectStorage.equals(INTERNAL_OBJECT_STORAGE)) {
                if (s3.doesObjectExist(this._objectStoageInternal, objectName)) {
                    copyAndDeleteObject(s3, this._objectStoageInternal, this._objectStoageToSkt, objectName,
                            newObjectName);
                }
                whichObjectStorage = this._objectStoageInternal;
            } else if (objectStorage.equals(ECO_OBJECT_STORAGE)) {
                whichObjectStorage = this._objectStoageToEco;
                if (s3.doesObjectExist(this._objectStoageToEco, objectName)) {
                    newObjectName = getConvertdeiedObjectStoragePath(videoResource, newFileName, objectName);
                    if (!objectName.equals(newObjectName)) {
                        copyAndDeleteObject(s3, this._objectStoageToEco, this._objectStoageToEco, objectName,
                                newObjectName);
                    }
                }

            } else {
                LOGGER.error("오브젝트 스토리지(" + whichObjectStorage + ")"
                        + "에서 파일을 찾을 수 없습니다. - objectName=" + objectName);
            }
        } catch (Exception e) {
            objectStorageException = true; // objectStorage 예외 발생 시 플래그 설정
            LOGGER.error("오브젝트 스토리지[processParts](" + whichObjectStorage + ") "
                    + "의 오브젝트 이름을 업데이트하는 데 실패했습니다. - objectName=" + objectName + ", error=" + e.getMessage(), e);
        } finally {
            if (!objectStorageException) { // objectStorage 예외가 발생하지 않은 경우에만 업데이트 수행
                try {
                    if (videoResource.getProjectSeq() != videoResource.getSubjectProjectSeq()
                            || partTwo != videoResource.getSubjectProjectSeq()) {
                        LOGGER.info("파일명과제군과 대상자정보과제군 일치하지 않습니다 파일명 과제군: " + partTwo + ", 대상자정보 과제군: "
                                + videoResource.getProjectSeq() + "상호작용 과제군: "
                                + videoResource.getSubjectProjectSeq());

                        // changeProjectSeqs(SYSTEM_ID, videoResource.getSubjectId(),
                        // videoResource.getProjectSeq(), videoResource.getSubjectProjectSeq());

                        videoResource.setProjectSeq(videoResource.getSubjectProjectSeq());
                        videoResource.setDateChangeProject(now);
                        // videoResource.setDateChangeProject(now);
                    }

                    // if (!videoResource.isVideoResourceState()) {
                    // this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.VideoResource,
                    // videoResource.getSubjectId(), videoResource.getProjectSeq(),
                    // videoResource.getTrialIndex(), true);
                    // }
                    if (!videoResource.isLabelled()) {
                        videoResource.setLabelled(true);
                        videoResource.setDateLabelled(now);
                    }
                    videoResource.setOriginalFileName(fileName);
                    videoResource.setTargetFileName(newFileName);
                    updateVideoResourceLabelled(videoResource);

                } catch (Exception e) {
                    LOGGER.error("비디오 자원을 업데이트하는 데 실패했습니다. - " + e.getMessage(), e);
                }
            }
        }
    }

    // 처리할 파일 파트가 없는 경우 처리하는 메서드
    private void processNoParts(AmazonS3 s3, VideoResource videoResource, String fileName, String objectName,
            String objectStorage) {
        LOGGER.warn("파일 이름에서 IF, 프로젝트 시퀀스, 시퀀스를 찾을 수 없습니다: " + fileName);
        String converFileName = videoResource.getTargetFileName();
        String newFileName = processFileNameAndExtension(videoResource);
        LOGGER.info("기본 파일 이름을 사용합니다: {}", newFileName);
        String newObjectName = getObjectStoragePath(videoResource, newFileName);
        boolean objectStorageException = false; // objectStorage 예외 여부를 저장하는 변수
        String whichObjectStorage = this._objectStoageToSkt;
        LocalDateTime now = LocalDateTime.now();
        try {
            if (objectStorage.equals(SKT_OBJECT_STORAGE)) {

                if (s3.doesObjectExist(this._objectStoageToSkt, objectName)) {
                    if (!objectName.equals(newObjectName)) {
                        copyAndDeleteObject(s3, this._objectStoageToSkt, this._objectStoageToSkt, objectName,
                                newObjectName);
                    }

                }

            } else if (objectStorage.equals(INTERNAL_OBJECT_STORAGE)) {
                if (s3.doesObjectExist(this._objectStoageInternal, objectName)) {
                    copyAndDeleteObject(s3, this._objectStoageInternal, this._objectStoageToSkt, objectName,
                            newObjectName);
                }
                whichObjectStorage = this._objectStoageInternal;
            } else if (objectStorage.equals(ECO_OBJECT_STORAGE)) {
                whichObjectStorage = this._objectStoageToEco;
                if (s3.doesObjectExist(this._objectStoageToEco, objectName)) {
                    newObjectName = getConvertdeiedObjectStoragePath(videoResource, newFileName, objectName);
                    LOGGER.info("EXSIST[OBJECTNAME]={}", objectName);
                    LOGGER.info("EXSIST[NEW-OBJECTNAME]={}", newObjectName);
                    if (!objectName.equals(newObjectName)) {
                        copyAndDeleteObject(s3, this._objectStoageToEco, this._objectStoageToEco, objectName,
                                newObjectName);
                    }
                }

            } else {
                LOGGER.error("오브젝트 스토리지(" + whichObjectStorage + ")"
                        + "에서 파일을 찾을 수 없습니다. - objectName=" + objectName);
            }
        } catch (Exception e) {
            objectStorageException = true; // objectStorage 예외 발생 시 플래그 설정
            LOGGER.error("오브젝트 스토리지[Noparts](" + whichObjectStorage + ") "
                    + "의 오브젝트 이름을 업데이트하는 데 실패했습니다. - objectName=" + objectName + ", error=" + e.getMessage(), e);
        } finally {
            if (!objectStorageException) { // objectStorage 예외가 발생하지 않은 경우에만 업데이트 수행
                try {
                    if (videoResource.getProjectSeq() != videoResource.getSubjectProjectSeq()) {
                        LOGGER.info("파일명과제군과 대상자정보과제군 일치하지 않습니다 대상자정보 과제군  : " + videoResource.getProjectSeq()
                                + "상호작용 과제군: "
                                + videoResource.getSubjectProjectSeq());
                        // changeProjectSeqs(SYSTEM_ID, videoResource.getSubjectId(),
                        // videoResource.getProjectSeq(), videoResource.getSubjectProjectSeq());
                        videoResource.setProjectSeq(videoResource.getSubjectProjectSeq());
                        videoResource.setDateChangeProject(now);
                        // videoResource.setDateChangeProject(now);
                    }

                    // if (!videoResource.isVideoResourceState()) {
                    // this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.VideoResource,
                    // videoResource.getSubjectId(), videoResource.getProjectSeq(),
                    // videoResource.getTrialIndex(), true);
                    // }
                    if (!videoResource.isLabelled()) {
                        videoResource.setLabelled(true);
                        videoResource.setDateLabelled(now);
                    }
                    videoResource.setOriginalFileName(fileName);
                    videoResource.setTargetFileName(newFileName);
                    videoResource.setDateUpdated(now);
                    updateVideoResourceLabelled(videoResource);

                } catch (Exception e) {
                    LOGGER.error("비디오 자원을 업데이트하는 데 실패했습니다. - " + e.getMessage(), e);
                }
            }
        }
    }

    // 새 파일 이름 생성 메서드
    private String generateDefaultFileName(VideoResource videoResource) {
        return fname(videoResource.getInterfaceId(), videoResource.getProjectSeq(), videoResource.getTrialIndex(),
                videoResource.getSubjectId(), videoResource.getRetryIndex());
    }

    // 오브젝트 복사 및 삭제 메서드
    private void copyAndDeleteObject(AmazonS3 s3, String fromObjectStorage, String toObjectStorage, String objectName,
            String newObjectName) {
        s3.copyObject(fromObjectStorage, objectName, toObjectStorage, newObjectName);
        s3.deleteObject(fromObjectStorage, objectName);
    }

    private String getConvertdeiedObjectStoragePath(VideoResource videoResource, String fileName, String objectName) {
        String[] parts = objectName.split("/");
        String folder = parts[0];
        int idx = fileName.indexOf(".");
        String targetFileName = String.format("deid_%s", fileName);
        String outputFileName;

        if (targetFileName.endsWith(FileCryptoKeys.ENCRYPTED_FILE_SUFFIX)) {
            outputFileName = targetFileName.substring(0,
                    targetFileName.length() - FileCryptoKeys.ENCRYPTED_FILE_SUFFIX.length());
            LOGGER.info("OUTPUTFILENAME[suffix]={}", outputFileName);
        } else {
            targetFileName = String.format("deid_%s", fileName.substring(0, idx), fileName.substring(idx));
            outputFileName = targetFileName + FileCryptoKeys.ENCRYPTED_FILE_PREFIX;
            LOGGER.info("OUTPUTFILENAME[prefix]={}", outputFileName);
        }
        String objectNames = String.format("%s/%s", folder, outputFileName);

        LOGGER.info("objectNames={}", objectNames);
        return String.format("%s/%s", folder, outputFileName);
    }

    // 오브젝트 경로 생성 메서드
    private String getObjectStoragePath(VideoResource videoResource, String fileName) {
        LocalDateTime publishedDateTime = videoResource.getDatePublished();
        if (publishedDateTime == null) {
            return null;
        }

        String folder = DateUtil
                .getCurrentDate(Date.from(publishedDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        return String.format("%s/%s", folder, fileName);
    }

    private String getObjectStoragePahthByLabelledDate(VideoResource videoResource, String fileName) {
        LocalDateTime labelledDateTime = videoResource.getDateLabelled();
        if (!videoResource.isLabelled() || labelledDateTime == null) {
            return null;
        }

        String folder = DateUtil.getCurrentDate(Date.from(labelledDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        return String.format("%s/%s", folder, fileName);
    }

    private String generateNewFileName(VideoResource videoResource) {
        long projectSeq = videoResource.getProjectSeq();
        if (videoResource.getProjectSeq() != videoResource.getSubjectProjectSeq()) {
            LOGGER.info("상호작용 과제군과 대상자정보과제군 일치하지 않습니다 파일명 과제군: " + videoResource.getProjectSeq() + ", 대상자정보 과제군: "
                    + videoResource.getSubjectProjectSeq());
            projectSeq = videoResource.getSubjectProjectSeq();
        }
        return fname(videoResource.getInterfaceId(), projectSeq, videoResource.getTrialIndex(),
                videoResource.getSubjectId(), videoResource.getRetryIndex());
    }

    private String fname(String interfaceId, long projectSeq, int trialIndex, String subjectId, int retryIndex) {

        return String.format("%s_%s_%s_%s_%s", interfaceId, projectSeq, trialIndex, subjectId, retryIndex);
    }

    private String processFileNameAndExtension(VideoResource videoResource) {
        String fileName = videoResource.getTargetFileName();
        String origName = videoResource.getOriginalFileName();
        String origExtension = FilenameUtils.getExtension(origName);
        String targetExtension = null;

        int lastIdx = fileName.lastIndexOf(".");

        if (lastIdx == -1) {
            LOGGER.error("파일 이름에서 확장자를 찾을 수 없습니다: " + fileName);
            targetExtension = FileCryptoKeys.ENCRYPTED_FILE_EXTENSION; // 기본 처리
        } else {
            String translate = fileName.substring(0, lastIdx);
            targetExtension = FilenameUtils.getExtension(translate);
        }

        // 확장자 변경 로직
        origExtension = origExtension.equals(FileCryptoKeys.ENCRYPTED_FILE_EXTENSION)
                ? FileCryptoKeys.MP4_FILE_EXTENSION
                : FileCryptoKeys.MP4_FILE_EXTENSION;
        targetExtension = targetExtension.equals(FileCryptoKeys.MP4_FILE_EXTENSION)
                ? FileCryptoKeys.ENCRYPTED_FILE_EXTENSION
                : FileCryptoKeys.ENCRYPTED_FILE_EXTENSION;

        // 새 파일 이름 설정
        String newFileName = generateNewFileName(videoResource);
        String newFileNameExtension = newFileName + "." + origExtension + "." + targetExtension;

        // VideoResource에 새 파일 이름 설정
        videoResource.setTargetFileName(newFileNameExtension);

        // 에러가 발생했더라도 처리가 완료되도록 추가
        LOGGER.info("파일 이름과 확장자가 처리되었습니다. fileExtension={}", newFileNameExtension);

        return newFileNameExtension;
    }

    // VideoResource 업데이트 메서드
    private void updateVideoResource(VideoResource videoResource) {
        try {
            this._videoResourceRepository.updateProjectSeq(videoResource);
        } catch (Exception e) {
            LOGGER.error(
                    "Fail to update projectSeq of the VideoResource" + videoResource.getId() + ". - " + e.getMessage(),
                    e);
        }
    }

    // VideoResource 업데이트 메서드
    private void updateVideoResourceLabelled(VideoResource videoResource) {
        try {

            this._videoResourceRepository.changeAfterFileNameLabelled(videoResource);
        } catch (Exception e) {
            LOGGER.error(
                    "Fail to update labelled of the VideoResource" + videoResource.getId() + ". - " + e.getMessage(),
                    e);
        }
    }

    private void changeProjectSeqs(String systemId, String subjectId, long fromProjectSeq, long toProjectSeq)
            throws Exception {

        // if (fromProjectSeq == toProjectSeq)
        // return;

        _EyeTrackingService.changeProjectSeq(SYSTEM_ID, subjectId, fromProjectSeq, toProjectSeq);
        _MChartService.changeProjectSeq(SYSTEM_ID, subjectId, fromProjectSeq, toProjectSeq);
        // _SurveyStatusService.changeProjectSeq(systemId, subjectId, fromProjectSeq,
        // toProjectSeq);
        // _videoResourceService.changeProjectSeq(systemId, subjectId, fromProjectSeq,
        // toProjectSeq);
    }

    public void doLabelling(String systemId, String subjectId, long projectSeq) throws Exception {
        // TODO Auto-generated method stub

    }
}
