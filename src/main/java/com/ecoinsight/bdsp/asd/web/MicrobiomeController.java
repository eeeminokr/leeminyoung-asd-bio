package com.ecoinsight.bdsp.asd.web;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecoinsight.bdsp.asd.Constants;
import com.ecoinsight.bdsp.asd.crypto.FileCryptoKeys;
import com.ecoinsight.bdsp.asd.crypto.FileEncryptor;
import com.ecoinsight.bdsp.asd.entity.Microbiome;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.MicrobiomeModel;

import com.ecoinsight.bdsp.asd.model.PupillrometryResult;
import com.ecoinsight.bdsp.asd.model.SubjectState;
import com.ecoinsight.bdsp.asd.service.MicrobiomeService;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(path = "/api/v1/microbiome")
public class MicrobiomeController extends AsdBaseApiController {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private String _hostname;

    @Value("${ecoinsight.microbiome.crypto.password}")
    private String _secretKey;
    @Value("${ecoinsight.microbiome.crypto.security-path}")
    private String _securityPath;
    @Value("${ecoinsight.microbiome.crypto.output-dir}")
    private String _outputDir;
    private File _outputDirFile;

    @Value("${ecoinsight.download-dir}")
    private String _downloadDir;

    @Autowired
    private IProjectRepository _projectRepository;

    @Resource(name = MicrobiomeService.ID)
    private MicrobiomeService _service;

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

    @ApiOperation(value = "대상자 장내미생물물 데이터 조회 ", notes = "대상자 장내미생물물 데이터 조회, 검색조건 출력")
    @GetMapping("/request/search")
    public ResponseEntity<JsonResponseObject> searchPupillometryList(@RequestParam final Map<String, Object> params) {
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
        List<Microbiome> list = this._service.findAll(systemId, subjectId, projectSeq, trialIndex,
                orgId, gender, page, offset);

        list.forEach(result -> {
            result.setUserName(userName);
            result.addAreaQcStatus(DataSummaryColumn.MICROBIOME.name(),
                    result.isCompleted() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);

        });

        int count = this._service.countAll(systemId, subjectId, projectSeq, trialIndex, orgId, gender);
        return OkResponseEntity("동공측정 데이터 수집 현황을 조회했습니다.", new ListDataModel<>(list, count, page, offset));
    }

    @PostMapping("/upload/file/{taskId}")
    public ResponseEntity<JsonResponseObject> uploadFile(
            @RequestParam("file") MultipartFile mfile,
            @PathVariable("taskId") String taskId,
            @ModelAttribute MicrobiomeModel model) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("-> upload File=" + mfile.getOriginalFilename() + ", " + model);
        }
        if (mfile == null) {
            return ErrorResponseEntity("Cannot find uploaded file. Please, select a uploading file");
        }

        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();
        String fileName = mfile.getOriginalFilename();
        String extension = FilenameUtils.getExtension(fileName);
        PupillrometryResult result = new PupillrometryResult();
        result.setFileName(fileName);
        LOGGER.info("filename={}", fileName);

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
        if (SubjectState.InformedConsentSigned != subject.getState()
                && SubjectState.EnrolledActive != subject.getState()) {
            return ErrorResponse(
                    String.format("장내미생물 데이터를 업로드하기 위한 대상자 상태 정보를 먼저 확인해 주세요. - systemId=%s, subjectId=%s, state=%s", systemId,
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

        LOGGER.info("subjectId={},projectSeq={}, subjectprojectSeq= {},trialIndex={}", model.getSubjectId(), model.getProjectSeq(), subject.getProjectSeq(), model.getTrialIndex());

        long projectSeq = subject.getProjectSeq();
        List<Microbiome> resources = _service.getMicrobiome(systemId, model.getSubjectId(), model.getProjectSeq(), subject.getProjectSeq(), model.getTrialIndex());
        if (resources.size() > 0) {
            for (Microbiome item : resources) {
                if (item.isPublished()) {
                    return ErrorResponse(String.format(
                            "현재  대상자의 장내미생물 업로드 중이므로 더이상 업로드를 할수 없습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s,  uploadedFiles=%s",
                            systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(),
                            resources.size()));
                }

                if (item.isCompleted()) {
                    return ErrorResponse(String.format(
                            "현재  대상자의 장내미생물 업로드가 이미 완료 되어서 더이상 업로드를 할수 없습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, interfaceId=%s, uploadedFiles=%s",
                            systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(),
                            resources.size()));
                }
                projectSeq = item.getProjectSeq();
            }
        }

        int trialIndex = model.getTrialIndex();
        String outputFile = this.fname("MICROBIOME", projectSeq, trialIndex, model.getSubjectId())
                + "." + extension + "." + FileCryptoKeys.ENCRYPTED_FILE_EXTENSION;
        File encryptedFile = new File(this._outputDirFile, outputFile);

        LOGGER.info("OUTFILE={}", outputFile);

        Microbiome item = new Microbiome();
        item.setSystemId(systemId);
        item.setOrgId(subject.getOrgId());
        item.setProjectSeq(projectSeq);
        item.setSubjectId(model.getSubjectId());
        item.setTrialIndex(trialIndex);
        item.setOriginalFileName(mfile.getOriginalFilename());
        item.setTargetFileName(outputFile);
        item.setDateCreated(LocalDateTime.now());
        item.setDateUpdated(LocalDateTime.now());
        item.setUserCreated(worker);
        item.setUserUpdated(worker);
        item.setHostname(this._hostname);
        item.setDeleted(false);

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
                    "업로드 파일을 암호화하는데 에러가 발생했습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, error=%s",
                    systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(), e.getMessage()), e);
        }

        try {
            _service.create(item);
        } catch (Exception e) {
            return ErrorResponse(String.format(
                    "장내미생물 테이블에 데이터를 생성할수 없습니다.. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, error=%s",
                    systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(), e.getMessage()), e);
        }

        _service.publish(encryptedFile, item);
        if (item.isPublished()) {
            result.setSuccess(true);
            result.setMessage("장내미생물 파일을 업로드 했습니다.");
        } else {
            result.setSuccess(false);
            result.setMessage("장내미생물파일 업로드가 실패되었습니다.");
        }

        return ResponseEntity.ok(new JsonResponseObject(
                result.isSuccess(), result.isSuccess() ? "장내미생물파일 업로드를 완료했습니다." : "장내미생물파일 업로드가 실패되었습니다.",
                result));
    }

    private String fname(String pupillometry, long projectSeq, int trialIndex, String subjectId) {
        return String.format("%s_%s_%s_%s", pupillometry, projectSeq, trialIndex, subjectId);
    }

}
