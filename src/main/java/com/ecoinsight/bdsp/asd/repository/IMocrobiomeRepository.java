package com.ecoinsight.bdsp.asd.repository;

import java.util.List;
import java.util.Objects;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import com.ecoinsight.bdsp.asd.entity.Microbiome;
import com.ecoinsight.bdsp.asd.model.FnirsResource;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

public interface IMocrobiomeRepository extends IEntityRepository {

    class MocrobiomeSqlProvider implements ProviderMethodResolver {

        public String findAll(String systemId, String subjectId, long projectSeq, int trialIndex, String orgId, String gender,
                int page, int offset) {

            return new SQL() {
                { // CASE WHEN PP.Completed = 0 THEN 'NO_DATA' ELSE CAST(PP.Completed AS CHAR) END
                    // AS StatusCompleted
                    SELECT("T.SubjectId, T.OrgId, T.OrgName, T.ProjectSeq, P.ProjectName , T.OrgName, T.Name, T.Gender, M.CDate as dateCreated, T.Registdate as Registdate, T.Registdate as Registdate, COALESCE(TIMESTAMPDIFF(MONTH, T.BirthDay, T.RegistDate) ,0) AS TrialBirthDay, COALESCE(TIMESTAMPDIFF(MONTH, T.BirthDay, T.RegistDate) ,0) as BirthDay, COALESCE(M.TrialIndex,0) as TrialIndex, M.Completed");
                    FROM("T_DataSummary T");
                    LEFT_OUTER_JOIN("T_Microbiome M ON M.SubjectId = T.SubjectId AND M.TrialIndex = T.TrialIndex AND M.ProjectSeq = T.ProjectSeq");
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

        public String countAll(String systemId, String subjectId, long projectSeq, int trialIndex, String orgId, String gender) {

            return new SQL() {
                {
                    SELECT("COUNT(T.SubjectId)");
                    FROM("T_DataSummary T");
                    LEFT_OUTER_JOIN("T_Microbiome M ON M.SubjectId = T.SubjectId AND M.TrialIndex = T.TrialIndex AND M.ProjectSeq = T.ProjectSeq");
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

    @SelectProvider(MocrobiomeSqlProvider.class)
    public List<Microbiome> findAll(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex,
            @Param("orgId") String orgId,
            @Param("gender") String gender, @Param("page") int page,
            @Param("offset") int offset);

    @SelectProvider(MocrobiomeSqlProvider.class)
    public int countAll(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex,
            @Param("orgId") String orgId,
            @Param("gender") String gender);

    @Select("SELECT Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, OriginalFileName, TargetFileName, Published, PublishedError, Completed, Deleted, CDate as DateCreated, CUser as UserCreated, UDate as DateUpdated, UUser as UserUpdated, PDate as DatePublished, CPDate as DateCompleted "
            + " FROM T_Microbiome "
            + " WHERE Deleted=false and Published = true and  Completed = true and SystemId = #{systemId} and SubjectId = #{subjectId} and ProjectSeq=#{projectSeq} and TrialIndex=#{trialIndex}")
    public List<Microbiome> queryMicrobiomeResources(@Param("systemId") String systemId, @Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex);

    @Insert("INSERT INTO T_Microbiome (Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, OriginalFileName, TargetFileName, Hostname, Published, Completed, Deleted, CDate, CUser, UDate, UUser)"
                        + " VALUES(#{id}, #{orgId}, #{systemId}, #{projectSeq}, #{subjectId}, #{trialIndex}, #{originalFileName}, #{targetFileName}, #{hostname}, #{published}, #{completed}, #{deleted}, #{dateCreated}, #{userCreated}, #{dateUpdated}, #{userUpdated})")
    @Options(useGeneratedKeys = true, keyColumn = "ID", keyProperty = "id")
    public void add(Microbiome microbiome);

    @Update("UPDATE T_Microbiome "
            + " SET Failed=#{failed}, Published=#{published}, Completed=#{completed}, PublishedError=#{publishedError}, PDate=#{datePublished} , CPDate=#{dateCompleted} "
            + " WHERE Id=#{id}")
    public boolean changeAfterPublishedAndLabelled(Microbiome microbiome);

    @Update("UPDATE T_Microbiome "
            + " SET  ProjectSeq = #{projectSeq}"
            + " WHERE Id=#{id}")
    public boolean updateSubjectProjectSeq(Microbiome microbiome);

}
