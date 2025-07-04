package com.ecoinsight.bdsp.asd.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.tomcat.jni.Local;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.stereotype.Service;

import com.ecoinsight.bdsp.asd.entity.DataSummary;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.OmniSubject;
import com.ecoinsight.bdsp.asd.model.Result;
import com.ecoinsight.bdsp.asd.repository.IAsdDataCommonRepoistory;
import com.ecoinsight.bdsp.asd.scheduling.AsdBaseScheduler;
import com.ecoinsight.bdsp.asd.scheduling.utils.CallRestApi;
import com.ecoinsight.bdsp.asd.scheduling.utils.TextMessageSender;
import com.ecoinsight.bdsp.core.repository.ISchedulerLogRepository;
import com.ecoinsight.bdsp.core.scheduling.TaskResult;
import com.google.gson.Gson;

@Service(ForcedUdateBatchService.ID)
public class ForcedUdateBatchService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String ID = "ForcedUdateBatchService";
    private static final String SYSTEM_ID = "ASD";

    @Value("${omni.host}")
    private String omniHost;

    @Autowired
    private com.ecoinsight.bdsp.asd.repository.IAsdProjectSubjectRepository _projectSubjectRepository;

    @Autowired
    protected ISchedulerLogRepository _schedulerLogRepository;

    @Autowired
    private MongoTemplate _mongoTemplate;

    @Autowired
    private DataCommonService _dataCommonService;

    @Autowired
    IAsdDataCommonRepoistory _dataCommonRepository;

    @Autowired
    private CallRestApi _callRestApi;

    @Autowired
    private TextMessageSender sender;

    public Map<String, Object> forcedSubjectTrialUpdateBatch(Collection<AsdProjectSubject> subjects) {
        java.util.Map<String, Object> results = new HashedMap<>();
        try {
            LocalDateTime currentDate = LocalDateTime.now();
            String formatDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<String> subjectIds = subjects.stream()
                    .map(AsdProjectSubject::getSubjectId)
                    .collect(Collectors.toList());
            // Collection<AsdProjectSubject> subjectsInMongoDB =
            // _projectSubjectRepository.findAllBySubjectIds(SYSTEM_ID,
            // subjectIds);

            // 집계 결과를 목록으로 변환
            // var list = aggregationResult.into(new ArrayList<>());
            // Iterable 리스트를 자바 컬렉션으로 변환
            // List<Document> documentList = new ArrayList<>();
            Collection<AsdProjectSubject> modifiedSubjects = new ArrayList<>();

            for (AsdProjectSubject subject : subjects) {
                String subjectId = subject.getSubjectId();
                int trialIndex = subject.getTrialIndex();

                int monthBetween = getMonth(trialIndex); // trialInex 값으로 monthBetween 값을 가져온다
                // MONTHBETWEEN은
                processSubject(monthBetween, subjectId, trialIndex, modifiedSubjects);
            }

            // for (AsdProjectSubject results : subjectsInMongoDB) {

            // String subjectId = results.getSubjectId();
            // int trialIndex = results.getTrialIndex();

            // int monthBetween = getMonth(trialIndex); // trialInex 값으로 monthBetween 값을
            // 가져온다
            // // MONTHBETWEEN은
            // processSubject(monthBetween, subjectId, trialIndex, modifiedSubjects);
            // }

            // 수정된 대상 목록을 MongoDB에 일괄 저장
            // _mongosaveAll(modifiedSubjects);
            Collection<AsdProjectSubject> savedSubjects = _projectSubjectRepository.saveAll(modifiedSubjects);

            List<String> modifiedSubjectIds = new ArrayList<>();
            savedSubjects.forEach(subject -> modifiedSubjectIds.add(subject.getSubjectId()));
            LOGGER.info("수정된 대상의 subjectId 목록 ={}", modifiedSubjectIds);

            results.put("success", !modifiedSubjects.isEmpty());
            results.put("modifiedSubjects", modifiedSubjects);
        } catch (Exception e) {
            LOGGER.error("An error occurred while updating subjects: " + e.getMessage(), e);

        }

        return results;

    }

    private void processSubject(int monthBetween, String subjectId, int trialIndex,
            Collection<AsdProjectSubject> modifiedSubjects) {
        // 대상 조회
        Collection<AsdProjectSubject> resultList = _projectSubjectRepository.findAllBySubjectId(SYSTEM_ID, subjectId);

        // 대상 처리 로깅
        LOGGER.info("대상자 정보를 조회했습니다. subjectId={}", subjectId);

        // 각 대상 처리
        for (AsdProjectSubject projectSubject : resultList) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime registDate = LocalDate.parse(projectSubject.getRegistDate(), formatter).atTime(11, 0);
            LOGGER.info("registDate={}", registDate);
            LocalDateTime newRegistDate = calculateCDate(monthBetween, registDate);

            // In your processSubject method
            int trialIndexs = isBetween(monthBetween); // Determine the trialIndex // monthBetween에 따라 trialIndex 설정
            // 대상 처리 전에 MongoDB 업데이트가 필요한지 확인
            boolean isMongoUpdateNeeded = true; // MongoDB 업데이트가 필요한지 여부
            if (projectSubject.getSubjectId().equals(subjectId) &&
                    projectSubject.getTrialIndex() == trialIndexs) {

                LOGGER.info("No MongoDB update needed for subjectId: {} and trialIndex: {}", subjectId, trialIndex);
                isMongoUpdateNeeded = false; // 동일한 subjectId와 trialIndex를 가진 데이터가 이미 리스트에 있는 경우 MongoDB 업데이트가 필요하지 않음
            }

            if (trialIndexs != 0) { // Check if a valid trialIndex is returned
                updateDataSummaries(subjectId, newRegistDate, registDate, trialIndexs); // Check MySQL trialIndex
                                                                                        // regardless
                // of isMongoUpdateNeeded
                if (isMongoUpdateNeeded) { // Check if MongoDB update is needed
                    projectSubject.setDateTrialIndex(LocalDateTime.now());
                    projectSubject.setTrialIndex(trialIndexs); // Set trialIndex if MongoDB update is needed
                    // 수정된 프로젝트 대상을 리스트에 추가
                    modifiedSubjects.add(projectSubject);
                }
            }

        }
    }

    private void updateDataSummaries(String subjectId, LocalDateTime newRegistDate, LocalDateTime registDate,
            int trialIndex) {
        List<DataSummary> dataSummaries = _dataCommonRepository.findbyLastTrialIndexDatSummaryList(subjectId);

        if (!dataSummaries.isEmpty()) {
            for (DataSummary result : dataSummaries) {
                int resultTrialIndex = result.getTrialIndex();
                String id = result.getSubjectId();

                if (trialIndex == resultTrialIndex) { // 기
                    LOGGER.info("resultTrialIndex == trialIndex Enabled.... resultTrialIndex ={} , trialIndex ={}  ",
                            resultTrialIndex, trialIndex);
                    List<DataSummary> stateList = checkedIsExists(id, trialIndex);
                    if (!stateList.isEmpty()) {
                        for (DataSummary list : stateList) {
                            _dataCommonRepository.addForcedSubjectForTrialIndex(list);
                        }

                        LOGGER.info(
                                "Attempting to add Missing to date updating perid trialIndex. SubjectId={}, TrialIndex={} ",
                                id, trialIndex);

                    }

                }

                // resultTrialIndex가 1이고 trialIndex가 2인 경우: 2을 저장
                // resultTrialIndex가 2이고 trialIndex가 3인 경우: 3을 저장
                // resultTrialIndex가 3이고 trialIndex가 4인 경우: 4를 저장
                // resultTrialIndex가 2이고 trialIndex가 4인 경우: 3과 4를 모두 저장

                if (resultTrialIndex < trialIndex) { // 1 < 3
                    LOGGER.info("resultTrialIndex < trialIndex Enabled.... resultTrialIndex ={} , trialIndex ={} ",
                            resultTrialIndex, trialIndex);
                    // for (int i = resultTrialIndex == 1 ? 1 : resultTrialIndex + 1; i <=
                    // trialIndex; i++) {
                    for (int i = resultTrialIndex + 1; i <= trialIndex; i++) {
                        LOGGER.info("result={}", result);
                        // DataSummary newDataSummary = new DataSummary();
                        // result.setSubjectId(id);
                        // newDataSummary.setTrialIndex(i);
                        // newDataSummary.setCDate(calculateCDate(getMonth(i), registDate));
                        // newDataSummary.setUDate(LocalDateTime.now());
                        // newDataSummary.setDateTrialindex(LocalDateTime.now());

                        DataSummary newDataSummary = new DataSummary();
                        newDataSummary.SummaryUpdateForm(result);
                        newDataSummary.setTrialIndex(i);
                        newDataSummary.setCDate(calculateCDate(getMonth(i), registDate));
                        newDataSummary.setUDate(LocalDateTime.now());
                        newDataSummary.setDateTrialIndex(calculateCDate(getMonth(i), registDate));

                        // Add a log before saving the newDataSummary
                        LOGGER.info("Attempting to add new DataSummary for SubjectId={} and trial index {}.", id, i);

                        // Save the newDataSummary
                        int exsists = _dataCommonRepository.addForcedSubjectForTrialIndex(newDataSummary);

                        // Get the subjectId from newDataSummary
                        String addSubjectId = newDataSummary.getSubjectId();
                        int addTrialIndex = newDataSummary.getTrialIndex();
                        // Check if the addition was successful and log accordingly
                        if (exsists > 0) {
                            LOGGER.info("[1]New DataSummary added successfully for SubjectId={} and trial index {}.",
                                    id,
                                    i);
                            LOGGER.info("[2]New DataSummary added successfully for SubjectId={} and trial index {}.",
                                    addSubjectId,
                                    addTrialIndex);
                        } else {
                            LOGGER.error(
                                    "Failed to add new DataSummary for SubjectId={} and trial index {}. Adding the DataSummary failed.",
                                    id, i);
                        }
                    }
                }
            }
        }
    }

    public int isBetween(int monthBetween) {
        if (monthBetween >= 6 && monthBetween < 12) {
            return 2;
        } else if (monthBetween >= 12 && monthBetween < 18) {
            return 3;
        } else if (monthBetween >= 18 && monthBetween < 24) {
            return 4;
        } else {
            return 0; // Return 0 if monthBetween doesn't fall into any range
        }
    }

    // calculate 범위 수정 (6, 12, 18) -> 범위 조건에 맞게 수정
    private LocalDateTime calculateCDate(int monthBetween, LocalDateTime registDate) {
        if (monthBetween >= 6 && monthBetween < 12) {
            return registDate.plusMonths(6);
        } else if (monthBetween >= 12 && monthBetween < 18) {
            return registDate.plusMonths(12);
        } else if (monthBetween >= 18 && monthBetween < 24) {
            return registDate.plusMonths(18);
        } else {
            return registDate;
        }
    }

    private int getMonth(int trialIndex) {
        int month;
        switch (trialIndex) {
            case 2:
                month = 6;
                break;
            case 3:
                month = 12;
                break;
            case 4:
                month = 18;
                break;
            default:
                month = -1;
                break;
        }
        return month;
    }

    private Collection<AsdProjectSubject> getListForMonth(int month) {
        LocalDate date = LocalDate.now().minusMonths(month); // 월 단위 날짜 빼는 function
        LOGGER.info("-> date: {}", date);

        Collection<AsdProjectSubject> list = _projectSubjectRepository.findByRegistDate(date.toString());

        list.forEach(s -> {

            s.getPhoneNumber();
        });

        list.forEach(subject -> {
            Integer trialIndex = getTrialIndex(month);
            if (trialIndex != null) {
                subject.setTrialIndex(trialIndex);
                subject.setDateTrialIndex(LocalDateTime.now());

            }
        });

        return list;
    }

    private Integer getTrialIndex(int month) {
        switch (month) {
            case 6:
                return 2;
            case 12:
                return 3;
            case 18:
                return 4;
            default:
                return null;
        }
    }

    private List<DataSummary> checkedIsExists(String subjectId, int trialIndex) {
        List<DataSummary> exists = new ArrayList<>(); // Initialize the list to hold DataSummary objects
        boolean found = false; // Track whether a non-null DataSummary is found
        int num = 0; // Variable to store the value of i

        // Loop from 1 to trialIndex - 1
        DataSummary data = null;
        for (int i = 1; i < trialIndex; i++) {
            data = new DataSummary();
            data = this._dataCommonRepository.findByTrialIndexList(subjectId, i);
            LOGGER.info("data={}", data);

            // Check if data is null
            if (data == null) {
                found = false;
                num = i; // Store the value of i when data is null
            } else {
                found = true; // Set found to true if a non-null DataSummary is found
            }
        }

        // If no non-null DataSummary is found, create a new DataSummary
        if (!found) {
            data = new DataSummary();
            // Fetch subject information from the repository
            var subjects = _projectSubjectRepository.findAllBySubjectId(SYSTEM_ID, subjectId);
            AsdProjectSubject subject = subjects.stream().findFirst().orElse(null);

            if (subject != null) {
                LocalDateTime time = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime registDate = LocalDate.parse(subject.getRegistDate(), formatter).atTime(2, 0);

                if (num == 1) {
                    // Set properties for the first trial index
                    String rgDates = time.format(formatter);

                    data.projectSubjectForm(subject);
                    data.setRegistDate(rgDates);
                    data.setTrialIndex(num);
                    data.setCDate(time);
                    data.setUDate(time);
                } else {
                    int month = getMonth(num);
                    LOGGER.info("i 값 :={} , month={} ", num, month);
                    LocalDateTime newDate = calculateCDate(month, registDate);
                    LOGGER.info("checkIsExists[EXCUTE-METHOD] : subject={} , trialIndex[num] ={} ,CDate ={}", subjectId,
                            num, newDate);

                    // Set properties for subsequent trial indexes
                    LOGGER.info("list subject ={} ", subject);
                    data.projectSubjectForm(subject);
                    data.setTrialIndex(num);
                    data.setCDate(newDate);
                    data.setDateTrialIndex(newDate);
                    data.setUDate(time);
                    LOGGER.info("data from setAsdProjectSubject ={} ", data);
                }
                // Add the new DataSummary to the exists list
                exists.add(data);
            }
            LOGGER.info("exists data  ={} ", exists);
        }

        return exists;
    }

    private JSONObject makeOmniJsonObject(Collection<AsdProjectSubject> subjects) {
        List<OmniSubject> omniSubjects = new ArrayList<>();

        for (AsdProjectSubject ps : subjects) {
            OmniSubject omniSubject = new OmniSubject();
            omniSubject.setId(ps.getSubjectId());
            omniSubject.setState(ps.getState());
            omniSubject.setName(ps.getName());
            omniSubject.setGender(ps.getGender());
            omniSubject.setBirth(ps.getBirthDay());
            omniSubject.setPhone(ps.getPhoneNumber());
            omniSubject.setAgreementDate(ps.getRegistDate());
            omniSubject.setResearchStartDate(ps.getStartDate());
            omniSubject.setResearchEndDate(ps.getEndDate());
            omniSubject.setProjectSeq((int) ps.getProjectSeq());
            omniSubject.setTrialIndex(ps.getTrialIndex());

            omniSubjects.add(omniSubject);
        }

        Gson gson = new Gson();
        String list = gson.toJson(omniSubjects);
        JSONArray jsonArray = new JSONArray(list);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("patients", jsonArray);

        return jsonObject;
    }

}