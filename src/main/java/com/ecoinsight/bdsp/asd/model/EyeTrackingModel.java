package com.ecoinsight.bdsp.asd.model;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * {
 * "patientSeq": 98150,
 * "subJectId": "1022101021"
 * "setKey": "b86ce480-115c-49dd-8e76-ae82cfa39ea4"
 * "type": "image",
 * "measureTime":10,
 * "registDt": "2023-01-25 14:05:33",
 * "eyeTrackingInfos": [
 * {
 * "sort": 1,
 * "grade": 8.76
 * },
 * {
 * "sort": 2,
 * "grade": 6.76
 * },
 * {
 * "sort": 3,
 * "grade": 5.43
 * },
 * {
 * "sort": 4,
 * "grade": 1.22
 * },
 * {
 * "sort": 5,
 * "grade": 2.56
 * }
 * ]
 * }
 */
public class EyeTrackingModel {
    @ApiModelProperty(value = "프로젝트 과제 순번", example = "")
    protected long projectSeq;
    @ApiModelProperty(value = "프로젝트 과제 차수", example = "")
    protected int trialIndex;
    @ApiModelProperty(value = "피험자 고유 번호", example = "")
    protected String subjectId;
    @ApiModelProperty(value = "피험자 고유 ID", example = "")
    protected Long patientSeq;
    @ApiModelProperty(value = "측정 set 키값", example = "")
    protected String setKey;
    @ApiModelProperty(value = "측정 컨텐츠 타입", example = "")
    protected String type;
    @ApiModelProperty(value = "측정 시간", example = "")
    protected Integer measureTime;
    @ApiModelProperty(value = "측정 날짜", example = "")
    protected String registDt;
    @ApiModelProperty(value = "시선 추적 데이터", example = "")
    protected List<EyeTrackingInfoModel> eyeTrackingInfos;
    @ApiModelProperty(value = "디바이스 높이", example = "")
    protected Integer deviceHeight;
    @ApiModelProperty(value = "디바이스 넓이", example = "")
    protected Integer deviceWidth;
    @ApiModelProperty(value = "옴니 data update failcount", example = "")
    protected Integer failCount;
    @ApiModelProperty(value = "옴니 appversion data", example = "")
    protected String appVersion;
    @ApiModelProperty(value = "옴니 client phone model", example = "")
    protected String phoneModel;
    @ApiModelProperty(value = "옴니 Osversion data", example = "")
    protected String osVersion;

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

    public String getRegistDt() {
        return registDt;
    }

    public void setRegistDt(String registDt) {
        this.registDt = registDt;
    }

    public void setMeasureTime(Integer measureTime) {
        this.measureTime = measureTime;
    }

    public List<EyeTrackingInfoModel> getEyeTrackingInfos() {
        return eyeTrackingInfos;
    }

    public void setEyeTrackingInfos(List<EyeTrackingInfoModel> eyeTrackingInfos) {
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
}
