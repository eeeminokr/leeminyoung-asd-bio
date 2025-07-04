import axios from 'axios';
import { apiPaths } from "./constants";
import { httpClient } from "./http-client";

class ProjectService {    
    getProjects() {
        const url = apiPaths.buildPath("/v1/projects/q/");
        return httpClient.get(url);
    }
    getAsdProjects() {
        const url = apiPaths.buildPath("/v1/projects/asd/q/");
        return httpClient.get(url);
    }
    getProject(id) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/`);
        return httpClient.get(url);
    }

    getProjectType(id) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/attributes/projecttype/`);
        return httpClient.get(url);
    }

    createProject(project) {
        const url = apiPaths.buildPath("/v1/projects/");
        return httpClient.post(url, project);
    }

    deleteProject(id) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/`);
        return httpClient.delete(url, null);
    }

    changeProject(id, project) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/`);        
        return httpClient.put(url, project);
    }

    getResearchers(id) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/researchers/`);
        return httpClient.get(url);
    }

    addResearchers(id, researchers) {
        const requests = [];
        (researchers || []).map(val=>{requests.push(this.addResearcher(id, val));});
        
        return axios.all(requests);
    }

    addResearcher(id, memberKey) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/researchers/`);
        return httpClient.post(url, {'memberKey': memberKey});
    }

    removeResearchers(id, researchers) {
        const requests = [];
        (researchers || []).map(val=>{requests.push(this.removeResearcher(id, val));});
        
        return axios.all(requests);
    }

    removeResearcher(id, memberKey) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/researchers/${memberKey}/`);
        return httpClient.delete(url);
    }

    changeResearchers(id, researchers) {
        const requests = [];
        (researchers || []).map(val=>{requests.push(this.changeResearcher(id, val));});
        
        return axios.all(requests);
    }

    changeResearcher(id, researcher) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/researchers/${researcher.id}/`);
        return httpClient.put(url, researcher, {headers:{'Content-Type': 'application/json; charset=utf-8'}});
    }

    getTrialItems(id) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/trials/trial-items/`);
        return httpClient.get(url);
    }

    getTrials(id) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/trials/`);
        return httpClient.get(url);
    }

    removeTrials(id, trials) {
        const requests = [];
        (trials || []).map(val=>{ requests.push(this.removeTrial(id, val));});
        
        return axios.all(requests);
    }

    removeTrial(id, trial) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/trials/remove/`);
        return httpClient.put(url, trial);
    }

    changeTrials(id, trials) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/trials/`);
        return httpClient.put(url, trials);
    }

    changeTrial(id, trial) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/trials/${trial.projectTrialSeq}/`);
        return httpClient.put(url, trial);
    }

    searchFunctions(id, memberkey) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/${memberkey}/functions/`);
        return httpClient.get(url);
    }

    changePermission(id, memberkey, permissions) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/${memberkey}/functions/`);
        return httpClient.put(url, permissions, {'Content-Type': 'application/json'});
    }
    changePermissions(id, memberkey, permissions) {
        const url = apiPaths.buildPath(`/v1/projects/asd/${id}/${memberkey}/functions/`);
        return httpClient.put(url, permissions, {'Content-Type': 'application/json'});
    }
    getOrganizations(id) {
        const url = apiPaths.buildPath(`/v1/projects/${id}/organizations/`);
        return httpClient.get(url);
    }

    getAsdOrganizations(id) {
        const url = apiPaths.buildPath(`/v1/projects/asd/${id}/organizations/`);
        return httpClient.get(url);
    }

}

export default new ProjectService();