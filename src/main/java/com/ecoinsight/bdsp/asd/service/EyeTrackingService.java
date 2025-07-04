package com.ecoinsight.bdsp.asd.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.beans.factory.annotation.Autowired;

import com.ecoinsight.bdsp.asd.entity.EyeTracking;
import com.ecoinsight.bdsp.asd.entity.EyeTrackingInfo;
import com.ecoinsight.bdsp.asd.entity.VideoResource;
import com.ecoinsight.bdsp.asd.model.DataSummaryColumn;
import com.ecoinsight.bdsp.asd.model.EyeTrackingInfoModel;
import com.ecoinsight.bdsp.asd.model.EyeTrackingModel;
import com.ecoinsight.bdsp.asd.model.EyeTrackingRequestModel;
import com.ecoinsight.bdsp.asd.model.ProjectSeq;
import com.ecoinsight.bdsp.asd.model.TrackInfoModel;
import com.ecoinsight.bdsp.asd.repository.IEyeTrackingInfoRepository;
import com.ecoinsight.bdsp.asd.repository.IEyeTrackingRepository;
import com.ecoinsight.bdsp.core.service.ServiceException;
import com.google.gson.Gson;

@Transactional
@Service(EyeTrackingService.ID)
public class EyeTrackingService implements AsdSubjectService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    public static final String ID = "EyeTrackingService";

    @Autowired
    private IEyeTrackingRepository _repository;
    @Autowired
    private IEyeTrackingInfoRepository _infoRepository;
    @Autowired
    private DataCommonService _dataCommonService;

    @Transactional(propagation = Propagation.REQUIRED)
    public void create(EyeTracking tracking) throws Exception {
        try {
            this._repository.add(tracking);
        } catch (Exception e) {
            throw new Exception("Fail to create a tracking in database. - " + e.getMessage(), e);
        }
        List<EyeTrackingInfo> infos = tracking.getEyeTrackingInfos();
        LOGGER.debug("tracking={}", infos.toString());

        if (infos != null) {
            for (EyeTrackingInfo info : infos) {
                LOGGER.debug("EYETRACKING2={}", info.getAoi());
                info.setEyeTrackingId(tracking.getId());
                try {
                    this._infoRepository.add(info);
                } catch (Exception e) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    throw new Exception("Fail to create a tracking info in database. - " + e.getMessage(), e);
                }
            }
        }

        this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.Eyetracking, tracking.getSubjectId(),
                tracking.getProjectSeq(), tracking.getTrialIndex(), true);
    }

    public Optional<EyeTracking> getEyeTracking(String systemId, String subjectId, String type, long projectSeq,
            int trialIndex) {
        return this._repository.query1(systemId, subjectId, type, projectSeq, trialIndex);
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
        List<EyeTracking> result = this._repository.query2(systemId, subjectId, ProjectSeq.HOLD.getSeq()); // 앞
                                                                                                           // Controller에서
                                                                                                           // projectSeq=4
                                                                                                           // 일때만
                                                                                                           // dolabelling
                                                                                                           // 실행되므로 4 고정
        for (EyeTracking item : result) {
            this._repository.updateProjectSeq(item.getId(), projectSeq);
        }

        /*
         * List<EyeTracking> result = this._repository.query2(systemId, subjectId, 0);
         * if (!result.isEmpty()) {
         * EyeTracking lastItem = null;
         * EyeTracking secondToLastItem = null;
         * 
         * for (int i = 0; i < result.size(); i++) {
         * EyeTracking item = result.get(i);
         * 
         * if (i == result.size() - 1) {
         * // Last item
         * lastItem = item;
         * } else if (i == result.size() - 2) {
         * // Second-to-last item
         * secondToLastItem = item;
         * }
         * 
         * // Perform other operations on 'item' if needed
         * // this._repository.updateProjectSeq(item.getId(), projectSeq);
         * }
         * 
         * if (lastItem != null) {
         * this._repository.updateProjectSeq(lastItem.getId(), projectSeq);
         * }
         * if (secondToLastItem != null) {
         * this._repository.updateProjectSeq(secondToLastItem.getId(), projectSeq);
         * }
         * }
         */
    }

    public boolean download(File downloadDir, String systemId, String subjectId, long projectSeq, int trialIndex)
            throws Exception {
        List<EyeTracking> result = this._repository.query2(systemId, subjectId, projectSeq);
        if (result.size() == 0) {
            throw new Exception(
                    String.format("업로드된 시선처리 데이터가 없습니다. systemId=%s, subjectId=%s, projectSeq=%s, trialIndex=%s",
                            systemId, subjectId, projectSeq, trialIndex));
        }
        for (EyeTracking etracking : result) {
            List<EyeTrackingInfo> infos = this._infoRepository.findByEyeTrackingId(etracking.getId()); // 시선추적 디테일 인포
                                                                                                       // 테이블 select
            etracking.setEyeTrackingInfos(infos);
        }

        int count = 0;
        for (EyeTracking etracking : result) {
            EyeTrackingModel model = new EyeTrackingModel();
            model.setPatientSeq(etracking.getPatientSeq());
            model.setSubjectId(etracking.getSubjectId());
            model.setSetKey(etracking.getSetKey());
            model.setType(etracking.getType());
            model.setMeasureTime(etracking.getMeasureTime());
            model.setRegistDt(etracking.getRegistDt());
            model.setProjectSeq(etracking.getProjectSeq());
            model.setTrialIndex(etracking.getTrialIndex());
            model.setDeviceHeight(etracking.getDeviceHeight());
            model.setDeviceWidth(etracking.getDeviceWidth());

            List<EyeTrackingInfoModel> eyeTrackingInfos = new ArrayList<EyeTrackingInfoModel>();
            for (EyeTrackingInfo info : etracking.getEyeTrackingInfos()) {
                EyeTrackingInfoModel imodel = new EyeTrackingInfoModel();
                imodel.setGrade(info.getGrade());
                imodel.setSort(info.getSort());

                imodel.setAoi(this.toListOfDouble(info.getAoi()));
                imodel.setMedia(this.toListOfDouble(info.getMedia()));
                imodel.setTrackInfos(toTrackInfoModel(info.getTrackInfos()));

                eyeTrackingInfos.add(imodel);
            }
            model.setEyeTrackingInfos(eyeTrackingInfos);
            File targetFile = new File(downloadDir,
                    String.format("eyetracking_%s_%s_%s_%s.json", subjectId, projectSeq, trialIndex, model.getType()));
            this.writeJsonFile(model, targetFile);
            count++;
        }
        if (count == 0) {
            LOGGER.warn("There is no downloaded file.");
            downloadDir.delete();
            return false;
        }

        return true;
    }

    private void writeJsonFile(EyeTrackingModel model, File targetFile) throws Exception {
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

    private List<Double> toListOfDouble(String data) {
        if (data == null)
            return null;
        Gson gson = new Gson();
        Double[] ds = gson.fromJson(data, Double[].class);
        return Arrays.asList(ds);
    }

    private List<TrackInfoModel> toTrackInfoModel(String data) {
        if (data == null)
            return null;
        Gson gson = new Gson();
        TrackInfoModel[] models = gson.fromJson(data, TrackInfoModel[].class);
        return Arrays.asList(models);
    }

    public void changeProjectSeq(String systemId, String subjectId, long fromProjectSeq, long toProjectSeq)
            throws Exception {
        List<EyeTracking> result = this._repository.query2(systemId, subjectId, fromProjectSeq); // 앞 Controller에서
                                                                                                 // projectSeq=4 일때만
                                                                                                 // dolabelling 실행되므로 4
                                                                                                 // 고정
        for (EyeTracking item : result) {
            if (item.getProjectSeq() != toProjectSeq) {
                this._repository.updateProjectSeq(item.getId(), toProjectSeq);
            } else {
                return;
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void extracted(EyeTrackingRequestModel model, String type, EyeTracking eyeTracking) throws ServiceException {
        boolean results = false;
        boolean eyeinfo = false;
        // EyeTracking 객체가 존재하는 경우 eyetrackingId를 가져옴.
        long eyetrackingId = eyeTracking.getId();
        // eyetrackingId를 사용하여 삭제 작업을 수행.
        // this._service.deleteEyeTrackingById(eyetrackingId);

        try {
            eyeinfo = this._infoRepository.deleteEyeTrackingInfoByEyeTrackingId(model.getSubjectId(),
                    eyetrackingId, model.getProjectSeq(), model.getTrialIndex(), type);

        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    String.format("시선추적상세정보 삭제 실행작업을 실패하였습니다 - subjectId=%s, projectSeq=%s, trialIndex=%s",
                            model.getSubjectId(), model.getProjectSeq(), model.getTrialIndex()),
                    e);
        }

        try {
            results = this._repository.deleteEyeTrackingBySubjectId(model.getSubjectId(), eyetrackingId,
                    model.getProjectSeq(), model.getTrialIndex(), type);

        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    String.format("시선추적 데이터 삭제 실행작업을 실패하였습니다. - subjectId=%s, projectSeq=%s, trialIndex=%s",
                            model.getSubjectId(), model.getProjectSeq(), model.getTrialIndex()),
                    e);
        }
        if (eyeinfo && results) {
            // this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.VideoResource,
            // resource.getSubjectId(), resource.getProjectSeq(), resource.getTrialIndex(),
            // result);
            this._dataCommonService.updateDataSummaryColumn(DataSummaryColumn.Eyetracking, model.getSubjectId(),
                    model.getProjectSeq(), model.getTrialIndex(), false);
        }

    }

}
