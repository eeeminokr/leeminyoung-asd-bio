package com.ecoinsight.bdsp.asd.entity;

import java.util.List;

public class DashBoard {
    private String year;
    private String month;
    private long projectSeq;
    private String count;

    private String orgId;
    private String orgName;

    private int trialIndex;
    private int totalStateCount;
    private int totalNotStateCount;
    private int totalRegisterStateCount;
    private int eachAllTotal;

    private int allTotal;

    public int getTrialIndex() {
        return trialIndex;
    }

    public void setTrialIndex(int trialIndex) {
        this.trialIndex = trialIndex;
    }

    public int getTotalStateCount() {
        return totalStateCount;
    }

    public void setTotalStateCount(int totalStateCount) {
        this.totalStateCount = totalStateCount;
    }

    public int getTotalNotStateCount() {
        return totalNotStateCount;
    }

    public void setTotalNotStateCount(int totalNotStateCount) {
        this.totalNotStateCount = totalNotStateCount;
    }

    public int getTotalRegisterStateCount() {
        return totalRegisterStateCount;
    }

    public void setTotalRegisterStateCount(int totalRegisterStateCount) {
        this.totalRegisterStateCount = totalRegisterStateCount;
    }

    public int getEachAllTotal() {
        return eachAllTotal;
    }

    public void setEachAllTotal(int eachAllTotal) {
        this.eachAllTotal = eachAllTotal;
    }

    public int getAllTotal() {
        return allTotal;
    }

    public void setAllTotal(int allTotal) {
        this.allTotal = allTotal;
    }

    private List<DashBoard> projectDataState;

    public List<DashBoard> getProjectDataState() {
        return projectDataState;
    }

    public void setProjectDataState(List<DashBoard> projectDataState) {
        this.projectDataState = projectDataState;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getProjectSeq() {
        return projectSeq;
    }

    public void setProjectSeq(long projectSeq) {
        this.projectSeq = projectSeq;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    @Override
    public String toString() {
        return "DashBoard{" +
                "year='" + year + '\'' +
                ", month='" + month + '\'' +
                ", projectSeq=" + projectSeq +
                ", count='" + count + '\'' +
                ", orgId='" + orgId + '\'' +
                ", orgName='" + orgName + '\'' +
                ", trialIndex=" + trialIndex +
                ", totalStateCount=" + totalStateCount +
                ", totalNotStateCount=" + totalNotStateCount +
                ", totalRegisterStateCount=" + totalRegisterStateCount +
                ", eachAllTotal=" + eachAllTotal +
                ", allTotal=" + allTotal +
                ", projectDataState=" + projectDataState +
                '}';
    }
}