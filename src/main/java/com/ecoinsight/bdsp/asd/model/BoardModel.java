package com.ecoinsight.bdsp.asd.model;

public class BoardModel {
    

/**
 *private String checkHeader;
 */
private long boardItemId;
private String userId;
private String writerName;
private String title;
private String contents;
private String password;



/**
 * @return
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
public String getTitle() {
    return title;
}
/**
 * @param title
 */
public void setTitle(String title) {
    this.title = title;
}
/**
 * @return
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
 * @return
 */
public String getPassword() {
    return this.password;
}
/**
 * @param contents
 */
public void setPassword(String password) {
    this.password = password;
}
















}
