package com.ecoinsight.bdsp.asd.web;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoinsight.bdsp.core.Constants;
import com.ecoinsight.bdsp.core.MemberActivityConstants;
import com.ecoinsight.bdsp.core.authentication.Roles;
import com.ecoinsight.bdsp.core.entity.Role;
import com.ecoinsight.bdsp.core.entity.RoleFunction;
import com.ecoinsight.bdsp.core.model.FunctionModel;
import com.ecoinsight.bdsp.core.model.MemberPermission;
import com.ecoinsight.bdsp.core.repository.IRoleRepository;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;
import com.ecoinsight.bdsp.core.web.RoleController;

import springfox.documentation.annotations.ApiIgnore;



@ApiIgnore
@RestController
@RequestMapping(path = "/api/v1/roles/asd")
public class AsdRoleController extends RoleController{
    

 private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private IRoleRepository _roleRepository;

    private static int calculateAccessLevel(FunctionModel model) {        
        /**
         * Admin, Manager 역할에 대한 특별한 권한 설정.
         * Admin : 모든 과제의 모든 데애터에 접근 가능.
         * Manager: 해당 사용자가 속한 기관의 모든 사용자가 참여하는 모든 기관의 데이터에 접근 가능.
         */
        return new MemberPermission(
            model.isCreateGranted(), 
            model.isDeleteGranted(), 
            model.isDownloadGranted(), 
            model.isQcGranted(), 
            model.isQueryGranted(),
            model.isQueryAllGranted() || Roles.APP_ADMIN.equals(model.getRoleId()),
            model.isQueryOrgDataGranted() || com.ecoinsight.bdsp.asd.authentication.Roles.APP_MANAGER.equals(null),
            model.getRoleId()).calculateAccessLevel();
    }

    @PutMapping(path="/{role}/functions/{function}/")
    public ResponseEntity<JsonResponseObject> changeFunctionPermission(
        final @PathVariable("role") String roleId, 
        final @PathVariable("function") String functionId, 
        @RequestBody FunctionModel model) {
        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();
        LOGGER.debug("modelRoleId={}",model.getRoleId());
        LOGGER.debug("isQueryGranted={}",model.isQueryGranted());
        LOGGER.debug("isQueryAllGranted={}",model.isQueryAllGranted());
        LOGGER.debug("isQueryOrgDataGranted={}",model.isQueryOrgDataGranted());


        final int accessLevel = calculateAccessLevel(model);


        LOGGER.debug("functionId={}",functionId);    
        LOGGER.debug("accessLeve={}", accessLevel);




        Optional<RoleFunction> fncOptional = this._roleRepository.findFuntionById(roleId, functionId, systemId);
        fncOptional.ifPresentOrElse(fnc->{
            fnc.setAccessLevel(accessLevel);
            fnc.setDateUpdated(LocalDateTime.now());
            fnc.setUserUpdated(worker);
            
            this._roleRepository.changeFunction(fnc);
        }, ()->{
            RoleFunction function = new RoleFunction();
            final Optional<Role> roleOptional = this._roleRepository.findById(roleId, systemId);
            function.setRoleSeq(roleOptional.get().getRoleSeq());
            function.setRoleId(roleId);
            function.setFunctionId(functionId);            
            function.setAccessLevel(accessLevel);
            function.setDateCreated(LocalDateTime.now());
            function.setUserCreated(worker);
            function.setDateUpdated(LocalDateTime.now());
            function.setUserUpdated(worker);
            function.setUsage(Constants.USAGE_USER);
    
            this._roleRepository.addFunction(function);
        });

        // 사용자 활동 로그 기록        
        writeActivityLog(MemberActivityConstants.TYPE_ROLE, MemberActivityConstants.ACT_ROLE_CHANGE, String.format("%s 권한에 %s 기능의 사용권한 변경 (Level:%s)", roleId, functionId, Integer.toBinaryString(accessLevel)));

        return OkResponseEntity("기능의 사용권한을 변경했습니다.", model);
    }

}
