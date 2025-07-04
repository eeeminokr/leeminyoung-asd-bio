package com.ecoinsight.bdsp.asd.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecoinsight.bdsp.asd.entity.Selection;
import com.ecoinsight.bdsp.asd.entity.SurveyStatus;
import com.ecoinsight.bdsp.asd.repository.ISelectionRepository;

@Transactional
@Service(SelectionService.ID)
public class SelectionService {

    public static final String ID = "SelectionService";
    @Autowired
    private ISelectionRepository _repository;

    @Autowired
    private DataCommonService _DataCommonService;

    @Autowired
    private SurveyStatusService _surveyStatusService;

    public List<SurveyStatus> getSelectionAll(String systemId, String subjectId, long projectSeq, String orgId, int page, int offset) {

        List<SurveyStatus> list = this._repository.findAll(systemId, subjectId, projectSeq, orgId, page, offset);
        // List<SurveyStatus> resultList = this._repository.findAlltemp(systemId, subjectId, projectSeq, orgId);
        // this._surveyStatusService.findSurveyDataBySubject(resultList);

        return list;
    }

    public  Collection<List<Map<String, Object>>> getSelectionAlltemp(String systemId, String subjectId, long projectSeq) {

        // List<SurveyStatus> list = this._repository.findAll(systemId, subjectId, projectSeq, orgId, page, offset);
        List<SurveyStatus> resultList = this._repository.findAlltemp(systemId, subjectId, projectSeq);
        Collection<List<Map<String, Object>>> resultCollection = this._surveyStatusService.findSurveyDataBySubject(resultList);

        return resultCollection;
    }

    public int countAll(String systemId, String subjectId, long projectSeq, String orgId) {
        return this._repository.countAll(systemId, subjectId, projectSeq, orgId);
    }

}
