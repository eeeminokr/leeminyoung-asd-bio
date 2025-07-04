package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SurveyStatus {

    private long id;
    private String systemId;
    private String orgId;
    private String subjectId;
    private long projectSeq;
    private int trialIndex;
    private String gender;
    private String birthDay;
    private String registDate;
    private String kdst;
    private String kmchat;
    private String kqchat;
    private String selsi;
    private String cbcl;
    private String ados2mt;
    private String ados2m1;
    private String ados2m2;
    private String ados2m3;
    private String srs2;
    private String adir;
    private String scqlifetime;
    private String scqcurrent;
    private String pres;
    private String kcars2;
    private String bedevelq;
    private String bedeveli;
    private String bedevelp;
    private String kvineland;
    private String kbayley;
    private String kwppsi;

    protected int month;

    public int getMonth() {
        return this.month;
    }

    public void setMonth(int month) {
        this.month = month;
    }
    private LocalDateTime dateCreated;
    private String userCreated;
    private LocalDateTime dateUpdated;
    private String userUpdated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getKdst() {
        return kdst;
    }

    public void setKdst(String kdst) {
        this.kdst = kdst;
    }

    public String getKmchat() {
        return kmchat;
    }

    public void setKmchat(String kmchat) {
        this.kmchat = kmchat;
    }

    public String getKqchat() {
        return kqchat;
    }

    public void setKqchat(String kqchat) {
        this.kqchat = kqchat;
    }

    public String getSelsi() {
        return selsi;
    }

    public void setSelsi(String selsi) {
        this.selsi = selsi;
    }

    public String getCbcl() {
        return cbcl;
    }

    public void setCbcl(String cbcl) {
        this.cbcl = cbcl;
    }

    public String getAdos2mt() {
        return ados2mt;
    }

    public void setAdos2mt(String ados2mt) {
        this.ados2mt = ados2mt;
    }

    public String getAdos2m1() {
        return ados2m1;
    }

    public void setAdos2m1(String ados2m1) {
        this.ados2m1 = ados2m1;
    }

    public String getAdos2m2() {
        return ados2m2;
    }

    public void setAdos2m2(String ados2m2) {
        this.ados2m2 = ados2m2;
    }

    public String getAdos2m3() {
        return ados2m3;
    }

    public void setAdos2m3(String ados2m3) {
        this.ados2m3 = ados2m3;
    }

    public String getSrs2() {
        return srs2;
    }

    public void setSrs2(String srs2) {
        this.srs2 = srs2;
    }

    public String getAdir() {
        return adir;
    }

    public void setAdir(String adir) {
        this.adir = adir;
    }

    public String getScqlifetime() {
        return scqlifetime;
    }

    public void setScqlifetime(String scqlifetime) {
        this.scqlifetime = scqlifetime;
    }

    public String getScqcurrent() {
        return scqcurrent;
    }

    public void setScqcurrent(String scqcurrent) {
        this.scqcurrent = scqcurrent;
    }

    public String getPres() {
        return pres;
    }

    public void setPres(String pres) {
        this.pres = pres;
    }

    public String getKcars2() {
        return kcars2;
    }

    public void setKcars2(String kcars2) {
        this.kcars2 = kcars2;
    }

    public String getBedevelq() {
        return bedevelq;
    }

    public void setBedevelq(String bedevelq) {
        this.bedevelq = bedevelq;
    }

    public String getBedeveli() {
        return bedeveli;
    }

    public void setBedeveli(String bedeveli) {
        this.bedeveli = bedeveli;
    }

    public String getBedevelp() {
        return bedevelp;
    }

    public void setBedevelp(String bedevelp) {
        this.bedevelp = bedevelp;
    }

    public String getKvineland() {
        return kvineland;
    }

    public void setKvineland(String kvineland) {
        this.kvineland = kvineland;
    }

    public String getKbayley() {
        return kbayley;
    }

    public void setKbayley(String kbayley) {
        this.kbayley = kbayley;
    }

    public String getKwppsi() {
        return kwppsi;
    }

    public void setKwppsi(String kwppsi) {
        this.kwppsi = kwppsi;
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

    public String getRegistDate() {
        return registDate;
    }

    public void setRegistDate(String registDate) {
        this.registDate = registDate;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("systemId", systemId);
        map.put("orgId", orgId);
        map.put("subjectId", subjectId);
        map.put("projectSeq", projectSeq);
        map.put("trialIndex", trialIndex);
        map.put("gender", gender);
        map.put("birthDay", birthDay);
        map.put("id", id);
        map.put("systemId", systemId);
        map.put("orgId", orgId);
        map.put("subjectId", subjectId);
        map.put("projectSeq", projectSeq);
        map.put("trialIndex", trialIndex);
        map.put("gender", gender);
        map.put("birthDay", birthDay);
        map.put("K-DST", kdst);
        map.put("KM-CHAT", kmchat);
        map.put("K-QCHAT", kqchat);
        map.put("SELSI", selsi);
        map.put("PRES", pres);
        map.put("CBCL", cbcl);
        map.put("ADOS-2(Mod T)", ados2mt);
        map.put("ADOS-2(Mod 1)", ados2m1);
        map.put("ADOS-2(Mod 2)", ados2m2);
        map.put("ADOS-2(Mod 3)", ados2m3);
        map.put("SRS-2", srs2);
        map.put("ADI-R", adir);
        map.put("SCQ(lifetime)", scqlifetime);
        map.put("K-CARS-2", kcars2);
        map.put("BEDEVEL-Q", bedevelq);
        map.put("BEDEVEL-I", bedeveli);
        map.put("BEDEVEL-P", bedevelp);
        map.put("K-Vineland", kvineland);
        map.put("K-Bayley-III", kbayley);
        map.put("K-WPPSI-IV", kwppsi);
        map.put("SCQ(current)", scqcurrent);
        map.put("dateCreated", dateCreated);
        map.put("userCreated", userCreated);
        map.put("dateUpdated", dateUpdated);
        map.put("userUpdated", userUpdated);
        map.put("registDate", registDate);
        map.put("dateCreated", dateCreated);
        map.put("userCreated", userCreated);
        map.put("dateUpdated", dateUpdated);
        map.put("userUpdated", userUpdated);
        map.put("registDate", registDate);
        return map;
    }

    @Override
    public String toString() {
        return "SurveyStatus [id=" + id + ", systemId=" + systemId + ", orgId=" + orgId + ", subjectId=" + subjectId
                + ", projectSeq=" + projectSeq + ", trialIndex=" + trialIndex + ", gender=" + gender + ", registDate="
                + registDate + ", birthDay="
                + birthDay + ", kdst=" + kdst + ", kmchat=" + kmchat + ", kqchat=" + kqchat + ", selsi=" + selsi
                + ", cbcl=" + cbcl + ", ados2mt=" + ados2mt + ", ados2m1=" + ados2m1 + ", ados2m2=" + ados2m2
                + ", ados2m3=" + ados2m3 + ", srs2="
                + srs2 + ", adir=" + adir + ", scqlifetime=" + scqlifetime + ", scqcurrent=" + scqcurrent + ", pres="
                + pres + ", kcars2=" + kcars2 + ", bedevelq="
                + bedevelq + ", bedeveli=" + bedeveli + ", bedevelp=" + bedevelp + ", kvineland=" + kvineland
                + ", kbayley=" + kbayley + ", kwppsi=" + kwppsi + ", dateCreated=" + dateCreated + ", userCreated="
                + userCreated + ", dateUpdated=" + dateUpdated + ", userUpdated=" + userUpdated + "]";
    }

}
