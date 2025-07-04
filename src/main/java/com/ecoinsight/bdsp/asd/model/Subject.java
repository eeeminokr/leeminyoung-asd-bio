package com.ecoinsight.bdsp.asd.model;

import java.time.LocalDateTime;

public class Subject {
    private String subjectId;
    private Integer state; // String
    private String name;
    private String gender;
    private String birthDay;
    private String phoneNumber;
    private String RegistDate;
    private String StartDate;
    private String EndDate;
    private String projectSeq;
    private String testYn;
    private int trialIndex; // 임시 stage 서버 옴니측에서 trialIndex 값의 업데이트를 위한 임시 변수 추가
    private LocalDateTime Cdate;
    private int monthsDifference;
    // 임시

    public int getMonthsDifference() {
        return monthsDifference;
    }

    public void setMonthsDifference(int monthsDifference) {
        this.monthsDifference = monthsDifference;
    }

    public LocalDateTime getCdate() {
        return Cdate;
    }

    public void setCdate(LocalDateTime cdate) {
        Cdate = cdate;
    }

    public int getTrialIndex() {
        return trialIndex;
    }

    public void setTrialIndex(int trialIndex) {
        this.trialIndex = trialIndex;
    }

    public Subject() {
    }

    public String getSubjectId() {
        return this.subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getState() {
        return this.state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDay() {
        return this.birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRegistDate() {
        return this.RegistDate;
    }

    public void setRegistDate(String RegistDate) {
        this.RegistDate = RegistDate;
    }

    public String getStartDate() {
        return this.StartDate;
    }

    public void setStartDate(String StartDate) {
        this.StartDate = StartDate;
    }

    public String getEndDate() {
        return this.EndDate;
    }

    public void setEndDate(String EndDate) {
        this.EndDate = EndDate;
    }

    public String getProjectSeq() {
        return this.projectSeq;
    }

    public void setProjectSeq(String projectSeq) {
        this.projectSeq = projectSeq;
    }

    public String getTestYn() {
        return testYn;
    }

    public void setTestYn(String testYn) {
        this.testYn = testYn;
    }

    @Override
    public String toString() {
        return "{" +
                " subjectId='" + getSubjectId() + "'" +
                ", state='" + getState() + "'" +
                ", name='" + getName() + "'" +
                ", gender='" + getGender() + "'" +
                ", birthDay='" + getBirthDay() + "'" +
                ", phoneNumber='" + getPhoneNumber() + "'" +
                ", testYn=" + getTestYn() + "'" +
                ", RegistDate='" + getRegistDate() + "'" +
                ", StartDate='" + getStartDate() + "'" +
                ", EndDate='" + getEndDate() + "'" +
                "}";
    }

}
