package com.ecoinsight.bdsp.asd.web;

import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoinsight.bdsp.asd.Constants;
import com.ecoinsight.bdsp.asd.entity.DataSummary;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.VDownloadKeys;
import com.ecoinsight.bdsp.asd.repository.IAsdDataCommonRepoistory;
import com.ecoinsight.bdsp.asd.service.EyeTrackingService;
import com.ecoinsight.bdsp.asd.service.IFnirsResourceService;
import com.ecoinsight.bdsp.asd.service.IPupillometryService;
import com.ecoinsight.bdsp.asd.service.ImageResourceService;
import com.ecoinsight.bdsp.asd.service.MChartService;
import com.ecoinsight.bdsp.asd.service.MicrobiomeService;
import com.ecoinsight.bdsp.asd.service.VideoResourceService;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.util.ZipUtil;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

@RestController
public class DataSummaryController extends AsdBaseApiController {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Value("${ecoinsight.download-dir}")
    private String _downloadDir;

    @Autowired
    private IAsdDataCommonRepoistory _asdDataCommonRepository;

    @Autowired
    private IProjectRepository _projectRepository;

    @Resource(name = EyeTrackingService.ID)
    private EyeTrackingService _eyeTrackingService;
    @Resource(name = VideoResourceService.ID)
    private VideoResourceService _videoResourceService;
    @Resource(name = MChartService.ID)
    private MChartService _mchartService;
    @Resource(name = ImageResourceService.ID)
    private ImageResourceService _imageResourceService;
    @Autowired
    private IFnirsResourceService _fnirsService;
    @Autowired
    private IPupillometryService _pupillometryService;
    @Autowired
    private MicrobiomeService _microbiomeService;

    @GetMapping(path = "/api/v1/datasummary/q/")
    public ResponseEntity<JsonResponseObject> searchDataIntegration(
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
        String gender = null;
        boolean orgFiltered = false;
        int accessLevel = 0b0111111;

        if (params != null && !params.isEmpty()) {
            for (String k : params.keySet()) {
                Object v = params.get(k);

                if (k.equals("id") && v != null) {
                    // filterDoc.append("projectSeq", (projectId = Long.parseLong(v.toString())));
                    projectSeq = Long.parseLong(v.toString());

                    String rolename = "";
                    // 과제에서 부여된 사용자 권한 조회
                    final Optional<ProjectMember> researcherOptional = this._projectRepository
                            .findResearcher(projectSeq, userName);
                    if (researcherOptional.isEmpty()) {
                        rolename = super.getHighestRole(userName).getRoleId();
                    } else {
                        rolename = researcherOptional.get().getRoleId();
                    }

                    accessLevel = super.calculateAccessLevel(rolename);
                }
                if (k.equals("subject") && v != null) {
                    // filterDoc.append("subjectId", v.toString());
                    // dim.setSubjectId(v.toString());
                    subjectId = v.toString();
                }
                if (k.equals("org") && v != null) {
                    orgFiltered = true;
                    // filterDoc.append("orgId", v.toString());
                    // dim.setOrgId(v.toString());
                    orgId = v.toString();
                }

            }
            if (params.containsKey("gender")) {
                gender = params.get("gender").toString();
            }
        }

        // Admin, Manager, QC User는 모든 연구기관의 데이터 조회 가능.
        if (!super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel) && !super.canQC(accessLevel)
                && !orgFiltered) {
            final String org = super.getOrgId();
            // filterDoc.append("orgId", org);
            orgId = org;
        }

        List<DataSummary> list = _asdDataCommonRepository.findAll(subjectId, projectSeq, orgId, gender, page, offset);
        // 쿼리 페이징 처리 추가 할 것
        int count = _asdDataCommonRepository.countAll(subjectId, projectSeq, orgId, gender);

        // 나이별로 셋팅 추가
        list.forEach(result -> {

            if (result.getTrialIndex() == 1) {
                result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST,
                        result.getFirstSelection() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND,
                        result.getSecondSelection() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);

                if (result.getProjectSeq() == 1) {
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_M_CHAT,
                            result.getMchat() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_EYE_TRACKING,
                            result.getEyeTracking() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_VIDEO,
                            result.getVideoResource() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_AUDIO,
                            result.getVideoResource() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    // result.addAreaQcStatus(DataSummary.DATA_GROUP_VITAL_SIGNS,
                    //         result.getVitalSigns() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_PUPILLOMETRY,
                            result.getPupillometry() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_MICROBIOME,
                            result.getMicrobiome() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_BLOOD, Constants.IMAGING_QC_NO_SUBJECT);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_STOOL, Constants.IMAGING_QC_NO_SUBJECT);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_URINE, Constants.IMAGING_QC_NO_SUBJECT);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FNIRS,
                            result.getFnirs() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_EEG, Constants.IMAGING_QC_NO_SUBJECT);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_MRI,
                            result.getMri() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                } else {
                    // result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST,
                    // result.getFirstSelection() ? Constants.IMAGING_QC_DONE_QC :
                    // Constants.IMAGING_QC_NO_DATA);
                    // result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND,
                    // result.getSecondSelection() ? Constants.IMAGING_QC_DONE_QC :
                    // Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_M_CHAT,
                            result.getMchat() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_EYE_TRACKING,
                            result.getEyeTracking() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_VIDEO,
                            result.getVideoResource() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    // result.addAreaQcStatus(DataSummary.DATA_GROUP_VITAL_SIGNS,
                    //         result.getVitalSigns() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_AUDIO,
                            result.getVideoResource() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_PUPILLOMETRY,
                            result.getPupillometry() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_MICROBIOME,
                            result.getMicrobiome() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_BLOOD,
                            result.getBlood() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_STOOL,
                            result.getStool() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_URINE,
                            result.getUrine() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FNIRS,
                            result.getFnirs() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_EEG,
                            result.getEeg() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_MRI,
                            result.getMri() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                            result.addAreaQcStatus(DataSummary.DATA_GROUP_MICROBIOME,
                            result.getMicrobiome() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                }

            } else if (result.getTrialIndex() == 2) {
                // if(result.getProjectSeq() == 2 ||result.getProjectSeq() )
                if (1 == result.getProjectSeq()) {
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST, Constants.IMAGING_QC_NO_SUBJECT);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND, Constants.IMAGING_QC_NO_SUBJECT);

                } else if (result.getProjectSeq() == 4) {
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST, Constants.IMAGING_QC_NO_SUBJECT);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND, Constants.IMAGING_QC_NO_SUBJECT);

                } else {
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST,
                            result.getFirstSelection() ? Constants.IMAGING_QC_DONE_QC
                            : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND,
                            result.getSecondSelection() ? Constants.IMAGING_QC_NO_SUBJECT// 미등록
                            : Constants.IMAGING_QC_NO_DATA);
                }
                // result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST,
                // Constants.IMAGING_QC_NO_SUBJECT);
                // result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND,
                // Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_M_CHAT, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_EYE_TRACKING,
                        result.getEyeTracking() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_VIDEO,
                        result.getVideoResource() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                // result.addAreaQcStatus(DataSummary.DATA_GROUP_VITAL_SIGNS, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_AUDIO,
                        result.getVideoResource() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_PUPILLOMETRY,
                        result.getPupillometry() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_BLOOD, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_STOOL, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_URINE, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_FNIRS,
                        result.getFnirs() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_EEG,
                        result.getEeg() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_MRI,
                        result.getMri() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                        result.addAreaQcStatus(DataSummary.DATA_GROUP_MICROBIOME,
                        result.getMicrobiome() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);

            } else if (result.getTrialIndex() == 3) {
                if (1 == result.getProjectSeq()) {
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST, Constants.IMAGING_QC_NO_SUBJECT);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND, Constants.IMAGING_QC_NO_SUBJECT);

                } else if (result.getProjectSeq() == 4) {
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST, Constants.IMAGING_QC_NO_SUBJECT);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND, Constants.IMAGING_QC_NO_SUBJECT);

                } else {
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST,
                            result.getFirstSelection() ? Constants.IMAGING_QC_DONE_QC
                            : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND,
                            result.getSecondSelection() ? Constants.IMAGING_QC_NO_SUBJECT// 미등록
                            : Constants.IMAGING_QC_NO_DATA);
                }
                // result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST,
                // Constants.IMAGING_QC_NO_SUBJECT);
                // result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND,
                // result.getSecondSelection() ? Constants.IMAGING_QC_DONE_QC :
                // Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_M_CHAT, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_EYE_TRACKING,
                        result.getEyeTracking() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_VIDEO, Constants.IMAGING_QC_NO_SUBJECT);
                // result.addAreaQcStatus(DataSummary.DATA_GROUP_VITAL_SIGNS, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_AUDIO, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_PUPILLOMETRY,
                        result.getPupillometry() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_BLOOD, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_STOOL, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_URINE, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_FNIRS,
                        result.getFnirs() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_EEG, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_MRI,
                        result.getMri() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_MICROBIOME,
                        result.getMicrobiome() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
            } else {
                if (1 == result.getProjectSeq()) {
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST, Constants.IMAGING_QC_NO_SUBJECT);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND, Constants.IMAGING_QC_NO_SUBJECT);

                } else if (result.getProjectSeq() == 4) {
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST, Constants.IMAGING_QC_NO_SUBJECT);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND, Constants.IMAGING_QC_NO_SUBJECT);

                } else {
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST,
                            result.getFirstSelection() ? Constants.IMAGING_QC_DONE_QC
                            : Constants.IMAGING_QC_NO_DATA);
                    result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND,
                            result.getSecondSelection() ? Constants.IMAGING_QC_NO_SUBJECT// 미등록
                            : Constants.IMAGING_QC_NO_DATA);
                }
                // result.addAreaQcStatus(DataSummary.DATA_GROUP_FIRST,
                // Constants.IMAGING_QC_NO_SUBJECT);
                // result.addAreaQcStatus(DataSummary.DATA_GROUP_SECOND,
                // Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_M_CHAT, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_EYE_TRACKING,
                        result.getEyeTracking() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_VIDEO,
                        result.getVideoResource() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                // result.addAreaQcStatus(DataSummary.DATA_GROUP_VITAL_SIGNS, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_AUDIO,
                        result.getVideoResource() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_PUPILLOMETRY,
                        result.getPupillometry() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_BLOOD, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_STOOL, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_URINE, Constants.IMAGING_QC_NO_SUBJECT);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_FNIRS,
                        result.getFnirs() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_EEG,
                        result.getEeg() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_MRI,
                        result.getMri() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
                result.addAreaQcStatus(DataSummary.DATA_GROUP_MICROBIOME,
                        result.getMicrobiome() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);
            }

            Optional<LocalDate> bdoptional = DateUtil.parseSimple(result.getBirthday().trim());
            // if(bdoptional.isEmpty()) {
            // return ErrorResponse("Invalid birthday format. - "+ result.getBirthday());
            // }

            Optional<LocalDate> rdoptional = DateUtil.parseSimple(result.getRegistDate().trim());
            if (rdoptional.isEmpty()) {
                throw new RuntimeException(
                        String.format("대상자 %s 의 서명동의일 : %s -> 올바른 형식이 아니거나 서명동의일 등록되지 않은 데이터가 있습니다. : ",
                                result.getSubjectId(), result.getRegistDate()));
            }

            LocalDate birthDate = bdoptional.get();
            LocalDate registDate = rdoptional.get();
            Period diff = Period.between(birthDate, registDate);
            int months = diff.getYears() * 12 + diff.getMonths();

            result.setMonth(months);

            if (months < 18 || months > 30) {
                result.addAreaQcStatus(DataSummary.DATA_GROUP_M_CHAT, Constants.IMAGING_QC_NO_SUBJECT);
            }

            result.addAreaQcStatus(DataSummary.DATA_GROUP_ALL,
                    result.getAll() ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_DATA);

        });

        return OkResponseEntity("데이터 수집 현황을 조회했습니다.", new ListDataModel<>(list, count, page, offset));
    }

    @PutMapping(value = "/api/v1/datasummary/q/")
    public ResponseEntity<JsonResponseObject> updateDataSummaryValueForColumn(@RequestBody Map<String, Object> params) {

        final String column = params.containsKey("column") ? params.get("column").toString() : null;
        final String subjectId = params.containsKey("subjectId") ? params.get("subjectId").toString() : null;
        final long projectSeq = params.containsKey("projectSeq") ? Integer.parseInt(params.get("projectSeq").toString())
                : null;
        final int trialIndex = params.containsKey("trialIndex") ? Integer.parseInt(params.get("trialIndex").toString())
                : null;
        boolean is = params.containsKey("boolean") ? (Boolean) (params.get("boolean")) : false;

        if (column != null) {
            int result = _asdDataCommonRepository.updateDataSummaryValueForColumn(column, subjectId, projectSeq,
                    trialIndex, is);
            LOGGER.info(String.format("updated value for {%s} {%s} {%s} at %s ", subjectId, projectSeq, trialIndex,
                    column));
            return OkResponseEntity("정상 변경 되었습니다.");
        } else {
            return ErrorResponseEntity("정상 등록 되지 않았습니다.");
        }

    }

    @GetMapping(value = "/api/v1/sso/test/")
    public void createCookie(HttpServletResponse response) {

        // ResponseCookie resCookie = ResponseCookie.from("test", "value")
        // .httpOnly(false)
        // //.domain(".test.com") // 해당 도메인에서만 유효한 쿠키
        // .sameSite("None")
        // // .secure(true) // HTTPS가 적용된 요청에만 전송되는 쿠키
        // .path("/")
        // .maxAge(Math.toIntExact(1 * 24 * 60 * 60))
        // .build();
        // response.addHeader("Set-Cookie", resCookie.toString());
        String column = "FirstSelection";
        String subjectId = "1021040440";
        long projectSeq = 2L;
        int trialIndex = 1;
        boolean flag = true;

        int result = this._asdDataCommonRepository.updateDataSummaryColumn(column, subjectId, projectSeq, trialIndex,
                flag);
        System.out.println("변경 됨 : " + result);

    }

    // @GetMapping(value = "/api/v1/datasummary/q/del/")
    // public ResponseEntity<JsonResponseObject> getFindUpdatedTrialIndexNormal() {
    // try {
    // this._dataCommonService.getFindUpdatedTrialIndexNormal();
    // return OkResponseEntity("정상군 과제차수 업데이트 된 대상자를 삭제 완료 하였습니다.");
    // } catch (Exception ex) {
    // return ErrorResponseEntity(ex.getMessage(), ex);
    // }
    // }
    /**
     *
     * @return
     */
    @PostMapping("/api/v1/datasummary/download/{subjectId}/{projectSeq}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable String subjectId,
            @PathVariable long projectSeq, @RequestBody Map<String, String[]> map) {
        final String systemId = super.getSystemId();
        var subjects = this._subjectRepository.findAllBySubjectId(systemId, subjectId);
        if (subjects == null || subjects.size() <= 0) {
            throw new RuntimeException(String.format("대상자 정보를 찾을수 없습니다. - systemId=%s, subjectId=%s", systemId, subjectId));
        }
        LOGGER.info("-> " + map);
        AsdProjectSubject subject = subjects.stream().findFirst().get();
        projectSeq = subject.getProjectSeq();

        // create a root download directory
        File downloadDir = new File(this._downloadDir, "datasummary");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        // create a subject video directory for download
        String datetime = DateUtil.getShortDateTimeStampString();
        String targetDir = String.format("datasummary_%s_%s_%s", subjectId, projectSeq, datetime);
        File ftargetDir = new File(downloadDir, targetDir);
        if (ftargetDir.exists()) {
            ftargetDir.delete();
        }
        ftargetDir.mkdirs();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("created a download directory. - " + ftargetDir.getAbsolutePath());
        }
        // key : v1, v2, v3, v4
        Set<String> keys = map.keySet();
        for (String key : keys) {
            String[] values = map.get(key);
            if (values == null || values.length == 0) {
                continue;
            }

            LOGGER.info("# doDownload. subjectId=" + subjectId + ", projectSeq=" + projectSeq + ", version=" + key);
            try {
                doDownload(subjectId, projectSeq, key, values, ftargetDir);
            } catch (Exception e) {
                LOGGER.error("Fail to make download files. subjectId=" + subjectId + ", projectSeq=" + projectSeq
                        + ",version=" + key + ", error=" + e.getMessage(), e);
            }
        }
        // make a zip file from ftargetDir.
        try {
            // TODO recursive handleing below root directory.
            File zipFile = ZipUtil.zip(downloadDir, targetDir);
            ftargetDir.delete();
            FileSystemResource resource = new FileSystemResource(zipFile);
            HttpHeaders header = new HttpHeaders();
            header.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zipFile.getName());
            header.add("Cache-Control", "no-cache, no-store, must-revalidate");
            header.add("Pragma", "no-cache");
            header.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(header)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Fail to make a zip file. - " + e.getMessage(), e);
        }
    }

    private void doDownload(String subjectId, long projectSeq, String version, String[] items, File rootDir) {
        File vrootDir = new File(rootDir, version);
        vrootDir.mkdir();
        // for(VDownloadKeys vkey : keys) {
        for (String item : items) {
            VDownloadKeys vkey = VDownloadKeys.value(version, item);
            if (vkey == null) {
                throw new RuntimeException("Invalid data. version=" + version + ", key=" + item);
            }
            LOGGER.info("\t# doDownload. subjectId=" + subjectId + ", projectSeq=" + projectSeq + ", version=" + version + ", type=" + vkey);
            switch (vkey) {
                case MCHAT:
                    downloadMChat(subjectId, projectSeq, version, vkey, vrootDir);
                    break;
                case EYETRACKING:
                    downloadEyeTracking(subjectId, projectSeq, version, vkey, vrootDir);
                    break;
                case VIDEO:
                    downloadVideo(subjectId, projectSeq, version, vkey, vrootDir);
                    break;
                case FNIRS: // T_FNIRsResource
                    downloadFNIRs(subjectId, projectSeq, version, vkey, vrootDir);
                    break;
                case MRI:
                    downloadMRI(subjectId, projectSeq, version, vkey, vrootDir);
                    break;
                case PUPILLOMETRY:
                    downloadPupillometry(subjectId, projectSeq, version, vkey, vrootDir);
                    break;
                case MICROBIOME:
                    downloadMicrobiome(subjectId, projectSeq, version, vkey, vrootDir);
                    break;
                default:
                    break;
            }
        }
    }

    private void downloadEyeTracking(String subjectId, long projectSeq, String version, VDownloadKeys dkey,
            File rootDir) {
        final String systemId = super.getSystemId();
        File troot = new File(rootDir, dkey.name().toLowerCase());
        troot.mkdir();
        int trialIndex = dkey.version(version);
        try {
            boolean result = this._eyeTrackingService.download(troot, systemId, subjectId, projectSeq, trialIndex);
            if (!result) {
                LOGGER.error("다운로드 할 데이터가 없습니다. type=" + dkey + ", subjectId=" + subjectId + ", projectSeq="
                        + projectSeq + ",trialIndex=" + trialIndex);
            }
        } catch (Exception e) {
            LOGGER.error("Fail to download eyetracking data. subjectId=" + subjectId + ", projectSeq=" + projectSeq
                    + ",trialIndex=" + trialIndex + ", error=" + e.getMessage(), e);
        }
    }

    private void downloadMChat(String subjectId, long projectSeq, String version, VDownloadKeys dkey, File rootDir) {
        final String systemId = super.getSystemId();
        File troot = new File(rootDir, dkey.name().toLowerCase());
        troot.mkdir();
        int trialIndex = dkey.version(version);
        try {
            File result = this._mchartService.download(troot, systemId, subjectId, projectSeq, trialIndex);
            if (result == null) {
                LOGGER.error("다운로드 할 데이터가 없습니다. type=" + dkey + ", subjectId=" + subjectId + ", projectSeq="
                        + projectSeq + ",trialIndex=" + trialIndex);
            }
        } catch (Exception e) {
            LOGGER.error("Fail to download mchart data. subjectId=" + subjectId + ", projectSeq=" + projectSeq
                    + ",trialIndex=" + trialIndex + ", error=" + e.getMessage(), e);
        }
    }

    private void downloadVideo(String subjectId, long projectSeq, String version, VDownloadKeys dkey, File rootDir) {
        final String systemId = super.getSystemId();
        File troot = new File(rootDir, dkey.name().toLowerCase());
        troot.mkdir();
        int trialIndex = dkey.version(version);
        try {
            boolean result = this._videoResourceService.download(troot, systemId, subjectId, projectSeq, trialIndex);
            if (!result) {
                LOGGER.error("다운로드 할 데이터가 없습니다. type=" + dkey + ", subjectId=" + subjectId + ", projectSeq="
                        + projectSeq + ",trialIndex=" + trialIndex);
            }
        } catch (Exception e) {
            LOGGER.error("Fail to download video resource data. subjectId=" + subjectId + ", projectSeq=" + projectSeq
                    + ",trialIndex=" + trialIndex + ", error=" + e.getMessage(), e);
        }
    }

    private void downloadMRI(String subjectId, long projectSeq, String version, VDownloadKeys dkey, File rootDir) {
        final String systemId = super.getSystemId();
        File troot = new File(rootDir, dkey.name().toLowerCase());
        troot.mkdir();
        int trialIndex = dkey.version(version);

        try {
            boolean result = this._imageResourceService.download(troot, systemId, subjectId, projectSeq, trialIndex);
            if (!result) {
                LOGGER.error("다운로드 할 데이터가 없습니다. type=" + dkey + ", subjectId=" + subjectId + ", projectSeq="
                        + projectSeq + ",trialIndex=" + trialIndex);
            }
        } catch (Exception e) {
            LOGGER.error("Fail to download MRI resource data. subjectId=" + subjectId + ", projectSeq=" + projectSeq
                    + ",trialIndex=" + trialIndex + ", error=" + e.getMessage(), e);
        }
    }

    private void downloadFNIRs(String subjectId, long projectSeq, String version, VDownloadKeys dkey, File rootDir) {
        final String systemId = super.getSystemId();
        File troot = new File(rootDir, dkey.name().toLowerCase());
        troot.mkdir();
        int trialIndex = dkey.version(version);

        try {
            boolean result = this._fnirsService.download(troot, systemId, subjectId, projectSeq, trialIndex);
            if (!result) {
                LOGGER.error("다운로드 할 데이터가 없습니다. type=" + dkey + ", subjectId=" + subjectId + ", projectSeq="
                        + projectSeq + ",trialIndex=" + trialIndex);
            }
        } catch (Exception e) {
            LOGGER.error("Fail to download fNIRs resource data. subjectId=" + subjectId + ", projectSeq=" + projectSeq
                    + ",trialIndex=" + trialIndex + ", error=" + e.getMessage(), e);
        }
    }

    private void downloadPupillometry(String subjectId, long projectSeq, String version, VDownloadKeys dkey, File rootDir) {
        final String systemId = super.getSystemId();
        File troot = new File(rootDir, dkey.name().toLowerCase());
        troot.mkdir();
        int trialIndex = dkey.version(version);

        try {
            boolean result = this._pupillometryService.download(troot, systemId, subjectId, projectSeq, trialIndex);
            if (!result) {
                LOGGER.error("다운로드 할 데이터가 없습니다. type=" + dkey + ", subjectId=" + subjectId + ", projectSeq="
                        + projectSeq + ",trialIndex=" + trialIndex);
            }
        } catch (Exception e) {
            LOGGER.error("Fail to download pupilemetry resource data. subjectId=" + subjectId + ", projectSeq=" + projectSeq
                    + ",trialIndex=" + trialIndex + ", error=" + e.getMessage(), e);
        }
    }

    private void downloadMicrobiome(String subjectId, long projectSeq, String version, VDownloadKeys dkey, File rootDir) {
        final String systemId = super.getSystemId();
        File troot = new File(rootDir, dkey.name().toLowerCase());
        troot.mkdir();
        int trialIndex = dkey.version(version);

        try {
            boolean result = this._microbiomeService.download(troot, systemId, subjectId, projectSeq, trialIndex);
            if (!result) {
                LOGGER.error("다운로드 할 데이터가 없습니다. type=" + dkey + ", subjectId=" + subjectId + ", projectSeq="
                        + projectSeq + ",trialIndex=" + trialIndex);
            }
        } catch (Exception e) {
            LOGGER.error("Fail to download microbiome resource data. subjectId=" + subjectId + ", projectSeq=" + projectSeq
                    + ",trialIndex=" + trialIndex + ", error=" + e.getMessage(), e);
        }
    }
}
