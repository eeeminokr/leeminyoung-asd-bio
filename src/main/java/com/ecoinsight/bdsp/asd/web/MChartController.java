package com.ecoinsight.bdsp.asd.web;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.bcel.generic.LOOKUPSWITCH;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoinsight.bdsp.asd.entity.MCharDataSet;
import com.ecoinsight.bdsp.asd.entity.MChart;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.MChartModel;
import com.ecoinsight.bdsp.asd.model.ProjectSeq;
import com.ecoinsight.bdsp.asd.model.Result;
import com.ecoinsight.bdsp.asd.model.SubjectState;
import com.ecoinsight.bdsp.asd.repository.IMChartRepository;
import com.ecoinsight.bdsp.asd.service.DataSetDocumnetProcessorService;
import com.ecoinsight.bdsp.asd.service.MChartService;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(path = "/api/v1/mchart")
public class MChartController extends AsdBaseApiController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Value("${ecoinsight.download-dir}")
    private String _downloadDir;
    @Resource(name = MChartService.ID)
    private MChartService _service;

    @Autowired
    private IMChartRepository _repository;
    @Autowired
    private IProjectRepository _projectRepository;

    @Autowired
    private DataSetDocumnetProcessorService _dataSetDownloadService;

    @ApiOperation(value = "M-Chat 데이터 업로드 API ", notes = " 대상자의 M-chart 데이터를 DB에 업로드")
    @PostMapping("/result")
    public ResponseEntity<JsonResponseObject> uploadResult(@RequestBody MChartModel model) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> " + model);
        }
        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();

        Result result = new Result();
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
                    String.format("데이터를 업로드하기 위한 대상자 상태 정보를 먼저확인해 주세요. - systemId=%s, subjectId=%s, state=%s", systemId,
                            model.getSubjectId(), subject.getState()));
        }

        Optional<LocalDate> bdoptional = DateUtil.parseSimple(subject.getBirthDay());
        if (bdoptional.isEmpty()) {
            return ErrorResponse("Invalid birthday format. - " + subject.getBirthDay());
        }
        Optional<LocalDate> rdoptional = DateUtil.parseSimple(subject.getRegistDate());
        if (rdoptional.isEmpty()) {
            return ErrorResponse("Invalid registDate format. - " + subject.getRegistDate());
        }
        LocalDate birthDate = bdoptional.get();
        LocalDate registDate = rdoptional.get();// LocalDate.from(subject.getDateTrialIndex());
        Period diff = Period.between(birthDate, registDate);
        int months = diff.getYears() * 12 + diff.getMonths();

        // if(months<18 || months >= 31) {
        // return ErrorResponse("해당 대상자는 MChat 데이터를 등록할수 있는 개월수가 아닙니다.(18<= months < 31)
        // - months="+months);
        // } // 개월 수 상관없이 수집완료 기준으로 옴니측에서 에코측 호스팅하는 api를 호출하여 파라미터 값을 전달해줘서 임시 주석제거

        long projectSeq = model.getProjectSeq();
        int trialIndex = model.getTrialIndex();
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

        if (trialIndex > 1) { // 지금은 옴니측에서 trailIndex 1차수 fixed로 파라미터값을 전송하지만, 여러 차수의 등록에 대한 확정이 되면 로직 변경을
                              // 해야함.
            return ErrorResponse(String.format(
                    "MChart 데이터는 1차수에만 등록할수 있습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s", systemId,
                    model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex()));
        }

        // startDate and endDate validation
        Optional<LocalDate> soptional = DateUtil.parseSimple(subject.getStartDate());
        Optional<LocalDate> eoptional = DateUtil.parseSimple(subject.getEndDate());
        if (soptional.isEmpty() || eoptional.isEmpty()) {
            return ErrorResponse(String.format(
                    "대상자 프로젝트 과제 차수의 시작일 혹은 종료일이 설정되지 안았습니다.. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, period=%s ~ %s",
                    systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(),
                    subject.getStartDate(), subject.getEndDate()));
        }
        // LocalDate startDate = soptional.get();
        // LocalDate endDate = eoptional.get();
        // LocalDate trailIndexDate = LocalDate.from(subject.getDateTrialIndex());
        // if (trailIndexDate.isBefore(startDate) || trailIndexDate.isAfter(endDate)) {
        // return ErrorResponse(String.format(
        // "대상자 프로젝트 과제 차수 기간이 아닙니다. 시작일과 종료일을 확인해 주세요. - systemId=%s, subjectId=%s,
        // projectSeq=%s, trialIndex=%s, period=%s ~ %s",
        // systemId, model.getSubjectId(), subject.getProjectSeq(),
        // subject.getTrialIndex(),
        // subject.getStartDate(), subject.getEndDate()));
        // }

        // check if it was already uploaded.
        Optional<MChart> optional = this._service.getMChart(systemId, model.getSubjectId(), projectSeq, trialIndex);
        if (optional.isPresent()) {
            return ErrorResponse(String.format(
                    "현재 차수의 MChat 데이터가 이미 등록되어 있습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s",
                    systemId, model.getSubjectId(), model.getProjectSeq(), trialIndex));
        }

        MChart chart = new MChart();
        chart.setOrgId(subject.getOrgId());
        chart.setProjectSeq(projectSeq);
        chart.setTrialIndex(trialIndex);
        chart.setSystemId(subject.getSystemId());
        chart.setSubjectId(model.getSubjectId());
        chart.setPatientSeq(model.getPatientSeq());
        chart.setRschKey(model.getRschKey());
        chart.setResult(model.getResult());
        chart.setRegistDt(model.getRegistDt());
        chart.setDateCreated(LocalDateTime.now());
        chart.setUserCreated(worker);
        try {
            this._service.create(chart, subject.getBirthDay(), subject.getName());
            result.setSuccess(true);
            result.setMessage("M-Chat 결과를 저장했습니다.");
        } catch (Exception e) {
            return ErrorResponse(
                    String.format("M-Chat 저장시 에러가 발생했습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s",
                            systemId, model.getSubjectId(), subject.getProjectSeq(), model.getTrialIndex()),
                    e);
        }
        return ResponseEntity.ok(new JsonResponseObject(result.isSuccess(), result.getMessage(), result));
    }

    @ApiOperation(value = "M-Chat 데이터 조회 API ", notes = " 대상자의 M-chat 데이터를 DB에서 조회")
    @GetMapping("/result")
    public ResponseEntity<JsonResponseObject> getMchat(
            @RequestParam Map<String, Object> params) {
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
                if (offset <= 0 || offset > ListDataModel.MAX_ROW_COUNT)
                    offset = ListDataModel.DEFAULT_ROW_COUNT;
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
                    projectSeq = Long.parseLong(v.toString());

                    String rolename = "";

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
                    subjectId = v.toString();
                }
                if (k.equals("org") && v != null) {
                    orgFiltered = true;
                    orgId = v.toString();
                }

            }
            if (params.containsKey("gender")) {
                gender = params.get("gender").toString();
            }
        }

        if (!super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel) && !super.canQC(accessLevel)
                && !orgFiltered) {
            final String org = super.getOrgId();
            orgId = org;
        }

        List<MChart> mchatlist = this._service.getMChartAll(systemId, subjectId, projectSeq, orgId, gender, page,
                offset);

        long months = 0;
        for (MChart mchat : mchatlist) {
            if (mchat.getRegistDt() != null) {
                months = calculateMonths(mchat.getBirthDay(), mchat.getRegistDt());
            } else {
                months = calculateMonths(mchat.getBirthDay(), mchat.getDateCreated().toString());
            }
            mchat.setMonths(months);
        }
        int count = this._service.countAll(systemId, subjectId, projectSeq, orgId, gender);

        return OkResponseEntity("Mchat 데이터를 조회했습니다.", new ListDataModel<>(mchatlist, count, page, offset));
    }

    private long calculateMonths(String birthDay, String registDate) {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birth = LocalDate.parse(birthDay, dateFormatter);

        LocalDateTime referenceDateTime;
        try {
            referenceDateTime = LocalDateTime.parse(registDate);
        } catch (DateTimeParseException e) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            referenceDateTime = LocalDateTime.parse(registDate, dateTimeFormatter);
        }

        LocalDate reference = referenceDateTime.toLocalDate();
        return ChronoUnit.MONTHS.between(birth, reference);
    }

    @ApiOperation(value = "대상자 MChat 데이터 다운로드", notes = "대상자 MChart 데이터 다운로드")
    @GetMapping("/download/{subjectId}/{projectSeq}/{trialIndex}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable String subjectId,
            @PathVariable long projectSeq, @PathVariable int trialIndex)
            throws Exception {
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

        File rootDir = new File(this._downloadDir, "mchat");
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }

        File file = this._service.download(rootDir, systemId, subjectId, projectSeq, trialIndex);
        if (file == null)
            throw new Exception("다운로드 할 데이터가 없습니다.");

        FileSystemResource resource = new FileSystemResource(file);
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    @ApiOperation(value = "대상자 시선처리 데이터 다운로드", notes = "대상자 시선처리 데이터 다운로드")
    @GetMapping(path = "/dataset/download/{projectSeq}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> dataSetDownload(@PathVariable long projectSeq) {
        final String systemId = super.getSystemId();
        List<MCharDataSet> mchatList = _repository.findAllByProjectSeq(projectSeq);
        List<Document> documents = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime dateTime = LocalDateTime.now();
        String dateTimeformmted = dateTime.format(formatter);
        try {
            for (MCharDataSet mchart : mchatList) {
                String subjectId = mchart.getSubjectId();
                Collection<AsdProjectSubject> subjects = _subjectRepository.findAllBySubjectId(systemId, subjectId);

                if (subjects == null || subjects.isEmpty()) {
                    LOGGER.error("Subject information not found for subjectId: " + subjectId);
                    continue;
                }

                for (AsdProjectSubject subject : subjects) {
                    // Validate subject state
                    if (SubjectState.InformedConsentSigned != subject.getState()
                            && SubjectState.EnrolledActive != subject.getState()) {
                        LOGGER.error("Invalid state for subject: " + subjectId);
                        continue;
                    }
                    if (subject.getNote() == null) {
                        mchart.setNote("-");
                    } else {
                        mchart.setNote(subject.getNote());
                    }
                    if (subject.getGender() == null) {
                        mchart.setGender("-");
                    } else {
                        mchart.setGender(subject.getGender());
                    }

                    Document doc = this._dataSetDownloadService.convertVoToDocument(mchart);
                    documents.add(doc);
                }
            }

            if (documents.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Convert documents to JSON string
            String jsonString = documents.stream()
                    .map(this._dataSetDownloadService::convertDocumentToJson)
                    .collect(Collectors.joining(","));

            jsonString = "[" + jsonString + "]";

            String projectName = null;
            for (ProjectSeq seq : ProjectSeq.values()) {
                if (seq.getSeq() == projectSeq) {
                    projectName = seq.name();
                }

            }

            // Prepare response entity
            byte[] jsonBytes = jsonString.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=T_MCHAT(" + projectName + "_" + dateTimeformmted + ").json");
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(jsonBytes.length)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBytes);
        } catch (Exception e) {
            LOGGER.error("Error occurred while processing data for download", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
