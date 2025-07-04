package com.ecoinsight.bdsp.asd.web;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoinsight.bdsp.asd.Constants;
import com.ecoinsight.bdsp.asd.entity.Board;
import com.ecoinsight.bdsp.asd.entity.BoardComment;
import com.ecoinsight.bdsp.asd.repository.IBoardRepository;
import com.ecoinsight.bdsp.core.entity.Member;
import com.ecoinsight.bdsp.core.entity.ProjectMember;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.repository.IOrganizationRepository;
import com.ecoinsight.bdsp.core.repository.IProjectRepository;
import com.ecoinsight.bdsp.core.repository.IProjectTrialItemRepository;
import com.ecoinsight.bdsp.core.repository.mongo.IProjectSubjectRepository;
import com.ecoinsight.bdsp.core.web.BaseApiController;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

/**
 * 게시판 관리
 */
@RestController
@RequestMapping("/api/v1/board")
public class BoardController extends AsdBaseApiController {

    @Autowired
    private IBoardRepository _boardRepository;

    @Autowired
    private IProjectSubjectRepository _subjectRepository;

    @Autowired
    private IProjectRepository _projectRepository;

    @Autowired
    private IOrganizationRepository _organizationRepository;

    // @Autowired
    // private com.ecoinsight.bdsp.asd.repository.IAsdOrganizationRepository
    // _asdOrganizationRepository;

    @Autowired
    private IProjectTrialItemRepository _projectTrialItemRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(BoardController.class);

    private static final List<String> ORGS = new ArrayList<String>() {
        {
            add("10");
            add("11");
        }
    };

    /**
     * 문의게시판 데이터 조회.
     * with Paging.
     * 
     * @param params
     * @return
     */
    @GetMapping(path = "/search")
    public ResponseEntity<JsonResponseObject> searchBoardList(  @RequestParam(required = false) final Map<String, Object> params) {
        final String boardCode = "QNA";
        // final String orgId = super.getOrgId(); // orgid 10[서울대병원],11[세브란스병원]
        final String systemId = super.getSystemId(); // ASD
        final String userName = super.getAuthenticatedUsername(); // memberkey - T_member memberkey

        

        // if (!ORGS.contains(orgId)) {
        // return ErrorResponseEntity("해당 페이지에 접근 가능한 연구기관이 아닙니다.");
        // }

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
                if (offset <= 0 || offset >= ListDataModel.MAX_ROW_COUNT) {
                    offset = ListDataModel.DEFAULT_ROW_COUNT;
                }
            } catch (NumberFormatException pex) {
                offset = ListDataModel.DEFAULT_ROW_COUNT;
            }
        }

        String userId = "";
        String title = "";
        String all = "";   
        String orgId = null; // web 사용자 연구기관코드
        int accessLevel = 0b0111111;
        long projectId = 0;
        if (params != null && !params.isEmpty()) {
            for (String k : params.keySet()) {
                Object v = params.get(k);

                if (k.equals("id") && v != null) {

                    // filterDoc.append("projectSeq", (projectId = Long.parseLong(v.toString())));
                    projectId = Long.parseLong(v.toString());

                    String rolename = "";
                    // 과제에서 부여된 사용자 권한 조회
                    final Optional<ProjectMember> researcherOptional = this._projectRepository.findResearcher(projectId,
                            userName);
                    if (researcherOptional.isEmpty()) {
                        rolename = super.getHighestRole(userName).getRoleId();
                    } else {
                        rolename = researcherOptional.get().getRoleId();
                    }

                    accessLevel = super.calculateAccessLevel(rolename);

                }

                if (k.equals("userId") && v != null) {
                    userId = v.toString();
                }

                if (k.equals("title") && v != null) {
                    title = v.toString();
                }

                if (k.equals("all") && v != null) {
                    all = v.toString();
                }

            }
        }



        

        // Admin, Manager, QC User는 모든 연구기관의 데이터 조회 가능.
        if (!super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel) && !super.canQC(accessLevel) ) {
            final String org = super.getOrgId();
            //filterDoc.append("orgId", org);
             orgId = org;
        }


        List<Board> items = this._boardRepository.findAllBoard(boardCode,orgId,userId, title, all, page <= 1 ? 0 : (page - 1) * offset, offset);

        items.forEach(results -> {
            List<BoardComment> commentitems = this._boardRepository.findOnBoardComment(boardCode,
                    results.getBoardItemId());
            Board board = new Board();
            if (commentitems.isEmpty()) {
                board.setBoardItemId(results.getBoardItemId());
                board.setNewBstate(true);
                board.setNewMstate(false);
                this._boardRepository.updateNewBoardState(board);
            }
        });

        final long count = this._boardRepository.getTotalCount(boardCode,orgId, userId, title, all);
        items.forEach(result -> {

            result.addAreaNewStatus("NEWSTATE", result.isNewBstate() ? Constants.IMAGING_BOARD_NEW_UPLOAD_STATE
                    : Constants.IMAGING_BOARD_NEW_NO_STATE);

        });

        return OkResponseEntity("문의게시판를 조회했습니다.", new ListDataModel<Board>(items, count, page, offset));
    }

    @GetMapping(path = "/{itemId}") // first 진입
    public ResponseEntity<JsonResponseObject> getBoardItem(@PathVariable("itemId") final long itemId) {
        final String orgId = super.getOrgId(); // orgid 11[서울대병원],12[세브란스병원]
        // if (!ORGS.contains(orgId)) {
        //     return ErrorResponseEntity("해당 페이지에 접근 가능한 연구기관이 아닙니다.");
        // }

        final String boardCode = "QNA";

        var boardItem = this._boardRepository.findyOnOneboard(boardCode, itemId);
        return OkResponseEntity("문의 게시물을 조회했습니다.", boardItem);

    }

    @GetMapping(path = "/{itemId}/comments")
    public ResponseEntity<JsonResponseObject> getBoardComments(@PathVariable("itemId") final long itemId
    // ,@PathVariable("proejctId") final long proejctId
    ) {
        final String boardCode = "QNA";

        var comments = this._boardRepository.findBoardComments(boardCode, itemId);
        if (comments != null && comments.size() > 0) {
            // WriterId와 로그인 사용자 ID가 동일한지 체크
            // 동일하면 해당 답변을 수정/삭제 가능
            final String userName = super.getAuthenticatedUsername();
            this._memberRepository.findById(userName).ifPresent(user -> {
                comments.stream().forEach(item -> item.setCanChange(user.getMemberId().equals(item.getWriterId())));
            });
        }

        return OkResponseEntity("문의 게시물의 답변을 조회했습니다.", comments);
    }

    @PostMapping(path = "/{itemId}/comments")
    public ResponseEntity<JsonResponseObject> addBoardComment(
            @PathVariable(required = true, name = "itemId") long itemId,
            @RequestBody BoardComment item) {
        final String BOARD_CODE = "QNA";
        final String userName = super.getAuthenticatedUsername(); // memberkey - T_member memberkey
        final String orgId = super.getOrgId(); // orgid 11[서울대병원],12[세브란스병원]

        // if (!ORGS.contains(orgId)) {
        //     return ErrorResponseEntity("해당 페이지에 접근 가능한 연구기관이 아닙니다.");
        // }

        this._memberRepository.findById(userName).ifPresent(user -> {
            item.setWriterName(user.getMemberName());
            item.setWriterId(user.getMemberId());
            item.setUserId(user.getMemberId());
        });

        List<BoardComment> count = this._boardRepository.findBoardComments(BOARD_CODE, itemId);
        Board boards = new Board();
        boards.setBoardItemId(itemId);

        if (!count.isEmpty()) {
            BoardComment lastComment = count.get(count.size() - 1);

            System.out.println("getauthmemberKey==>" + userName); // 플랫폼의 사용자 memeberkey
            System.out.println("users[usercreated]==>" + lastComment.getUserCreated());
            if (lastComment.getUserCreated() != userName) {
                // pc관리자 측에서 값이 다르다면 user가 모바일사용자라는 뜻// 그러므로 관리자가 댓글을 등록하는 거기 땨문에 board에서
                // NewMsate true로 변경하고
                // NewBstate에서는 false로 값을 변경한다. 즉, 모바일 화면에서 new가 true로 icon이 떠야한다는 말임.

                boards.setNewBstate(false);
                boards.setNewMstate(true);

            } else if (lastComment.getUserCreated() == userName) {

                boards.setNewBstate(false);
                boards.setNewMstate(true);

            }
            this._boardRepository.updateNewBoardState(boards);
        } else { // emty일 경우에는 관리자가 처음 댓글을 달게 되므로 사용자 newMstate는 true /관리자는 newBstate는 false로
                 // 주어지게 된다.
            System.out.println("count.isemtpy==>" + count.isEmpty());
            boards.setNewBstate(false);
            boards.setNewMstate(true);
            this._boardRepository.updateNewBoardState(boards);
        }

        item.setBoardItemId(itemId);
        item.setUserCreated(userName);
        item.setDateCreated(LocalDateTime.now());
        item.setUserUpdated(userName);
        item.setDateUpdated(LocalDateTime.now());
        this._boardRepository.addBoardComment(item);

        return OkResponseEntity("문의 게시물의 새 답변을 등록했습니다.", item);
    }

    /**
     * @param projectId
     * @param boardItemId
     * @return
     */
    @PutMapping(path = "/{itemId}/comments")
    public ResponseEntity<JsonResponseObject> changeBoardComment(
            @PathVariable(required = true, name = "itemId") long itemId,
            @RequestBody BoardComment item) {
        final String BOARD_CODE = "QNA";
        final String userName = super.getAuthenticatedUsername(); // memberkey - T_member memberkey
        final String orgId = super.getOrgId(); // orgid 11[서울대병원],12[세브란스병원]

        // if (!ORGS.contains(orgId)) {
        //     return ErrorResponseEntity("해당 페이지에 접근 가능한 연구기관이 아닙니다.");
        // }

        item.setBoardItemId(itemId);
        item.setUserUpdated(userName);
        item.setDateUpdated(LocalDateTime.now());
        this._boardRepository.updateBoardComment(item);

        return OkResponseEntity("문의 게시물의 답변을 저장했습니다.", item);
    }

    @DeleteMapping(path = "/{itemId}/comments/{commentId}")
    public ResponseEntity<JsonResponseObject> removeBoardComment(
            @PathVariable(required = true, name = "itemId") long itemId,
            @PathVariable(required = true, name = "commentId") long commentId) {
        final String BOARD_CODE = "QNA";
        final String userName = super.getAuthenticatedUsername(); // memberkey - T_member memberkey
        final String orgId = super.getOrgId(); // orgid 11[서울대병원],12[세브란스병원]
        // if (!ORGS.contains(orgId)) {
        //     return ErrorResponseEntity("해당 페이지에 접근 가능한 연구기관이 아닙니다.");
        // }

        this._boardRepository.deleteChildBoardComments(commentId);
        return OkResponseEntity("문의 게시물의 답변을 삭제했습니다.", commentId);
    }

    @GetMapping("/{itemId}/files")
    public ResponseEntity<JsonResponseObject> getBoardFiles(@PathVariable("itemId") long itemId) {
        final String orgId = super.getOrgId(); // orgid 11[서울대병원],12[세브란스병원]
        // if (!ORGS.contains(orgId)) {
        //     return ErrorResponseEntity("해당 페이지에 접근 가능한 연구기관이 아닙니다.");
        // }

        final String BOARD_CODE = "QNA";

        var files = this._boardRepository.findAllFiles(BOARD_CODE, itemId);
        return OkResponseEntity("게시물의 첨부파일 목록을 조회했습니다.", files);
    }

    /* File download */
    @GetMapping("/{itemId}/files/{fileId}")
    public ResponseEntity<?> downloadBoardFile(
            @PathVariable(required = true, name = "itemId") long itemId,
            @PathVariable("fileId") final Long fileId) {
        final String orgId = super.getOrgId(); // orgid 11[서울대병원],12[세브란스병원]
        // if (!ORGS.contains(orgId)) {
        //     return ErrorResponseEntity("해당 페이지에 접근 가능한 연구기관이 아닙니다.");
        // }

        final String BOARD_CODE = "QNA";
        var file = this._boardRepository.findOneFile(BOARD_CODE, fileId);
        if (file == null || file.getFileId() <= 0) {
            return ErrorResponseEntity("파일이 존재하지 않습니다.");
        }

        final Path filePath = Path.of(file.getFilePath());
        if (!Files.exists(filePath)) {
            return ErrorResponseEntity("파일이 존재하지 않습니다.");
        }

        try {
            byte[] content = Files.readAllBytes(filePath);
            HttpHeaders header = new HttpHeaders();

            final String fileName = file.getFileName();
            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            header.add("Cache-Control", "no-cache, no-store, must-revalidate");
            header.add("Pragma", "no-cache");
            header.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(header)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.getFileSize())
                    .body(new InputStreamResource(new ByteArrayInputStream(content)));
        } catch (Exception ex) {
            LOGGER.error("Error while getting board-item-file. [FILE={}]", fileId);
        }

        return ErrorResponseEntity("파일을 다운로드 할 수 없습니다.");
    }

    // // 댓글이 처음달리면 연구관리자 쪽에서 그것이 원댓글
    // // 연구자의 답변이 없으면 사용자의 댓글등록 비활성화 시키기
    // // 댓글 달기, 댓글 등록할때 commentId보내기
    // return (errorMessages.size() > 0)
    // ? ErrorResponseEntity("댓글 등록 중 오류가 발생했습니다.",
    // errorMessages.toArray(String[]::new))
    // : OkResponseEntity("댓글을 정상적으로 등록 하였습니다.", comment);

    // }

    @GetMapping(path = "/search/{id}/{boardSeq}")
    public ResponseEntity<JsonResponseObject> getOneboard(
            @PathVariable("id") final long projectId,
            final @PathVariable(required = true, name = "boardSeq") long boardItemId) {

        final String boardCode = "QNA";
        final String systemId = super.getSystemId(); // ASD
        final String userName = super.getAuthenticatedUsername(); // memberkey - T_member memberkey
        final String orgId = super.getOrgId(); // orgid 11[서울대병원],12[세브란스병원]

        String rolename = "";
        int accessLevel = 0b0111111;

        final Optional<ProjectMember> researcherOptional = this._projectRepository.findResearcher(projectId, userName);
        if (researcherOptional.isEmpty()) {
            rolename = super.getHighestRole(userName).getRoleId();

        } else {
            rolename = researcherOptional.get().getRoleId();

        }

        accessLevel = super.calculateAccessLevel(rolename);

        Board boards = new Board();

        if (super.canQueryAll(accessLevel) || super.canQueryOrg(accessLevel)) { // 어드민,책임,참여연구자 조회가능

            var boardsOptional = this._boardRepository.findyOnOneboard(boardCode, boardItemId);
            boards = boardsOptional.get();
        } else if (accessLevel == 1 && !super.canQueryAll(accessLevel) && !super.canQueryOrg(accessLevel)
                && !super.canQC(accessLevel)) {
            return ErrorResponseEntity("일반사용자는 다른사용자의 게시물을 조회할  권한이 없습니다.");

        } else if (super.canQC(accessLevel)) {
            return ErrorResponseEntity("QC검수자는 다른사용자의 게시물을 조회할  권한이 없습니다.");

        }

        // if (boards.isEmpty()) {
        // return ErrorResponseEntity("게시물 정보가 존재하지 않습니다.");
        // }
        return OkResponseEntity("사용자 정보를 조회했습니다.", boards);
    }

    /**
     * @param projectId
     * @param boardItemId
     * @return
     */
    @PostMapping(path = "/{id}/comment/{boardSeq}/")
    public ResponseEntity<JsonResponseObject> addBoardComments(
            @PathVariable("id") final long projectId,
            @PathVariable(required = true, name = "boardSeq") long boardItemId,
            @ModelAttribute BoardComment item) {

        final String boardCode = "QNA";
        final String systemId = super.getSystemId(); // ASD

        final String userName = super.getAuthenticatedUsername(); // memberkey - T_member memberkey
        // final String orgId = super.getOrgId(); // orgid 11[서울대병원],12[세브란스병원]
        final String seoulOrgId = "11";
        final String severancelOrgId = "12"; // 소속기관 추가가될수도 있으므로 HardCoding으로 일단 로직구성

        final String orgId = super.getOrgId();
        final String memberid = super.getMemberId();

        final Optional<Member> reasearcherInfo = this._memberRepository.findById(userName);
        final List<String> errorMessages = new ArrayList<>();
        String rolename = "";
        String membername = "";
        // String memberid2 = "";
        int accessLevel = 0b0111111;

        final Optional<ProjectMember> researcherOptional = this._projectRepository.findResearcher(projectId, userName);
        int commentList = 0;
        BoardComment comment = new BoardComment();

        if (researcherOptional.isEmpty()) {
            rolename = super.getHighestRole(userName).getRoleId();

        } else {
            rolename = researcherOptional.get().getRoleId();

            final String memberid2 = reasearcherInfo.get().getMemberId();

            membername = reasearcherInfo.get().getMemberName();

            accessLevel = super.calculateAccessLevel(rolename);

            // if(super.canQueryAll(accessLevel) || super.canQueryOrg(accessLevel)){
            // 내일 orgid의 11,12 이면 저장insert mmebername,memeberid...등등, 아니면 노저장 [에러]
            // 셀렉트한 코멘트가 하나도 안달려있으면 처음 답변이 부모글
            // 모바일용에서는 셀렉틀해서 값이 하나도 없으면 모바일용 사용자
            // 댓글등록 비활성화시키기 ,답변이있다면 댓글 활성화

            // List<BoardComment> commentList = new ArrayList<BoardComment>();

            if (orgId.toString().equals(seoulOrgId.toString()) || orgId.toString().equals(severancelOrgId.toString())) {

                List<BoardComment> count = this._boardRepository.findBoardComments(boardCode, boardItemId);

                comment.setBoardItemId(boardItemId);
                comment.setWriterName(membername);
                comment.setContents(item.getContents());
                comment.setWriterId(memberid2);
                comment.setDateCreated(LocalDateTime.now());
                comment.setUserCreated(membername);

                if (count.size() <= 0 && item.getBoardCommentId() == 0) {
                    // "답변 및 댓글이 하나도 없으므로 새로운 답변 글 작성[물론 부모의 id값 0]"

                    try {
                        this._boardRepository.addBoardComment(comment);
                    } catch (Exception ex) {
                        LOGGER.error("Error while saving board-item", ex);
                    }
     
                } else if (count.size() > 0 && item.getBoardCommentId() >= 0) {
 

                    List<BoardComment> list = this._boardRepository.findByPCommentId(boardItemId,
                            item.getBoardCommentId());

                    if (list.size() <= 0) {
                        // "list==0이고 parentId에 0이상의 값이있다면 자식의 첫댓글 이후의 글로 저장이된다.[즉,첫번째 댓글]"

                        comment.setParentCommentId(item.getBoardCommentId());
                        try {
                            this._boardRepository.addBoardComment(comment);
                        } catch (Exception ex) {
                            LOGGER.error("Error reply failed", ex);
                        }

                    } else if (list.size() > 0) {
                        list.forEach(data -> {

                            if (item.getBoardCommentId() != data.getParentCommentId()
                                    && data.getParentCommentId() == 0) {

                                comment.setParentCommentId(item.getBoardCommentId());

                                // "부모id컬럼값과 boardSeq가 다르고 [partentCommentId가 0값] 자식의 첫번째 댓글로 인식 저장[진입]"
                                try {
                                    this._boardRepository.addBoardComment(comment);
                                } catch (Exception ex) {
                                    LOGGER.error("Error while saving board-item", ex);
                                }
                            } else if (item.getBoardCommentId() == data.getParentCommentId()
                                    && data.getParentCommentId() >= 0) {

                                // "부모id컬럼값과 boardSeq가 같고 자식의 댓글이 0이상인 걸로 인식 저장[진입]"
                                try {
                                    this._boardRepository.addBoardComment(comment);
                                } catch (Exception ex) {
                                    LOGGER.error("Error while saving board-item", ex);
                                }
                            }

                        });
                    }

                }

            } else {
                ErrorResponseEntity("댓글 등록할 수 있는 기관이 아닙니다.", orgId);
            }

        }

        // 댓글이 처음달리면 연구관리자 쪽에서 그것이 원댓글
        // 연구자의 답변이 없으면 사용자의 댓글등록 비활성화 시키기
        // 댓글 달기, 댓글 등록할때 commentId보내기
        return (errorMessages.size() > 0)
                ? ErrorResponseEntity("댓글 등록 중 오류가 발생했습니다.",
                        errorMessages.toArray(String[]::new))
                : OkResponseEntity("댓글을 정상적으로 등록 하였습니다.", comment);

    }
}
