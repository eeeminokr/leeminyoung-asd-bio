package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.springframework.web.multipart.MultipartFile;

public class Board { 
    private long boardItemId; // ==> boardSeq
    private String userId; 
    private String boardCode;
    private String title;
    private String contents;
    private String writerId;
    private String writerName;
    private String password;
    private LocalDateTime dateCreated;
    private String userCreated;
    private LocalDateTime dateUpdated;
    private String userUpdated;
    private String orgId;

    private String orgName;
    private String status;
    private int commentCount;
    private boolean newMstate;
    private boolean newBstate;



    private HashMap<String, String> areaNewStatus = new HashMap<String, String>();


    public HashMap<String, String> getAreaNewStatus() {
        return areaNewStatus;
    }

    public void setAreaNewStatus(HashMap<String, String> areaNewStatus) {
        this.areaNewStatus = areaNewStatus;
    }
    // private List<QcStatusItem> qcItems;
    private HashMap<String, Boolean> hasData;
    
    


    public HashMap<String,Boolean> getHasData() {
        return this.hasData;
    }

    public void setHasData(HashMap<String,Boolean> hasData) {
        this.hasData = hasData;
    }
    public void addAreaNewStatus(String itemName, String status) {
        this.areaNewStatus.put(itemName, status);
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


   public boolean isNewMstate() {
        return newMstate;
    }
    public void setNewMstate(boolean newMstate) {
        this.newMstate = newMstate;
    }
    public boolean isNewBstate() {
        return newBstate;
    }
    public void setNewBstate(boolean newBstate) {
        this.newBstate = newBstate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
    * @return the boardCode
    */
    public String getBoardCode() {
        return this.boardCode;
    }
    /**
     * @param boardCode the boardCode to set
     */
    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * @return the contents
     */
    public String getContents() {
        return contents;
    }
    /**
     * @param contents the contents to set
     */
    public void setContents(String contents) {
        this.contents = contents;
    }
     /**
     * @return
     */
    public String getWriterId() {
        return writerId;
    }
    /**
     * @param writerId
     */
    public void setWriterId(String writerId) {
        this.writerId = writerId;
    }
    /**
     * @return the writerName
     */
    public String getWriterName() {
        return writerName;
    }
    /**
     * @param writerName the writerName to set
     */
    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }
    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
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
     * @param dateTimeCreated the dateTimeCreated to set
     */
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
    /**
     * @return the userUpdated
     */
    public String getUserUpdated() {
        return userUpdated;
    }
    /**
     * @param userUpdated the userUpdated to set
     */
    public void setUserUpdated(String userUpdated) {
        this.userUpdated = userUpdated;
    }
    /**
     * @return the dateTimeUpdated
     */
    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }
    public String getDateTimeUpdatedFormatted() {
        return formatDate(dateUpdated);
    }
    /**
     * @param dateUpdated the dateUpdated to set
     */
    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    private static String formatDate(LocalDateTime date)
    {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private static String formatDateTime(LocalDateTime date)
    {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
    }
    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    /**
     * @return the commentCount
     */
    public int getCommentCount() {
        return commentCount;
    }
    /**
     * @param commentCount the commentCount to set
     */
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    
}
