package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDateTime;

public class SmsScheduler {

    // private int smsScheduler;
    private String subjectId;
    private long projectSeq;

    private int trialIndex;

    private String systemId;
    private String taskName;
    private String phoneNumber;

    private String result;
    private LocalDateTime dateTimeExecuted;

    public static final String ADVANCE_SMS_TASK = "ADVANCE_SMS_TASK_SCHEDULER";
    public static final String NON_IMPLEMENT_SMS_TASK = "NON_IMPLEMENT_SMS_K_SCHEDULER";
    public static final String TODAY_SUBJECT_UPDATE_SMS_TASK = "TODAY_SUBJECT_UPDATE_SMS_TASK_SCHEDULER";

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public long getProjectSeq() {
        return projectSeq;
    }

    public void setProjectSeq(long projectSeq) {
        this.projectSeq = projectSeq;
    }

    public int getTrialIndex() {
        return trialIndex;
    }

    public void setTrialIndex(int trialIndex) {
        this.trialIndex = trialIndex;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getDateTimeExecuted() {
        return dateTimeExecuted;
    }

    public void setDateTimeExecuted(LocalDateTime dateTimeExecuted) {
        this.dateTimeExecuted = dateTimeExecuted;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public static String getNonImplementSmsTask() {
        return NON_IMPLEMENT_SMS_TASK;
    }

    public static String getTodaySubjectUpdateSmsTask() {
        return TODAY_SUBJECT_UPDATE_SMS_TASK;
    }

}
