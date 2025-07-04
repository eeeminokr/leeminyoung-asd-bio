package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDateTime;

public class BoardFile {

    private long fileId; // boardFileSeq
    private long boardItemId;
    private String boardCode;
    private String fileName;
    private String fileExtension;
    private long fileSize;
    private String baseUrl;
    private String fileUrl;
    private String filePath;
    private String userCreated;
    private LocalDateTime dateCreated;

    public BoardFile() {
    }

    public BoardFile(long itemId, long fileId, String fileName, String filePath) {
        this.boardItemId = itemId;
        this.fileId = fileId;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public BoardFile(long itemId, String fileName, String filePath) {
        this.boardItemId = itemId;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    /**
     * @return the fileId
     */
    public long getFileId() {
        return fileId;
    }

    /**
     * @param fileId the fileId to set
     */
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    /**
     * @return the boardItemId
     */
    public long getBoardItemId() {
        return boardItemId;
    }

    /**
     * @param boardItemId the boardItemId to set
     */
    public void setBoardItemId(long boardItemId) {
        this.boardItemId = boardItemId;
    }
    /**
     * @return
     */
    public String getBoardCode() {
        return boardCode;
    }

    /**
     * @param boardCode
     */
    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }
    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the fileExtension
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * @param fileExtension the fileExtension to set
     */
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize the fileSize to set
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @return the fileUrl
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * @param fileUrl the fileUrl to set
     */
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the userCreated
     */
    public String getUserCreated() {
        return userCreated;
    }

    /**
     * @param userCreated the userCreated to set
     */
    public void setUserCreated(String userCreated) {
        this.userCreated = userCreated;
    }

    /**
     * @return the dateTimeCreated
     */
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated the dateTimeCreated to set
     */
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

}
