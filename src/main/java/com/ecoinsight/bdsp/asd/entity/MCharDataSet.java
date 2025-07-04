package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "mchartId",
        "orgId",
        "dystemId",
        "subjectId",
        "projectSeq",
        "trialIndex",
        "note",
        "gender",
        "patientSeq",
        "rschKey",
        "result",
        "registDt",
        "cUser",
        "cDate"
})
public class MCharDataSet {

    // {
    // "MchartId" : 11,
    // "OrgId" : "10",
    // "SystemId" : "ASD",
    // "SubjectId" : "1023032001",
    // "대상군" : 1,
    // "TrialIndex" : 1,
    // "PatientSeq" : 100,
    // "RschKey" : "f5931363-71ca-4e68-90d3-6f01477a1d57",
    // "Result" : "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0",
    // "RegistDt" : "2023-03-20 20:05:00",
    // "CUser" : "OMNI@ASD",
    // "CDate" : "2023-03-20 20:05:00"
    // },

    private long mchartId;
    private String orgId;
    private String systemId;
    private String subjectId;
    private long projectSeq;
    private int trialIndex;
    private String note;
    private String gender;
    private long patientSeq;
    private String rschKey;
    private String result;
    private String registDt;
    private String cUser;
    private String cDate;

    public long getMchartId() {
        return mchartId;
    }

    public void setMchartId(long mchartId) {
        this.mchartId = mchartId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public String getcUser() {
        return cUser;
    }

    public void setcUser(String cUser) {
        this.cUser = cUser;
    }

    public String getcDate() {
        return cDate;
    }

    public void setcDate(String cDate) {
        this.cDate = cDate;
    }

}