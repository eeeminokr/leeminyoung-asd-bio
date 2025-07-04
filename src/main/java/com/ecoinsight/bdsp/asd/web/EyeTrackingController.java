package com.ecoinsight.bdsp.asd.web;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import javax.annotation.Resource;

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

import com.ecoinsight.bdsp.asd.Constants;
import com.ecoinsight.bdsp.asd.entity.EyeTracking;
import com.ecoinsight.bdsp.asd.entity.EyeTrackingDataSet;
import com.ecoinsight.bdsp.asd.entity.EyeTrackingInfo;
import com.ecoinsight.bdsp.asd.entity.EyeTrackingInfo.IMAGE_TYPE;
import com.ecoinsight.bdsp.asd.entity.EyeTrackingInfo.VIDEO_TYPE;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.EyeTrackingInfoModel;
import com.ecoinsight.bdsp.asd.model.EyeTrackingModel;
import com.ecoinsight.bdsp.asd.model.EyeTrackingRequestModel;
import com.ecoinsight.bdsp.asd.model.ProjectSeq;
import com.ecoinsight.bdsp.asd.model.TrackInfoModel;
import com.ecoinsight.bdsp.asd.repository.IEyeTrackingInfoRepository;
import com.ecoinsight.bdsp.asd.repository.IEyeTrackingRepository;
import com.ecoinsight.bdsp.asd.model.Result;
import com.ecoinsight.bdsp.asd.model.SubjectState;
import com.ecoinsight.bdsp.asd.service.DataCommonService;
import com.ecoinsight.bdsp.asd.service.DataSetDocumnetProcessorService;
import com.ecoinsight.bdsp.asd.service.EyeTrackingService;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.util.ZipUtil;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(path = "/api/v1/eyetracking")
public class EyeTrackingController extends AsdBaseApiController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Value("${ecoinsight.download-dir}")
    private String downloadDir;

    @Resource(name = EyeTrackingService.ID)
    private EyeTrackingService _service;

    @Resource(name = DataSetDocumnetProcessorService.ID)
    private DataSetDocumnetProcessorService _dataSetDownloadService;

    @Autowired
    private IProjectRepository _projectRepository;

    @Autowired
    private IEyeTrackingRepository _repository;

    @Autowired
    private IEyeTrackingInfoRepository _eyetrackingInfoRepository;

    @ApiOperation(value = "시선처리 데이터 업로드 API ", notes = " 대상자의 시선처리 데이터를 DB에 업로드")
    @PostMapping("/result")
    public ResponseEntity<JsonResponseObject> uploadResult(@RequestBody EyeTrackingModel model) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> " + model);
        }
        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();

        Result result = new Result();
        Collection<AsdProjectSubject> subjects = this._subjectRepository.findAllBySubjectId(systemId,
                model.getSubjectId());
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

        // if (trialIndex == 3 || trialIndex > 4) {
        // return ErrorResponse(String.format(
        // "업로드한 MChart의 차수 정보가 잘못되었습니다.. - systemId=%s, subjectId=%s, projectSeq=%s,
        // trialIndex=%s", systemId,
        // model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex()));
        // }

        // startDate and endDate validation
        Optional<LocalDate> soptional = DateUtil.parseSimple(subject.getStartDate());
        Optional<LocalDate> eoptional = DateUtil.parseSimple(subject.getEndDate());
        if (soptional.isEmpty() || eoptional.isEmpty()) {
            return ErrorResponse(String.format(
                    "대상자 프로젝트 과제 차수의 시작일 혹은 종료일이 설정되지 안았습니다.. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s, period=%s ~ %s",
                    systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex(),
                    subject.getStartDate(), subject.getEndDate()));
        }
        LocalDate startDate = soptional.get();
        LocalDate endDate = eoptional.get();
        LocalDate trailIndexDate = LocalDate.from(subject.getDateTrialIndex());
        // if (trailIndexDate.isBefore(startDate) || trailIndexDate.isAfter(endDate)) {
        // return ErrorResponse(String.format(
        // "대상자 프로젝트 과제 차수 기간이 아닙니다. 시작일과 종료일을 확인해 주세요. - systemId=%s, subjectId=%s,
        // projectSeq=%s, trialIndex=%s, period=%s ~ %s",
        // systemId, model.getSubjectId(), subject.getProjectSeq(),
        // subject.getTrialIndex(),
        // subject.getStartDate(), subject.getEndDate()));
        // } // endDate 값 업데이트가 안하고 있었던 상황이기때문에 validation excetioon error로 임시 주석

        // if(model.getEyeTrackingInfos()==null ||
        // model.getEyeTrackingInfos().size()==0) {
        // return ErrorResponse(String.format("시선 처리 측정 데이터가 포함되어 있지 않습니다. -
        // systemId=%s, subjectId=%s, eyeTrackingInfos=%s", systemId,
        // model.getSubjectId(),
        // model.getEyeTrackingInfos()==null?"null":model.getEyeTrackingInfos().toString()));
        // }

        // check if it was already uploaded.
        Optional<EyeTracking> optional = this._service.getEyeTracking(systemId, model.getSubjectId(), model.getType(),
                projectSeq, trialIndex); // 해당값에서 trialinfo 값이 없어도 저장이 되어야하므로, image타입이 없으면 조회안됨
        if (optional.isPresent()) {
            return ErrorResponse(String.format(
                    "현재 차수의 시선처리 데이터가 이미 등록되어 있습니다. - systemId=%s, subjectId=%s, type=%s, projectSeq=%s, trialIndex=%s",
                    systemId, model.getSubjectId(), model.getType(), subject.getProjectSeq(), subject.getTrialIndex()));
        }

        EyeTracking tracking = new EyeTracking();
        tracking.setOrgId(subject.getOrgId());
        tracking.setProjectSeq(projectSeq);
        tracking.setTrialIndex(trialIndex);
        tracking.setSystemId(subject.getSystemId());
        tracking.setSubjectId(model.getSubjectId());
        tracking.setPatientSeq(model.getPatientSeq());
        tracking.setSetKey(model.getSetKey());
        tracking.setType(model.getType());
        tracking.setMeasureTime(model.getMeasureTime());
        tracking.setRegistDt(model.getRegistDt());
        tracking.setDeviceHeight(model.getDeviceHeight());
        tracking.setDeviceWidth(model.getDeviceWidth());
        tracking.setFailCount(model.getFailCount());
        tracking.setAppVersion(model.getAppVersion());
        tracking.setPhoneModel(model.getPhoneModel());
        tracking.setOsVersion(model.getOsVersion());
        tracking.setDateCreated(LocalDateTime.now());
        tracking.setUserCreated(worker);
        try {
            List<EyeTrackingInfo> infos = new ArrayList<EyeTrackingInfo>();
            tracking.setEyeTrackingInfos(infos);

            if (model.getEyeTrackingInfos().isEmpty()) {
                List<EyeTrackingInfoModel> modifiedInfos = defaultSetValue(model.getEyeTrackingInfos());
                model.setEyeTrackingInfos(modifiedInfos);
            }

            if (model.getEyeTrackingInfos() != null) {
                for (EyeTrackingInfoModel item : model.getEyeTrackingInfos()) {
                    EyeTrackingInfo info = new EyeTrackingInfo();
                    info.setEyeTrackingId(tracking.getId());
                    info.setSort(item.getSort());
                    info.setGrade(item.getGrade());
                    info.setDateCreated(LocalDateTime.now());
                    info.setUserCreated(worker);
                    info.setAoi(this.toListOfDouble(item.getAoi()));
                    info.setMedia(this.toListOfDouble(item.getMedia()));
                    info.setTrackInfos(this.toTrackingInfos(item.getTrackInfos()));
                    infos.add(info);
                }
            }
            this._service.create(tracking);
            result.setSuccess(true);
            result.setMessage("시선처리 데이터를 저장했습니다.");
        } catch (Exception e) {
            return ErrorResponse(
                    String.format("시선처리 데이터를 저장하지 못했습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s",
                            systemId, model.getSubjectId(), subject.getProjectSeq(), subject.getTrialIndex()),
                    e);
        }
        return ResponseEntity.ok(new JsonResponseObject(result.isSuccess(), result.getMessage(), result));
    }

    private String toTrackingInfos(List<TrackInfoModel> infos) {
        if (infos == null)
            return null;
        Gson gson = new Gson();
        return gson.toJson(infos);
    }

    private String toListOfDouble(List<Double> data) {
        if (data == null)
            return null;
        Gson gson = new Gson();
        return gson.toJson(data);
    }

    private List<EyeTrackingInfoModel> defaultSetValue(List<EyeTrackingInfoModel> info) {

        // if (info.isEmpty()) {
        List<EyeTrackingInfoModel> defaultList = new ArrayList<>();
        EyeTrackingInfoModel defaultInfo = new EyeTrackingInfoModel();
        defaultInfo.setSort(null);
        defaultInfo.setGrade(null);
        defaultInfo.setAoi(null);
        defaultInfo.setMedia(null);
        defaultList.add(defaultInfo);

        return defaultList;
        // }
        // else {
        // return info;
        // }
    }

    @ApiOperation(value = "대상자 시선추적 조회 ", notes = "대상자 시선추적 데이터 조회, 검색조건 출력")
    @GetMapping("/request/search")
    public ResponseEntity<JsonResponseObject> searchVideoDataList(@RequestParam final Map<String, Object> params) {
        final String systemId = super.getSystemId();
        final String userName = super.getAuthenticatedUsername();
        LOGGER.info("USERNAME={}", userName);
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

        List<EyeTrackingInfo> list = _eyetrackingInfoRepository.findAll(projectSeq, subjectId, orgId, gender, page,
                offset);
        List<EyeTrackingInfo> countList = _eyetrackingInfoRepository.getPagingCount(projectSeq, subjectId, orgId,
                gender, page, offset);
        List<EyeTrackingInfo> count = _eyetrackingInfoRepository.countAll(projectSeq, subjectId, orgId, gender);

        list.forEach(result -> {
            boolean VIDEO = false;
            boolean IMAGE = false;
            result.setUserName(userName);
            if (result.getType().contains("video")) {
                VIDEO = true;
                for (VIDEO_TYPE type : VIDEO_TYPE.values()) {
                    boolean hastypes = false;
                    if (result.getSort() == null) {
                        LOGGER.debug("sortnull1={}", result.getSort());
                        result.setSort(1);
                    }
                    if (type.getValue().contains(result.getSort().toString()) && VIDEO == true) {
                        hastypes = true;
                        result.addAreaQcStatus("VIDEO",
                                hastypes ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_DONE_QC);
                    }

                }

            } else if (result.getType().contains("NO_DATA")) {
                result.addAreaQcStatus("VIDEO", VIDEO ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
            }

            if (result.getType().contains("image")) {
                IMAGE = true;
                for (IMAGE_TYPE type : IMAGE_TYPE.values()) {
                    boolean hastypes = false;
                    if (result.getSort() == null) {
                        LOGGER.debug("sortnull2={}", result.getSort());
                        result.setSort(1);
                    }
                    if (type.getValue().contains(result.getSort().toString()) && IMAGE == true) {
                        hastypes = true;
                        result.addAreaQcStatus("IMAGE",
                                hastypes ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_DONE_QC);
                    }
                }

            } else if (result.getType().contains("NO_DATA")) {
                result.addAreaQcStatus("IMAGE", IMAGE ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
            }

        });
        return OkResponseEntity("데이터 수집 현황을 조회했습니다.", new ListDataModel<>(list, count.size(), page, offset));
    }

    @ApiOperation(value = "대상자 시선처리 데이터 다운로드", notes = "대상자 시선처리 데이터 다운로드")
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

        File rootDir = new File(this.downloadDir, "eyetracking");
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }

        String datetime = DateUtil.getShortDateTimeStampString();
        String baseDir = String.format("eyetracking_%s_%s_%s_%s", subjectId, projectSeq, trialIndex, datetime);
        File targetDir = new File(rootDir, baseDir);
        if (targetDir.exists()) {
            targetDir.delete();
        }
        targetDir.mkdir();

        boolean result = this._service.download(targetDir, systemId, subjectId, projectSeq, trialIndex);
        if (!result)
            throw new Exception("다운로드 할 데이터가 없습니다.");

        // make a zip file and delete target directory
        File zipFile = ZipUtil.makeZip(rootDir, baseDir);
        targetDir.delete();

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

    @ApiOperation(value = "대상자 시선처리 데이터 다운로드", notes = "대상자 시선처리 데이터 다운로드")
    @GetMapping(path = "/dataset/download/{projectSeq}")
    public ResponseEntity<byte[]> dataSetDownload(@PathVariable long projectSeq) {
        final String systemId = super.getSystemId();
        List<EyeTrackingDataSet> eyetrackingList = _repository.findAllByProjectSeq(projectSeq);
        List<Document> documents = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime dateTime = LocalDateTime.now();
        String dateTimeformmted = dateTime.format(formatter);

        try {
            for (EyeTrackingDataSet eyetracking : eyetrackingList) {
                String subjectId = eyetracking.getSubjectId();
                Collection<AsdProjectSubject> subjects = this._subjectRepository.findAllBySubjectId(systemId,
                        subjectId);

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
                        eyetracking.setNote("-");
                    } else {
                        eyetracking.setNote(subject.getNote());
                    }
                    if (subject.getGender() == null) {
                        eyetracking.setGender("-");
                    } else {
                        eyetracking.setGender(subject.getGender());
                    }
                    Document doc = this._dataSetDownloadService.convertVoToDocument(eyetracking);
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
                    "attachment; filename=T_EyeTrackingInfo(" + projectName + "_" + dateTimeformmted + ").json");
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

    @ApiOperation(value = "대상자 시선추적 데이터 삭제 요청 API ", notes = "대상자 시선추적 데이터 옵션 체크후 삭제처리.")
    @PostMapping("/delete")
    public ResponseEntity<JsonResponseObject> deleteEyeTrackingbyType(@RequestBody EyeTrackingRequestModel model) {
        final String systemId = super.getSystemId();
        List<String> EyeTrackingTypes = model.getTypes();
        final String userName = super.getAuthenticatedUsername();
        LOGGER.info("USERAME : {}", userName);
        if (EyeTrackingTypes.isEmpty()) {
            throw new RuntimeException("The types is null.");
        }

        Result result = new Result(); // Result 변수를 밖에서 선언
        List<Optional<EyeTracking>> optionalList = new ArrayList<>();

        for (String type : EyeTrackingTypes) {
            Optional<EyeTracking> optional = this._service.getEyeTracking(systemId, model.getSubjectId(), type,
                    model.getProjectSeq(), model.getTrialIndex());

            optional.ifPresent(eyeTracking -> {
                try {
                    this._service.extracted(model, type, eyeTracking);
                    result.setSuccess(true);
                    result.setMessage("대상자 시선추적 데이터 삭제를 완료 하였습니다.");
                } catch (Exception e) {
                    // 에러 처리
                    result.setSuccess(false);
                    result.setMessage(String.format(
                            "대상자  삭제시 에러가 발생했습니다. - systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s",
                            systemId, model.getSubjectId(), model.getProjectSeq(), model.getTrialIndex()));
                    // 에러 로깅
                    LOGGER.error("An error occurred while processing M-Chat data deletion.", e);
                }
            });
            optionalList.add(optional);
        }

        LOGGER.info("response[Optional]={}", optionalList.stream().toString());
        return ResponseEntity.ok(new JsonResponseObject(result.isSuccess(), result.getMessage(), optionalList));
    }

}
