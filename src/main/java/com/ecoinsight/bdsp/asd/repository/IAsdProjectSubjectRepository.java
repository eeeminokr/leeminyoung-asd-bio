package com.ecoinsight.bdsp.asd.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.core.entity.SubjectSampleItem;

/**
 * 임상시험 참여 대상자 관리 기능 제공. (Mongo DB)
 */
public interface IAsdProjectSubjectRepository extends MongoRepository<AsdProjectSubject, String> {
    @Query("{'systemId': ?0}")
    public Collection<AsdProjectSubject> findAllBySystemId(String systemId);

    @Query("{'systemId': ?0, 'subjectId': ?1}")
    public Collection<AsdProjectSubject> findAllBySubjectId(String systemId, String subjectId);

    // @Query("{'systemId': ?0, 'projectSeq': ?1} ,'state': { $ne: 2,4 }")
    // @Query("{'systemId': ?0, 'subjectId': {'$in':
    // ['1123061511','1123062212','1123101711']}, 'projectSeq': ?1, 'state': { $nin:
    // [2, 4] } }")
    @Query("{'systemId': ?0, 'projectSeq': ?1, 'state': { $nin: [2, 4] } }")
    public Collection<AsdProjectSubject> findAllByProjectSeq(String systemId, long projectSeq);

    @Query("{'systemId': ?0, 'projectSeq': ?1, 'subjectId': ?2}")
    public Collection<AsdProjectSubject> findBySubjectId(String systemId, long projectId, String subjectId);

    @DeleteQuery("{'systemId': ?0, 'projectSeq': ?1, 'subjectId': ?2}")
    public long deleteBySubjectId(String systemId, long projectId, String subjectId);

    @DeleteQuery("{'systemId': ?0, 'projectSeq': ?1}")
    public long deleteByProject(String systemId, long projectId);

    @Query(value = "{'systemId': ?0, 'subjectId': ?1}", fields = "{'samples': true}")
    public Collection<SubjectSampleItem> findAllSamplesBySubjectId(String systemId, String subjectId);

    @Query(value = "{'systemId': ?0, 'samples._id': ?1}", fields = "{'samples.$': true}")
    public AsdProjectSubject findOneSampleByItemId(String systemId, String itemId);

    // @Query(value = "{'registDate': ?0, 'projectSeq': { $ne: NumberLong(1) }}")
    @Query(value = "{'registDate': ?0, 'projectSeq': { $ne: NumberLong(1) }, 'state': { $ne: 4 }}")
    Collection<AsdProjectSubject> findByRegistDate(String date);

    @Query(value = "{'dateTrialIndex': ?0, 'projectSeq': { $ne: NumberLong(1) }, 'state': { $ne: 4 }}")
    Collection<AsdProjectSubject> findByDateTrialIndex(String date);

    @Query("{'_id': ?0}")
    Collection<AsdProjectSubject> updateAsdProjectSubjectTrialIndex(String id, int trialIndex);

    @Query("{'systemId': ?0, 'subjectId': {'$in': ?1}, 'state': { $ne: 4 }}")
    public Collection<AsdProjectSubject> findAllBySubjectIdsList(String systemId, List<String> subjectIds);

    @Query("{'systemId': ?0, 'subjectId': {'$in': ?1}, 'projectSeq': { $ne: NumberLong(1) }, 'state': { $ne: 4 }}")
    public Collection<AsdProjectSubject> findAllBySubjectIds(String systemId, List<String> subjectIds);
}
