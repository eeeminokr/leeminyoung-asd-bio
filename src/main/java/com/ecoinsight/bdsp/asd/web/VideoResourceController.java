package com.ecoinsight.bdsp.asd.web;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecoinsight.bdsp.asd.Constants;
import com.ecoinsight.bdsp.asd.crypto.FileCryptoKeys;
import com.ecoinsight.bdsp.asd.crypto.FileEncryptor;
import com.ecoinsight.bdsp.asd.entity.VideoResource;
import com.ecoinsight.bdsp.asd.entity.VideoReupload;
import com.ecoinsight.bdsp.asd.entity.VideoResource.InterfaceIdone;
import com.ecoinsight.bdsp.asd.entity.VideoResource.InterfaceIdthree;
import com.ecoinsight.bdsp.asd.entity.VideoResource.InterfaceIdtwo;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.SubjectState;
import com.ecoinsight.bdsp.asd.model.VideoModel;
import com.ecoinsight.bdsp.asd.model.VideoResult;
import com.ecoinsight.bdsp.asd.model.VideoReuploadListModel;
import com.ecoinsight.bdsp.asd.model.VideoReuploadState;
import com.ecoinsight.bdsp.asd.repository.IVideoResourceRepository;
import com.ecoinsight.bdsp.asd.model.VideoReuploadModel;
import com.ecoinsight.bdsp.asd.service.DataCommonService;
import com.ecoinsight.bdsp.asd.service.VideoResourceService;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.service.ServiceException;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.util.ZipUtil;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(path = "/api/v1/video")
public class VideoResourceController extends AsdBaseApiController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private String _hostname;
    @Value("${ecoinsight.video.crypto.password}")
    private String _secretKey;
    @Value("${ecoinsight.video.crypto.security-path}")
    private String _securityPath;
    @Value("${ecoinsight.video.crypto.output-dir}")
    private String _outputDir;
    private File _outputDirFile;
    @Value("${ecoinsight.download-dir}")
    private String _downloadDir;

    @Resource(name = VideoResourceService.ID)
    private VideoResourceService _service;

    @Autowired
    private IVideoResourceRepository _videoResourceRepository;
    @Autowired
    private IProjectRepository _projectRepository;
    @Autowired
    private DataCommonService _dataCommonService;

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

    /**
     * Not supported.
     * 
     * @param mfiles
     * @param model
     * @return
     */
    @ApiOperation(value = "상호작용 영상데이터 업로드 API ", notes = " 대상자의 상호작용 영상 데이터를 암호화 후 ObjectStorage에 저장함")
    @PostMapping("/upload/file")
    public ResponseEntity<JsonResponseObject> uploadFile(@RequestPart("file") MultipartFile mfile,
            @ModelAttribute VideoModel model) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> uploadFile - " + mfile.getOriginalFilename() + ", " + model);
        }
        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();
        String fileName = mfile.getOriginalFilename();
        String extension = FilenameUtils.getExtension(fileName);

        VideoResult result = new VideoResult();
        result.setFileName(fileName);
        IFVideo ifid = null;
        try {
            // interfaceId validation
            ifid = IFVideo.valueOf(model.getInterfaceId());
        } catch (Exception e) {
            return ErrorResponse(String.format("인터페이스 ID가 잘못되었습니다. - subjectId=%s, interfaceId=%s, ",
                    model.getSubjectId(), model.getInterfaceId()), e);
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
        // Interface 별 age Validation
        try {
            String birthday = subject.getBirthDay();
            String registDate = subject.getRegistDate(); // LocalDate.from(subject.getDateTrialIndex());
            boolean validAge = ifid.validate(registDate, birthday);
            if (!validAge) {
                return ErrorResponse(
                        String.format("영상 업로드를 위한 연령을 확인해 주세요. - systemId=%s, subjectId=%s, valid age=(%s ~ %s) months",
                                systemId, model.getSubjectId(), ifid.getMinimum(), ifid.getMaximum()));
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Invlid birthday of this subject. SystemId=%s, SubjectId=%s, Birthday=%s",
                    systemId, model.getSubjectId(), subject.getBirthDay()));
            result.setSuccess(false);
            result.setMessage(String.format("등록된 대상자의 생년월일 포맷이 잘못되었습니다. - systemId=%s, subjectId=%s, birthday=%s",
                    systemId, model.getSubjectId(), subject.getBirthDay()));
            return ResponseEntity.ok(new JsonResponseObject(result.isSuccess(), result.getMessage(), result));
        }

        long projectSeq = model.getProjectSeq();
        int trialIndex = model.getTrialIndex();
        // long projectSeq = subject.getProjectSeq();
        // int trailIndex = this.getTrialIndex(systemId, model, ifid);

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

        // 재찰영 요청 처리
        Optional<VideoReupload> optional = this._service.getActiveVideoReupload(systemId, model.getSubjectId(),
                projectSeq, trialIndex, ifid.name());
        VideoReupload reupload = null;
        if (optional.isPresent()) {
            reupload = optional.get();
        }
        if (reupload != null && VideoReuploadState.PUBLISHED.equals(reupload.getState())
                && ifid.equals(reupload.getInterfaceId())) {
            projectSeq = reupload.getProjectSeq();
            trialIndex = reupload.getTrialIndex();
            // 종료일 : 종료일은 요청일로부터 6개월 - need to confirm
            LocalDate reuploadReqDate = LocalDate.from(reupload.getDateCreated());
            endDate = reuploadReqDate.plusMonths(6);
        }

        // 재촬영일 경우 : 재촬영 요청의 projectSeq, trialIndex를 사용하고, 요청일로 부터 6개월 내 영상을 업로드 해야 한다.
        // projectSeq validation with subject
        if (projectSeq != subject.getProjectSeq()) {
            return ErrorResponse(String.format("대상자 프로젝트 과제 순번이 일치하지 않습니다. - systemId=%s, subjectId=%s, projectSeq=%s",
                    systemId, model.getSubjectId(), subject.getProjectSeq()));
        }
        // trialIndex validation with subject
        if (trialIndex != model.getTrialIndex()) {
            return ErrorResponse(String.format(
                    "대상자 프로젝트 과제 차수가 일치하지 않습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s", systemId,
                    model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex()));
        }
        // 영상 업로드 기간 확인(차수 시작/종료일내)
        // LocalDate trailIndexDate = LocalDate.from(subject.getDateTrialIndex());
        // if (trailIndexDate.isBefore(startDate) || trailIndexDate.isAfter(endDate)) {
        // return ErrorResponse(String.format(
        // "대상자 프로젝트 과제 차수 기간이 아닙니다. 시작일과 종료일을 확인해 주세요. - systemId=%s, subjectId=%s,
        // projectSeq=%s, trialIndex=%s, period=%s ~ %s",
        // systemId, model.getSubjectId(), subject.getProjectSeq(),
        // subject.getTrialIndex(),
        // subject.getStartDate(), subject.getEndDate()));
        // }

        // approvedExtended
        if (trialIndex == 3 || (!subject.isApprovedExtended() && trialIndex > 4)) {
            return ErrorResponse(
                    String.format("업로드한 영상의 차수 정보가 잘못되었습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s",
                            systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex()));
        }
        // check if it was already uploaded. systemId, subjectId, projectSeq,
        // trialIndex, interfaceId order by RetryIndex
        List<VideoResource> resources = this._service.getVideoResources(systemId, model.getSubjectId(),
                model.getProjectSeq(), model.getTrialIndex(), ifid.name());
        if (resources.size() > 0) {
            VideoResource first = resources.get(0);
            if (first.getRetryIndex() == 0 || resources.size() == 3) {
                return ErrorResponse(String.format(
                        "현재 차수에 대상자의 영상 업로드가 이미 완료 되어서 더이상 업로드를 할수 없습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, interfaceId=%s, uploadedFiles=%s",
                        systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(), ifid.name(),
                        resources.size()));
            }

            // need to check duplicated retryIndex
            VideoResource last = resources.get(resources.size() - 1);
            if (last.getRetryIndex() == model.getRetryIndex()) {
                return ErrorResponse(String.format(
                        "대상자의 재촬영 영상이 이미 업로드되었습니다. Invalid RetryIndex - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, interfaceId=%s, retryIndex=%s",
                        systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(), ifid.name(),
                        model.getRetryIndex()));
            }
        }

        // String id = this.id(model.getProjectSeq(), model.getTrialIndex(),
        // subject.getOrgId(), model.getSubjectId(), systemId);
        // file name =
        // {InterfaceId}_{ProjectSeq}_{TrialIndex}_{SubjectId}_{RetryIndex}.{extension}.enc
        String outputFile = this.fname(model.getInterfaceId(), projectSeq, trialIndex, model.getSubjectId(),
                model.getRetryIndex()) + "." + extension + "." + FileCryptoKeys.ENCRYPTED_FILE_EXTENSION;
        File encryptedFile = new File(this._outputDirFile, outputFile);

        VideoResource vr = new VideoResource();
        vr.setSystemId(systemId);
        vr.setOrgId(subject.getOrgId());
        vr.setProjectSeq(projectSeq);
        vr.setSubjectId(model.getSubjectId());
        vr.setTrialIndex(trialIndex);
        vr.setInterfaceId(model.getInterfaceId());
        vr.setAnsweredYn(model.getAnsweredYn());
        vr.setOriginalFileName(mfile.getOriginalFilename());
        vr.setTargetFileName(outputFile);
        vr.setDateCreated(LocalDateTime.now());
        vr.setUserCreated(worker);
        vr.setDateUpdated(LocalDateTime.now());
        vr.setUserUpdated(worker);
        vr.setHostname(this._hostname);
        vr.setRetryIndex(model.getRetryIndex());
        vr.setDeleted(false); // insert deleted=0
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
                    systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(), ifid.name()), e);
        }

        try {
            this._service.create(subject, vr, reupload);
            result.setSuccess(true);
            result.setMessage("영상 파일을 업로드 했습니다.");
            return ResponseEntity.ok(new JsonResponseObject(
                    result.isSuccess(), result.isSuccess() ? "영상파일 업로드를 완료했습니다." : "일부 영상파일 업로드가 실패되었습니다.", result));
        } catch (Exception e) {
            return ErrorResponse(String.format(
                    "업로드 영상 파일 정보를 저장하는데 에러가 발생했습니다. 관리자에게 문의하십시요. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, interfaceId=%s, error=%s",
                    systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(), ifid.name(),
                    e.getMessage()), e);
        }
    }

    @ApiOperation(value = "대상자 상호작용 영상데이터 재업로드 요청 API ", notes = "대상자 상호작용 영상 재촬영 요청을 등록함.")
    @PostMapping("/request/reupload")
    public ResponseEntity<JsonResponseObject> requestVideoReupload(@RequestBody VideoReuploadModel model)
            throws Exception {
        if (model.getSubjectId() == null || "".equals(model.getSubjectId())) {
            return ErrorResponse(String.format("대상자 ID는 필수 입력항목 입니다. - subjectId=%s", model.getSubjectId()));
        }
        if (model.getProjectSeq() <= 0) {
            return ErrorResponse(String.format("과제 순번은 필수 입력항목 입니다. - projectSeq=%s", model.getProjectSeq()));
        }
        // if (model.getTrialIndex() <= 0 || model.getTrialIndex() == 3) {
        if (model.getTrialIndex() <= 0) {
            return ErrorResponse(String.format("과제 차수는 필수 입력항목 입니다. - trailIndex=%s", model.getTrialIndex()));
        }
        IFVideo ifid = null;
        try {
            // interfaceId validation
            ifid = IFVideo.valueOf(model.getInterfaceId());
        } catch (Exception e) {
            return ErrorResponse(String.format("인터페이스 ID가 잘못되었습니다. - subjectId=%s, interfaceId=%s, ",
                    model.getSubjectId(), model.getInterfaceId()), e);
        }

        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();

        var subjects = this._subjectRepository.findAllBySubjectId(systemId, model.getSubjectId());
        if (subjects == null || subjects.size() <= 0) {
            return ErrorResponse(
                    String.format("대상자 정보를 찾을수 없습니다. - systemId=%s, subjectId=%s", systemId, model.getSubjectId()));
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

        List<VideoResource> resources = this._service.getVideoResources(systemId, model.getSubjectId(),
                model.getProjectSeq(), model.getTrialIndex(), ifid.name());
        if (resources != null && resources.size() > 0) {
            try {
                this._service.delete(resources, model, worker);
            } catch (ServiceException e) {
                throw new Exception("Fail to handle data delete =1. - ", e);
            } // insert deleted=1

        } else { // TODO : Exception?

        }

        Optional<VideoReupload> optional = this._service.getActiveVideoReupload(systemId, model.getSubjectId(),
                model.getProjectSeq(), model.getTrialIndex(), ifid.name());
        if (optional.isPresent()) {
            VideoReupload vr = optional.get();
            if (vr != null) {
                return ErrorResponse(String.format(
                        "현재 재촬영 요청이 진행중입니다. - subjectId=%s, projectSeq=%s, trailIndex=%s, interfaceId=%s",
                        model.getSubjectId(), model.getProjectSeq(), model.getTrialIndex(), model.getInterfaceId()));
            }
        }

        // insert video reupload request
        VideoReupload vr = new VideoReupload();
        vr.setSystemId(systemId);
        vr.setOrgId(subject.getOrgId());
        vr.setSubjectId(model.getSubjectId());
        vr.setProjectSeq(model.getProjectSeq());
        vr.setTrialIndex(model.getTrialIndex());
        vr.setInterfaceId(model.getInterfaceId());
        vr.setState(VideoReuploadState.REQUESTED.name());
        vr.setDateCreated(LocalDateTime.now());
        vr.setUserCreated(worker);
        vr.setDateUpdated(LocalDateTime.now());
        vr.setUserUpdated(worker);
        try {
            this._service.create(vr);
            // TODO : send reupload request to
            // OMNI C&S -> batch job
            return OkResponseEntity("재촬영 요청을 정상적으로 처리했습니다.");
        } catch (Exception e) {
            return ErrorResponse(
                    String.format(
                            "재촬영 요청을 저장하는데 에러가 발생했습니다. - subjectId=%s, projectSeq=%s, trailIndex=%s, interfaceId=%s",
                            model.getSubjectId(), model.getProjectSeq(), model.getTrialIndex(), model.getInterfaceId()),
                    e);
        }
    }

    @ApiOperation(value = "대상자 상호작용 영상데이터 재업로드 요첟 API ", notes = "대상자 상호작용 영상 재촬영 요청을 등록함.")
    @PostMapping("/request/reuploads")
    public ResponseEntity<List<Map<String, Object>>> requestVideoReupload(@RequestBody VideoReuploadListModel model)
            throws Exception {
        List<String> interfaceIds = model.getInterfaceIds();
        if (interfaceIds == null) {
            throw new RuntimeException("The interfaceIds is null.");
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (String interfaceId : interfaceIds) {
            VideoReuploadModel vmodel = new VideoReuploadModel();
            vmodel.setInterfaceId(interfaceId);
            vmodel.setProjectSeq(model.getProjectSeq());
            vmodel.setSubjectId(model.getSubjectId());
            vmodel.setTrialIndex(model.getTrialIndex());
            ResponseEntity<JsonResponseObject> response = this.requestVideoReupload(vmodel);
            JsonResponseObject result = response.getBody();
            HttpStatus status = response.getStatusCode();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("interfaceId", interfaceId);
            map.put("status", status == null ? null : status.value());
            map.put("message", result == null ? null : result.getMessage());
            list.add(map);
        }

        return ResponseEntity.ok().body(list);
    }

    private String fname(String interfaceId, long projectSeq, int trialIndex, String subjectId, int retryIndex) {
        return String.format("%s_%s_%s_%s_%s", interfaceId, projectSeq, trialIndex, subjectId, retryIndex);
    }

    @ApiOperation(value = "대상자 상호작용 영상데이터 조회 ", notes = "대상자 상호작용 데이터 조회, 검색조건 출력")
    @GetMapping("/request/search")
    public ResponseEntity<JsonResponseObject> searchVideoDataList(@RequestParam final Map<String, Object> params) {
        final String systemId = super.getSystemId();
        final String userName = super.getAuthenticatedUsername();
        final int EIGHTTEEN = 18;
        final int TWOWENTY_THREE = 23;
        final int TWOWENTY_FOUR = 24;
        final int THIRTY_FIVE = 35;
        final int THIRTY_SIX = 36;
        final int FOURTY_EIGHT = 48;
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
        String orgId = null;
        String gender = null;
        boolean orgFiltered = false;
        int accessLevel = 0b0111111;
        if (params != null && !params.isEmpty()) {
            for (String k : params.keySet()) {
                Object v = params.get(k);

                if (k.equals("id") && v != null) {
                    // filterDoc.append("projectSeq", (projectId = Long.parseLong(v.toString())));
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
                if (k.equals("org") && v != null) {
                    orgFiltered = true;
                    // filterDoc.append("orgId", v.toString());
                    // dim.setOrgId(v.toString());
                    orgId = v.toString();
                }
            }
            if (params.containsKey("gender")) {
                gender = params.get("gender").toString();
            }
        }

        // Admin, Manager, QC User는 모든 연구기관의 데이터 조회 가능.
        if (!super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel) && !super.canQC(accessLevel)
                && !orgFiltered) {
            final String org = super.getOrgId();
            // filterDoc.append("orgId", org);
            orgId = org;
        }

        List<VideoResource> list = _videoResourceRepository.findAll(projectSeq, subjectId, orgId, gender, null, page,
                offset);
        List<VideoResource> countList = _videoResourceRepository.getTotalCount(projectSeq, subjectId, orgId, gender);

        for (VideoResource resource : list) {
            HashMap<String, String> areaQcStatus = new HashMap<String, String>();
            boolean hasInterfaceId = false;

            if (EIGHTTEEN <= resource.getBirthDay() && TWOWENTY_THREE >= resource.getBirthDay()) { // 18~ 23개월
                for (InterfaceIdone interfaceId : InterfaceIdone.values()) {
                    if (interfaceId.name().equals(resource.getInterfaceId())) {
                        areaQcStatus.put(interfaceId.name(), interfaceId.getValue());
                        hasInterfaceId = true;
                    }
                }
                if (!hasInterfaceId) {
                    areaQcStatus.put(resource.getInterfaceId(), Constants.IMAGING_QC_NO_DATA); // 수정
                }
            } else if (TWOWENTY_FOUR <= resource.getBirthDay() && THIRTY_FIVE >= resource.getBirthDay()) { // 24~ 35개월
                for (InterfaceIdtwo interfaceId : InterfaceIdtwo.values()) {
                    if (interfaceId.name().equals(resource.getInterfaceId())) {
                        areaQcStatus.put(interfaceId.name(), interfaceId.getValue());
                        hasInterfaceId = true;
                    }
                }
                if (!hasInterfaceId) {
                    areaQcStatus.put(resource.getInterfaceId(), Constants.IMAGING_QC_NO_DATA); // 수정
                }
            } else if (THIRTY_SIX <= resource.getBirthDay() && FOURTY_EIGHT >= resource.getBirthDay()) { // 36~ 48개월
                for (InterfaceIdthree interfaceId : InterfaceIdthree.values()) {
                    if (interfaceId.name().equals(resource.getInterfaceId())) {
                        areaQcStatus.put(interfaceId.name(), interfaceId.getValue());
                        hasInterfaceId = true;
                    }
                }
                if (!hasInterfaceId) {
                    areaQcStatus.put(resource.getInterfaceId(), Constants.IMAGING_QC_NO_DATA); // 수정
                }
            }
            // else if
            // (resource.getBirthDay()==0&&resource.getInterfaceId().equals("NO_DATA")) {
            // for (InterfaceIdone interfaceId : InterfaceIdone.values()) {

            // areaQcStatus.put(interfaceId.name(), Constants.IMAGING_QC_NO_DATA);
            // }

            // }

            resource.setAreaQcStatus(areaQcStatus);
            // LOGGER.info("status={}", resource.getAreaQcStatus());
        }

        return OkResponseEntity("데이터 수집 현황을 조회했습니다.", new ListDataModel<>(list, countList.size(), page, offset));
    }

    @ApiOperation(value = "대상자 상호작용 영상 다운로드", notes = "대상자 상호작용 영상 다운로드")
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
        if (!downloadDir.exists())
            downloadDir.mkdirs();

        // create a subject video directory for download
        String datetime = DateUtil.getShortDateTimeStampString();
        String targetDir = String.format("video_%s_%s_%s_%s", subjectId, projectSeq, trialIndex, datetime);
        File ftargetDir = new File(downloadDir, targetDir);
        if (ftargetDir.exists()) {
            ftargetDir.delete();
        }
        ftargetDir.mkdirs();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("created a download directory. - " + ftargetDir.getAbsolutePath());
        }

        boolean result = this._service.download(ftargetDir, systemId, subjectId, projectSeq, trialIndex);
        if (!result)
            throw new Exception("다운로드 할 수 있는 비식별화 영상이 없습니다.");

        // make a zip file from ftargetDir.
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

    @ApiOperation(value = "대상자 비식별화된 상호작용 영상 다운로드", notes = "대상자 비식별화된 상호작용 영상 다운로드")
    @GetMapping("/download/{subjectId}/{projectSeq}/{trialIndex}/{ifid}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable String subjectId,
            @PathVariable long projectSeq, @PathVariable int trialIndex, String ifId) throws Exception {
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
        if (!downloadDir.exists())
            downloadDir.mkdirs();

        // create a subject video directory for download
        String targetDir = String.format("video_downloads");
        File ftargetDir = new File(downloadDir, targetDir);
        if (!ftargetDir.exists()) {
            ftargetDir.mkdirs();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("created a download directory. - " + ftargetDir.getAbsolutePath());
            }
        }

        File result = this._service.download(ftargetDir, systemId, subjectId, projectSeq, trialIndex, ifId);
        if (result == null) {
            throw new Exception("다운로드 할 수 있는 비식별화 영상이 없습니다.");
        }

        FileSystemResource resource = new FileSystemResource(result);
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.getName());
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

}
