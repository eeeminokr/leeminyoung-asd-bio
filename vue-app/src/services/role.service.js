import axios from 'axios';
import { apiPaths } from "./constants";
import { httpClient } from "./http-client";

class RoleService {
    searchRoles() {
        const url = apiPaths.buildPath("/v1/roles/");
        return httpClient.get(url);
    }

    createRole(role) {
        const url = apiPaths.buildPath("/v1/roles/");
        return httpClient.post(url, role);
    }

    removeRoles(roles) {
        const requests = [];
        (roles || []).map(id=>{requests.push(this.removeRole(id));});
        
        return axios.all(requests);
    }

    removeRole(roleId) {
        this.removeRoles(null);

        const url = apiPaths.buildPath(`/v1/roles/${roleId}/remove/`);
        return httpClient.put(url);
    }

    searchFunctions(roleId) {
        const url = apiPaths.buildPath(`/v1/roles/${roleId}/functions/`);
        return httpClient.get(url);
    }

    grantFunction(roleId, functionId) {
        const url = apiPaths.buildPath(`/v1/roles/${roleId}/functions/`);
        const data = { 'functionId': functionId };
        return httpClient.post(url, data, {'Content-Type': 'application/json'});
    }

    revokeFunction(roleId, functionId) {
        const url = apiPaths.buildPath(`/v1/roles/${roleId}/functions/${functionId}/`);
        return httpClient.delete(url);
    }

    changePermission(roleId, functionId, permissions) {
        const url = apiPaths.buildPath(`/v1/roles/${roleId}/functions/${functionId}/`);
        return httpClient.put(url, permissions, {'Content-Type': 'application/json'});
    }

    
    changePermissions(roleId, functionId, permissions) {
        const url = apiPaths.buildPath(`/v1/roles/asd/${roleId}/functions/${functionId}/`);
        return httpClient.put(url, permissions, {'Content-Type': 'application/json'});
    }

    // changeGroupPermission (groupPermission) {
    //     const url = apiPaths.buildPath("/v1/functions/member-group/permission/change");
    //     return httpClient.post(url, groupPermission, {'Content-Type': 'application/json'});
    // }

    

    // changeMemberPermission (id, memberPermission) {
    //     const url = apiPaths.buildPath(`/v1/functions/members/${id}/change`);
    //     return httpClient.post(url, memberPermission, {'Content-Type': 'application/json'});
    // }

    
}

export default new RoleService();