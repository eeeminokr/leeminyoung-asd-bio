package com.ecoinsight.bdsp.asd.repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

import com.ecoinsight.bdsp.asd.UrbanSurveyData;
import com.ecoinsight.bdsp.asd.entity.SurveyStatus;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

@Mapper
public interface ISurveyStatusRepository extends IEntityRepository {

        class ISurveyStatusProvider implements ProviderMethodResolver {

                public String query2(String systemId,
                                String subjectId,
                                long projectSeq) {

                        return new SQL() {
                                {
                                        SELECT("Id, OrgId, SystemId, SubjectId, ProjectSeq, TrialIndex, Gender, BirthDay, CUser, CDate, UUser, UDate");
                                        FROM("T_SurveyStatus");
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

        @SelectProvider(ISurveyStatusProvider.class)
        public List<SurveyStatus> query2(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
                        @Param("projectSeq") long projectSeq);

        @Select("SELECT Id,OrgId,SystemId,SubjectId,ProjectSeq,TrialIndex,Gender,BirthDay,KDST,KMCHAT,KQCHAT,SELSI,CBCL,ADOS2MT,ADOS2M1,ADOS2M2,ADOS2M3,SRS2,ADIR,SCQLIFETIME,PRES,KCARS2,BEDEVELQ,BEDEVELI,BEDEVELP,KVINELAND,KBAYLEY,KWPPSI,SCQCURRENT,CUser,CDate,UUser,UDate FROM T_SurveyStatus"
                        + " WHERE SystemId=#{systemId} and SubjectId=#{subjectId} and ProjectSeq=#{projectSeq} and TrialIndex=#{trialIndex}")
        public Optional<SurveyStatus> findByKeys(@Param("systemId") String systemId,
                        @Param("subjectId") String subjectId,
                        @Param("projectSeq") long projectSeq, @Param("trialIndex") int trialIndex);

        @Insert("INSERT INTO T_SurveyStatus(OrgId,SystemId,SubjectId,ProjectSeq,TrialIndex,Gender,BirthDay,KDST,KMCHAT,KQCHAT,SELSI,CBCL,ADOS2MT,ADOS2M1,ADOS2M2,ADOS2M3,SRS2,ADIR,SCQLIFETIME,PRES,KCARS2,BEDEVELQ,BEDEVELI,BEDEVELP,KVINELAND,KBAYLEY,KWPPSI,SCQCURRENT,CUser,CDate,UUser,UDate)"
                        + " VALUES(#{orgId}, #{systemId}, #{subjectId}, #{projectSeq}, #{trialIndex}, #{gender}, #{birthDay}, #{kdst},#{kmchat},#{kqchat},#{selsi},#{cbcl},#{ados2mt},#{ados2m1},#{ados2m2},#{ados2m3},#{srs2},#{adir},#{scqlifetime},#{pres},#{kcars2},#{bedevelq},#{bedeveli},#{bedevelp},#{kvineland},#{kbayley},#{kwppsi},#{scqcurrent}, #{userCreated},#{dateCreated},#{userUpdated},#{dateUpdated})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        public void add(SurveyStatus survey);

        @Update("UPDATE T_SurveyStatus SET KDST=#{kdst},KMCHAT=#{kmchat},KQCHAT=#{kqchat},SELSI=#{selsi},CBCL=#{cbcl},ADOS2MT=#{ados2mt},ADOS2M1=#{ados2m1},ADOS2M2=#{ados2m2},ADOS2M3=#{ados2m3},SRS2=#{srs2},ADIR=#{adir},SCQLIFETIME=#{scqlifetime},PRES=#{pres},KCARS2=#{kcars2},BEDEVELQ=#{bedevelq},BEDEVELI=#{bedeveli},BEDEVELP=#{bedevelp},KVINELAND=#{kvineland},KBAYLEY=#{kbayley},KWPPSI=#{kwppsi},SCQCURRENT=#{scqcurrent},UUser=#{userUpdated},UDate=#{dateUpdated} "
                        + " WHERE SystemId=#{systemId} and SubjectId=#{subjectId} and ProjectSeq=#{projectSeq} and TrialIndex=#{trialIndex}")
        public boolean update(SurveyStatus survey);

        @Update("UPDATE T_SurveyStatus SET ProjectSeq=#{projectSeq} WHERE Id=#{id}")
        public void updateProjectSeq(@Param("id") long id, @Param("projectSeq") long projectSeq);

        // @Select("SELECT ts.SUBJECT_NO AS SubjectNo, " // -- 연구대상자ID
        // + " tsr.NUM_OF_TIMES AS NumOfTimes, "
        // + " ts.SUBJECT_GROUP_CD AS subjectGroupCd, "
        // + " tsrbd.SURVEY_KIND_CD AS surveyKindCd, " // -- 설문종류
        // + " tsb.MARK_NO AS MarkNo," // -- 질문번호
        // + " tsb.QUESTION_NM AS QuestionNm, " // -- 질문
        // + " (CASE WHEN tsb.QUESTION_TYPE_CD = 'SELECT' THEN "
        // + " (SELECT tsbi.ITEM_NM "
        // + " FROM TB_SURVEY_BASIC_ITEM tsbi "
        // // + "AND "
        // + " WHERE tsbi.SUBJECT_GROUP_CD = tsrbd.SUBJECT_GROUP_CD "
        // + " AND tsbi.SURVEY_KIND_CD = tsrbd.SURVEY_KIND_CD "
        // + " AND tsbi.SURVEY_KIND_SEQ = tsrbd.SURVEY_KIND_SEQ "
        // + " AND tsbi.QUESTION_SEQ = tsrbd.QUESTION_SEQ "
        // + " AND tsbi.ITEM_SEQ = tsrbd.SELECT_ITEM_SEQ ) "
        // + " WHEN tsb.QUESTION_TYPE_CD = 'INPUT' THEN tsrbd.INPUT_VALUE "
        // + " ELSE '' "
        // + " END) as ANSWER " // -- 답변
        // + " FROM TB_SUBJECT ts "
        // + " JOIN TB_SURVEY_RESULT tsr ON ts.ID = tsr.SUBJECT_ID "
        // + " JOIN TB_SURVEY_RESULT_BASIC_DTL tsrbd ON tsr.ID = tsrbd.SURVEY_RESULT_ID
        // "
        // + " JOIN TB_SURVEY_BASIC tsb ON tsrbd.SUBJECT_GROUP_CD = tsb.SUBJECT_GROUP_CD
        // AND tsrbd.SURVEY_KIND_CD = tsb.SURVEY_KIND_CD AND tsrbd.SURVEY_KIND_SEQ =
        // tsb.SURVEY_KIND_SEQ AND tsrbd.QUESTION_SEQ = tsb.QUESTION_SEQ "
        // + " WHERE ts.SUBJECT_GROUP_CD=#{subjectGroupCd} AND tsrbd.SURVEY_KIND_CD
        // =#{surveyKindCd} "
        // + " AND tsr.STATUS_CD = 'COMPLETE'")
        // public List<UrbanSurveyData> findSurveyListbySubject(@Param("subjectGroupCd")
        // String subjectGroupCd,
        // @Param("surveyKindCd") String surveyKindCd);
        // -- WHERE ts.SUBJECT_NO ='1423120162'

        @Select("SELECT S.*, D.registDate "
                        + " FROM T_SurveyStatus S"
                        + " INNER JOIN T_DataSummary D ON D.SubjectId = S.SubjectId AND D.ProjectSeq = S.ProjectSeq AND D.TrialIndex=S.TrialIndex"
                        + " WHERE EXISTS (SELECT 1 FROM T_SurveyStatus E INNER JOIN SEOUL_ASD.T_DataSummary D ON E.SubjectId= D.SubjectId AND E.ProjectSeq=D.ProjectSeq"
                        + " WHERE S.SubjectId = E.SubjectId AND S.ProjectSeq = D.ProjectSeq) "
                        + " AND S.SubjectId =#{subjectId}"
                        + " ORDER BY S.SubjectId, S.TrialIndex")
        public List<SurveyStatus> findSurveyStatusbySubject(@Param("subjectId") String subjectId);

}
