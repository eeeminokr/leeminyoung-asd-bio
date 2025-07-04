package com.ecoinsight.bdsp.asd.scheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecoinsight.bdsp.asd.entity.SmsScheduler;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.repository.ISmsSchudlerHistoryRepository;
import com.ecoinsight.bdsp.asd.scheduling.utils.TextMessageSender;
import com.ecoinsight.bdsp.asd.service.DataCommonService;
import com.ecoinsight.bdsp.core.repository.ISchedulerLogRepository;
import com.ecoinsight.bdsp.core.scheduling.TaskResult;
import com.ecoinsight.bdsp.core.service.ServiceException;

@Component
public class AdvanceSMSTaskScheduler extends AsdBaseScheduler {
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
    private ISmsSchudlerHistoryRepository _smsSchedulerHistory;

    @Autowired
    private DataCommonService _dataCommonService;

    @Autowired
    private TextMessageSender sender;

    @Override
    public String getTaskName() {
        return this.taskName;
    }

    @PostConstruct
    public void startup() throws UnknownHostException {
        this.taskName = "AdvanceSMSTaskScheduler";
        this._hostname = InetAddress.getLocalHost().getHostName();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> startup hostname=" + this._hostname);
        }
    }

    @Scheduled(cron = "${scheduling.subject.period.cron}")
    @Override
    public void doPerform() {

        final TaskResult result = new TaskResult(taskName, LocalDateTime.now());
        result.setTaskRunner(getClass().getName());

        final List<Exception> exceptions = new ArrayList<Exception>();

        try {
            LOGGER.info("-> AdvanceSMSTaskScheduler.doPerform....");
            int[] noticeDurations = { 6, 12, 18 };

            for (int months : noticeDurations) {
                getListNoticeSubject(months);
            }
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

    // 케이스 SMS 6,12,18 인자값을 나눠서 SMSTXT를 보내 PUT의 CONTENT를 나눈다.

    // Print the result

    // System.out.println("Current Date: " + currentDate);
    // System.out.println("One Week Ago: " + oneWeekAgo);

    /**
     * @param month
     */
    private void getListNoticeSubject(int month) {
        // 등록일 기준 -6개월 + 7일
        LocalDate date = LocalDate.now().minusMonths(month).plusWeeks(1);

        // 개월 수에 따른 SMS 전달다르게[객체생성해서MSG 전달하는 방식. ]

        // Collection<AsdProjectSubject> list =
        // _projectSubjectRepository.findByRegistDate(date.toString());
        // Assuming you have a list of AsdProjectSubject objects
        // Assuming you have a collection of AsdProjectSubject objects
        Collection<AsdProjectSubject> collection = _projectSubjectRepository.findByRegistDate(date.toString());

        if (collection.size() <= 0) {
            // If no subjects are found for the given month, return from the method
            return;
        }
        // Convert the collection to a list
        List<AsdProjectSubject> list = new ArrayList<>(collection);

        // Extract phone numbers and add 6 months to the registDate in a single stream
        List<Map<String, String>> recipients = list.stream()
                .filter(subject -> {
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

                    LocalDate currentRegistDate = LocalDate.parse(subject.getRegistDate(),
                            DateTimeFormatter.ISO_LOCAL_DATE);
                    LocalDate newDate = currentRegistDate.plusMonths(month);
                    String updatedRegistDate = newDate
                            .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREAN));
                    // String updatedRegistDate = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    Map<String, String> recipientMap = new HashMap<>();
                    String phoneNumber = subject.getPhoneNumber();
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        recipientMap.put("receiver", subject.getPhoneNumber());
                        recipientMap.put("subjectId", subject.getSubjectId());
                        recipientMap.put("projectSeq", String.valueOf(subject.getProjectSeq()));
                        recipientMap.put("trialIndex", String.valueOf(subject.getTrialIndex()));
                        recipientMap.put("systemId", SYSTEM_ID);
                        recipientMap.put("msg",
                                "안녕하세요? \n돌아오는 " + updatedRegistDate + " 부터 " + month + "개월 추적검사에 참여하실 수 있습니다.\n"
                                        + "모바일 앱으로 진행되는 시선추적 검사와 가정으로 보내드리는 설문 검사에 꼭 참여하시어 아이의 행동변화를 점검해 보시길 권해드립니다.\n"
                                        +
                                        "감사합니다"); // Display updated registDate
                        recipientMap.put("result", "sucess");
                        recipientMap.put("taskName", getTaskName());

                    }

                    // + "Original RegistDate: " + originalRegistDate + "\n" // Display original
                    // registDate

                    return recipientMap;
                })
                .filter(map -> map.get("receiver") != null && !map.get("receiver").toString().isEmpty()) // 전화번호가 없는 항목을
                .collect(Collectors.toList());

        try {
            this.sender.sendExecuteTaskPeriod(recipients);

            LOGGER.info("AdvanceSMSTaskScheuler -> sended SMS text to the subjects. subjects={}",
                    recipients.toArray().toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}