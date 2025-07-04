package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDateTime;
import java.util.HashMap;

public class EyeTrackingInfo {
    private long id;
    protected long eyeTrackingId;
    protected Integer sort;
    protected Double grade;
    protected String aoi;
    protected String media;
    private String trackInfos;

    private String systemId;
    protected long projectSeq;
    private String projectName;

    protected int trialIndex;
    private String orgId;
    protected String orgName;
    private String registDate;
    protected String birthDay;
    private String gender;
    private String Name;
    protected String subjectId;
    protected long patientSeq;
    protected String type;

    private LocalDateTime dateCreated;
    private String userCreated;

    private HashMap<String, String> areaQcStatus = new HashMap<String, String>();

    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void addAreaQcStatus(String itemName, String status) {
        this.areaQcStatus.put(itemName, status);
    }

    public HashMap<String, String> getAreaQcStatus() {
        return areaQcStatus;
    }

    public void setAreaQcStatus(HashMap<String, String> areaQcStatus) {
        this.areaQcStatus = areaQcStatus;
    }

    public enum VIDEO_TYPE {
        SORT1("1"),
        SORT2("2"),
        SORT3("3"),
        SORT4("4"),
        SORT5("5");

        private final String value;

        VIDEO_TYPE(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum IMAGE_TYPE {
        SORT1("1"),
        SORT2("2"),
        SORT3("3"),
        SORT4("4"),
        SORT5("5"),
        SORT6("6"),
        SORT7("7"),
        SORT8("8"),
        SORT9("9"),
        SORT10("10");

        private final String value;

        IMAGE_TYPE(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public String getOrgName() {
        return orgName;
    }

    public String getRegistDate() {
        return registDate;
    }

    public void setRegistDate(String registDate) {
        this.registDate = registDate;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public long getPatientSeq() {
        return patientSeq;
    }

    public void setPatientSeq(long patientSeq) {
        this.patientSeq = patientSeq;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getEyeTrackingId() {
        return eyeTrackingId;
    }

    public void setEyeTrackingId(long eyeTrackingId) {
        this.eyeTrackingId = eyeTrackingId;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getTrackInfos() {
        return trackInfos;
    }

    public void setTrackInfos(String trackInfos) {
        this.trackInfos = trackInfos;
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

}
