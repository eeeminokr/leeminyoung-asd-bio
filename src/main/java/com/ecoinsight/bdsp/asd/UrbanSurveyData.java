package com.ecoinsight.bdsp.asd;

public class UrbanSurveyData {
    private String subjectNo;
    private String subjectGroupCd;
    private String projectSeq;
    private int numOfTimes;
    private String surveyKindCd;
    private String markNo;
    private String questionNm;
    private String answer;

    public String getSubjectNo() {
        return subjectNo;
    }

    public void setSubjectNo(String subjectNo) {
        this.subjectNo = subjectNo;
    }

    public String getProjectSeq() {
        return projectSeq;
    }

    public void setProjectSeq(String projectSeq) {
        this.projectSeq = projectSeq;
    }

    public String getSubjectGroupCd() {
        return subjectGroupCd;
    }

    public void setSubjectGroupCd(String subjectGroupCd) {
        this.subjectGroupCd = subjectGroupCd;
    }

    public int getNumOfTimes() {
        return numOfTimes;
    }

    public void setNumOfTimes(int numOfTimes) {
        this.numOfTimes = numOfTimes;
    }

    public String getSurveyKindCd() {
        return surveyKindCd;
    }

    public void setSurveyKindCd(String surveyKindCd) {
        this.surveyKindCd = surveyKindCd;
    }

    public String getMarkNo() {
        return markNo;
    }

    public void setMarkNo(String markNo) {
        this.markNo = markNo;
    }

    public String getQuestionNm() {
        return questionNm;
    }

    public void setQuestionNm(String questionNm) {
        this.questionNm = questionNm;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public enum ProjectSeqNm {
        NORMAL("1"),
        ASD_HIGH("2"),
        ASD("3"),
        HOLD("4");

        private final String value;

        ProjectSeqNm(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
