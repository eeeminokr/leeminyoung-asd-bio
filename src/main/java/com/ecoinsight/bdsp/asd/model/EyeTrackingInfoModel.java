package com.ecoinsight.bdsp.asd.model;

import java.util.List;
import io.swagger.annotations.ApiModelProperty;

public class EyeTrackingInfoModel {
    @ApiModelProperty(value = "측정 순서", example = "")
    private Integer sort;
    @ApiModelProperty(value = "시선 추적 점수", example = "")
    private Double grade;
    @ApiModelProperty(value = "Score 인직 영역 좌료", example = "")
    protected List<Double> aoi;
    @ApiModelProperty(value = "이미지/영상 위치", example = "")
    protected List<Double> media;
    @ApiModelProperty(value = "시선 Tracking 상세 정보", example = "")
    private List<TrackInfoModel> trackInfos;

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

    public List<TrackInfoModel> getTrackInfos() {
        return trackInfos;
    }

    public void setTrackInfos(List<TrackInfoModel> trackInfos) {
        this.trackInfos = trackInfos;
    }

    public List<Double> getAoi() {
        return aoi;
    }

    public void setAoi(List<Double> aoi) {
        this.aoi = aoi;
    }

    public List<Double> getMedia() {
        return media;
    }

    public void setMedia(List<Double> media) {
        this.media = media;
    }

    @Override
    public String toString() {
        return "EyeTrackingInfoModel [sort=" + sort + ", grade=" + grade + "]";
    }

}
