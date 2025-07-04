import axios from 'axios';
import { apiPaths } from "./constants";
import { httpClient } from "./http-client";

class TrialService {
    searchAllData(filters) {
        let ops = [];
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);

        //const url = apiPaths.buildPath(`/v1/trials/q/?${ops.join("&")}`);
        const url = apiPaths.buildPath(`/v1/datasummary/q/?${ops.join("&")}`);
        return httpClient.get(url);
    }

    uploadDataFile(taskId, form) {
        const url = apiPaths.buildPath(`/v1/imaging/upload/file/${taskId}`);
        return httpClient.post(url, form, {'Content-Type': 'multipart/form-data'});
    }



    deleteManyImagingData(items) {
        const requests = [];
        (items || []).map(val=>{ 
            const url = apiPaths.buildPath(`/v1/projects/${val.projectId}/trials/${val.trialIndex}/subjects/${val.subjectId}/imaging/`);
            const f = function(url){ return httpClient.delete(url); }
            requests.push(f(url));
        });

        return axios.all(requests);
    }

    uploadDataFiles(taskId, form, multipleFiles, onUploadProgress) {            
        let url = apiPaths.buildPath(`/v1/imaging/upload/files/${taskId}`);
        if (!multipleFiles) url = apiPaths.buildPath(`/v1/imaging/upload/file/${taskId}`);

        return httpClient.post(url, form, {
            headers: {
            "Content-Type": "multipart/form-data"
          },
          onUploadProgress: onUploadProgress});
    }
    uploadPupillometryDataFile(taskId, form, multipleFiles, onUploadProgress) {
        const url = apiPaths.buildPath(`/v1/pupillometry/upload/file/${taskId}`);
        return httpClient.post(url, form, {
             headers : 
            {'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: onUploadProgress
        });
    }

    uploadFnirsDataFile(taskId, form, multipleFiles, onUploadProgress) {
        const url = apiPaths.buildPath(`/v1/fnirsresource/upload/file/${taskId}`);
        return httpClient.post(url, form, {
             headers : 
            {'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: onUploadProgress
        });
    }

    uploadMicrobiomeDataFile(taskId, form, multipleFiles, onUploadProgress) {
        const url = apiPaths.buildPath(`/v1/microbiome/upload/file/${taskId}`);
        return httpClient.post(url, form, {
             headers : 
            {'Content-Type': 'multipart/form-data'
            },
            onUploadProgress: onUploadProgress
        });
    }

    countSubjectsHavingImagingData(filters) {
        let ops = [];
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);

        const url = apiPaths.buildPath(`/v1/trials/imaging/subjects-count/?${ops.join("&")}`);
        return httpClient.get(url);
    }

    searchImagingData(filters) {
        let ops = [];
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);

        const url = apiPaths.buildPath(`/v1/trials/imaging/q/?${ops.join("&")}`);
        return httpClient.get(url);
    }

    downloadImagingData(idx) {
        const url = apiPaths.buildPath(`/v1/trials/imaging/download/`);
        return httpClient.post(url, idx, { responseType: 'blob' });
    }

    searchVideoResource(filters){
        let ops = [];        
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);

        const url = apiPaths.buildPath(`/v1/video/request/search/?${ops.join("&")}`);
        return httpClient.get(url);

    }

    searchEyeTrackingResource(filters){
        let ops = [];        
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);

        const url = apiPaths.buildPath(`/v1/eyetracking/request/search/?${ops.join("&")}`);
        return httpClient.get(url);
    }


    searchPupillometryResource(filters){
        let ops = [];        
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.trialIndex) ops.push("trialIndex=" + filters.trialIndex);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);
        const url = apiPaths.buildPath(`/v1/pupillometry/request/search/?${ops.join("&")}`);
        return httpClient.get(url);
    }

    searchFnirsResourceResource(filters){
        let ops = [];        
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.trialIndex) ops.push("trialIndex=" + filters.trialIndex);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);
        const url = apiPaths.buildPath(`/v1/fnirsresource/request/search/?${ops.join("&")}`);
        return httpClient.get(url);
    }
    searchMicrobiomeResource(filters){
        let ops = [];        
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.trialIndex) ops.push("trialIndex=" + filters.trialIndex);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);
        const url = apiPaths.buildPath(`/v1/microbiome/request/search/?${ops.join("&")}`);
        return httpClient.get(url);
    }


    searchMchatData(filters) { 
        let ops = [];
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);

        const url = apiPaths.buildPath(`/v1/mchart/result/?${ops.join("&")}`);
        return httpClient.get(url);
    }
   
    videoDataCount(filters){

        let ops = [];        
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);

        const url = apiPaths.buildPath(`/v1/video/request/search/count/?${ops.join("&")}`);
        return httpClient.get(url);


    }

    searchSelectionData(filters) {
        let ops = [];        
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);

        const url = apiPaths.buildPath(`/v1/selection/result/?${ops.join("&")}`);
        return httpClient.get(url);
    }

    searchMcdData(filters) {
        let ops = [];        
        if (filters.projectId > 0) ops.push("id=" + filters.projectId);
        if (filters.subjectId) ops.push("subject=" + filters.subjectId);
        if (filters.orgId) ops.push("org=" + filters.orgId);
        if (filters.gender) ops.push("gender=" + filters.gender);
        if (filters.page) ops.push("page=" + filters.page);
        if (filters.offset) ops.push("offset=" + filters.offset);

        const url = apiPaths.buildPath(`/v1/trials/mcd/q/?${ops.join("&")}`);
        return httpClient.get(url);
    }


    getImagingData(id) {        
        const url = apiPaths.buildPath(`/v1/trials/imaging/${id}/`);
        return httpClient.get(url);
    }

    changeQcStatus(id, status) {
        const url = apiPaths.buildPath(`/v1/trials/imaging/${id}/qc/`);
        return httpClient.put(url, status);
    }

    saveComment(id, comment) {
        const url = apiPaths.buildPath(`/v1/trials/imaging/${id}/comment/`);
        return httpClient.put(url, comment);
    }

    deleteManyImagingSeriesData(id, series) {
        const params = [];
        series.map(val => params.push({name: val}));
        const url = apiPaths.buildPath(`/v1/trials/imaging/${id}/series/`);
        return httpClient.delete(url, { data: params });
    }

    deleteImagingSeriesData(id, seriesId) {
        const url = apiPaths.buildPath(`/v1/trials/imaging/${id}/series/${seriesId}/`);
        return httpClient.delete(url);
    }

    getProgress(taskId) {
        const url = apiPaths.buildPath(`/v1/imaging/upload/files/progress/${taskId}/`);
        return httpClient.get(url);
    }

    deleteTrialData(projectId, trialIndex, subjectId) {
        const url = apiPaths.buildPath(`/v1/projects/${projectId}/trials/${trialIndex}/subjects/${subjectId}/`);
        return httpClient.delete(url);
    }

    deleteTrialDatas(data) {
        const requests = [];
        (data || []).map(val=>{requests.push(this.deleteTrialData(val.projectId, val.trialIndex, val.subjectId));});
        
        return axios.all(requests);
    }

    deleteTrialDatas(items, type) {
        const requests = [];
        (items || []).map(val=>{ 
            const url = apiPaths.buildPath(`/v1/projects/${val.projectId}/trials/${val.trialIndex}/subjects/${val.subjectId}/${type}/`);
            console.debug(url);
            
            const f = function(url){ return httpClient.delete(url); }
            requests.push(f(url));
        });

        return axios.all(requests);
    }

    changeDataSummaryValue(id, projectId, trialIndex, column, boolean) {
        const url = apiPaths.buildPath(`/v1/datasummary/q/`);
        let data = {
            "column" : column,
            "subjectId" : id,
            "projectSeq" : projectId,
            "trialIndex" : trialIndex,
            "boolean" : boolean
        }
        return httpClient.put(url, JSON.stringify(data), {headers : { "Content-Type": `application/json`}});
    }

    download(subjectId, projectSeq, data) {
        // /api/v1/datasummary/download/{subjectId}/{projectSeq}
        const url = apiPaths.buildPath(`/v1/datasummary/download/${subjectId}/${projectSeq}`);
        const options = {
            responseType: 'arraybuffer',
            headers: {
              'Content-Type': 'application/json'
            }
        }
        return httpClient.post(url, JSON.stringify(data), options);
    }

    /**
     * 
     * @param {String} id MongoDB ImageInfo collection의 id 값
     * @param {String} seriesName ImageInfo에 등록된 series name
     */
    getBidsTag(id, seriesName) {
        const url = apiPaths.buildPath(`/v1/imaging/bids/tags/${id}/${seriesName}`);
        return httpClient.get(url);
    }

    /**
     * KBDS 모든 Tag들의 description
     * @returns 
     */
    getBidsTagDescription() {
        const url = apiPaths.buildPath(`/v1/imaging/bids/tags/description`);
        return httpClient.get(url);
    }


    dataSetDownload(projectSeq) {
        const options = {
            responseType: 'arraybuffer',
            headers: {
                'Content-Type': 'application/json'
            }
        }
        const url = apiPaths.buildPath(`/v1/mchart/dataset/download/${projectSeq}`);
        console.log("service.js[;url] - " + url)
        return httpClient.get(url, options);
    }


    dataSetSurveyDownload(filters) {
        let ops = [];        
        if (filters.projectId > 0) ops.push("projectSeq=" + filters.projectId);
        if (filters.subjectId) ops.push("subjectId=" + filters.subjectId);
        const options = {
            responseType: 'arraybuffer',
            headers: {
                'Content-Type': 'application/json'
            }
        }
        const url = apiPaths.buildPath(`/v1/survey/result/urban/surveystate?${ops.join("&")}`);
        console.log("service.js[;url] - " + url)
        return httpClient.get(url, options);
    }
}

export default new TrialService();