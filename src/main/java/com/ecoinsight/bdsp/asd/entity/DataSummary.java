package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.imaging.QcStatusItem;

public class DataSummary {

    private String id; //mongoDB
    private String subjectId; //mongoDB
    private String projectName;
    private long projectSeq; //mongoDB
    private String gender; //mongoDB
    private String birthday; //mongoDB
    private int trialIndex; //mongoDB
    private int state; //mongoDB
    private String orgId;
    private String orgName;
    private String Name; //mongoDB
    private boolean firstSelection;
    private boolean secondSelection;
    private boolean mchat;
    private boolean eyeTracking;
    private boolean videoResource;
    private boolean pupillometry;
    private boolean microbiome;
    private boolean vitalSigns;
    private boolean audioResource;
    private boolean blood;
    private boolean stool;
    private boolean Urine;
    private boolean fnirs;
    private boolean eeg;
    private boolean mri;
    private boolean all;
    private String cUser;
    private LocalDateTime cDate;
    private String uUser;
    private LocalDateTime uDate;

    private String tsakName;

    private String testYn; //mongoDB

    private String year;
    private int month;
    private String count;

    private String registDate;
    private LocalDateTime dateTrialIndex;

    private HashMap<String, String> areaQcStatus = new HashMap<String, String>();
    private List<QcStatusItem> qcItems;
    private HashMap<String, Boolean> hasData;

    public static final String DATA_GROUP_FIRST = "FIRST";
    public static final String DATA_GROUP_SECOND = "SECOND";
    public static final String DATA_GROUP_M_CHAT = "M_CHAT";
    public static final String DATA_GROUP_EYE_TRACKING = "EYE_TRACKING";
    public static final String DATA_GROUP_VIDEO = "VIDEO";
    public static final String DATA_GROUP_VITAL_SIGNS = "VITAL_SIGNS"; //음성자료료
    public static final String DATA_GROUP_PUPILLOMETRY = "PUPILLOMETRY";
    public static final String DATA_GROUP_AUDIO = "AUDIO";
    public static final String DATA_GROUP_BLOOD = "BLOOD";
    public static final String DATA_GROUP_STOOL = "STOOL";
    public static final String DATA_GROUP_URINE = "URINE";
    public static final String DATA_GROUP_FNIRS = "FNIRS";
    public static final String DATA_GROUP_EEG = "EEG";
    public static final String DATA_GROUP_MRI = "MRI";
    public static final String DATA_GROUP_MICROBIOME = "MICROBIOME";
    public static final String DATA_GROUP_ALL = "ALL";

    public static final Hashtable<String, List<String>> PREFIX_TABLE = new Hashtable<>() {
        {
            put(DATA_GROUP_FIRST, List.of("FIRST"));
            put(DATA_GROUP_SECOND, List.of("SECOND"));
            put(DATA_GROUP_M_CHAT, List.of("M_CHAT"));
            put(DATA_GROUP_EYE_TRACKING, List.of("EYE_TRACKING"));
            put(DATA_GROUP_VIDEO, List.of("VIDEO"));
            put(DATA_GROUP_VITAL_SIGNS, List.of("VITAL_SIGNS"));
            put(DATA_GROUP_PUPILLOMETRY, List.of("PUPILLOMETRY"));
            put(DATA_GROUP_AUDIO, List.of("AUDIO"));
            put(DATA_GROUP_BLOOD, List.of("BLOOD"));
            put(DATA_GROUP_STOOL, List.of("STOOL"));
            put(DATA_GROUP_URINE, List.of("URINE"));
            put(DATA_GROUP_FNIRS, List.of("FNIRS"));
            put(DATA_GROUP_EEG, List.of("EEG"));
            put(DATA_GROUP_MRI, List.of("MRI"));
            put(DATA_GROUP_ALL, List.of("ALL"));

        }
    };

    public void SummaryUpdateForm(DataSummary summary) {
        this.subjectId = summary.getSubjectId();
        this.projectSeq = summary.getProjectSeq();
        this.gender = summary.getGender();
        this.birthday = summary.getBirthday();
        this.trialIndex = summary.getTrialIndex();
        this.state = summary.getState();
        this.orgId = summary.getOrgId();
        this.orgName = summary.getOrgName();
        this.Name = summary.getName();
        this.cUser = summary.getCUser();
        this.cDate = summary.getCDate();
        this.uUser = summary.getUUser();
        this.uDate = summary.getUDate();
        this.testYn = summary.getTestYn();
        this.year = summary.getYear();
        this.month = summary.getMonth();
        this.registDate = summary.getRegistDate();

    }

    // // Check if the summary has a value for dateTrialIndex
    // if (summary.getDateTrialindex() != null) {
    // this.dateTrialindex = summary.getDateTrialindex();
    // } else {
    // // Handle the case where dateTrialIndex is null
    // // Set a default value for dateTrialIndex
    // // For example, set it to LocalDateTime.now()
    // this.dateTrialindex = LocalDateTime.now();
    // }
    public void projectSubjectForm(AsdProjectSubject source) {
        // Copy the values from the AsdProjectSubject object to the fields of this class

        this.Name = source.getName();
        this.cUser = source.getUserCreated();
        this.cDate = source.getDateCreated();
        this.uUser = source.getUserCreated();
        this.uDate = source.getDateUpdated();
        this.uUser = source.getUserUpdated();
        this.orgId = source.getOrgId();
        this.orgName = source.getOrgName();
        this.projectSeq = source.getProjectSeq();
        this.projectName = source.getProjectName();
        this.subjectId = source.getSubjectId();
        this.gender = source.getGender();
        this.state = source.getState();
        this.registDate = source.getRegistDate();
        this.trialIndex = source.getTrialIndex();
        this.birthday = source.getBirthDay();
        this.dateTrialIndex = source.getDateTrialIndex();
        this.testYn = source.getTestYn();
    }

    public Map<String, Boolean> trialInfoState(DataSummary summary) {

        Map<String, Boolean> map = new HashMap<>();

        this.mchat = summary.getMchat();
        this.eyeTracking = summary.getEyeTracking();
        this.videoResource = summary.getVideoResource();
        this.firstSelection = summary.getFirstSelection();
        this.secondSelection = summary.getSecondSelection();
        this.vitalSigns = summary.getVitalSigns();
        this.pupillometry = summary.getPupillometry();
        this.audioResource = summary.getAudioResource();
        this.blood = summary.getBlood();
        this.stool = summary.getStool();
        this.Urine = summary.getUrine();
        this.fnirs = summary.getFnirs();
        this.eeg = summary.getEeg();
        this.mri = summary.getMri();

        map.put(DataSummaryColumn.MChat.name(), summary.getMchat());
        map.put(DataSummaryColumn.Eyetracking.name(), summary.getEyeTracking());
        map.put(DataSummaryColumn.VideoResource.name(), summary.getVideoResource());
        map.put(DataSummaryColumn.FirstSelection.name(), summary.getFirstSelection());
        map.put(DataSummaryColumn.SecondSelection.name(), summary.getSecondSelection());
        map.put(DataSummaryColumn.VitalSigns.name(), summary.getVitalSigns());
        map.put(DataSummaryColumn.PUPILLOMETRY.name(), summary.getPupillometry());
        map.put(DataSummaryColumn.AudioResource.name(), summary.getAudioResource());
        map.put(DataSummaryColumn.Blood.name(), summary.getBlood());
        map.put(DataSummaryColumn.Stool.name(), summary.getStool());
        map.put(DataSummaryColumn.Urine.name(), summary.getUrine());
        map.put(DataSummaryColumn.FNIRS.name(), summary.getFnirs());
        map.put(DataSummaryColumn.EEG.name(), summary.getEeg());
        map.put(DataSummaryColumn.MRI.name(), summary.getMri());
        map.put(DataSummaryColumn.MICROBIOME.name(), summary.getMicrobiome());
        return map;

    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubjectId() {
        return this.subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public long getProjectSeq() {
        return this.projectSeq;
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

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return this.birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getTrialIndex() {
        return this.trialIndex;
    }

    public void setTrialIndex(int trialIndex) {
        this.trialIndex = trialIndex;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public boolean isFirstSelection() {
        return this.firstSelection;
    }

    public boolean getFirstSelection() {
        return this.firstSelection;
    }

    public void setFirstSelection(boolean firstSelection) {
        this.firstSelection = firstSelection;
    }

    public boolean isSecondSelection() {
        return this.secondSelection;
    }

    public boolean getSecondSelection() {
        return this.secondSelection;
    }

    public void setSecondSelection(boolean secondSelection) {
        this.secondSelection = secondSelection;
    }

    public boolean isMchat() {
        return this.mchat;
    }

    public boolean getMchat() {
        return this.mchat;
    }

    public void setMchat(boolean mchat) {
        this.mchat = mchat;
    }

    public boolean isEyeTracking() {
        return this.eyeTracking;
    }

    public boolean getEyeTracking() {
        return this.eyeTracking;
    }

    public void setEyeTracking(boolean eyeTracking) {
        this.eyeTracking = eyeTracking;
    }

    public boolean isVideoResource() {
        return this.videoResource;
    }

    public boolean getVideoResource() {
        return this.videoResource;
    }

    public void setVideoResource(boolean videoResource) {
        this.videoResource = videoResource;
    }

    public boolean isVitalSigns() {
        return this.vitalSigns;
    }

    public boolean getVitalSigns() {
        return this.vitalSigns;
    }

    public void setVitalSigns(boolean vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public boolean isAudioResource() {
        return this.audioResource;
    }

    public boolean getAudioResource() {
        return this.audioResource;
    }

    public void setAudioResource(boolean audioResource) {
        this.audioResource = audioResource;
    }

    public boolean isBlood() {
        return this.blood;
    }

    public boolean getBlood() {
        return this.blood;
    }

    public void setBlood(boolean blood) {
        this.blood = blood;
    }

    public boolean isStool() {
        return this.stool;
    }

    public boolean getStool() {
        return this.stool;
    }

    public void setStool(boolean stool) {
        this.stool = stool;
    }

    public boolean isUrine() {
        return this.Urine;
    }

    public boolean getUrine() {
        return this.Urine;
    }

    public void setUrine(boolean Urine) {
        this.Urine = Urine;
    }

    public boolean isFnirs() {
        return this.fnirs;
    }

    public boolean getFnirs() {
        return this.fnirs;
    }

    public void setFnirs(boolean fnirs) {
        this.fnirs = fnirs;
    }

    public boolean isEeg() {
        return this.eeg;
    }

    public boolean getEeg() {
        return this.eeg;
    }

    public void setEeg(boolean eeg) {
        this.eeg = eeg;
    }

    public boolean isMri() {
        return this.mri;
    }

    public boolean getMri() {
        return this.mri;
    }

    public void setMri(boolean mri) {
        this.mri = mri;
    }

    public boolean isAll() {
        return this.all;
    }

    public boolean getAll() {
        return this.all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public String getCUser() {
        return this.cUser;
    }

    public void setCUser(String cUser) {
        this.cUser = cUser;
    }

    public LocalDateTime getCDate() {
        return this.cDate;
    }

    public void setCDate(LocalDateTime cDate) {
        this.cDate = cDate;
    }

    public String getUUser() {
        return this.uUser;
    }

    public void setUUser(String uUser) {
        this.uUser = uUser;
    }

    public LocalDateTime getUDate() {
        return this.uDate;
    }

    public void setUDate(LocalDateTime uDate) {
        this.uDate = uDate;
    }

    public HashMap<String, String> getAreaQcStatus() {
        return this.areaQcStatus;
    }

    public void setAreaQcStatus(HashMap<String, String> areaQcStatus) {
        this.areaQcStatus = areaQcStatus;
    }

    public List<QcStatusItem> getQcItems() {
        return this.qcItems;
    }

    public void setQcItems(List<QcStatusItem> qcItems) {
        this.qcItems = qcItems;
    }

    public HashMap<String, Boolean> getHasData() {
        return this.hasData;
    }

    public void setHasData(HashMap<String, Boolean> hasData) {
        this.hasData = hasData;
    }

    public void addAreaQcStatus(String itemName, String status) {
        this.areaQcStatus.put(itemName, status);
    }

    public String getOrgName() {
        return this.orgName;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

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

    public String getTestYn() {
        return testYn;
    }

    public void setTestYn(String testYn) {
        this.testYn = testYn;
    }

    public LocalDateTime getDateTrialIndex() {
        return dateTrialIndex;
    }

    public void setDateTrialIndex(LocalDateTime dateTrialIndex) {
        this.dateTrialIndex = dateTrialIndex;
    }

    public String getTsakName() {
        return tsakName;
    }

    public boolean isPupillometry() {
        return this.pupillometry;
    }

    public boolean getPupillometry() {
        return this.pupillometry;
    }

    public void setPupillometry(boolean pupillometry) {
        this.pupillometry = pupillometry;
    }

    public boolean isMicrobiome() {
        return this.microbiome;
    }

    public boolean getMicrobiome() {
        return this.microbiome;
    }

    public void setMicrobiome(boolean microbiome) {
        this.microbiome = microbiome;
    }

    public void setTsakName(String tsakName) {
        this.tsakName = tsakName;
    }

    @Override
    public String toString() {
        return "{"
                + " id='" + getId() + "'"
                + ", subjectId='" + getSubjectId() + "'"
                + ", projectName='" + getProjectName() + "'"
                + ", projectSeq='" + getProjectSeq() + "'"
                + ", gender='" + getGender() + "'"
                + ", birthday='" + getBirthday() + "'"
                + ", trialIndex='" + getTrialIndex() + "'"
                + ", orgId='" + getOrgId() + "'"
                + ", orgName='" + getOrgName() + "'"
                + ", Name='" + getName() + "'"
                + ", firstSelection='" + isFirstSelection() + "'"
                + ", secondSelection='" + isSecondSelection() + "'"
                + ", mchat='" + isMchat() + "'"
                + ", eyeTracking='" + isEyeTracking() + "'"
                + ", videoResource='" + isVideoResource() + "'"
                + ", pupillometry='" + isPupillometry() + "'"
                + ", audioResource='" + isAudioResource() + "'"
                + ", blood='" + isBlood() + "'"
                + ", stool='" + isStool() + "'"
                + ", Urine='" + isUrine() + "'"
                + ", fnirs='" + isFnirs() + "'"
                + ", eeg='" + isEeg() + "'"
                + ", mri='" + isMri() + "'"
                + ", microbiome='" + isMicrobiome() + "'"
                + ", all='" + isAll() + "'"
                + ", cUser='" + getCUser() + "'"
                + ", cDate='" + getCDate() + "'"
                + ", uUser='" + getUUser() + "'"
                + ", uDate='" + getUDate() + "'"
                + ", year='" + getYear() + "'"
                + ", month='" + getMonth() + "'"
                + ", count='" + getCount() + "'"
                + ", areaQcStatus='" + getAreaQcStatus() + "'"
                + ", qcItems='" + getQcItems() + "'"
                + ", hasData='" + getHasData() + "'"
                + "}";
    }

}
