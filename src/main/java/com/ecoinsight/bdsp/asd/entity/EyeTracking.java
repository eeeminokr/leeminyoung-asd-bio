package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDateTime;
import java.util.List;

public class EyeTracking {
    private long id;
    private String systemId;
    protected long projectSeq;
    protected int trialIndex;
    private String orgId;
    protected String subjectId;
    protected Long patientSeq;
    protected String setKey;
    protected String type;
    protected Integer measureTime;
    protected String registDt;
    protected Integer deviceHeight;
    protected Integer deviceWidth;
    protected Integer failCount;
    protected String appVersion;
    protected String phoneModel;
    protected String osVersion;

    private List<EyeTrackingInfo> eyeTrackingInfos;
    private LocalDateTime dateCreated;
    private String userCreated;

    private String note;
    private String gender;

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

    private long eyeTrackingInfoId;
    private Integer sort;
    private Double grade;
    private String aoi;
    private String media;
    private String trackInfos;
    private LocalDateTime eyeInfoDateCreated;

    public long getEyeTrackingInfoId() {
        return eyeTrackingInfoId;
    }

    public void setEyeTrackingInfoId(long eyeTrackingInfoId) {
        this.eyeTrackingInfoId = eyeTrackingInfoId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public String getAoi() {
        return aoi;
    }

    public void setAoi(String aoi) {
        this.aoi = aoi;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getTrackInfos() {
        return trackInfos;
    }

    public void setTrackInfos(String trackInfos) {
        this.trackInfos = trackInfos;
    }

    public LocalDateTime getEyeInfoDateCreated() {
        return eyeInfoDateCreated;
    }

    public void setEyeInfoDateCreated(LocalDateTime eyeInfoDateCreated) {
        this.eyeInfoDateCreated = eyeInfoDateCreated;
    }

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

    public Long getPatientSeq() {
        return patientSeq;
    }

    public void setPatientSeq(Long patientSeq) {
        this.patientSeq = patientSeq;
    }

    public String getSetKey() {
        return setKey;
    }

    public void setSetKey(String setKey) {
        this.setKey = setKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(Integer measureTime) {
        this.measureTime = measureTime;
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

    public List<EyeTrackingInfo> getEyeTrackingInfos() {
        return eyeTrackingInfos;
    }

    public void setEyeTrackingInfos(List<EyeTrackingInfo> eyeTrackingInfos) {
        this.eyeTrackingInfos = eyeTrackingInfos;
    }

    public Integer getDeviceHeight() {
        return deviceHeight;
    }

    public void setDeviceHeight(Integer deviceHeight) {
        this.deviceHeight = deviceHeight;
    }

    public Integer getDeviceWidth() {
        return deviceWidth;
    }

    public void setDeviceWidth(Integer deviceWidth) {
        this.deviceWidth = deviceWidth;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    @Override
    public String toString() {
        return "EyeTracking [id=" + id + ", systemId=" + systemId + ", projectSeq=" + projectSeq + ", trialIndex="
                + trialIndex + ", orgId=" + orgId + ", subjectId=" + subjectId + ", patientSeq=" + patientSeq
                + ", setKey=" + setKey + ", type=" + type + ", measureTime=" + measureTime
                + ", failCount=" + failCount + ",  appVersion=" + appVersion + ", registDt=" + registDt
                + ", dateCreated=" + dateCreated + ", userCreated="
                + userCreated + "]";
    }

}
