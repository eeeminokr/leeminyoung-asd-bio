package com.ecoinsight.bdsp.asd.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.xalan.lib.sql.ObjectArray;
import org.dcm4che3.data.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import com.ecoinsight.bdsp.asd.image.CompressedFile;
import com.ecoinsight.bdsp.asd.image.DefacingProcessor;
import com.ecoinsight.bdsp.asd.image.ImageClassProcessor;
import com.ecoinsight.bdsp.asd.model.ImageModel;
import com.ecoinsight.bdsp.asd.model.ImageSeriesModel;
import com.ecoinsight.bdsp.asd.model.ImageType;
import com.ecoinsight.bdsp.asd.model.ProjectSeq;
import com.ecoinsight.bdsp.asd.model.TaskProgress;
import com.ecoinsight.bdsp.asd.model.bids.BIDSFactory;
import com.ecoinsight.bdsp.asd.model.bids.BaseDef;
import com.ecoinsight.bdsp.asd.model.imaging.ImagingDataProcessProgress;
import com.ecoinsight.bdsp.asd.model.kbds.KBDSFactory;
import com.ecoinsight.bdsp.asd.model.kbds.KMRIDef;
import com.ecoinsight.bdsp.asd.model.kbds.KPETDef;
import com.ecoinsight.bdsp.asd.service.TaskProgressService;
import com.ecoinsight.bdsp.core.MemberActivityConstants;
import com.ecoinsight.bdsp.core.entity.ImageClassRule;
import com.ecoinsight.bdsp.core.entity.ImageInfo;
import com.ecoinsight.bdsp.core.entity.ImageSeries;
import com.ecoinsight.bdsp.core.entity.MongoKeyGenerator;
import com.ecoinsight.bdsp.core.entity.ProjectSubject;
import com.ecoinsight.bdsp.core.repository.IImageClassRuleRepository;
import com.ecoinsight.bdsp.core.repository.mongo.IImageResourceRepository;
import com.ecoinsight.bdsp.core.repository.mongo.IProjectSubjectRepository;
import com.ecoinsight.bdsp.core.util.DateUtil;
import com.ecoinsight.bdsp.core.util.FileUtil;
import com.ecoinsight.bdsp.core.util.ProcessRunner;
import com.ecoinsight.bdsp.core.util.ZipUtil;
import com.ecoinsight.bdsp.core.web.BaseApiController;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import springfox.documentation.annotations.ApiIgnore;

/**
 * 
 */
@ApiIgnore
@RequestScope
@RestController
@RequestMapping(path = "/api/v1/imaging")
public class ImageResourceController extends BaseApiController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Value("#{'${ecoinsight.imaging.nii.ignore-keywords}'.split(',')}")
    private List<String> _niiIgnoreKeywords;

    @Value("${ecoinsight.imaging.upload-dir}")
    private String _uploadPath;
    @Value("${ecoinsight.imaging.decompress-dir}")
    private String _decompressPath;
    @Value("${ecoinsight.imaging.classification-dir}")
    private String _classificationPath;

    @Value("${ecoinsight.imaging.dcm2niix-path}")
    private String _dcm2niix;
    @Value("${ecoinsight.imaging.thumbnail-dti-path}")
    private String _tuhmbnailDTI;
    @Value("${ecoinsight.imaging.thumbnail-pet-path}")
    private String _tuhmbnailPET;
    @Value("${ecoinsight.imaging.thumbnail-path}")
    private String _thumbnail;

    @Value("${ecoinsight.imaging.keep-uploaded-file:false}")
    private Boolean _keepUploadedFile;
    @Value("${ecoinsight.imaging.keep-dicomzip-file:true}")
    private Boolean _keepDicomZipFile;

    @Autowired
    @Qualifier("taskProgressService")
    private TaskProgressService _taskProgressService;
    @Autowired
    private IImageClassRuleRepository _classRuleRepository;
    @Autowired
    private IImageResourceRepository _imageResourceRepository;
    @Autowired
    private IProjectSubjectRepository _subjectRepository;
    @Autowired
    private DefacingProcessor _defacingProcessor;
    @Autowired
    private ImageClassProcessor _imageClassProcessor;

    private List<ImageClassRule> _classRules;

    private static final long FIXED_PROJECT_SEQ = 4L;

    @Autowired
    private ResourceLoader resourceLoader;

    public ImageResourceController() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("-> Created " + this.getClass().getName());
        }
    }

    @PostConstruct
    public void startup() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> startup : _niiIgnoreKeywords=" + this._niiIgnoreKeywords);
            LOGGER.info("-> startup : _uploadPath=" + this._uploadPath);
            LOGGER.info("-> startup : _decompressPath=" + this._decompressPath);
            LOGGER.info("-> startup : _classificationPath=" + this._classificationPath);
        }
    }

    /**
     * FOR tsting
     */
    @GetMapping("/info/{taskId}")
    public Map<String, Object> info(@PathVariable String taskId) {
        String worker = this.getAuthenticatedUsername();
        TaskProgress taskProgress = new TaskProgress();
        taskProgress.setTaskId(taskId);
        taskProgress.setWorker(worker);
        if (!this._taskProgressService.containsKey(taskId, worker)) {
            this._taskProgressService.addTaskProgress(taskProgress);
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("upload-path", this._uploadPath);
        m.put("decompress-path", this._decompressPath);
        m.put("classification-path", this._classificationPath);
        m.put("task-id-list", this._taskProgressService.getAllTaskIds());
        m.put("this", this.toString());
        m.put("user", worker);

        return m;
    }

    /**
     * BIDS tags json API for imageId and item.
     * 
     * imageId : Image ID of ImageInfo in mongoDB.
     * item : item name of Image in ImageInfo. T1, T2, FLAIR
     */
    @GetMapping("/bids/tags/{imageId}/{item}")
    public void getBIDSTagJson(@PathVariable("imageId") String imageId, @PathVariable("item") String item,
            HttpServletResponse response) throws Exception {
        Optional<ImageInfo> optional = this._imageResourceRepository.findById(imageId);
        if (optional.isEmpty()) {
            ErrorResponseEntity("Cannot find an ImageInfo. - id=" + imageId);
        }

        String jsonPath = null;
        ImageInfo info = optional.get();
        for (ImageSeries is : info.getSeries()) {
            if (is.getName().equals(item)) {
                jsonPath = is.getJsonPath();
                break;
            }
        }

        if (jsonPath == null) {
            throw new Exception("Cannot find Image item or jsonPath is null.", null);
        }

        File file = new File(jsonPath);
        if (!file.exists()) {
            throw new Exception("Cannot find json file. - " + jsonPath);
        }
        response.setContentType("application/json charset=utf-8");
        InputStream instream = null;
        OutputStream outstream = null;
        try {
            instream = new FileInputStream(file);
            outstream = response.getOutputStream();
            byte[] buff = new byte[1024];
            while (true) {
                int l = instream.read(buff);
                if (l == -1)
                    break;
                outstream.write(buff, 0, l);
                outstream.flush();
            }
        } finally {
            try {
                if (instream != null)
                    instream.close();
            } catch (Exception e) {
            }
            try {
                if (outstream != null)
                    outstream.close();
            } catch (Exception e) {
            }
        }

    }

    /**
     * BIDS Tags description
     * 
     */
    @GetMapping("/bids/tags/description")
    public void getBIDSTagDescription(HttpServletResponse response) throws Exception {
        final String path = "classpath:imaging/bids_tags.json";
        response.setContentType("application/json charset=utf-8");

        Resource resource = this.resourceLoader.getResource(path);
        if (resource == null) {
            throw new Exception("Cannot find tag description file. - " + path);
        }
        InputStream instream = null;
        OutputStream outstream = null;
        try {
            instream = resource.getInputStream();
            outstream = response.getOutputStream();
            byte[] buff = new byte[1024];
            while (true) {
                int l = instream.read(buff);
                if (l == -1)
                    break;
                outstream.write(buff, 0, l);
                outstream.flush();
            }
        } finally {
            try {
                if (instream != null)
                    instream.close();
            } catch (Exception e) {
            }
            try {
                if (outstream != null)
                    outstream.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * KBDS tags json API for imageId and item.
     * 
     * imageId : Image ID of ImageInfo in mongoDB.
     * item : item name of Image in ImageInfo. T1, T2, FLAIR
     * 
     * @param response
     * @throws Exception
     */
    @GetMapping("/kbds/tags/{imageId}/{item}")
    public void getKBDSTagJson(@PathVariable("imageId") String imageId, @PathVariable("item") String item,
            HttpServletResponse response) throws Exception {
        Optional<ImageInfo> optional = this._imageResourceRepository.findById(imageId);
        if (optional.isEmpty()) {
            ErrorResponseEntity("Cannot find an ImageInfo. - id=" + imageId);
        }

        String jsonPath = null;
        ImageInfo info = optional.get();
        for (ImageSeries is : info.getSeries()) {
            if (is.getName().equals(item)) {
                jsonPath = is.getJsonPathKBDS();
                break;
            }
        }

        if (jsonPath == null) {
            throw new Exception("Cannot find Image item or jsonPathKBDS is null.", null);
        }

        File file = new File(jsonPath);
        if (!file.exists()) {
            throw new Exception("Cannot find json file. - " + jsonPath);
        }
        response.setContentType("application/json charset=utf-8");
        InputStream instream = null;
        OutputStream outstream = null;
        try {
            instream = new FileInputStream(file);
            outstream = response.getOutputStream();
            byte[] buff = new byte[1024];
            while (true) {
                int l = instream.read(buff);
                if (l == -1)
                    break;
                outstream.write(buff, 0, l);
                outstream.flush();
            }
        } finally {
            try {
                if (instream != null)
                    instream.close();
            } catch (Exception e) {
            }
            try {
                if (outstream != null)
                    outstream.close();
            } catch (Exception e) {
            }
        }

    }

    /**
     * KBDS tags json description
     * 
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping("/kbds/tags/description")
    public ResponseEntity<Object> getKBDSTagDescription(HttpServletResponse response) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        for (KMRIDef item : KMRIDef.values()) {
            if (map.containsKey(item.name()) || !item.isEnabledUI())
                continue;
            map.put(item.name(), item.getDescription());
        }

        for (KPETDef item : KPETDef.values()) {
            if (map.containsKey(item.name()) || !item.isEnabledUI())
                continue;
            map.put(item.name(), item.getDescription());
        }

        return ResponseEntity.ok().body(map);
    }

    /**
     * Tag information response API of a dicom file.
     * 
     * @param id ImageInfo id
     * @return
     * @throws Exception
     */
    @Deprecated
    @GetMapping("/dicom/tag-data/{id}")
    public ResponseEntity<JsonResponseObject> getDicomFileInformation(@PathVariable("id") String id) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        Optional<ImageInfo> optional = this._imageResourceRepository.findById(id);
        if (optional.isEmpty())
            return ErrorResponseEntity("Cannot find the ImageInfo. id=" + id);

        ImageInfo info = optional.get();
        Set<ImageSeries> series = info.getSeries();
        if (series == null || series.size() == 0)
            return ErrorResponseEntity("There is no uploaded series for this ImageInfo. id=" + id);

        ImageSeries is = series.iterator().next();
        String path = is.getDicomPath();
        File zipFile = new File(path + ".zip");
        if (!zipFile.exists())
            return ErrorResponseEntity("Cannot find the dicom zip file. - " + zipFile.getAbsolutePath());

        String baseName = FilenameUtils.getBaseName(zipFile.getName());
        File targetDir = new File("/tmp", baseName);
        if (!targetDir.exists())
            targetDir.mkdirs();

        ZipUtil.unzipFile(zipFile, targetDir);
        File[] files = targetDir.listFiles();
        File file = files.length == 0 ? null : files[0];
        if (file == null)
            return ErrorResponseEntity("Cannot find the dicom file. - " + targetDir.getAbsolutePath());

        map = this._imageClassProcessor.getAttributeMap(file);
        return OkResponseEntity("Success", map);
    }

    /**
     * ImageInfo response API
     * 
     * @param projectSeq
     * @param subjectId
     * @param trialIndex
     * @return
     */
    @GetMapping("/{projectSeq}/{subjectId}/{orgId}/{trialIndex}") // projectSeq, trialIndex, orgId, subjectId
    public ResponseEntity<JsonResponseObject> getImageInfo(
            @PathVariable("projectSeq") long projectSeq,
            @PathVariable("subjectId") String subjectId,
            @PathVariable("orgId") String orgId,
            @PathVariable("trialIndex") int trialIndex) {
        final String systemId = super.getSystemId();
        String id = this.id(projectSeq, trialIndex, orgId, subjectId, systemId);
        Optional<ImageInfo> optional = this._imageResourceRepository.findById(id);
        if (optional.isEmpty()) {
            ErrorResponseEntity("Cannot find an ImageInfo. - id=" + id);
        }
        return OkResponseEntity("Success", optional.get());
    }

    @GetMapping("/upload/files/progress/{taskId}/")
    public ResponseEntity<JsonResponseObject> getProgress(@PathVariable String taskId) {
        String worker = this.getAuthenticatedUsername();

        if (this._taskProgressService.containsKey(taskId, worker)) {
            TaskProgress progress = this._taskProgressService.getTaskProgress(taskId, worker);

            LOGGER.debug("The progress of the task => {}, {}/{} ", taskId, progress.getProcessedTask(),
                    progress.getTotalTask());

            return OkResponseEntity(
                    "The progress of the task " + taskId,
                    new JsonResponseObject(true,
                            new ImagingDataProcessProgress(progress.getProcessedTask(), progress.getTotalTask())));
        } else {
            return ErrorResponseEntity("No task found.");
            // "The progress of the task " + taskId,
            // new JsonResponseObject(true, new ImagingDataProcessProgress(0, 0)));
        }
    }

    @PostMapping("/upload/file/{taskId}")
    public ResponseEntity<JsonResponseObject> uploadFile(
            @RequestParam("file") MultipartFile mfile,
            @PathVariable("taskId") String taskId,
            @ModelAttribute ImageModel model) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("-> upload File=" + mfile.getOriginalFilename() + ", " + model);
        }

        if (mfile == null) {
            return ErrorResponseEntity("Cannot find uploaded file. Please, select a uploading file.");
        }

        MultipartFile[] mfiles = { mfile };
        return this.uploadFiles(mfiles, taskId, model, false);
    }

    @PostMapping("/upload/files/{taskId}")
    public ResponseEntity<JsonResponseObject> uploadFiles(
            @RequestParam("files") MultipartFile[] mfiles,
            @PathVariable("taskId") String taskId,
            @ModelAttribute ImageModel model) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("-> upload Files=" + mfiles.length + ", " + model);
        }

        if (mfiles == null || mfiles.length == 0) {
            return ErrorResponseEntity("Cannot find uploaded file. Please, select a uploading file.");
        }

        return this.uploadFiles(mfiles, taskId, model, true);
    }

    /**
     * update comment/remark of an ImageSeries
     */
    @PutMapping("/series/remark")
    public ResponseEntity<JsonResponseObject> updateSeriesRemark(@RequestBody ImageSeriesModel model) {
        final String systemId = super.getSystemId();
        String id = this.id(model.getProjectSeq(), model.getTrialIndex(), model.getOrgId(), model.getSubjectId(),
                systemId);
        Optional<ImageInfo> optional = this._imageResourceRepository.findById(id);
        if (optional.isEmpty()) {
            ErrorResponseEntity("Cannot find an ImageInfo. - id=" + id);
        }

        final String worker = super.getAuthenticatedUsername();
        ImageInfo info = optional.get();
        for (ImageSeries is : info.getSeries()) {
            if (is.getName().equals(model.getName())) {
                is.setRemark(model.getRemark());
                is.setDateUpdated(new Timestamp(System.currentTimeMillis()));
                is.setUserUpdated(worker);
                this._imageResourceRepository.save(info);

                // 사용자 활동 로그 기록
                writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_IMAGING_CHANGE,
                        String.format("영상 코멘트 (과제=%s, 차수=%s, 대상자=%s, 영상=%s) 변경", model.getProjectSeq(),
                                model.getTrialIndex(), model.getSubjectId(), model.getName()));

                return OkResponseEntity("Success");
            }
        }
        return ErrorResponseEntity("Cannot find ImageSeries. id=" + id + ", name=" + model.getName());
    }

    /**
     * TODO : T1 ANALYIZE
     * 
     * @param model
     * @return
     */
    @PutMapping("/series/qc-status")
    public ResponseEntity<JsonResponseObject> changeSeriesQCStatus(@RequestBody ImageSeriesModel model) {
        final String systemId = super.getSystemId();
        String id = this.id(model.getProjectSeq(), model.getTrialIndex(), model.getOrgId(), model.getSubjectId(),
                systemId);
        Optional<ImageInfo> optional = this._imageResourceRepository.findById(id);
        if (optional.isEmpty()) {
            ErrorResponseEntity("Cannot find an ImageInfo. - id=" + id);
        }

        final String worker = super.getAuthenticatedUsername();
        ImageInfo info = optional.get();
        for (ImageSeries is : info.getSeries()) {
            if (is.getName().equals(model.getName())) {
                is.setQcStatus(model.isQcStatus());
                is.setDateUpdated(new Timestamp(System.currentTimeMillis()));
                is.setUserUpdated(worker);
                this._imageResourceRepository.save(info);

                // 사용자 활동 로그 기록
                writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA, MemberActivityConstants.ACT_IMAGING_QC,
                        String.format("영상 QC (과제=%s, 차수=%s, 대상자=%s, 영상=%s, QC=%s) 변경", model.getProjectSeq(),
                                model.getTrialIndex(), model.getSubjectId(), model.getName(), model.isQcStatus()));

                return OkResponseEntity("Success");
            }
        }

        return ErrorResponseEntity("Cannot find ImageSeries. id=" + id + ", name=" + model.getName());
    }

    /**
     * task-1 : uploaded file decompression
     * task-2 : dicom file classification
     * task-3 : zip file compression
     * task-4 : Nifti file creation
     * task-5 : Defacing
     * task-6 : thumbnail file creation
     * 
     */
    private ResponseEntity<JsonResponseObject> uploadFiles(MultipartFile[] mfiles, String taskId, ImageModel model,
            boolean bulkUploaded) {
        List<MultipartFile> uploadFiles = new ArrayList<MultipartFile>();

        LOGGER.info("Started uploading files. count={}", mfiles == null ? 0 : mfiles.length);
        if (mfiles == null || mfiles.length <= 0) {
            return ErrorResponseEntity("처리할 영상파일이 없습니다.");
        }

        for (MultipartFile mfile : mfiles) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("-> upload File=" + mfile.getOriginalFilename());
            }
            String extension = FilenameUtils.getExtension(mfile.getOriginalFilename());
            if (!CompressedFile.isValidExtension(extension)) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Invalid file extension. Ignored file=" + mfile.getOriginalFilename());
                }
                continue;
            }

            if (mfile.getSize() == 0) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("The size of uploaded file is zero. Ignored file=" + mfile.getOriginalFilename());
                }
                continue;
            }
            uploadFiles.add(mfile);
        }

        if (uploadFiles.size() == 0)
            return ErrorResponseEntity("There is no valid uploaded file. Please, check uploaded files.");

        final String systemId = super.getSystemId();
        final String worker = super.getAuthenticatedUsername();
        final int totalTask = 6; // 1: file upload, 2~6: processing imaging data

        TaskProgress taskProgress = null;
        if (this._taskProgressService.containsKey(taskId, worker)) {
            taskProgress = this._taskProgressService.getTaskProgress(taskId, worker);
            taskProgress.reset();
        } else {
            taskProgress = new TaskProgress();
            taskProgress.setTaskId(taskId);
            taskProgress.setWorker(worker);
            this._taskProgressService.addTaskProgress(taskProgress);
        }

        taskProgress.setzTotal(uploadFiles.size());
        try {
            int zipFileCount = 0;
            for (MultipartFile mfile : uploadFiles) {
                taskProgress.setTotalTask(totalTask);
                taskProgress.setProcessedTask(1); // file upload completed.

                String fileName = mfile.getOriginalFilename();
                String extension = FilenameUtils.getExtension(fileName);
                String baseName = FilenameUtils.getBaseName(fileName);

                LOGGER.debug("Started processing upload file. File={}, Project={}, Subject={}, Trial={}", fileName,
                        model.getProjectSeq(), model.getSubjectId(), model.getTrialIndex());

                if (bulkUploaded) { // 대상자ID_차수명.확장자 - {대상자ID, 차수명}
                    try {
                        String[] fitems = FileUtil.parseFileName(baseName, '_');
                        model.setSubjectId(fitems[0].toUpperCase()); // Subject Id should be uppercase.
                        model.setTrialIndex(Integer.parseInt(fitems[1]));
                    } catch (RuntimeException runex) {
                        throw new Exception("파일명 명명규칙에 어긋납니다. 유효한 파일명은 '대상자ID_차수명.확장자' 형식입니다.", runex);
                    }

                    LOGGER.info("Multiple files are uploaded. Subject={}, Trial={}, File={}", model.getSubjectId(),
                            model.getTrialIndex(), baseName);
                }

                var subjects = this._subjectRepository.findAllBySubjectId(systemId, model.getSubjectId());
                if (subjects == null || subjects.size() <= 0) {
                    LOGGER.error(String.format("No subject found. File=%s, Project=%s, Subject=%s, Trial=%s", fileName,
                            model.getProjectSeq(), model.getSubjectId(), model.getTrialIndex()));

                    throw new Exception("대상자 정보를 찾을수 없습니다. - " + model.getSubjectId());
                }

                if (subjects.size() > 1) {
                    LOGGER.warn(String.format(
                            "Found multiple subjects. Use the first one. File=%s, Project=%s, Subject=%s, Trial=%s",
                            fileName, model.getProjectSeq(), model.getSubjectId(), model.getTrialIndex()));
                }

                ProjectSubject subject = subjects.stream().findFirst().get();

                LOGGER.debug("Found subject. Subject={}, Org={}", subject.getSubjectId(), subject.getOrgId());

                model.setOrgId(subject.getOrgId()); // to create id of ImageInfo
                this._classRules = this._classRuleRepository.getClassCriterias(systemId, model.getOrgId());
                if (this._classRules.size() == 0) {
                    return ErrorResponseEntity(
                            "Not found dicom classification rules. Please, register rules for this organization. - systemId="
                                    + systemId + ", organizationId=" + model.getOrgId());
                }

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("-> Loaded dicom classification rules(systemId=" + systemId + ", model.getOrgId()="
                            + model.getOrgId() + ") from database. - " + this._classRules);
                }

                taskProgress.setzTotalProcessed(++zipFileCount);
                // create upload and decompress directory,
                String yyyyMmDd = DateUtil.getCurrentDate();
                String timestamp = DateUtil.getShortDateTimeStampString();

                String uploadPath = String.format("%s/%s/%s/%s/%s", this._uploadPath, systemId, yyyyMmDd,
                        model.getSubjectId(), timestamp);
                String decompressPath = String.format("%s/%s/%s/%s/%s", this._decompressPath, systemId, yyyyMmDd,
                        model.getSubjectId(), timestamp);
                FileUtil.createDirectories(uploadPath);

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("-> Created a upload path. - " + uploadPath);
                }
                FileUtil.createDirectories(decompressPath);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("-> Created a decompress path. - " + decompressPath);
                }

                File targetFile = new File(uploadPath, fileName);
                mfile.transferTo(targetFile);

                LOGGER.debug("Upload file copied. Target={}", targetFile.getPath());

                ImageInfo info = null;
                String id = this.id(model.getProjectSeq(), model.getTrialIndex(), model.getOrgId(),
                        model.getSubjectId(), systemId);
                Optional<ImageInfo> optional = this._imageResourceRepository.findById(id);

                LOGGER.debug("Finding Image info in mongo-db.", id);

                if (optional.isPresent()) {
                    info = optional.get();
                    info.addOriginFilePath(targetFile.getAbsolutePath());
                    info.setDateUpdated(Calendar.getInstance().getTime());
                    info.setUserUpdated(worker);
                    var added = this._imageResourceRepository.save(info);
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("-> Updated the ImageInfo. - " + added);
                    }
                } else {
                    info = new ImageInfo();
                    info.setId(id);
                    info.setSystemId(systemId);
                    info.setOrgId(model.getOrgId());
                    info.setProjectSubjectId(MongoKeyGenerator.generateProjectSubjectId(systemId, model.getOrgId(),
                            model.getProjectSeq(), model.getSubjectId()));
                    info.setProjectSeq(model.getProjectSeq());
                    info.setTrialIndex(model.getTrialIndex());
                    info.setSubjectId(model.getSubjectId());
                    info.addOriginFilePath(targetFile.getAbsolutePath());
                    info.setDateCreated(Calendar.getInstance().getTime());
                    info.setUserCreated(worker);
                    info.setDataSource(ImageResourceController.class.getName());
                    var added = this._imageResourceRepository.save(info);
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("-> Created an ImageInfo. - " + added);
                    }
                }

                File decompressDir = new File(decompressPath, baseName);
                try {
                    LOGGER.debug("Start decompress. File={}, Decompress Path={}", targetFile.getName(),
                            decompressDir.getPath());

                    // 1. File Decompression
                    taskProgress.setTaskNum(1);
                    CompressedFile compFile = CompressedFile.valueOf(extension);
                    compFile.decompress(targetFile, decompressDir);

                    LOGGER.debug("1=>Completed decompress. File={}, Decompress Path={}", targetFile.getName(),
                            decompressDir.getPath());

                    taskProgress.setProcessedTask(taskProgress.getProcessedTask() + 1);

                    // 2. delete previous Etc dicom directory
                    ImageSeries etc = info.getSeries(ImageClassProcessor.ETC);
                    if (etc != null && etc.getDicomPath() != null) {
                        String path = etc.getDicomPath();
                        File etcDicomPath = new File(path);
                        FileUtil.deletePath(etcDicomPath);
                        info.removeSeries(etc);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("-> Deleted previous etc dicom directory. - " + path);
                        }
                    }

                    LOGGER.debug("2=>Delete previous Etc dicom directory. File={}, Decompress Path={}",
                            targetFile.getName(), decompressDir.getPath());

                    taskProgress.setProcessedTask(taskProgress.getProcessedTask() + 1);

                    // 3. dicom File Classification
                    taskProgress.setTaskNum(2);
                    doDicomClassification(decompressDir, targetFile, model, info, taskId, systemId);
                    LOGGER.debug("3=>Dicom File Classification. File={}, Decompress Path={}", targetFile.getName(),
                            decompressDir.getPath());

                    taskProgress.setProcessedTask(taskProgress.getProcessedTask() + 1);

                    // 4. zip file compression
                    taskProgress.setTaskNum(3);
                    taskProgress.setTotalProcessed(0);
                    doDicomZipProcess(info, taskId);

                    LOGGER.debug("4=>Zip file compression. File={}, Decompress Path={}", targetFile.getName(),
                            decompressDir.getPath());

                    taskProgress.setProcessedTask(taskProgress.getProcessedTask() + 1);

                    // 5. NifTi file creatioin
                    taskProgress.setTotalProcessed(0);
                    doNIIProcess(model, info, taskId);

                    LOGGER.debug("5=>NifTi file creatioin. File={}, Decompress Path={}", targetFile.getName(),
                            decompressDir.getPath());

                    taskProgress.setProcessedTask(taskProgress.getProcessedTask() + 1);

                } finally {
                    // 6. delete dicom and decompressed directories.
                    doDeleteDirectories(info, decompressDir);
                    this._imageResourceRepository.save(info);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Completed tasks. Tasks={}/{}, File={}, Id={}, Project={}, Subject={}, Trial={}.",
                                taskProgress.getProcessedTask(), taskProgress.getTotalTask(), fileName, info.getId(),
                                info.getProjectSeq(), info.getSubjectId(), info.getTrialIndex());
                    }

                    // Set total task as the final value because it is not clear how many tasks
                    // should be processed.
                    taskProgress.setProcessedTask(totalTask);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(
                                "Saved Image Info into mongo-db. File={}, Id={}, Project={}, Subject={}, Trial={}.",
                                fileName, info.getId(), info.getProjectSeq(), info.getSubjectId(),
                                info.getTrialIndex());
                    }
                }

                LOGGER.debug("Completed processing upload file. File={}", fileName);

                // // 사용자 활동 로그 기록
                // writeActivityLog(MemberActivityConstants.TYPE_TRIALDATA,
                // MemberActivityConstants.ACT_IMAGING_ADD, String.format("과제 %s(%s 차수)에 대상자 %s
                // 영상 등록.(파일 %s)", model.getProjectSeq(), model.getTrialIndex(),
                // model.getSubjectId(), fileName));
            }

            return OkResponseEntity("영상파일 업로드를 완료했습니다.", zipFileCount);
        } catch (Exception e) {
            LOGGER.error("Fail to process uploaded images. - " + e.getMessage(), e);
            return ErrorResponseEntity("Fail to process uploaded images. - " + e.getMessage(), e);
        } finally {
            if (taskProgress != null) {
                taskProgress.reset();
                this._taskProgressService.removeTaskProgress(taskId, worker);
            }

            LOGGER.info("Completed uploading files. count={}/{}", uploadFiles.size(),
                    mfiles == null ? 0 : mfiles.length);
        }
    }

    private void doDicomClassification(File sourcePath, File originFile, ImageModel model, ImageInfo info,
            String taskId, String systemId) throws Exception {
        String worker = this.getAuthenticatedUsername();
        TaskProgress taskProgress = this._taskProgressService.getTaskProgress(taskId, worker);

        File[] fileList = sourcePath.listFiles();
        if (fileList == null) {
            LOGGER.warn("Cannot find any file in the decompressed directory. - " + sourcePath.getAbsolutePath());
            return;
        }

        taskProgress.setTotal(fileList.length);

        String savePath = String.format("%s/%s/%s/%s", this._classificationPath, systemId, model.getProjectSeq(),
                model.getTrialIndex());

        LOGGER.debug("Start DicomClassification. File={}, System={}, Project={}, Trial={}, Task={}",
                sourcePath.getName(), systemId, model.getProjectSeq(), model.getTrialIndex(), taskId);
        LOGGER.debug("Path for DicomClassification. Path={}", savePath);

        for (int i = 0; i < fileList.length; i++) {
            File file = fileList[i];
            if (file.isDirectory()) {

                LOGGER.debug(
                        "Do DicomClassification in directory. Directory={}, System={}, Project={}, Trial={}, Task={}",
                        file.getAbsolutePath(), systemId, model.getProjectSeq(), model.getTrialIndex(), taskId);

                if (file.getName().equals("META-INF"))
                    continue;

                doDicomClassification(file, originFile, model, info, taskId, systemId);
                continue;
            }

            String fileName = file.getName().toUpperCase();
            String baseName = FilenameUtils.getBaseName(fileName);
            String extension = FilenameUtils.getExtension(fileName);

            String suffix = null;
            Attributes dataset = null;

            LOGGER.debug("File extension => {}", extension);

            if (CompressedFile.gz.equalsIgnoreCase(extension) || ImageClassProcessor.NII.equals(extension)) { // NII
                                                                                                              // file
                                                                                                              // upload
                LOGGER.debug("The file extension is gz or NII...");

                ImageClassRule rule = this._imageClassProcessor.findRuleByFileName(this._classRules, baseName);
                if (rule == null) {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Cannot find a rule for this uploaded nii file. - " + file.getAbsolutePath());
                    taskProgress.setTotalProcessed(i + 1);
                    continue;
                }

                LOGGER.debug("Found a rule for this uploaded nii file. File={}, Code={}", file.getAbsolutePath(),
                        rule.getCode());

                // T1 or flair nii file uploaded cases
                suffix = rule.getCode();

                String path = String.format("%s/%s/%s_%s_%s_nii", savePath, model.getSubjectId(), model.getTrialIndex(),
                        model.getSubjectId(), suffix);
                File destPath = new File(path);

                ImageSeries is = null; // new ImageSeries();
                Calendar current = Calendar.getInstance();
                if (info.hasSeries(suffix)) {

                    LOGGER.debug("Imaging Info has series. Series={}", suffix);

                    is = info.getSeries(suffix);
                    is.setUserUpdated(worker);
                    is.setDateUpdated(current.getTime());
                } else {

                    LOGGER.debug("Imaging Info has no series. Series={}", suffix);

                    is = new ImageSeries(suffix);
                    info.addSeries(is);
                    is.setUserUpload(worker);
                    is.setDateUpload(current.getTime());
                    is.setUserUpdated(worker);
                    is.setDateUpdated(current.getTime());
                }

                is.setType(ImageType.NII.name());
                if (FileUtil.createDirectories(destPath)) {
                    is.resetResults();
                    is.setUploading(true);

                    LOGGER.debug("Directory created. Path={}", destPath);
                }

                File destFile = new File(destPath, file.getName());
                FileUtil.copy(file, destFile);

                LOGGER.debug("File copied. Source={}, Target", file.getAbsolutePath(), destFile.getAbsolutePath());

                // /savePath/subjectId/trialIndex_subjectId_suffix_dicom
                String finalSavePath = String.format("%s/%s/%s_%s_%s_dicom", savePath, model.getSubjectId(),
                        model.getTrialIndex(), model.getSubjectId(), suffix);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("-> " + finalSavePath);
                }
                is.addResult("NIfTi file uploaded for this series.");
                is.setUploadedNii(true);
                is.setDicomPath(finalSavePath);
                this._imageResourceRepository.save(info);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("-> Updated Series(" + info.getId() + ") - " + is);
                }
            } else if (ImageClassProcessor.PAR.equalsIgnoreCase(extension)
                    || ImageClassProcessor.REC.equalsIgnoreCase(extension)) { // PAR/REC file
                LOGGER.debug("The file extension is not gz or NII... -  extension=" + extension);
                if (ImageClassProcessor.REC.equalsIgnoreCase(extension))
                    continue;

                /**
                 * PAR file handling
                 * 1. get Protocol name
                 * 2. replace header
                 * 
                 * # CLINICAL TRYOUT Research image export tool V4.2
                 * . Protocol name : Axial DTI 45dir SENSE
                 */
                Map<String, String> tagMap = this._imageClassProcessor.getTagMap(file);
                String protocolName = tagMap.get(ImageClassProcessor.PROTOCOL_NAME);
                ImageClassRule rule = null;
                if (protocolName == null) {
                    rule = ImageClassProcessor.ETCRULE;
                } else {
                    rule = this._imageClassProcessor.findRule(this._classRules, protocolName);
                }
                String parFileName = file.getName();
                String parBaseName = FilenameUtils.getBaseName(parFileName);
                File recFile = new File(file.getParentFile(), parBaseName + "." + ImageClassProcessor.REC);
                if (!recFile.exists()) {
                    LOGGER.warn("Cannot find the REC file. - " + file.getAbsolutePath());
                    rule = ImageClassProcessor.ETCRULE;
                }
                suffix = rule.getCode();
                LOGGER.debug("Found rule for PER file. Rule={}, Path={}", rule.getCode(), file.getAbsolutePath());

                ImageSeries is = null;
                Calendar current = Calendar.getInstance();
                if (info.hasSeries(suffix)) {
                    LOGGER.debug("Image Info has series. Series={}", suffix);
                    is = info.getSeries(suffix);
                } else {
                    LOGGER.debug("Image Info has no series. Series={}", suffix);

                    is = new ImageSeries(suffix);
                    // /savePath/subjectId/trialIndex_subjectId_suffix_dicom
                    String path = String.format("%s/%s/%s_%s_%s_dicom", savePath, model.getSubjectId(),
                            model.getTrialIndex(), model.getSubjectId(), suffix);
                    is.setDicomPath(path);
                    info.addSeries(is);
                }
                is.setType(ImageType.PARREC.name());
                is.setSupportDeface(rule.isSupportDeface());
                is.setUserUpdated(worker);
                is.setDateUpdated(current.getTime());

                File destPath = new File(is.getDicomPath());
                if (FileUtil.createDirectories(destPath)) {
                    is.resetResults();
                    is.setUploading(true);

                    LOGGER.debug("Directory created. Path={}", destPath);
                }
                is.setUpdatedTag(true);
                is.setProtocol(protocolName);
                is.setStudyDate(tagMap.get(ImageClassProcessor.EXAMINATION_DATETIME));
                is.addResult("Par/Rec files uploaded for this series.");
                // copy par and rec file to dicom folder
                if (recFile.exists()) {
                    // copy REC file
                    File destFile = new File(destPath, recFile.getName());
                    FileUtil.copy(recFile, destFile);
                    LOGGER.debug("File copied. Source={}, Target={}", recFile.getAbsolutePath(),
                            destFile.getAbsolutePath());
                }
                // copy PAR file - replace header
                File destFile = new File(destPath, file.getName());
                this._imageClassProcessor.copyPARFile(file, destFile);
                LOGGER.debug("File copied. Source={}, Target={}", file.getAbsolutePath(), destFile.getAbsolutePath());

                this._imageResourceRepository.save(info);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("-> Updated Series(" + info.getId() + ") - " + is);
                }
            } else { // DICOM file
                LOGGER.debug("The file extension is not gz or NII... -  extension=" + extension);

                LOGGER.debug("Getting dicom attributes => {}", file.getAbsolutePath());

                dataset = this._imageClassProcessor.getAttributes(file);
                if (dataset == null || !this._imageClassProcessor.isValidDicomFile(dataset)) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Dicom attributes is null or not valid dicom file. Path={}",
                                file.getAbsolutePath());
                    }
                    continue;
                }

                // handle classification of a dicom file
                ImageClassRule rule = this._imageClassProcessor.findRule(this._classRules, dataset);
                suffix = rule.getCode();

                LOGGER.debug("Found rule for dicom file. Rule={}, Path={}", rule.getCode(), file.getAbsolutePath());

                ImageSeries is = null; // new ImageSeries();
                Calendar current = Calendar.getInstance();
                if (info.hasSeries(suffix)) {
                    LOGGER.debug("Image Info has series. Series={}", suffix);
                    is = info.getSeries(suffix);
                } else {
                    LOGGER.debug("Image Info has no series. Series={}", suffix);

                    is = new ImageSeries(suffix);
                    // /savePath/subjectId/trialIndex_subjectId_suffix_dicom
                    String path = String.format("%s/%s/%s_%s_%s_dicom", savePath, model.getSubjectId(),
                            model.getTrialIndex(), model.getSubjectId(), suffix);
                    is.setDicomPath(path);
                    info.addSeries(is);
                }
                is.setType(ImageType.DICOM.name());
                is.setSupportDeface(rule.isSupportDeface());
                is.setUserUpdated(worker);
                is.setDateUpdated(current.getTime());

                File destPath = new File(is.getDicomPath());
                if (FileUtil.createDirectories(destPath)) {
                    is.resetResults();
                    is.setUploading(true);

                    LOGGER.debug("Directory created. Path={}", destPath);
                }

                File destFile = new File(destPath, file.getName());
                FileUtil.copy(file, destFile);

                LOGGER.debug("File copied. Source={}, Target={}", file.getAbsolutePath(), destFile.getAbsolutePath());

                LOGGER.debug("Image Series => isUpdatedTag={}, isUploading={}", is.isUpdatedTag(), is.isUploading());

                if (!is.isUpdatedTag() && is.isUploading()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("-> " + destPath);
                    }

                    is.setUpdatedTag(true);

                    // BIDS Tag JSon file
                    Map<String, String> map = BaseDef.toMap(dataset);
                    Map<String, String> additionalMap = BIDSFactory.toMap(is.getName(), dataset);
                    if (additionalMap == null) {
                        // exception handling for not matched series.
                        LOGGER.error("Not found BDIS json type. - " + is.getName());
                        is.setJsonPath("");
                        is.addResult("Not found BDIS json type. - " + is.getName());
                    } else {
                        map.putAll(additionalMap);
                        // create json file
                        String path = String.format("%s/%s/bids_%s_%s_%s.json", savePath, model.getSubjectId(),
                                model.getTrialIndex(), model.getSubjectId(), suffix);
                        File filePath = new File(path);
                        File parent = filePath.getParentFile();
                        if(!parent.exists()) parent.mkdirs();
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.writeValue(filePath, map);
                        is.setJsonPath(path);
                    }

                    // KBDS Tag JSon file
                    Map<String, String> kbdsMap = KBDSFactory.toMap(is.getName(), dataset);
                    if (kbdsMap == null) {
                        // exception handling for not matched series.
                        LOGGER.error("Not found KBDS json type. - " + is.getName());
                        is.setJsonPathKBDS("");
                        is.addResult("Not found KBDS json type. - " + is.getName());
                    } else {
                        String path = String.format("%s/%s/kbds_%s_%s_%s.json", savePath, model.getSubjectId(),
                                model.getTrialIndex(), model.getSubjectId(), suffix);
                        File filePath = new File(path);
                        File parent = filePath.getParentFile();
                        if(!parent.exists()) parent.mkdirs();
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.writeValue(filePath, map);
                        is.setJsonPathKBDS(path);
                    }

                    this._imageResourceRepository.save(info);
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("-> Updated Series(" + info.getId() + ") - " + is);
                    }
                }
            }
            taskProgress.setTotalProcessed(i + 1);
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> Dicom classification completed. - sourceDir=" + sourcePath.getAbsolutePath());
        }
    }

    /**
     * xxx_dicom zip processing
     * 
     * @param info
     * @param taskId
     * @return
     * @throws Exception
     */
    private void doDicomZipProcess(ImageInfo info, String taskId) throws Exception {
        String worker = this.getAuthenticatedUsername();
        TaskProgress taskProgress = this._taskProgressService.getTaskProgress(taskId, worker);

        LOGGER.debug("Started Dicom Zip Process... {}", info);

        Set<ImageSeries> series = info.getSeries();

        LOGGER.debug("Image Info Series => {}", series == null ? 0 : series.size());

        if (series == null || series.isEmpty())
            return;

        Instant start = Instant.now();
        File dicomDirPath = null;
        try {
            taskProgress.setTotal(info.getUploadingFileCount());
            taskProgress.setmTotal(info.getUploadingFileCount());

            ImageSeries[] items = series.toArray(new ImageSeries[series.size()]);
            for (int i = 0; i < items.length; i++) {

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Image Series => Name={}, isUploading={}", items[i].getName(), items[i].isUploading());
                }

                // skip Etc or not uploaded series
                if (ImageClassProcessor.ETC.equals(items[i].getName()) || !items[i].isUploading())
                    continue;

                dicomDirPath = new File(items[i].getDicomPath());
                if (!dicomDirPath.exists()) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("-> The dicom directory does not exist. - " + items[i].getDicomPath());
                    }
                    items[i].addResult("The dicom directory dose not exist.");
                    continue;
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Dicom Directory => {}", items[i].getDicomPath());
                }

                String zipName = dicomDirPath.getName();
                File parentPath = dicomDirPath.getParentFile();
                ZipUtil.makeZip(parentPath, zipName);
                items[i].addResult("The dicom directory is compressed.");
                taskProgress.setTotalProcessed(i + 1);
                taskProgress.setmTotalProcessed(i + 1);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("-> Created a zip file - " + dicomDirPath.getAbsolutePath() + ".zip");
                }
            }
        } catch (Exception e) {
            throw new Exception("Fail to make a zip file(" + dicomDirPath + "). - " + e.getMessage(), e);
        } finally {
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("-> Zip process completed. - " + (timeElapsed / 1000.) + " secs");
            }
        }
    }

    /**
     * 1. case-1 : NIfTi file upload (GZ or NII extension and T1 or FLAIR suffix)
     * 2. case-2 : dicom file upload
     * 
     */
    private void doNIIProcess(ImageModel model, ImageInfo info, String taskId) throws Exception {

        LOGGER.debug("Started NII Process...{}", info);

        Set<ImageSeries> series = info.getSeries();

        LOGGER.debug("Image Info Series =>{}", series == null ? 0 : series.size());

        if (series == null || series.isEmpty())
            return;

        String worker = this.getAuthenticatedUsername();
        TaskProgress taskProgress = this._taskProgressService.getTaskProgress(taskId, worker);
        taskProgress.setTaskNum(4); // Nifiti file creation
        taskProgress.setTotal(info.getUploadingFileCount());

        try {
            ImageSeries[] items = series.toArray(new ImageSeries[series.size()]);
            for (int i = 0; i < items.length; i++) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Image Series => Name={}, isUploading={}", items[i].getName(), items[i].isUploading());
                }

                // skip Etc or not uploaded series
                if (ImageClassProcessor.ETC.equals(items[i].getName()))
                    continue;

                if (!items[i].isUploading()) { // skip : not current upload series
                    // Set png file path for not uploaded series
                    File dicomPath = new File(items[i].getDicomPath());
                    File pngPath = new File(dicomPath.getAbsolutePath().replace("_dicom", "_png"));
                    if (!pngPath.exists())
                        continue;
                    for (File file : pngPath.listFiles()) {
                        String baseName = FilenameUtils.getBaseName(file.getName());
                        String pngFilePath = String.format("%s/%s.png", pngPath.getAbsolutePath(), baseName);

                        items[i].setPngPath(pngPath.getAbsolutePath());
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("-> Set a thumbnail file path. - " + pngFilePath);
                        }
                    }
                    continue;
                }

                File dicomPath = new File(items[i].getDicomPath());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("-> doNIIProcess >> dicomPath=" + dicomPath.getAbsolutePath());
                }

                File niiPath = new File(dicomPath.getAbsolutePath().replace("_dicom", "_nii"));
                // if xxx_nii path exists, delete it for zipPath=xxx_dicom
                // because xxx_nii path can be created by dcm2niix command : xxx_dicom to
                // xxx_nii
                if (!items[i].isUploadedNii() && niiPath.exists()) {
                    ZipUtil.deleteDir(niiPath);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("NII directory deleted. Path={}", niiPath.getAbsolutePath());
                    }
                }

                if (!FileUtil.createDirectories(niiPath)) {
                    if (LOGGER.isInfoEnabled())
                        LOGGER.info("-> The NII path already exists. - " + niiPath.getAbsolutePath());
                }

                if (!items[i].isUploadedNii()) { // dcm2niix : dicom to nii
                    // output file name : %p-protocol name, ${subjectId}_${trialIndex}_${image type}
                    String outputFileName = this.toFileName(model, items[i]);
                    String[] elements = { this._dcm2niix, "-f", outputFileName, "-a", "y", "-z", "y", "-o",
                            niiPath.getAbsolutePath(), dicomPath.getAbsolutePath() };
                    List<String> cmd = Arrays.asList(elements);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Executing dcm2niix. Command={} -f " + outputFileName + " -a y -z y -o {} {}",
                                this._dcm2niix, niiPath.getAbsolutePath(), dicomPath.getAbsolutePath());
                    }

                    new ProcessRunner(cmd).execute();
                    items[i].setNiiPath(niiPath.getAbsolutePath());
                    items[i].addResult("Completed creating a NIfTI file.");
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("-> Created nii files. - " + niiPath.getAbsolutePath());
                    }
                } else {
                    items[i].setNiiPath(niiPath.getAbsolutePath());
                    items[i].addResult("Skipped creating a NIfTI file due to NII file upload.");

                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Skipped creating a NIfTI file due to NII file upload.");
                    }
                }
                // niiPath : /1_SD-SSN-0002_T1_nii directory that contains result files of
                // dcm2niix command : 3D_Sagittal_T1_GRE_SENSE.json and
                // 3D_Sagittal_T1_GRE_SENSE.nii.gz
                for (File file : niiPath.listFiles()) {
                    String fileName = file.getName();

                    String baseName = FilenameUtils.getBaseName(fileName);
                    String extension = FilenameUtils.getExtension(fileName);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("NII file extension => {}", extension);
                    }

                    if (!"gz".equals(extension) && !"nii".equals(extension))
                        continue;

                    if (isThisNii(baseName)) {

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("NII file isUploadedNii={}, isSupportDeface={}", items[i].isUploadedNii(),
                                    items[i].isSupportDeface());
                        }

                        // Defacging in case of T1 or flair
                        if (!items[i].isUploadedNii() && items[i].isSupportDeface()) { // skip defacing for nii file
                                                                                       // upload.

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Calling defacing command. File={}", file.getAbsolutePath());
                            }

                            boolean result = this._defacingProcessor.doExecute(file, taskProgress);
                            items[i].addResult("Called defacing command. result=" + result);

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Called defacing command. result=" + result);
                            }

                            if (result) {
                                items[i].setDefaced(true);
                            }
                        } else {
                            items[i].addResult("Skipped defacing due to NII file upload(" + items[i].isUploadedNii()
                                    + ") or support defacing(" + items[i].isSupportDeface() + ").");

                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn("Skipped defacing due to NII file upload(" + items[i].isUploadedNii()
                                        + ") or support defacing(" + items[i].isSupportDeface() + ").");
                            }
                        }

                        // thumbnail file
                        taskProgress.setTaskNum(6);
                        File pngPath = new File(dicomPath.getAbsolutePath().replace("_dicom", "_png"));
                        ZipUtil.deleteDir(pngPath);

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Directory for thumbnail deleted. Path={}", pngPath);
                        }

                        FileUtil.createDirectories(pngPath);

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Directory for thumbnail created. Path={}", pngPath);
                        }

                        String pngFilePath = String.format("%s/%s.png", pngPath.getAbsolutePath(), baseName);

                        String[] elements = null;
                        if (ImageClassProcessor.DTI.equals(items[i].getName())) {
                            elements = new String[] { this._tuhmbnailDTI, file.getAbsolutePath(), pngFilePath };
                        } else if (ImageClassProcessor.PET.equals(items[i].getName())) {
                            elements = new String[] { this._tuhmbnailPET, file.getAbsolutePath(), pngFilePath };
                        } else {
                            elements = new String[] { this._thumbnail, file.getAbsolutePath(), pngFilePath };
                        }

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Executing thumbnail process. Command={} {} {}", elements[0], elements[1],
                                    elements[2]);
                        }

                        List<String> cmd = Arrays.asList(elements);
                        new ProcessRunner(cmd).execute();
                        items[i].setPngPath(pngPath.getAbsolutePath());
                        items[i].addResult("Completed creating a thumbnail file.");
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("-> Created a thumbnail file. - " + pngFilePath);
                        }

                        break; // after creating one png file, exit
                    } else { // invalid NIfTI file
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("-> Invalid NIfTi file. - " + file.getAbsolutePath());
                        }
                        items[i].addResult(
                                "Skipped defacing and creating a thumbnail file due to invalid NIfTI file. - fileName="
                                        + baseName);
                    }

                    this._imageResourceRepository.save(info);
                }

                // create a zip file from nii files.
                String zipName = niiPath.getName();
                File parent = dicomPath.getParentFile();
                ZipUtil.makeZip(parent, zipName);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("-> Created a zip file. - " + niiPath.getAbsolutePath());
                }

                taskProgress.setTotalProcessed(i + 1);
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("-> NII process completed.");
            }
        } catch (Exception e) {
            throw new Exception("Fail to perform nii process. - " + e.getMessage(), e);
        }
    }

    /**
     * dcm2niix output filename
     */
    private String toFileName(ImageModel model, ImageSeries series) {
        String fileName = String.format("%s_%s_%s", model.getSubjectId(), model.getTrialIndex(), series.getName());
        return fileName;
    }

    private void doDeleteDirectories(ImageInfo info, File decompressDir) {
        LOGGER.debug("Deleting directory. keepDicomZipFile={}, keepUploadedFile={} - {}", this._keepDicomZipFile,
                this._keepUploadedFile, info);

        // 5. dicom directories delete
        Set<ImageSeries> series = info.getSeries();
        if (series != null) {
            ImageSeries[] items = series.toArray(new ImageSeries[series.size()]);
            for (ImageSeries is : items) {
                if (!is.isUploading() || ImageClassProcessor.ETC.equals(is.getName()))
                    continue;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Deleting directory. Image Series => isUploading={}, Name={}, dicomPath={}",
                            is.isUploading(), is.getName(), is.getDicomPath());
                }

                // delete all dicom directory in classification dir.
                File dicomDirPath = new File(is.getDicomPath());
                if (!dicomDirPath.exists())
                    continue;
                ZipUtil.deleteDir(dicomDirPath);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("-> Deleted a dicom directory: " + dicomDirPath.getAbsolutePath());
                }

                File dicomZipPath = new File(is.getDicomPath() + ".zip");
                if (dicomZipPath.exists() && !this._keepDicomZipFile) {
                    boolean result = dicomZipPath.delete();
                    is.addResult("Deleted dicom zip file due to defaced " + is.getName() + ", result=" + result);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Deleted dicom zip file due to defaced " + is.getName() + ", result=" + result);
                    }
                }
            }
        }

        // delete origin uploaded file.
        if (!this._keepUploadedFile) {
            List<String> paths = info.getOriginFilePaths();
            String uploadedPath = paths.get(paths.size() - 1);
            File f = new File(uploadedPath);
            boolean result = f.delete();
            if (result) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Deleted the uploaded zip file due to defaced result. - " + f.getAbsolutePath());
                }
            }
        }

        // Etc ImageSeries handling,
        // 6. deCompressed Files delete : decompressDir
        FileUtil.deletePath(decompressDir);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-> deleted decompressed files: " + decompressDir.getAbsolutePath());
        }
    }

    private boolean isThisNii(String path) {
        for (String keyword : this._niiIgnoreKeywords) {
            if (keyword == null || keyword.equals(""))
                continue;
            if (path.toUpperCase().contains(keyword.toUpperCase())) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Invalid NIfTi - filename=" + path + ", keyword=" + keyword);
                }
                return false;
            }
        }
        return true;
    }

    private String id(long projectSeq, int trialIndex, String orgId, String subjectId, String systemId) {
        return MongoKeyGenerator.generateTrialDataId(systemId, orgId, FIXED_PROJECT_SEQ, trialIndex, subjectId);
    }
}
