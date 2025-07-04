package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDateTime;
import java.util.HashMap;

public class VideoResource {
    private long id;
    private String systemId;
    private long projectSeq;
    private String projectName;
    private int trialIndex;
    private String orgId;
    private String subjectId;
    private String interfaceId;
    private String answeredYn;
    private int retryIndex;
    private String originalFileName;
    private String targetFileName;
    private LocalDateTime dateCreated;
    private String userCreated;
    private LocalDateTime dateUpdated;
    private String userUpdated;
    private String hostname;
    private boolean failed;
    private boolean published;
    private boolean labelled;
    private boolean deleted;
    private String publishedError;
    private LocalDateTime datePublished;
    private LocalDateTime dateLabelled;
    private LocalDateTime dateChangeProject;
    private String orgName;
    private String gender;
    private String registDate;
    private String trialBirthDay;
    private int birthDay;
    private String interfaceName;
    private boolean videoResourceState;
    private long subjectProjectSeq;

    public long getSubjectProjectSeq() {
        return subjectProjectSeq;
    }

    public void setSubjectProjectSeq(long subjectProjectSeq) {
        this.subjectProjectSeq = subjectProjectSeq;
    }

    public boolean isVideoResourceState() {
        return videoResourceState;
    }

    public void setVideoResourceState(boolean videoResourceState) {
        this.videoResourceState = videoResourceState;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    private HashMap<String, String> areaQcStatus = new HashMap<String, String>();

    /*
     * 추후 변경 요청으로 인해 유지
     * IF2001("말소리에 반응하기"),
     * IF2002("모방하기A"),
     * IF2006("간식활동"),
     * IF2007("자유놀이"),
     * IF5("NO_SUBJECT"),
     * IF6("NO_SBUJECT");
     */
    public enum InterfaceIdone {
        IF2001("A"),
        IF2002("B"),
        IF2006("F"),
        IF2007("G"),
        IF5("NO_SUBJECT"),
        IF6("NO_SBUJECT");

        private final String value;

        InterfaceIdone(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /*
     * 추후 변경 요청으로 인해 유지
     * IF2001("말소리에 반응하기"),
     * IF2003("모방하기B"),
     * IF2004("공놀이"),
     * IF2006("간식활동"),
     * IF2007("자유놀이"),
     * IF6("NO_SBUJECT");
     */
    public enum InterfaceIdtwo {
        IF2001("A"),
        IF2003("C"),
        IF2004("D"),
        IF2006("F"),
        IF2007("G"),
        IF6("NO_SBUJECT");

        private final String value;

        InterfaceIdtwo(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    /*
     * 추후 변경 요청으로 인해 유지
     * IF2001("말소리에 반응하기"),
     * IF2003("모방하기B"),
     * IF2004("공놀이"),
     * IF2005("상징놀이"),
     * IF2006("간식활동"),
     * IF2007("자유놀이");
     */
    public enum InterfaceIdthree {
        IF2001("A"),
        IF2003("C"),
        IF2004("D"),
        IF2005("E"),
        IF2006("F"),
        IF2007("G");

        private final String value;

        InterfaceIdthree(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static boolean contains(String interfaceId) {
            for (InterfaceIdthree id : InterfaceIdthree.values()) {
                if (id.name().equals(interfaceId)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void addAreaQcStatus(String itemName, String status) {
        this.areaQcStatus.put(itemName, status);
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

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSystemId() {
        return this.systemId;
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

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
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

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isFailed() {
        return this.failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public LocalDateTime getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(LocalDateTime datePublished) {
        this.datePublished = datePublished;
    }

    public String getPublishedError() {
        return this.publishedError;
    }

    public void setPublishedError(String publishedError) {
        this.publishedError = publishedError;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getUserUpdated() {
        return userUpdated;
    }

    public void setUserUpdated(String userUpdated) {
        this.userUpdated = userUpdated;
    }

    public boolean isLabelled() {
        return labelled;
    }

    public void setLabelled(boolean labelled) {
        this.labelled = labelled;
    }

    public LocalDateTime getDateLabelled() {
        return dateLabelled;
    }

    public void setDateLabelled(LocalDateTime dateLabelled) {
        this.dateLabelled = dateLabelled;
    }

    public LocalDateTime getDateChangeProject() {
        return dateChangeProject;
    }

    public void setDateChangeProject(LocalDateTime dateChangeProject) {
        this.dateChangeProject = dateChangeProject;
    }

    public String getRegistDate() {
        return registDate;
    }

    public void setRegistDate(String registDate) {
        this.registDate = registDate;
    }

    public HashMap<String, String> getAreaQcStatus() {
        return areaQcStatus;
    }

    public void setAreaQcStatus(HashMap<String, String> areaQcStatus) {
        this.areaQcStatus = areaQcStatus;
    }

    public String getAnsweredYn() {
        return answeredYn;
    }

    public void setAnsweredYn(String answeredYn) {
        this.answeredYn = answeredYn;
    }
}
