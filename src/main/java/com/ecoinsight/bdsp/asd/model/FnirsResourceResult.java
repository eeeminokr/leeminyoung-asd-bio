package com.ecoinsight.bdsp.asd.model;

public class FnirsResourceResult extends Result {
    private String fileName;
    private int fileCountProcess;

    public int getFileCountProcess() {
        return fileCountProcess;
    }

    public void setFileCountProcess(int fileCountProcess) {
        this.fileCountProcess = fileCountProcess;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
