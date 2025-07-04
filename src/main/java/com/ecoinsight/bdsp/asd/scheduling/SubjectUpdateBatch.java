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
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.OmniSubject;
import com.ecoinsight.bdsp.asd.scheduling.utils.CallRestApi;
import com.ecoinsight.bdsp.asd.scheduling.utils.TextMessageSender;
import com.ecoinsight.bdsp.asd.service.DataCommonService;
import com.ecoinsight.bdsp.core.repository.ISchedulerLogRepository;
import com.ecoinsight.bdsp.core.scheduling.TaskResult;
import com.google.gson.Gson;

@Component
public class SubjectUpdateBatch extends AsdBaseScheduler {
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
    private com.ecoinsight.bdsp.asd.repository.IAsdProjectSubjectRepository _projectSubjectRepository;

    @Autowired
    protected ISchedulerLogRepository _schedulerLogRepository;

    @Autowired
    private DataCommonService _dataCommonService;

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
        this.taskName = "SubjectUpdateBatch";
        this._hostname = InetAddress.getLocalHost().getHostName();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> startup hostname=" + this._hostname);
        }
    }

    // @Scheduled(cron = "${scheduling.subject.cron}") // SubjectUpdateSMSScheduler
    // subjectUpdate 및 sms 당일공지 api 생성으로 중단
    // @Scheduled(cron = "${scheduling.subject.cron-test}")
    @Override
    public void doPerform() {
        final TaskResult result = new TaskResult(taskName, LocalDateTime.now());
        result.setTaskRunner(getClass().getName());

        final List<Exception> exceptions = new ArrayList<Exception>();

        try {
            LOGGER.info("-> SubjectUpdateBatch.doPerform...." + this._hostname);

            // 해당 대상자의 서면동의일의 6개월, 12개월, 18개월이 되는 날짜에 TrialIndex를 업로드 한다.
            Collection<AsdProjectSubject> combinedList = new ArrayList<>();
            combinedList.addAll(getListForMonth(6));
            combinedList.addAll(getListForMonth(12));
            combinedList.addAll(getListForMonth(18));

            Collection<AsdProjectSubject> asdProjectSubjects = _projectSubjectRepository.saveAll(combinedList);

            // mysql DateSummary 테이블에 새로운 차수 대상자 등록
            _dataCommonService.addSubjectForTrialIndex(true);

            _callRestApi.requestPost(this.omniHost + OmniApi.PATSTATUSINFO, makeOmniJsonObject(combinedList));

            result.setMessage(String.format("The TrialIndex of today's target has been changed to normal."));
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
