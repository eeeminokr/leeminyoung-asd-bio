package com.ecoinsight.bdsp.asd.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ecoinsight.bdsp.asd.Constants;
import com.ecoinsight.bdsp.asd.model.imaging.QcStatusItem;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TrialQcStatusJsonObject {
    private long projectSeq;
    private String subjectId;
    private String gender;
    private String birthday;    
    private String orgId;
    private String orgName;
    private int age;
    

    private String imageInfoId;
    private String projectSubjectId;
    private int trialIndex;
    private Date dateUpdated;

    private HashMap<String, String> areaQcStatus = new HashMap<String, String>();

    private List<QcStatusItem> qcItems;
    private HashMap<String, Boolean> hasData;    

    public List<QcStatusItem> getQcItems() {
        return qcItems;
    }
    public void setQcItems(List<QcStatusItem> qcItems) {
        this.qcItems = qcItems;
    }
    public HashMap<String, Boolean> getHasData() {
        return hasData;
    }
    public void setHasData(HashMap<String, Boolean> hasData) {
        this.hasData = hasData;
    }

    public TrialQcStatusJsonObject(){}
    public TrialQcStatusJsonObject(TrialQcStatusModel model){
        if (model != null) {
            this.setProjectSubjectId(model.getId());
            this.setAge(model.getAge());
            this.setGender(model.getGender());
            this.setOrgId(model.getOrgId());
            this.setOrgName(model.getOrgName());
            this.setProjectSeq(model.getProjectSeq());
            this.setSubjectId(model.getSubjectId());
            this.setProjectSubjectId(model.getProjectSubjectId());
        }
    }

    public long getProjectSeq() {
        return projectSeq;
    }
    public void setProjectSeq(long projectSeq) {
        this.projectSeq = projectSeq;
    }
    public String getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    @JsonIgnore
    public String getGender() {
        return gender;
    }
    @JsonProperty(value="gender")
    public String getGenderFormatted() {
        return Constants.translate(this.getGender());
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getBirthday() {
        return birthday;
    }
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    public String getOrgId() {
        return orgId;
    }
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
    public String getOrgName() {
        return orgName;
    }
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getProjectSubjectId() {
        return projectSubjectId;
    }
    public void setProjectSubjectId(String projectSubjectId) {
        this.projectSubjectId = projectSubjectId;
    }
    public int getTrialIndex() {
        return trialIndex;
    }
    public void setTrialIndex(int trialIndex) {
        this.trialIndex = trialIndex;
    }
    public String getImageInfoId() {
        return imageInfoId;
    }
    public void setImageInfoId(String imageInfoId) {
        this.imageInfoId = imageInfoId;
    }
    @JsonIgnore
    public Date getDateUpdated() {
        return dateUpdated;
    }
    @JsonProperty(value="dateUpdated")
    public String getDateUpdatedFormatted() {
        return this.getDateUpdated() == null ? "" : DateUtil.formatSimpleDate(this.getDateUpdated());
    }
    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
    public HashMap<String, String> getAreaQcStatus() {
        return areaQcStatus;
    }
    public void setAreaQcStatus(HashMap<String, String> qcStatus) {
        this.areaQcStatus = qcStatus;
    }
    public void addAreaQcStatus(String itemName, String status) {
        this.areaQcStatus.put(itemName, status);
    }
}