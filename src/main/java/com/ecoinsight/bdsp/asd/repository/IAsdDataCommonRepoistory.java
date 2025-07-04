package com.ecoinsight.bdsp.asd.repository;

import com.ecoinsight.bdsp.asd.entity.DashBoard;
import com.ecoinsight.bdsp.asd.entity.DataSummary;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.util.StringUtils;

public interface IAsdDataCommonRepoistory extends IEntityRepository {

    class DataCommonSqlProvider implements ProviderMethodResolver {

        public String updateDataBySubjectId(final String table, final AsdProjectSubject subject, final String status) {

            if (status == "DEL") {
                return new SQL() {
                    {
                        UPDATE(table);
                        if (org.apache.commons.lang.StringUtils.equals(table, "T_VideoResource")) {
                            SET("SubjectId = CONCAT(" + "'" + subject.getSubjectId() + "'," + "'_'"
                                    + ",now()), Deleted = 1");
                        } else {
                            SET("SubjectId = CONCAT(" + "'" + subject.getSubjectId() + "'," + "'_'" + ",now())");
                        }
                        WHERE("SubjectId=" + "'" + subject.getSubjectId() + "'");
                    }
                }.toString();
            } else {
                return new SQL() {
                    {
                        UPDATE(table);
                        SET("ProjectSeq = " + subject.getProjectSeq());
                        WHERE("SubjectId=" + "'" + subject.getSubjectId() + "'");
                    }
                }.toString();
            }
        }

        public String findAll(String subjectId, long projectSeq, String orgId, String gender, int page, int offset) {
            return new SQL() {
                {
                    SELECT("T.Id, T.SubjectId, T.Name, T.Gender, T.BirthDay, T.RegistDate as registDate, T.TrialIndex, T.ProjectSeq,P.ProjectName, T.OrgId, T.OrgName");
                    SELECT("T.FirstSelection, T.SecondSelection, T.Mchat, T.Eyetracking, T.VideoResource");
                    SELECT("T.AudioResource,T.PUPILLOMETRY, T.Blood, T.Stool, T.Urine, T.FNIRS, T.EEG, T.MRI, T.CUser, T.CDate, T.UUser, T.UDate");
                    SELECT("CASE WHEN T.FirstSelection=true AND T.SecondSelection=true AND T.Mchat=true AND T.Eyetracking=true AND T.VideoResource=true AND T.VitalSigns=true AND "
                            + "T.AudioResource=true AND T.Blood=true AND T.Stool=true AND T.Urine=true AND T.FNIRS=true AND T.EEG=true AND T.MRI=true THEN 1 ELSE 0 END as `all`");

                    FROM("T_DataSummary T");
                    LEFT_OUTER_JOIN("T_Project P ON T.ProjectSeq = P.ProjectSeq");
                    WHERE("1=1");
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
                    ORDER_BY("T.CDate DESC");
                    LIMIT(offset);
                    OFFSET(page <= 1 ? 0 : (page - 1) * offset);

                }
            }.toString();

        }

        public String countAll(String subjectId, long projectSeq, String orgId, String gender) {
            return new SQL() {
                {
                    SELECT("COUNT(T.Id)");
                    // SELECT("T.Id, T.SubjectId, T.Name, T.Gender, T.BirthDay, T.TrialIndex,
                    // T.ProjectSeq, T.OrgId, T.OrgName");
                    // SELECT("T.FirstSelection, T.SecondSelection, T.Mchat, T.Eyetracking,
                    // T.VideoResource, T.VitalSigns");
                    // SELECT("T.AudioResource, T.Blood, T.Stool, T.Urine, T.FNIRS, T.EEG, T.MRI,
                    // T.CUser, T.CDate, T.UUser, T.UDate");
                    FROM("T_DataSummary T");
                    LEFT_OUTER_JOIN("T_Project P ON T.ProjectSeq = P.ProjectSeq");
                    WHERE("1=1");
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
                    // ORDER_BY("T.UDate DESC");
                    // LIMIT(offset);
                    // OFFSET(page <= 1 ? 0 : (page - 1) * offset);

                }
            }.toString();

        }

        public String findDataSummarybyOrgId(long projectSeq) {
            return new SQL() {
                {
                    SELECT("s.OrgId ,Count(*) AS Count , o.OrgName");
                    if (projectSeq != 0) {
                        SELECT("s.ProjectSeq");
                    }
                    FROM("T_DataSummary s");
                    JOIN("T_Organization o ON s.OrgId = o.OrgId");
                    WHERE("TrialIndex = 1 AND TestYn ='N'");
                    AND().WHERE("s.SubjectId NOT LIKE '%%%230101%%'");
                    if (projectSeq != 0) {
                        AND().WHERE("s.ProjectSeq = " + projectSeq);
                    }
                    GROUP_BY("s.OrgId, o.OrgName");
                    if (projectSeq != 0) {
                        GROUP_BY("s.projectSeq");
                    }
                    ORDER_BY("s.OrgId");
                    if (projectSeq != 0) {
                        ORDER_BY("s.ProjectSeq");
                    }

                }
            }.toString();
        }

        public String findListForUpdateTrialIndex2(int trialIndex) {
            return new SQL() {
                // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
                // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 6 MONTH) AND State <>4 and
                // ProjectSeq != 1;")
                {
                    SELECT("*");
                    FROM("T_DataSummary");
                    WHERE("STR_TO_DATE(RegistDate, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 6 MONTH)");
                    AND().WHERE("State <>4");
                    AND().WHERE("ProjectSeq != 1");
                    if (trialIndex != 0) {
                        AND().WHERE("TrialIndex =" + trialIndex);
                    }

                }

            }.toString();

        }

        public String findListForUpdateTrialIndex3(int trialIndex) {
            return new SQL() {
                // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
                // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 6 MONTH) AND State <>4 and
                // ProjectSeq != 1;")
                {
                    SELECT("*");
                    FROM("T_DataSummary");
                    WHERE("STR_TO_DATE(RegistDate, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 12 MONTH)");
                    AND().WHERE("State <>4");
                    AND().WHERE("ProjectSeq != 1");
                    if (trialIndex != 0) {
                        AND().WHERE("TrialIndex =" + trialIndex);
                    }

                }

            }.toString();

        }

        public String findListForUpdateTrialIndex4(int trialIndex) {
            return new SQL() {
                // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
                // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 6 MONTH) AND State <>4 and
                // ProjectSeq != 1;")
                {
                    SELECT("*");
                    FROM("T_DataSummary");
                    WHERE("STR_TO_DATE(RegistDate, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 18 MONTH)");
                    AND().WHERE("State <>4");
                    AND().WHERE("ProjectSeq != 1");
                    if (trialIndex != 0) {
                        AND().WHERE("TrialIndex =" + trialIndex);
                    }

                }

            }.toString();

        }

        public String findListForUpdateTrialIndexName2(String subjectId, int trialIndex) {
            return new SQL() {
                // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
                // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 6 MONTH) AND State <>4 and
                // ProjectSeq != 1;")
                {
                    SELECT("*");
                    FROM("T_DataSummary");
                    WHERE("STR_TO_DATE(RegistDate, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 6 MONTH)");
                    AND().WHERE("State <>4");
                    AND().WHERE("ProjectSeq != 1");
                    if (trialIndex != 0) {
                        AND().WHERE("TrialIndex =" + trialIndex);
                    }
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("T.SubjectId=" + subjectId);
                    }

                }

            }.toString();

        }

        public String findListForUpdateTrialIndexName3(String subjectId, int trialIndex) {
            return new SQL() {
                // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
                // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 6 MONTH) AND State <>4 and
                // ProjectSeq != 1;")
                {
                    SELECT("*");
                    FROM("T_DataSummary");
                    WHERE("STR_TO_DATE(RegistDate, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 12 MONTH)");
                    AND().WHERE("State <>4");
                    AND().WHERE("ProjectSeq != 1");
                    if (trialIndex != 0) {
                        AND().WHERE("TrialIndex =" + trialIndex);
                    }
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("T.SubjectId=" + subjectId);
                    }

                }

            }.toString();

        }

        public String findListForUpdateTrialIndexName4(String subjectId, int trialIndex) {
            return new SQL() {
                // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
                // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 6 MONTH) AND State <>4 and
                // ProjectSeq != 1;")
                {
                    SELECT("*");
                    FROM("T_DataSummary");
                    WHERE("STR_TO_DATE(RegistDate, '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 18 MONTH)");
                    AND().WHERE("State <>4");
                    AND().WHERE("ProjectSeq != 1");
                    if (trialIndex != 0) {
                        AND().WHERE("TrialIndex =" + trialIndex);
                    }
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("T.SubjectId=" + subjectId);
                    }

                }

            }.toString();

        }

        public String findBySubject(String subjectId, long projectSeq, int trialIndex) {
            return new SQL() {
                {
                    SELECT("*");
                    FROM("T_DataSummary");
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("SubjectId =" + subjectId);
                    }
                    if (projectSeq != 0) {
                        AND().WHERE("ProjectSeq = " + projectSeq);
                    }
                    if (trialIndex != 0) {
                        AND().WHERE("TrialIndex =" + trialIndex);
                    }

                }

            }.toString();

        }

        // @Select("SELECT * FROM T_DataSummary where SubjectId = #{subjectId}")
    }

    @SelectProvider(DataCommonSqlProvider.class)
    public List<DashBoard> findDataSummarybyOrgId(@Param("projectSeq") long projectSeq);

    // @Select("SELECT SubjectId, MAX(TrialIndex) AS TrialIndex, MAX(CDate) AS
    // CDate, EyeTracking FROM T_DataSummary"
    // + " WHERE Eyetracking = 0 AND TrialIndex > 1 AND State <> 4 AND ProjectSeq !=
    // 1 GROUP BY SubjectId HAVING MAX(CDate) <= CURDATE() - INTERVAL 14 DAY ORDER
    // BY SubjectId")
    // public List<DataSummary> findNonImplemenDataSummaries();
    @Select("SELECT SubjectId, TrialIndex, CDate, EyeTracking FROM (SELECT SubjectId, TrialIndex, CDate, EyeTracking, "
            + " ROW_NUMBER() OVER (PARTITION BY SubjectId ORDER BY TrialIndex DESC) AS rn "
            + " FROM T_DataSummary "
            + " WHERE EyeTracking = 0 AND TrialIndex > 1 AND State IN (2,4) AND TestYn ='N' AND ProjectSeq != 1 AND CDate <= CURDATE() - INTERVAL 14 DAY) AS subquery WHERE rn = 1")
    public List<DataSummary> findNonImplemenDataSummaries();

    @Select("SELECT ProjectSeq, CMonth AS Month, EXTRACT(YEAR FROM CDate) AS Year, COUNT(*)As Count FROM T_DataSummary"
            + " WHERE State NOT IN (2,4)  AND TestYn ='N' AND TrialIndex =1 AND SubjectId NOT LIKE '%%%230101%%' "
            + " GROUP BY ProjectSeq, CMonth, Year"
            + " UNION SELECT NULL AS ProjectSeq, CMonth AS Month, EXTRACT(YEAR FROM CDate) AS Year, COUNT(*)"
            + " FROM  T_DataSummary"
            + " WHERE State IN (2,4)  AND TestYn ='N' AND TrialIndex =1  AND SubjectId NOT LIKE '%%%230101%%' "
            + " GROUP BY CMonth, Year"
            + " ORDER BY Year ASC, Month ASC , COALESCE(ProjectSeq, 0)")
    public List<DashBoard> getTotalDataSummaryState();

    public Integer queryCount(SelectStatementProvider statement);

    @UpdateProvider(DataCommonSqlProvider.class)
    public Integer updateDataBySubjectId(@Param("table") String table, AsdProjectSubject subject,
            @Param("status") String stauts);

    @Delete("DELETE FROM T_DataSummary WHERE subjectId = #{subjectId}")
    public int deleteDataSummaryBySubjectId(AsdProjectSubject subject);

    @Delete("DELETE FROM T_DataSummary WHERE SubjectId = #{subjectId} and ProjectSeq = #{projectSeq} and TrialIndex= #{trialIndex}")
    public int deleteDataSummaryFindBySubjectId(AsdProjectSubject subject);

    @Delete("UPDATE ${group} SET SubjectId = CONCAT(#{subjectId}, '_', now()) WHERE SystemId = #{systemId} AND SubjectId= #{subjectId} AND ProjectSeq= #{projectSeq} AND TrialIndex= #{trialIndex}")
    public int deleteTrialData(String systemId, String subjectId, long projectSeq, @Param("trialIndex") int trialIndex,
            String group);

    @Insert("INSERT INTO T_DataSummary(SubjectId, Name, Gender, BirthDay, TrialIndex, ProjectSeq, State, OrgId, OrgName, RegistDate, TestYn, CUser, CDate) VALUES(#{subjectId}, #{name}, #{gender}, #{birthDay}, #{trialIndex}, #{projectSeq}, #{state}, substring(#{subjectId}, 1,2), #{orgName}, #{registDate}, #{testYn}, #{userCreated}, CURRENT_TIMESTAMP)")
    public int addDataIntegrationBySubjectId(AsdProjectSubject subject);

    @Insert("UPDATE T_DataSummary SET ${column} = #{is} WHERE SubjectId = #{subjectId} and TrialIndex = #{trialIndex} and ProjectSeq = #{projectSeq}")
    public int updateDataSummaryValueForColumn(@Param("column") String column, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex, @Param("is") boolean is);

    // @Update("UPDATE T_DataSummary SET Name= #{name}, Gender= #{gender},
    // BirthDay=#{birthDay}, TrialIndex= #{trialIndex}, ProjectSeq= #{projectSeq},
    // State=#{state}, TestYn =#{testYn}"
    // + " where SubjectId = #{subjectId}"
    // + " and TrialIndex = (SELECT TrialIndex FROM (SELECT MAX(TrialIndex) AS
    // TrialIndex FROM T_DataSummary where SubjectId = #{subjectId}) AS subquery)")
    @Update("UPDATE T_DataSummary SET Name = #{name}, Gender = #{gender}, BirthDay = #{birthday}, TrialIndex = #{trialIndex}, ProjectSeq = #{projectSeq}, State = #{state}, TestYn = #{testYn}, UUser = #{uUser} , UDate = #{uDate} "
            + " WHERE SubjectId = #{subjectId} and Id = #{id}"
            + " AND TrialIndex = ( SELECT TrialIndex FROM (SELECT MAX(TrialIndex) AS TrialIndex FROM T_DataSummary WHERE SubjectId = #{subjectId} ) AS subquery)"
            + " ORDER BY CDate DESC"
            + " LIMIT 1")
    public boolean updateDateIntegrationBySubjectIdLastList(DataSummary subject); // 상용할 쿼리로 통일 시킬 것

    // @Update("UPDATE T_DataSummary SET Name= #{name}, Gender= #{gender}, BirthDay=
    // #{birthDay}, TrialIndex= #{trialIndex}, ProjectSeq= #{projectSeq}, State=
    // #{state}, TestYn =#{testYn} where SubjectId = #{subjectId} and Id = #{id} ")
    // public int updateDateIntegrationBySubjectId(AsdProjectSubject subject); //
    // stage 테스트를 위한 임시적용
    @Update("UPDATE T_DataSummary SET Name= #{Name}, Gender= #{gender}, BirthDay= #{birthday}, TrialIndex= #{trialIndex}, ProjectSeq= #{projectSeq}, State= #{state}, TestYn =#{testYn} , UUser = #{uUser} , UDate = #{uDate} where SubjectId = #{subjectId} and Id = #{id} ")
    public int updateDateIntegrationBySubjectId(DataSummary subject); // stage 테스트를 위한 임시적용

    @Select("SELECT * FROM T_DataSummary where SubjectId = #{subjectId}")
    public List<DataSummary> findAllBySubject(@Param("subjectId") String subjectId);

    @SelectProvider(DataCommonSqlProvider.class)
    public List<DataSummary> findBySubject(@Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq,
            @Param("trialIndex") int trialIndex);

    // @Select("SELECT * FROM T_DataSummary WHERE SubjectId = #{subjectId} and
    // TrialIndex = (SELECT TrialIndex FROM (SELECT MAX(TrialIndex) AS TrialIndex
    // FROM T_DataSummary where SubjectId = #{subjectId}) AS sub)")
    @Select("SELECT * FROM T_DataSummary WHERE SubjectId = #{subjectId} ORDER BY TrialIndex DESC ,CDate DESC LIMIT 1")
    public List<DataSummary> findbyLastTrialIndexDatSummaryList(@Param("subjectId") String subjectId);

    @Select("SELECT TrialIndex FROM (SELECT MAX(TrialIndex) AS TrialIndex FROM T_DataSummary where SubjectId = #{subjectId}) AS sub")
    public List<DataSummary> findbyLastTrialIndex(@Param("subjectId") String subjectId);

    // @Update("UPDATE T_DataSummary SET Name= #{name}, Gender= #{gender}, BirthDay=
    // #{birthDay}, TrialIndex= #{trialIndex}, ProjectSeq= #{projectSeq}, State=
    // #{state}, TestYn =#{testYn} where SubjectId = #{subjectId}")
    // public int updateDateIntegrationBySubjectId(AsdProjectSubject subject); //
    // stage 테스트를 위한 임시적용
    @Select("WITH OrgIds AS ( "
            + " SELECT 10 AS OrgId UNION ALL "
            + " SELECT 11 UNION ALL "
            + " SELECT 12 UNION ALL "
            + " SELECT 13 UNION ALL "
            + " SELECT 14 UNION ALL "
            + " SELECT 15 UNION ALL "
            + " SELECT 16 UNION ALL "
            + " SELECT 17 ), "
            + " TrialInfoStates AS ( SELECT D.ProjectSeq, D.TrialIndex, D.OrgId, D.Mchat, D.Eyetracking, D.VideoResource, D.FirstSelection, D.SecondSelection FROM T_DataSummary D WHERE D.TestYn = 'N' ), "
            + " TrialInfoStateMatches AS ( SELECT TS.ProjectSeq, TS.TrialIndex, TS.OrgId, CASE WHEN TS.Mchat = 1 THEN 1 ELSE 0 END AS Mchats, CASE WHEN TS.Eyetracking = 1 THEN 1 ELSE 0 END AS EyeTrackings, "
            + " CASE WHEN TS.VideoResource = 1 THEN 1 ELSE 0 END AS VideoResources, CASE WHEN TS.FirstSelection = 1 THEN 1 ELSE 0 END AS FirstSelections, CASE WHEN TS.SecondSelection = 1 THEN 1 ELSE 0 END AS SecondSelections "
            + " FROM TrialInfoStates TS ), CountedTrialInfoStates AS ( SELECT ProjectSeq, TrialIndex, OrgId, SUM(CASE WHEN (CASE WHEN Mchats = 1 THEN 1 ELSE 0 END + CASE WHEN EyeTrackings = 1 THEN 1 ELSE 0 END + "
            + " CASE WHEN VideoResources = 1 THEN 1 ELSE 0 END + CASE WHEN FirstSelections = 1 AND SecondSelections IN (0, 1) THEN 1 ELSE 0 END) = 4 THEN 1 ELSE 0 END) AS StateCount ,"
            + " SUM(CASE WHEN (CASE WHEN Mchats = 1 THEN 1 ELSE 0 END + CASE WHEN EyeTrackings = 1 THEN 1 ELSE 0 END + CASE WHEN VideoResources = 1 THEN 1 ELSE 0 END + CASE WHEN FirstSelections = 1 "
            + " AND SecondSelections IN (0, 1) THEN 1 ELSE 0 END) BETWEEN 1 AND 3 THEN 1 ELSE 0 END) AS NotStateCount FROM TrialInfoStateMatches GROUP BY ProjectSeq, TrialIndex, OrgId ), "
            + " AllCombinations AS ( SELECT DISTINCT C.ProjectSeq, C.TrialIndex, O.OrgId FROM CountedTrialInfoStates C CROSS JOIN OrgIds O ) "
            + " SELECT A.ProjectSeq, A.TrialIndex, A.OrgId, COALESCE(SUM(CI.StateCount), 0) AS TotalStateCount, COALESCE(SUM(CI.NotStateCount), 0) AS TotalNotStateCount "
            + " FROM AllCombinations A LEFT JOIN CountedTrialInfoStates CI ON A.ProjectSeq = CI.ProjectSeq AND A.TrialIndex = CI.TrialIndex AND A.OrgId = CI.OrgId "
            + " GROUP BY A.ProjectSeq, A.TrialIndex, A.OrgId HAVING ProjectSeq IS NOT NULL AND A.TrialIndex = #{trialIndex} ORDER BY A.ProjectSeq, A.TrialIndex, A.OrgId")
    public List<DashBoard> findAllTrialInfoStateSurmmary(@Param("trialIndex") int trialIndex);

    @Update("UPDATE T_DataSummary SET ${column} = #{boolean} WHERE SubjectId = #{subjectId} AND ProjectSeq = #{projectSeq} AND TrialIndex = #{trialIndex}")
    public int updateDataSummaryColumn(@Param("column") String column, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex, @Param("boolean") boolean flag);

    @Update("UPDATE T_DataSummary SET SubjectId = #{subjectId}, ProjectSeq = #{projectSeq}, Gender = #{gender}, Birthday = #{birthday}, TrialIndex = #{trialIndex}, "
            + " State = #{state}, OrgId = #{orgId}, OrgName = #{orgName}, Name = #{Name}, "
            + " FirstSelection = #{firstSelection}, SecondSelection = #{secondSelection}, "
            + " Mchat = #{mchat}, EyeTracking = #{eyeTracking}, VideoResource = #{videoResource}, "
            + " VitalSigns = #{vitalSigns}, AudioResource = #{audioResource}, Blood = #{blood}, "
            + " Stool = #{stool}, Urine = #{Urine}, Fnirs = #{fnirs}, Eeg = #{eeg}, Mri = #{mri}, CUser = #{cUser}, CDate = #{cDate}, "
            + " UUser = #{uUser}, UDate = #{uDate}, TestYn = #{testYn}, "
            + " RegistDate = #{registDate}, DateTrialIndex = #{dateTrialIndex}"
            + " WHERE Id = #{id}")
    public boolean updateDataSummaryForSyncFromMongoDB(DataSummary summary);

    // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
    // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 6 MONTH) AND State <>4 and
    // ProjectSeq != 1;")
    @SelectProvider(DataCommonSqlProvider.class)
    public List<DataSummary> findListForUpdateTrialIndex2(@Param("trialIndex") int trialIndex);

    // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
    // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 12 MONTH) AND State <>4 and
    // ProjectSeq != 1;")
    @SelectProvider(DataCommonSqlProvider.class)
    public List<DataSummary> findListForUpdateTrialIndex3(@Param("trialIndex") int trialIndex);

    // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
    // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 18 MONTH) AND State <>4 and
    // ProjectSeq != 1;")
    @SelectProvider(DataCommonSqlProvider.class)
    public List<DataSummary> findListForUpdateTrialIndex4(@Param("trialIndex") int trialIndex);

    @SelectProvider(DataCommonSqlProvider.class)
    public List<DataSummary> findListForUpdateTrialIndexName2(@Param("subjectId") String subjectId,
            @Param("trialIndex") int trialIndex);

    // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
    // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 12 MONTH) AND State <>4 and
    // ProjectSeq != 1;")
    @SelectProvider(DataCommonSqlProvider.class)
    public List<DataSummary> findListForUpdateTrialIndexName3(@Param("subjectId") String subjectId,
            @Param("trialIndex") int trialIndex);

    // @Select("SELECT * FROM T_DataSummary WHERE STR_TO_DATE(RegistDate,
    // '%Y-%m-%d') = DATE_SUB(CURDATE(), INTERVAL 18 MONTH) AND State <>4 and
    // ProjectSeq != 1;")
    @SelectProvider(DataCommonSqlProvider.class)
    public List<DataSummary> findListForUpdateTrialIndexName4(@Param("subjectId") String subjectId,
            @Param("trialIndex") int trialIndex);

    @Insert("INSERT INTO T_DataSummary(SubjectId, Name, Gender, BirthDay, TrialIndex, ProjectSeq, State, OrgId, OrgName, RegistDate, CUser, CDate) VALUES(#{subjectId}, #{name}, #{gender}, #{birthday}, #{trialIndex}, #{projectSeq}, #{state}, substring(#{subjectId}, 1,2), #{orgName}, #{registDate}, #{cUser},#{testYn}, CURRENT_TIMESTAMP)")
    public int addSubjectForTrialIndex(DataSummary dataSummary);

    @Insert("INSERT INTO T_DataSummary(SubjectId, Name, Gender, BirthDay, TrialIndex, ProjectSeq, State, OrgId, OrgName, RegistDate, CUser, CDate,UUser, UDate,DateTrialIndex,TestYn) VALUES(#{subjectId}, #{name}, #{gender}, #{birthday}, #{trialIndex}, #{projectSeq}, #{state}, substring(#{subjectId}, 1,2), #{orgName}, #{registDate}, #{cUser}, #{cDate},#{uUser},#{uDate},#{dateTrialIndex},#{testYn})")
    public int addForcedSubjectForTrialIndex(DataSummary dataSummary);

    @Select("SELECT * FROM T_DataSummary where SubjectId = #{subjectId} AND TrialIndex = #{trialIndex}")
    public DataSummary findByTrialIndexList(@Param("subjectId") String subjectId,
            @Param("trialIndex") int trialIndex);

    @SelectProvider(DataCommonSqlProvider.class)
    public List<DataSummary> findAll(String subjectId, long projectSeq, String orgId, String gender,
            @Param("page") int page, @Param("offset") int offset);

    @Select("SELECT * FROM T_DataSummary WHERE SubjectId = #{subjectId} AND ProjectSeq = #{projectSeq} AND TrialIndex = #{trialIndex}")
    public List<DataSummary> findOne(@Param("subjectId") String subjectId, @Param("projectSeq") long projectSeq,
            @Param("trialIndex") int trialIndex);

    @Select("SELECT SubjectId FROM T_SmsSchedulerHistory where TaskName = #{taskName} and Result ='sucess' and TrialIndex > 1")
    public List<String> findSubjectUpdateBatchList(@Param("taskName") String taskName);

    @SelectProvider(DataCommonSqlProvider.class)
    public int countAll(String subjectId, long projectSeq, String orgId, String gender);

}
