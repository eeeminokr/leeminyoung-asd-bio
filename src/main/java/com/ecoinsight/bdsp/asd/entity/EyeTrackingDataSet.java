package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "eyeTrackingId",
        "eyeTrackingInfoId",
        "subjectId",
        "projectSeq",
        "trialIndex",
        "note",
        "gender",
        "patientSeq",
        "sort",
        "type",
        "measureTime",
        "registDt",
        "deviceHeight",
        "deviceWidth",
        "setKey",
        "grade",
        "aoi",
        "media",
        "trackInfos",
        "userCreated",
        "dateCreated",
        "eyeInfoDateCreated"
})

public class EyeTrackingDataSet {

    private long eyeTrackingId;
    private long eyeTrackingInfoId;
    private String subjectId;
    private long projectSeq;
    private int trialIndex;
    private String note;
    private String gender;
    private Long patientSeq;
    private Integer sort;
    private String type;
    private Integer measureTime;
    private String registDt;
    private Integer deviceHeight;
    private Integer deviceWidth;
    private String setKey;
    private Double grade;
    private String aoi;
    private String media;
    private String trackInfos;
    private String userCreated;
    private String dateCreated;
    private String eyeInfoDateCreated;

    public long getEyeTrackingId() {
        return eyeTrackingId;
    }

    public void setEyeTrackingId(long eyeTrackingId) {
        this.eyeTrackingId = eyeTrackingId;
    }

    public long getEyeTrackingInfoId() {
        return eyeTrackingInfoId;
    }

    public void setEyeTrackingInfoId(long eyeTrackingInfoId) {
        this.eyeTrackingInfoId = eyeTrackingInfoId;
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

    public Long getPatientSeq() {
        return patientSeq;
    }

    public void setPatientSeq(Long patientSeq) {
        this.patientSeq = patientSeq;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
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

    public String getSetKey() {
        return setKey;
    }

    public void setSetKey(String setKey) {
        this.setKey = setKey;
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

    public String getUserCreated() {
        return userCreated;
    }

    public void setUserCreated(String userCreated) {
        this.userCreated = userCreated;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getEyeInfoDateCreated() {
        return eyeInfoDateCreated;
    }

    public void setEyeInfoDateCreated(String eyeInfoDateCreated) {
        this.eyeInfoDateCreated = eyeInfoDateCreated;
    }

}