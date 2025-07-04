import { apiPaths } from "./constants";
import { httpClient } from "./http-client";


class BoardService {
    // For Mobile App
    addBoard(form) {
        const url = apiPaths.buildPath(`/v1/mobileboard/upload`);
        return httpClient.post(url, form,{'Content-Type': 'multipart/form-data'});
    }

    
    searchAllData(filters) {
        let ops =[];            
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);
        if (filters.userId) ops.push("userId=" + filters.userId);
        // if (filters.title) ops.push("title=" + filters.title);
        if (filters.all) ops.push("all=" + filters.all);
        const url = apiPaths.buildPath(`/v1/mobileboard/search/?${ops.join("&")}`);
        return httpClient.get(url);  
       
    }

    searchPassword(data){

  
        const url = apiPaths.buildPath(`/v1/mobileboard/search/verify/password/`)
        return httpClient.post(url, data, {'Content-Type': 'application/x-www-form-urlencoded'});

    }

    getOneBoard(id) {

        const url = apiPaths.buildPath(`/v1/mobileboard/search/${id}`);
        return httpClient.get(url);
    }

    getMobileBoardFiles(id) {
        const url = apiPaths.buildPath(`/v1/mobileboard/search/${id}/files`);
        return httpClient.get(url);
    }
  /**
     * 게시물의 첨부파일을 다운로드 한다.
     * @param {Number} itemId 
     * @param {Number} fileId 
     * @returns 
     */

    downloadingBoardFile(itemId, fileId) {
        const url = apiPaths.buildPath(`/v1/mobileboard/${itemId}/files/${fileId}`);
        return httpClient.get(url, { responseType: 'blob' });
    }

    searchCommentStatus(boardItemId) {
        const url = apiPaths.buildPath(`/v1/mobileboard/comment/upload/status/${boardItemId}`);
        return httpClient.get(url);
    }

    searchAllComment(id) {
        const url = apiPaths.buildPath(`/v1/mobileboard/comment/search/${id}`);
        return httpClient.get(url);
    }

    addBoardComment(boardItemId, PCommentId, data) {
        const url = apiPaths.buildPath(`/v1/mobileboard/comment/upload/${boardItemId}/${PCommentId}`);
        return httpClient.post(url, data,{'Content-Type': 'application/json'});
    }

    // For Web

    /**
     * 게시판 목록을 검색한다.
     * @param {} filters
     * @returns 
     */
    search(filters) {
        let ops =[];          
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);  
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);
        if (filters.userId) ops.push("userId=" + filters.userId);
        if (filters.title) ops.push("title=" + filters.title);
        if (filters.all) ops.push("all=" + filters.all);

        const url = apiPaths.buildPath(`/v1/board/search/?${ops.join("&")}`);
        return httpClient.get(url);        
    }

    /**
     * 게시물을 조회한다.
     * @param {Number} itemId 
     * @returns 
     */
    getBoardItem(itemId) {
        const url = apiPaths.buildPath(`/v1/board/${itemId}`);
        return httpClient.get(url);        
    }

    /**
     * 게시물의 답변 목록을 조회한다.
     * @param {Number} itemId 
     * @returns 
     */
    getBoardComments(itemId) {
        const url = apiPaths.buildPath(`/v1/board/${itemId}/comments`);
        return httpClient.get(url);        
    }



    /**
     * 게시물에 답변을 등록한다.
     * @param {Number} itemId 
     * @param {*} comment 
     * @returns 
     */
    addNewBoardComment(itemId, comment) {
        const url = apiPaths.buildPath(`/v1/board/${itemId}/comments`);
        return httpClient.post(url, comment,{'Content-Type': 'application/json'});
    }

    /**
     * 게시물의 답변을 수정한다.
     * @param {Number} itemId 
     * @param {*} comment 
     * @returns 
     */
    changeBoardComment(itemId, comment) {
        const url = apiPaths.buildPath(`/v1/board/${itemId}/comments`);
        return httpClient.put(url, comment);
    }

    /**
     * 게시물의 답변을 삭제한다.
     * @param {Number} itemId 
     * @param {*} commentId 
     * @returns 
     */
    removeBoardComment(itemId, commentId) {
        const url = apiPaths.buildPath(`/v1/board/${itemId}/comments/${commentId}`);
        return httpClient.delete(url);
    }

    /**
     * 게시물의 첨부파일 목록을 조회한다.
     * @param {Number} itemId 
     * @returns 
     */
    getBoardFiles(itemId) {
        const url = apiPaths.buildPath(`/v1/board/${itemId}/files`);
        return httpClient.get(url);        
    }

    /**
     * 게시물의 첨부파일을 다운로드 한다.
     * @param {Number} itemId 
     * @param {Number} fileId 
     * @returns 
     */
    downloadBoardFile(itemId, fileId) {
        const url = apiPaths.buildPath(`/v1/board/${itemId}/files/${fileId}`);
        return httpClient.get(url, { responseType: 'blob' });
        //return httpClient.post(url, idx, { responseType: 'blob' });
    }



    getfindAllOrganizations() {
        const url = apiPaths.buildPath(`/v1/projects/asd/organizations/q/`);
        return httpClient.get(url);
    }



}

export default new BoardService();
