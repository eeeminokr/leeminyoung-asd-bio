package com.ecoinsight.bdsp.asd.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.ecoinsight.bdsp.asd.entity.DataSummary;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.repository.IAsdDataCommonRepoistory;
import com.ecoinsight.bdsp.core.util.DateUtil;

@Service(DataCommonService.ID)
public class DataCommonService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String ID = "DataCommonService";

    @Autowired
    IAsdDataCommonRepoistory _dataCommonRepository;

    public void updateDataSummaryColumn(DataSummaryColumn columnEnum, String subjectId, long projectSeq, int trialIndex,
            boolean flag) {
        List<DataSummary> result = this._dataCommonRepository.findOne(subjectId, projectSeq, trialIndex);
        if (result == null || result.size() == 0) {
            throw new RuntimeException("Not found a data summary. - subjectId=" + subjectId + ", projectSeq="
                    + projectSeq + ", trialIndex=" + trialIndex);
        }
        LocalDateTime now = LocalDateTime.now();
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        String dateString = DateUtil.formatSimpleDateTime(LocalDateTime.now());

        // outputFormat.parse(now)
        int status = this._dataCommonRepository.updateDataSummaryColumn(columnEnum.name(), subjectId, projectSeq,
                trialIndex, flag);
        if (status > 0) {
            LOGGER.info("업데이트 완료되었습니다 => ,subjectId ={},projectSeq={},trialIndex={},flag ={}", subjectId,
                    projectSeq,
                    trialIndex, flag);
        }

    }

    public void addSubjectForTrialIndex(boolean flag) throws Exception {
        updateTrialIndexForDataSummary(2, "2차");
        updateTrialIndexForDataSummary(3, "3차");
        updateTrialIndexForDataSummary(4, "4차");
    }

    private void updateTrialIndexForDataSummary(int trialIndex, String trialDescription) throws Exception {
        List<DataSummary> results = getDataSummaryForTrialIndex(trialIndex);

        if (results.isEmpty()) {
            LOGGER.info("Unable to find a data summary list for adding trialIndex " + trialDescription + ".");
            return;
        }

        LOGGER.info("Inserting subjects for " + trialDescription + ":");
        for (DataSummary result : results) {
            LOGGER.info("-> insert subject => subjectId=" + result.getSubjectId() +
                    ", projectSeq=" + result.getProjectSeq() +
                    ", trialIndex=" + result.getTrialIndex());

            result.setTrialIndex(trialIndex);
            this._dataCommonRepository.addSubjectForTrialIndex(result);
        }
    }

    public List<DataSummary> getDataSummaryForTrialIndex(int trialIndex) throws Exception {
        switch (trialIndex) {
            case 2:
                return this._dataCommonRepository.findListForUpdateTrialIndex2(0);
            case 3:
                return this._dataCommonRepository.findListForUpdateTrialIndex3(0);
            case 4:
                return this._dataCommonRepository.findListForUpdateTrialIndex4(0);
            default:
                return Collections.emptyList();
        }
    }

    // public void getFindUpdatedTrialIndexNormal() {

    // List<DataSummary> list = this._dataCommonRepository.findBySubject(null, 1,
    // 2);

    // boolean state = false;
    // int deleted = 0;
    // if (!list.isEmpty()) {
    // LOGGER.info("Attempting to found updated TrialIndex into ProjectSeq mysql
    // DataSurmmary={}", list);
    // for (DataSummary summary : list) {

    // Map<String, Boolean> map = summary.trialInfoState(summary);

    // for (DataSummaryColumn column : DataSummaryColumn.values()) {

    // if (map.containsKey(column.name())) {
    // if (!map.get(column.name())) {
    // state = true;
    // } else {
    // state = false;
    // break;
    // }
    // }
    // }
    // if (state) {
    // var subjects = this._projectSubjectRepository.findBySubjectId(SYSTEM_ID,
    // summary.getProjectSeq(),
    // summary.getSubjectId());

    // // AsdProjectSubject subject = subjects.stream().findFirst().orElse(null);
    // for (AsdProjectSubject subject : subjects) {
    // if (subject.getTrialIndex() == summary.getTrialIndex()) {
    // subject.setTrialIndex(1);

    // this._projectSubjectRepository.saveAll(subjects);

    // deleted =
    // this._dataCommonRepository.deleteDataSummaryFindBySubjectId(subject);
    // } else {
    // subject.setTrialIndex(2);
    // deleted =
    // this._dataCommonRepository.deleteDataSummaryFindBySubjectId(subject);
    // }

    // }
    // }
    // if (deleted > 0) {
    // LOGGER.info("success updated trialIndex into ProjectSEq", list);
    // }
    // }
    // }

    // }
}
