package com.ecoinsight.bdsp.asd.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;
import com.ecoinsight.bdsp.asd.entity.EyeTracking;
import com.ecoinsight.bdsp.asd.entity.EyeTrackingDataSet;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

@Mapper
public interface IEyeTrackingRepository extends IEntityRepository {

    class IEyeTrackingSqlProvider implements ProviderMethodResolver {

        public String query2(String systemId, String subjectId, long projectSeq) {

            return new SQL() {
                {
                    SELECT("EyeTrackingId as Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, PatientSeq, SetKey, Type, MeasureTime, RegistDt, DeviceHeight, DeviceWidth, CUser, CDate");
                    FROM("T_EyeTracking");
                    WHERE("1=1");
                    if (StringUtils.hasText(systemId)) {
                        AND().WHERE("SystemId= #{systemId}");
                    }
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("SubjectId=" + subjectId);
                    }
                    if (projectSeq != 0) {
                        AND().WHERE("ProjectSeq= " + projectSeq);
                    }
                }
            }.toString();

        }

    }

    // phonemodel, oseVersion, appVersion 추가
    @Insert("INSERT INTO T_EyeTracking(OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, PatientSeq, SetKey, Type, MeasureTime, RegistDt, DeviceHeight, DeviceWidth, FailCount, AppVersion,PhoneModel,OsVersion, CUser, CDate) "
            + "VALUES(#{orgId}, #{systemId}, #{projectSeq}, #{subjectId}, #{trialIndex}, #{patientSeq}, #{setKey}, #{type}, #{measureTime}, #{registDt}, #{deviceHeight}, #{deviceWidth}, #{failCount}, #{appVersion},#{phoneModel},#{osVersion}, #{userCreated}, #{dateCreated})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    public void add(EyeTracking tracking);

    @Select("SELECT EyeTrackingId as Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, PatientSeq, SetKey, Type, MeasureTime, RegistDt, DeviceHeight, DeviceWidth, CUser, CDate FROM T_EyeTracking "
            + " WHERE SystemId=#{systemId} and SubjectId=#{subjectId} and Type=#{type} and ProjectSeq=#{projectSeq} and TrialIndex=#{trialIndex}")
    public Optional<EyeTracking> query1(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("type") String type, @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex);

    // @Select("SELECT EyeTrackingId FROM T_EyeTracking WHERE subjectId
    // =#{subjectId} AND projectSeq = #{projectSeq} AND systemId = #{systemId} AND
    // orgId = #{orgId}")
    // public int getEyeTrackingId(EyeTracking tracking);

    // @Select("SELECT EyeTrackingId as Id, OrgId, SystemId, ProjectSeq, SubjectId,
    // TrialIndex, PatientSeq, SetKey, Type, MeasureTime, RegistDt, DeviceHeight,
    // DeviceWidth, CUser, CDate FROM T_EyeTracking "
    // +" WHERE SystemId=#{systemId} and SubjectId=#{subjectId} and
    // ProjectSeq=#{projectSeq}")
    // public List<EyeTracking> query2(@Param("systemId") String systemId,
    // @Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq);

    @SelectProvider(IEyeTrackingSqlProvider.class)
    public List<EyeTracking> query2(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq);

    @Update("UPDATE T_EyeTracking SET ProjectSeq=#{projectSeq} WHERE EyeTrackingId=#{id}")
    public void updateProjectSeq(@Param("id") long id, @Param("projectSeq") long projectSeq);

    @Select("SELECT E.EyeTrackingId AS EyeTrackingId, EI.EyeTrackingInfoId AS EyeTrackingInfoId, E.SubjectId, E.ProjectSeq, E.TrialIndex, E.PatientSeq, EI.Sort, E.Type, E.MeasureTime, E.RegistDt "
            + ",E.DeviceWidth, E.DeviceHeight, E.SetKey, EI.Grade, EI.Aoi, EI.Media ,EI.TrackInfos, E.CUser AS UserCreated, E.CDate AS DateCreated, EI.CDate AS EyeInfoDateCreated "
            + " FROM T_EyeTracking E "
            + " INNER JOIN T_EyeTrackingInfo EI ON E.EyeTrackingId = EI.EyeTrackingId "
            + " INNER JOIN (SELECT SubjectId  FROM T_DataSummary WHERE TestYn NOT IN ('Y') AND TestYn IS NOT NULL AND State NOT IN (2, 4) AND ProjectSeq = #{projectSeq} "
            + " GROUP BY  SubjectId) AS S ON E.SubjectId = S.SubjectId "
            + " WHERE SUBSTRING_INDEX(E.SubjectId, '_', -1) NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$' AND  E.ProjectSeq = #{projectSeq}"
            + " GROUP BY EI.EyeTrackingInfoId ORDER BY E.TrialIndex")
    public List<EyeTrackingDataSet> findAllByProjectSeq(@Param("projectSeq") long projectSeq);

    @Delete("DELETE FROM T_EyeTracking E "
            + " WHERE SUBSTRING_INDEX(E.SubjectId, '_', -1) NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$' "
            + " AND  SUBSTRING_INDEX(SUBSTRING_INDEX(E.SubjectId,'_',1),'_',-1) = #{subjectId} AND E.EyeTrackingId = #{eyeTrackingId} "
            + " AND E.ProjectSeq = #{projectSeq} AND E.TrialIndex = #{trialIndex} "
            + " AND E.Type = #{type}")
    public boolean deleteEyeTrackingBySubjectId(@Param("subjectId") String subjectId,
            @Param("eyeTrackingId") long eyeTrackingId,
            @Param("projectSeq") long projectSeq,
            @Param("trialIndex") int trialIndex,
            @Param("type") String type);

}
