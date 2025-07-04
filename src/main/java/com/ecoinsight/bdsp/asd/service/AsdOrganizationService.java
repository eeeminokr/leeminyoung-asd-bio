package com.ecoinsight.bdsp.asd.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;

import com.ecoinsight.bdsp.asd.entity.IMember;
import com.ecoinsight.bdsp.asd.repository.AsdOrganizationRepository;
import com.ecoinsight.bdsp.core.entity.Organization;
import com.ecoinsight.bdsp.core.service.ServiceException;

@Primary
@Qualifier("IOrganizationRepository")
public class AsdOrganizationService  {
    
@Autowired
private AsdOrganizationRepository asdOrganizationRepository;

   public List<Organization> findBySystemId(final String systemId) throws ServiceException{
    List<Organization> organizations = new ArrayList<Organization>();
     organizations =  this.asdOrganizationRepository.findBySystemId(systemId);

        return organizations;
   }  


   public List<IMember> getMembersbyOrgId(final String systemId, final String orgId) throws ServiceException {

         List<IMember> members = new ArrayList<IMember>();
         members = this.asdOrganizationRepository.getMembersbyOrgId(systemId, orgId);

         return members;
   }


}
