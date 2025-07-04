package com.ecoinsight.bdsp.asd.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.ecoinsight.bdsp.asd.entity.Board;
import com.ecoinsight.bdsp.asd.entity.BoardFile;
import com.ecoinsight.bdsp.asd.entity.Mail;
import com.ecoinsight.bdsp.asd.model.SentEmailEvent;
import com.ecoinsight.bdsp.asd.repository.IBoardRepository;
import com.ecoinsight.bdsp.core.crypto.IEncryptor;
import com.ecoinsight.bdsp.core.service.ServiceException;
import com.ecoinsight.bdsp.core.util.DateUtil;


@Service

public class BoardService  implements ApplicationEventPublisherAware {


    
    private  ApplicationEventPublisher eventPublisher;


    @Value("${ecoinsight.board.upload-dir}")
    private String FILE_UPLOAD_DIR;

    // @Value("${board.file-upload-max-size}")
    // private long FILE_UPLOAD_MAX_SIZE;

    @Autowired
    private IBoardRepository _Brepository;

   @Autowired
	@Qualifier("stringEncryptor")
    private IEncryptor _encryptor;

    @Autowired
    @Qualifier("IOrganizationRepository")
    private AsdOrganizationService asdOrganizationService;

    @Autowired
    private EmailService emailService;

    @Value("${ecoinsight.template.host}")
    private String host;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());




    @Transactional
    public void addBoardItem(final String boardCode,final String systemId, Board item, MultipartFile[] files) throws Exception {

        item.setDateUpdated(LocalDateTime.now());
        item.setDateCreated(LocalDateTime.now());
        // Board board = new Board();
        if (item.getBoardItemId() > 0) { // 수정

      
            try {
                this._Brepository.changeOneMboardItem(item);  
            } catch (Exception ex) {
                   LOGGER.error("Failed to update board item (boardItemId={}, UserId={})",item.getBoardItemId(),item.getUserId());
                
                throw ex;

            }
            if (files != null && files.length > 0) {
                final long itemId = item.getBoardItemId();
                try {
                    // 삭제한다 모든 파일 해당하는 불특정사용자의.

                    final Path fileDir = Path.of(FILE_UPLOAD_DIR, boardCode.toLowerCase());
                    if (!Files.exists(fileDir)) {
                        Files.createDirectories(fileDir);

                        LOGGER.info("File upload dir created. [PATH={}]", fileDir.toString());
                    }

                    for (MultipartFile file : files) {

                        String fileName = file.getOriginalFilename();

                        if (!StringUtils.hasText(fileName) || file.getSize() <= 0) {
                            continue;
                        }
                        // TODO Should support multi-file upload (only 1 file supported for now)
                        this._Brepository.deleteMboardFiles(itemId);

                        try {
                            Path path = Path.of(fileDir.toString(), fileName);
                            if (Files.exists(path)) {
                                // TODO rename if the same file name exists
                            }

                            // 파일 복사
                            file.transferTo(path);
                            LOGGER.debug("Copied file. [PATH={}]", path);

                            // 모바일용게시판파일첨부테이블에 파일을 저장한다

                            var boardFile = new BoardFile(itemId, fileName, path.toString());
                            boardFile.setDateCreated(LocalDateTime.now());
                            boardFile.setUserCreated(item.getUserCreated());
                            boardFile.setFileSize(file.getSize()); // 파일사이즈

                            this._Brepository.addOneBoardFile(boardFile);

                            LOGGER.debug("Inserted BoardFile. [PATH={}]", path);
                        } catch (Exception exection) {
                            LOGGER.error("Error while saving uploaded file. [Board={}/{}, File={}]", itemId,
                                    file.getOriginalFilename(), exection);
                        }

                    }

                } catch (Exception exection) {
                    throw exection;
                }

            }

        } else { // New item


            item.setNewBstate(true); // 사용자의 첫 게시판 등록시 관리자플랫폼에서   true 설정으로 인해 new icon state ture
            item.setNewMstate(false); // 사용자의 첫 게시판 등록시라서 사용자의 모바일 폼에서 해당 new icon 비활성 flase 로 셋팅
         

            try {
            
            //   boolean board = true;
             boolean board = this._Brepository.add(item);
              
              if(board){
            // LOGGER.info("New boardItem created. (boardItemId={}, UserId={})", item.getBoardItemId(),item.getUserId());
           
            Mail mail = new Mail();
            List<String> toMailList = new ArrayList<>(); // 여러 수신자 메일을 담는 배열 선언 부분임
           List<String> roleNameList = new ArrayList<>();
            try {         
                var list  =   this.asdOrganizationService.getMembersbyOrgId(systemId,item.getOrgId());
                
        

           
                list.forEach(result ->{
                    LOGGER.debug("email={}",_encryptor.decrypt(result.getEmail()));
                    toMailList.add(_encryptor.decrypt(result.getEmail()));
                    roleNameList.add(result.getRoleName());
                });


    



                mail.setTitle("[아이 AI] 플랫폼에 새로운 문의 글이 등록되었습니다.");
                mail.setToEmailArray(toMailList);    
                mail.setRoleNameArray(roleNameList);    
                // mail.setTemplateName("mail");   

                //템플릿에 전달할 데이터 설정
                HashMap<String, Object> emailValues = new HashMap<>();
                emailValues.put("emailTitle", "새로운 문의 게시글이 업데이트 되었습니다.");
                emailValues.put("boardTitle", item.getTitle());
                emailValues.put("orgName", item.getOrgName());     
                emailValues.put("userId",item.getUserId());
                emailValues.put("host", host);
                LocalDate localDate = item.getDateCreated().toLocalDate();
                emailValues.put("boardCreatedDate",DateUtil.formatSimpleDate(localDate));
                mail.setContent(emailValues);        

                eventPublisher.publishEvent(new SentEmailEvent(this, mail));
                // try {
                //     emailService.emailSend(mail);
                    
                // } catch (MailException  |  MessagingException ex) {
                //     LOGGER.error("Error send to email ", ex.getMessage());
                // }    

               

                } catch (ServiceException ex) {
                    LOGGER.error("Failed to find email to users to same of orgId.", ex);
			           
                }    
             
                }
              
             

            } catch (Exception ex) {
                   LOGGER.error("Failed to New upload board item (boardItemId={}, UserId={})",item.getUserId());
                
                throw ex;

            }
            

            // final long itemId = board.getBoardItemId();
                final long itemId = item.getBoardItemId();
            if (files != null && files.length > 0) {
                try {
                    // TODO Should delete and insert only changed files

                    final Path fileDir = Path.of(FILE_UPLOAD_DIR);
                    if (!Files.exists(fileDir)) {
                        Files.createDirectories(fileDir);

                        LOGGER.info("File upload dir created. [PATH={}]", fileDir.toString());
                    }

                    // final long itemId = this._Brepository.get

                    for (MultipartFile file : files) {
                        String fileName = file.getOriginalFilename();

                        if (!StringUtils.hasText(fileName) || file.getSize() <= 0) {
                            continue;
                        }

                        try {
                            Path path = Path.of(fileDir.toString(), fileName);
                            if (Files.exists(path)) {
                                // TODO rename if the same file name exists
                            }

                            // Copy file
                            file.transferTo(path);
                            LOGGER.debug("Copied file. [PATH={}]", path);

                            // Insert file into BoardFile
                            var boardFile = new BoardFile(itemId, fileName, path.toString());
                            boardFile.setDateCreated(LocalDateTime.now());
                            boardFile.setUserCreated(item.getUserCreated());
                            boardFile.setFileSize(file.getSize());
                            this._Brepository.addOneBoardFile(boardFile);

                            LOGGER.debug("Inserted BoardFile. [PATH={}]", path);
                        } catch (Exception exception) {
                            LOGGER.error("Error while saving uploaded file. [Board={}/{}, File={}]", itemId,
                                    file.getOriginalFilename(), exception);
                        }
                    }
                } catch (Exception exception) {
                    throw exception;
                }
            }

        }
 

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

}