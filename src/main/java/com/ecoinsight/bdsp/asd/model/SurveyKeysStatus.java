package com.ecoinsight.bdsp.asd.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecoinsight.bdsp.core.util.DateUtil;

public enum SurveyKeysStatus {

    // ProjectSeq = 2,3
    K_DST(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 53, "K-DST", "Y", "Y"),
    KM_CHAT(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 30, "KM-CHAT", "Y", "Y"),
    K_QCHAT(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 30, "K-QCHAT", "Y", "Y"),
    SELSI(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 48, "SELSI", "Y", "Y"),
    PRES(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 48, "PRES", "Y", "Y"),
    CBCL(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 48, "CBCL", "Y", "Y"),
    SCQ_LIFETIME(new long[]{1, 2, 3, 4}, new int[]{1}, 24, 48, "SCQ(lifetime)", "Y", "Y"),
    BEDEVEL_Q(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 42, "BEDEVEL-Q", "Y", "Y"),
    BEDEVEL_I(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 42, "BEDEVEL-I", "Y", "Y"),
    BEDEVEL_P(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 42, "BEDEVEL-P", "Y", "Y"),
    K_VINELAND(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 48, "K-Vineland",
            "Y", "Y"),
    K_CARS_2(new long[]{2, 3, 4}, new int[]{1}, 24, 48, "K-CARS-2", "Y", "Y"), // projectSeq =2,3 ("Y")
    ADOS_2_MOD_T_Y(new long[]{2, 3}, new int[]{1}, 18, 48, "ADOS-2(Mod T)", "Y", "Y"), // // ProjectSeq =4
    // ("S") projectSeq
    // =2,3("Y")
    // trialIndex =1
    ADOS_2_MOD_1_Y(new long[]{2, 3}, new int[]{1}, 18, 48, "ADOS-2(Mod 1)", // ProjectSeq =4 ("S")
            "Y", "Y"), // projectSeq =2,3("Y")
    // trialIndex =1
    ADOS_2_MOD_2_Y(new long[]{2, 3}, new int[]{1}, 18, 48, "ADOS-2(Mod 2)", // ProjectSeq =4 ("S")
            "Y", "Y"), // projectSeq =2,3("Y")
    // trialIndex =1
    ADOS_2_MOD_3_Y(new long[]{2, 3}, new int[]{1}, 18, 48, "ADOS-2(Mod 3)", // ProjectSeq =4 ("S")
            "Y", "Y"), // projectSeq =2,3("Y")
    SRS_Y(new long[]{1, 2, 3, 4}, new int[]{1}, 31, 48, "SRS-2", "Y", "Y"),
    // OPTIONAL
    ADOS_2_MOD_T_S(new long[]{2, 3}, new int[]{3}, 18, 48, "ADOS-2(Mod T)", "Y", "N"), // projectSeq =2,3
    // trialIndex =// 3("S")
    ADOS_2_MOD_1_S(new long[]{2, 3}, new int[]{3}, 18, 48, "ADOS-2(Mod 1)", "Y", "N"), // projectSeq =2,3
    // trialIndex =// 3("S")
    ADOS_2_MOD_2_S(new long[]{2, 3}, new int[]{3}, 18, 48, "ADOS-2(Mod 2)", "Y", "N"), // projectSeq =2,3
    ADOS_2_MOD_3_S(new long[]{2, 3}, new int[]{3}, 18, 48, "ADOS-2(Mod 3)", "Y", "N"), // projectSeq =2,3

    // Optional

    SRS_S(new long[]{2, 3}, new int[]{2, 3}, 31, 48, "SRS-2", "Y", "N"), // ProjectSeq = 2,3 TrialIndex =2,3,4
    // ("S")
    SRS_30_1(new long[]{1, 2, 3, 4}, new int[]{1}, 30, 30, "SRS-2", "Y", "N"), // TrialIndex =1 ("S"),
    // ProjectSeq//
    // = // 1,2,3,4
    SRS_30_2(new long[]{2, 3}, new int[]{2, 3, 4}, 30, 30, "SRS-2", "Y", "N"), // TrialIndex =2,3,4 ("S"),
    // ProjectSeq =
    SCQ_QURRENT(new long[]{2, 3}, new int[]{2, 3, 4}, 24, 48, "SCQ(current)", "S", "N"), // TRIALiNDEX =2,3,4
    ADI_R(new long[]{2, 3, 4}, new int[]{1}, 24, 48, "ADI-R", "S", "N"),
    K_Bayley_III(new long[]{1, 2, 3, 4}, new int[]{1}, 18, 48, "K-Bayley-III", "S", "N"),
    K_WPPSI_IV(new long[]{1, 2, 3, 4}, new int[]{1}, 30, 48, "K-WPPSI-IV", "S", "N");

    // D-
    // K_DST(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 18, 53, "kdst", "Y"),
    // KM_CHAT(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 18, 30, "kmchat", "Y"),
    // K_QCHAT(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 18, 30, " kqchat", "Y"),
    // SELSI(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 18, 48, "selsi", "Y"),
    // PRES(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 18, 48, "pres", "Y"),
    // CBCL(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 18, 48, "cbcl", "Y"),
    // SCQ_LIFETIME(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 24, 48,
    // "scqlifetime", "Y"),
    // BEDEVEL_Q(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 18, 42, "bedevelq",
    // "Y"),
    // BEDEVEL_I(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 18, 42, "bedeveli",
    // "Y"),
    // BEDEVEL_P(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 18, 42, "bedevelp",
    // "Y"),
    // K_VINELAND(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 18, 48, "kvineland",
    // "Y"),
    // K_CARS_2(new long[] { 2, 3 }, new int[] { 1 }, 24, 48, "kcars2", "Y"), //
    // projectSeq =2,3 ("Y")
    // ADOS_2_MOD_T_Y(new long[] { 2, 3 }, new int[] { 1 }, 18, 48, "ados2mt", "Y"),
    // // projectSeq =2,3("Y")
    // // trialIndex =1
    // ADOS_2_MOD_1_Y(new long[] { 2, 3 }, new int[] { 1 }, 18, 48, "ados2m1", "Y"),
    // // projectSeq =2,3("Y")
    // // trialIndex =1
    // ADOS_2_MOD_2_Y(new long[] { 2, 3 }, new int[] { 1 }, 18, 48, "ados2m2", "Y"),
    // // projectSeq =2,3("Y")
    // // trialIndex =1
    // // OPTIONAL
    // ADOS_2_MOD_T_S(new long[] { 2, 3 }, new int[] { 3 }, 18, 48, "ados2mt", "S"),
    // // projectSeq =2,3 trialIndex =
    // // 3("S")
    // ADOS_2_MOD_1_S(new long[] { 2, 3 }, new int[] { 3 }, 18, 48, "ados2m1", "S"),
    // // projectSeq =2,3 trialIndex =
    // // 3("S")
    // ADOS_2_MOD_2_S(new long[] { 2, 3 }, new int[] { 3 }, 18, 48, "ados2m2", "S"),
    // // projectSeq =2,3 trialIndex =
    // // 3("S")
    // SRS_Y(new long[] { 1, 2, 3, 4 }, new int[] { 1 }, 31, 48, "srs", "Y"), //
    // TrialIndex =1 ("Y"), ProjectSeq = 2,3
    // SRS_S(new long[] { 2, 3 }, new int[] { 2, 3 }, 31, 48, "srs", "S"), //
    // ProjectSeq = 2,3 TrialIndex =2,3,4 ("S")
    // SRS_30(new long[] { 1, 2, 3, 4 }, new int[] { 1, 2, 3, 4 }, 30, 30, "srs",
    // "S"), // TrialIndex =1 ("S"), ProjectSeq
    // // = 2,3
    // // TrialIndex =2,3,4 ("S")
    // SCQ_QURRENT(new long[] { 2, 3 }, new int[] { 2, 3, 4 }, 24, 38, "scqcurrent",
    // "S"); // TRIALiNDEX =2,3,4
    private long[] projectSeqList;
    private int[] trialIndexList;
    private String subjectId;
    private long projectSeq;
    private int trialIndex;
    private int minBetweenMonth;
    private int maxBetweenMonth;
    private final String key;
    private String validationValue;

    private String value;

    SurveyKeysStatus(long[] projectSeqList, int[] trialIndexList, int minBetweenMonth, int maxBetweenMonth, String key,
            String validationValue, String value) {
        this.projectSeqList = projectSeqList;
        this.trialIndexList = trialIndexList;
        this.minBetweenMonth = minBetweenMonth;
        this.maxBetweenMonth = maxBetweenMonth;
        this.key = key;
        this.validationValue = validationValue;
        this.value = value;
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

    public long[] getProjectSeqList() {
        return this.projectSeqList;
    }

    public int[] getTrialIndexList() {
        return this.trialIndexList;
    }

    public String getKey() {
        return key;
    }

    public int getMinBetweenMonth() {
        return this.minBetweenMonth;
    }

    public int getMaxBetweenMonth() {
        return this.maxBetweenMonth;
    }

    public String getValue() {
        return value;
    }

    public String getValidationValue() {
        return validationValue;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SurveyKeysStatus.class);

    public static Map<String, Object> getSurveyKeys(long[] projectSeqArray, int[] trialIndexArray,
            int betweenMonth) {
        Map<String, Object> resultList = new HashMap<>();
        for (SurveyKeysStatus surveyKey : SurveyKeysStatus.values()) {

            if (Arrays.stream(surveyKey.projectSeqList)
                    .anyMatch(p -> Arrays.stream(projectSeqArray).anyMatch(q -> q == p))) {
                LOGGER.info("surveytrialIndex={}", surveyKey.trialIndex);
                LOGGER.info("surveytrialIndexArr={}", trialIndexArray);
                // Check if any value in the surveytrialIndex array matches any value in
                // surveytrialIndexArr
                boolean matchFoundProjectSeq = Arrays.stream(surveyKey.trialIndexList)
                        .anyMatch(t -> Arrays.stream(trialIndexArray).anyMatch(s -> s == t));
                boolean matchFoundTrialIndex = Arrays.stream(surveyKey.trialIndexList)
                        .anyMatch(t -> Arrays.stream(trialIndexArray).anyMatch(s -> s == t));

                if (matchFoundTrialIndex && betweenMonth >= surveyKey.minBetweenMonth
                        && betweenMonth <= surveyKey.maxBetweenMonth) {

                    resultList.put(surveyKey.getKey(), surveyKey.getValidationValue());
                } else {
                    LOGGER.warn(
                            "Trialindex 또는 개월수 차이 범위내에 해당하지 않습니다.");
                }
            }
        }

        LOGGER.info("resultList : {}", resultList);

        return resultList;
    }

    // public static SurveyKeysStatus getSurveyKey(long[] projectSeqArray, int[]
    // trialIndexArray, int betweenMonth) {
    // for (SurveyKeysStatus surveyKey : SurveyKeysStatus.values()) {
    // if (Arrays.stream(surveyKey.projectSeq)
    // .anyMatch(p -> Arrays.stream(projectSeqArray).anyMatch(q -> q == p))) {
    // LOGGER.info("surveytrialIndex={}", surveyKey.trialIndex);
    // LOGGER.info("surveytrialIndexArr={}", trialIndexArray);
    // if (Arrays.equals(surveyKey.trialIndex, trialIndexArray) &&
    // betweenMonth >= surveyKey.minBetweenMonth &&
    // betweenMonth <= surveyKey.maxBetweenMonth) {
    // return surveyKey;
    // } else {
    // throw new IllegalArgumentException(
    // "Trial index or betweenMonth is not within the valid range for the specified
    // SurveyKey");
    // }
    // }
    // }
    // return null;
    // }
    public static int getMonthsOfAge(String registDate, String birthday) throws Exception {
        Optional<LocalDate> boptional = DateUtil.parseSimple(birthday);
        Optional<LocalDate> roptional = DateUtil.parseSimple(registDate);
        if (boptional.isEmpty()) {
            throw new Exception("Invalid birthday format. - " + birthday);
        }
        if (roptional.isEmpty()) {
            throw new Exception("Invalid registDate format. - " + registDate);
        }
        LocalDate ldate = boptional.get();
        LocalDate rdate = roptional.get();
        Period diff = Period.between(ldate, rdate);
        int years = diff.getYears();
        int months = diff.getMonths();
        return years * 12 + months;
    }

    public boolean validate(String registDate, String birthday) throws Exception {
        int monthsOfAge = getMonthsOfAge(registDate, birthday);
        if (minBetweenMonth <= monthsOfAge && monthsOfAge <= maxBetweenMonth) {
            return true;
        }
        return false;
    }
    // public static SurveyKeysStatus surveyKeys(String name) {
    // for (SurveyKeys surveyKey : SurveyKeys.values()) {
    // if (surveyKey.name().equals(name)) {
    // return surveyKey;
    // }
    // }
    // return null;
    // }
}
