package com.ecoinsight.bdsp.asd.repository;

import java.util.List;
import java.util.Objects;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

import com.ecoinsight.bdsp.asd.entity.MChart;
import com.ecoinsight.bdsp.asd.entity.Selection;
import com.ecoinsight.bdsp.asd.entity.SurveyStatus;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

@Mapper
public interface ISelectionRepository extends IEntityRepository {

    class SelectionSqlProvider implements ProviderMethodResolver {

        public String findAll(String systemId, String subjectId, long projectSeq, String orgId, int page, int offset) {
            return new SQL() {
                {
                    SELECT("D.SubjectId as subjectId, D.orgId, D.Gender as gender, D.registDate AS registDate, D.OrgName as orgName, D.BirthDay as birthDay, D.ProjectSeq,P.ProjectName, D.TrialIndex, D.state");
                    SELECT("T.KDST, T.KMCHAT, T.KQCHAT, T.SELSI, T.CBCL, T.ADOS2MT, T.ADOS2M1, T.ADOS2M2, T.ADOS2M3, T.SRS2, T.ADIR, T.SCQLIFETIME, T.PRES, T.KCARS2, T.BEDEVELQ, T.BEDEVELI, T.BEDEVELP, T.KVINELAND, T.KBAYLEY, T.KWPPSI ,T.SCQCURRENT");

                    SELECT("T.KQCHAT, T.BEDEVELQ, T.BEDEVELI, T.BEDEVELP, T.KVINELAND,   T.KDST, T.KMCHAT,  T.SELSI, T.CBCL, T.ADOS2MT, T.ADOS2M1, T.ADOS2M2, T.ADOS2M3, T.SRS2, T.ADIR, T.SCQLIFETIME, T.PRES, T.KCARS2, T.KBAYLEY, T.KWPPSI ,T.SCQCURRENT");

                    SELECT("D.CUser as userCreated, D.CDate as dateCreated");
                    // FROM("T_DataSummary D");
                    FROM("T_SurveyStatus T");
                    LEFT_OUTER_JOIN(
                            "T_DataSummary D ON D.SubjectId=T.SubjectId AND D.ProjectSeq=T.ProjectSeq AND D.TrialIndex=T.TrialIndex");
                    LEFT_OUTER_JOIN("T_Project P ON D.ProjectSeq = P.ProjectSeq");
                    WHERE("1=1");
                    AND().WHERE("D.state NOT IN (2,4)");
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("D.SubjectId=" + subjectId);
                    }
                    if (projectSeq != 0) {
                        AND().WHERE("D.ProjectSeq= " + projectSeq);
                    }
                    if (!Objects.isNull(orgId)) {
                        AND().WHERE("D.OrgId= " + orgId);
                    }

                    ORDER_BY("T.CDate DESC");
                    LIMIT(offset);
                    OFFSET(page <= 1 ? 0 : (page - 1) * offset);

                }
            }.toString();

        }

        public String findAlltemp(String systemId, String subjectId, long projectSeq) {
            return new SQL() {
                {
                    SELECT("D.SubjectId as subjectId, D.orgId, D.Gender as gender, D.registDate AS registDate, D.OrgName as orgName, D.BirthDay as birthDay, D.ProjectSeq,P.ProjectName, D.TrialIndex, D.state");
                    SELECT("T.KDST, T.KMCHAT, T.KQCHAT, T.SELSI, T.CBCL, T.ADOS2MT, T.ADOS2M1, T.ADOS2M2, T.ADOS2M3, T.SRS2, T.ADIR, T.SCQLIFETIME, T.PRES, T.KCARS2, T.BEDEVELQ, T.BEDEVELI, T.BEDEVELP, T.KVINELAND, T.KBAYLEY, T.KWPPSI ,T.SCQCURRENT");

                    SELECT("T.KQCHAT, T.BEDEVELQ, T.BEDEVELI, T.BEDEVELP, T.KVINELAND,   T.KDST, T.KMCHAT,  T.SELSI, T.CBCL, T.ADOS2MT, T.ADOS2M1, T.ADOS2M2, T.ADOS2M3, T.SRS2, T.ADIR, T.SCQLIFETIME, T.PRES, T.KCARS2, T.KBAYLEY, T.KWPPSI ,T.SCQCURRENT");

                    SELECT("D.CUser as userCreated, D.CDate as dateCreated");
                    // FROM("T_DataSummary D");
                    FROM("T_SurveyStatus T");
                    LEFT_OUTER_JOIN(
                            "T_DataSummary D ON D.SubjectId=T.SubjectId AND D.ProjectSeq=T.ProjectSeq AND D.TrialIndex=T.TrialIndex");
                    LEFT_OUTER_JOIN("T_Project P ON D.ProjectSeq = P.ProjectSeq");
                    WHERE("1=1");
                    AND().WHERE("D.state NOT IN (2,4)");
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("D.SubjectId=" + subjectId);
                    }
                    if (projectSeq != 0) {
                        AND().WHERE("D.ProjectSeq= " + projectSeq);
                    }
                    // if (!Objects.isNull(orgId)) {
                    //     AND().WHERE("D.OrgId= " + orgId);
                    // }

                    ORDER_BY("T.CDate DESC");

                }
            }.toString();

        }

        public String countAll(String systemId, String subjectId, long projectSeq, String orgId) {
            return new SQL() {
                {
                    SELECT("COUNT(D.SubjectId)");
                    // SELECT("T.Id, T.SubjectId, T.Name, T.Gender, T.BirthDay, T.TrialIndex,
                    // T.ProjectSeq, T.OrgId, T.OrgName");
                    // SELECT("T.FirstSelection, T.SecondSelection, T.Mchat, T.Eyetracking,
                    // T.VideoResource, T.VitalSigns");
                    // SELECT("T.AudioResource, T.Blood, T.Stool, T.Urine, T.FNIRS, T.EEG, T.MRI,
                    // T.CUser, T.CDate, T.UUser, T.UDate");
                    FROM("T_SurveyStatus T");
                    LEFT_OUTER_JOIN(
                            "T_DataSummary D ON D.SubjectId=T.SubjectId AND D.ProjectSeq=T.ProjectSeq AND D.TrialIndex=T.TrialIndex");
                    WHERE("1=1");
                    AND().WHERE("D.state <> 4");
                    if (!Objects.isNull(subjectId)) {
                        AND().WHERE("D.SubjectId=" + subjectId);
                    }
                    if (projectSeq != 0) {
                        AND().WHERE("D.ProjectSeq= " + projectSeq);
                    }
                    if (!Objects.isNull(orgId)) {
                        AND().WHERE("D.OrgId= " + orgId);
                    }

                    // ORDER_BY("T.UDate DESC");
                    // LIMIT(offset);
                    // OFFSET(page <= 1 ? 0 : (page - 1) * offset);
                }
            }.toString();

        }
    }

    @SelectProvider(SelectionSqlProvider.class)
    public int countAll(String systemId, String subjectId, long projectSeq, String orgId);

    @SelectProvider(SelectionSqlProvider.class)
    public List<SurveyStatus> findAll(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq, @Param("orgId") String orgId, @Param("page") int page,
            @Param("offset") int offset);

    @SelectProvider(SelectionSqlProvider.class)
    public List<SurveyStatus> findAlltemp(@Param("systemId") String systemId, @Param("subjectId") String subjectId,
            @Param("projectSeq") long projectSeq
    );
}
