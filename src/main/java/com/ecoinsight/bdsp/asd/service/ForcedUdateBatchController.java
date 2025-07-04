package com.ecoinsight.bdsp.asd.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ecoinsight.bdsp.asd.OmniApi;
import com.ecoinsight.bdsp.asd.entity.DataSummary;
import com.ecoinsight.bdsp.asd.entity.SmsScheduler;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.OmniSubject;
import com.ecoinsight.bdsp.asd.model.Result;
import com.ecoinsight.bdsp.asd.repository.IAsdDataCommonRepoistory;
import com.ecoinsight.bdsp.asd.repository.ISmsSchudlerHistoryRepository;
import com.ecoinsight.bdsp.asd.scheduling.utils.CallRestApi;
import com.ecoinsight.bdsp.asd.scheduling.utils.TextMessageSender;
import com.ecoinsight.bdsp.core.repository.ISchedulerLogRepository;
import com.ecoinsight.bdsp.core.web.BaseApiController;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;
import com.google.gson.Gson;
import io.swagger.annotations.ApiOperation;

@RestController
public class ForcedUdateBatchController extends BaseApiController {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    // public static final String ID = "ForcedUdateBatchService";
    private static final String SYSTEM_ID = "ASD";
    protected String taskName;
    protected String systemId;
    private String _hostname;

    @Value("${omni.host}")
    private String omniHost;

    @Autowired
    private com.ecoinsight.bdsp.asd.repository.IAsdProjectSubjectRepository _projectSubjectRepository;

    @Autowired
    protected ISchedulerLogRepository _schedulerLogRepository;

    @Autowired
    private MongoTemplate _mongoTemplate;

    @Autowired
    private ISmsSchudlerHistoryRepository _smsSchedulerHistory;

    @Autowired
    IAsdDataCommonRepoistory _dataCommonRepository;

    @Autowired
    private ForcedUdateBatchService _forcedUdateBatchService;

    @Autowired
    private CallRestApi _callRestApi;

    @Autowired
    private TextMessageSender sender;

    public String getTaskName() {
        return this.taskName;
    }

    @PostConstruct
    public void startup() throws UnknownHostException {
        this.taskName = "SubjectUpdateSMSScheduler";
        this._hostname = InetAddress.getLocalHost().getHostName();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> startup hostname=" + this._hostname);
        }
    }

    // @PostMapping(path = "/api/v1/subject/to-omni/result")
    @ApiOperation(value = "forced 대상자 정보 업데이트 API ", notes = "Period missing Issue TrialIndex update Forced대상 ")
    @GetMapping(path = "/api/v1/subject/forced/trialIndex/result")
    public ResponseEntity<JsonResponseObject> updateTrialIndexPeriodDate(
            @RequestParam(required = false) String subjectId) {
        // final TaskResult result = new TaskResult(taskName, LocalDateTime.now());
        // result.setTaskRunner(getClass().getName());
        // final List<Exception> exceptions = new ArrayList<Exception>();
        systemId = super.getSystemId();
        Result results = new Result();
        try {
            LOGGER.info("-> Forced SubjectUpdate & SMSScheduler.doPerform....");
            Collection<AsdProjectSubject> combinedList = combinedList(subjectId);

            processDataSummaries(combinedList);

            results.setSuccess(true);
            results.setMessage("대상자 trialUndex 업데이트가 완료 되었습니다.");

        } catch (Exception e) {
            LOGGER.error("An error occurred in SubjectUpdateBatch.doPerform", e);
            // result.setMessage(String.format("Error while updating TrialIndex for
            // Subjects. [TASK={}]", taskName, e));
            results.setSuccess(false);
            results.setMessage("대상자 trialUndex 업데이트가 실패.");
        } finally {
            // result.setExceptions(exceptions);
            // result.setSucceeded(exceptions.size() <= 0);

            // 태스크 처리 결과 저장
            try {
                // this.asdBaseScheduler.reportResult(result);
                // super.reportResult(result);

                LOGGER.info("Inserted T_SchedulerLog. [TASK={}]", taskName);
            } catch (Exception ex) {
                LOGGER.error("Error while reporting result. [TASK={}]", taskName, ex);
            }

        }
        return ResponseEntity.ok(new JsonResponseObject(results.isSuccess(), results.getMessage(), results));
    }

    private Collection<AsdProjectSubject> combinedList(String subjectId) {
        Collection<AsdProjectSubject> combinedList = new ArrayList<>();
        combinedList.addAll(getListForMonth(subjectId, 6));
        combinedList.addAll(getListForMonth(subjectId, 12));
        combinedList.addAll(getListForMonth(subjectId, 18));

        return combinedList;

    }

    public List<DataSummary> getDataSummaryFindByTrialIndex(int trialIndex) throws Exception {
        List<DataSummary> dataSummaryList;

        switch (trialIndex) {
            case 2:
                dataSummaryList = this._dataCommonRepository.findListForUpdateTrialIndex2(trialIndex);
                break;
            case 3:
                dataSummaryList = this._dataCommonRepository.findListForUpdateTrialIndex3(trialIndex);
                break;
            case 4:
                dataSummaryList = this._dataCommonRepository.findListForUpdateTrialIndex4(trialIndex);
                break;
            default:
                throw new IllegalArgumentException("Invalid trial index: " + trialIndex);
        }

        return dataSummaryList;
    }

    public List<DataSummary> getDataSummaryFindByTrialIndexName(String subjectId, int trialIndex) throws Exception {
        List<DataSummary> dataSummaryList;

        switch (trialIndex) {
            case 2:
                dataSummaryList = this._dataCommonRepository.findListForUpdateTrialIndexName2(subjectId, trialIndex);
                break;
            case 3:
                dataSummaryList = this._dataCommonRepository.findListForUpdateTrialIndexName3(subjectId, trialIndex);
                break;
            case 4:
                dataSummaryList = this._dataCommonRepository.findListForUpdateTrialIndexName4(subjectId, trialIndex);
                break;
            default:
                throw new IllegalArgumentException(
                        "Invalid trial Index=>  subjectId:" + trialIndex + " trial index: " + trialIndex);
        }

        return dataSummaryList;
    }

    private void processDataSummaries(Collection<AsdProjectSubject> subjects) {
        try {
            LOGGER.info("processDataSummaries 메소드 진입");
            List<String> subjectIds = subjects.stream()
                    .map(AsdProjectSubject::getSubjectId)
                    .collect(Collectors.toList());
            LOGGER.info("subjectIds={}", subjectIds);
            Map<String, Object> result = null;

            if (!subjects.isEmpty()) {
                LOGGER.info("forcedSubjectTrialUpdateBatch service Excute method....", result);
                result = this._forcedUdateBatchService.forcedSubjectTrialUpdateBatch(subjects);
            } else {

                LOGGER.info("can not fid update to trialIndex ", subjects);
                return;
            }

            // for(String key : result.keySet()){
            //     String value = result.get(key).toString();
            //     System.out.println(key+" : "+value);
            // }

            for (String key : result.keySet()) {
                Object value = result.get(key);
                String subjectId = null;
                boolean sucess = false;
                List<AsdProjectSubject> listValue = new ArrayList<>();
                if (value instanceof List) {
                    listValue = (List<AsdProjectSubject>) value;
                    // 리스트인 경우, AsdProjectSubject 객체의 목록으로 형변환하여 처리
                } else if (value instanceof Boolean) {
                    // 부울 값은 문자열로 변환하지 않고 직접 출력
                    sucess = (boolean) value;
                    LOGGER.info("sucess={} ", sucess);
                } else {
                    // 다른 타입의 값은 필요에 따라 처리
                    System.out.println(key + ": " + value.toString());
                }

                if (sucess) {
                    // for (AsdProjectSubject subject : listValue) {
                    // 각 AsdProjectSubject 객체에 대해 처리
                    // LOGGER.info("subjectId={} ", subject.getSubjectId());
                    subjectId = (String) result.get("subjectId");
                    addSubjectForTrialIndex(subjectId, sucess);
                    _callRestApi.requestPost(this.omniHost + OmniApi.PATSTATUSINFO, makeOmniJsonObject(subjects));

                    /// }

                }
            }
            // for (AsdProjectSubject subject : subjects) {

            // List<DataSummary> trialIndexList = _dataCommonService
            // .getDataSummaryForTrialIndex(subject.getTrialIndex());

            // if (!trialIndexList.isEmpty()) {
            // for (DataSummary dataSummary : trialIndexList) {
            // if (subject.getSubjectId().equals(dataSummary.getSubjectId())) {
            // // Call the forcedSubjectTrialUpdateBatch method and store the result
            // java.util.Map<String, Object> result = this._forcedUdateBatchService
            // .forcedSubjectTrialUpdateBatch(subject.getSubjectId());

            // // Use the result as needed
            // boolean success = (boolean) result.get("success");

            // // Check if the update was successful
            // if (success) {
            // // Call getDataSummaryFindByTrialIndex for each trial index if the update was
            // // successful
            // for (int trialIndex = 2; trialIndex <= 4; trialIndex++) {
            // List<DataSummary> dataSummaryList =
            // getDataSummaryFindByTrialIndex(trialIndex);
            // // Proceed with sending SMS if necessary
            // sendSMSForDataSummary(dataSummaryList, subject);
            // }
            // }
            // }
            // }
            // }
            // }
        } catch (Exception e) {
            LOGGER.error("Error occurred while processing data summaries.", e);
        }
    }

    public void addSubjectForTrialIndex(String subjectId, boolean flag) throws Exception {
        // updateTrialIndexForDataSummary(2, subjectId, "2차");
        // updateTrialIndexForDataSummary(3, subjectId, "3차");
        // updateTrialIndexForDataSummary(4, subjectId, "4차");
        LOGGER.info("Attempting to find update TrialIndex to mysql DataSurmmary={}", flag);
        updateTrialIndexForDataSummary(subjectId, 2, "2차");
        updateTrialIndexForDataSummary(subjectId, 3, "3차");
        updateTrialIndexForDataSummary(subjectId, 4, "4차");
    }

    private void updateTrialIndexForDataSummary(String subjectId, int trialIndex, String trialDescription)
            throws Exception {
        List<DataSummary> results = getDataSummaryFindByTrialIndex(trialIndex);

        if (results.isEmpty()) {

            Collection<AsdProjectSubject> lists = findUpdateTrialIndexfromMongoDB(subjectId, trialIndex); // mongoDB -
                                                                                                          // using
            // getMonth method
            // calculate from
            // TrialInex Output
            // BetweemMonth
            List<DataSummary> updatedList = new ArrayList<>(); // 변경된 DataSummary 객체를 보관할 리스트

            if (!lists.isEmpty()) {
                for (AsdProjectSubject project : lists) {

                    // var data =
                    // _asdDataCommonRepository.findListForUpdateTrialIndexName2(project.getSubjectId(),
                    // project.getTrialIndex());

                    var data = this._dataCommonRepository.findOne(project.getSubjectId(), project.getProjectSeq(),
                            trialIndex);
                    if (!data.isEmpty()) {
                        DataSummary subjects = data.stream().findFirst().orElse(null);
                        if (project.getRegistDate() != subjects.getRegistDate()) {
                            LOGGER.info(
                                    "Attemping to update set RegistDate DataSummary. SubjectId={} ,beforeRegistDate={} ,mergetRegistDate={} ",
                                    subjects.getSubjectId(), subjects.getRegistDate(), project.getRegistDate());
                            // subjects.setRegistDate(project.getRegistDate());

                            // RegistDate 변경
                            subjects.setRegistDate(project.getRegistDate());

                            // 데이터 업데이트
                            boolean result = this._dataCommonRepository.updateDataSummaryForSyncFromMongoDB(subjects);

                            // 업데이트가 성공했을 때만 리스트에 추가
                            if (result) {
                                updatedList.add(subjects);
                            }
                        }
                    }

                }

            } else {
                LOGGER.info(
                        "Unable to find a data summary list for adding trialIndex " + trialIndex + trialDescription
                                + ".");
                return;
            }

            // 변경된 데이터를 SMS로 전송
            if (!updatedList.isEmpty()) {

                // List<SmsScheduler> list = new ArrayList<>();
                for (DataSummary summary : updatedList) {

                    List<SmsScheduler> list = this._smsSchedulerHistory.findBySenedSubjectSms(summary.getProjectSeq(),
                            summary.getSubjectId(), summary.getTrialIndex(), taskName);

                    if (!list.isEmpty()) {
                        LOGGER.info(
                                "Already to send SMS  [TASK={}] , subjectId={} ,trialIdnex={}", taskName,
                                summary.getTsakName(),
                                summary.getSubjectId(),
                                summary.getTrialIndex()
                                        + ".");
                    } else {
                        sendSMSForDataSummary(updatedList);

                    }

                }

            }

        }

        LOGGER.info("Inserting subjects for " + trialDescription + ":");
        for (DataSummary result : results) {
            LOGGER.info("-> insert subject => subjectId=" + result.getSubjectId() +
                    ", projectSeq=" + result.getProjectSeq() +
                    ", trialIndex=" + result.getTrialIndex());

            // result.setTrialIndex(trialIndex);
            // // this._asdDataCommonRepository.addSubjectForTrialIndex(result);
            // this._asdDataCommonRepository.addForcedSubjectForTrialIndex(result);
            List<SmsScheduler> list = this._smsSchedulerHistory.findBySenedSubjectSms(result.getProjectSeq(),
                    result.getSubjectId(), result.getTrialIndex(), taskName);
            if (!list.isEmpty()) {
                LOGGER.info(
                        "Already to send SMS  [TASK={}] , subjectId={} ,trialIdnex={}", taskName, result.getTsakName(),
                        result.getSubjectId(),
                        result.getTrialIndex()
                                + ".");
            } else {
                sendSMSForDataSummary(results);
            }

        }

    }

    // sendSMSForDataSummary(List<DataSummary> dataSummaryList, AsdProjectSubject
    // subject) {
    private void sendSMSForDataSummary(List<DataSummary> dataSummaryList) {
        try {
            if (shouldSendSMS(dataSummaryList)) {
                LOGGER.info("shouldSendSMS={}", shouldSendSMS(dataSummaryList));
                List<String> subjectIds = dataSummaryList.stream()
                        .map(DataSummary::getSubjectId)
                        .collect(Collectors.toList());
                Collection<AsdProjectSubject> subjectsInMongoDB = this._projectSubjectRepository
                        .findAllBySubjectIds(SYSTEM_ID, subjectIds);
                List<Map<String, String>> recipients = subjectsInMongoDB.stream()
                        .map(subj -> {
                            Map<String, String> recipientMap = new HashMap<>();
                            String phoneNumber = subj.getPhoneNumber();
                            LocalDateTime currentDateTime = LocalDateTime.now();
                            // Format date as "yyyyMMdd"
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                            String formattedDate = currentDateTime.format(dateFormatter);
                            LOGGER.info("formattedDate={}", formattedDate);
                            // Format time as "HHmm"
                            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");
                            String formattedTime = currentDateTime.format(timeFormatter);
                            LOGGER.info("formattedTime={}", formattedTime);
                            int month = getMonth(subj.getTrialIndex());
                            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                recipientMap.put("receiver", phoneNumber);
                                recipientMap.put("subjectId", subj.getSubjectId());
                                recipientMap.put("projectSeq", String.valueOf(subj.getProjectSeq()));
                                recipientMap.put("trialIndex", String.valueOf(subj.getTrialIndex()));
                                recipientMap.put("msg", buildSMSMessage(month));
                                recipientMap.put("result", "sucess");
                                recipientMap.put("taskName", getTaskName()); // 이게 곧 고유 식별 기능 task 식별 되는 seq가 될것 with
                                                                             // trialIndex
                                // projectSeq , trialIndex, taskName, result, datetimeExcuted
                                recipientMap.put("systemId", SYSTEM_ID);

                            }

                            return recipientMap;
                        })
                        // .filter(map -> !map.isEmpty()) // 전화번호가 없는 항목을 필터링하여 최종 결과에 유효한 전화번호만 포함
                        .filter(map -> map.get("receiver") != null && !map.get("receiver").toString().isEmpty()) // 전화번호가
                        .collect(Collectors.toList());

                try {
                    this.sender.sendExecuteTaskPeriod(recipients);
                    List<String> successSubjectIds = recipients.stream()
                            .map(map -> map.get("subjectId").toString() + ": " + map.get("receiver").toString())
                            .collect(Collectors.toList());
                    LOGGER.info("Sent SMS to successful target subjectId and receiver list: " + successSubjectIds);
                } catch (Exception e) {
                    LOGGER.error("An error occurred while sending SMS.", e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while processing data summaries.", e);
        }
    }

    private String buildSMSMessage(int month) {
        return String.format("안녕하세요?\n" +
                "오늘부터 %d개월 추적검사에 참여하실 수 있습니다.\n" +
                "모바일 앱으로 진행되는 시선추적 검사와 가정으로\n" +
                "보내드리는 설문 검사에 꼭 참여하시어 아이의 행동변화를 점검해 보시길 권해드립니다\n" +
                "감사합니다", month);
    }

    private boolean shouldSendSMS(List<DataSummary> dataSummaryList) {

        return !dataSummaryList.isEmpty();
    }

    private boolean checkedPriviousTrialIndex(List<DataSummary> trialIndexList) {

        return !trialIndexList.isEmpty();
    }

    private Collection<AsdProjectSubject> findUpdateTrialIndexfromMongoDB(String subjectId, int trialIndex) {

        var subjects = this._projectSubjectRepository.findAllBySubjectId(systemId, subjectId);
        AsdProjectSubject subjectList = subjects.stream().findFirst().get();
        String registDate = subjectList.getRegistDate();
        LOGGER.info("registDate={}", registDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate targetDate = LocalDate.parse(registDate, formatter);
        int monthsAgo = getMonth(trialIndex);
        LocalDate plusDate = targetDate.plusMonths(monthsAgo);
        LOGGER.info("-> formmated date: {}", targetDate);
        LOGGER.info("-> plus date: {}", plusDate);

        int dayOfMonth = plusDate.getDayOfMonth();
        int lastValidDayOfMonth = targetDate.lengthOfMonth();
        // boolean isLastDayOfMonth = dayOfMonth == now.lengthOfMonth();
        LOGGER.info("-> dayOfmonth: {}", targetDate);
        LOGGER.info("-> lastValidDayOfMonth: {}", lastValidDayOfMonth);

        List<String> datesToSearch = new ArrayList<>();
        datesToSearch = getDateToSearch(plusDate, targetDate, dayOfMonth, lastValidDayOfMonth);

        // int month = getMonth(trialIndex);
        // LOGGER.info("-> [fromMongoDB]month: {}", month);
        // LocalDate date = LocalDate.now().minusMonths(month);
        // LOGGER.info("-> date: {}", date);
        Collection<AsdProjectSubject> list = null;
        for (String date : datesToSearch) {
            list = this._projectSubjectRepository.findByRegistDate(date.toString());
            LOGGER.info("datesToSearch : {}", date);
        }

        LOGGER.info("list = {}", list);

        return list;
    }

    private List<String> getDateToSearch(LocalDate now, LocalDate targetDate, int dayOfMonth, int lastValidDayOfMonth) {

        boolean isLastDayOfMonth = dayOfMonth == now.lengthOfMonth();

        List<String> datesToSearch = new ArrayList<>();

        if (isLastDayOfMonth) {
            // 만약 오늘이 현재 달의 마지막 날이라면, 타겟 달의 끝날까지 모든 날짜를 고려해야 합니다.
            for (int i = targetDate.getDayOfMonth(); i <= lastValidDayOfMonth; i++) {
                datesToSearch.add(targetDate.withDayOfMonth(i).toString());
            }
        } else {
            if (dayOfMonth > lastValidDayOfMonth) {
                // 만약 현재 일이 타겟 달의 마지막 유효 일보다 크다면, 타겟 날짜를 조정합니다.
                targetDate = targetDate.withDayOfMonth(lastValidDayOfMonth);
                datesToSearch.add(targetDate.toString());
            } else {
                // 그렇지 않다면, 타겟 날짜와 타겟 달의 끝날까지의 다음 날들을 고려합니다.
                for (int i = dayOfMonth; i <= lastValidDayOfMonth; i++) {
                    datesToSearch.add(targetDate.withDayOfMonth(i).toString());
                }
            }
        }

        return datesToSearch;
    }

    private Collection<AsdProjectSubject> getListForMonth(String subjectId, int monthsAgo) {
        // LocalDate now = LocalDate.now();

        // LocalDate targetDate = now.minusMonths(monthsAgo);
        var subjects = this._projectSubjectRepository.findAllBySubjectId(systemId, subjectId);
        AsdProjectSubject subjectList = subjects.stream().findFirst().get();
        String registDate = subjectList.getRegistDate();
        LOGGER.info("registDate={}", registDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate targetDate = LocalDate.parse(registDate, formatter);
        LocalDate plusDate = targetDate.plusMonths(monthsAgo);
        LOGGER.info("-> formmated date: {}", targetDate);
        LOGGER.info("-> plus date: {}", plusDate);

        int dayOfMonth = plusDate.getDayOfMonth();
        int lastValidDayOfMonth = targetDate.lengthOfMonth();
        // boolean isLastDayOfMonth = dayOfMonth == now.lengthOfMonth();
        LOGGER.info("-> dayOfmonth: {}", targetDate);
        LOGGER.info("-> lastValidDayOfMonth: {}", lastValidDayOfMonth);
        List<String> datesToSearch = new ArrayList<>();
        datesToSearch = getDateToSearch(plusDate, targetDate, dayOfMonth, lastValidDayOfMonth);
        // if (isLastDayOfMonth) {
        // // 만약 오늘이 현재 달의 마지막 날이라면, 타겟 달의 끝날까지 모든 날짜를 고려해야 합니다.
        // for (int i = targetDate.getDayOfMonth(); i <= lastValidDayOfMonth; i++) {
        // datesToSearch.add(targetDate.withDayOfMonth(i).toString());
        // }
        // } else {
        // if (dayOfMonth > lastValidDayOfMonth) {
        // // 만약 현재 일이 타겟 달의 마지막 유효 일보다 크다면, 타겟 날짜를 조정합니다.
        // targetDate = targetDate.withDayOfMonth(lastValidDayOfMonth);
        // datesToSearch.add(targetDate.toString());
        // } else {
        // // 그렇지 않다면, 타겟 날짜와 타겟 달의 끝날까지의 다음 날들을 고려합니다.
        // for (int i = dayOfMonth; i <= lastValidDayOfMonth; i++) {
        // datesToSearch.add(targetDate.withDayOfMonth(i).toString());
        // }
        // }
        // }

        LOGGER.info("-> adjusted target dates to search: {}", datesToSearch);

        // 범위 내의 모든 날짜에 대해 결과를 찾습니다.
        Collection<AsdProjectSubject> list = new ArrayList<>();
        for (String date : datesToSearch) {
            list.addAll(this._projectSubjectRepository.findByRegistDate(date));
        }

        // 각 주제를 시험 인덱스와 현재 날짜 시간으로 업데이트합니다.
        list.forEach(subject -> {
            Integer trialIndex = getTrialIndex(monthsAgo);
            if (trialIndex != null) {
                subject.setTrialIndex(trialIndex);
                subject.setDateTrialIndex(calculateCDate(6, targetDate).atTime(11, 0));
            }
        });

        return list;
    }

    @ApiOperation(value = "TrialIndex update Forced대상 처리 일괄 옴니 Post API", notes = "TrialIndex update Forced대상 처리 일괄 옴니 Post API")
    @PostMapping(path = "/api/v1/subject/to-omni/result")
    public ResponseEntity<JsonResponseObject> forcedSendTrialIndexSubject() {

        String taskNames = "SubjectUpdateSMSScheduler";
        final String systemIds = super.getSystemId();
        List<String> subjectIds = this._dataCommonRepository.findSubjectUpdateBatchList(taskNames);

        if (!subjectIds.isEmpty()) {

            Collection<AsdProjectSubject> projects = this._projectSubjectRepository.findAllBySubjectIds(SYSTEM_ID,
                    subjectIds);

            if (!projects.isEmpty()) {
                try {
                    _callRestApi.requestPost(this.omniHost + OmniApi.PATSTATUSINFO, makeOmniJsonObject(projects));

                    // Return OK response if the request is successful
                    return ResponseEntity
                            .ok(new JsonResponseObject(true, "Omni Post request successfully sent.", projects));
                } catch (Exception e) {
                    // Log the error
                    LOGGER.error("Error occurred while sending Omni Post request: {}", e.getMessage());
                    // Return an error response if an exception occurs
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new JsonResponseObject(false, "Failed to send Omni Post request.",
                                    projects));
                }
            }
        }

        // Return an OK response even if no projects were found to process
        return ResponseEntity.ok(new JsonResponseObject(true, "Omni Post request successfully sent."));
    }

    // API 엔드포인트: 강제 대상 처리 일괄 업데이트
    @ApiOperation(value = "강제 대상 처리 일괄 업데이트", notes = "강제 대상 처리 일괄 업데이트 API")
    @PostMapping(path = "/api/v1/subject/forced/result")
    public ResponseEntity<JsonResponseObject> forcedSubjectTrialUpdateBatch(@RequestParam Map<String, Object> params) {
        // 현재 날짜 가져오기
        LocalDate currentDate = LocalDate.now();
        // 날짜 형식 지정
        DateTimeFormatter formats = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 날짜를 형식에 맞게 변환
        String formatDate = currentDate.format(formats);

        // MongoDB 집계 파이프라인을 설정하기 위한 문서 생성
        Document matchDoc = new Document("$match", new Document()
                .append("systemId", SYSTEM_ID)
                .append("projectSeq", new Document("$ne", 1L))
                .append("state", new Document("$ne", 4))
                .append("$and", Arrays.asList(
                        new Document("$expr", new Document("$gt", Arrays.asList(
                                new Document("$subtract", Arrays.asList(
                                        new Document("$toDate", formatDate),
                                        new Document("$toDate", "$registDate"))),
                                15552000000L)))
                        // ,new Document("trialIndex", 1)
                        , new Document("trialIndex", new Document("$in", Arrays.asList(1, 2, 3, 4))) // Include
                                                                                                     // trialIndex
                // condition here
                )));

        // 요청 파라미터에 subjectId가 있는지 확인하고, 있다면 질의에 추가
        if (params.containsKey("subjectId")) {
            matchDoc.get("$match", Document.class).append("subjectId", params.get("subjectId").toString());
        }

        // MongoDB 집계 파이프라인에 필드를 추가하기 위한 문서 생성
        Document addField = new Document("$addFields",
                new Document("monthBetween", new Document("$toInt", new Document("$divide", Arrays.asList(
                        new Document("$subtract", Arrays.asList(
                                new Document("$toDate", formatDate),
                                new Document("$toDate", "$registDate"))),
                        2592000000L)))));

        // 파이프라인 생성
        List<Document> pipeline = Arrays.asList(matchDoc, addField);

        // MongoDB에서 집계 실행
        var aggregationResult = this._mongoTemplate
                .getMongoDatabaseFactory()
                .getMongoDatabase()
                .getCollection("projectSubject")
                .aggregate(pipeline);

        // 집계 결과를 목록으로 변환
        var list = aggregationResult.into(new ArrayList<>());

        // 수정된 대상 목록을 저장하기 위한 리스트 초기화
        List<AsdProjectSubject> modifiedSubjects = new ArrayList<>();

        // Iterable 리스트를 자바 컬렉션으로 변환
        List<Document> documentList = new ArrayList<>();
        list.forEach(documentList::add);

        // 자바 컬렉션을 반복
        for (Document document : documentList) {
            // 문서에서 필요한 정보 추출
            int monthBetween = document.getInteger("monthBetween");
            String subjectId = document.getString("subjectId");
            int trialIndex = document.getInteger("trialIndex");
            LOGGER.info("monthBetween={}", monthBetween);
            LOGGER.info("subjectId={}", subjectId);

            // monthBetween에 따라 대상 처리 로직 실행
            processSubject(monthBetween, subjectId, trialIndex, modifiedSubjects);
        }

        // 수정된 대상 목록을 MongoDB에 일괄 저장
        // _mongosaveAll(modifiedSubjects);
        Collection<AsdProjectSubject> savedSubjects = _projectSubjectRepository.saveAll(modifiedSubjects);

        if (!savedSubjects.isEmpty()) {
            try {
                _callRestApi.requestPost(this.omniHost + OmniApi.PATSTATUSINFO, makeOmniJsonObject(savedSubjects));

                // Return OK response if the request is successful
                return ResponseEntity
                        .ok(new JsonResponseObject(true, "Omni Post request successfully sent.", savedSubjects));
            } catch (Exception e) {
                // Log the error
                LOGGER.error("Error occurred while sending Omni Post request: {}", e.getMessage());
                // Return an error response if an exception occurs
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new JsonResponseObject(false, "Failed to send Omni Post request.",
                                savedSubjects));
            }
        }

        List<String> modifiedSubjectIds = new ArrayList<>();
        savedSubjects.forEach(subject -> modifiedSubjectIds.add(subject.getSubjectId()));
        LOGGER.info("수정된 대상의 subjectId 목록: {}", modifiedSubjectIds);
        // try {
        // _callRestApi.requestPost(this.omniHost + OmniApi.PATSTATUSINFO,
        // makeOmniJsonObject(savedSubjects));
        // } catch (Exception e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // 사용자에게 응답 보내기

        return OkResponseEntity("사용자 목록에 응답합니다.", modifiedSubjectIds.toString());
    }

    private ResponseEntity<JsonResponseObject> OkResponseEntitys(List<AsdProjectSubject> list) {
        Result result = new Result(); // Create a result object

        // Perform logic based on the list parameter to determine success and message
        if (list == null || list.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("List is empty.");
        } else {
            // Your custom logic here based on the contents of the list
            // For example:
            // if (/* some condition based on list */) {
            // result.setSuccess(true);
            // result.setMessage("List has elements and meets condition X.");
            // } else {
            result.setSuccess(true);
            result.setMessage("재촬영 요청을 정상적으로 처리했습니다.");

            // }
        }

        // Construct and return the ResponseEntity with the JsonResponseObject
        JsonResponseObject responseObject = new JsonResponseObject(result.isSuccess(), result.getMessage(), list);
        return ResponseEntity.ok(responseObject);
    }

    // 대상 처리를 위한 메서드
    private void processSubject(int monthBetween, String subjectId, int trialIndex,
            List<AsdProjectSubject> modifiedSubjects) {
        // 대상 조회
        Collection<AsdProjectSubject> resultList = _projectSubjectRepository.findAllBySubjectId(SYSTEM_ID, subjectId);

        // 대상 처리 로깅
        LOGGER.info("대상자 정보를 조회했습니다. subjectId={}", subjectId);

        // 각 대상 처리
        for (AsdProjectSubject projectSubject : resultList) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate registDate = LocalDate.parse(projectSubject.getRegistDate(), formatter);
            // .atTime(2, 0);
            LOGGER.info("registDate={}", registDate);
            LocalDate newRegistDate = calculateCDate(monthBetween, registDate);

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
                    projectSubject.setDateTrialIndex(newRegistDate.atTime(11, 0));
                    projectSubject.setTrialIndex(trialIndexs); // Set trialIndex if MongoDB update is needed
                    // 수정된 프로젝트 대상을 리스트에 추가
                    modifiedSubjects.add(projectSubject);
                }
            }

        }
    }

    private void updateDataSummaries(String subjectId, LocalDate newRegistDate, LocalDate registDate,
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
                        newDataSummary.setCDate(calculateCDate(getMonth(i), registDate).atTime(11, 0));
                        newDataSummary.setUDate(LocalDateTime.now());
                        newDataSummary.setDateTrialIndex(calculateCDate(getMonth(i), registDate).atTime(11, 0));

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
    private LocalDate calculateCDate(int monthBetween, LocalDate registDate) {
        // LocalDateTime localDate =
        // LocalDateTime.parse(DateUtil.formatSimpleDate(registDate)).atTime;

        boolean islastMonth = (registDate.getDayOfMonth() == registDate.lengthOfMonth());

        if (monthBetween >= 6 && monthBetween < 12) {
            if (islastMonth) {

                // LocalDateTime plusmonth = localDate.plusMonths(6);

            }
            // registDate.getDayOfMonth() == registDate.get

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

    // private Collection<AsdProjectSubject> getListForMonth(int month) {
    // LocalDate date = LocalDate.now().minusMonths(month); // 월 단위 날짜 빼는 function
    // LOGGER.info("-> date: {}", date);

    // Collection<AsdProjectSubject> list =
    // _projectSubjectRepository.findByRegistDate(date.toString());

    // list.forEach(s -> {

    // s.getPhoneNumber();
    // });

    // list.forEach(subject -> {
    // Integer trialIndex = getTrialIndex(month);
    // if (trialIndex != null) {
    // subject.setTrialIndex(trialIndex);
    // subject.setDateTrialIndex(LocalDateTime.now());

    // }
    // });

    // return list;
    // }

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
                LocalDate registDate = LocalDate.parse(subject.getRegistDate(), formatter);

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
                    LocalDate newDate = calculateCDate(month, registDate);
                    LOGGER.info("checkIsExists[EXCUTE-METHOD] : subject={} , trialIndex[num] ={} ,CDate ={}", subjectId,
                            num, newDate);

                    // Set properties for subsequent trial indexes
                    LOGGER.info("list subject ={} ", subject);
                    data.projectSubjectForm(subject);
                    data.setTrialIndex(num);
                    data.setCDate(newDate.atTime(11, 0));
                    data.setDateTrialIndex(newDate.atTime(11, 0));
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
