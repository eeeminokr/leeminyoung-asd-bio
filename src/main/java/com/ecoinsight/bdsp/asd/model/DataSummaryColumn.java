package com.ecoinsight.bdsp.asd.model;

public enum DataSummaryColumn {

    FirstSelection("1차선별"),
    SecondSelection("2차선별"),
    MChat("M-Chat"),
    Eyetracking("시선처리"),
    VideoResource("상호작용영상"),
    VitalSigns("생체신호"),
    AudioResource("음성자료"),
    Blood("혈액"),
    Stool("대변"),
    Urine("소변"),
    FNIRS("fNIRS"),
    EEG("EEG"),
    MRI("MRI"),
    PUPILLOMETRY("동공측정"),
    MICROBIOME("장내미생물");

    private String description;

    private DataSummaryColumn(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

}
