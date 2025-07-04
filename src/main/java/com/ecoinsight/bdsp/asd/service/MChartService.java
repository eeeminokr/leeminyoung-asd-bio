package com.ecoinsight.bdsp.asd.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.ecoinsight.bdsp.asd.entity.MChart;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.MChartModel;
import com.ecoinsight.bdsp.asd.model.ProjectSeq;
import com.ecoinsight.bdsp.asd.repository.IMChartRepository;
import com.ecoinsight.bdsp.core.service.ServiceException;
import com.google.gson.Gson;

@Transactional
@Service(MChartService.ID)
public class MChartService implements AsdSubjectService {
    public static final String ID = "MChartService";
    @Autowired
    private IMChartRepository _repository;
    @Autowired
    private DataCommonService _dataCommonService;
    @Resource(name = KMChartService.ID)
    private KMChartService kmChartService;

    @Transactional(propagation = Propagation.REQUIRED)
    public void create(MChart chart, String birthday, String name) throws ServiceException {
        try {
            this._repository.add(chart);
            this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.MChat, chart.getSubjectId(),
                    chart.getProjectSeq(), chart.getTrialIndex(), true); // 각기능 업로드시에 옴니측에서 해당 trialIndex 값의 조건으로 업데이트
                                                                         // 되므로
                                                                         // updateDateIntegrationBySubjectId 와 다른 sql 진행
        } catch (Exception e) {
            throw new ServiceException("Fail to create a mchart in database. - " + e.getMessage(), e);
        }

        try {
            this.kmChartService.sendMChart(chart, birthday, name);
        } catch (Exception e) {
            throw new ServiceException(
                    String.format("KM Chat 전송시 에러가 발생했습니다. - subjectId=%s, projectSeq=%s, trialIndex=%s",
                            chart.getSubjectId(), chart.getProjectSeq(), chart.getTrialIndex()),
                    e);
        }
    }

    public Optional<MChart> getMChart(String systemId, String subjectId, long projectSeq, int trialIndex) {
        return this._repository.query1(systemId, subjectId, projectSeq, trialIndex);
    }

    public List<MChart> getMChart(String systemId, String subjectId, long projectSeq) {
        return this._repository.query2(systemId, subjectId, projectSeq);
    }

    public List<MChart> getMChartAll(String systemId, String subjectId, long projectSeq, String orgId, String gender,
            int page, int offset) {
        return this._repository.findAll(systemId, subjectId, projectSeq, orgId, gender, page, offset);
    }

    public int countAll(String systemId, String subjectId, long projectSeq, String orgId, String gender) {
        return this._repository.countAll(systemId, subjectId, projectSeq, orgId, gender);
    }

    /**
     * 보류(4) to 정상(1), 고위험군(2), 자폐(3)
     * 
     * @param systemId
     * @param subjectId
     * @param projectSeq
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void doLabelling(String systemId, String subjectId, long projectSeq) throws Exception {

        List<MChart> result = this._repository.query2(systemId, subjectId, ProjectSeq.HOLD.getSeq());
        for (MChart item : result) {
            this._repository.updateProjectSeq(item.getId(), projectSeq);
        }

        /*
         * List<MChart> result = this._repository.query2(systemId, subjectId, 0);
         * try {
         * if (!result.isEmpty()) {
         * MChart lastItem = result.get(result.size() - 1);
         * this._repository.updateProjectSeq(lastItem.getId(), projectSeq);
         * }
         * } catch (IndexOutOfBoundsException e) {
         * System.out.println("An error occurred: " + e.getMessage());
         * }
         */
    }

    public File download(File downloadDir, String systemId, String subjectId, long projectSeq, int trialIndex)
            throws Exception {
        Optional<MChart> optional = this._repository.query1(systemId, subjectId, projectSeq, trialIndex);
        if (optional.isEmpty()) {
            downloadDir.delete();
            return null;
        }

        MChart mchart = optional.get();
        MChartModel model = new MChartModel();
        model.setSubjectId(subjectId);
        model.setProjectSeq(projectSeq);
        model.setTrialIndex(trialIndex);
        model.setPatientSeq(mchart.getPatientSeq());
        model.setRschKey(mchart.getRschKey());
        model.setResult(mchart.getResult());
        model.setRegistDt(mchart.getRegistDt());

        File targetFile = new File(downloadDir,
                String.format("mchat_%s_%s_%s.json", subjectId, projectSeq, trialIndex));
        if (targetFile.exists())
            targetFile.delete();

        this.writeJsonFile(model, targetFile);
        return targetFile;
    }

    private void writeJsonFile(MChartModel model, File targetFile) throws Exception {
        Gson gson = new Gson();
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(targetFile);
            bos = new BufferedOutputStream(fos);
            String json = gson.toJson(model);
            byte[] buff = json.getBytes();
            bos.write(buff, 0, buff.length);
            bos.flush();
        } finally {
            try {
                if (bos != null)
                    bos.close();
            } catch (Exception e) {
            }
            try {
                if (fos != null)
                    fos.close();
            } catch (Exception e) {
            }
        }
    }

    public void changeProjectSeq(String systemId, String subjectId, long fromProjectSeq, long toProjectSeq)
            throws Exception {
        List<MChart> result = this._repository.query2(systemId, subjectId, fromProjectSeq);
        for (MChart item : result) {
            if (item.getProjectSeq() != toProjectSeq) {
                this._repository.updateProjectSeq(item.getId(), toProjectSeq);
            } else {
                return;
            }
        }
    }
}
