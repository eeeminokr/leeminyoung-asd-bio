package com.ecoinsight.bdsp.asd.model;

public enum ProjectSeq {
    NORMAL(1L),
    ASD_HIGH(2L),
    ASD(3L),
    HOLD(4L);

    private String title;
    private long seq;
    
    private ProjectSeq(long seq){
        this.seq = seq;
    }

    public long getSeq(){
        return this.seq;
    }

}
