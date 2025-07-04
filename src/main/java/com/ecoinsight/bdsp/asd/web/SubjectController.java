package com.ecoinsight.bdsp.asd.web;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.batik.css.engine.value.StringValue;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecoinsight.bdsp.asd.Constants;
import com.ecoinsight.bdsp.asd.OmniApi;
import com.ecoinsight.bdsp.asd.entity.DataSummary;
import com.ecoinsight.bdsp.asd.model.AsdProjectSubject;
import com.ecoinsight.bdsp.asd.model.DataTable;
import com.ecoinsight.bdsp.asd.model.McdSubjectModel;
import com.ecoinsight.bdsp.asd.model.OmniSubject;
import com.ecoinsight.bdsp.asd.model.ProjectSeq;
import com.ecoinsight.bdsp.asd.model.ProjectSubjectModel;
import com.ecoinsight.bdsp.asd.model.Result;
import com.ecoinsight.bdsp.asd.model.Subject;
import com.ecoinsight.bdsp.asd.model.SubjectFileUploadResult;
import com.ecoinsight.bdsp.asd.model.SubjectState;
import com.ecoinsight.bdsp.asd.repository.IAsdDataCommonRepoistory;
import com.ecoinsight.bdsp.asd.repository.IAsdProjectSubjectRepository;
import com.ecoinsight.bdsp.asd.scheduling.utils.CallRestApi;
import com.ecoinsight.bdsp.asd.scheduling.utils.TextMessageSender;
import com.ecoinsight.bdsp.asd.service.EyeTrackingService;
import com.ecoinsight.bdsp.asd.service.FilePathGenerator;
import com.ecoinsight.bdsp.asd.service.IVideoResourceUpdateService;
import com.ecoinsight.bdsp.asd.service.MChartService;
import com.ecoinsight.bdsp.asd.service.SurveyStatusService;
import com.ecoinsight.bdsp.asd.service.VideoResourceService;
import com.ecoinsight.bdsp.core.MemberActivityConstants;
import com.ecoinsight.bdsp.core.crypto.IBinaryEncryptor;
import com.ecoinsight.bdsp.core.entity.MemberRole;
import com.ecoinsight.bdsp.core.entity.MongoKeyGenerator;
import com.ecoinsight.bdsp.core.entity.Organization;
import com.ecoinsight.bdsp.core.entity.Project;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.entity.ProjectSubject;
import com.ecoinsight.bdsp.core.entity.ProjectSubjectFile;
import com.ecoinsight.bdsp.core.lib.apache.poi.CellException;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.model.ModelValidationException;
import com.ecoinsight.bdsp.core.repository.IOrganizationRepository;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.repository.IRoleRepository;
import com.ecoinsight.bdsp.core.repository.mongo.IProjectSubjectRepository;
import com.ecoinsight.bdsp.core.service.ITrialService;
import com.ecoinsight.bdsp.core.service.PdfFileEncryptor;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.util.FileUtil;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;
import com.google.gson.Gson;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 임상 실험 참여 대상자 관리 기능.
 */

@RestController
public class SubjectController extends AsdBaseApiController {
    @Autowired
    private IOrganizationRepository _organizationRepository;

    @Autowired
    private IProjectSubjectRepository _projectSubjectRepository;

    @Autowired
    private IAsdProjectSubjectRepository _asdProjectSubjectRepository;

    @Autowired
    private IProjectRepository _projectRepository;

    @Autowired
    private IRoleRepository _roleRepository;

    @Autowired
    private MongoTemplate _mongoTemplate;

    @Autowired
    @Qualifier("trialService")
    private ITrialService _trialService;

    @Autowired
    @Qualifier("filePathGenerator")
    private FilePathGenerator _filePathGenerator;

    @Autowired
    private CallRestApi _callRestApi;

    @Value("${omni.host}")
    private String omniHost;

    @Autowired
    private TextMessageSender _textMessageSender;

    @Autowired
    private IAsdDataCommonRepoistory _asdDataCommonRepository;

    @Autowired
    private EyeTrackingService _EyeTrackingService;

    @Autowired
    private MChartService _MChartService;

    @Autowired
    private VideoResourceService _videoResourceService;

    @Autowired
    private SurveyStatusService _surveyStatusService;

    @Autowired
    private IVideoResourceUpdateService _IVideoResourceUpdateService;
    /**
     * PDF 파일 암호화, 복호화
     */
    private final static IBinaryEncryptor encryptor = new PdfFileEncryptor();
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @ApiIgnore
    @GetMapping(path = "/api/v1/subjects/")
    public ResponseEntity<JsonResponseObject> getSubjects(
            @RequestParam Map<String, Object> params) {
        final String systemId = super.getSystemId();
        final String userName = super.getAuthenticatedUsername();

        final MemberRole role = super.getHighestRole(userName);

        var canQueryAll = false;
        var canQueryOrg = false;

        var levels = role.getRoleLevel();

        // var role = this._roleRepository.findByRoleId(rolename, systemId);

        // var level = role.get().getLevel();
        LOGGER.debug("levels={}", levels);

        canQueryAll = super.canQueryAll(levels);
        canQueryOrg = super.canQueryOrg(levels); // ueryAllOrgPermissionGranted(int level)

        /*
         * Mongo DB Aggregation - Match
         */
        Document filterDoc = new Document("systemId", systemId);
        boolean orgFiltered = false;
        if (params.containsKey("id")) {
            filterDoc.append("projectSeq", Long.parseLong(params.get("id").toString()));
        } else {
            if (!super.canQueryAll(role)) {
                final String org = super.getOrgId();

                List<Project> userProjects = this._projectRepository.findAll(systemId, org,
                        super.canQueryOrg(role) ? null : userName, null);
                List<Long> ids = new ArrayList<Long>();
                if (userProjects != null && userProjects.size() > 0) {
                    userProjects.forEach(p -> {
                        ids.add(p.getProjectSeq());
                    });
                }
                filterDoc.append("projectSeq", new Document("$in", ids));
            }
        }

        if (params.containsKey("subject")) {
            filterDoc.append("subjectId", params.get("subject").toString());
        }

        if (params.containsKey("org")) {
            filterDoc.append("orgId", params.get("org").toString());
            orgFiltered = true;
        }

        // Filter org if the user is an enduser. (Enduser can see only trials which the
        // user's organization joined.)
        if (!super.canQueryAll(role) && !super.canQueryOrg(role) && !orgFiltered) {
            final String org = super.getOrgId();
            filterDoc.append("orgId", org);
        }

        Document matchDoc = new Document("$match", filterDoc);
        var agg = Arrays.asList(
                matchDoc,
                new Document("$sort",
                        new Document("subjectId", 1L)));

        List<McdSubjectModel> list = new ArrayList<McdSubjectModel>();
        this._mongoTemplate
                .getMongoDatabaseFactory()
                .getMongoDatabase()
                .getCollection("projectSubject")
                .aggregate(agg, McdSubjectModel.class)
                .forEach(val -> {
                    list.add(val);
                });

        return OkResponseEntity("대상자 정보를 조회했습니다.", list);
    }

    @ApiIgnore
    @GetMapping(path = "/api/v1/subjects/q/")
    public ResponseEntity<JsonResponseObject> searchSubjects(
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

        /*
         * Mongo DB Aggregation - Match
         */
        Document filterDoc = new Document("systemId", systemId);
        boolean orgFiltered = false;
        int accessLevel = 0b0111111;

        var canQueryAll = false;
        var canQueryOrg = false;

        if (params.containsKey("id")) {

            final long projectId = Long.parseLong(params.get("id").toString());
            filterDoc.append("projectSeq", projectId);

            // 과제에서 부여된 사용자 권한 조회
            String rolename = "";
            final Optional<ProjectMember> researcherOptional = this._projectRepository.findResearcher(projectId,
                    userName);

            LOGGER.debug("researcherOptional={}", researcherOptional);
            if (researcherOptional.isEmpty()) {
                rolename = super.getHighestRole(userName).getRoleId();
            } else {
                rolename = researcherOptional.get().getRoleId();
            }

            accessLevel = super.calculateAccessLevel(rolename);
            LOGGER.debug("rolename={}", rolename);
            LOGGER.debug("accessLevel={}", accessLevel);
            var role = this._roleRepository.findByRoleId(rolename, systemId);

            var level = role.get().getLevel();
            LOGGER.debug("level={}", level);

            canQueryAll = super.canQueryAll(level);
            canQueryOrg = super.canQueryOrg(level); // ueryAllOrgPermissionGranted(int level)

        } else {
            if (!super.canQueryAll(accessLevel)) {

                final String org = super.getOrgId();

                List<Project> userProjects = this._projectRepository.findAll(systemId, org,
                        super.canQueryOrg(accessLevel) ? null : userName, null);
                List<Long> ids = new ArrayList<Long>();
                if (userProjects != null && userProjects.size() > 0) {
                    userProjects.forEach(p -> {
                        ids.add(p.getProjectSeq());
                    });
                }
                filterDoc.append("projectSeq", new Document("$in", ids));
            }
        }

        if (params.containsKey("subject")) {
            filterDoc.append("subjectId", params.get("subject").toString());
        }

        if (params.containsKey("gender")) {
            filterDoc.append("gender", params.get("gender").toString());
        }
        if (params.containsKey("org")) {
            filterDoc.append("orgId", params.get("org").toString());
            orgFiltered = true;
        }

        // Filter org if the user is an enduser. (Enduser can see only trials which the
        // user's organization joined.)

        // if(!canQueryAll && !canQueryOrg && !orgFiltered){
        if (!super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel) && !orgFiltered) {
            final String org = super.getOrgId();
            filterDoc.append("orgId", org);
        }
        // filterDoc.append("subjectId", new Document("$not", ".*230101.*"));
        Document matchDoc = new Document("$match", filterDoc);
        var agg = Arrays.asList(
                matchDoc,
                new Document("$sort",
                        new Document("subjectId", 1L)),
                new Document("$skip", (page - 1) * offset),
                new Document("$limit", offset)

        );

        List<McdSubjectModel> list = new ArrayList<McdSubjectModel>();
        this._mongoTemplate
                .getMongoDatabaseFactory()
                .getMongoDatabase()
                .getCollection("projectSubject")
                .aggregate(agg, McdSubjectModel.class)
                .forEach(val -> {
                    list.add(val);
                });

        /*
         * Get Total (SubjectId)
         * [{$match: {
         * projectSeq: 1
         * }}, {$count: 'subjectId'}]
         * 
         * Arrays.asList(new Document("$match",
         * new Document("projectSeq", 1L)),
         * new Document("$count", "subjectId"))
         */
        var countAgg = Arrays.asList(
                matchDoc,
                new Document("$count", "subjectId"));

        final Document doc = this._mongoTemplate
                .getMongoDatabaseFactory()
                .getMongoDatabase()
                .getCollection("projectSubject")
                .aggregate(countAgg)
                .first();
        final long count = doc == null ? 0 : doc.getInteger("subjectId");

        // 사용자 활동 로그 기록
        writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_SUBJECT_SEARCH);

        return OkResponseEntity("대상자 정보를 조회했습니다.", new ListDataModel<McdSubjectModel>(list, count, page, offset));
    }

    @ApiIgnore
    @GetMapping(path = "/api/v1/projects/{id}/subjects/{subject}/")
    public ResponseEntity<JsonResponseObject> getSubject(
            @PathVariable("id") long projectId,
            @PathVariable(value = "subject") String subjectId) {
        final String systemId = super.getSystemId();

        /*
         * 대상자 ID가 명명규칙에 맞는지 체크
         */
        var subjectIdCheckResult = new SubjectIdNamingPolicy().passed(subjectId);
        if (!subjectIdCheckResult.isPassed()) {
            return ErrorResponseEntity(
                    subjectIdCheckResult.getErrors().stream().reduce((t, u) -> t + ", " + u).orElse(""));
        }

        var subjects = this._projectSubjectRepository.findAllBySubjectId(systemId, subjectId);
        if (subjects != null && subjects.size() > 0) {
            return OkResponseEntity("대상자를 조회했습니다.", subjects);
        }

        return OkResponseEntity("대상자가 존재하지 않습니다.");
    }

    @ApiIgnore
    @PostMapping(path = "/api/v1/projects/{id}/subjects/")
    public ResponseEntity<JsonResponseObject> addNewSubject(
            @PathVariable("id") long projectId,
            @RequestBody ProjectSubjectModel model) {
        try {
            final String systemId = super.getSystemId();
            final String worker = super.getAuthenticatedUsername();
            final String orgId = model.getOrgId();

            model.throwIfNotValid();

            /*
             * 대상자 ID가 명명규칙에 맞는지 체크
             */
            var subjectIdCheckResult = new SubjectIdNamingPolicy().passed(model.getSubjectId());
            if (!subjectIdCheckResult.isPassed()) {
                return ErrorResponseEntity(
                        subjectIdCheckResult.getErrors().stream().reduce((t, u) -> t + ", " + u).orElse(""));
            }

            Optional<Organization> orgOptional = this._organizationRepository.findById(orgId, systemId);
            if (orgOptional.isEmpty()) {
                return ErrorResponseEntity(String.format("No found organization. (id=%s)", orgId));
            }

            Organization org = orgOptional.get();

            if (!org.isActiveRecord()) {
                return ErrorResponseEntity(String.format("기관상태가 사용가능한 상태가 아닙니다. (상태:%s)", org.getRecordStatus()));
            }
            if (!org.isClientAccessible()) {
                return ErrorResponseEntity(String.format("사용자에게 접근이 허가된 기관이 아닙니다. (사용:%s)", org.getUsage()));
            }

            final String orgName = org.getOrgName();

            Optional<Project> projectOptional = this._projectRepository.findById(model.getProjectSeq(), systemId);
            if (projectOptional.isEmpty()) {
                return ErrorResponseEntity("과제 정보가 존재 하지않습니다.");
            }

            final Project project = projectOptional.get();

            // Check if subject id is unique in a project.
            final String subjectId = model.getSubjectId().toUpperCase();
            var subjects = this._projectSubjectRepository.findAllBySubjectId(systemId, subjectId);
            if (!subjects.isEmpty()) {
                var subjectOptional = subjects.stream().filter(p -> p.getProjectSeq() == project.getProjectSeq())
                        .findFirst();
                if (subjectOptional.isPresent() && subjectOptional.get().getProjectSeq() == model.getProjectSeq()) {
                    return ErrorResponseEntity("동일한 대상자 ID가 존재합니다.");
                }
            }

            var newSubject = new ProjectSubject();
            newSubject.setSubjectId(subjectId);
            newSubject.setActualBirthday(model.getBirthDay());
            newSubject.setGender(model.getGender());

            // 등록일 기준 대상자의 나이 계산
            if (StringUtils.hasText(newSubject.getActualBirthday())) {
                final int age = ProjectSubject.calculateAge(newSubject.getActualBirthday(), LocalDate.now());
                newSubject.setAge(age);
            }

            // Save subject id as uppercase.
            newSubject.setId(
                    MongoKeyGenerator.generateProjectSubjectId(systemId, orgId, model.getProjectSeq(), subjectId));
            newSubject.setProjectSeq(model.getProjectSeq());
            newSubject.setProjectName(project.getProjectName());
            newSubject.setOrgId(orgId);
            newSubject.setOrgName(orgName);
            newSubject.setSystemId(project.getSystemId());

            newSubject.setDateCreated(Calendar.getInstance().getTime());
            newSubject.setUserCreated(worker);
            newSubject.setDataSource(SubjectController.class.getName());

            var added = this._projectSubjectRepository.save(newSubject);

            // 사용자 활동 로그 기록
            // writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA,
            // MemberActivityConstants.ACT_SUBJECT_ADD, String.format("과제 %s 에 대상자 %s 추가",
            // projectId, subjectId));

            return OkResponseEntity("대상자를 추가했습니다.", added);
        } catch (ModelValidationException ex) {
            return ErrorResponseEntity(ex.getErrorMessages(), ex);
        } catch (Exception ex) {
            return ErrorResponseEntity(ex.getMessage(), ex);
        }
    }

    @ApiIgnore
    @PutMapping(path = "/api/v1/projects/{id}/subjects/{subjectId}/")
    public ResponseEntity<JsonResponseObject> changeSubject(
            final @PathVariable("id") long projectId,
            final @PathVariable("subjectId") String subjectId,
            @RequestBody ProjectSubjectModel model) throws Exception {
        final String systemId = super.getSystemId();

        var subjects = this._asdProjectSubjectRepository.findBySubjectId(systemId, projectId, subjectId);
        if (subjects == null || subjects.isEmpty()) {
            return ErrorResponseEntity("대상자 정보가 존재하지 않습니다.");
        }
        if (subjects.size() > 1) {
            return ErrorResponseEntity("대상자가 다수 존재하여 변경할 수 없습니다.");
        }
        var subject = subjects.stream().findFirst().get();
        subject.setNote(model.getNote());
        this._asdProjectSubjectRepository.save(subject);

        LOGGER.debug("Subject changed. [Project={}, Subject Id={}]", projectId, subjectId);

        // 사용자 활동 로그 기록
        writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_SUBJECT_CHANGE,
                String.format("과제 %s 에서 대상자 %s 수정", projectId, subjectId));

        return OkResponseEntity("대상자를 수정했습니다.", model);
    }

    @ApiIgnore
    @DeleteMapping(path = "/api/v1/projects/{id}/subjects/{subjectId}/")
    public ResponseEntity<JsonResponseObject> deleteSubject(
            final @PathVariable("id") long projectId,
            final @PathVariable("subjectId") String subjectId) throws Exception {
        final String systemId = super.getSystemId();

        this._trialService.deleteSubjectAndData(subjectId, projectId, systemId);

        LOGGER.debug("Subject and data deleted. [Project={}, Subject Id={}]", projectId, subjectId);

        // 사용자 활동 로그 기록
        writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_SUBJECT_REMOVE,
                String.format("과제 %s 에서 대상자 %s 삭제", projectId, subjectId));

        return OkResponseEntity("대상자를 삭제했습니다.", subjectId);
    }

    @ApiIgnore
    @PostMapping(path = "/api/v1/subjects/add/excel/")
    public ResponseEntity<JsonResponseObject> addSubjectsByExcelFile(
            @RequestParam("uploadFile") MultipartFile uploadFile) throws Exception {
        if (uploadFile.isEmpty()) {
            return ErrorResponseEntity("파일이 없습니다.");
        }

        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();
        final SubjectIdNamingPolicy subjectNamingPolicy = new SubjectIdNamingPolicy();

        int headerCount = 2;
        Workbook workBook = null;
        HashMap<String, Long> projectMap = new HashMap<>();
        HashMap<String, String> orgMap = new HashMap<>();
        List<CellException> errors = new ArrayList<>();
        List<String> addedSubjectIds = new ArrayList<String>();
        try {
            workBook = WorkbookFactory.create(uploadFile.getInputStream());
            Sheet sheet = workBook.getSheetAt(0);
            sheet.rowIterator().forEachRemaining((row) -> {
                final int rowIndex = row.getRowNum();
                if (rowIndex >= headerCount) {
                    final ProjectSubject subject = new ProjectSubject();
                    subject.setSystemId(systemId);
                    subject.setDateCreated(Calendar.getInstance().getTime());
                    subject.setUserCreated(worker);
                    subject.setDataSource(SubjectController.class.getName());

                    final List<CellException> cellErrors = new ArrayList<>();

                    row.cellIterator().forEachRemaining((cell) -> {
                        final int columnIndex = cell.getColumnIndex();

                        switch (columnIndex) {
                            case 0: // Project Name
                                if (StringUtils.hasText(cell.getStringCellValue())) {
                                    final String cellValue = cell.getStringCellValue().trim();
                                    if (projectMap.containsKey(cellValue)) {
                                        subject.setProjectSeq(projectMap.get(cellValue));
                                        subject.setProjectName(cellValue);
                                    } else {
                                        final List<Project> projects = this._projectRepository.findAll(systemId, null,
                                                null, cellValue);
                                        projects.stream().filter(p -> cellValue.equals(p.getProjectName())).findFirst()
                                                .ifPresentOrElse(prj -> {
                                                    final long projectId = prj.getProjectSeq();
                                                    subject.setProjectSeq(projectId);
                                                    subject.setProjectName(prj.getProjectName());

                                                    projectMap.putIfAbsent(cellValue, projectId);
                                                },
                                                        () -> {
                                                            CellException cellErr = new CellException();
                                                            cellErr.setColumnIndex(columnIndex + 1);
                                                            cellErr.setRowIndex(rowIndex + 1);
                                                            cellErr.setMessage("과제명이 존재하지 않습니다.");
                                                            cellErrors.add(cellErr);
                                                        });
                                    }
                                } else {
                                    CellException cellErr = new CellException();
                                    cellErr.setColumnIndex(columnIndex + 1);
                                    cellErr.setRowIndex(rowIndex + 1);
                                    cellErr.setMessage("과제명이 공백입니다.");
                                    cellErrors.add(cellErr);
                                }
                                break;
                            case 1: // Org ID
                                if (StringUtils.hasText(cell.getStringCellValue())) {
                                    final String cellValue1 = cell.getStringCellValue().trim();
                                    if (orgMap.containsKey(cellValue1)) {
                                        subject.setOrgId(cellValue1);
                                        subject.setOrgName(orgMap.get(cellValue1));
                                    } else {
                                        this._organizationRepository.findById(cellValue1, systemId)
                                                .ifPresentOrElse((o) -> {
                                                    final String orgid = o.getOrgId();
                                                    subject.setOrgId(orgid);
                                                    subject.setOrgName(o.getOrgName());
                                                    orgMap.putIfAbsent(orgid, o.getOrgName());
                                                }, () -> {
                                                    CellException cellErr = new CellException();
                                                    cellErr.setColumnIndex(columnIndex + 1);
                                                    cellErr.setRowIndex(rowIndex + 1);
                                                    cellErr.setMessage("참여기관이 존재하지 않습니다.");
                                                    cellErrors.add(cellErr);
                                                });
                                    }
                                } else {
                                    CellException cellErr = new CellException();
                                    cellErr.setColumnIndex(columnIndex + 1);
                                    cellErr.setRowIndex(rowIndex + 1);
                                    cellErr.setMessage("참여기관이 공백입니다.");
                                    cellErrors.add(cellErr);
                                }
                                break;
                            case 2: // Subject ID
                                if (StringUtils.hasText(cell.getStringCellValue())) {
                                    final String cellValue2 = cell.getStringCellValue().trim();

                                    /*
                                     * 대상자 ID가 명명규칙에 맞는지 체크
                                     */
                                    var subjectIdCheckResult = subjectNamingPolicy.passed(cellValue2);
                                    if (!subjectIdCheckResult.isPassed()) {
                                        CellException cellErr = new CellException();
                                        cellErr.setColumnIndex(columnIndex + 1);
                                        cellErr.setRowIndex(rowIndex + 1);
                                        cellErr.setMessage(subjectIdCheckResult.getErrors().stream()
                                                .reduce((t, u) -> t + ", " + u).orElse(""));
                                        cellErrors.add(cellErr);
                                        break;
                                    }

                                    final Collection<ProjectSubject> list = this._projectSubjectRepository
                                            .findAllBySubjectId(systemId, cellValue2);
                                    if (list != null && list.size() > 0) {
                                        CellException cellErr = new CellException();
                                        cellErr.setColumnIndex(columnIndex + 1);
                                        cellErr.setRowIndex(rowIndex + 1);
                                        cellErr.setMessage("동일한 대상자ID가 존재합니다.");
                                        cellErrors.add(cellErr);
                                    } else {
                                        subject.setSubjectId(cellValue2);
                                    }
                                } else {
                                    CellException cellErr = new CellException();
                                    cellErr.setColumnIndex(columnIndex + 1);
                                    cellErr.setRowIndex(rowIndex + 1);
                                    cellErr.setMessage("대상자ID가 공백입니다.");
                                    cellErrors.add(cellErr);
                                }
                                break;
                            case 3: // Gender (M/F)
                                if (StringUtils.hasText(cell.getStringCellValue())) {
                                    final String cellValue3 = cell.getStringCellValue().trim();
                                    String gender = cellValue3.equals("F") ? Constants.GENDER_FEMALE
                                            : cellValue3.equals("M") ? Constants.GENDER_MALE : null;
                                    if (gender == null) {
                                        CellException cellErr = new CellException();
                                        cellErr.setColumnIndex(columnIndex + 1);
                                        cellErr.setRowIndex(rowIndex + 1);
                                        cellErr.setMessage("성별값은 F(여자) 또는 M(남자)만 사용가능합니다.");
                                        cellErrors.add(cellErr);
                                    } else {
                                        subject.setGender(gender);
                                    }
                                } else {
                                    CellException cellErr = new CellException();
                                    cellErr.setColumnIndex(columnIndex + 1);
                                    cellErr.setRowIndex(rowIndex + 1);
                                    cellErr.setMessage("성별이 공백입니다.");
                                    cellErrors.add(cellErr);
                                }
                                break;
                            case 4: // Birthday YYYY-MM-DD
                                String cellValue4 = null;
                                // 날짜형식으로 셀값 읽기 시도 후 오류 시 문자열로 처리.
                                try {
                                    if (CellType.STRING == cell.getCellType()) {
                                        cellValue4 = cell.getStringCellValue();
                                    } else if (CellType.NUMERIC == cell.getCellType()) {
                                        cellValue4 = DateUtil.formatSimpleDate(cell.getDateCellValue());
                                    }
                                } catch (Exception ex) {
                                    cellValue4 = cell.getStringCellValue();
                                    LOGGER.error("Error while reading date value in excel file.", ex);
                                }

                                if (StringUtils.hasText(cellValue4)) {
                                    cellValue4 = cellValue4.trim();
                                    try {
                                        final int age = ProjectSubject.calculateAge(cellValue4, LocalDate.now());
                                        subject.setAge(age);
                                        subject.setActualBirthday(cellValue4);
                                    } catch (DateTimeParseException dex) {
                                        CellException cellErr = new CellException();
                                        cellErr.setColumnIndex(columnIndex + 1);
                                        cellErr.setRowIndex(rowIndex + 1);
                                        cellErr.setMessage("출생년월 형식이 올바르지 않습니다. (가능형식:년-월-일)");
                                        cellErrors.add(cellErr);
                                    }
                                } else {
                                    CellException cellErr = new CellException();
                                    cellErr.setColumnIndex(columnIndex + 1);
                                    cellErr.setRowIndex(rowIndex + 1);
                                    cellErr.setMessage("출생년월이 공백입니다.");
                                    cellErrors.add(cellErr);
                                }
                                break;
                        }
                    });

                    // Row에서 오류가 하나도 없어야만 읽은 대상자 저장 가능.
                    if (cellErrors.size() <= 0) {
                        subject.setId(MongoKeyGenerator.generateProjectSubjectId(systemId, subject.getOrgId(),
                                subject.getProjectSeq(), subject.getSubjectId()));
                        this._projectSubjectRepository.save(subject);
                        addedSubjectIds.add(subject.getSubjectId());
                    } else {
                        errors.addAll(cellErrors);
                    }
                }
            });
        } catch (Exception ex) {
            LOGGER.error("Error while reading excel file", ex);
        }

        SubjectFileUploadResult result = new SubjectFileUploadResult();

        // Send only max 50 errors.
        int errorCount = 0;
        if ((errorCount = errors.size()) > 50) {
            result.setErrors(errors.subList(0, 49));
        } else {
            result.setErrors(errors);
        }
        result.setErrorCount(errorCount);
        result.setRowCount(addedSubjectIds.size());

        return OkResponseEntity("대상자 목록을 저장했습니다.", result);
    }

    /**
     * 인체유래물 기증 동의서 파일 (PDF)을 암호화하여 저장한다.
     * 업로드하는 pdf 파일명은 대상자 ID를 포함해야 한다.
     * (파일형식: 대상자ID_N.pdf, 예:CH-CH20001_1.pdf)
     * 차수는 숫자로, 대상자ID와 차수는 '_'로 연결되어야 한다.
     * 
     * @param uploadFiles
     * @param projectId
     * @return
     * @throws Exception
     * @see com.ecoinsight.core.crypto.IBinaryEncryptor
     * @see com.ecoinsight.bdsp.core.service.PdfFileEncryptor
     */
    @ApiIgnore
    @PostMapping(path = "/api/v1/projects/{id}/subjects/upload/HumanDerivativeDonationAgreement/")
    public ResponseEntity<JsonResponseObject> uploadHumanDerivativeDonationAgreeFile(
            @RequestParam("uploadFiles") MultipartFile[] uploadFiles,
            final @PathVariable("id") long projectId) throws Exception {
        final String systemId = super.getSystemId();

        int fileCount = 0;
        if (uploadFiles == null || (fileCount = uploadFiles.length) <= 0) {
            return ErrorResponseEntity("업로드할 파일이 없습니다.");
        }

        final Path uploadDirPath = this._filePathGenerator.generateHumanDerivativeDonationAgreementPath(systemId,
                projectId);
        if (!Files.exists(uploadDirPath)) {
            Files.createDirectories(uploadDirPath);

            LOGGER.info("New dir for HumanDerivativeDonationAgreement PDF upload. path={}",
                    uploadDirPath.toAbsolutePath().toString());
        }

        final String CATEGORY = ProjectSubjectFile.CATEGORY_HumanDerivativeDonationAgreement;
        final List<String> errorMessages = new ArrayList<>();

        for (int i = 0; i < fileCount; i++) {
            final MultipartFile uploadFile = uploadFiles[i];
            final String fileName = uploadFile.getOriginalFilename();
            final int extIndex = fileName.lastIndexOf(".");
            final String ext = fileName.substring(extIndex, fileName.length());

            LOGGER.debug("HumanDerivativeDonationAgreement PDF. [ORG-FILE={}, EXT={}]", fileName, ext);

            if (extIndex <= 0 || !fileName.toLowerCase()
                    .endsWith(ProjectSubjectFile.HUMAN_DERIVATIVE_DONATION_AGREEMENT_FILE_EXT_PDF)) {
                errorMessages.add(String.format("%s 파일형식은 지원하지 않습니다. (%s 파일만 지원합니다.)", ext,
                        ProjectSubjectFile.HUMAN_DERIVATIVE_DONATION_AGREEMENT_FILE_EXT_PDF));
                continue;
            }

            /*
             * 업로드하는 파일명은 subject-id_N.pdf (예: BD-BDP-001_1.pdf) 이어야 한다.
             */
            // 파일명에서 차수 추출
            final int trialIndexBegin = fileName.indexOf(ProjectSubjectFile.UPLOAD_FILENAME_DELIMITER);
            int trialIndexEnd = fileName.indexOf(ProjectSubjectFile.UPLOAD_FILENAME_DELIMITER, trialIndexBegin + 1);
            if (trialIndexEnd <= 0) {
                trialIndexEnd = extIndex;
            }
            final String trialValue = fileName.substring(
                    trialIndexBegin + (ProjectSubjectFile.UPLOAD_FILENAME_DELIMITER).length(), trialIndexEnd);

            LOGGER.debug("HumanDerivativeDonationAgreement PDF. [PROJECT={}, TRIAL={}]", projectId, trialValue);

            int trialIndex = 0;
            try {
                trialIndex = Integer.parseInt(trialValue);
            } catch (NumberFormatException nex) {
                errorMessages.add(String.format("파일명에서 차수 형식이 올바르지 않습니다. (예:BD-BDP-001_1.pdf)"));
                continue;
            }

            // 파일명에서 대상자 ID 추출
            final String subjectId = fileName.substring(0, trialIndexBegin);
            final Collection<ProjectSubject> subjects = this._projectSubjectRepository.findAllBySubjectId(systemId,
                    subjectId);

            LOGGER.debug("HumanDerivativeDonationAgreement PDF. [SUBJECT={}]", subjectId);

            if (subjects == null || subjects.isEmpty()) {
                errorMessages.add(String.format("대상자 %s가 시스템에 존재하지 않습니다. (대상자를 먼저 등록하시기 바랍니다.)", subjectId));
                continue;
            }

            // 조회한 대상자 목록을 과제 ID로 필터링. (과제별로 동일한 대상자 ID가 존재할 수도 있으므로.)
            var subjectList = subjects.stream().filter(t -> t.getProjectSeq() == projectId)
                    .collect(Collectors.toList());
            if (subjectList.isEmpty()) {
                errorMessages.add(String.format("대상자 %s가 해당 연구과제의 대상자가 아닙니다. (연구과제와 대상자를 확인하세요.)", subjectId));
                continue;
            }
            if (subjectList.size() > 1) {
                LOGGER.warn("Too many subject found. [System={}, Project={}, Subject={}]", systemId, projectId,
                        subjectId);
                errorMessages.add(String.format("동일한 대상자 ID(%s)가 여러개 등록되어 있습니다. (대상자 ID를 확인하세요.)", subjectId));
                continue;
            }

            final ProjectSubject subject = subjectList.get(0);

            LOGGER.debug("HumanDerivativeDonationAgreement Subject. [SUBJECT={}, ORG={}]", subject.getSubjectId(),
                    subject.getOrgId());

            // 원래 파일 확장자를 암호화 파일 확장자로 변경
            final String newFileName = this._filePathGenerator.generateHumanDerivativeDonationAgreementFileName(
                    fileName, ProjectSubjectFile.HUMAN_DERIVATIVE_DONATION_AGREEMENT_FILE_EXT_PDF);
            final Path filePath = Path.of(uploadDirPath.toAbsolutePath().toString(), newFileName);
            final byte[] encBytes = encryptor.encrypt(uploadFile.getBytes());

            // 암호화한 PDF 파일을 디렉토리에 저장.
            try (final OutputStream output = Files.newOutputStream(filePath)) {
                output.write(encBytes);

                LOGGER.debug("HumanDerivativeDonationAgreement encrypted file saved. [FILE={}, PATH={}]", newFileName,
                        filePath);

                final ProjectSubjectFile file = new ProjectSubjectFile();
                file.setProjectSeq(projectId);
                file.setTrialIndex(trialIndex);
                file.setCategory(CATEGORY);
                file.setCategoryName(ProjectSubjectFile.CATEGORY_HumanDerivativeDonationAgreement_KR);
                file.setEncorder(encryptor.getClass().getName());
                file.setEncrypted(true);
                file.setOriginalFileName(fileName);
                file.setFileName(newFileName);
                file.setFilePath(filePath.toString());
                file.setFileType(ext);

                final List<ProjectSubjectFile> files = subject.getFiles();
                if (files == null || files.size() <= 0) {
                    subject.setFiles(List.of(file));
                } else {
                    final int trialindex = trialIndex;
                    // 과제의 차수에 대한 인체유래물 기증 동의서 파일 정보 조회
                    files.stream()
                            .filter(f -> CATEGORY.equals(f.getCategory()) && f.getProjectSeq() == projectId
                                    && f.getTrialIndex() == trialindex)
                            .findFirst()
                            .ifPresent(f -> {
                                // Remove the files if it exists
                                subject.getFiles().remove(f);

                                // Delete old file if the file name is not same.
                                if (!newFileName.equals(f.getFileName())) {
                                    FileUtil.deleteOneFile(Path.of(f.getFilePath()).toFile());

                                    LOGGER.debug(
                                            "HumanDerivativeDonationAgreement old file deleted. [FILE={}, PATH={}]",
                                            f.getFileName(), f.getFilePath());
                                }

                                LOGGER.debug(
                                        "HumanDerivativeDonationAgreement file removed in file list. [FILE={}, PATH={}]",
                                        f.getFileName(), f.getFilePath());
                            });

                    subject.getFiles().add(file);
                }

                // 암호화한 파일명, 경로를 Mongo DB에 저장.
                this._projectSubjectRepository.save(subject);

                LOGGER.info("HumanDerivativeDonationAgreement pdf file uploaded. [Path={}]", filePath.toString());
            }
        }

        return (errorMessages.size() > 0)
                ? ErrorResponseEntity("인체유래물 기증 동의서 파일 저장 중 오류가 발생했습니다.", errorMessages.toArray(String[]::new))
                : OkResponseEntity("인체유래물 기증 동의서 파일을 저장했습니다.");
    }

    @ApiOperation(value = "대상자 정보 등록 API ", notes = "새로운 대상자 정보를 등록함")
    @PostMapping(path = "/api/v1/subject/result")
    public ResponseEntity<JsonResponseObject> addSubject(@RequestBody Subject subject) {
        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();

        // 신규 등록, 몽고 DB에 업로드
        // 1. 중복 조회 없으면 바로 insert
        // 2. 중복 조회가 있으면 insert 하지 않는다

        // 각 항목에 대한 validation 추가

        if (subject.getProjectSeq() == null) {
            return ErrorResponseEntity("실험군이 입력 되지 않았습니다.");
        }

        // if(subject.getSubjectId()==null) { // subject.getSubjectId() == empty string
        // 을 처리하지 못함.
        final String subjectId = subject.getSubjectId();
        if (!StringUtils.hasText(subjectId)) {
            return ErrorResponseEntity("대상자 ID가 입력 되지 않았습니다.");
        }
        if (subject.getName() == null) {
            return ErrorResponseEntity("대상자 성명이 입력 되지 않았습니다.");
        }

        if (subject.getBirthDay() == null) {
            return ErrorResponseEntity("대상자 생년월일이 입력 되지 않았습니다.");
        }

        if (!ValidDateFormat(subject.getBirthDay())) {
            return ErrorResponseEntity("올바르지 않은 생년월일 양식 (yyyy-MM-dd) 입니다.");
        }
        if (subject.getGender() == null) {
            return ErrorResponseEntity("대상자 성별이 입력 되지 않았습니다.");
        }

        if (subject.getState() == null) {
            return ErrorResponseEntity("대상자 상태 정보가 입력 되지 않았습니다.");
        }

        if (subject.getRegistDate() == null) {
            return ErrorResponseEntity("대상자 등록일이 입력 되지 않았습니다.");
        }

        if (!ValidDateFormat(subject.getRegistDate())) {
            return ErrorResponseEntity("올바르지 않은 등록일 양식 (yyyy-mm-dd) 입니다.");
        }

        Result result = new Result();
        Collection<AsdProjectSubject> subjects = _asdProjectSubjectRepository.findAllBySubjectId(systemId, subjectId);

        if (subjects.size() > 0) {
            return ErrorResponseEntity(
                    String.format("이미 등록 된 대상자입니다. - systemId=%s, subjectId=%s,", systemId, subjectId));

        } else {
            // TODO 만약에 subjectId가 2글자 이하면??
            if (subjectId.length() != 10) {
                return ErrorResponseEntity("올바르지 않은 대상자ID 입니다.");
            }
            String orgId = subjectId.substring(0, 2);

            // TODO 만약에 systemId + orgId가 AsdOrganization enum 에 정의되 값이 아니면???
            // AsdOrganization ao = AsdOrganization.valueOf(systemId + orgId);
            // final String orgName = AsdOrganization.valueOf(systemId + orgId).getName();
            Optional<Organization> orgOptional = _organizationRepository.findById(orgId, systemId);
            if (orgOptional.isEmpty()) {
                return ErrorResponseEntity(String.format("No found organization. (id=%s)", orgId));
            }

            Organization organization = orgOptional.get();
            final String orgName = organization.getOrgName();

            AsdProjectSubject asdSubject = new AsdProjectSubject();

            // if(org.apache.commons.lang.StringUtils.equals(subject.getProjectSeq(),"NORMAL")){
            if ("NORMAL".equals(subject.getProjectSeq())) {
                asdSubject.setProjectSeq(ProjectSeq.NORMAL.getSeq());
                // }else
                // if(org.apache.commons.lang.StringUtils.equals(subject.getProjectSeq(),"ASD_HIGH")){
            } else if ("ASD_HIGH".equals(subject.getProjectSeq())) {
                asdSubject.setProjectSeq(ProjectSeq.ASD_HIGH.getSeq());
                // }else
                // if(org.apache.commons.lang.StringUtils.equals(subject.getProjectSeq(),"ASD")){
            } else if ("ASD".equals(subject.getProjectSeq())) {
                asdSubject.setProjectSeq(ProjectSeq.ASD.getSeq());
                // }else
                // if(org.apache.commons.lang.StringUtils.equals(subject.getProjectSeq(),"HOLD")){
            } else if ("HOLD".equals(subject.getProjectSeq())) {
                asdSubject.setProjectSeq(ProjectSeq.HOLD.getSeq());
            } else {
                return ErrorResponseEntity("올바르지 않은 대상군입니다.");
            }

            // final String id = generateId(systemId, asdSubject.getProjectSeq(), orgId,
            // subjectId);

            asdSubject.setSubjectId(subjectId);
            asdSubject.setId(
                    MongoKeyGenerator.generateProjectSubjectId(systemId, orgId, asdSubject.getProjectSeq(), subjectId));
            asdSubject.setName(subject.getName());
            asdSubject.setSystemId(systemId);
            asdSubject.setOrgId(orgId);
            asdSubject.setOrgName(orgName);
            asdSubject.setGender(subject.getGender());
            asdSubject.setBirthDay(subject.getBirthDay());

            asdSubject.setPhoneNumber(subject.getPhoneNumber());
            asdSubject.setState(subject.getState());
            asdSubject.setTestYn(subject.getTestYn());
            asdSubject.setRegistDate(subject.getRegistDate());
            asdSubject.setStartDate(LocalDate.now().toString());
            asdSubject.setEndDate(LocalDate.now().plusMonths(6).toString());
            asdSubject.setTrialIndex(1);
            asdSubject.setDateCreated(LocalDateTime.now());
            asdSubject.setDateUpdated(LocalDateTime.now());
            asdSubject.setUserCreated(worker);
            asdSubject.setUserUpdated(worker);
            asdSubject.setDateTrialIndex(LocalDateTime.now());

            // try {
            // try {
            // _callRestApi.requestPost(this.omniHost + OmniApi.PATSTATUSINFO,
            // makeOmniJsonObject(asdSubject, false));
            // asdSubject.setOmniStatus(true);
            // asdSubject.setOmniResult("정상 등록");
            // } catch (Exception e) {
            // asdSubject.setOmniStatus(false);
            // asdSubject.setOmniResult(e.getMessage());

            // // 작동 여부를 DB 로그로 체크한다.
            // } finally {
            // // _asdProjectSubjectRepository.save(asdSubject);
            // }

            // try {
            // _textMessageSender.send(subject);
            // asdSubject.setTextStatus(true);
            // } catch (Exception e) {
            // asdSubject.setTextStatus(false);
            // asdSubject.setTextResult(e.getMessage());

            // // 작동 여부를 DB 로그로 체크한다.
            // } finally {
            // // _asdProjectSubjectRepository.save(asdSubject);
            // }

            // // TODO 트랜잭션 처리 필요

            // _asdProjectSubjectRepository.save(asdSubject); // only single data excetion
            // already exsit Collection
            // // Data
            // _asdDataCommonRepository.addDataIntegrationBySubjectId(asdSubject);

            // } catch (Exception e) {
            // LOGGER.error("Error for updateSubjects. -> {}", e.getMessage());
            // return ErrorResponseEntity(String.format("대상자 등록에 실패 하였습니다. - systemId=%s,
            // subjectId=%s,", systemId,
            // subject.getSubjectId()), e);
            // } finally {

            // }

            try {
                try {
                    _callRestApi.requestPost(this.omniHost + OmniApi.PATSTATUSINFO,
                            makeOmniJsonObject(asdSubject, false));
                    asdSubject.setOmniStatus(true);
                    asdSubject.setOmniResult("정상 등록");
                } catch (Exception e) {
                    asdSubject.setOmniStatus(false);
                    asdSubject.setOmniResult(e.getMessage());
                    LOGGER.error("Error occurred while sending request to Omni API. -> {}", e.getMessage());
                    throw new RuntimeException("Error occurred while sending request to Omni API.", e);
                }

                try {
                    _textMessageSender.send(subject);
                    asdSubject.setTextStatus(true);
                } catch (Exception e) {
                    asdSubject.setTextStatus(false);
                    asdSubject.setTextResult(e.getMessage());
                    LOGGER.error("Error occurred while sending text message. -> {}", e.getMessage());
                    throw new RuntimeException("Error occurred while sending text message.", e);
                }

                // 대상자 정보 저장
                try {
                    saveSubject(asdSubject);
                    result.setSuccess(true);
                    result.setMessage("대상자 정보를 성공적으로 저장했습니다.");
                    return OkResponseEntity(result);
                } catch (Exception e) {
                    result.setSuccess(false);
                    result.setMessage("대상자 정보를 저장하는 중에 오류가 발생했습니다.");
                    return ErrorResponseEntity(result);
                }

            } catch (Exception e) {
                LOGGER.error("Error occurred while registering subject. -> {}", e.getMessage());
                return ErrorResponseEntity(String.format("대상자 등록에 실패 하였습니다. - systemId=%s, subjectId=%s", systemId,
                        subject.getSubjectId()), e);
            }

        }
        // return ResponseEntity.ok(new JsonResponseObject(result.isSuccess(),
        // result.getMessage(), result));

    }

    private void saveSubject(AsdProjectSubject asdSubject) {
        try {
            // MongoDB에 대상자 정보 저장
            _asdProjectSubjectRepository.save(asdSubject);

            // 추가 작업 처리
            _asdDataCommonRepository.addDataIntegrationBySubjectId(asdSubject);
        } catch (Exception e) {
            // 예외 처리
            LOGGER.error("Error occurred while saving subject information. -> {}", e.getMessage());
            throw e;
        }
    }

    private ResponseEntity<JsonResponseObject> ErrorResponseEntity(Result result) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JsonResponseObject(result.isSuccess(), result.getMessage(), result));
    }

    private ResponseEntity<JsonResponseObject> OkResponseEntity(Result result) {
        return ResponseEntity.ok(new JsonResponseObject(result.isSuccess(), result.getMessage(), result));
    }

    @ApiOperation(value = "대상자 정보 수정 API ", notes = "기존 대상자 정보를 새로운 대상자 정보로 수정함")
    @PutMapping(path = "/api/v1/subject/result")
    public ResponseEntity<JsonResponseObject> updateSubjects(@RequestBody Subject subject) {
        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();
        // 기존 등록 업데이트

        // 1. 중복 조회가 있으면 insert 한다.
        // 2. 중복 조회가 없으면 변경하지 않는다.

        final String subjectId = subject.getSubjectId();
        if (!StringUtils.hasText(subjectId)) {
            return ErrorResponseEntity("대상자 ID가 입력 되지 않았습니다.");
        }

        if (subject.getBirthDay() == null) {
            return ErrorResponseEntity("대상자 생년월일이 입력 되지 않았습니다.");
        }

        if (!ValidDateFormat(subject.getBirthDay())) {
            return ErrorResponseEntity("올바르지 않은 생년월일 양식 (yyyy-MM-dd) 입니다.");
        }

        if (subject.getName() == null) {
            return ErrorResponseEntity("대상자 성명이 입력 되지 않았습니다.");
        }

        if (subject.getGender() == null) {
            return ErrorResponseEntity("대상자 성별이 입력 되지 않았습니다.");
        }

        if (subject.getState() == null) {
            return ErrorResponseEntity("대상자 상태 정보가 입력 되지 않았습니다.");
        }

        if (subject.getRegistDate() == null) {
            return ErrorResponseEntity("대상자 등록일이 입력 되지 않았습니다.");
        }

        if (!ValidDateFormat(subject.getRegistDate())) {
            return ErrorResponseEntity("올바르지 않은 등록일 양식 (yyyy-mm-dd) 입니다.");
        }

        if (subject.getProjectSeq() == null) {
            return ErrorResponseEntity("실험군이 입력 되지 않았습니다.");
        }

        Collection<AsdProjectSubject> subjects = _asdProjectSubjectRepository.findAllBySubjectId(systemId,
                subject.getSubjectId());
        Result result = new Result();
        if (subjects.size() > 0) {
            if (subjects.size() > 1) {
                return ErrorResponseEntity(String.format("대상자가 한 명 이상 입니다. - systemId=%s, subjectId=%s,", systemId,
                        subject.getSubjectId()));
            }

            AsdProjectSubject asdSubject = subjects.stream().findFirst().get();

            List<DataSummary> dataList = _asdDataCommonRepository
                    .findbyLastTrialIndexDatSummaryList(asdSubject.getSubjectId());
            int latestTrialIndex = dataList.get(0).getTrialIndex();

            if (asdSubject.getTrialIndex() < latestTrialIndex) {
                if (subject.getTrialIndex() != 0) {
                    asdSubject.setTrialIndex(subject.getTrialIndex());
                } else {
                    asdSubject.setTrialIndex(latestTrialIndex);
                }
                asdSubject.setTrialIndex(latestTrialIndex);
            } else {
                if (subject.getTrialIndex() != 0) {
                    asdSubject.setTrialIndex(latestTrialIndex);
                }
            }

            if (asdSubject.getTrialIndex() == subject.getTrialIndex()) { // stage 서버 스테이지
                // 서버 옴니측 테스트 대상자를 위한 임시로직
                asdSubject.setTrialIndex(subject.getTrialIndex());
            }

            if (!org.apache.commons.lang.StringUtils.equals(asdSubject.getBirthDay(), subject.getBirthDay())) {
                asdSubject.setBirthDay(subject.getBirthDay());
            }

            if (!org.apache.commons.lang.StringUtils.equals(asdSubject.getRegistDate(), subject.getRegistDate())) {
                asdSubject.setRegistDate(subject.getRegistDate());
            }

            if (!org.apache.commons.lang.StringUtils.equals(asdSubject.getGender(), subject.getGender())) {
                asdSubject.setGender(subject.getGender());
            }

            if (!org.apache.commons.lang.StringUtils.equals(asdSubject.getPhoneNumber(), subject.getPhoneNumber())) {
                asdSubject.setPhoneNumber(subject.getPhoneNumber());
            }

            if (!org.apache.commons.lang.StringUtils.equals(asdSubject.getName(), subject.getName())) {
                asdSubject.setName(subject.getName());
            }
            if (!org.apache.commons.lang.StringUtils.equals(asdSubject.getTestYn(), subject.getTestYn())) {
                asdSubject.setTestYn(subject.getTestYn());
            }
            if (asdSubject.getState() != subject.getState()) {
                if (subject.getState() == 1) {
                    asdSubject.setState(SubjectState.InformedConsentSigned);
                } else if (subject.getState() == 2) {
                    asdSubject.setState(SubjectState.ScreeningFailure);
                } else if (subject.getState() == 3) {
                    asdSubject.setState(SubjectState.EnrolledActive);
                } else if (subject.getState() == 4) {
                    asdSubject.setState(SubjectState.OffStudy);
                } else {
                    return ErrorResponseEntity("올바르지 않은 state 입니다.");
                }
            }

            // if(org.apache.commons.lang.StringUtils.equals(subject.getProjectSeq(),"NORMAL")){
            if ("NORMAL".equals(subject.getProjectSeq())) {
                // 보류에서 1,2,3 으로 변경될 때 lableling 을 실행한다.
                // 보류(HOLD) 뿐만아니라 1,2,3 값으로 처음 등록될 경우가 발생한 이슈로 인해 1,2,3,4 에서 다른 대상군으로 변경될때도
                // labelling
                // 실행할 수 있도록 한다.
                if (4L == asdSubject.getProjectSeq()) {
                    try {
                        doLabelling(systemId, subjectId, ProjectSeq.NORMAL.getSeq());
                    } catch (Exception e) {
                        LOGGER.error("Error for labelling. -> {}", e.getMessage());
                        return ErrorResponseEntity(String.format(
                                "대상자 정보 수정으로 기존 데이터 수정에 실패했습니다. - systemId=%s, subjectId=%s, projectSeq=%s", systemId,
                                subject.getSubjectId(), ProjectSeq.NORMAL.getSeq()), e);
                    }
                } else {
                    try {
                        changeProjectSeq(systemId, subjectId, asdSubject.getProjectSeq(), ProjectSeq.NORMAL.getSeq());
                    } catch (Exception e) {
                        LOGGER.error("Error for changeProjectSeq. -> {}", e.getMessage());
                        return ErrorResponseEntity(String.format(
                                "대상자 정보 수정으로 기존 데이터 수정에 실패했습니다. - systemId=%s, subjectId=%s, fromprojectSeq=%s ,toprojectSeq=%s",
                                systemId, subject.getSubjectId(), asdSubject.getProjectSeq(),
                                ProjectSeq.NORMAL.getSeq()), e);
                    }

                }
                asdSubject.setProjectSeq(ProjectSeq.NORMAL.getSeq());

                // }else
                // if(org.apache.commons.lang.StringUtils.equals(subject.getProjectSeq(),"ASD_HIGH")){
            } else if ("ASD_HIGH".equals(subject.getProjectSeq())) {
                if (4L == asdSubject.getProjectSeq()) {
                    try {
                        doLabelling(systemId, subjectId, ProjectSeq.ASD_HIGH.getSeq());
                    } catch (Exception e) {
                        LOGGER.error("Error for labelling. -> {}", e.getMessage());
                        return ErrorResponseEntity(String.format(
                                "대상자 정보 수정으로 기존 데이터 수정에 실패했습니다. - systemId=%s, subjectId=%s, projectSeq=%s", systemId,
                                subject.getSubjectId(), ProjectSeq.ASD_HIGH.getSeq()), e);
                    }
                } else {
                    try {
                        changeProjectSeq(systemId, subjectId, asdSubject.getProjectSeq(), ProjectSeq.ASD_HIGH.getSeq());
                    } catch (Exception e) {
                        LOGGER.error("Error for changeProjectSeq. -> {}", e.getMessage());
                        return ErrorResponseEntity(String.format(
                                "대상자 정보 수정으로 기존 데이터 수정에 실패했습니다. - systemId=%s, subjectId=%s, fromprojectSeq=%s ,toprojectSeq=%s",
                                systemId, subject.getSubjectId(), asdSubject.getProjectSeq(),
                                ProjectSeq.ASD_HIGH.getSeq()), e);
                    }

                }
                asdSubject.setProjectSeq(ProjectSeq.ASD_HIGH.getSeq());
                // }else
                // if(org.apache.commons.lang.StringUtils.equals(subject.getProjectSeq(),"ASD")){
            } else if ("ASD".equals(subject.getProjectSeq())) {
                if (4L == asdSubject.getProjectSeq()) {
                    try {
                        doLabelling(systemId, subjectId, ProjectSeq.ASD.getSeq());
                    } catch (Exception e) {
                        LOGGER.error("Error for labelling. -> {}", e.getMessage());
                        return ErrorResponseEntity(
                                String.format("대상자 labelling . - systemId=%s, subjectId=%s, projectSeq=%s", systemId,
                                        subject.getSubjectId(), ProjectSeq.ASD.getSeq()),
                                e);
                    }
                } else {
                    try {
                        changeProjectSeq(systemId, subjectId, asdSubject.getProjectSeq(), ProjectSeq.ASD.getSeq());
                    } catch (Exception e) {
                        LOGGER.error("Error for changeProjectSeq. -> {}", e.getMessage());
                        return ErrorResponseEntity(String.format(
                                "대상자 정보 수정으로 기존 데이터 수정에 실패했습니다. - systemId=%s, subjectId=%s, fromprojectSeq=%s ,toprojectSeq=%s",
                                systemId, subject.getSubjectId(), asdSubject.getProjectSeq(),
                                ProjectSeq.ASD.getSeq()), e);
                    }

                }

                asdSubject.setProjectSeq(ProjectSeq.ASD.getSeq());
                // }else
                // if(org.apache.commons.lang.StringUtils.equals(subject.getProjectSeq(),"HOLD")){
            } else if ("HOLD".equals(subject.getProjectSeq())) {
                asdSubject.setProjectSeq(ProjectSeq.HOLD.getSeq());
            } else {
                return ErrorResponseEntity("올바르지 않은 대상군입니다.");
            }
            asdSubject.setTestYn(subject.getTestYn());
            asdSubject.setUserUpdated(worker);
            asdSubject.setDateUpdated(LocalDateTime.now());

            try {
                _asdProjectSubjectRepository.save(asdSubject);

                List<DataSummary> subjectList = _asdDataCommonRepository.findAllBySubject(subjectId);
                if (!subjectList.isEmpty()) {
                    if (subjectList.size() > 1) {
                        for (DataSummary summary : subjectList) {
                            DataSummary data = setDataSummaryData(asdSubject);
                            data.setSubjectId(subjectId);
                            data.setId(summary.getId());
                            data.setTrialIndex(summary.getTrialIndex());
                            data.setUUser(worker);
                            data.setUDate(LocalDateTime.now());
                            _asdDataCommonRepository.updateDateIntegrationBySubjectId(data);
                            result.setSuccess(true);
                            result.setMessage("대상자 수정이 완료 되었습니다.");

                        }

                    } else if (subjectList.size() == 1) {
                        DataSummary data = setDataSummaryData(asdSubject);
                        data.setSubjectId(subjectId);
                        data.setId(dataList.get(0).getId());
                        data.setTrialIndex(latestTrialIndex);
                        data.setUUser(worker);
                        data.setUDate(LocalDateTime.now());
                        _asdDataCommonRepository.updateDateIntegrationBySubjectIdLastList(data);
                        result.setSuccess(true);
                        result.setMessage("대상자 수정이 완료 되었습니다.");

                    }
                    // 최종 trialIndex 값의 update 수집 현황 테이블 대상자 정보 업데이트 // max trialIndex에 해당 정보 업데이트

                } else {

                    return ErrorResponseEntity(
                            String.format("대상자 정보내역이 존재하지 않습니다. 수정에 실패 하였습니다. - systemId=%s, subjectId=%s,", systemId,
                                    subject.getSubjectId()));

                }

                // 데이터 테이블 ProjectSeq업데이트
                // updateDataBySubjectId(asdSubject, "UPDATE");p

                try {
                    _callRestApi.requestPost(this.omniHost + OmniApi.PATSTATUSINFO,
                            makeOmniJsonObject(asdSubject, false));
                    asdSubject.setOmniStatus(true);
                } catch (Exception e) {
                    asdSubject.setOmniStatus(false);
                    asdSubject.setOmniResult(e.getMessage());

                } finally {
                    _asdProjectSubjectRepository.save(asdSubject);
                }

                // try{
                // _textMessageSender.send(subject);
                // ps.setTextStatus(true);
                // }catch (Exception e){
                // ps.setTextStatus(false);
                // ps.setTextResult(e.getMessage());
                // } finally{
                // _asdProjectSubjectRepository.save(ps);
                // }

            } catch (Exception e) {
                LOGGER.error("Error for updateSubjects. -> {}", e);
                return ErrorResponseEntity(String.format("대상자 수정에 실패 하였습니다. - systemId=%s, subjectId=%s,", systemId,
                        subject.getSubjectId()), e);
            } finally {

            }

        } else {
            LOGGER.error("Error for Not found subjects at updateSubjects. -> {}", subject.getSubjectId());
            return ErrorResponseEntity(String.format("대상자 목록에서 수정 할 대상자를 찾을 수 없습니다. - systemId=%s, subjectId=%s,",
                    systemId, subject.getSubjectId()));
        }

        return ResponseEntity.ok(new JsonResponseObject(result.isSuccess(), result.getMessage(), result));
    }

    public DataSummary setDataSummaryData(AsdProjectSubject subject) {
        DataSummary summary = new DataSummary();
        if (subject != null) {

            // Name= #{name}, Gender= #{gender}, BirthDay= #{birthDay}, TrialIndex=
            // #{trialIndex}, ProjectSeq= #{projectSeq}, State= #{state}, TestYn =#{testYn}
            // where SubjectId = #{subjectId} and Id = #{id}
            summary.setName(subject.getName());
            summary.setGender(subject.getGender());
            summary.setBirthday(subject.getBirthDay());
            summary.setProjectSeq(subject.getProjectSeq());
            summary.setState(subject.getState());
            summary.setTestYn(subject.getTestYn());
        }

        return summary;

    }

    @ApiOperation(value = "대상자 정보 삭제(상태정보변경) API ", notes = " 대상자의 상태정보를 Off Study 상태로 변경함")
    @DeleteMapping(path = "/api/v1/subject/result")
    public ResponseEntity<JsonResponseObject> deleteSubjects(@RequestBody Map<String, String> map) {
        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();

        final String subjectId = map.get("subjectId");

        if (subjectId == null) {
            return ErrorResponseEntity("대상자 ID가 입력 되지 않았습니다.");
        }
        // 해당 리스트 체크
        // state 값을 DROP으로 변경해준다.

        // 데이터가 올라가 있는지 체크 하여 데이터가 없으면 대상자를 삭제 처리 한다.
        Result result = new Result();
        Collection<AsdProjectSubject> subjects = null;
        AsdProjectSubject asdSubject = null;

        subjects = _asdProjectSubjectRepository.findAllBySubjectId(systemId, subjectId);

        if (subjects.size() > 0) {
            if (subjects.size() > 1) {
                return ErrorResponseEntity(String.format("대상자 정보가 두개 이상의 과제에 등록되어 있습니다. - systemId=%s, subjectId=%s",
                        systemId, subjectId));
            }

            asdSubject = subjects.stream().findFirst().get();
            asdSubject.setUserUpdated(worker);
            asdSubject.setDateUpdated(LocalDateTime.now());

            try {
                _asdProjectSubjectRepository.delete(asdSubject);
                updateDataBySubjectId(asdSubject, "DEL");
                result.setSuccess(true);
                result.setMessage("대상자 삭제가 완료 되었습니다.");

                try {
                    _callRestApi.requestPost(this.omniHost + OmniApi.PATSTATUSINFO,
                            makeOmniJsonObject(asdSubject, true));
                    asdSubject.setOmniStatus(true);
                } catch (Exception e) {
                    asdSubject.setOmniStatus(false);
                    asdSubject.setOmniResult(e.getMessage());
                } finally {

                }

                // try{
                // Subject subject = new Subject();
                // subject.setPhoneNumber(ps.getPhoneNumber());
                // _textMessageSender.send(subject);
                // ps.setTextStatus(true);
                // }catch (Exception e){
                // ps.setTextStatus(false);
                // ps.setTextResult(e.getMessage());
                // } finally{

                // }

            } catch (Exception e) {
                LOGGER.error("Error for deleteSubjects. -> {}", e.getMessage());
                return ErrorResponseEntity(
                        String.format("대상자 삭제에 실패 하였습니다. - systemId=%s, subjectId=%s,", systemId, subjectId), e);
            } finally {
            }
        } else {
            LOGGER.error("Error for Not found subjects at deleteSubjects. -> {}", subjectId);
            return ErrorResponseEntity(
                    String.format("대상자를 찾을 수 없습니다. - systemId=%s, subjectId=%s", systemId, subjectId));
        }
        return ResponseEntity.ok(new JsonResponseObject(result.isSuccess(), result.getMessage(), result));
    }

    private static String generateId(String systemId, long projectSeq, String orgId, String subjectId) {
        return String.format("%s-%s-%s-%s", systemId, projectSeq, orgId, subjectId);
    }

    private JSONObject makeOmniJsonObject(AsdProjectSubject ps, boolean delete) {

        OmniSubject omniSubject = new OmniSubject();
        omniSubject.setId(ps.getSubjectId());
        omniSubject.setState(ps.getState());
        omniSubject.setName(ps.getName());
        omniSubject.setGender(ps.getGender());
        omniSubject.setBirth(ps.getBirthDay());
        omniSubject.setPhone(ps.getPhoneNumber());
        omniSubject.setAgreementDate(ps.getRegistDate());
        omniSubject.setResearchStartDate(ps.getStartDate());
        omniSubject.setResearchEndDate(ps.getEndDate());
        omniSubject.setProjectSeq((int) ps.getProjectSeq());
        omniSubject.setTrialIndex(ps.getTrialIndex());

        if (delete) {
            // 삭제시 옴니 측에는 state를 0로 setting 함
            omniSubject.setState(0);
        }

        Gson gson = new Gson();
        List<OmniSubject> cover = new ArrayList<>(1);
        cover.add(omniSubject);
        String list = gson.toJson(cover);
        JSONArray jsonArray = new JSONArray(list);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("patients", jsonArray);

        return jsonObject;
    }

    private boolean ValidDateFormat(String date) {
        if (date == null || !date.matches("\\d{4}-[01]\\d-[0-3]\\d")) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error validation of DateFormat. -> {}", e.getMessage());
            return false;
        }
    }

    private void updateDataBySubjectId(AsdProjectSubject subject, String status) throws Exception {
        int tableCount = 0;
        DataTable[] dt = DataTable.values();

        for (int i = 0; i < dt.length; i++) {
            int count = _asdDataCommonRepository.updateDataBySubjectId(dt[i].toString(), subject, status);
            tableCount++;

        }

        if (tableCount > 0) {

        }
        int count = _asdDataCommonRepository.deleteDataSummaryBySubjectId(subject);
        // count 0 일때 수정하기
    }

    private void doLabelling(String systemId, String subjectId, long projectSeq) throws Exception {
        _EyeTrackingService.doLabelling(systemId, subjectId, projectSeq);
        _MChartService.doLabelling(systemId, subjectId, projectSeq);
        _videoResourceService.doLabelling(systemId, subjectId, projectSeq);
        _surveyStatusService.doLabelling(systemId, subjectId, projectSeq);

    }

    private void changeProjectSeq(String systemId, String subjectId, long fromProjectSeq, long toProjectSeq)
            throws Exception {

        if (fromProjectSeq == toProjectSeq)
            return;

        _EyeTrackingService.changeProjectSeq(systemId, subjectId, fromProjectSeq, toProjectSeq);
        _MChartService.changeProjectSeq(systemId, subjectId, fromProjectSeq, toProjectSeq);
        _surveyStatusService.changeProjectSeq(systemId, subjectId, fromProjectSeq, toProjectSeq);
        _IVideoResourceUpdateService.changeProjectSeq(systemId, subjectId, fromProjectSeq, toProjectSeq);
        // _videoResourceService.changeProjectSeq(systemId, subjectId, fromProjectSeq,
        // toProjectSeq);
    }

}
