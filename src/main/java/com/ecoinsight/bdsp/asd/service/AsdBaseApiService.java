package com.ecoinsight.bdsp.asd.service;

import com.ecoinsight.bdsp.asd.authentication.Roles;
import com.ecoinsight.bdsp.core.model.MemberPermission;

public class AsdBaseApiService {
  
    
    /**
     * @return
     */
    public int calculateAccessLevel(String rolename){

     int accessLevel = 0;
        switch (rolename) {
            case Roles.APP_ADMIN:
                accessLevel += MemberPermission.QUERY_ALL;
                break;
            case Roles.APP_MANAGER:  // General Manager // 총괄 매니저 // 본 책임연구원 
                accessLevel += MemberPermission.QUERY_ALL_ORG; 
                break;
            case Roles.USER_ENDUSER: // Manager // 기관매니저 // 본
                accessLevel += MemberPermission.QUERY;
                break;    
            case Roles.USER_QC_ENDUSER:
                accessLevel += MemberPermission.QUERY;
                break;
           case Roles.APP_SPECIFIEDVIEWER:
                accessLevel += MemberPermission.QUERY_ALL;
                break;
        }

        return accessLevel;

    } 
       


}
