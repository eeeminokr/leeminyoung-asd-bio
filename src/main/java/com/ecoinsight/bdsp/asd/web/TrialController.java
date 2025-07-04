package com.ecoinsight.bdsp.asd.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoinsight.bdsp.asd.Constants;
import com.ecoinsight.bdsp.asd.model.ImageSeriesModel;
import com.ecoinsight.bdsp.asd.model.TrialQcStatusJsonObject;
import com.ecoinsight.bdsp.asd.model.TrialQcStatusModel;
import com.ecoinsight.bdsp.asd.model.imaging.ImagingDataJsonObject;
import com.ecoinsight.bdsp.asd.model.imaging.ImagingDataModel;
import com.ecoinsight.bdsp.asd.model.imaging.ImagingDataStatus;
import com.ecoinsight.bdsp.asd.model.imaging.ImagingTrialDataJsonObject;
import com.ecoinsight.bdsp.asd.model.imaging.ImagingTrialDataStatus;
import com.ecoinsight.bdsp.asd.model.imaging.ImagingTrialStatusModel;
import com.ecoinsight.bdsp.core.MemberActivityConstants;
import com.ecoinsight.bdsp.core.entity.ImageInfo;
import com.ecoinsight.bdsp.core.entity.ImageSeries;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.entity.ProjectTrialItem;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.repository.IOrganizationRepository;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.repository.IProjectTrialItemRepository;
import com.ecoinsight.bdsp.core.repository.mongo.IImageResourceRepository;
import com.ecoinsight.bdsp.core.repository.mongo.IProjectSubjectRepository;
import com.ecoinsight.bdsp.core.service.IProjectService;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.util.FileUtil;
import com.ecoinsight.bdsp.core.util.ZipUtil;
import com.ecoinsight.bdsp.core.web.BaseApiController;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

import springfox.documentation.annotations.ApiIgnore;


@ApiIgnore
@RestController
public class TrialController extends AsdBaseApiController {    
    @Value("${ecoinsight.imaging.classification-dir}")
    private String imagingDataThumbnailPath;
    
    @Autowired
    private IOrganizationRepository _organizationRepository;

    @Autowired
    private IProjectSubjectRepository _projectSubjectRepository;

    @Autowired
    private IProjectTrialItemRepository _projectTrialItemRepository;

    @Autowired
    private IImageResourceRepository _imageResourceRepository;

    @Autowired
    private IProjectRepository _projectRepository;

    @Autowired
    @Qualifier("projectService")
    private IProjectService _projectService;

    @Autowired
    private MongoTemplate _mongoTemplate;

    private final Logger LOGGER = LoggerFactory.getLogger(TrialController.class);

    /**
     * 임상시험 데이터 통합 현황 조회 (영상, MCD 모두).
     * with Paging.
     * @param params
     * @return
     */
    @GetMapping(path="/api/v1/trials/q/")
	public ResponseEntity<JsonResponseObject> searchTrialDataStatus(
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
            }
            catch (NumberFormatException pex) {
                page = 1;
            }
        }
        if (params.containsKey("offset")) {
            try {
                offset = Integer.parseInt(params.get("offset").toString());
                if (offset <= 0 || offset >= ListDataModel.MAX_ROW_COUNT) {
                    offset = ListDataModel.DEFAULT_ROW_COUNT;
                }
            }
            catch (NumberFormatException pex) {
                offset = ListDataModel.DEFAULT_ROW_COUNT;
            }
        }

        /*
            Mongo DB Aggregation - Match
        */
        Document filterDoc = new Document("systemId", systemId);
        Document matchDoc = new Document("$match", filterDoc);

        long projectId = 0;
        boolean orgFiltered = false;
        int accessLevel = 0b0111111;

        if (params != null && !params.isEmpty()) {
            for (String k : params.keySet()) {
                Object v = params.get(k);

                if (k.equals("id") && v != null) {
                    filterDoc.append("projectSeq", (projectId = Long.parseLong(v.toString())));

                    String rolename = "";
                    // 과제에서 부여된 사용자 권한 조회
                    final Optional<ProjectMember> researcherOptional = this._projectRepository.findResearcher(projectId, userName);
                    if (researcherOptional.isEmpty()) {
                        rolename = super.getHighestRole(userName).getRoleId();
                    }
                    else {
                        rolename = researcherOptional.get().getRoleId();
                    }
            
                    accessLevel = super.calculateAccessLevel(rolename);
                }
                if (k.equals("subject") && v != null) {
                    filterDoc.append("subjectId", v.toString());
                }
                if (k.equals("org") && v != null) {
                    orgFiltered = true;
                    filterDoc.append("orgId", v.toString());
                }
            }
        }

        // Admin, Manager, QC User는 모든 연구기관의 데이터 조회 가능.
        if (!super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel) && !super.canQC(accessLevel) && !orgFiltered) {
            final String org = super.getOrgId();
            filterDoc.append("orgId", org);
        }

        var trialItems = this._projectTrialItemRepository.findTrialItems(systemId, projectId);
        if (trialItems == null || trialItems.isEmpty()) {
            LOGGER.error(String.format("No found trial items. [System=%s, Project=%s]", systemId, projectId));
            return ErrorResponseEntity("임상시험 데이터가 존재하지 않습니다.");
        }

        var agg = Arrays.asList(
            matchDoc, 
            new Document("$lookup", 
                new Document("from", "ImageInfo")
                    .append("localField", "_id")
                    .append("foreignField", "projectSubjectId")
                    .append("pipeline", Arrays.asList(new Document("$project", 
                        new Document("_id", true)
                            .append("trialIndex", true)
                            .append("hasUserCompletedQC", true)
                            .append("series.name", true)
                            .append("series.qcStatus", true)
                            .append("series.studyDate", true))))
                    .append("as", "imagingData")),
            new Document("$sort", 
                new Document("subjectId", 1L))
        );

        List<TrialQcStatusModel> list = new ArrayList<TrialQcStatusModel>();
        this._mongoTemplate
            .getMongoDatabaseFactory()
            .getMongoDatabase()
            .getCollection("projectSubject")
            .aggregate(agg, TrialQcStatusModel.class)
            .forEach(val->{
                list.add(val);
        });

        List<TrialQcStatusJsonObject> results = new ArrayList<TrialQcStatusJsonObject>();
        final String notValidName = "ETC";

        // Distinct trial indexx
        final HashSet<ProjectTrialAreaKey> trialGroups = new HashSet<ProjectTrialAreaKey>();
        final HashSet<Integer> trialIndexx = new HashSet<Integer>();
        trialItems.forEach((o) -> {
            trialGroups.add(new ProjectTrialAreaKey(o.getProjectSeq(), o.getTrialIndex(), o.getResearchAreaGroupId()));
            trialIndexx.add(o.getTrialIndex());
        });
        
        list.forEach(subject->{
            final List<ImagingTrialDataStatus> imagingData = subject.getImagingData();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Calculation Status in trial data. [Subject Id={}, Imaging Count={}]", 
                    subject.getId(), 
                    imagingData != null ? imagingData.size() : 0);
            }

            final String subjectId = subject.getId();                        
            
            final String researchAreaGroupImaging = "BIOMARKER";
            
            trialIndexx.forEach((index)->{
                final TrialQcStatusJsonObject result = new TrialQcStatusJsonObject(subject);
                result.setTrialIndex(index);

                // Initialize with 대상아님.
                result.addAreaQcStatus(researchAreaGroupImaging, Constants.IMAGING_QC_NO_SUBJECT);
                
                if (subject.hasUserCompletedQC()) {
                    result.addAreaQcStatus(researchAreaGroupImaging, Constants.IMAGING_QC_DONE_QC);
                }
                else if (trialGroups.stream().filter(f->f.trialIndex == index && researchAreaGroupImaging.equals(f.groupName)).findFirst().isPresent()) {
                    if (imagingData == null || imagingData.isEmpty()) {
                        // Upload된 영상데이터가 없으면 NO_DATA
                        result.setDateUpdated(new Date());
                        result.addAreaQcStatus(researchAreaGroupImaging, Constants.IMAGING_QC_NO_DATA);
                    }
                    else {
                        imagingData.stream().filter(p->p.getTrialIndex() == index).findFirst().ifPresentOrElse((data)->{
                            final String imageInfoId = data.getId();
                            result.setImageInfoId(imageInfoId);
                            
                            /*
                                BIOMARKER 데이터의 QC 상태 체크
                                영상데이터 중 하나라도 미등록이면, 통합페이지 상태는 미등록.
                                영상데이터 중 하나라도 미검증이면, 통합페이지의 상태는 미검증.
                                전체 영상데이터가 완료이어야 통합페이지의 상태는 완료.
                            */
                            final List<ImagingDataStatus> items = data.getItems();

                            if (items == null || items.isEmpty()) {
                                result.addAreaQcStatus(researchAreaGroupImaging, Constants.IMAGING_QC_NO_DATA);
                            }
                            else {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Checking QC status. [Subject Id={}, Project Subject Id={}, Trial Index={}, Data Count={}]", subjectId, subject.getId(), index, items.size());
                                }

                                // 임상시험 영상 데이터 모든 항목의 값 존재 유무 체크
                                List<ProjectTrialItem> indexTrialItems = new ArrayList<ProjectTrialItem>();
                                trialItems.stream().filter(t->t.getTrialIndex() == index
                                    && (t.getResearchAreaId().equals("BIOMARKER") || t.getResearchAreaId().equals("IMAGING_PET"))).forEach(m->indexTrialItems.add(m));

                                int countOfDoneQC = 0;
                                int countOfName = 0;

                                for (int i=indexTrialItems.size()-1; i>=0; i--) {
                                    for (int j=items.size()-1; j>=0; j--) {
                                        final var s = items.get(j);
                                        if (s.getName().equals(notValidName)) continue;
                                        if (s.getName().equalsIgnoreCase(indexTrialItems.get(i).getItemName())) {
                                            countOfName ++;

                                            if (s.isQced()) {
                                                countOfDoneQC ++;
                                            }
                                        }
                                    }
                                }
                                
                                if (countOfName - indexTrialItems.size() != 0) {
                                    result.addAreaQcStatus(researchAreaGroupImaging, Constants.IMAGING_QC_NO_DATA);
                                }
                                else if (countOfDoneQC - indexTrialItems.size() == 0) {
                                    result.addAreaQcStatus(researchAreaGroupImaging, Constants.IMAGING_QC_DONE_QC);
                                }
                                else {
                                    result.addAreaQcStatus(researchAreaGroupImaging, Constants.IMAGING_QC_NO_QC);
                                }
                            }
                        },
                        () -> {
                            result.addAreaQcStatus(researchAreaGroupImaging, Constants.IMAGING_QC_NO_DATA);
                        });
                    }
                }

                results.add(result);
            });            
        });

        List<TrialQcStatusJsonObject> pagingItems = results.stream().skip((page - 1) * offset).limit(offset).collect(Collectors.toList());
        final int total = results.size();

        return OkResponseEntity("임상 데이터를 조회했습니다.", new ListDataModel<TrialQcStatusJsonObject>(pagingItems, total, page, offset));
	}

    /**
     * 영상데이터가 업로드된 대상자 수를 조회한다.
     * @param params
     * @return
     */
    @GetMapping(path="/api/v1/trials/imaging/subjects-count/")
	public ResponseEntity<JsonResponseObject> countSubjectsHavingImagingData(
        @RequestParam Map<String, Object> params) {
        final String systemId = super.getSystemId();
        final String userName = super.getAuthenticatedUsername();

        /*
            Mongo DB Aggregation - Match
        */
        Document filterDoc = new Document("systemId", systemId);
        Document matchDoc = new Document("$match", filterDoc);

        long projectId = 0;
        boolean orgFiltered = false;
        int accessLevel = 0b0111111;

        if (params != null && !params.isEmpty()) {
            for (String k : params.keySet()) {
                Object v = params.get(k);

                if (k.equals("id") && v != null) {
                    filterDoc.append("projectSeq", (projectId = Long.parseLong(v.toString())));

                    String rolename = "";
                    // 과제에서 부여된 사용자 권한 조회
                    final Optional<ProjectMember> researcherOptional = this._projectRepository.findResearcher(projectId, userName);
                    if (researcherOptional.isEmpty()) {
                        rolename = super.getHighestRole(userName).getRoleId();
                    }
                    else {
                        rolename = researcherOptional.get().getRoleId();
                    }
            
                    accessLevel = super.calculateAccessLevel(rolename);                    
                }
                if (k.equals("subject") && v != null) {
                    filterDoc.append("subjectId", v.toString());
                }
                if (params.containsKey("gender")) {
                    filterDoc.append("gender", params.get("gender").toString());
                }
                if (k.equals("org") && v != null) {
                    filterDoc.append("orgId", v.toString());
                    orgFiltered = true;
                }
            }
        }

        // Admin, Manager, QC User는 모든 연구기관의 데이터 조회 가능.
        if (!super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel) && !super.canQC(accessLevel) && !orgFiltered) {
            final String org = super.getOrgId();
            filterDoc.append("orgId", org);
        }

        var agg = Arrays.asList(
            matchDoc, 
            new Document("$lookup", 
                new Document("from", "ImageInfo")
                    .append("localField", "_id")
                    .append("foreignField", "projectSubjectId")
                    .append("pipeline", Arrays.asList(new Document("$project", 
                        new Document("count", 
                        new Document("$size", 
                        new Document("$filter", 
                        new Document("input", "$series")
                                            .append("as", "data")
                                            .append("cond", 
                        new Document("$ne", Arrays.asList("$$data.name", "ETC")))))))))
                    .append("as", "imageData")), 
            new Document("$match", 
            new Document("imageData.0.count", 
            new Document("$gt", 0L))), 
            new Document("$count", "total"));

        var resultDoc = this._mongoTemplate
            .getMongoDatabaseFactory()
            .getMongoDatabase()
            .getCollection("projectSubject")
            .aggregate(agg).first();

        int total = resultDoc == null || resultDoc.isEmpty() ? 0 : resultDoc.getInteger("total");

        return OkResponseEntity("영상 데이터가 업로드된 대상자 수를 조회했습니다.", total);
	}

    /**
     * 영상 데이터 현황 조회.
     * with Paging
     * @param params
     * @return
     */
    @GetMapping(path="/api/v1/trials/imaging/q/")
	public ResponseEntity<JsonResponseObject> searchImagingTrialDataStatus(
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
            }
            catch (NumberFormatException pex) {
                page = 1;
            }
        }
        if (params.containsKey("offset")) {
            try {
                offset = Integer.parseInt(params.get("offset").toString());
                if (offset <= 0 || offset > ListDataModel.MAX_ROW_COUNT) {
                    offset = ListDataModel.DEFAULT_ROW_COUNT;
                }
            }
            catch (NumberFormatException pex) {
                offset = ListDataModel.DEFAULT_ROW_COUNT;
            }
        }

        /*
            Mongo DB Aggregation - Match
        */
        Document filterDoc = new Document("systemId", systemId);
        Document matchDoc = new Document("$match", filterDoc);

        long projectId = 0;
        boolean orgFiltered = false;
        int accessLevel = 0b0111111;

        if (params != null && !params.isEmpty()) {
            for (String k : params.keySet()) {
                Object v = params.get(k);

                if (k.equals("id") && v != null) {
                    filterDoc.append("projectSeq", (projectId = Long.parseLong(v.toString())));

                    String rolename = "";
                    // 과제에서 부여된 사용자 권한 조회
                    final Optional<ProjectMember> researcherOptional = this._projectRepository.findResearcher(projectId, userName);
                    if (researcherOptional.isEmpty()) {
                        rolename = super.getHighestRole(userName).getRoleId();
                    }
                    else {
                        rolename = researcherOptional.get().getRoleId();
                    }
            
                    accessLevel = super.calculateAccessLevel(rolename);                    
                }
                if (k.equals("subject") && v != null) {
                    filterDoc.append("subjectId", v.toString());
                }
                if (params.containsKey("gender")) {
                    filterDoc.append("gender", params.get("gender").toString());
                }                
                if (k.equals("org") && v != null) {
                    filterDoc.append("orgId", v.toString());
                    orgFiltered = true;
                }
            }
        }

        // Admin, Manager, QC User는 모든 연구기관의 데이터 조회 가능.
        if (!super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel) && !super.canQC(accessLevel) && !orgFiltered) {
            final String org = super.getOrgId();
            filterDoc.append("orgId", org);
        }

        var agg = Arrays.asList(
            matchDoc, 
            new Document("$lookup", 
                new Document("from", "ImageInfo")
                    .append("localField", "_id")
                    .append("foreignField", "projectSubjectId")
                    .append("pipeline", 
                        Arrays.asList(
                            new Document("$project", 
                                new Document("_id", true)
                                    .append("trialIndex", true)
                                    .append("dateUpdated", true)
                                    .append("series.name", true)
                                    .append("series.qcStatus", true)
                                    .append("series.studyDate", true))))
                .append("as", "imagingDataRaw")),
            new Document("$addFields", 
                new Document("imagingData", 
                    new Document("$map", 
                    new Document("input", "$imagingDataRaw")
                        .append("as", "data")
                        .append("in",                         
                            new Document("_id", "$$data._id")
                                .append("trialIndex", "$$data.trialIndex")
                                .append("series", 
                                    new Document("$map", 
                                        new Document("input", "$$data.series")
                                            .append("as", "series")
                                            .append("in", 
                                                new Document("name", new Document("$toUpper", "$$series.name"))
                                                        .append("qc", 
                                                            new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$$series.qcStatus", true)), "DONE_QC", "NO_QC"))
                                                        )
                                                        .append("studyDate", "$$series.studyDate")
                                            )
                                    )
                                )
                        )
                    ))), 
            new Document("$project", 
            new Document("_id", true)
                    .append("subjectId", true)
                    .append("gender", true)
                    .append("age", true)
                    .append("orgId", true)
                    .append("orgName", true)
                    .append("projectSeq", true)
                    .append("birthDay", true)
                    .append("imagingData", true)), 
            new Document("$sort", new Document("subjectId", 1L).append("imagingData._id", 1L))
        );

        List<ImagingTrialStatusModel> list = new ArrayList<ImagingTrialStatusModel>();
        this._mongoTemplate
            .getMongoDatabaseFactory()
            .getMongoDatabase()
            .getCollection("projectSubject")
            .aggregate(agg, ImagingTrialStatusModel.class)
            .forEach(val->{
                list.add(val);
        });

        LOGGER.debug("Found trial data in mongo. [Count={}]", list.size());

        var results = new ArrayList<ImagingTrialDataJsonObject>();
        var trialItems = this._projectTrialItemRepository.findTrialItems(systemId, projectId);
        if (trialItems == null || trialItems.isEmpty()) {
            LOGGER.error(String.format("No found trial items. [System=%s, Project=%s]", systemId, projectId));
            return ErrorResponseEntity("임상시험 데이터가 존재하지 않습니다.");
        }

        LOGGER.debug("Found trial items in the project. [Count={}]", trialItems.size());
        
        // Distinct trial indexx
        HashSet<Integer> trialIndexx = new HashSet<Integer>();
        trialItems.forEach((o) -> trialIndexx.add(o.getTrialIndex()) );

        list.forEach(subject->{
            final List<ImagingTrialDataStatus> imagingData = subject.getImagingData();
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Processing imaging data. [Subject Id={}, Data Count={}]", subject.getId(), imagingData != null ? imagingData.size() : 0);
            }

            final String subjectId = subject.getId();
            
            if (imagingData != null && !imagingData.isEmpty()) {
                trialIndexx.forEach((index) -> {
                    var result = new ImagingTrialDataJsonObject(subject);
                    result.setTrialIndex(index);

                    var d = imagingData.stream().filter(p->p.getTrialIndex() == index).findFirst();

                    imagingData.stream().filter(p->p.getTrialIndex() == index).findFirst().ifPresent((data)->{
                        result.setImageInfoId(data.getId());

                        final List<ImagingDataStatus> items = data.getItems();                        

                        trialItems.stream().filter(p->p.getTrialIndex() == index).forEach(trialItem->{
                            //final String itemName = trialItem.getItemName().toUpperCase();

                            if (items != null && !items.isEmpty()) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Processing trial data. [Subject Id={}, Project Subject Id={}, Trial Index={}, Data Count={}]", subjectId, subject.getId(), index, items.size());
                                }

                                items.stream().forEach(item->{
                                    // Set study date (BIOMARKER)
                                    if (StringUtils.hasText(item.getStudyDate())) {
                                        result.setStudyDate(item.getStudyDate());
                                    }
                                    
                                    result.addQcStatus(item.getName(), item.getQcStatus());
                                });
                            }
                        });  
                    });

                    results.add(result);
                });
            }
            else {
                final HashMap<Integer, HashMap<String, String>> trialQcItems = new HashMap<Integer, HashMap<String, String>>();
                
                trialItems.forEach(t->{                    
                    final String itemName = t.getItemName().toUpperCase();
                    final int trialIndex = t.getTrialIndex();

                    if (trialQcItems.containsKey(trialIndex)) {
                        trialQcItems.get(trialIndex).put(itemName, Constants.IMAGING_QC_NO_DATA);
                    }
                    else {
                        trialQcItems.put(trialIndex, new HashMap<>(){ { put(itemName, Constants.IMAGING_QC_NO_DATA); } });                        
                    }
                });

                trialQcItems.forEach((key, value) -> {
                    var result = new ImagingTrialDataJsonObject(subject);
                    result.setQcStatus(value);
                    result.setTrialIndex(key);

                    results.add(result);
                });
            }
        });

        final long count = results.stream().count();
        List<ImagingTrialDataJsonObject> pagingItems = results.stream().skip((page - 1) * offset).limit(offset).collect(Collectors.toList()); 
        
        // 사용자 활동 로그 기록
        writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_IMAGING_SEARCH);

        return OkResponseEntity("영상 현황 데이터를 조회했습니다.", new ListDataModel<ImagingTrialDataJsonObject>(pagingItems, count, page, offset));
	}

    /**
     * 영상 데이터 정보 조회
     * @param itemId
     * @return
     */
    @GetMapping(path="/api/v1/trials/imaging/{id}/")
	public ResponseEntity<JsonResponseObject> getImagingTrialDataDetail(
        @PathVariable("id") String itemId) {
            
        var item = this._imageResourceRepository.findById(itemId);

        if (item.isEmpty()) {
            LOGGER.error("No imaging data found. [ImageInfo Id={}]", itemId);
            return ErrorResponseEntity("영상 데이터가 존재하지 않습니다.");
        }

        final String systemId = super.getSystemId();
        final ImageInfo imagingData = item.get();
        final long projectId = imagingData.getProjectSeq();
        final String userName = super.getAuthenticatedUsername();

        String rolename = "";
        // 과제에서 부여된 사용자 권한 조회
        final Optional<ProjectMember> researcherOptional = this._projectRepository.findResearcher(projectId, userName);
        if (researcherOptional.isEmpty()) {
            rolename = super.getHighestRole(userName).getRoleId();
        }
        else {
            rolename = researcherOptional.get().getRoleId();
        }

        int accessLevel = super.calculateAccessLevel(rolename);

        /*
         * 사용자 권한이 Admin, Manager, QC User가 아닐 경우, 해당 사용자의 기관이 프로젝트에 참여한 기관인지 체크한다.
         */
        if (!(super.canQueryAll(accessLevel) || super.canQueryOrg(accessLevel) || super.canQC(accessLevel))) {
            final String userOrgId = super.getOrgId();
            List<ProjectMember> researchers = this._projectRepository.findAllResearchers(projectId, systemId);
            if (researchers.stream().filter(p->p.getOrgId().equals(userOrgId)).findAny().isEmpty()) {
                return ErrorResponseEntity("영상 데이터를 관리할 수 있는 기관에 속하지 않습니다.");
            }
        }
        
        final String orgId = imagingData.getOrgId();

        LOGGER.debug("Found imaging data. [ImageInfo Id={}, Org={}]", itemId, orgId);

        if (!StringUtils.hasText(orgId)) {
            LOGGER.error("Org Id is empty in ImageInfo. [ImageInfo Id={}]", itemId);
            return ErrorResponseEntity("영상 데이터에 기관정보가 존재하지 않습니다.");
        }
        
        var org = this._organizationRepository.findById(orgId, systemId);

        if (org.isEmpty()) {
            LOGGER.error(String.format("No organization found [Org Id=%s, ImageInfo Id=%s]", orgId, itemId));
            return ErrorResponseEntity("영상 데이터의 기관정보를 찾을 수 없습니다.");
        }

        imagingData.setOrgName(org.get().getOrgName());

        LOGGER.debug("Found organization. [Org={}, {}]", orgId, imagingData.getOrgName());

        //final int trialIndex = imagingData.getTrialIndex();
        final String subjectId = imagingData.getProjectSubjectId();

        // var trialItems = this._projectTrialItemRepository.findProjectTrialItems(systemId, projectId, trialIndex);
        // trialItems.sort(new Comparator<ProjectTrialItem>(){
        //     @Override
        //     public int compare(ProjectTrialItem o1, ProjectTrialItem o2) {
        //         return o1.getItemName().compareToIgnoreCase(o2.getItemName());
        //     }            
        // });

        // LOGGER.debug("Found trial items. [Count={}, Project={}, Trial={}]", trialItems.size(), projectId, trialIndex);

        var subject = this._projectSubjectRepository.findById(subjectId);
        if (subject.isEmpty()) {
            LOGGER.error(String.format("No subject found [Subject Id=%s, ImageInfo Id=%s]", subjectId, itemId));
            return ErrorResponseEntity("참여대상자 정보를 찾을 수 없습니다.");
        }

        ImagingDataJsonObject result = new ImagingDataJsonObject();        
        result.setSubject(subject.get());
        //result.setTrialItems(trialItems);
        result.setHasUserCompletedQC(imagingData.hasUserCompletedQC());

        List<ImagingDataModel> itemNameData = new ArrayList<ImagingDataModel>();

        // Set QC Status for researcher items (NO_DATA, NO_QC, DONE_QC)
        // trialItems.stream().filter(p->(
        //         p.getResearchAreaId().equals("BIOMARKER") || p.getResearchAreaId().equals("IMAGING_PET")) 
        //         && !p.getItemName().equalsIgnoreCase("ETC"))
        //     .forEach(n->{
        //     final String itemName = n.getItemName();
        //     final String areaName = n.getResearchAreaId();

        //     final ImagingDataModel data = new ImagingDataModel(itemName.toUpperCase());

        //     boolean found = false;
        //     for (ImageSeries series : imagingData.getSeries()) {

        //         if (LOGGER.isDebugEnabled()) {
        //             LOGGER.debug("Checking trial item in ImageSeries. [Area={}, Item Name={}, Series={}]", areaName, itemName, series.getName());
        //         }

        //         if (series.getName().equalsIgnoreCase(itemName)) {
        //             // Set thumbnail file (png)
        //             final String baseUrl = "/bdsp/imaging/thumbnail";
        //             final String pngPath = series.getPngPath(); 
                                 
        //             data.setPngFile(series.getPngFile());
        //             data.setComment(series.getComment());
                    
        //             if (pngPath != null && !StringUtils.hasText(series.getPngFile())) {
        //                 Optional<Path> file = getThumbnailFiles(Path.of(pngPath));
        //                 file.ifPresent(f-> {
        //                     data.setPngPath(pngPath);
        //                     data.setPngFile(f.toString().replaceFirst(this.imagingDataThumbnailPath, baseUrl));
        //                 });
    
        //                 if (LOGGER.isDebugEnabled()) {
        //                     LOGGER.debug("Set thumbnail file. [Item Name={}, PNG Path={} -> {}]", itemName, pngPath, series.getPngFile());
        //                 }
        //             }
        //             else {
        //                 LOGGER.warn("Thumbnail PNG path is null in mongo. [Item Name={}]", itemName);
        //             }

        //             // Set nii_file
                    
        //             final String niiPath = series.getNiiPath();
        //             data.setNiiFile(series.getNiiFile());

        //             if (niiPath != null && !StringUtils.hasText(series.getNiiFile())) {
        //                 Optional<Path> file = getNiiFiles(Path.of(niiPath));
        //                 file.ifPresent(f-> {
        //                     data.setNiiPath(niiPath);
        //                     data.setNiiFile(f.toString().replaceFirst(this.imagingDataThumbnailPath, baseUrl));
        //                 });
    
        //                 if (LOGGER.isDebugEnabled()) {
        //                     LOGGER.debug("Set nii file. [Item Name={}, nii Path={} -> {}]", itemName, niiPath, series.getNiiFile());
        //                 }
        //             }
        //             else {
        //                 LOGGER.warn("nii path is null in mongo. [Item Name={}]", itemName);
        //             }

        //             // Set QC status
        //             final boolean status = series.isQcStatus();
        //             result.addItemQcStatus(itemName, status ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_QC);
        //             data.setOldStatus(status);

        //             if (LOGGER.isDebugEnabled()) {
        //                 LOGGER.debug("Checking QC Status in ImageSeries. [Area={}, Item Name={}, Series={}, QC={}]", areaName, itemName, series.getName(), status);
        //             }

        //             // Set upload date
        //             // Research Area별로 검사일자를 저장. (날짜 순서와 무관하게 날짜가 존재하면 무조건 업데이트)
        //             final String date = series.getStudyDate();
        //             if (date != null) {
        //                 result.addItemAreaStudyDate(areaName, date);

        //                 if (LOGGER.isDebugEnabled()) {
        //                     LOGGER.debug("Checking study date in ImageSeries. [Area={}, Item Name={}, Series={}, Study Date={}]", areaName, itemName, series.getName(), date);
        //                 }
        //             }
        //             else {
        //                 if (LOGGER.isDebugEnabled()) {
        //                     LOGGER.warn("Study date is null in ImageSeries. [Area={}, Item Name={}, Series={}, Study Date=NULL]", areaName, itemName, series.getName());
        //                 }
        //             }

        //             found = true;
        //             break;
        //         }
        //     }
        //     if (!found) {
        //         result.addItemQcStatus(itemName, Constants.IMAGING_QC_NO_DATA);

        //         if (LOGGER.isDebugEnabled()) {
        //             LOGGER.warn("No item found in ImageSeries. Setting QC status as NO_DATA. [Area={}, Item Name={}]", areaName, itemName);
        //         }
        //     }

        //     itemNameData.add(data);
        // });

        for (ImageSeries series : imagingData.getSeries()) {
            final String itemName = series.getName();
            final ImagingDataModel data = new ImagingDataModel(itemName.toUpperCase());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking trial item in ImageSeries. [Item Name={}]", itemName);
            }
            
            // Set thumbnail file (png)
            final String baseUrl = "/bdsp/imaging/thumbnail";
            final String pngPath = series.getPngPath(); 
                            
            data.setPngFile(series.getPngFile());
            data.setComment(series.getComment());
            
            if (pngPath != null && !StringUtils.hasText(series.getPngFile())) {
                Optional<Path> file = getThumbnailFiles(Path.of(pngPath));
                file.ifPresent(f-> {
                    data.setPngPath(pngPath);
                    data.setPngFile(f.toString().replaceFirst(this.imagingDataThumbnailPath, baseUrl));
                });

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Set thumbnail file. [Item Name={}, PNG Path={} -> {}]", itemName, pngPath, series.getPngFile());
                }
            }
            else {
                LOGGER.warn("Thumbnail PNG path is null in mongo. [Item Name={}]", itemName);
            }

            // Set nii_file
            
            final String niiPath = series.getNiiPath();
            data.setNiiFile(series.getNiiFile());

            if (niiPath != null && !StringUtils.hasText(series.getNiiFile())) {
                Optional<Path> file = getNiiFiles(Path.of(niiPath));
                file.ifPresent(f-> {
                    data.setNiiPath(niiPath);
                    data.setNiiFile(f.toString().replaceFirst(this.imagingDataThumbnailPath, baseUrl));
                });

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Set nii file. [Item Name={}, nii Path={} -> {}]", itemName, niiPath, series.getNiiFile());
                }
            }
            else {
                LOGGER.warn("nii path is null in mongo. [Item Name={}]", itemName);
            }

            // Set QC status
            final boolean status = series.isQcStatus();
            result.addItemQcStatus(itemName, status ? Constants.IMAGING_QC_DONE_QC : Constants.IMAGING_QC_NO_QC);
            data.setOldStatus(status);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking QC Status in ImageSeries. [Item Name={}, QC={}]", itemName, status);
            }

            // Set upload date
            // Research Area별로 검사일자를 저장. (날짜 순서와 무관하게 날짜가 존재하면 무조건 업데이트)
            final String date = series.getStudyDate();
            if (date != null) {
                result.addItemAreaStudyDate(itemName, date);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Checking study date in ImageSeries. [Item Name={}, Study Date={}]", itemName, date);
                }
            }
            else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.warn("Study date is null in ImageSeries. [Item Name={}, Study Date=NULL]", itemName);
                }
            }

            itemNameData.add(data);
        }
        // if (!found) {
        //     result.addItemQcStatus(itemName, Constants.IMAGING_QC_NO_DATA);

        //     if (LOGGER.isDebugEnabled()) {
        //         LOGGER.warn("No item found in ImageSeries. Setting QC status as NO_DATA. [Area={}, Item Name={}]", areaName, itemName);
        //     }
        // }

        result.setImageData(itemNameData);

        return OkResponseEntity("영상 데이터를 조회했습니다.", result);
	}

    /**
     * 영상 데이터 QC 처리
     * @param itemId
     * @param model
     * @return
     */
    @PutMapping(path="/api/v1/trials/imaging/{id}/qc/")
    public ResponseEntity<JsonResponseObject> changeQcStatus(
        @PathVariable("id") String itemId, 
        @RequestBody ImagingDataModel model) {
        final String worker = super.getAuthenticatedUsername();
        
        var item = this._imageResourceRepository.findById(itemId);

        if (item.isEmpty()) {
            LOGGER.error("No imaging data found. [ImageInfo Id={}]", itemId);
            return ErrorResponseEntity("영상 데이터가 존재하지 않습니다.");
        }

        ImageInfo imageInfo = item.get();

        if (!StringUtils.hasText(model.getItemName())) {
            imageInfo.setHasUserCompletedQC(model.isUserCompletedAllQC());
            imageInfo.setDateUpdated(new Date());
            imageInfo.setUserUpdated(worker);

            this._imageResourceRepository.save(imageInfo);

            // 사용자 활동 로그 기록
        writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_IMAGING_QC, 
            String.format("영상 QC (과제=%s, 차수=%s, 대상자=%s, 영상=%s) %s 수동 처리.", imageInfo.getProjectSeq(), imageInfo.getTrialIndex(), imageInfo.getSubjectId(), itemId, model.isUserCompletedAllQC() ? "완료" : "미완료"));

        }
        else {
            LOGGER.debug("Changing QC Status. [Item={}, {}, {}]", itemId, model.getItemName(), model.getNewStatus());
            imageInfo.getSeries().forEach(s->{
                if (s.getName().equalsIgnoreCase(model.getItemName())) {
                    s.setQcStatus(model.getNewStatus());
                    s.setDateUpdated(new Date());
                    s.setUserUpdated(worker);
                }
            });

            this._imageResourceRepository.save(imageInfo);

            // 사용자 활동 로그 기록
            writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_IMAGING_QC, 
                String.format("영상 QC (과제=%s, 차수=%s, 대상자=%s, 영상=%s, QC=%s) 변경", imageInfo.getProjectSeq(), imageInfo.getTrialIndex(), imageInfo.getSubjectId(), itemId, model.getNewStatus()));
        }
        
        return OkResponseEntity("QC 상태를 변경했습니다.", model);
    }

    @PutMapping(path="/api/v1/trials/imaging/{id}/comment/")
    public ResponseEntity<JsonResponseObject> saveComment(
        @PathVariable("id") String itemId, 
        @RequestBody ImagingDataModel model) {
        final String worker = super.getAuthenticatedUsername();
        
        var item = this._imageResourceRepository.findById(itemId);

        if (item.isEmpty()) {
            LOGGER.error("No imaging data found. [ImageInfo Id={}]", itemId);
            return ErrorResponseEntity("영상 데이터가 존재하지 않습니다.");
        }

        LOGGER.debug("Saving comment. [Item={}, {}, {}]", itemId, model.getItemName(), model.getComment());

        ImageInfo imageInfo = item.get();
        imageInfo.getSeries().forEach(s->{
            if (s.getName().equalsIgnoreCase(model.getItemName())) {
                s.setComment(model.getComment());
                s.setDateUpdated(new Date());
                s.setUserUpdated(worker);
            }
        });

        this._imageResourceRepository.save(imageInfo);

        // 사용자 활동 로그 기록
        writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_IMAGING_CHANGE, 
            String.format("영상 코멘트 (과제=%s, 차수=%s, 대상자=%s, 영상=%s) 변경", imageInfo.getProjectSeq(), imageInfo.getTrialIndex(), imageInfo.getSubjectId(), model.getItemName()));

        return OkResponseEntity("코멘트를 저장했습니다.", imageInfo);
    }

    /**
     * 영상 데이터의 시리즈 삭제
     * @param itemId
     * @param models
     * @return
     */
    @DeleteMapping(path="/api/v1/trials/imaging/{id}/series/")
    public ResponseEntity<JsonResponseObject> deleteSeries(
        @PathVariable("id") String itemId,
        final @RequestBody(required = true) ImageSeriesModel[] models) {
        int len = 0;
        if (models == null || (len = models.length) <= 0) {
            return ErrorResponseEntity("요청을 처리할 정보가 부족합니다.");
        }

        try {
            
            String[] seriesNames = new String[len];
            for (int i=0; i<len; i++) {
                seriesNames[i] = models[i].getName();
            }
            
            this._projectService.deleteImagingMongoData(itemId, seriesNames);

            for (int i=0; i<len; i++) {
                // 사용자 활동 로그 기록
                writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_IMAGING_REMOVE, 
                String.format("영상 (영상=%s, %s) 삭제", itemId, seriesNames[i]));        
            }            
        }
        catch (Exception ex) {
            return ErrorResponseEntity(ex.getLocalizedMessage());
        }

        return OkResponseEntity("영상 series를 삭제했습니다.");
    }

    /**
     * 대상자의 모든 임상시험 데이터 삭제
     * @param projectId
     * @param trialIndex
     * @param subjectId
     * @return
     */
    @DeleteMapping(path="/api/v1/projects/{projectId}/trials/{trialIndex}/subjects/{subjectId}/")
    public ResponseEntity<JsonResponseObject> deleteTrialData(
        final @PathVariable("projectId") long projectId, 
        final @PathVariable("trialIndex") int trialIndex,
        final @PathVariable("subjectId") String subjectId) {
        final String systemId = super.getSystemId();



        LOGGER.debug("trialIndex={}",trialIndex);
    

        if (!this._projectService.deleteTrialData(systemId, projectId, trialIndex, subjectId)) {
            return ErrorResponseEntity("임상시험 데이터를 삭제하지 못했습니다.");
        }

        // 사용자 활동 로그 기록
        writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_IMAGING_REMOVE, 
            String.format("영상 (과제=%s, 차수=%s, 대상자=%s) 삭제", projectId, trialIndex, subjectId));
        
        writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_MCD_REMOVE, 
            String.format("MCD (과제=%s, 차수=%s, 대상자=%s) 삭제", projectId, trialIndex, subjectId));        

        return OkResponseEntity("데이터를 삭제했습니다.");
    }

    /**
     * 대상자의 영상 데이터 삭제
     * @param projectId
     * @param trialIndex
     * @param subjectId
     * @return
     */
    @DeleteMapping(path="/api/v1/projects/{projectId}/trials/{trialIndex}/subjects/{subjectId}/imaging/")
    public ResponseEntity<JsonResponseObject> deleteImagingData(
        final @PathVariable("projectId") long projectId, 
        final @PathVariable("trialIndex") int trialIndex,
        final @PathVariable("subjectId") String subjectId) {
        final String systemId = super.getSystemId();

        final long deleted = this._projectService.deleteImagingMongoData(systemId, projectId, trialIndex, subjectId);

        // 사용자 활동 로그 기록
        writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_IMAGING_REMOVE, 
            String.format("영상 (과제=%s, 차수=%s, 대상자=%s) 삭제", projectId, trialIndex, subjectId));
        
        
        return OkResponseEntity("영상 데이터를 삭제했습니다.", deleted);
    }

    /**
     * 선택한 영상 데이터 (Mongo DB _id 기준)를 다운로드 한다.
     * @param filters
     * @return
     */
    @PostMapping(path="/api/v1/trials/imaging/download/")
	public ResponseEntity<?> downloadImagingData(
        @RequestBody String[] selectedSubjectId) {
        final String systemId = super.getSystemId();
        final String userName = super.getAuthenticatedUsername();

        final String[] idx = selectedSubjectId;
        if (idx == null || idx.length <= 0) {
            return ErrorResponseEntity("다운로드 받을 데이터를 선택하지 않았습니다.");
        }

        Query query = new Query(Criteria.where("systemId").is(systemId));
        query.addCriteria(Criteria.where("projectSubjectId").in(List.of(idx)));
        List<ImageInfo> items = this._mongoTemplate.find(query, ImageInfo.class);
        
        final List<Path> zipFiles = new ArrayList<Path>();
        for (ImageInfo imaging : items) {
            Set<ImageSeries> series = imaging.getSeries();
            if (series == null) continue;

            final List<Path> files = new ArrayList<Path>();
            final List<String> imageTypeNames = new ArrayList<String>();

            series.forEach(s->{
                final String seriesName = s.getName();
                if (seriesName == null || "ETC".equals(seriesName)) return;

                final String dicomPath = s.getDicomPath();
                final String niiPath = s.getNiiPath();

                if (StringUtils.hasText(dicomPath)) {
                    files.add(Path.of(dicomPath));
                    files.add(Path.of(dicomPath + ".zip")); // Dicom 파일은 디렉토리에 압축 해제되지 않고 zip 파일로 존재하는 경우도 있음.
                }
                if (StringUtils.hasText(niiPath)) {
                    files.add(Path.of(niiPath + ".zip"));
                }
                
                imageTypeNames.add(seriesName);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Dicom file to download. [Path={}]", dicomPath);
                    LOGGER.debug("Dicom file to download. [Path={}.zip]", dicomPath);
                    LOGGER.debug("NII file to download. [Path={}.zip]", niiPath);
                }
            });

            if (files.size() <= 0) continue;

            try {
                var zip = makeZipfile(files, imaging.getProjectSeq(), imaging.getSubjectId(), imaging.getProjectSubjectId(), imaging.getTrialIndex(), imageTypeNames, imaging.getId());
                zipFiles.add(zip);
            }
            catch (Exception ex) {
                LOGGER.error("Error while making zip file for imaging data. [Subject={}, Id={}, User={}]", imaging.getSubjectId(), imaging.getId(), userName);
            }
        }

        if (zipFiles.size() <= 0) return ErrorResponseEntity("다운로드할 영상파일이 없습니다.");

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating zip file to download. [Count={}]", zipFiles.size());
            }

            final String fileName = "ImagingData_" + DateUtil.getShortDateTimeString() + ".zip";
            File zipFile = ZipUtil.makeZip(zipFiles, fileName);
            byte[] content = Files.readAllBytes(zipFile.toPath());

            // Delete all temp files.
            FileUtil.deleteAll(zipFile.getParentFile());

            return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                String.format("%s; filename=%s", fileName, fileName))
                .body(new InputStreamResource(new ByteArrayInputStream(content)));
        } catch (Exception ex) {
            LOGGER.error("Error while creating zip file to download imaging data.", ex);
        }
        finally{
            // Delete temp files
            for (Path p : zipFiles) {
                FileUtil.deleteAll(p.getParent().toFile());
            }
        }
        
        return ErrorResponseEntity("영상파일 다운로드에 실패했습니다.");
    }

    private Path makeZipfile (List<Path> files, final long projectId, final String subjectId, final String projectSubjectId, final int trialIndex, List<String> imageTypeNames, final String id) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            for (Path f : files) {
                try {
                    LOGGER.debug("Checking if imaging files (Dicom, NII) exists. [Path={}, Exist={}]", f, Files.exists(f));
                }
                catch(Exception ex){}
            }
        }
        
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating zip file to download. [Subject={}, Trial={}, Count={}]", subjectId, trialIndex, files.size());
            }

            Path zip = ZipUtil.makeZip(files, subjectId + "_차수-" + trialIndex + ".zip").toPath();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Created zip file to download. [Subject={}, Trial={}, Path={}]", subjectId, trialIndex, zip);
            }

            return zip;
        } catch (Exception ex) {
            LOGGER.error("Error while creating zip file to download imaging data.", ex);
            throw ex;
        }
    }

    private static Optional<Path> getThumbnailFiles(Path path) {
        try {
            return Files.list(path).findFirst();
        }
        catch (IOException ioex) {
            return Optional.empty();
        }
    }

    private static Optional<Path> getNiiFiles(Path path) {
        try {
            return Files.list(path).filter(f -> f.toString().endsWith(".gz")).findFirst();
        }
        catch (IOException ioex) {
            return Optional.empty();
        }
    }

    private class ProjectTrialAreaKey {
        public long projectSeq;
        public int trialIndex;
        public String groupName;

        public ProjectTrialAreaKey(long project, int index, String name) {
            this.projectSeq = project;
            this.trialIndex = index;
            this.groupName = name;
        }

        @Override
        public int hashCode() {
            return ObjectUtils.nullSafeHashCode(new Object[]{ this.projectSeq, this.trialIndex, this.groupName });
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ProjectTrialAreaKey) {
                final ProjectTrialAreaKey other =  (ProjectTrialAreaKey) obj;
                return (other.groupName.equals(this.groupName) && other.projectSeq - this.projectSeq == 0 && other.trialIndex - this.trialIndex == 0);
            }

            return false;
        }
    }
}
