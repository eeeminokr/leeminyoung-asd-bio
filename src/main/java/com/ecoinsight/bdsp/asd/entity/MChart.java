package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class MChart {
    private long id;
    private String systemId;
    protected long projectSeq;
    private String projectName;
    protected int trialIndex;
    private String orgId;
    private String gender;
    protected String orgName;
    protected String birthDay;
    protected String subjectId; 
    protected long patientSeq;
    private String rschKey;
    private String result;
    private String registDt;
    private LocalDateTime dateCreated;
    private LocalDate mChatCreatedDate;
    private String userCreated;
    private long months;    
    
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getSystemId() {
        return systemId;
    }
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
    public long getProjectSeq() {
        return projectSeq;
    }
    public void setProjectSeq(long projectSeq) {
        this.projectSeq = projectSeq;
    }
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public int getTrialIndex() {
        return trialIndex;
    }
    public void setTrialIndex(int trialIndex) {
        this.trialIndex = trialIndex;
    }
    public String getOrgId() {
        return orgId;
    }
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
    public String getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
    public long getPatientSeq() {
        return patientSeq;
    }
    public void setPatientSeq(long patientSeq) {
        this.patientSeq = patientSeq;
    }
    public String getRschKey() {
        return rschKey;
    }
    public void setRschKey(String rschKey) {
        this.rschKey = rschKey;
    }
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
    public String getRegistDt() {
        return registDt;
    }
    public void setRegistDt(String registDt) {
        this.registDt = registDt;
    }
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
    public String getUserCreated() {
        return userCreated;
    }
    public void setUserCreated(String userCreated) {
        this.userCreated = userCreated;
    }
    
    public String getOrgName() {
        return this.orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
    public String getBirthDay() {
        return this.birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }


    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", systemId='" + getSystemId() + "'" +
            ", projectSeq='" + getProjectSeq() + "'" +
            ", trialIndex='" + getTrialIndex() + "'" +
            ", orgId='" + getOrgId() + "'" +
            ", orgName='" + getOrgName() + "'" +
            ", birthDay='" + getBirthDay() + "'" +
            ", subjectId='" + getSubjectId() + "'" +
            ", patientSeq='" + getPatientSeq() + "'" +
            ", rschKey='" + getRschKey() + "'" +
            ", result='" + getResult() + "'" +
            ", registDt='" + getRegistDt() + "'" +
            ", dateCreated='" + getDateCreated() + "'" +
            ", userCreated='" + getUserCreated() + "'" +
            "}";
    }

    public long getMonths() {
        return this.months;
    }

    public void setMonths(long months) {
        this.months = months;
    }
    
    public LocalDate getMChatCreatedDate() {
        return this.mChatCreatedDate;
    }

    public void setMChatCreatedDate(LocalDate mChatCreatedDate) {
        this.mChatCreatedDate = mChatCreatedDate;
    }
    
    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


}