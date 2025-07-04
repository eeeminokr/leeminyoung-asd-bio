package com.ecoinsight.bdsp.asd.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.ecoinsight.bdsp.asd.entity.SmsScheduler;
import com.ecoinsight.bdsp.core.entity.SchedulerLog;
import com.ecoinsight.bdsp.core.repository.IEntityRepository;

public interface ISmsSchudlerHistoryRepository extends IEntityRepository {

        /**
         * @param subjectId
         * @param trialIndex
         * @return
         */
        @Select("SELECT * FROM T_SmsSchedulerHistory WHERE SubjectId = #{subjectId} AND ProjectSeq= #{projectSeq} AND TrialIndex =#{trialIndex} AND TaskName = #{taskName}")
        public List<SmsScheduler> findBySenedSubjectSms(@Param("projectSeq") long projectSeq,
                        @Param("subjectId") String subjectId,
                        @Param("trialIndex") int trialIndex,
                        @Param("taskName") String taskName);

        @Insert("INSERT INTO T_SmsSchedulerHistory (SubjectId, ProjectSeq, TrialIndex, SystemId, PhoneNumber, TaskName, Result, DateTimeExecuted) "
                        + "VALUES (#{subjectId}, #{projectSeq}, #{trialIndex}, #{systemId}, #{phoneNumber}, #{taskName}, #{result}, #{dateTimeExecuted})")
        public int addSchedulerHistory(SmsScheduler smsScheduler);

}
