import axios from 'axios'
import { apiPaths, constants } from './constants'
import { httpClient } from "./http-client"

var Buffer = require('buffer/').Buffer

const instance = axios.create({
    //baseURL: 'http://localhost:9090/api/',
    timeout: 60000,
    headers: {'X-Custom-Header': 'kapju'}
  });

const authUser = {
    isAuthenticated: sessionStorage.getItem(constants.STORAGE_KEY_TOKEN)
};

class AuthService {
    
    tokenExpired() { 
        const exp = new Date(this.getJwtValue("exp") * 1000);
        const expired = new Date() > exp;

        return expired;
    }
    
    login(user) {
        // remove access token in session storage
        sessionStorage.removeItem(constants.STORAGE_KEY_TOKEN);
        // remove refresh token in session storage
        sessionStorage.removeItem(constants.STORAGE_KEY_REFRESH_TOKEN);        

        const url = apiPaths.Login;
        return instance.post(
            url,
            {
                systemId: user.systemId,
                userId: user.userId,
                password: user.password
            }
        )
        .then(res=>{
            if (res.data.succeeded && res.data.jwtToken) {
                // Save access token in session storage
                sessionStorage.setItem(constants.STORAGE_KEY_TOKEN, JSON.stringify(res.data.jwtToken));
                // Save refresh token in session storage
                sessionStorage.setItem(constants.STORAGE_KEY_REFRESH_TOKEN, res.data.refreshToken);
            }
            else {
                return Promise.reject("로그인 요청을 처리할 수 없습니다.");
            }
            return res.data;
        })
        .catch(error=>{
            if (!error.response) {
                alert('요청을 처리 중 오류가 발생했습니다.\n잠시 후 다시 시도하시기 바랍니다.');
              }
            return Promise.reject(error);
        });
    }

    logout() {
        const url = apiPaths.Logout;
        return httpClient.post(
            url,
            { memberKey: this.getLoginMemberKey() }
        );
    }

    /**
     * Request new jwt access token
     * @returns new jwt access token
     */
    refreshToken() {
        const url = apiPaths.RefreshToken;
        return instance.post(
            url,
            { refreshToken: sessionStorage.getItem(constants.STORAGE_KEY_REFRESH_TOKEN) }
        );
    }

    getLoginMemberKey() {
        return this.getJwtValue(constants.JWT_SUB);
    }

    getLoginUserName() {
        return this.getJwtValue(constants.JWT_CLAIM_USERNAME);
    }

    getLoginUserOrganization() {
        return this.getJwtValue(constants.JWT_CLAIM_ORG);
    }

    getJwtValue(claim) {
        const token = sessionStorage.getItem(constants.STORAGE_KEY_TOKEN);
        if (token) {
            const base64Url = token.split('.')[1];
            const decoded = Buffer.from(base64Url, 'base64').toString();
            const val = JSON.parse(decoded);

            return val[claim];
        }
    }

    isAuthenticated() { return sessionStorage.getItem(constants.STORAGE_KEY_TOKEN) != undefined }


    redirectEcrf(){
        const url = apiPaths.buildPath(`/v1/security/sso/redirect`);
        return httpClient.get(url);


    }

}

export default new AuthService();