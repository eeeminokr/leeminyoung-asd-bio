package com.ecoinsight.bdsp.asd.web;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoinsight.bdsp.asd.entity.SurveyStatus;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.ProjectSeq;
import com.ecoinsight.bdsp.asd.model.SurveyKeys;
import com.ecoinsight.bdsp.asd.model.SurveyStatusModel;
import com.ecoinsight.bdsp.asd.repository.IAsdDataCommonRepoistory;
import com.ecoinsight.bdsp.asd.repository.ISurveyStatusRepository;
import com.ecoinsight.bdsp.asd.service.DataSetDocumnetProcessorService;
import com.ecoinsight.bdsp.asd.service.SurveyStatusService;
import com.ecoinsight.bdsp.core.service.ServiceException;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;
import com.google.gson.internal.LinkedTreeMap;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(path = "/api/v1/survey")
public class SurveyStatusController extends AsdBaseApiController {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Resource(name = SurveyStatusService.ID)
    private SurveyStatusService service;
    @Autowired
    private IAsdDataCommonRepoistory _dataCommonRepository;

    @Autowired
    private ISurveyStatusRepository _repoistory;
    @Resource(name = DataSetDocumnetProcessorService.ID)
    private DataSetDocumnetProcessorService _dataSetDownloadService;

    @PostMapping("/status")
    public ResponseEntity<JsonResponseObject> postStatus(@RequestBody SurveyStatusModel model) {
        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();

        ProjectSeq pseq = null;
        try {
            pseq = ProjectSeq.valueOf(model.getProjectGroup());
        } catch (Exception e) {
            return ErrorResponseEntity("Invalid projectSeq. - " + model.getProjectGroup());
        }

        if (model.getSubjectId() == null) {
            return ErrorResponseEntity("Invalid subjectId. - " + model.getSubjectId());
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("postStatus -> " + model);
        }

        // subject validation from mongodb
        // for(int i=0;i<2;i++) {
        int trialindex = 0;
        for (int i = 0; i < 4; i++) {
            SurveyStatus sstatus = new SurveyStatus();
            sstatus.setSystemId(systemId);
            sstatus.setSubjectId(model.getSubjectId());
            sstatus.setProjectSeq(pseq.getSeq());
            sstatus.setTrialIndex(i == 0 ? 1 : i + 1);
            // trialIndex update ,registDate 4배열 //trialIndex =2 //
            // 1차 실무 ,
            try {
                Map<String, String[]> resultMap = model.getResultMap();
                for (Map.Entry<String, String[]> entry : resultMap.entrySet()) {
                    String key = entry.getKey();
                    String value = i < entry.getValue().length ? entry.getValue()[i] : null;
                    if (value == null) {
                        continue;
                    }
                    if (!value.equals("Y") && !value.equals("N") && !value.equals("N/A")) {
                        throw new RuntimeException("Invalid survey status. - " + value);
                    }

                    SurveyKeys sk = SurveyKeys.surveyKeys(key);
                    if (sk == null) {
                        throw new RuntimeException("Invalid survey name. - " + key);
                    }
                    switch (sk) {
                        case adir:
                            sstatus.setAdir(value);
                            break;
                        case ados2m1:
                            sstatus.setAdos2m1(value);
                            break;
                        case ados2m2:
                            sstatus.setAdos2m2(value);
                            break;
                        case ados2m3:
                            sstatus.setAdos2m3(value);
                            break;
                        case ados2mt:
                            sstatus.setAdos2mt(value);
                            break;
                        case bedeveli:
                            sstatus.setBedeveli(value);
                            break;
                        case bedevelp:
                            sstatus.setBedevelp(value);
                            break;
                        case bedevelq:
                            sstatus.setBedevelq(value);
                            break;
                        case cbcl:
                            sstatus.setCbcl(value);
                            break;
                        case kbayley:
                            sstatus.setKbayley(value);
                            break;
                        case kcars2:
                            sstatus.setKcars2(value);
                            break;
                        case kdst:
                            sstatus.setKdst(value);
                            break;
                        case kmchat:
                            sstatus.setKmchat(value);
                            break;
                        case kqchat:
                            sstatus.setKqchat(value);
                            break;
                        case kvineland:
                            sstatus.setKvineland(value);
                            break;
                        case kwppsi:
                            sstatus.setKwppsi(value);
                            break;
                        case pres:
                            sstatus.setPres(value);
                            break;
                        case scqlifetime:
                            sstatus.setScqlifetime(value);
                            break;
                        case scqcurrent:
                            sstatus.setScqcurrent(value);
                            break;
                        case selsi:
                            sstatus.setSelsi(value);
                            break;
                        case srs2:
                            sstatus.setSrs2(value);
                            break;
                        default:
                            break;
                    }
                }

                sstatus.setUserCreated(worker);
                sstatus.setUserUpdated(worker);
                this.service.postSurveyStatus(sstatus);
            } catch (Exception e) {
                return ErrorResponseEntity("Fail to update survey status. " + e.getMessage());
            }
        }
        return OkResponseEntity("Survey status is updated successfully.");
    }

    // @ApiOperation(value = "urban 데이터 조회 API ", notes = " urban 데이터를 DB에서 조회")
    // @GetMapping("/result")
    // public ResponseEntity<JsonResponseObject> getUrbanSurveyState(String
    // subjectGroupCd) {
    // // Save Urban Survey data by subject
    // this.service.saveSurveyDataBySubject(subjectGroupCd);
    // try {
    // // Respond with success message
    // return ResponseEntity.ok()
    // .body(new JsonResponseObject(true, "데이터를 조회했습니다."));
    // } catch (Exception e) {
    // // Respond with error message
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(new JsonResponseObject(false, "데이터 조회 중 오류가 발생했습니다."));
    // }
    // }
    /**
     * @param subjectId
     * @param projectSeq
     * @return
     */
    @ApiOperation(value = "설문지 수집데이터 state 수집유무 조회 & 업데이트", notes = "설문지 수집데이터state 수집유무 조회 & 업데이트")
    @GetMapping("/result/surveystate")
    public ResponseEntity<JsonResponseObject> findBySurveyStateUpdate(
            @RequestParam(required = false) String subjectId, @RequestParam(required = false) Long projectSeq) {
        final String systemId = getSystemId();
        final String worker = getAuthenticatedUsername();

        try {
            Collection<AsdProjectSubject> subjects = null;

            // subjectId 또는 projectSeq에 따라 데이터 조회
            if (subjectId != null) {
                subjects = _subjectRepository.findAllBySubjectId(systemId, subjectId);
            } else if (projectSeq != 0) {
                subjects = _subjectRepository.findAllByProjectSeq(systemId, projectSeq);
            }

            if (subjects == null || subjects.isEmpty()) {
                LOGGER.error("대상자를 찾을 수 없습니다. systemId={}, subjectId={}, projectSeq={}", systemId, subjectId, projectSeq);
                return ErrorResponseEntity(
                        String.format("대상자를 찾을 수 없습니다. - systemId=%s, subjectId=%s, projectSeq=%s", systemId, subjectId, projectSeq));
            }

            Collection<List<Map<String, Object>>> processedData = new ArrayList<>();

            // 조회된 subjects 처리
            subjects.forEach(entry -> {
                List<SurveyStatus> list = _repoistory.findSurveyStatusbySubject(entry.getSubjectId());
                LOGGER.info("Retrieved survey statuses for subjectId={}: {}", entry.getSubjectId(), list);
                this.service.findSurveyDataBySubject(list); // 데이터 처리
            });

            return ResponseEntity.ok()
                    .body(new JsonResponseObject(true, "데이터를 조회했습니다.", processedData));

        } catch (Exception e) {
            LOGGER.error("Error occurred while processing survey data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JsonResponseObject(false, "데이터 조회 중 오류가 발생했습니다."));
        }
    }

    @ApiOperation(value = "설문지 urban 수집데이터 request ", notes = "설문지 urban 수집데이터 request api")
    @GetMapping("/result/urban/surveystate")
    public ResponseEntity<byte[]> getUrbanSurveyState(
            @RequestParam(required = false, name = "subjectId") String subjectId,
            @RequestParam(required = false, name = "projectSeq") Long projectSeq) {
        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime dateTime = LocalDateTime.now();
        String dateTimeFormatted = dateTime.format(formatter);
        LOGGER.info("researchCd={}", systemId);

        // 대상자 정보 조회
        Collection<AsdProjectSubject> subjectList = null;
        if (subjectId == null) {
            subjectList = this._subjectRepository.findAllByProjectSeq(systemId, projectSeq);
        } else {
            subjectList = this._subjectRepository.findAllBySubjectId(systemId, subjectId);
        }

        // 대상자 정보 확인
        if (subjectList == null || subjectList.isEmpty()) {
            LOGGER.error("대상자 정보를 찾을수 없습니다. - systemId={}, subjectId={}", systemId, subjectId);
            return ResponseEntity.notFound().build();
        }
        List<AsdProjectSubject> subjectIdList = subjectList.stream()
                .collect(Collectors.toList());

        List<LinkedTreeMap<String, Object>> documentList = new ArrayList<>();
        String projectName = null;
        try {
            // 각 대상자에 대한 설문조사 데이터 요청
            for (AsdProjectSubject subject : subjectIdList) {
                LinkedTreeMap<String, Object> doc = this.service.requestGetUrbanSurvey(systemId, subject.getSubjectId(),
                        subject.getNote());

                // 요청된 데이터가 있을 경우 리스트에 추가
                if (doc != null) {
                    documentList.add(doc);
                }
            }

            // 요청된 데이터가 없을 경우 404 응답 반환
            if (documentList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // JSON 배열로 문서 리스트 변환
            // JSONArray jsonArray = new JSONArray();
            // for (LinkedTreeMap<String, Object> jsonObject : documentList) {
            // JSONObject formattedJsonObject = new JSONObject(jsonObject);
            // jsonArray.put(formattedJsonObject);
            // }
            // LinkedHashMap으로 변환한 데이터를 중복으로 추가하지 않고 새로운 리스트에 추가
            List<LinkedHashMap<String, Object>> formattedDocumentList = new ArrayList<>();
            for (LinkedTreeMap<String, Object> jsonObject : documentList) {
                LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>(jsonObject);
                formattedDocumentList.add(linkedHashMap);
            }

            // JSON 배열을 가독성있는 문자열로 변환
            String formattedJsonString = this._dataSetDownloadService.convertListHashMapTomapper(formattedDocumentList);
            // LOGGER.info("formattedJSON={}", formattedDocumentList);
            // 프로젝트명을 projectSeq로부터 가져오기
            for (ProjectSeq seq : ProjectSeq.values()) {
                if (seq.getSeq() == projectSeq) {
                    projectName = seq.name();
                    break;
                }
            }

            // 응답 헤더 및 본문 준비
            byte[] jsonBytes = formattedJsonString.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=T_SurveySatus(" + projectName + "_" + dateTimeFormatted + ").json");
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(jsonBytes.length)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBytes);
        } catch (ServiceException e) {
            LOGGER.error("데이터 조회 중 오류가 발생했습니다.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
