package com.ecoinsight.bdsp.asd.web;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoinsight.bdsp.asd.entity.Selection;
import com.ecoinsight.bdsp.asd.service.SelectionService;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecoinsight.bdsp.asd.entity.SurveyStatus;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;

@RestController
@RequestMapping(path = "/api/v1/selection")
public class SelectionController extends AsdBaseApiController {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Resource(name = SelectionService.ID)
    private SelectionService _service;

    @Autowired
    private IProjectRepository _projectRepository;

    @ApiOperation(value = "임상/선별 수집 유무 조회 API", notes = "대상자의 임상 설문 수집 유무를 DB에서 조회")
    @GetMapping(value = "/result")
    public ResponseEntity<JsonResponseObject> getSelection(
            @RequestParam Map<String, Object> params) {
        final String systemId = super.getSystemId();
        final String userName = super.getAuthenticatedUsername();

        int page = 1;
        int offset = 20;
        if (params.containsKey("page")) {
            try {
                page = Integer.parseInt(params.get("page").toString());
                if (page <= 0 || page > Integer.MAX_VALUE) {
                    page = 1;
                }
            } catch (NumberFormatException pex) {
                page = 1;
            }
        }
        if (params.containsKey("offset")) {
            try {
                offset = Integer.parseInt(params.get("offset").toString());
                if (offset <= 0 || offset > ListDataModel.MAX_ROW_COUNT) {
                    offset = ListDataModel.DEFAULT_ROW_COUNT;
                }
            } catch (NumberFormatException pex) {
                offset = 20;
            }
        }

        String subjectId = null;
        long projectSeq = 0;
        String orgId = null;

        boolean orgFiltered = false;
        int accessLevel = 0b0111111;

        if (params != null && !params.isEmpty()) {
            for (String k : params.keySet()) {
                Object v = params.get(k);

                if (k.equals("id") && v != null) {
                    projectSeq = Long.parseLong(v.toString());

                    String rolename = "";

                    final Optional<ProjectMember> researcherOptional = this._projectRepository.findResearcher(projectSeq, userName);
                    if (researcherOptional.isEmpty()) {
                        rolename = super.getHighestRole(userName).getRoleId();
                    } else {
                        rolename = researcherOptional.get().getRoleId();
                    }

                    accessLevel = super.calculateAccessLevel(rolename);
                }
                if (k.equals("subject") && v != null) {
                    subjectId = v.toString();
                }
                if (k.equals("org") && v != null) {
                    orgFiltered = true;
                    orgId = v.toString();
                }
            }
        }

        if (!super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel) && !super.canQC(accessLevel) && !orgFiltered) {
            final String org = super.getOrgId();
            orgId = org;
        }

        List<SurveyStatus> list = this._service.getSelectionAll(systemId, subjectId, projectSeq, orgId, page, offset);
        int count = this._service.countAll(systemId, subjectId, projectSeq, orgId);

        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        for (SurveyStatus selection : list) {
            Optional<LocalDate> bdoptional = DateUtil.parseSimple(selection.getBirthDay());
            Optional<LocalDate> rdoptional = DateUtil.parseSimple(LocalDate.now(zoneId).toString());
            LocalDate birthDate = bdoptional.get();
            LocalDate registDate = rdoptional.get();
            Period diff = Period.between(birthDate, registDate);
            int months = diff.getYears() * 12 + diff.getMonths();

            selection.setMonth(months);

        }

        return OkResponseEntity("선벌/검사 데이터를 조회했습니다.", new ListDataModel<>(list, count, page, offset));
    }

    @ApiOperation(value = "설문지 수집데이터 state 수집유무 조회 & 업데이트", notes = "설문지 수집데이터state 수집유무 조회 & 업데이트")
    @GetMapping("/result/surveystate/temp")
    public ResponseEntity<JsonResponseObject> findBySurveyStateUpdateTemp(
            @RequestParam(required = false) String subjectId, @RequestParam(required = false) Long projectSeq) {
        final String systemId = getSystemId();
        final String worker = getAuthenticatedUsername();

        try {
            Collection<AsdProjectSubject> subjects = null;

            // subjectId 또는 projectSeq에 따라 데이터 조회
            if (subjectId != null) {
                subjects = _subjectRepository.findAllBySubjectId(systemId, subjectId);
            } else if (projectSeq != 0) {
                subjects = _subjectRepository.findAllByProjectSeq(systemId, projectSeq);
            }

            if (subjects == null || subjects.isEmpty()) {
                LOGGER.error("대상자를 찾을 수 없습니다. systemId={}, subjectId={}, projectSeq={}", systemId, subjectId, projectSeq);
                return ErrorResponseEntity(
                        String.format("대상자를 찾을 수 없습니다. - systemId=%s, subjectId=%s, projectSeq=%s", systemId, subjectId, projectSeq));
            }

            // 조회된 subjects 처리
            // ;
            // subjects.forEach(entry -> {
            //     List<SurveyStatus> list = _repoistory.findSurveyStatusbySubject(entry.getSubjectId());
            //     LOGGER.info("Retrieved survey statuses for subjectId={}: {}", entry.getSubjectId(), list);
            //     this.service.findSurveyDataBySubject(list); // 데이터 처리
            //     var result = this.service.findSurveyDataBySubject(list);
            //     processedData.addAll(result);
            // });
            Collection<List<Map<String, Object>>> processedData = this._service.getSelectionAlltemp(systemId, subjectId, projectSeq);

            return ResponseEntity.ok()
                    .body(new JsonResponseObject(true, "데이터를 조회했습니다.", processedData));

        } catch (Exception e) {
            LOGGER.error("Error occurred while processing survey data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JsonResponseObject(false, "데이터 조회 중 오류가 발생했습니다."));
        }
    }

}
