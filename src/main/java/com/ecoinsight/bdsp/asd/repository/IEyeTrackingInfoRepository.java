package com.ecoinsight.bdsp.asd.repository;

import java.util.List;
import java.util.Objects;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import com.ecoinsight.bdsp.asd.entity.EyeTrackingInfo;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

import io.swagger.annotations.ApiOperation;

public interface IEyeTrackingInfoRepository extends IEntityRepository {
    class EyeTrackingInfoSqlProvider implements ProviderMethodResolver {

        public String findAll(long projectSeq, String subjectId, String orgId, String gender) {

            return new SQL() {
                {
                    SELECT("T.SubjectId,T.OrgId,T.OrgName, T.ProjectSeq,P.ProjectName,T.OrgName,T.Name, T.Gender,E.RegistDt as RegistDate ,COALESCE(TIMESTAMPDIFF(MONTH, T.BirthDay , T.RegistDate) ,0) as BirthDay, COALESCE(E.TrialIndex,0) as TrialIndex ,COALESCE(E.Type , 'NO_DATA') as Type, EI.Sort as Sort");
                    FROM("T_DataSummary T");
                    LEFT_OUTER_JOIN(
                            "T_EyeTracking E ON E.SubjectId = T.SubjectId LEFT JOIN T_EyeTrackingInfo EI ON E.EyeTrackingId = EI.EyeTrackingId");
                    LEFT_OUTER_JOIN("T_Project P ON P.ProjectSeq = T.ProjectSeq");
                    WHERE("T.State <> 4");
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("T.SubjectId=" + subjectId);
                    }
                    if (projectSeq != 0) {
                        AND().WHERE("T.ProjectSeq= " + projectSeq);
                    }
                    if (!Objects.isNull(orgId)) {
                        AND().WHERE("T.OrgId= " + orgId);
                    }
                    if (StringUtils.hasText(gender)) {
                        AND().WHERE("T.Gender=  #{gender}");
                    }
                    ORDER_BY("T.CDate DESC ,EI.CDate,EI.Sort");

                }
            }.toString();

        }

        @ApiOperation("쿼리 ROW 계산으로 인한 PAGING COUNT QUERYS")
        public String getPagingCount(long projectSeq, String subjectId, String orgId, String gender, int page,
                int offset) {
            return new SQL() {
                {
                    SELECT(" T.SubjectId,T.OrgId,T.ProjectSeq,E.TrialIndex");
                    FROM("T_DataSummary T");
                    LEFT_OUTER_JOIN(
                            "T_EyeTracking E ON E.SubjectId = T.SubjectId  LEFT OUTER JOIN T_EyeTrackingInfo EI ON E.EyeTrackingId = EI.EyeTrackingId");
                    WHERE("T.State <> 4");
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("T.SubjectId=" + subjectId);
                    }
                    if (projectSeq != 0) {
                        AND().WHERE("T.ProjectSeq= " + projectSeq);
                    }
                    if (!Objects.isNull(orgId)) {
                        AND().WHERE("T.OrgId= " + orgId);
                    }
                    if (StringUtils.hasText(gender)) {
                        AND().WHERE("T.Gender=  #{gender}");
                    }
                    GROUP_BY("T.SubjectId, T.OrgId, T.ProjectSeq,E.TrialIndex");
                    LIMIT(offset);
                    OFFSET(page <= 1 ? 0 : (page - 1) * offset);
                }
            }.toString();

        }

        public String countAll(long projectSeq, String subjectId, String orgId, String gender) {
            return new SQL() {
                {
                    SELECT(" T.SubjectId,T.OrgId,T.ProjectSeq,E.TrialIndex");
                    FROM("T_DataSummary T");
                    LEFT_OUTER_JOIN(
                            "T_EyeTracking E ON E.SubjectId = T.SubjectId  LEFT OUTER JOIN T_EyeTrackingInfo EI ON E.EyeTrackingId = EI.EyeTrackingId");
                    WHERE("T.State <> 4");
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("T.SubjectId=" + subjectId);
                    }
                    if (projectSeq != 0) {
                        AND().WHERE("T.ProjectSeq= " + projectSeq);
                    }
                    if (!Objects.isNull(orgId)) {
                        AND().WHERE("T.OrgId= " + orgId);
                    }
                    if (StringUtils.hasText(gender)) {
                        AND().WHERE("T.Gender=  #{gender}");
                    }
                    GROUP_BY("T.SubjectId, T.OrgId, T.ProjectSeq,E.TrialIndex");

                }
            }.toString();

        }

    }

    @SelectProvider(EyeTrackingInfoSqlProvider.class)
    public List<EyeTrackingInfo> findAll(long projectSeq, String subjectId, String orgId, String gender, int page,
            int offset);

    @SelectProvider(EyeTrackingInfoSqlProvider.class)
    public List<EyeTrackingInfo> getPagingCount(long projectSeq, String subjectId, String orgId, String gender,
            int page, int offset);

    // @Select("SELECT COUNT(C.EyeTrackingId) as total"
    // +" FROM("
    // +" SELECT EI.EyeTrackingId EyeTrackingId ,T.SubjectId,T.OrgId, T.ProjectSeq"
    // +" FROM T_DataSummary T"
    // +" LEFT OUTER JOIN T_EyeTracking E ON E.SubjectId = T.SubjectId AND
    // E.ProjectSeq !=3"
    // +" JOIN T_EyeTrackingInfo EI ON E.EyeTrackingId = EI.EyeTrackingId WHERE
    // T.ProjectSeq !=3 group by EI.EyeTrackingId, T.SubjectId, T.OrgId,
    // T.ProjectSeq) AS C")

    @SelectProvider(EyeTrackingInfoSqlProvider.class)
    public List<EyeTrackingInfo> countAll(long projectSeq, String subjectId, String orgId, String gender);

    @Insert("INSERT INTO T_EyeTrackingInfo(EyeTrackingId, Sort, Grade, Aoi, Media, TrackInfos, CUser, CDate) VALUES(#{eyeTrackingId}, #{sort}, #{grade}, #{aoi}, #{media}, #{trackInfos}, #{userCreated}, #{dateCreated})")
    public void add(EyeTrackingInfo info);

    @Select("SELECT EyeTrackingInfoId as Id, EyeTrackingId, Sort, Grade, Aoi, Media, TrackInfos, CUser, CDate FROM T_EyeTrackingInfo "
            + " WHERE EyeTrackingId=#{eyeTrackingId}")
    public List<EyeTrackingInfo> findByEyeTrackingId(@Param("eyeTrackingId") long eyeTrackingId);

    @Delete("DELETE FROM T_EyeTrackingInfo WHERE EyeTrackingId IN ( SELECT E.EyeTrackingId FROM T_EyeTracking E "
            + " WHERE  SUBSTRING_INDEX(E.SubjectId, '_', -1) NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$' "
            + " AND SUBSTRING_INDEX(SUBSTRING_INDEX(E.SubjectId,'_',1),'_',-1) = #{subjectId} AND E.EyeTrackingId = #{eyeTrackingId} "
            + " AND E.ProjectSeq = #{projectSeq} AND E.TrialIndex = #{trialIndex} "
            + " AND  E.Type = #{type} )")
    public boolean deleteEyeTrackingInfoByEyeTrackingId(@Param("subjectId") String subjectId,
            @Param("eyeTrackingId") long eyeTrackingId,
            @Param("projectSeq") long projectSeq,
            @Param("trialIndex") int trialIndex,
            @Param("type") String type);
}
