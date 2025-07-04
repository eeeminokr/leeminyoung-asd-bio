package com.ecoinsight.bdsp.asd.repository;

import java.util.List;
import java.util.Objects;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import com.ecoinsight.bdsp.asd.model.FnirsResource;
import com.ecoinsight.bdsp.asd.model.Pupillometry;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

public interface IPupillometryRepository extends IEntityRepository {

    class PupillometrySqlProvider implements ProviderMethodResolver {

        public String findAll(long projectSeq, int trialIndex, String subjectId, String orgId, String gender,
                int page, int offset) {

            return new SQL() {
                { // CASE WHEN PP.Completed = 0 THEN 'NO_DATA' ELSE CAST(PP.Completed AS CHAR) END
                    // AS StatusCompleted
                    SELECT("T.SubjectId, T.OrgId, T.OrgName, T.ProjectSeq, P.ProjectName , T.OrgName, T.Name, T.Gender, PP.CDate as dateCreated, T.Registdate as Registdate, T.Registdate as Registdate, COALESCE(TIMESTAMPDIFF(MONTH, T.BirthDay, T.RegistDate) ,0) AS TrialBirthDay, COALESCE(TIMESTAMPDIFF(MONTH, T.BirthDay, T.RegistDate) ,0) as BirthDay,COALESCE(PP.TrialIndex,0) as TrialIndex, PP.Completed");
                    FROM("T_DataSummary T");
                    LEFT_OUTER_JOIN("T_Pupillometry PP ON PP.SubjectId = T.SubjectId AND PP.TrialIndex = T.TrialIndex AND PP.ProjectSeq = T.ProjectSeq");
                    LEFT_OUTER_JOIN("T_Project P ON P.ProjectSeq = T.ProjectSeq");
                    WHERE("T.State NOT IN (2, 4) AND T.TestYn ='N'");
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("T.SubjectId=" + subjectId);
                    }
                    if (projectSeq != 0) {
                        AND().WHERE("T.ProjectSeq= " + projectSeq);
                    }
                    if (trialIndex != 0) {
                        AND().WHERE("T.TrialIndex= " + trialIndex);
                    }
                    if (!Objects.isNull(orgId)) {
                        AND().WHERE("T.OrgId= " + orgId);
                    }
                    if (StringUtils.hasText(gender)) {
                        AND().WHERE("T.Gender=  #{gender}");
                    }
                    ORDER_BY("T.CDate DESC ,T.SubjectId");
                    LIMIT(offset);
                    OFFSET(page <= 1 ? 0 : (page - 1) * offset);
                }
            }.toString();

        }

        public String countAll(long projectSeq, int trialIndex, String subjectId, String orgId, String gender,
                int page, int offset) {

            return new SQL() {
                {
                    SELECT("T.SubjectId, T.OrgId, T.OrgName, T.ProjectSeq, P.ProjectName , T.OrgName, T.Name, T.Gender, PP.CDate as dateCreated, T.Registdate as Registdate, T.Registdate as Registdate, COALESCE(TIMESTAMPDIFF(MONTH, T.BirthDay, T.RegistDate) ,0) AS TrialBirthDay, COALESCE(TIMESTAMPDIFF(MONTH, T.BirthDay, T.RegistDate) ,0) as BirthDay,COALESCE(PP.TrialIndex,0) as TrialIndex, PP.Completed");
                    FROM("T_DataSummary T");
                    LEFT_OUTER_JOIN("T_Pupillometry PP ON PP.SubjectId = T.SubjectId AND PP.TrialIndex = T.TrialIndex AND PP.ProjectSeq = T.ProjectSeq");
                    LEFT_OUTER_JOIN("T_Project P ON P.ProjectSeq = T.ProjectSeq");
                    WHERE("T.State NOT IN (2, 4) AND T.TestYn ='N'");
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("T.SubjectId=" + subjectId);
                    }
                    if (projectSeq != 0) {
                        AND().WHERE("T.ProjectSeq= " + projectSeq);
                    }
                    if (trialIndex != 0) {
                        AND().WHERE("T.TrialIndex= " + trialIndex);
                    }
                    if (!Objects.isNull(orgId)) {
                        AND().WHERE("T.OrgId= " + orgId);
                    }
                    if (StringUtils.hasText(gender)) {
                        AND().WHERE("T.Gender=  #{gender}");
                    }
                    // ORDER_BY("T.CDate DESC ,T.SubjectId");
                    // LIMIT(offset);
                    // OFFSET(page <= 1 ? 0 : (page - 1) * offset);
                }
            }.toString();
        }

    }

    @Select("SELECT Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, OriginalFileName, TargetFileName, Published, PublishedError, Completed, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, CPDate as DateCompleted "
            + " FROM T_Pupillometry "
            + " WHERE Deleted=false and Published = true and  Completed = true and SystemId = #{systemId} and SubjectId = #{subjectId} and ProjectSeq=#{projectSeq} and TrialIndex=#{trialIndex}")
    public List<Pupillometry> queryPupillometryResources(@Param("systemId") String systemId,
            @Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq,
            @Param("trialIndex") int trialIndex);

    @Select("SELECT Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, OriginalFileName, TargetFileName, Published, PublishedError,Completed, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated"
            + " FROM T_Pupillometry "
            + " WHERE Failed=false and Deleted=false and Hostname = #{hostname} and Published = #{published}")
    public List<Pupillometry> queryByPublished(@Param("hostname") String hostname,
            @Param("published") boolean published);

    @SelectProvider(PupillometrySqlProvider.class)
    public List<Pupillometry> findAll(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex,
            @Param("orgId") String orgId,
            @Param("gender") String gender, @Param("page") int page,
            @Param("offset") int offset);

    @SelectProvider(PupillometrySqlProvider.class)
    public List<Pupillometry> countAll(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex,
            @Param("orgId") String orgId,
            @Param("gender") String gender, @Param("page") int page,
            @Param("offset") int offset);

    @Select("SELECT Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, OriginalFileName, TargetFileName, Published, PublishedError, Completed, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, CPDate as DateCompleted "
            + " FROM T_Pupillometry "
            + " WHERE Deleted=false and SystemId = #{systemId} and SubjectId = #{subjectId} and ProjectSeq=#{projectSeq} and TrialIndex=#{trialIndex}")
    public List<Pupillometry> findByProjectSeqAndTrialIndex(@Param("systemId") String systemId,
            @Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq,
            @Param("trialIndex") int trialIndex);
    
    @Select("SELECT Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, OriginalFileName, TargetFileName, Published, PublishedError, Completed, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, CPDate as DateCompleted "
            + " FROM T_Pupillometry "
            + " WHERE Deleted=false and SystemId = #{systemId} and SubjectId = #{subjectId}")
    public List<Pupillometry> findByProjectIdAndTrialIndex(@Param("systemId") String systemId, @Param("subjectId") String subjectId);

    @Insert("INSERT INTO T_Pupillometry (Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, OriginalFileName, TargetFileName, Hostname, Published, Completed, Deleted, CDate, CUser, UDate, UUser)"
            + " VALUES(#{id}, #{orgId}, #{systemId}, #{projectSeq}, #{subjectId}, #{trialIndex}, #{originalFileName}, #{targetFileName}, #{hostname}, #{published}, #{completed}, #{deleted}, #{dateCreated}, #{userCreated}, #{dateUpdated}, #{userUpdated})")
    public void add(Pupillometry pupillometry);

    @Update("UPDATE T_Pupillometry "
            + " SET Failed=#{failed}, Published=#{published}, Completed=#{completed}, PublishedError=#{publishedError}, PDate=#{datePublished} , CPDate=#{dateCompleted} "
            + " WHERE Id=#{id}")
    public boolean changeAfterPublishedAndLabelled(Pupillometry pupillometry);

    @Update("UPDATE T_Pupillometry "
            + " SET  ProjectSeq = #{projectSeq}, UDate=#{dateUpdated} "
            + " WHERE Id=#{id}")
    public boolean updateSubjectProjectSeq(Pupillometry pupillometry);

    @Delete("DELETE FROM T_Pupillometry E "
            + " WHERE  SUBSTRING_INDEX(E.SubjectId, '_', -1) NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$' "
            + " AND SUBSTRING_INDEX(SUBSTRING_INDEX(E.SubjectId,'_',1),'_',-1) = #{subjectId}  and E.Completed = true"
            + " AND E.ProjectSeq = #{projectSeq} AND E.TrialIndex = #{trialIndex} ")
    public boolean delete(@Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq,
            @Param("trialIndex") int trialIndex);

}
