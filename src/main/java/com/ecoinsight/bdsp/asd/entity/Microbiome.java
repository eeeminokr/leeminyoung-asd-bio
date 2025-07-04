package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Microbiome {
    private Long id;
    private String systemId;
    private long projectSeq;
    private String orgId;
    private String orgName;
    private String projectName;
    private String subjectId;
    private int trialIndex;
    private String originalFileName;
    private String targetFileName;
    private String userCreated;
    private LocalDateTime dateCreated;
    private String userUpdated;
    private LocalDateTime dateUpdated;
    private String hostname;
    private boolean failed;
    private boolean published;
    private String statusCompleted;
    private boolean completed;
    private boolean deleted;
    private String publishedError;
    private LocalDateTime datePublished;
    private LocalDateTime dateCompleted;
    private String userName;
    private String gender;
    private String registDate;
    private int birthDay;
    private String trialBirthDay;
    private HashMap<String, String> areaQcStatus = new HashMap<String, String>();

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public int getTrialIndex() {
        return trialIndex;
    }

    public void setTrialIndex(int trialIndex) {
        this.trialIndex = trialIndex;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public String getUserCreated() {
        return userCreated;
    }

    public void setUserCreated(String userCreated) {
        this.userCreated = userCreated;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getUserUpdated() {
        return userUpdated;
    }

    public void setUserUpdated(String userUpdated) {
        this.userUpdated = userUpdated;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getStatusCompleted() {
        return statusCompleted;
    }

    public void setStatusCompleted(String statusCompleted) {
        this.statusCompleted = statusCompleted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getPublishedError() {
        return publishedError;
    }

    public void setPublishedError(String publishedError) {
        this.publishedError = publishedError;
    }

    public LocalDateTime getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(LocalDateTime datePublished) {
        this.datePublished = datePublished;
    }

    public LocalDateTime getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(LocalDateTime dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRegistDate() {
        return registDate;
    }

    public void setRegistDate(String registDate) {
        this.registDate = registDate;
    }

    public int getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(int birthDay) {
        this.birthDay = birthDay;
    }

    public String getTrialBirthDay() {
        return trialBirthDay;
    }

    public void setTrialBirthDay(String trialBirthDay) {
        this.trialBirthDay = trialBirthDay;
    }

    public HashMap<String, String> getAreaQcStatus() {
        return areaQcStatus;
    }

    public void setAreaQcStatus(HashMap<String, String> areaQcStatus) {
        this.areaQcStatus = areaQcStatus;
    }

    public void addAreaQcStatus(String itemName, String status) {
        this.areaQcStatus.put(itemName, status);
    }
}
