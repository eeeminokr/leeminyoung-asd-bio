package com.ecoinsight.bdsp.asd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.ecoinsight.bdsp.asd.entity.DashBoard;
import com.ecoinsight.bdsp.asd.repository.IAsdDataCommonRepoistory;
import com.ecoinsight.bdsp.core.web.BaseApiController;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

/**
 * 임상 실험 참여 대상자 관리 기능.
 */
@RestController
public class HomeController extends BaseApiController {
    @Autowired
    private IAsdDataCommonRepoistory dataSummaryRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @GetMapping(path = "/api/v1/dashboard/data-state")
    public ResponseEntity<JsonResponseObject> getSubjectGenderSummary() {

        List<DashBoard> list = this.dataSummaryRepository.getTotalDataSummaryState();
        List<Map<String, Object>> results = new ArrayList<>();

        if (list.isEmpty()) {
            LOGGER.error(String.format("No found trial items. [list=%s]", list));
        }

        for (DashBoard dashBoard : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("Year", dashBoard.getYear());
            item.put("Month", dashBoard.getMonth());
            item.put("ProjectSeq", dashBoard.getProjectSeq());
            item.put("Count", dashBoard.getCount());
            results.add(item);
        }

        return OkResponseEntity("대상자 성별통계를 조회했습니다.", results);
    }

    @GetMapping(path = "/api/v1/dashboard/data-state/{id}")
    public ResponseEntity<JsonResponseObject> getDataSummarybyOrgId(
            @PathVariable(required = true, name = "id") long projectSeq) {

        List<DashBoard> list = this.dataSummaryRepository.findDataSummarybyOrgId(projectSeq);
        List<Map<String, Object>> results = new ArrayList<>();

        if (list.isEmpty()) {
            LOGGER.error(String.format("No found trial items. [list=%s]", list));
        }

        for (DashBoard dashBoard : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("OrgId", dashBoard.getOrgId());
            item.put("OrgName", dashBoard.getOrgName());
            item.put("Count", dashBoard.getCount());
            item.put("ProjectSeq", dashBoard.getProjectSeq());

            results.add(item);
        }

        return OkResponseEntity("기관별 대상군통계를 조회했습니다.", results);
    }

    @GetMapping(path = "/api/v1/dashboard/trialdata-state/{index}")
    public ResponseEntity<JsonResponseObject> getTrialInfoStateSurmmary(
            @PathVariable(required = true, name = "index") int trialIndex) {

        List<DashBoard> mapList = this.dataSummaryRepository.findAllTrialInfoStateSurmmary(trialIndex);
        mapList.stream()
                .map(dashBoard -> {
                    int registerCount = dashBoard.getTotalStateCount() + dashBoard.getTotalNotStateCount();
                    dashBoard.setTotalRegisterStateCount(registerCount);
                    return dashBoard;
                })
                .collect(Collectors.toList());

        // Aggregate by orgId and projectSeq
        Map<String, Map<Long, DashBoard>> aggregatedByOrgIdAndProjectSeq = mapList.stream()
                .collect(Collectors.groupingBy(
                        DashBoard::getOrgId,
                        Collectors.groupingBy(
                                DashBoard::getProjectSeq,
                                Collectors.collectingAndThen(
                                        Collectors.reducing((d1, d2) -> {
                                            d1.setTotalRegisterStateCount(
                                                    d1.getTotalRegisterStateCount() + d2.getTotalRegisterStateCount());
                                            d1.setTotalStateCount(d1.getTotalStateCount() + d2.getTotalStateCount());
                                            d1.setTotalNotStateCount(
                                                    d1.getTotalNotStateCount() + d2.getTotalNotStateCount());
                                            return d1;
                                        }),
                                        Optional::get))));

        // Print aggregated results by orgId and projectSeq
        aggregatedByOrgIdAndProjectSeq.forEach((orgId, projectSeqMap) -> {
            projectSeqMap.forEach((projectSeq, dashboard) -> {
                LOGGER.info(String.format(
                        "OrgId: %s, ProjectSeq: %d, TotalRegisterStateCount: %d, TotalStateCount: %d, TotalNotStateCount: %d",
                        orgId, projectSeq, dashboard.getTotalRegisterStateCount(), dashboard.getTotalStateCount(),
                        dashboard.getTotalNotStateCount()));
            });
        });

        // Prepare the response data
        Map<String, Object> mergedValues = new HashMap<>();
        mergedValues.put("orgIdDashBoard", aggregatedByOrgIdAndProjectSeq);

        LOGGER.info("Merged Values: {}", mergedValues);

        return ResponseEntity.ok(new JsonResponseObject(true, mergedValues));
    }
}
