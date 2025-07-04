package com.ecoinsight.bdsp.asd.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BoardComment {

    private long boardCommentId; // ==> boardCommentSeq // increament id
    private long boardItemId;
    private String contents;
    private String userId;
    private String writerId; // T_member name //연구관리자
    private String writerName;
    private long parentCommentId; // ==> partentCommentId 원댓글 번호
    private LocalDateTime dateCreated;
    private String userCreated;
    private LocalDateTime dateUpdated;
    private String userUpdated;
    private int countNum;
    private int newState;



    /**
     * 해당 게시물을 수정(삭제) 할 수 있는지 여부 (true: 수정/삭제 가능)
     */
    private boolean canChange;

    private int level;

    /**
     * @return the boardCommentId
     */
    public long getBoardCommentId() {
        return boardCommentId;
    }

    /**
     * @param boardCommentId
     */
    public void setBoardCommentId(long boardCommentId) {
        this.boardCommentId = boardCommentId;
    }

    /**
     * @return the boardItemId
     */
    public long getBoardItemId() {
        return boardItemId;
    }

    /**
     * @param boardItemId
     */
    public void setBoardItemId(long boardItemId) {
        this.boardItemId = boardItemId;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the Contents
     */
    public String getContents() {
        return contents;
    }

    /**
     * @param contents
     */
    public void setContents(String contents) {
        this.contents = contents;
    }

    /**
     * @return the wirterId
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
     * @return
     */
    public String getWriterName() {
        return writerName;
    }

    /**
     * @param writerName
     */
    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    /**
     * @return
     */
    public long getParentCommentId() {
        return parentCommentId;
    }

    /**
     * @param parentCommentId
     */
    public void setParentCommentId(long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    /**
     * @return the DateCreated
     */
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated
     */
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return the UserCreated
     */
    public String getUserCreated() {
        return userCreated;
    }

    /**
     * @param userCreated
     */
    public void setUserCreated(String userCreated) {
        this.userCreated = userCreated;
    }

    /**
     * @return the DateUpdated
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

    private static String formatDate(LocalDateTime date) {
        if (date == null)
            return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private static String formatDateTime(LocalDateTime date) {
        if (date == null)
            return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
    }

    /**
     * @return the Userupdated
     */
    public String getUserUpdated() {
        return userUpdated;
    }

    /**
     * @param userUpdated
     */
    public void setUserUpdated(String userUpdated) {
        this.userUpdated = userUpdated;
    }

    /**
     * @return
     */
    public int getCountNum() {
        return countNum;
    }

    /**
     * @param countNum
     */
    public void setCountNum(int countNum) {
        this.countNum = countNum;
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return the canChange
     */
    public boolean isCanChange() {
        return canChange;
    }

    /**
     * @param canChange the canChange to set
     */
    public void setCanChange(boolean canChange) {
        this.canChange = canChange;
    }
    
    public int getNewState() {
        return newState;
    }

    public void setNewState(int newState) {
        this.newState = newState;
    }
}
