package com.ecoinsight.bdsp.asd.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;
import com.ecoinsight.bdsp.asd.entity.MChart;
import com.ecoinsight.bdsp.asd.entity.MCharDataSet;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

@Mapper
public interface IMChartRepository extends IEntityRepository {

    class MchartSqlProvider implements ProviderMethodResolver {

        public String findAll(String systemId, String subjectId, long projectSeq, String orgId, String gender, int page,
                int offset) {
            // return new SQL() {
            // {
            // SELECT("D.SubjectId as subjectId, D.orgId, D.Gender as gender, D.OrgName as
            // orgName, D.BirthDay as birthDay, D.ProjectSeq,P.ProjectName, D.TrialIndex,
            // D.state, T.PatientSeq, T.RschKey, T.Result");
            // SELECT("T.RegistDt, D.CUser as userCreated, D.CDate as dateCreated,
            // DATE_FORMAT(T.CDate, '%Y-%m-%d') as mChatCreatedDate");
            // // FROM("T_DataSummary D");
            // FROM("T_MChart T");
            // INNER_JOIN(
            // SELECT("SubjectId");
            // FROM("T_DataSummary");
            // WHERE("TestYn NOT IN ('Y') AND TestYn IS NOT NULL AND State NOT IN (2, 4)");
            // // AND().WHERE()
            // // WHERE AND ProjectSeq = AND TrialIndex=1 GROUP BY SubjectId"
            // if (projectSeq != 0) {
            // AND().WHERE("D.ProjectSeq= " + projectSeq);
            // }

            // ).AS("S").ON("ON T.SubjectId = S.SubjectId");
            // LEFT_OUTER_JOIN("T_MChart T ON D.SubjectId=T.SubjectId");
            // LEFT_OUTER_JOIN("T_Project P ON D.ProjectSeq = P.ProjectSeq");
            // WHERE("1=1");
            // AND().WHERE("D.state <> 4");
            // if (!Objects.isNull(subjectId)) {
            // AND().WHERE("D.SubjectId=" + subjectId);
            // }
            // if (projectSeq != 0) {
            // AND().WHERE("D.ProjectSeq= " + projectSeq);
            // }
            // if (!Objects.isNull(orgId)) {
            // AND().WHERE("D.OrgId= " + orgId);
            // }
            // if (StringUtils.hasText(gender)) {
            // AND().WHERE("D.Gender= #{gender}");
            // }
            // ORDER_BY("T.CDate DESC");
            // LIMIT(offset);
            // OFFSET(page <= 1 ? 0 : (page - 1) * offset);
            // }
            // }.toString();
            return new SQL() {
                {
                    SELECT("D.SubjectId as subjectId, D.Gender as gender, D.OrgName as orgName, D.BirthDay as birthDay, D.ProjectSeq, P.ProjectName, D.TrialIndex, D.state, T.PatientSeq, T.RschKey, T.Result");
                    SELECT("T.RegistDt, D.CUser as userCreated, D.CDate as dateCreated, DATE_FORMAT(T.CDate, '%Y-%m-%d') as mChatCreatedDate");
                    FROM("T_DataSummary D ");
                    LEFT_OUTER_JOIN("("
                            + "SELECT SubjectId,TrialIndex,ProjectSeq "
                            + "FROM T_DataSummary "
                            + "WHERE TestYn NOT IN ('Y') "
                            + "AND TestYn IS NOT NULL "
                            + "AND State NOT IN (2, 4) "
                            + "AND TrialIndex = 1 "
                            + (projectSeq != 0 ? "AND ProjectSeq = #{projectSeq} " : "")
                            + // Parameter for projectSeq
                            "GROUP BY SubjectId,TrialIndex,ProjectSeq) AS S ON D.SubjectId = S.SubjectId  AND D.TrialIndex = S.TrialIndex AND D.ProjectSeq = S.ProjectSeq");
                    LEFT_OUTER_JOIN(
                            "T_MChart T ON T.SubjectId = D.SubjectId  AND T.TrialIndex = D.TrialIndex AND T.ProjectSeq = D.ProjectSeq AND D.TrialIndex = 1 "
                            + (projectSeq != 0 ? "AND D.ProjectSeq = #{projectSeq} " : "") // Parameter
                    // forprojectSeq
                    );
                    LEFT_OUTER_JOIN("T_Project P ON D.ProjectSeq = P.ProjectSeq");
                    WHERE("D.State NOT IN (2,4) and D.TestYn ='N' AND SUBSTRING_INDEX(D.SubjectId, '_', -1) NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$' AND D.TrialIndex = 1");
                    if (projectSeq != 0) {
                        AND().WHERE("D.ProjectSeq= " + projectSeq);
                    }
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("D.SubjectId = #{subjectId}");
                    }
                    if (!Objects.isNull(gender)) {
                        AND().WHERE("D.Gender = #{gender}");
                    }
                    ORDER_BY("T.CDate DESC");
                    LIMIT(offset);
                    OFFSET(page <= 1 ? 0 : (page - 1) * offset);
                }
            }.toString();

        }

        public String countAll(String systemId, String subjectId, long projectSeq, String orgId, String gender) {
            return new SQL() {
                {
                    SELECT("COUNT(D.SubjectId)");
                    FROM("T_DataSummary D ");
                    LEFT_OUTER_JOIN("("
                            + "SELECT SubjectId,TrialIndex,ProjectSeq "
                            + "FROM T_DataSummary "
                            + "WHERE TestYn NOT IN ('Y') "
                            + "AND TestYn IS NOT NULL "
                            + "AND State NOT IN (2, 4) "
                            + "AND TrialIndex = 1 "
                            + (projectSeq != 0 ? "AND ProjectSeq = #{projectSeq} " : "")
                            + // Parameter for projectSeq
                            "GROUP BY SubjectId,TrialIndex,ProjectSeq) AS S ON D.SubjectId = S.SubjectId  AND D.TrialIndex = S.TrialIndex AND D.ProjectSeq = S.ProjectSeq");
                    LEFT_OUTER_JOIN(
                            "T_MChart T ON T.SubjectId = D.SubjectId  AND T.TrialIndex = D.TrialIndex AND T.ProjectSeq = D.ProjectSeq AND D.TrialIndex = 1 "
                            + (projectSeq != 0 ? "AND D.ProjectSeq = #{projectSeq} " : "") // Parameter
                    // forprojectSeq
                    );
                    LEFT_OUTER_JOIN("T_Project P ON D.ProjectSeq = P.ProjectSeq");
                    WHERE("D.State NOT IN (2,4) and D.TestYn ='N' AND SUBSTRING_INDEX(D.SubjectId, '_', -1) NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$' AND D.TrialIndex = 1");
                    if (projectSeq != 0) {
                        AND().WHERE("D.ProjectSeq= " + projectSeq);
                    }
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("D.SubjectId = #{subjectId}");
                    }
                    if (!Objects.isNull(gender)) {
                        AND().WHERE("D.Gender = #{gender}");
                    }

                }
            }.toString();

        }

        public String query2(String systemId, String subjectId, long projectSeq) {
            return new SQL() {
                {
                    SELECT("MChartId as Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, PatientSeq, RschKey, Result, RegistDt, CUser, CDate");
                    FROM("T_MChart");
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

    @SelectProvider(MchartSqlProvider.class)
    public int countAll(String systemId, String subjectId, long projectSeq, String orgId, String gender);

    @SelectProvider(MchartSqlProvider.class)
    public List<MChart> findAll(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq, @Param("orgId") String orgId, @Param("gender") String gender,
            @Param("page") int page, @Param("offset") int offset);

    @Insert("INSERT INTO T_MChart(OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, PatientSeq, RschKey, Result, RegistDt, CUser, CDate) "
            + "VALUES(#{orgId}, #{systemId}, #{projectSeq}, #{subjectId}, #{trialIndex}, #{patientSeq}, #{rschKey}, #{result}, #{registDt}, #{userCreated}, #{dateCreated})")
    public void add(MChart chart);

    @Select("SELECT MChartId as Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, PatientSeq, RschKey, Result, RegistDt, CUser, CDate FROM T_MChart "
            + " WHERE SystemId=#{systemId} and SubjectId=#{subjectId} and TrialIndex=#{trialIndex} ORDER BY CDate DESC")
    public List<MChart> query(@Param("systemId") String systemId, @Param("subjectId") String subjectId, @Param("trialIndex") int trialIndex);

    @Select("SELECT MChartId as Id, OrgId, SystemId, ProjectSeq, SubjectId, TrialIndex, PatientSeq, RschKey, Result, RegistDt, CUser, CDate FROM T_MChart "
            + " WHERE SystemId=#{systemId} and SubjectId=#{subjectId} and ProjectSeq=#{projectSeq} and TrialIndex=#{trialIndex}")
    public Optional<MChart> query1(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex);

    // @Select("SELECT MChartId as Id, OrgId, SystemId, ProjectSeq, SubjectId,
    // TrialIndex, PatientSeq, RschKey, Result, RegistDt, CUser, CDate FROM T_MChart
    // "
    // + " WHERE SystemId=#{systemId} and SubjectId=#{subjectId} and
    // ProjectSeq=#{projectSeq}")
    // public List<MChart> query2(@Param("systemId") String systemId,
    // @Param("subjectId") String subjectId,
    // @Param("projectSeq") long projectSeq);
    @SelectProvider(MchartSqlProvider.class)
    public List<MChart> query2(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq);

    @Select("SELECT M.* FROM T_MChart M "
            + " INNER JOIN (SELECT SubjectId  FROM T_DataSummary WHERE TestYn NOT IN ('Y') AND TestYn IS NOT NULL AND State NOT IN (2, 4) AND ProjectSeq = #{projectSeq} "
            + " GROUP BY  SubjectId) AS S ON M.SubjectId = S.SubjectId "
            + " LEFT JOIN T_DataSummary D ON M.SubjectId = D.SubjectId AND D.TrialIndex = M.TrialIndex AND D.ProjectSeq = #{projectSeq}"
            + " WHERE SUBSTRING_INDEX(M.SubjectId, '_', -1)NOT REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$' "
            + " AND M.ProjectSeq = #{projectSeq} ORDER BY M.SubjectId, M.MChartId, M.TrialIndex ")
    public List<MCharDataSet> findAllByProjectSeq(@Param("projectSeq") long projectSeq);

    @Update("UPDATE T_MChart SET ProjectSeq=#{projectSeq} WHERE MChartId=#{id}")
    public void updateProjectSeq(@Param("id") long id, @Param("projectSeq") long projectSeq);
}
