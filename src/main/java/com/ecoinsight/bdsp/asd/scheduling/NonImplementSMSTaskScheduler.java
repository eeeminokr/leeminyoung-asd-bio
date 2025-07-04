package com.ecoinsight.bdsp.asd.scheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecoinsight.bdsp.asd.entity.DataSummary;
import com.ecoinsight.bdsp.asd.entity.SmsScheduler;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.repository.IAsdDataCommonRepoistory;
import com.ecoinsight.bdsp.asd.repository.ISmsSchudlerHistoryRepository;
import com.ecoinsight.bdsp.asd.scheduling.utils.TextMessageSender;
import com.ecoinsight.bdsp.asd.service.DataCommonService;
import com.ecoinsight.bdsp.core.repository.ISchedulerLogRepository;
import com.ecoinsight.bdsp.core.scheduling.TaskResult;

@Component
public class NonImplementSMSTaskScheduler extends AsdBaseScheduler {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private static final String SYSTEM_ID = "ASD";
    protected String taskName;
    protected String systemId;
    private String _hostname;

    @Autowired
    private com.ecoinsight.bdsp.asd.repository.IAsdProjectSubjectRepository _projectSubjectRepository;

    @Autowired
    protected ISchedulerLogRepository _schedulerLogRepository;

    @Autowired
    private IAsdDataCommonRepoistory _asdDataCommonRepository;

    @Autowired
    private DataCommonService _dataCommonService;

    @Autowired
    private ISmsSchudlerHistoryRepository _smsSchedulerHistory;

    @Autowired
    private TextMessageSender sender;

    @Override
    public String getTaskName() {
        return this.taskName;
    }

    @PostConstruct
    public void startup() throws UnknownHostException {
        this.taskName = "NonImplementSMSTaskScheduler";
        this._hostname = InetAddress.getLocalHost().getHostName();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> startup hostname=" + this._hostname);
        }
    }

    @Scheduled(cron = "${scheduling.subject.non-implement.cron}")
    @Override
    public void doPerform() {
        final TaskResult result = new TaskResult(taskName, LocalDateTime.now());
        result.setTaskRunner(getClass().getName());

        final List<Exception> exceptions = new ArrayList<>();

        LOGGER.info("-> NonImplementSMSTaskScheduler.doPerform...." + this._hostname);

        List<DataSummary> subjectList = this._asdDataCommonRepository.findNonImplemenDataSummaries();

        List<String> subjectIds = subjectList.stream()
                .map(DataSummary::getSubjectId)
                .collect(Collectors.toList());
        // .limit(5)
        Collection<AsdProjectSubject> subjectsInMongoDB = _projectSubjectRepository.findAllBySubjectIds(SYSTEM_ID,
                subjectIds);

        List<Map<String, String>> recipients = subjectsInMongoDB.stream()
                .filter(subject -> {
                    // int subjectId = subject.getSubjectId();
                    long projectSeq = subject.getProjectSeq();
                    String subjectId = subject.getSubjectId();
                    int trialIndex = subject.getTrialIndex();

                    List<SmsScheduler> smsHistory = _smsSchedulerHistory.findBySenedSubjectSms(projectSeq,
                            subjectId, trialIndex, taskName);
                    boolean hasSmsHistory = !smsHistory.isEmpty();
                    // TRUE이면 기록이(리스트)가 있다는 것.
                    LOGGER.info("hasSMS={}", hasSmsHistory);
                    LOGGER.info("FITER[SUBJECTID]={}", subject.getSubjectId());
                    if (!hasSmsHistory) {
                        LOGGER.info("Already to send SMS  [TASK={}] , subjectId={} , trialIndex={}",
                                taskName, subjectId, trialIndex);
                    }
                    return !hasSmsHistory;
                })
                .map(subject -> {
                    Map<String, String> recipientMap = new HashMap<>();
                    String phoneNumber = subject.getPhoneNumber();
                    LOGGER.info("MAP[SUBJECTID]={}", subject.getSubjectId());
                    int month = getMonth(subject.getTrialIndex());
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        recipientMap.put("receiver", phoneNumber);
                        recipientMap.put("subjectId", subject.getSubjectId());
                        recipientMap.put("projectSeq", String.valueOf(subject.getProjectSeq()));
                        recipientMap.put("trialIndex", String.valueOf(subject.getTrialIndex()));
                        recipientMap.put("systemId", SYSTEM_ID);
                        recipientMap.put("SCHEDULER_TASK", SmsScheduler.NON_IMPLEMENT_SMS_TASK);
                        recipientMap.put("msg", "안녕하세요?\n"
                                + "현재 " + month + "개월 추적검사에 참여하실 수 있습니다.\n"
                                + "많이 바쁘시겠지만, 잠시만 시간을 내주시어 추적검사에 참여해 주시길 부탁드립니다.\n"
                                + "혹시 과제 수행에 어려움이 있으시거나 문의하상이 있으시면 담당 연구원에게 연락주세요.\n"
                                + "감사합니다");
                        recipientMap.put("result", "sucess");
                        recipientMap.put("taskName", getTaskName());

                    }

                    return recipientMap;
                })
                // .filter(map -> !map.isEmpty()) // 전화번호가 없는 항목을 필터링하여 최종 결과에 유효한 전화번호만 포함
                .filter(map -> map.get("receiver") != null && !map.get("receiver").toString().isEmpty()) // 전화번호가 없는 항목을
                .collect(Collectors.toList());

        try {
            this.sender.sendExecuteTaskPeriod(recipients);
            List<String> successSubjectIds = recipients.stream()
                    .map(map -> map.get("subjectId").toString() + ": " + map.get("receiver").toString())
                    .collect(Collectors.toList());
            LOGGER.info("sended SMS s success target subjectId and receiver list: " + successSubjectIds);
        } catch (Exception e) {
            LOGGER.error("SMS 전송 중 에러 발생.", e);
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

    private Integer getMonth(int trialIndex) {
        switch (trialIndex) {
            case 2:
                return 6;
            case 3:
                return 12;
            case 4:
                return 18;
            default:
                return null;
        }
    }

    private boolean getDuplicatefindBysubjectId() {

        return true;
    }

}
