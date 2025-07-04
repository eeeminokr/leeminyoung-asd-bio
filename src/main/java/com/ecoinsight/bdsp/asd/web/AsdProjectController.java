package com.ecoinsight.bdsp.asd.web;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoinsight.bdsp.asd.service.AsdBaseApiService;
// import com.ecoinsight.bdsp.asd.repository.AsdOrganizationRepository;
import com.ecoinsight.bdsp.asd.service.AsdOrganizationService;
import com.ecoinsight.bdsp.core.Constants;
import com.ecoinsight.bdsp.core.MemberActivityConstants;
import com.ecoinsight.bdsp.core.authentication.Roles;
import com.ecoinsight.bdsp.core.entity.MemberRole;
import com.ecoinsight.bdsp.core.entity.Organization;
import com.ecoinsight.bdsp.core.entity.Project;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.entity.ProjectMemberFunction;
import com.ecoinsight.bdsp.core.model.FunctionModel;
import com.ecoinsight.bdsp.core.model.MemberPermission;
import com.ecoinsight.bdsp.core.repository.IMemberRepository;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.repository.IRoleRepository;
import com.ecoinsight.bdsp.core.service.ServiceException;
import com.ecoinsight.bdsp.core.web.BaseApiController;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;
import com.ecoinsight.bdsp.core.web.ProjectController;

import springfox.documentation.annotations.ApiIgnore;


@ApiIgnore
@RestController
@RequestMapping(path = "/api/v1/projects/asd")
public class AsdProjectController extends ProjectController  {
    
    // @Autowired
    // private IOrganizationRepository _organizationRepository;
    
    // @Autowired
    // private oRepository  _asdOrganizationRepository;

    @Autowired
    private IProjectRepository _projectRepository;

   

    @Autowired

    private IRoleRepository _roleRepository;


	@Autowired
	private IMemberRepository _memberRepository;

    @Autowired
    @Qualifier("projectBaseApiService")
	private  AsdBaseApiService _asdBaseApiService;
    @Autowired
    @Qualifier("IOrganizationRepository")
    private AsdOrganizationService asdOrganizationService;

    private final Logger LOGGER = LoggerFactory.getLogger(AsdProjectController.class);



    
   


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
            model.isQueryAllGranted() || Roles.APP_ADMIN.equals(model.getRoleId()) || com.ecoinsight.bdsp.asd.authentication.Roles.APP_SPECIFIEDVIEWER.equals(model.getRoleId()),
            model.isQueryOrgDataGranted() || com.ecoinsight.bdsp.asd.authentication.Roles.APP_MANAGER.equals(null),
            model.getRoleId()
            ).calculateAccessLevel(); // APP_Manager의 전체 소속기관의 조회가 필요없으므로 조건성립하지않게해둠.
    }


    @Override
    @PutMapping(path="/{id}/{memberkey}/functions/")
    public ResponseEntity<JsonResponseObject> changeMemberFunctionPermission(
        final @PathVariable("id") long projectId, 
        final @PathVariable("memberkey") String memberKey,         
        @RequestBody FunctionModel model) {
        final String worker = super.getAuthenticatedUsername();
        final String functionId = model.getFunctionId(); 
        final int accessLevel = calculateAccessLevel(model);

        Optional<ProjectMemberFunction> fncOptional = this._projectRepository.findFunctionByMemberKeyAndFunctionId(projectId, memberKey, functionId);
        fncOptional.ifPresentOrElse(fnc->{
            fnc.setAccessLevel(accessLevel);            
            this._projectRepository.changeFunction(fnc);
        }, ()->{
            ProjectMemberFunction function = new ProjectMemberFunction();
            function.setProjectSeq(projectId);
            function.setMemberKey(memberKey);
            function.setFunctionId(functionId);
            function.setAccessLevel(accessLevel);
            function.setDateCreated(LocalDateTime.now());
            function.setUserCreated(worker);
            function.setUsage(Constants.USAGE_USER);
    
            this._projectRepository.addFunction(function);
        });

        // 사용자 활동 로그 기록
        writeActivityLog(MemberActivityConstants.TYPE_MEMBER, MemberActivityConstants.ACT_ROLE_CHANGE, String.format("%s 사용자의 %s 기능 사용권한 변경 (과제=%s)", memberKey, functionId, projectId));
        
        return OkResponseEntity("기능의 사용권한을 변경했습니다.", model);
    }
    @Override
    @GetMapping(path="/q/")
	public ResponseEntity<JsonResponseObject> getAllProjects(
        final @RequestParam (required = false, name = "org") String orgId, 
		final @RequestParam (required = false, name = "project") String projectName) {
        final String systemId = super.getSystemId();
        final String userName = super.getAuthenticatedUsername();
        final MemberRole role = super.getHighestRole(userName);


            LOGGER.debug("userName={}",userName);
            LOGGER.debug("ProjectName={}",projectName);

        /*
         * 연구과제 목록 조회 제한
         *  1. Admin 권한은 모든 과제 목록 조회 가능
         *  2. Manager 권한은 사용자가 속한 과제만 조회 가능
         *  3. 기타 권한은 사용자가 속한 과제만 조회 가능 
         */

        List<Project> list = new ArrayList<Project>();
        if (role.getRoleId().equals("Admin") || role.getRoleId().equals("GeneralManager") || role.getRoleId().equals("Manager")) {
            list = this._projectRepository.findAll(systemId, orgId, userName, projectName);
            
        }
        // else if (super.canQueryOrg(role)) {
        //     list = this._projectRepository.findAll(systemId, StringUtils.hasText(orgId) ? orgId : super.getOrgId(), null, projectName);
        // }
        else { // Get only projejcts that an enduser joined if the member is an enduser.
            final String org = super.getOrgId();
            list = this._projectRepository.findAll(systemId, org, userName, projectName);
        }

		return OkResponseEntity("", list);
	}

    @Override
    @GetMapping(path="/{id}/organizations/")
	public ResponseEntity<JsonResponseObject> getAllOrganizations(
        @PathVariable("id") long projectId) {
        final String systemId = super.getSystemId();
        final String userName = super.getAuthenticatedUsername();
        final MemberRole role = super.getHighestRole(userName);
         final String org = super.getOrgId();
         long ProjectSet = 9999;
        var canQueryAll = false;
        var canQC = false;
        var canQueryOrg = false;
        String rolename = "";
        final Optional<ProjectMember> researcherOptional = this._projectRepository.findResearcher(projectId, userName);
        if (researcherOptional.isEmpty()) {
            rolename = super.getHighestRole(userName).getRoleId();
        }
        else {
            rolename = researcherOptional.get().getRoleId();
        } 

        if(projectId == ProjectSet){
            projectId = 0;
        }

        LOGGER.debug("rolename={}",rolename);
        LOGGER.debug("[org]projectId={}",projectId);
        int accessLevel = this._asdBaseApiService.calculateAccessLevel(rolename);
       
        LOGGER.debug("accessLevel={}",accessLevel);        
        LOGGER.debug("[org]canQueryAll(accessLevel)={}",super.canQueryAll(accessLevel));
        LOGGER.debug("[org]canQueryOrg(accessLevel)={}",super.canQueryOrg(accessLevel));
        LOGGER.debug("[org]canQC(accessLevel)={}",super.canQC(accessLevel));


    
        List<Organization> organizations = new ArrayList<Organization>();
    
            List<Project> projectList = new ArrayList<Project>();
            String projectName = null;
            LOGGER.debug("ProjectName[parser]={}",projectName);
            if (role.getRoleId().equals("Admin") || role.getRoleId().equals("GeneralManager") || role.getRoleId().equals("Manager")) { // 소속기관 9999 일때 해당연구자 소속된 과제의 소속기관 추출을 위한것
            projectList = this._projectRepository.findAll(systemId, org, userName, projectName);                                                                  // 총괄매니저일때 해당모든과제 등록한 모든 소속기관 조회
                                                                                                                                                                  // 기관매니저일때  해당 모든과제 소속기관만 조회 
            }
    
        if (super.canQueryAll(accessLevel) || super.canQC(accessLevel)) {
        // if(canQueryAll){
            // organizations = this._asdOrganizationRepository.findBySystemId(systemId);
            try {
                organizations = this.asdOrganizationService.findBySystemId(systemId); // ADMIN OR 총괄
            } catch (ServiceException e) {
                // TODO Auto-generated catch block
                  return ErrorResponseEntity(e.getLocalizedMessage());
            }
        }
        else if (super.canQueryOrg(accessLevel)) {  
            if(projectId ==0 &&projectList.size()>0){
               projectId = projectList.get(0).getId();
            }
            organizations = this._projectRepository.getAllOrganizations(systemId, projectId);
        }
        else {
                if(projectId ==0 &&projectList.size()>0){
               projectId = projectList.get(0).getId();
                }
            var list = this._projectRepository.getProjectMemberOrganization(systemId, projectId, userName); 
            if (list != null) {
                organizations = List.of(list);
            }
        }

        // Sort by organization name.
        if (organizations != null && organizations.size() > 1) organizations.sort(Comparator.comparing(Organization::getOrgName));

		return OkResponseEntity("", organizations);
	}




     @GetMapping(path="/organizations/q/")
	public ResponseEntity<JsonResponseObject> getfindAllOrganizations() {
        
         final String systemId = super.getSystemId();
         final String boardCode = "QNA";



        List<Organization> organizations = new ArrayList<Organization>();

        try {
                organizations = this.asdOrganizationService.findBySystemId(systemId);
            } catch (ServiceException e) {
               return ErrorResponseEntity(e.getLocalizedMessage());
            }


     	return OkResponseEntity("", organizations);

    }



       
  







    
}
