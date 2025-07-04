package com.ecoinsight.bdsp.asd.web;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecoinsight.bdsp.asd.Constants;
import com.ecoinsight.bdsp.asd.entity.Board;
import com.ecoinsight.bdsp.asd.entity.BoardComment;
import com.ecoinsight.bdsp.asd.model.BoardModel;
import com.ecoinsight.bdsp.asd.repository.IBoardRepository;
import com.ecoinsight.bdsp.asd.service.AsdOrganizationService;
import com.ecoinsight.bdsp.asd.service.BoardService;
import com.ecoinsight.bdsp.asd.service.EmailService;
import com.ecoinsight.bdsp.core.model.ListDataModel;
import com.ecoinsight.bdsp.core.service.IOrganizationService;
import com.ecoinsight.bdsp.core.web.BaseApiController;
import com.ecoinsight.bdsp.core.web.JsonResponseObject;

@RestController
@RequestMapping(path = "/api/v1/mobileboard")
public class IMobileboardContorller extends BaseApiController {

    @Value("${ecoinsight.board.upload-dir}")
    private String FILE_DOWNLOAD_DIR;

    @Autowired
    private IBoardRepository _Brepository;

    @Autowired
    @Qualifier("boardService")
    private BoardService boardService;

    @Autowired
    private PasswordEncoder _passwordEncoder;

    // @Autowired
    // private EmailService emailService;

    @Autowired
    @Qualifier("IOrganizationRepository")
    private AsdOrganizationService asdOrganizationService;



    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    // 하드코딩으로 haeder값 board 특정값 지정 쿼리로

//  @RequestParam(name = "file", required = false) MultipartFile[] mfiles,
//             @RequestParam(name = "boardId") String boardId,
//             @RequestParam(name = "contents") String contents,
//             @RequestParam(name = "password") String password,
//             @RequestParam(name = "title") String title,
//             @RequestParam(name = "orgName") String orgName




    @PostMapping("/upload")
    public ResponseEntity<JsonResponseObject> saveMboard(
        @RequestParam(name = "file", required = false) MultipartFile[] mfiles,
        @ModelAttribute Board item) {
            
         final String systemId = super.getSystemId();       
        final String CODE = "QNA";
        Board boardUp = new Board();

        if (StringUtils.hasText(item.getPassword())) {

            // final String newPassword = this._passwordEncoder.encode(password);
            final String newPassword = this._passwordEncoder.encode(item.getPassword());
            boardUp.setPassword(newPassword);
        }


        boardUp.setBoardCode(CODE);
        boardUp.setUserId(item.getUserId());
        boardUp.setWriterName(item.getWriterName());
        boardUp.setTitle(item.getTitle());
        boardUp.setContents(item.getContents());
        boardUp.setUserCreated(item.getWriterName());
        boardUp.setOrgId(item.getOrgId());
        boardUp.setOrgName(item.getOrgName());
        boardUp.setDateCreated(LocalDateTime.now());
        boardUp.setUserUpdated(item.getWriterName());
        boardUp.setDateUpdated(LocalDateTime.now());
       
        



        //모바일 등록
        final List<String> errorMessages = new ArrayList<>();
        try {
          this.boardService.addBoardItem(CODE,systemId, boardUp,mfiles);
            // this.boardService.addBoardItem(CODE, boardUp, mfiles); 20230620
            // this.boardService.addBoardItem(boardUp, mfiles);        
            // this.emailService.emailSend(null);
       
       
        } catch (Exception ex) {
            LOGGER.error("Error while saving board-item", ex);
        }

        return (errorMessages.size() > 0)
                ? ErrorResponseEntity("문의게시판 저장 중 오류가 발생했습니다.",
                        errorMessages.toArray(String[]::new))
                : OkResponseEntity("문의 게시판을 정상적으로 저장했습니다.", boardUp);
    }

    /**
     * 문의게시판 데이터 조회.
     * with Paging.
     * 
     * @param params
     * @return
     */
    @GetMapping(path = "/search")
    public ResponseEntity<JsonResponseObject> searchBoardList(
            @RequestParam(required = false) Map<String, Object> params) {

        final String boardCode = "QNA";

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
        if (params.containsKey("userId")) {
            userId = params.get("userId").toString();
        }

        String title = "";
        if (params.containsKey("title")) {
            title = params.get("title").toString();
        }
        String all = "";
        if (params.containsKey("all")) {

            all = params.get("all").toString();
        }
        String orgId = null;
        List<Board> items = this._Brepository.findAllBoard(boardCode,orgId,userId, title, all, page <= 1 ? 0 : (page - 1) * offset,
                offset);


        items.forEach(results -> {
            List<BoardComment> commentitems = this._Brepository.findOnBoardComment(boardCode, results.getBoardItemId());
            Board board = new Board();
            if(commentitems.isEmpty()){
                board.setBoardItemId(results.getBoardItemId());
                board.setNewBstate(true);
                board.setNewMstate(false);
                this._Brepository.updateNewBoardState(board);
            }
        });
   



        final long count = this._Brepository.getTotalCount(boardCode,null,userId, title, all); //불툭정 다수이기때문에 orgId null 처리
        items.forEach(result ->{
            result.addAreaNewStatus("NEWSTATE",result.isNewMstate() ? Constants.IMAGING_BOARD_NEW_UPLOAD_STATE : Constants.IMAGING_BOARD_NEW_NO_STATE );

        });;

        return OkResponseEntity("문의게시판를 조회했습니다.", new ListDataModel<Board>(items, count, page, offset));

        /*
         * 
         * 
         * 게시물 조회 -- select 날리면 board 권한.......
         * select 조회
         * 
         */

    }

    @GetMapping(path = "/search/{id}")
    public ResponseEntity<JsonResponseObject> getOneboard(
            final @PathVariable(required = true, name = "id") long boardItem) {

        final String boardCode = "QNA";
        var item = this._Brepository.findyOnOneboard(boardCode, boardItem);



        
        // var fileItem = this._Brepository.findByboardSeqFile(itemId);

        // if (item.isEmpty()) {
        // return ErrorResponseEntity("게시물 정보가 존재하지 않습니다.");
        // }
        return OkResponseEntity("사용자 정보를 조회했습니다.", item);

    }

    @PostMapping(path = "/search/verify/password/")
    public ResponseEntity<JsonResponseObject> findBypassword(
            final BoardModel model) {

        final String boardCode = "QNA";
        var boardOptional = this._Brepository.findByboardItemId(boardCode, model.getBoardItemId());

        if (boardOptional.isEmpty()) {
            return ErrorResponseEntity("게시판 사용자 정보가 존재하지 않습니다.");
        }

        Board board = boardOptional.get();

        if (this._passwordEncoder.matches(model.getPassword(), board.getPassword())) {
            return OkResponseEntity("사용자의 비밀번호 검증이 성공했습니다.", board);
        } else {
            return ErrorResponseEntity("입력한 비밀번호가 일치하지 않습니다.");
        }

    }

    @GetMapping(path = "/search/{id}/files")
    public ResponseEntity<?> getBoardFiles(@PathVariable("id") long boardItem) {
        var item = this._Brepository.findByboardSeqFile(boardItem);

        return OkResponseEntity("파일 정보를 조회했습니다.", item);
    }

    /* File download */
    /**
     * 
     * @param boardCode
     * @param fileId
     * @return
     */
    @GetMapping(path = "/{itemId}/files/{fileId}")
    public ResponseEntity<?> downloadBoardFile(
            @PathVariable(required = true, name = "itemId") long itemId,
            @PathVariable("fileId") final Long fileId) {
        System.out.println("DOWNLOADDIR:" + FILE_DOWNLOAD_DIR);
        final String BOARD_CODE = "QNA";
        var file = this._Brepository.findByfileSeqFile(BOARD_CODE.toUpperCase(), fileId);
        if (file == null || file.getFileId() <= 0) {
            return ResponseEntity.notFound().build();
        }

        final Path filePath = Path.of(file.getFilePath());

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] content = Files.readAllBytes(filePath);
            HttpHeaders header = new HttpHeaders();

            // String fileName = (String)
            // JsonUtil.toHashMapObject(JsonUtil.toJson(result.getResultData())).get(CommCode.ATCH_FILENM);
            // byte[] file =
            // Base64.getDecoder().decode(JsonUtil.toHashMapObject(JsonUtil.toJson(result.getResultData())).get(CommCode.ATCH_FILE).toString());

            final String fileNames = file.getFileName();

            header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileNames);
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

        return ResponseEntity.badRequest().build();
    }

    /**
     * 
     * @param boardItemId
     * @return
     */
    @GetMapping("/comment/upload/status/{boardItemId}")
    public ResponseEntity<JsonResponseObject> searchCommentStatus(
            @PathVariable(name = "boardItemId") long boardItemId) {

        final String boardCode = "QNA";

        var checkingCommentList = this._Brepository.findOnBoardComment(boardCode, boardItemId);

        // 연구관리자의 답변 이후의 list 사이즈가 0 이상이면
        // 아래의 mapper 쿼리를 사용하여 부모의 boardCommentId,writerId 등등을 조회한다
        // => 댓글등록을 위한 값

        BoardComment comment = new BoardComment();
        checkingCommentList.forEach(v -> {

            long parentCId = v.getParentCommentId();

            if (checkingCommentList.size() > 0 && parentCId == 0) {

                var count = this._Brepository.findByPCommentId(boardItemId, parentCId);
                count.forEach(k -> {

                    comment.setBoardItemId(k.getBoardItemId());
                    comment.setBoardCommentId(k.getBoardCommentId());
                    comment.setParentCommentId(k.getParentCommentId());
                    comment.setWriterId(k.getWriterId());

                });
            }

        });

        return OkResponseEntity("사용자 정보를 조회했습니다.", comment);

    }

    /**
     * 
     * @param boardItemId
     * @return
     */
    @GetMapping("/comment/search/{id}")
    public ResponseEntity<JsonResponseObject> searchAllComment(
            @PathVariable(required = true, name = "id") long boardItemId) {

        final String boardCode = "QNA";
        
        List<BoardComment> list = this._Brepository.findOnBoardComment(boardCode, boardItemId);






        BoardComment comment = new BoardComment();

        // items.forEach(k -> {

        // comment.setBoardCommentId(k.getBoardCommentId());
        // comment.setBoardItemId(k);

        // });

        return OkResponseEntity("사용자 정보를 조회했습니다.", list);

    }

    /**
     * @param projectId
     * @param boardItemId
     * @return
     */
    @PostMapping(path = "/comment/upload/{boardItemId}/{PCommentId}")
    public ResponseEntity<JsonResponseObject> addBoardComment(
            @PathVariable(required = true, name = "boardItemId") long boardItemId,
            @PathVariable(required = true, name = "PCommentId") long PCommentId,
            @RequestBody BoardComment item) {

        final List<String> errorMessages = new ArrayList<>();
        BoardComment comment = new BoardComment();

        final String boardCode = "QNA";

        List<BoardComment> count = this._Brepository.findOnBoardComment(boardCode, boardItemId);

      var board =  this._Brepository.findyOnOneboard(boardCode, boardItemId);
        final boolean orignMstate = board.get().isNewMstate();
        final boolean orignBstate = board.get().isNewBstate();        
             
         

        if (!count.isEmpty()) {

            List<BoardComment> list = this._Brepository.findByPCommentId(boardItemId, PCommentId);

            if (list.size() < 0) {
                // "list==0이고 parentId에 0이상의 값이있다면 자식의 첫댓글 이후의 글로 저장이된다.[즉,첫번째 댓글]"

                // comment.setParentCommentId(item.getBoardCommentId());

                return ErrorResponseEntity("관리자 답변글 정보가 존재하지 않습니다.");

                // try {
                // this._Brepository.addBoardComment(comment);
                // } catch (Exception ex) {
                // LOGGER.error("Error reply failed", ex);
                // }

            } else if (list.size() >= 0) {

                // 이미 status에서 댓글달기를 누른 부모댓글의 컬럼값을 걸러 가져왔다.

                if (item.getBoardCommentId() < 0 && item.getParentCommentId() < 0) {
                    // 값이없다면
                    return ErrorResponseEntity("해당 댓글을 달 답변 리스트가 존재하지 않습니다.");

                } else {
                    




        

                    comment.setBoardItemId(item.getBoardItemId()); // 해당 게시글 시퀀스아이디
                    comment.setWriterId(item.getWriterId()); // 사용자의 writerId[userId] 기입
                    comment.setUserId(item.getUserId()); // 사용자의 UserId 기입
                    comment.setWriterName(item.getWriterName()); // 사용자 이름
                    comment.setContents(item.getContents()); // 작성한 댓글 내용
                    comment.setParentCommentId(item.getBoardCommentId()); // 댓글에 저장할 부모댓글시퀀스아이값 넣기
                    comment.setDateCreated(LocalDateTime.now());
                    comment.setUserCreated(item.getUserId());
                    comment.setDateUpdated(LocalDateTime.now());
                    comment.setUserUpdated(item.getUserId());

                }


                try {

                    Board boards = new Board();
                 boards.setBoardItemId(boardItemId);  

            BoardComment lastComment = count.get(count.size() - 1);


            if(lastComment.getWriterId() != item.getWriterId()){ 
                    //writerName이 사용자이고 , userCreated가 관리자 id계정이라면 
                    // 해당 계정자가 다르다면 모바일에는 newMstate가 false고 pc의 newBstate true로 설정한다.
           
                    boards.setNewBstate(true);
                    boards.setNewMstate(false);  
                 
                
                }else if(lastComment.getWriterId() == item.getWriterId()){
      
                    boards.setNewBstate(true);
                    boards.setNewMstate(false);
                  
                }
                this._Brepository.updateNewBoardState(boards);

                    long num = this._Brepository.addBoardComment(comment);
                    if (num < 0) {

                        return ErrorResponseEntity("댓글 등록 중 오류가 발생했습니다.", String.valueOf(num));
                    }

                } catch (Exception ex) {
                    LOGGER.error("Error while saving board-item", ex);
                }

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
