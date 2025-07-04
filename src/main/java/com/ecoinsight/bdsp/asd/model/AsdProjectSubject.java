package com.ecoinsight.bdsp.asd.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("projectSubject")
public class AsdProjectSubject {
    @Id
    private String id;
    private String name;
    private LocalDateTime dateCreated;
    private String userCreated;
    private String dataSource;

    private LocalDateTime dateUpdated;
    private String userUpdated;

    boolean approvedExtended;

    /**
     * T_System.SystemId
     */
    private String systemId;
    /**
     * T_Organization.OrgId
     */
    private String orgId;
    /**
     * T_Organization.OrgName
     */
    private String orgName;
    /**
     * T_Project.ProjectSeq
     */
    private long projectSeq;
    /**
     * T_Project.ProjectName
     */
    private String projectName;

    private String subjectId;
    private String gender;
    
    private String note;


    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

  
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getSystemId() {
        return systemId;
    }
    public void setSystemId(String systemId) {
        this.systemId = systemId;
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
    public String getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getUserCreated() {
        return userCreated;
    }
    public void setUserCreated(String userCreated) {
        this.userCreated = userCreated;
    }
    public String getDataSource() {
        return dataSource;
    }
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
 
    /**
     * 대상자의 생년월일로 나이 계산
     * @param birthday 생년월일 (yyyy-mm-dd)
     * @param today 기준일
     * @return
     */
    public static int calculateAge(final String birthday, final LocalDate today) {
        final LocalDate day = LocalDate.parse(birthday);
        int years = Period.between(day, LocalDate.now()).getYears();
        return years;
    }
    
    /**
     * ASD 대상자 정보
     */
    private int state;
    private String registDate;
    private String startDate;
    private String endDate;
    private int trialIndex;
    private String phoneNumber;
    private String birthDay;
    private boolean omniStatus;
    private String omniResult;
    private boolean textStatus;
    private String textResult;
    private LocalDateTime dateTrialIndex;
    private String testYn;


    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getRegistDate() {
        return this.registDate;
    }

    public void setRegistDate(String registDate) {
        this.registDate = registDate;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getTrialIndex() {
        return this.trialIndex;
    }

    public void setTrialIndex(int trialIndex) {
        this.trialIndex = trialIndex;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

   
    public String getUserUpdated() {
        return userUpdated;
    }
    
    public void setUserUpdated(String userUpdated) {
        this.userUpdated = userUpdated;
    }


    public LocalDateTime getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateUpdated() {
        return this.dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getBirthDay() {
        return this.birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public boolean isApprovedExtended() {
        return this.approvedExtended;
    }

    public boolean getApprovedExtended() {
        return this.approvedExtended;
    }

    public void setApprovedExtended(boolean approvedExtended) {
        this.approvedExtended = approvedExtended;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isOmniStatus() {
        return this.omniStatus;
    }

    public boolean getOmniStatus() {
        return this.omniStatus;
    }

    public void setOmniStatus(boolean omniStatus) {
        this.omniStatus = omniStatus;
    }

    public String getOmniResult() {
        return this.omniResult;
    }

    public void setOmniResult(String omniResult) {
        this.omniResult = omniResult;
    }

    public boolean isTextStatus() {
        return this.textStatus;
    }

    public boolean getTextStatus() {
        return this.textStatus;
    }

    public void setTextStatus(boolean textStatus) {
        this.textStatus = textStatus;
    }

    public String getTextResult() {
        return this.textResult;
    }

    public void setTextResult(String textResult) {
        this.textResult = textResult;
    }
    
    public LocalDateTime getDateTrialIndex() {
        return this.dateTrialIndex;
    }

    public void setDateTrialIndex(LocalDateTime dateTrialIndex) {
        this.dateTrialIndex = dateTrialIndex;
    }
    
    public String getTestYn() {
        return testYn;
    }

    public void setTestYn(String testYn) {
        this.testYn = testYn;
    }
}
