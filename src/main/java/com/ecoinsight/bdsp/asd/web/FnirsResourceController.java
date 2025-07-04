package com.ecoinsight.bdsp.asd.web;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3;
import com.ecoinsight.bdsp.asd.Constants;
import com.ecoinsight.bdsp.asd.crypto.FileCryptoKeys;
import com.ecoinsight.bdsp.asd.crypto.FileEncryptor;
import com.ecoinsight.bdsp.asd.image.CompressedFile;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.FnirsResource;
import com.ecoinsight.bdsp.asd.model.FnirsResourceResult;
import com.ecoinsight.bdsp.asd.model.Pupillometry;
import com.ecoinsight.bdsp.asd.model.Result;
import com.ecoinsight.bdsp.asd.model.SubjectState;
import com.ecoinsight.bdsp.asd.repository.IFnirsResourceRepository;
import com.ecoinsight.bdsp.asd.service.DataCommonService;
import com.ecoinsight.bdsp.asd.service.IFnirsResourceService;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.util.ZipUtil;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(path = "/api/v1/fnirsresource")
public class FnirsResourceController extends AsdBaseApiController {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private String _hostname;

    @Value("${ecoinsight.fnirs.crypto.password}")
    private String _secretKey;
    @Value("${ecoinsight.fnirs.crypto.security-path}")
    private String _securityPath;
    @Value("${ecoinsight.fnirs.crypto.output-dir}")
    private String _outputDir;
    @Value("${ecoinsight.fnirs.upload-batch-enabled}")
    private Boolean enabled;
    private File _outputDirFile;
    @Value("${ecoinsight.download-dir}")
    private String _downloadDir;

    private static final String SYSTEM_ID = "ASD";
    @Value("${ecoinsight.object-storage.endpoint}")
    private String _endpoint;
    @Value("${ecoinsight.object-storage.region}")
    private String _region;
    @Value("${ecoinsight.object-storage.access-key}")
    private String _accessKey;
    @Value("${ecoinsight.object-storage.secret-key}")
    private String _objSecretKey;

    @Value("${ecoinsight.object-storage.internal}")
    private String _objectStoageInternal;
    @Value("${ecoinsight.object-storage.to.skt}")
    private String _objectStoageToSkt;
    @Value("${ecoinsight.object-storage.to.eco}")
    private String _objectStoageToEco;

    @Resource(name = IFnirsResourceService.ID)
    @Autowired
    private IFnirsResourceService _service;
    @Autowired
    private IProjectRepository _projectRepository;
    @Autowired
    private DataCommonService _dataCommonService;

    @Autowired
    private IFnirsResourceRepository _fNIRsResourceRepository;

    @PostConstruct
    public void startup() throws UnknownHostException {
        this._outputDirFile = new File(this._outputDir);
        if (!this._outputDirFile.exists()) {
            this._outputDirFile.mkdirs();
        }
        this._hostname = InetAddress.getLocalHost().getHostName();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> startup hostname=" + this._hostname);
        }
    }

    private ResponseEntity<JsonResponseObject> uploadFiles(MultipartFile[] mfiles, String taskId, FnirsResource model) {
        List<MultipartFile> uploadFiles = new ArrayList<MultipartFile>();

        LOGGER.info("Started uploading files. count={}", mfiles == null ? 0 : mfiles.length);
        if (mfiles == null || mfiles.length <= 0) {
            return ErrorResponseEntity("처리할 영상파일이 없습니다.");
        }
        for (MultipartFile mfile : mfiles) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("-> upload File=" + mfile.getOriginalFilename());
            }
            String extension = FilenameUtils.getExtension(mfile.getOriginalFilename());
            if (!CompressedFile.isValidExtension(extension)) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Invalid file extension. Ignored file=" + mfile.getOriginalFilename());
                }
                continue;
            }

            if (mfile.getSize() == 0) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("The size of uploaded file is zero. Ignored file=" + mfile.getOriginalFilename());
                }
                continue;
            }
            uploadFiles.add(mfile);
        }
        if (uploadFiles.size() == 0) {
            return ErrorResponseEntity("There is no valid uploaded file. Please, check uploaded files.");
        }

        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();
        final int totalTask = 6; // 1: file upload, 2~6: processing imaging data

        try {
            int zipFileCount = 0;
            for (MultipartFile mfile : uploadFiles) {
                String fileName = mfile.getOriginalFilename();
                String extension = FilenameUtils.getExtension(fileName);
                String baseName = FilenameUtils.getBaseName(fileName);
                ++zipFileCount;
            }

            FnirsResourceResult result = new FnirsResourceResult();
            result.setSuccess(true);
            result.setMessage("일부 동공측정파일업로드가 실패되었습니다.");
            result.setFileCountProcess(zipFileCount);

            return ResponseEntity.ok(new JsonResponseObject(
                    result.isSuccess(), "일부 동공측정파일업로드가 실패되었습니다.", result));

            // return OkResponseEntity("영상파일 업로드를 완료했습니다.", zipFileCount);
        } catch (Exception e) {
            LOGGER.error("Fail to process uploaded images. - " + e.getMessage(), e);
            return ErrorResponseEntity("Fail to process uploaded images. - " + e.getMessage(), e);
        }

    }

    @ApiOperation(value = "대상자 동공측정 데이터 조회 ", notes = "대상자 동공측정 데이터 조회, 검색조건 출력")
    @GetMapping("/request/search")
    public ResponseEntity<JsonResponseObject> searchFnirsList(@RequestParam final Map<String, Object> params) {
        final String systemId = super.getSystemId();
        final String userName = super.getAuthenticatedUsername();

        int page = 1;
        int offset = 20;

        if (params.containsKey("page")) {
            try {
                page = Integer.parseInt(params.get("page").toString());
                if (page <= 0 || page > Integer.MAX_VALUE) {
                    page = 1;
                }
            } catch (NumberFormatException pex) {
                page = 1;
            }
        }
        if (params.containsKey("offset")) {
            try {
                offset = Integer.parseInt(params.get("offset").toString());
                if (offset <= 0 || offset > ListDataModel.MAX_ROW_COUNT) {
                    offset = ListDataModel.DEFAULT_ROW_COUNT;
                }
            } catch (NumberFormatException pex) {
                offset = 20;
            }
        }

        String subjectId = null;
        long projectSeq = 0;
        int trialIndex = 0;
        String orgId = null;
        String gender = null;
        boolean orgFiltered = false;
        int accessLevel = 0b0111111;
        if (params != null && !params.isEmpty()) {
            for (String k : params.keySet()) {
                Object v = params.get(k);

                if (k.equals("id") && v != null) {

                    projectSeq = Long.parseLong(v.toString());

                    String rolename = "";
                    // 과제에서 부여된 사용자 권한 조회
                    final Optional<ProjectMember> researcherOptional = this._projectRepository
                            .findResearcher(projectSeq, userName);
                    if (researcherOptional.isEmpty()) {
                        rolename = super.getHighestRole(userName).getRoleId();
                    } else {
                        rolename = researcherOptional.get().getRoleId();
                    }

                    accessLevel = super.calculateAccessLevel(rolename);
                }
                if (k.equals("subject") && v != null) {
                    // filterDoc.append("subjectId", v.toString());
                    // dim.setSubjectId(v.toString());
                    subjectId = v.toString();
                }
                if (k.equals("trialIndex") && v != null) {
                    trialIndex = Integer.parseInt(v.toString());
                }

                if (k.equals("org") && v != null) {
                    orgFiltered = true;
                    // filterDoc.append("orgId", v.toString());
                    // dim.setOrgId(v.toString());
                    orgId = v.toString();
                }
                if (params.containsKey("gender")) {
                    gender = params.get("gender").toString();
                }
            }

        }
        // Admin, Manager, QC User는 모든 연구기관의 데이터 조회 가능.
        if (!super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel) && !super.canQC(accessLevel)
                && !orgFiltered) {
            final String org = super.getOrgId();
            // filterDoc.append("orgId", org);
            orgId = org;
        }
        List<FnirsResource> list = this._fNIRsResourceRepository.findAll(systemId, subjectId, projectSeq, trialIndex,
                orgId, gender, page, offset);

        list.forEach(result -> {
            result.setUserName(userName);
            result.addAreaQcStatus(DataSummaryColumn.FNIRS.name(),
                    result.isCompleted() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);

        });

        List<FnirsResource> count = this._fNIRsResourceRepository.countAll(systemId, subjectId, projectSeq,
                trialIndex,
                orgId, gender, page, offset);
        LOGGER.info("size={}", count.size());
        // List<EyeTrackingInfo> count = _eyetrackingInfoRepository.countAll(projectSeq,
        // subjectId, orgId, gender);

        return OkResponseEntity("동공측정 데이터 수집 현황을 조회했습니다.", new ListDataModel<>(list, count.size(), page, offset));
    }

    @PostMapping("/upload/file/{taskId}")
    public ResponseEntity<JsonResponseObject> uploadFile(
            @RequestParam("file") MultipartFile mfile,
            @PathVariable("taskId") String taskId,
            @ModelAttribute FnirsResource model) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("-> upload File=" + mfile.getOriginalFilename() + ", " + model);
        }
        if (mfile == null) {
            return ErrorResponseEntity("Cannot find uploaded file. Please, select a uploading file");
        }
        MultipartFile[] mfiles = {mfile};
        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();
        String fileName = mfile.getOriginalFilename();
        String extension = FilenameUtils.getExtension(fileName);
        FnirsResourceResult result = new FnirsResourceResult();
        result.setFileName(fileName);
        LOGGER.info("filename={}", fileName);
        // String fileName = mfile.getOriginalFilename();
        int idx = fileName.indexOf("_");
        String targetFileName = String.format(fileName.substring(0, idx));
        if (idx != -1) {
            LOGGER.info("추출된 filename subjectId : {}", targetFileName);
            if (!targetFileName.equals(model.getSubjectId())) {
                return ErrorResponseEntity(
                        String.format(
                                "you must check subjectId. diffrent filename SubjectId and selected subjectId filename={},subjectId={}",
                                targetFileName, model.getSubjectId()));
            }
        } else {
            LOGGER.info("파일 이름에 '_'가 없습니다.");
            return ErrorResponseEntity("Cannot find name subjectId  '_' file.");
        }

        var subjects = this._subjectRepository.findAllBySubjectId(systemId, model.getSubjectId());

        if (subjects == null || subjects.size() <= 0) {
            return ErrorResponse(
                    String.format("대상자 정보에서 연구대상자를 찾을수 없습니다.\n대상자정보를 확인해주세요. \n- systemId=%s, subjectId=%s", systemId,
                            model.getSubjectId()));
        }

        if (subjects.size() > 1) {
            return ErrorResponse(String.format("대상자 정보가 두개 이상의 과제에 등록되어 있습니다. - systemId=%s, subjectId=%s", systemId,
                    model.getSubjectId()));
        }

        AsdProjectSubject subject = subjects.stream().findFirst().get();
        // subject state validation
        if (SubjectState.InformedConsentSigned != subject.getState()
                && SubjectState.EnrolledActive != subject.getState()) {
            return ErrorResponse(
                    String.format("영상을 업로드하기 위한 대상자 상태 정보를 먼저 확인해 주세요. - systemId=%s, subjectId=%s, state=%s", systemId,
                            model.getSubjectId(), subject.getState()));
        }
        // startDate and endDate validation
        Optional<LocalDate> soptional = DateUtil.parseSimple(subject.getStartDate());
        Optional<LocalDate> eoptional = DateUtil.parseSimple(subject.getEndDate());
        if (soptional.isEmpty() || eoptional.isEmpty()) {
            return ErrorResponse(String.format(
                    "대상자 프로젝트 과제 차수의 시작일 혹은 종료일이 설정되지 안았습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, period=%s ~ %s",
                    systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(),
                    subject.getStartDate(), subject.getEndDate()));
        }
        LocalDate startDate = soptional.get();
        LocalDate endDate = eoptional.get();

        LOGGER.info("subjectId={},projectSeq={},trialIndex={}", model.getSubjectId(), model.getProjectSeq(),
                model.getTrialIndex());

        long projectSeq = subject.getProjectSeq();
        List<FnirsResource> resources = this._service.getFnirsResource(systemId, model.getSubjectId(),
                model.getProjectSeq(), subject.getProjectSeq(), model.getTrialIndex());
        if (resources.size() > 0) {
            for (FnirsResource fnirs : resources) {

                if (fnirs.isPublished()) {
                    return ErrorResponse(String.format(
                            "현재  대상자의 동공측정 업로드 중이므로 더이상 업로드를 할수 없습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s,  uploadedFiles=%s",
                            systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(),
                            resources.size()));
                }

                if (fnirs.isCompleted()) {
                    return ErrorResponse(String.format(
                            "현재  대상자의 동공측정 업로드가 이미 완료 되어서 더이상 업로드를 할수 없습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, interfaceId=%s, uploadedFiles=%s",
                            systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(),
                            resources.size()));
                }
                projectSeq = fnirs.getProjectSeq();
            }

        }

        int trialIndex = model.getTrialIndex();
        String outputFile = this.fname("fNIRS", projectSeq, trialIndex, model.getSubjectId())
                + "." + extension + "." + FileCryptoKeys.ENCRYPTED_FILE_EXTENSION;
        File encryptedFile = new File(this._outputDirFile, outputFile);

        LOGGER.info("OUTFILE={}", outputFile);

        FnirsResource fnirs = new FnirsResource();

        fnirs.setSystemId(systemId);
        fnirs.setOrgId(subject.getOrgId());
        fnirs.setProjectSeq(projectSeq);
        fnirs.setSubjectId(model.getSubjectId());
        fnirs.setTrialIndex(trialIndex);
        fnirs.setOriginalFileName(mfile.getOriginalFilename());
        fnirs.setTargetFileName(outputFile);
        fnirs.setDateCreated(LocalDateTime.now());
        fnirs.setDateUpdated(LocalDateTime.now());
        fnirs.setUserCreated(worker);
        fnirs.setUserUpdated(worker);
        fnirs.setHostname(this._hostname);
        fnirs.setDeleted(false);

        try {
            InputStream instream = mfile.getInputStream();
            FileEncryptor fencryptor = new FileEncryptor(this._secretKey);
            fencryptor.setSecurityPath(this._securityPath);
            if (!fencryptor.existsSaltFile()) {
                String saltFile = fencryptor.createSaltFile();
                LOGGER.info("The saltFile is created. - " + saltFile);
            }
            fencryptor.loadSaltFile();
            fencryptor.encrypt(instream, encryptedFile);
        } catch (Exception e) {
            return ErrorResponse(String.format(
                    "업로드 파일을 암호화하는데 에러가 발생했습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, interfaceId=%s",
                    systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex()), e);
        }

        try {

            // result.setSuccess(true);
            // result.setMessage("동공측정 파일을 업로드 했습니다.");
            ResponseEntity<JsonResponseObject> resulst = this.uploadFiles(mfiles, taskId, model);
            // resulst.getBody().getMessage();
            // resulst.getBody().getData();
            // resulst.getStatusCode();
            if (resulst.getStatusCode() == HttpStatus.OK) {
                LOGGER.info("messege={},data={}", resulst.getBody().getMessage(), resulst.getBody().getData());
                fnirs.setPublished(true);
                this._service.create(subject, fnirs);
                result.setSuccess(true);
                result.setMessage("동공측정 파일을 업로드 했습니다.");
            }

            // return this.uploadFiles(mfiles, taskId, model);
            return ResponseEntity.ok(new JsonResponseObject(
                    result.isSuccess(), result.isSuccess() ? "동공측정파일 업로드를 완료했습니다." : "일부 동공측정파일업로드가 실패되었습니다.",
                    result));
        } catch (Exception e) {
            LOGGER.error("Fail to process uploaded fnirsillometries. - " + e.getMessage(), e);
            return ErrorResponse(String.format(
                    "업로드 영상 파일 정보를 저장하는데 에러가 발생했습니다. 관리자에게 문의하십시요. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, error=%s",
                    systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(),
                    e.getMessage()), e);
        }

    }

    @ApiOperation(value = "대상자 비식별화된 동공측정 파일 다운로드", notes = "대상자 비식별화된 동공측정 파일 다운로드")
    @GetMapping("/download/{subjectId}/{projectSeq}/{trialIndex}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable String subjectId,
            @PathVariable long projectSeq, @PathVariable int trialIndex) throws Exception {
        final String systemId = super.getSystemId();
        var subjects = this._subjectRepository.findAllBySubjectId(systemId, subjectId);
        if (subjects == null || subjects.size() <= 0) {
            throw new Exception(String.format("대상자 정보를 찾을수 없습니다. - systemId=%s, subjectId=%s", systemId, subjectId));
        }

        if (subjects.size() > 1) {
            throw new Exception(
                    String.format("대상자 정보가 두개 이상의 과제에 등록되어 있습니다. - systemId=%s, subjectId=%s", systemId, subjectId));
        }

        AsdProjectSubject subject = subjects.stream().findFirst().get();
        // subject state validation
        if (SubjectState.InformedConsentSigned != subject.getState()
                && SubjectState.EnrolledActive != subject.getState()) {
            throw new Exception(String.format("대상자 상태 정보를 먼저 확인해 주세요. - systemId=%s, subjectId=%s, state=%s", systemId,
                    subjectId, subject.getState()));
        }

        // create a root download directory
        File downloadDir = new File(this._downloadDir);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        // create a subject video directory for download
        String targetDir = String.format("fNIRSressource_downloads");
        File ftargetDir = new File(downloadDir, targetDir);
        if (!ftargetDir.exists()) {
            ftargetDir.mkdirs();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("created a download directory. - " + ftargetDir.getAbsolutePath());
            }
        }

        boolean result = this._service.download(ftargetDir, systemId, subjectId, projectSeq, trialIndex);
        if (!result) {
            throw new Exception("다운로드 할 수 있는 비식별화 파일이 없습니다.");
        }

        File zipFile = ZipUtil.makeZip(downloadDir, targetDir);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("created a zip file. - " + zipFile.getAbsolutePath());
        }
        ftargetDir.delete();

        FileSystemResource resource = new FileSystemResource(zipFile);
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zipFile.getName());
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    private String fname(String fnirsResource, long projectSeq, int trialIndex, String subjectId) {
        return String.format("%s_%s_%s_%s", fnirsResource, projectSeq, trialIndex, subjectId);
    }

    // 내부 및 외부 오브젝트 스토리지 버킷 확인 메서드
    private void validateBucketExistence(AmazonS3 s3, String bucketName) {
        if (!s3.doesBucketExistV2(bucketName)) {
            throw new RuntimeException("Ncloud의 오브젝트 스토리지를 찾을 수 없습니다. 오브젝트 스토리지: " + bucketName);
        }
    }

    @ApiOperation(value = "대상자 fNIRS 데이터 삭제 요청 API", notes = "대상자 fNIRS 데이터 옵션 체크후 삭제처리.")
    @PostMapping("/{projectSeq}/{trialIndex}/remove/{subjectId}/")
    public ResponseEntity<JsonResponseObject> deleteFnirsResourceData(
            @PathVariable("subjectId") String subjectId,
            @PathVariable("projectSeq") long projectSeq,
            @PathVariable("trialIndex") int trialIndex,
            @ModelAttribute FnirsResource model) {

        final String systemId = getSystemId();
        final String userName = getAuthenticatedUsername();

        LOGGER.info("subjectId={}, projectSeq={}, trialIndex={}", subjectId, projectSeq, trialIndex);
        // LOGGER.info("USERNAME: {}", userName);

        Result result = new Result();
        var subjects = this._subjectRepository.findAllBySubjectId(systemId, model.getSubjectId());
        if (subjects == null || subjects.size() <= 0) {
            return ErrorResponse(
                    String.format("대상자 정보를 찾을수 없습니다. - systemId=%s, subjectId=%s", systemId, model.getSubjectId()));
        }

        try {
            AsdProjectSubject subject = subjects.stream().findFirst().get();
            List<FnirsResource> resources = this._service.getFnirsResource(systemId, subjectId, projectSeq, subject.getProjectSeq(), trialIndex);
            if (resources != null && !resources.isEmpty()) {
                this._service.delete(resources, model, userName);
                model.setUserName(userName);
                result.setSuccess(true);
                result.setMessage("대상자 시선추적 데이터 삭제를 완료하였습니다.");
            } else {
                result.setSuccess(false);
                result.setMessage("해당 데이터를 찾을 수 없습니다.");
                LOGGER.warn("No pupillometry data found for subjectId={}, projectSeq={}, trialIndex={}", subjectId,
                        projectSeq, trialIndex);
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(String.format(
                    "대상자 삭제 시 에러가 발생했습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s",
                    systemId, subjectId, projectSeq, trialIndex));
            LOGGER.error("An error occurred while processing pupillometry data deletion.", e);
        }

        return ResponseEntity.ok(new JsonResponseObject(result.isSuccess(), result.getMessage(), model));
    }

}
