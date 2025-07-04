package com.ecoinsight.bdsp.asd.repository;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import com.ecoinsight.bdsp.asd.entity.IMember;
import com.ecoinsight.bdsp.core.entity.Member;
import com.ecoinsight.bdsp.core.entity.Organization;
import com.ecoinsight.bdsp.core.repository.IOrganizationRepository;

public interface AsdOrganizationRepository extends IOrganizationRepository {
  class AsdOrganizationSqlProvider implements ProviderMethodResolver {
    //

    public static String findBySystemId(@Param("systemId") String systemId) {
      return new SQL() {
        {
          SELECT(
              "CAST(OrgId AS signed int) as OrgId, SystemId, OrgName, Description, RStatus RecordStatus, CUser UserCreated, CDate DateCreated, UUser UserUpdated, UDate DateUpdated, `_Usage` `Usage`, `_Version` `Version`");
          FROM("T_Organization");
          WHERE("RStatus='ACTIVE' and _Usage='USER' and OrgId > 0");
          if (StringUtils.hasText(systemId)) {
            AND().WHERE("systemId=#{systemId}");
          }
          ORDER_BY("OrgId");

        }
      }.toString();
    }

    public static String getMembersbyOrgId(@Param("systemId") final String systemId,
        @Param("orgId") final String orgId) {
      return new SQL() {
        {
          SELECT("distinct m.Email,m.MemberName,r.Description as RoleName");
          FROM("T_ProjectMember pm");
          INNER_JOIN(
              "T_Member m inner join T_Organization o on (o.SystemId=m.SystemId and o.RStatus='ACTIVE' ) on (m.MemberKey = pm.MemberKey and (pm.RoleSeq =3 or pm.RoleSeq = 4) and m.RStatus='ACTIVE')");
          LEFT_OUTER_JOIN("T_Role r on r.RoleSeq = pm.RoleSeq");
          WHERE("o.SystemId=#{systemId}");
          if (StringUtils.hasText(orgId)) {
            AND().WHERE("o.OrgId = m.OrgId and o.OrgId=#{orgId}");
          }
          AND().WHERE("NOT m.OrgId is NULL and NOT m.Email is null");

        }
      }.toString();
    }

  }

  /**
   * //
   * 시스템에 속한 모든 기관을 반환한다. (RStatus='ACTIVE' and _Usage='USER')
   * 
   * @param systemId
   * @return
   */

  @SelectProvider(AsdOrganizationSqlProvider.class)
  // @Select("SELECT CAST(OrgId AS signed int) OrgId, SystemId, OrgName,
  // Description, RStatus RecordStatus, CUser UserCreated, CDate DateCreated,
  // UUser UserUpdated, UDate DateUpdated, `_Usage` `Usage`, `_Version` `Version`
  // FROM T_Organization where SystemId=#{systemId} and RStatus='ACTIVE' and
  // _Usage='USER'")
  @Override
  public List<Organization> findBySystemId(@Param("systemId") String systemId);

  /**
   * 
   */
  @SelectProvider(AsdOrganizationSqlProvider.class)
  public List<IMember> getMembersbyOrgId(@Param("systemId") final String systemId, @Param("orgId") final String orgId);

}
