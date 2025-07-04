package com.ecoinsight.bdsp.asd.service;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.xpath.operations.Bool;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecoinsight.bdsp.asd.entity.DataSummary;
import com.ecoinsight.bdsp.asd.entity.SurveyStatus;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.ProjectSeq;
import com.ecoinsight.bdsp.asd.model.SubjectState;
import com.ecoinsight.bdsp.asd.model.SurveyKeysStatus;
import com.ecoinsight.bdsp.asd.repository.IAsdDataCommonRepoistory;
import com.ecoinsight.bdsp.asd.repository.IAsdProjectSubjectRepository;
import com.ecoinsight.bdsp.asd.repository.ISurveyStatusRepository;
import com.ecoinsight.bdsp.core.service.ServiceException;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

@Transactional
@Service(SurveyStatusService.ID)
public class SurveyStatusService implements AsdSubjectService {

    public static final String ID = "SurveyStatusService";
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    protected IAsdProjectSubjectRepository _subjectRepository;
    @Autowired
    private ISurveyStatusRepository _repository;
    @Autowired
    private IAsdDataCommonRepoistory _dataSummaryRepository;
    @Autowired
    private DataCommonService _dataCommonService;

    // @Value("${ecoinsight-dataDset-download-dir}")
    // private String filePath;
    @Resource(name = DataSetDocumnetProcessorService.ID)
    private DataSetDocumnetProcessorService _dataSetDownloadService;
    public static final String REQUEST_SURVEY_RESULT_URI = "/api/survey-result";
    @Value("${ecrf.apiplatform.url}")
    private String targetUrl;
    @Value("${ecrf.apiplatform.key}")
    private String authKey;
    private RestTemplate restTemplate = new RestTemplate();

    public void postSurveyStatus(SurveyStatus survey) throws Exception {
        var subjects = this._subjectRepository.findAllBySubjectId(survey.getSystemId(), survey.getSubjectId());
        if (subjects == null || subjects.size() <= 0) {
            throw new Exception(String.format("대상자 정보를 찾을수 없습니다. - systemId=%s, subjectId=%s", survey.getSystemId(),
                    survey.getSubjectId()));
        }

        if (subjects.size() > 1) {
            throw new Exception(String.format("대상자 정보가 두개 이상의 과제에 등록되어 있습니다. - systemId=%s, subjectId=%s",
                    survey.getSystemId(), survey.getSubjectId()));
        }

        AsdProjectSubject subject = subjects.stream().findFirst().get();
        // subject state validation
        if (SubjectState.InformedConsentSigned != subject.getState()
                && SubjectState.EnrolledActive != subject.getState()) {
            throw new Exception(
                    String.format("대상자 상태 정보를 먼저 확인해 주세요. Active 상태가 아닙니다. - systemId=%s, subjectId=%s, state=%s",
                            survey.getSystemId(), survey.getSubjectId(), subject.getState()));
        }

        survey.setOrgId(subject.getOrgId());

        Optional<SurveyStatus> optional = this._repository.findByKeys(survey.getSystemId(), survey.getSubjectId(),
                survey.getProjectSeq(), survey.getTrialIndex());
        LocalDateTime now = LocalDateTime.now();
        List<SurveyStatus> surveyStatusList = new ArrayList<>();
        if (optional.isEmpty()) {
            // create
            survey.setGender(subject.getGender());
            survey.setRegistDate(subject.getRegistDate());
            survey.setBirthDay(subject.getBirthDay());
            survey.setDateCreated(now);
            survey.setDateUpdated(now);
            this._repository.add(survey);
            return;
        }

        SurveyStatus exist = optional.get();
        exist.setKdst(survey.getKdst());
        exist.setKmchat(survey.getKmchat());
        exist.setKqchat(survey.getKqchat());
        exist.setSelsi(survey.getSelsi());
        exist.setCbcl(survey.getCbcl());
        exist.setAdos2mt(survey.getAdos2mt());
        exist.setAdos2m1(survey.getAdos2m1());
        exist.setAdos2m2(survey.getAdos2m2());
        exist.setAdos2m3(survey.getAdos2m3());
        exist.setSrs2(survey.getSrs2());
        exist.setAdir(survey.getAdir());
        exist.setScqlifetime(survey.getScqlifetime());
        exist.setPres(survey.getPres());
        exist.setKcars2(survey.getKcars2());
        exist.setBedevelq(survey.getBedevelq());
        exist.setBedeveli(survey.getBedeveli());
        exist.setBedevelp(survey.getBedevelp());
        exist.setKvineland(survey.getKvineland());
        exist.setKbayley(survey.getKbayley());
        exist.setKwppsi(survey.getKwppsi());
        exist.setScqcurrent(survey.getScqcurrent());

        exist.setRegistDate(subject.getRegistDate());
        exist.setUserUpdated(survey.getUserUpdated());
        exist.setDateUpdated(now);

        surveyStatusList.add(exist);

        boolean result = this._repository.update(exist);
        LOGGER.debug("surveyStatusList={}", surveyStatusList);

        findSurveyDataBySubject(surveyStatusList);

    }

    public void changeProjectSeq(String systemId, String subjectId, long fromProjectSeq, long toProjectSeq)
            throws Exception {
        List<SurveyStatus> result = this._repository.query2(systemId, subjectId, fromProjectSeq); // 앞 Controller에서
        // projectSeq=4 일때만
        // dolabelling 실행되므로 4
        // 고정
        for (SurveyStatus item : result) {
            if (item.getProjectSeq() != toProjectSeq) {
                this._repository.updateProjectSeq(item.getId(), toProjectSeq);
            } else {
                return;
            }
        }
    }

    /**
     * 보류(4) to 정상(1), 고위험군(2), 자폐(3)
     *
     * @param systemId
     * @param subjectId
     * @param projectSeq
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void doLabelling(String systemId, String subjectId, long projectSeq) throws Exception {

        List<SurveyStatus> result = this._repository.query2(systemId, subjectId, ProjectSeq.HOLD.getSeq());
        for (SurveyStatus item : result) {
            this._repository.updateProjectSeq(item.getId(), projectSeq);
        }

        /*
         * List<MChart> result = this._repository.query2(systemId, subjectId, 0);
         * try {
         * if (!result.isEmpty()) {
         * MChart lastItem = result.get(result.size() - 1);
         * this._repository.updateProjectSeq(lastItem.getId(), projectSeq);
         * }
         * } catch (IndexOutOfBoundsException e) {
         * System.out.println("An error occurred: " + e.getMessage());
         * }
         */
    }

    // public void saveSurveyDataBySubject(String subjectGroupCd) {
    // ObjectMapper objectMapper = new ObjectMapper();
    // String[] surveyKindCdList = { "K-DST", "KM-CHAT", "K-QCHAT", "SELSI", "CBCL",
    // "ADOS-2(Mod T)", "ADOS-2(Mod 1)",
    // "ADOS-2(Mod 2)", "SRS", "ADI-R", "SCQ(lifetime)", "PRES", "K-CARS-2",
    // "BEDEVEL-Q", "BEDEVEL-I",
    // "BEDEVEL-P", "K-Vineland", "K-Bayley-III", "K-WPPSI-IV", "SCQ(current)" };
    // // Define the directory to save the JSON files
    // String directoryPath = "C:/Users/my.lee/SURVEY/";
    // final String finalSubjectGroupCd; // Declare final variable
    // // Assign value based on condition
    // if (ProjectSeqNm.NORMAL.name().equals(subjectGroupCd)) {
    // finalSubjectGroupCd = ProjectSeqNm.NORMAL.getValue();
    // } else if (ProjectSeqNm.ASD_HIGH.name().equals(subjectGroupCd)) {
    // finalSubjectGroupCd = ProjectSeqNm.ASD_HIGH.getValue();
    // } else if (ProjectSeqNm.ASD.name().equals(subjectGroupCd)) {
    // finalSubjectGroupCd = ProjectSeqNm.ASD.getValue();
    // } else {
    // finalSubjectGroupCd = ProjectSeqNm.HOLD.getValue();
    // // Map to store survey data grouped by SUBJECT_NO and numOfTimes
    // Map<String, Map<Integer, Map<String, ArrayNode>>> subjectMap = new
    // HashMap<>();
    // // Loop through each survey kind code
    // for (String surveyKindCd : surveyKindCdList) {
    // List<UrbanSurveyData> surveyList =
    // _repository.findSurveyListbySubject(subjectGroupCd, surveyKindCd);
    // // Group survey data by SUBJECT_NO and numOfTimes
    // for (UrbanSurveyData survey : surveyList) {
    // String subjectNo = survey.getSubjectNo();
    // int numOfTimes = survey.getNumOfTimes();
    // ObjectNode surveyNode = objectMapper.createObjectNode();
    // surveyNode.put("MARK_NO", survey.getMarkNo());
    // surveyNode.put("QUESTION_NM", survey.getQuestionNm());
    // surveyNode.put("ANSWER", survey.getAnswer());
    // Map<Integer, Map<String, ArrayNode>> numOfTimesMap =
    // subjectMap.getOrDefault(subjectNo,
    // new HashMap<>());
    // Map<String, ArrayNode> surveyKindMap = numOfTimesMap.getOrDefault(numOfTimes,
    // new HashMap<>());
    // ArrayNode surveyArray = surveyKindMap.getOrDefault(surveyKindCd,
    // objectMapper.createArrayNode());
    // surveyArray.add(surveyNode);
    // surveyKindMap.put(surveyKindCd, surveyArray);
    // numOfTimesMap.put(numOfTimes, surveyKindMap);
    // subjectMap.put(subjectNo, numOfTimesMap);
    // }
    // }
    // // Save survey data by SUBJECT_NO and numOfTimes into separate JSON files
    // subjectMap.forEach((subjectNo, numOfTimesMap) -> {
    // numOfTimesMap.forEach((numOfTimes, surveyKindMap) -> {
    // ObjectNode subjectNode = objectMapper.createObjectNode();
    // subjectNode.put("SubjectId", subjectNo);
    // subjectNode.put("ProjectSeq", finalSubjectGroupCd);
    // subjectNode.put("TrialIndex", numOfTimes);
    // ArrayNode surveyDataArray = objectMapper.createArrayNode();
    // surveyKindMap.forEach((surveyKindCd, surveyArray) -> {
    // ObjectNode surveyKindNode = objectMapper.createObjectNode();
    // surveyKindNode.put("설문지 유형", surveyKindCd);
    // surveyKindNode.set("SURVEY_DATA", surveyArray);
    // surveyDataArray.add(surveyKindNode);
    // });
    // subjectNode.set("SURVEY_DATA", surveyDataArray);
    // String fileName = "T_SURVEY_" + subjectNo + "_" + finalSubjectGroupCd + "_" +
    // numOfTimes + ".json";
    // try {
    // FileWriter fileWriter = new FileWriter(directoryPath + fileName);
    // objectMapper.writerWithDefaultPrettyPrinter().writeValue(fileWriter,
    // subjectNode);
    // fileWriter.close();
    // System.out.println("Survey data for SUBJECT_NO " + subjectNo + " with
    // numOfTimes " + numOfTimes
    // + " saved to " + fileName);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // });
    // });
    // }
    // }
    private Map<String, Object> processSelection(Map<String, Object> map, String selectionKey) {
        boolean status = true;
        // Map<String, Object> selectionSet = new HashMap<>();
        Map<String, Object> selectionSet = new HashMap<>();
        List<Map<String, Object>> selectionDataSet = new ArrayList<>();
        List<List<Map<String, Object>>> processedDataSet2 = new ArrayList<>();
        if (map.containsKey(selectionKey)) {

            Map<String, Object> selection = (Map<String, Object>) map.get(selectionKey);
            String subjectId = (String) selection.get("subjectId");
            long projectSeq = (long) selection.get("projectSeq");
            int trialIndex = (int) selection.get("trialIndex");

            // boolean selectionTrue = selection.containsKey(true);
            // boolean selectionfalse
            status = selection.containsValue(false) ? false : true;

            if (!(selection.containsValue(false) || selection.containsValue(true))) {
                status = false;
            }

            LOGGER.info("{}진입", selectionKey);
            LOGGER.info("{}SubjectId = {}, trialIndex = {}, status = {}", selectionKey, subjectId, trialIndex, status);
            selection.put("status", status);

            // processedDataSet.add(selection);
            List<DataSummary> summary = _dataSummaryRepository.findBySubject(subjectId, projectSeq, trialIndex);

            if (!summary.isEmpty()) {
                boolean existingStatus = summary.stream()
                        .map(selectionKey.equals("firstSelection") ? DataSummary::getFirstSelection
                                : DataSummary::getSecondSelection)
                        .findFirst()
                        .orElse(false);

                if (status != existingStatus) {
                    this._dataCommonService.updateDataSummaryColumn(
                            selectionKey.equals("firstSelection") ? DataSummaryColumn.FirstSelection
                            : DataSummaryColumn.SecondSelection,
                            subjectId, projectSeq, trialIndex, status);
                }
            }
            if (!status) {
                selectionSet.put(selectionKey, selection);
            }
            // selectionSet.put(selectionKey, selection);
        }
        return selectionSet;
    }

    public Collection<List<Map<String, Object>>> findSurveyDataBySubject(List<SurveyStatus> surveyStates) {

        LOGGER.info("surveyStates = {}", surveyStates);
        List<Map<String, Object>> resultList = new ArrayList<>();
        Collection<List<Map<String, Object>>> resultCollection = new ArrayList<>();
        if (!surveyStates.isEmpty()) {

            surveyStates.forEach(status -> {

                Map<String, Object> resulMap = processSurveyStatus(status);
                resultList.add(resulMap);
            });
            LocalDate test = LocalDate.of(2024, 4, 30);
            LOGGER.info("testDate:{}", test.minusMonths(6).toString());
            LOGGER.info("findSurveyMethod 진입={}", surveyStates.toString());
        }

        resultCollection.add(resultList);
        LOGGER.info("resultMapCollection = {}", resultCollection);
        return resultCollection;
    }

    public Map<String, Object> processedDataDetail(List<Map<String, Object>> processedData, String subjectId, int month) {
        List<Map<String, Object>> processedDataSet = new ArrayList<>();
        Map<String, Object> declaredSubjectIdMap = new HashMap<>();
        processedData.forEach(map -> {
            Map<String, Object> firstSelectionSet = processSelection(map, "firstSelection");
            Map<String, Object> secondSelectionSet = processSelection(map, "secondSelection");
            processedDataSet.add(firstSelectionSet);
            processedDataSet.add(secondSelectionSet);
        });
        declaredSubjectIdMap.put("subjectId", subjectId);
        declaredSubjectIdMap.put("month", month);
        declaredSubjectIdMap.put("result", processedDataSet);
        LOGGER.info("processedDataSet2={}", processedDataSet);
        LOGGER.info("declaredSubjectIdMap ={}", declaredSubjectIdMap);
        return declaredSubjectIdMap;
    }

    private Map<String, Object> processSurveyStatus(SurveyStatus status) {
        Map<String, Object> processedDataSetMap = new HashMap<>();

        try {
            LOGGER.info("Processing survey status for subject ID {}: month={}", status.getSubjectId(),
                    SurveyKeysStatus.getMonthsOfAge(status.getRegistDate(), status.getBirthDay()));

            List<Map<String, Object>> processedData = processStatus(status.toMap(), status.getProjectSeq(),
                    status.getTrialIndex(), status.getSubjectId(),
                    SurveyKeysStatus.getMonthsOfAge(status.getRegistDate(), status.getBirthDay()));
            processedDataSetMap = processedDataDetail(processedData, status.getSubjectId(), SurveyKeysStatus.getMonthsOfAge(status.getRegistDate(), status.getBirthDay()));

        } catch (Exception e) {
            LOGGER.error("Error occurred while processing survey status for subject ID {}: {}",
                    status.getSubjectId(), e.getMessage(), e);
        }
        return processedDataSetMap;
    }

    public List<Map<String, Object>> processStatus(Map<String, Object> entry,
            long projectSeq, int trialIndex, String subjectId,
            int month) {
        List<Map<String, Object>> processedDataSet = new ArrayList<>();

        Map<String, Object> status = SurveyKeysStatus.getSurveyKeys(new long[]{projectSeq},
                new int[]{trialIndex},
                month);
        LOGGER.info("[urban]statetoMap : {}", entry);
        LOGGER.info("Retrieved survey keys: {}", status);

        Map<String, Object> firstSelectionMap = new HashMap<>();
        Map<String, Object> secondSelectionMap = new HashMap<>();

        firstSelectionMap.put("subjectId", subjectId);
        firstSelectionMap.put("projectSeq", projectSeq);
        firstSelectionMap.put("trialIndex", trialIndex);

        secondSelectionMap.put("subjectId", subjectId);
        secondSelectionMap.put("projectSeq", projectSeq);
        secondSelectionMap.put("trialIndex", trialIndex);

        if (!status.isEmpty()) {
            status.forEach((key, value) -> {
                if (!key.equals("subjectId") && !key.equals("projectISeq") && !key.equals("trialIndex")) {
                    if (entry.containsKey(key)) {
                        boolean state = entry.get(key).equals(value);
                        if (value.equals("S")) {
                            LOGGER.info("Optional MATCH: key={}, value={}, dbValue={}", key, value, entry.get(key));
                            if (entry.get(key).equals("Y")) {
                                state = true;
                            }
                            if (key.equals(SurveyKeysStatus.ADOS_2_MOD_T_S.getKey()) || key.equals(SurveyKeysStatus.ADOS_2_MOD_1_S.getKey())
                                    || key.equals(SurveyKeysStatus.ADOS_2_MOD_2_S.getKey()) || key.equals(SurveyKeysStatus.ADOS_2_MOD_3_S.getKey())) {
                                secondSelectionMap.put(SurveyKeysStatus.ADOS_2_MOD_T_S.getKey(), true);
                                secondSelectionMap.put(SurveyKeysStatus.ADOS_2_MOD_1_S.getKey(), true);
                                secondSelectionMap.put(SurveyKeysStatus.ADOS_2_MOD_2_S.getKey(), true);
                                secondSelectionMap.put(SurveyKeysStatus.ADOS_2_MOD_3_S.getKey(), true);
                            } else {
                                secondSelectionMap.put(key, state);
                            }

                        } else if (value.equals("Y")) {
                            LOGGER.info("SYNK MATCH: key={}, value={}, dbValue={}", key, value, entry.get(key));
                            if (key.equals(SurveyKeysStatus.SELSI.getKey()) || key.equals(SurveyKeysStatus.PRES.getKey())) {
                                LOGGER.info("selsi or pres [key] : {}", key);
                                LOGGER.info("selsi or pres [getKey] : {},{}", SurveyKeysStatus.SELSI.getKey(), SurveyKeysStatus.PRES.getKey());
                                firstSelectionMap.put(SurveyKeysStatus.SELSI.getKey(), true);
                                firstSelectionMap.put(SurveyKeysStatus.PRES.getKey(), true);
                            } else if (key.equals(SurveyKeysStatus.ADOS_2_MOD_T_Y.getKey()) || key.equals(SurveyKeysStatus.ADOS_2_MOD_1_Y.getKey())
                                    || key.equals(SurveyKeysStatus.ADOS_2_MOD_2_Y.getKey()) || key.equals(SurveyKeysStatus.ADOS_2_MOD_3_Y.getKey())) {
                                LOGGER.info("ONE OF ADOS [key] : {}", key);

                                firstSelectionMap.put(SurveyKeysStatus.ADOS_2_MOD_T_Y.getKey(), true);
                                firstSelectionMap.put(SurveyKeysStatus.ADOS_2_MOD_1_Y.getKey(), true);
                                firstSelectionMap.put(SurveyKeysStatus.ADOS_2_MOD_2_Y.getKey(), true);
                                firstSelectionMap.put(SurveyKeysStatus.ADOS_2_MOD_3_Y.getKey(), true);
                            } else {
                                firstSelectionMap.put(key, state);
                            }
                        }
                    }
                }
            });
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("firstSelection", firstSelectionMap);
        resultMap.put("secondSelection", secondSelectionMap);
        processedDataSet.add(resultMap);

        LOGGER.info("processedDataSet={}", processedDataSet);
        return processedDataSet;
    }

    private ResponseEntity<JsonResponseObject> ErrorResponseEntity(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new JsonResponseObject(false, message));
    }

    private int determineDataSetIndex(int dataSetCount, int totalKeys) {
        // Calculate the index by dividing the total number of keys processed by the
        // number of data sets
        return totalKeys % dataSetCount;
    }

    public LinkedTreeMap<String, Object> requestGetUrbanSurvey(String researchCd, String subjectId, String note)
            throws ServiceException {
        // 참가자 정보 없이 subjectId를 저장하는 목록
        List<String> subjectsWithoutParticipantInfo = new ArrayList<>();

        try {
            // UriComponentsBuilder를 사용하여 URI 생성
            URI uri = UriComponentsBuilder.fromUriString(targetUrl)
                    .path(REQUEST_SURVEY_RESULT_URI)
                    .queryParam("researchCd", researchCd)
                    .queryParam("subjectId", subjectId)
                    .build()
                    .toUri();

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-KEY", authKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HTTP GET 요청 수행
            ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers),
                    String.class);

            // 상태 코드 확인
            if (!result.getStatusCode().is2xxSuccessful()) {
                throw new ServiceException(
                        "도시 조사 데이터를 가져오는 데 실패했습니다. 상태 코드: " + result.getStatusCode() + ", 응답 본문: "
                        + result.getBody());
            }

            // 응답 본문 파싱
            String body = result.getBody();

            if (body == null || body.isEmpty()) {
                throw new ServiceException("응답 본문이 비어 있습니다.");
            }
            // Gson 라이브러리를 사용하여 LinkedTreeMap으로 파싱
            Gson gson = new Gson();
            LinkedTreeMap<String, Object> resultDatas = gson.fromJson(body, LinkedTreeMap.class);

            // 결과 데이터에 body 추가
            resultDatas.put("body", body);

            int statusCode = ((Double) resultDatas.get("statusCode")).intValue(); // 숫자형이므로 형변환 필요
            String message = (String) resultDatas.get("message");

            // 응답 정보 출력
            LOGGER.info("statusCode: {}", statusCode);
            LOGGER.info("message: {}", message);
            LinkedTreeMap<String, Object> resultData = new LinkedTreeMap<>();
            if (20000 == statusCode) {
                resultData = (LinkedTreeMap<String, Object>) resultDatas
                        .get("resultData");
                // 성공적인 응답, 필요에 따라 데이터를 여기서 파싱.
                LinkedTreeMap<String, Object> participantInfo = (LinkedTreeMap<String, Object>) resultData
                        .get("ParticipantInfo");
                LOGGER.info("참가자 정보: {}", participantInfo);

                if (participantInfo == null) {
                    LOGGER.error("resultData에서 ParticipantInfo를 찾을 수 없습니다.");
                    // 참가자 정보 없는 subjectId를 목록에 추가
                    subjectsWithoutParticipantInfo.add(subjectId);
                    return null;
                }
                // ParticipantInfo 배열의 각 객체에 note 추가
                participantInfo.put("Note", note != null ? note : "-");
                return resultData;
            } else {
                throw new ServiceException(
                        "설문지 데이터를 가져오는 데 실패했습니다. 상태 코드: " + statusCode + ", 메시지: " + message);
            }
        } catch (RestClientException e) {
            throw new ServiceException("HTTP 요청을 보내는 중 오류 발생: " + e.getMessage(), e);
        } catch (ServiceException e) {
            // 오류 로깅 및 계속 진행
            LOGGER.error("subjectId {}에 대한 도시 조사 데이터 가져오기 실패", subjectId);
            LOGGER.error("오류: {}", e.getMessage());
            // 참가자 정보 없는 subjectId를 목록에 추가
            subjectsWithoutParticipantInfo.add(subjectId);
            // 다른 subjectId 처리를 계속하기 위해 null 반환
            return null;
        }
    }

}
