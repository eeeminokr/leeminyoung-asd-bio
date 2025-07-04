package com.ecoinsight.bdsp.asd.scheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecoinsight.bdsp.asd.OmniApi;
import com.ecoinsight.bdsp.asd.entity.DataSummary;
import com.ecoinsight.bdsp.asd.entity.SmsScheduler;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.OmniSubject;
import com.ecoinsight.bdsp.asd.repository.IAsdDataCommonRepoistory;
import com.ecoinsight.bdsp.asd.repository.IAsdProjectSubjectRepository;
import com.ecoinsight.bdsp.asd.repository.ISmsSchudlerHistoryRepository;
import com.ecoinsight.bdsp.asd.scheduling.utils.CallRestApi;
import com.ecoinsight.bdsp.asd.scheduling.utils.TextMessageSender;
import com.ecoinsight.bdsp.asd.service.DataCommonService;
import com.ecoinsight.bdsp.asd.service.ForcedUdateBatchService;
import com.ecoinsight.bdsp.core.repository.ISchedulerLogRepository;
import com.ecoinsight.bdsp.core.scheduling.TaskResult;
import com.google.gson.Gson;

@Component
public class SubjectUpdateSMSScheduler extends AsdBaseScheduler {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private static final String SYSTEM_ID = "ASD";
    protected String taskName;
    protected String systemId;
    private String _hostname;

    // @Value("${ecoinsight.subject-trialIndex.upload-batch-enabled}")
    // private Boolean enabled;
    @Value("${omni.host}")
    private String omniHost;

    @Autowired
    private IAsdProjectSubjectRepository _projectSubjectRepository;

    @Autowired
    protected ISchedulerLogRepository _schedulerLogRepository;

    @Autowired
    private IAsdDataCommonRepoistory _asdDataCommonRepository;
    @Autowired
    private ISmsSchudlerHistoryRepository _smsSchedulerHistory;
    @Autowired
    private DataCommonService _dataCommonService;

    @Autowired
    private ForcedUdateBatchService _forcedUdateBatchService;
    @Autowired
    private CallRestApi _callRestApi;
    @Autowired
    private TextMessageSender sender;

    @Override
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

    @Scheduled(cron = "${scheduling.subject.today.cron}")
    @Override
    public void doPerform() {

        final TaskResult result = new TaskResult(taskName, LocalDateTime.now());
        result.setTaskRunner(getClass().getName());
        final List<Exception> exceptions = new ArrayList<Exception>();
        try {
            LOGGER.info("-> SubjectUpdate & SMSScheduler.doPerform....");
            getFindUpdatedTrialIndexNormal();
            Collection<AsdProjectSubject> combinedList = combinedList();

            processDataSummaries(combinedList);
        } catch (Exception e) {
            LOGGER.error("An error occurred in SubjectUpdateBatch.doPerform", e);
            result.setMessage(String.format("Error while updating TrialIndex for Subjects. [TASK={}]", taskName, e));
        } finally {
            result.setExceptions(exceptions);
            result.setSucceeded(exceptions.size() <= 0);

            // 태스크 처리 결과 저장
            try {
                super.reportResult(result);

                LOGGER.info("Inserted T_SchedulerLog. [TASK={}]", taskName);
            } catch (Exception ex) {
                LOGGER.error("Error while reporting result. [TASK={}]", taskName, ex);
            }

        }

    }

    private void getFindUpdatedTrialIndexNormal() {

        List<DataSummary> list = this._asdDataCommonRepository.findBySubject(null, 1, 2);

        boolean state = false;
        int deleted = 0;
        if (!list.isEmpty()) {
            LOGGER.info("Attempting to found updated TrialIndex into ProjectSeq mysql DataSurmmary={}", list);
            for (DataSummary summary : list) {

                Map<String, Boolean> map = summary.trialInfoState(summary);

                for (DataSummaryColumn column : DataSummaryColumn.values()) {

                    if (map.containsKey(column.name())) {
                        if (!map.get(column.name())) {
                            state = true;
                        } else {
                            state = false;
                            break;
                        }
                    }
                }
                if (state) {
                    var subjects = this._projectSubjectRepository.findBySubjectId(SYSTEM_ID, summary.getProjectSeq(),
                            summary.getSubjectId());

                    // AsdProjectSubject subject = subjects.stream().findFirst().orElse(null);
                    for (AsdProjectSubject subject : subjects) {
                        if (subject.getTrialIndex() == summary.getTrialIndex()) {
                            subject.setTrialIndex(1);
                            this._projectSubjectRepository.saveAll(subjects);

                        }

                        subject.setTrialIndex(2);
                        deleted = this._asdDataCommonRepository.deleteDataSummaryFindBySubjectId(subject);
                    }
                }
                if (deleted > 0) {
                    LOGGER.info("success updated trialIndex into ProjectSEq", list);
                }
            }
        }

    }

    private Collection<AsdProjectSubject> combinedList() {
        Collection<AsdProjectSubject> combinedList = new ArrayList<>();
        combinedList.addAll(getListForMonth(6));
        combinedList.addAll(getListForMonth(12));
        combinedList.addAll(getListForMonth(18));

        return combinedList;

    }

    public List<DataSummary> getDataSummaryFindByTrialIndex(int trialIndex) throws Exception {
        List<DataSummary> dataSummaryList;

        switch (trialIndex) {
            case 2:
                dataSummaryList = this._asdDataCommonRepository.findListForUpdateTrialIndex2(trialIndex);
                break;
            case 3:
                dataSummaryList = this._asdDataCommonRepository.findListForUpdateTrialIndex3(trialIndex);
                break;
            case 4:
                dataSummaryList = this._asdDataCommonRepository.findListForUpdateTrialIndex4(trialIndex);
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
                dataSummaryList = this._asdDataCommonRepository.findListForUpdateTrialIndexName2(subjectId, trialIndex);
                break;
            case 3:
                dataSummaryList = this._asdDataCommonRepository.findListForUpdateTrialIndexName3(subjectId, trialIndex);
                break;
            case 4:
                dataSummaryList = this._asdDataCommonRepository.findListForUpdateTrialIndexName4(subjectId, trialIndex);
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
                    addSubjectForTrialIndex(sucess);
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

    public void addSubjectForTrialIndex(boolean flag) throws Exception {
        // updateTrialIndexForDataSummary(2, subjectId, "2차");
        // updateTrialIndexForDataSummary(3, subjectId, "3차");
        // updateTrialIndexForDataSummary(4, subjectId, "4차");
        LOGGER.info("Attempting to find update TrialIndex to mysql DataSurmmary={}", flag);
        updateTrialIndexForDataSummary(2, "2차");
        updateTrialIndexForDataSummary(3, "3차");
        updateTrialIndexForDataSummary(4, "4차");
    }

    private void updateTrialIndexForDataSummary(int trialIndex, String trialDescription)
            throws Exception {
        List<DataSummary> results = getDataSummaryFindByTrialIndex(trialIndex);

        if (results.isEmpty()) {

            Collection<AsdProjectSubject> lists = findUpdateTrialIndexfromMongoDB(trialIndex); // mongoDB - using
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
                    var data = _asdDataCommonRepository.findOne(project.getSubjectId(), project.getProjectSeq(),
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
                            boolean result = _asdDataCommonRepository.updateDataSummaryForSyncFromMongoDB(subjects);

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
            LOGGER.info("-> insert subject => subjectId=" + result.getSubjectId()
                    + ", projectSeq=" + result.getProjectSeq()
                    + ", trialIndex=" + result.getTrialIndex());

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
        return String.format("안녕하세요?\n"
                + "오늘부터 %d개월 추적검사에 참여하실 수 있습니다.\n"
                + "모바일 앱으로 진행되는 시선추적 검사와 가정으로\n"
                + "보내드리는 설문 검사에 꼭 참여하시어 아이의 행동변화를 점검해 보시길 권해드립니다\n"
                + "감사합니다", month);
    }

    private boolean shouldSendSMS(List<DataSummary> dataSummaryList) {

        return !dataSummaryList.isEmpty();
    }

    private boolean checkedPriviousTrialIndex(List<DataSummary> trialIndexList) {

        return !trialIndexList.isEmpty();
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

    private Collection<AsdProjectSubject> findUpdateTrialIndexfromMongoDB(int trialIndex) {

        int month = getMonth(trialIndex);
        LOGGER.info("-> [fromMongoDB]month: {}", month);
        LocalDate date = LocalDate.now().minusMonths(month);
        LOGGER.info("-> date: {}", date);
        Collection<AsdProjectSubject> list = this._projectSubjectRepository.findByRegistDate(date.toString());

        return list;
    }

    private Collection<AsdProjectSubject> getListForMonth(int month) {
        LocalDate date = LocalDate.now().minusMonths(month);
        LOGGER.info("-> date: {}", date);

        Collection<AsdProjectSubject> list = this._projectSubjectRepository.findByRegistDate(date.toString());

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
            case 7:
                return 2;
            case 12:
                return 3;
            case 13:
                return 3;
            case 18:
                return 4;
            case 19:
                return 4;
            default:
                return null;
        }
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
            omniSubject.setPhone(ps.getPhoneNumber()); // 등록
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
