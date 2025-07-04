import axios from 'axios';
import { apiPaths } from "./constants";
import { httpClient } from "./http-client";

class SubjectService {
    /**
     * 대상자 목록 조회 (페이징)
     * @param {Object} filters 
     * @returns 
     */
    searchSubjects(filters) {
        let ops = [];        
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if(filters.gender)ops.push("gender="+filters.gender);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);        

        const url = apiPaths.buildPath(`/v1/subjects/q/?${ops.join("&")}`);
        return httpClient.get(url);
    }

    /**
     *  모든 대상자 조회
     * @param {Object} filters 
     * @returns 
     */
    getAllSubjects(filters) {
        let ops = [];        
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);  

        const url = apiPaths.buildPath(`/v1/subjects/?${ops.join("&")}`);
        return httpClient.get(url);
    }

    /**
     * 대상자 조회
     * @param {Number} projectId 과제 번호
     * @param {String} subjectId 
     * @returns 
     */
    getSubject(projectId, subjectId) {
        const url = apiPaths.buildPath(`/v1/projects/${projectId}/subjects/${subjectId}/`);
        return httpClient.get(url);
    }

    addNewSubject(projectId, data) {
        const url = apiPaths.buildPath(`/v1/projects/${projectId}/subjects/`);
        return httpClient.post(url, data);
    }

    changeSubject(projectId, subjectid, data) {
        const url = apiPaths.buildPath(`/v1/projects/${projectId}/subjects/${subjectid}/`);
        return httpClient.put(url, data);
    }

    deleteSubjects(subjects) {
        const requests = [];
        (subjects || []).map(val=>{ requests.push(this.deleteSubject(val.projectSeq, val.subjectId));});
        
        return axios.all(requests);
    }

    deleteSubject(projectId, subjectId) {
        const url = apiPaths.buildPath(`/v1/projects/${projectId}/subjects/${subjectId}/`);
        return httpClient.delete(url, {});
    }

    uploadSubjectExcelFile(form) {
        const url = apiPaths.buildPath(`/v1/subjects/add/excel/`);
        return httpClient.post(url, form, {'Content-Type': 'multipart/form-data'});
    }

    /**
     * 다수의 인체유래물 기증 동의서 파일들 (PDF)을 업로드한다.
     * @param {number} projectId 
     * @param {FormData} form 
     * @param {string} fileType 
     * @returns
     */
    uploadHumanDerivativeDonationAgreementFile(projectId, form, fileType = 'pdf') {
        const url = apiPaths.buildPath(`/v1/projects/${projectId}/subjects/upload/HumanDerivativeDonationAgreement/?type=${fileType}`);
        return httpClient.post(url, form, {'Content-Type': 'multipart/form-data'});
    }
}

export default new SubjectService();