package com.ecoinsight.bdsp.asd.model;

import io.swagger.annotations.ApiModelProperty;

public class VideoModel {

    @ApiModelProperty(value = "프로젝트 과제 순번", example = "")
    protected long projectSeq;
    @ApiModelProperty(value = "프로젝트 과제 차수", example = "")
    protected int trialIndex;
    @ApiModelProperty(value = "영상별 인터페이스 ID", example = "")
    protected String interfaceId;
    @ApiModelProperty(value = "대상자 ID", example = "")
    protected String subjectId;
    @ApiModelProperty(value = "재시도 차수", example = "")
    protected int retryIndex;
    @ApiModelProperty(value = "상호작용답변", example = "")
    protected String answeredYn;


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

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public int getRetryIndex() {
        return retryIndex;
    }

    public void setRetryIndex(int retryIndex) {
        this.retryIndex = retryIndex;
    }
    public String getAnsweredYn() {
        return answeredYn;
    }

    public void setAnsweredYn(String answeredYn) {
        this.answeredYn = answeredYn;
    }

    @Override
    public String toString() {
        return "ImageBaseModel [interfaceId=" + interfaceId + ", subjectId=" + subjectId
                + ", retryIndex=" + retryIndex + ", answeredYn=" + answeredYn + "]";
    }

}
