package com.ecoinsight.bdsp.asd.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecoinsight.bdsp.asd.authentication.Roles;
import com.ecoinsight.bdsp.asd.repository.IAsdProjectSubjectRepository;
import com.ecoinsight.bdsp.asd.service.AsdBaseApiService;
import com.ecoinsight.bdsp.core.entity.MemberRole;
import com.ecoinsight.bdsp.core.model.MemberPermission;
import com.ecoinsight.bdsp.core.web.BaseApiController;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

public class AsdBaseApiController extends BaseApiController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    protected IAsdProjectSubjectRepository _subjectRepository;
    
    @Autowired
    protected AsdBaseApiService _asdBaseApiService;


    
    protected static  ResponseEntity<JsonResponseObject> OkResponseEntity(java.lang.String message) {
        return ResponseEntity.ok(new JsonResponseObject(true, message));
    }

    protected ResponseEntity<JsonResponseObject> ErrorResponse(String message) {
        LOGGER.error(message);
        return ResponseEntity.ok(new JsonResponseObject(false, message));
    }

    protected ResponseEntity<JsonResponseObject> ErrorResponse(String message, Exception e) {
        LOGGER.error(message, e);
        return ResponseEntity.ok(new JsonResponseObject(false, message, e));
    }


      /**
     * 권한명으로 사용권한을 계산한다.
     * <ul>
     * <li>총괄 관리자(Roles.APP_ADMIN): MemberPermission.QUERY_ALL</li>
     * <li>책임 연구원(Roles.APP_MANAGER): MemberPermission.QUERY // ASD 책임연구원은 본인기관 소속 데이터만 조회하는 접근기능을 가지게한다.
     * <li>QC 검수자(Roles.USER_QC_ENDUSER): MemberPermission.QC</li>
     * <li>기타: MemberPermission.QUERY</li>
     * </ul>
     * @param rolename
     * @return
     */
    // protected int calculateAccessLevel(String rolename) {
    //     int accessLevel = 0;
    //     switch (rolename) {
    //         case Roles.APP_ADMIN:
    //             accessLevel += MemberPermission.QUERY_ALL;
    //             break;
    //         case Roles.APP_MANAGER:
    //             accessLevel += MemberPermission.QUERY;
    //             break;
    //         case Roles.USER_QC_ENDUSER:
    //             accessLevel += MemberPermission.QC;
    //             break;
    //         default:
    //             accessLevel += MemberPermission.QUERY;
    //             break;
    //     }

    //     return accessLevel;
    // }





    @Override
    protected int calculateAccessLevel(String rolename) {
        int accessLevel = this._asdBaseApiService.calculateAccessLevel(rolename);

  
        

        return accessLevel;
    }


    

    /**
    * 사용자 권한을 확인하여 모든 프로젝트의 데이터를 조회할수 있는지 판단.
     * <p>MemberRole.getRoleLevel()값이 Roles.APP_ADMIN_LEVEL (T_Role.Level=1) 이하일 경우 true. </p>
     * @param role
     * @return
     */
    protected boolean canQueryAll(final MemberRole role) {
        return role == null ? false : role.getRoleLevel() <= Roles.APP_ADMIN_LEVEL;
    }

    /**
     * 사용자 권한을 확인하여 연구기관에 속한 사용자가 참여하는 모든 프로젝트의 데이터를 조회할수 있는지 판단.
     * <p>MemberRole.getRoleLevel()값이 Roles.APP_MANAGER_LEVEL (T_Role.Level = 10) 이하일 경우 true. </p>
     * @param role
     * @return
     */
    protected boolean canQueryOrg(final MemberRole role) {
        return role == null ? false : role.getRoleLevel() <= Roles.APP_MANAGER_LEVEL;
    }


     /**
    * 사용자 권한을 확인하여 모든 프로젝트의 데이터를 조회할수 있는지 판단.
     * <p>MemberRole.getRoleLevel()값이 Roles.APP_ADMIN_LEVEL (T_Role.Level=1) 이하일 경우 true. </p>
     * @param role
     * @return
     
    protected boolean canQC(final MemberRole role) {
        return role == null ? false : role.getRoleLevel() == Roles.USER_QC_ENDUSER_LEVEL;
    }
    */


}
