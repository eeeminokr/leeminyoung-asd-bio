<template>
  <div id="ContentsDiv">
    <div class="cont_body mt_34">
      <h3 class="tit_bul">문의 게시판</h3>
    </div>
    <div class="board_view">
      <table class="tb_head tb_body">
        <colgroup>
          <col style="width:100px;">
          <col>
        </colgroup>
        <tbody>
          <tr>
            <th>대상자 ID (등록일)</th>
            <td>{{ boardItem.userId }} <span>({{ BoardformatDate(boardItem.dateCreated) }})</span></td>
          </tr>
          <tr>
            <th>제 &nbsp; 목</th>
            <td class="view_title">{{ boardItem.title }}</td>
          </tr>
          <tr>
            <th>내 &nbsp; 용</th>
            <td class="view_content">
              <div v-html="formatContent(boardItem.contents)"></div>
            </td>
          </tr>
          <tr class="view_attach">
            <th class="tit">첨부파일</th>
            <td>
              <span  v-if="canDownloadImaging" class="file" v-for="file in files" :key="file.fileId">
                <a @click="downloadFile(file.fileId)"> {{ file.fileName }} ({{ file.fileSize / 1000 }} kb)</a>
              </span>
              <span class="file" v-if="files.length <= 0"><a href="">"파일첨부
                  해당없음"</a></span>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="view_replay">
        <span class="tit">답변사항 리스트</span>
        <div class="replay_list">
          <div v-for="comment in comments" :key="comment.boardCommentId">
            <!-- :style="{'padding-left': (comment.level - 1) * 30 + 'px'}"-->
            <ul v-if="comment.writerId != boardWriterId && comment.parentCommentId == 0">
              <li>
                <textarea id="textContents" v-model="comment.contents"
                  style="height:50px; width:80%; position: relative; display: inline-block"
                  :class="getEditStyle(comment.boardCommentId)"></textarea>
                <span style="color: #0094FA; display: inline-block; position: relative; bottom: 10px;"> 관리자 : </span>
                <span class:="getReadStyle(comment.boardCommentId)"
                  style="position: relative; display: inline-block;  bottom: 10px;"
                  v-html="formatContent(comment.contents)"></span>
                <span class="date" style="position: relative; display: inline-block; bottom: 10px;">{{
                  formatDate(comment.dateUpdated) }}</span>
                <div class="btn_wrap">
                  <button v-if="canCreateImaging" type="button" class="btn" :class="getReadStyle(comment.boardCommentId)"
                    @click="setEditMode(comment.boardCommentId, 'edit')">수정</button>
                  <button v-if="canCreateImaging" type="button" class="btn" :class="getEditStyle(comment.boardCommentId)"
                    @click="saveComment(comment.boardCommentId)">저장</button>
                  <button type="button" class="btn" :class="getEditStyle(comment.boardCommentId)"
                    @click="cancelSaveComment(comment.boardCommentId)">취소</button>
                  <button v-if="canDeleteImaging" type="button" class="btn" :class="getReadStyle(comment.boardCommentId)"
                    @click="removeComment(comment.boardCommentId)">삭제</button>
                </div>
              </li>

            </ul>
            <ul class="childreplyAdmin" v-if="comment.writerName != boardWriterName && comment.parentCommentId > 0">
              <li>
                <span style="color: #0094FA; display: inline-block; position: relative; bottom: 10px;"> 관리자 : </span>
                <span style="position: relative; display: inline-block;  bottom: 10px;"
                  class:="getReadStyle(comment.boardCommentId)" v-html="formatContent(comment.contents)"></span>
                <span class="date" style="position: relative; display: inline-block; bottom: 10px;">{{
                  formatDate(comment.dateUpdated) }}</span>
                <div class="btn_wrap">
                  <button v-if="canCreateImaging" type="button" class="btn" :class="getReadStyle(comment.boardCommentId)"
                    @click="setEditMode(comment.boardCommentId, 'edit')">수정</button>
                  <button v-if="canCreateImaging" type="button" class="btn" :class="getEditStyle(comment.boardCommentId)"
                    @click="saveComment(comment.boardCommentId)">저장</button>
                  <button type="button" class="btn" :class="getEditStyle(comment.boardCommentId)"
                    @click="cancelSaveComment(comment.boardCommentId)">취소</button>
                  <button v-if="canDeleteImaging" type="button" class="btn" :class="getReadStyle(comment.boardCommentId)"
                    @click="removeComment(comment.boardCommentId)">삭제</button>
                </div>
              </li>
            </ul>
            <ul class="childreplyUser" v-if="comment.writerName == boardWriterName && comment.parentCommentId > 0"><span
                style="color: #41B883;position: relative; display: inline-block; bottom: 10px;">{{ comment.userId }}
                사용자 : </span>
              <span style="position: relative; display: inline-block; bottom: 10px;"
                class:="getReadStyle(comment.boardCommentId)" v-html="formatContent(comment.contents)"></span>
              <span class="date" style="position: relative; display: inline-block;"> {{ formatDate(comment.dateUpdated)
              }}</span>
              <span class="btn_wrap">
                <button v-if="canCreateImaging" type="button" class="btn" :class="getEditStyle(comment.boardCommentId)"
                  @click="saveComment(comment.boardCommentId)">저장</button>
                <button v-if="canCreateImaging" type="button" class="btn" :class="getEditStyle(comment.boardCommentId)"
                  @click="cancelSaveComment(comment.boardCommentId)">취소</button>
                <button v-if="canDeleteImaging" type="button" class="btn" :class="getReadStyle(comment.boardCommentId)"
                  @click="removeComment(comment.boardCommentId)">삭제</button>
              </span>
            </ul>
            <!--
                  <div class="reply-title" >
                    <textarea id="textContents" v-model="comment.contents" style="height:50px; width:80%;" :class="getEditStyle(comment.boardCommentId)"></textarea>
                    <div style="width:80%;" :class="getReadStyle(comment.boardCommentId)" v-html="formatContent(comment.contents)"></div>
                    <div class="btn_wrap">
                      <span class="date" style="margin-right:20px;">{{ comment.writerName }} ({{ formatDate(comment.dateUpdated) }})</span>
                        <button type="button" class="btn ">댓글달기</button> 
                        <button type="button" class="btn" :class="getReadStyle(comment.boardCommentId)" @click="setEditMode(comment.boardCommentId, 'edit')">수정</button>
                        <button type="button" class="btn" :class="getEditStyle(comment.boardCommentId)" @click="saveComment(comment.boardCommentId)">저장</button>
                        <button type="button" class="btn" :class="getEditStyle(comment.boardCommentId)" @click="cancelSaveComment(comment.boardCommentId)">취소</button>
                        <button type="button" class="btn" :class="getReadStyle(comment.boardCommentId)"  @click="removeComment(comment.boardCommentId)">삭제</button>
                        <button type="button" class="btn btn_blue2" @click="setReplyMode(comment.boardCommentId)">답변등록</button>
                    </div>
                
                    <div class="replay_input" style="display:none" :class="{'show': replyCommentId == comment.boardCommentId}">
                      <textarea style="width:80%;height:50px;" v-model="newCommentReply"></textarea>
                      <div class="btn_wrap">
                          <button type="button" class="btn btn_blue2" @click="addNewComment(comment.boardCommentId)">등록</button>
                          <button type="button" class="btn btn_blue2" @click="cancelNewComment(comment.boardCommentId)">취소</button>
                      </div>
                    </div>
                    
                  </div>
                    -->
          </div>
          <div class="btn_wrap">
            <button v-if="canCreateImaging" type="button" class="btn"  @click="setReplyMode(parentCommentId)"
              style="margin: 10px 10px 10px 10px;">댓글달기</button>
          </div>
        </div>
        <div class="replay_input" v-if="canUploadComment == true && canCreateImaging">
          <!-- <textarea style="width:80%;height:50px; display:none" :class="{'show': replyCommentId == undefined}"  v-model="newCommentContent"></textarea> -->
          <textarea style="width:80%;height:50px;" v-model="newCommentReply"></textarea>
          <div class="btn_wrap">

            <button v-if="canCreateImaging" type="button" class="btn btn_blue2" style="margin-top: 10px;"
              @click="addNewComment(parentCommentId)">답글등록</button>
            <button type="button" class="btn btn_blue2" style="margin-top: 10px;"
              @click="cancelNewComment(parentCommentId)">취소</button>
          </div>
        </div>
      </div>
    </div>
    <div class="buttons" style="text-align:center">
      <button class="btn btn_violet" type="button" @click="goToList()">목록</button>
    </div>
  </div>
</template>
  
<script>

import BoardService from '../../services/board.service';
import AlertService from '../../services/alert.service';
import { jqueryUI } from '../../services/jquery-ui';
import { constants, utils, FUNCTION_NAMES } from '../../services/constants';
import ProjectService from '../../services/project.service';
import MemberService from "../../services/member.service";


  const FUNCTION_NAME = FUNCTION_NAMES.BOARD_QNA;

export default {
  name: 'BoardDetail',
  props: {
    boardItemId: Number | String
  },
  data() {
    return {
      boardItem: {},
      comments: [],
      files: [],
      editingComment: {},
      replyCommentId: undefined,
      oldCommentContent: undefined,
      newCommentContent: undefined,
      newCommentReply: undefined,
      fileId: undefined,
      fileName: undefined,
      boardWriterName: undefined,
      boardWriterId: undefined,
      theCommentPermission: undefined,
      parentCommentId: undefined,
      parentBoardCommentId: undefined,
      filters: {
                userId: undefined,
                title: undefined,
                all : undefined,
                projectId: "",
            },
      projectId: undefined,      
      thePermissionImaging: {
					queryGranted: false,
					createGranted: false,
					deleteGranted: false,
					downloadGranted: false
				},
    }
  },
  computed: {
    canUploadComment() {
      return this.theCommentPermission;
    },
/**
			 * 조회 권한이 있는가?
			 */
       canQuery() { return true; },
               canQueryImaging() { return this.thePermissionImaging.queryGranted },
			/**
			 * 등록 권한이 있는가?
			 */
			canCreateImaging() { return this.thePermissionImaging.createGranted; },
			/**
			 * 삭제 권한이 있는가?
			 */
			canDeleteImaging() { return this.thePermissionImaging.deleteGranted; },
			/**
			 * 다운로드 권한이 있는가?
			 */
			canDownloadImaging() { return this.thePermissionImaging.downloadGranted; },


  },
  watch: {

  },
  methods: {
    goToList() {
      this.$router.push("/board");
    },
    setReplyMode(commentId) {
      this.replyCommentId = commentId;

      this.theCommentPermission = true;
    },
    setEditMode(commentId, mode) {
      this.editingComment[commentId] = mode;
      this.oldCommentContent = this.comments.find(x => x.boardCommentId == commentId).contents;
    },
    getEditStyle(commentId) {
      return this.editingComment[commentId] == 'edit' ? 'show' : 'hide';
    },
    getReadStyle(commentId) {
      return !!this.editingComment[commentId] ? 'hide' : 'show';
    },
    getFiles(itemId) {
      BoardService.getBoardFiles(itemId).then(response => {
        if (response.data.succeeded) {
          this.files = response.data.data;

          response.data.data.forEach((n, v) => {

            this.fileName = n.fileName;
            this.fileId = n.fileId;



          })

        }
      }, error => console.error(error));
    },
    downloadFile(fileId) {
      if (!this.fileId || isNaN(this.fileId)) return;
      BoardService.downloadBoardFile(this.boardItemId, fileId).then(response => {


        const url = window.URL.createObjectURL(new Blob([response.data]));
        var el = document.createElement('a');
        el.href = url;
        el.setAttribute('download', this.fileName);
        document.body.appendChild(el);
        el.click();
        el.remove();

      },
        error => {
          console.error(error);
        });
    },
    cancelSaveComment(commentId) {
      this.comments.find(x => x.boardCommentId == commentId).contents = this.oldCommentContent;
      this.editingComment = {};
      this.oldCommentContent = undefined;
      this.replyCommentId = undefined;
      this.newCommentReply = undefined;
    },
    saveComment(commentId) {
      var comment = this.comments.find(x => x.boardCommentId == commentId);
      if (!comment || comment.contents.trim().length <= 0) return;

      BoardService.changeBoardComment(this.boardItemId, comment).then(response => {
        if (response.data.succeeded) {

        };
      }, error => console.error(error));

      this.editingComment = {};
      this.oldCommentContent = undefined;
      this.replyCommentId = undefined;
      this.newCommentReply = undefined;
    },
    removeComment(commentId) {
      if (!confirm('선택한 댓글을 삭제 하겠습니까?')) return;

      BoardService.removeBoardComment(this.boardItemId, commentId).then(response => {
        if (response.data.succeeded) {
          this.getComments(this.boardItemId);
        };
      }, error => console.error(error));

      this.editingComment = {};
      this.oldCommentContent = undefined;
      this.replyCommentId = undefined;
      this.newCommentReply = undefined;
    },
    cancelNewComment(commentPId) {
      this.replyCommentId = undefined;
      this.newCommentContent = undefined;
      this.newCommentReply = undefined;
      this.theCommentPermission = false;
    },
    addNewComment(commentPId) {
      // const content = !!commentPId ? this.newCommentReply : this.newCommentContent;

      const content = this.newCommentReply;

      if (!content || content.trim().length <= 0) {
        AlertService.info("댓글내용을 기입해주세요")
        return;
      
      }
      const comment = {
        boardItemId: this.boardItemId,
        parentCommentId: commentPId,
        contents: content
      };



      BoardService.addNewBoardComment(this.boardItemId, comment).then(response => {
        if (response.data.succeeded) {
          this.newCommentContent = undefined;
          this.theCommentPermission = false;
          this.getComments(this.boardItemId);
        };
      }, error => console.error(error));

      this.editingComment = {};
      this.oldCommentContent = undefined;
      this.replyCommentId = undefined;
      this.newCommentReply = undefined;
    },
    getBoardItem(itemId) {
      this.boardItem = {};
      this.comments = [];
      this.files = [];
      this.editingComment = {};
      this.oldCommentContent = undefined;
      this.replyCommentId = undefined;
      this.newCommentReply = undefined;

      this.projectId = this.filters.projectId;
      


      MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAME).then(result => {
                if (result.data.succeeded) {
                    this.thePermissionImaging = result.data.data;
                    const canQuery = result.data.data.queryGranted;
                  
            
                    if (!canQuery) {
                        alert('데이터를 조회할 사용권한이 없습니다.');
                        return;
                    }



      BoardService.getBoardItem(itemId).then(response => {
        if (response.data.succeeded) {
          this.boardItem = response.data.data;
          
          // this.boardWriterId = response.data.data.writerName; // board 작성자id

          
            this.boardWriterId = response.data.data.userId; // 게시판 작성장 userId[writerId 댓글 작성자 writerId와 비교를 위한 것.]

          this.boardWriterName = response.data.data.writerName; // board 작성자이름
          this.getComments(this.boardItemId);
          this.getFiles(this.boardItemId);
        }
        else {
          //alert (response.data.message);
          this.$router.push('/errors/no-permission');
        }
      }, error => {
                console.error(error);
                })
                .finally(() => {
                jqueryUI.perfectScrollbar.create();
                });
              }
            },
                error => {
                    console.error(error);
                });

    },
    uploadPossibleStatus() {


      this.theCommentPermission = true;


    },
    getComments(itemId) {
      BoardService.getBoardComments(itemId).then(response => {
        if (response.data.succeeded) {
          this.comments = response.data.data;



          this.comments.forEach(n => {




            if (this.comments.length > 0 && n.parentCommentId == 0) {

              //   this.parentBoardCommentId = n.boardCommentId;


              this.parentCommentId = n.boardCommentId;



              this.parentBoardCommentId = n.boardCommentId;

            } else if (this.comments.length > 0 && n.parentCommentId == 0) {

              this.parentCommentId = n.parentCommentId


            }
            else if (n.parentCommentId > 0) {
              this.parentCommentId = n.parentCommentId


            }

          })

        }
      }, error => console.error(error));
    },
    formatContent(content) {
      return !content ? "" : content.replace(/\n/g, '<br/>');
    },
    formatDate(date) {

      return utils.formatSimpleDateKorean(date)
    },
    BoardformatDate(date) {

      return utils.formatSimpleDate(date)
    }
  },
  mounted() {
    const projectid = utils.getProjectId();
            if (!!projectid) {
                this.filters.projectId = projectid;
            }

            ProjectService.getProjects().then(result=>{
                if (result.data.succeeded) {
                    this.projects = result.data.data;
                    if (!!!this.filters.projectId) {
                        this.filters.projectId = this.projects.length > 0 ? this.projects[0].projectSeq : '';
                    }
                }

                if (!!this.boardItemId) {
          this.getBoardItem(this.boardItemId);
            }
            }, 
            error=>{ console.error(error); });
  },
  create() {
  


  }
}
</script>
  
<style scoped>
.tb_body td {
  text-align: left;
}

/* 
.view_replay {
    position: relative;
    
    padding: 25px 20px 0;
}
.view_replay .tit {    
    position: relative;
    display: block;
    margin: 0 0 22px;
    padding-left: 15px;
    line-height: 1.4rem;
    font-size: 0.8rem;
    font-weight: 700;
    color: #333;
}
.view_replay .tit:before {
    position: absolute;
    top: 6px;
    left: 0;
    content: "";
    display: block;
    width: 7px;
    height: 7px;
    background-color: #2165c1;
    border-radius: 50%;
}
.view_replay .replay_list {
    overflow-y: auto;
    margin-bottom: 20px;
    padding: 0 20px 0 0;
    width: calc(100% + 18px);
    box-sizing: border-box;
}
.view_replay .replay_list div {
  font-size: 12px;
  position: relative;
  padding-left: 12px;
  line-height: 1.9rem;
  margin: 0px;
}
.view_replay .replay_list div.reply-title:before {
    position: absolute;
    top: 10px;
    left: 0;
    content: "";
    display: block;
    width: 5px;
    height: 5px;
    background-color: #cbcdd1;
    border-radius: 50%;
}
.view_replay .replay_list ul li {
  font-size: 12px;
  position: relative;
  padding-left: 12px;
  line-height: 1.9rem;
  margin: 0px 20px 20px 20px;
}
.view_replay .replay_list ul li:before {
    position: absolute;
    top: 10px;
    left: 0;
    content: "";
    display: block;
    width: 5px;
    height: 5px;
    background-color: #cbcdd1;
    border-radius: 50%;
}
.date {
    float:left;
    display: block;
    line-height: 2rem;
    color: #797979;
} */


.childreplyUser {

  margin-left: 12px;

}

.childreplyAdmin {

  /* margin-left: 25px; */
  font-size: 1.2rem;
}

/* 질문목록관리자 : 2023-02-06 */
.w_88 {
  width: 88px;
}

.mt_34 {
  margin-top: 34px;
}

.fc_blue {
  color: #1e65c5;
}

.btn.btn_green {
  padding: 0 10px;
  min-width: 78px;
  height: 25px;
  line-height: 26px;
  background: #719f4a;
}

.btn.btn_gwhite {
  padding: 0 10px;
  min-width: 78px;
  height: 25px;
  line-height: 24px;
  background: #fff;
  border: 1px solid #86b162;
  color: #629d30;
}

.btn.btn_bwhite2 {
  padding: 0 10px;
  background: #fff;
  border: 1px solid #4e56b0;
  color: #333;
  height: 27px;
  line-height: 26px;
}

/*.ico_lock {margin:-3px 0 0 15px;display:inline-block;width:10px;height:12px;background:url(../images/ico_lock.png) no-repeat 0 0;vertical-align:middle;}*/
.tb_page {
  margin-bottom: 7px;
  padding-top: 4px;
}

.tb_subject {
  overflow: hidden;
  display: inline-block;
  max-width: calc(100% - 30px);
  color: #333;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.board_view {
  margin-bottom: 14px;
  border-top: 2px solid #3d73af;
  border-bottom: 1px solid #c5c9cc;
  background-color: #f2f3f5;
}

.board_view .date {
  display: block;
  line-height: 14px;
  font-size: 12px;
  color: #6f6f6f;
}

.board_view .view_title {
  padding: 20px 21px 10px;
  border-bottom: 1px solid #c5c9cc;
  background-color: #dee1e6;
}

.board_view .view_title p {
  margin-bottom: 12px;
  line-height: 20px;
  font-size: 14px;
  font-weight: 700;
  color: #333;
}

.board_view .view_content {
  padding: 25px 21px 15px;
  line-height: 20px;
  border-bottom: 2px solid #dcdfe3;
}

.board_view .view_content p {
  line-height: 20px;
}

.board_view .view_content .view_attach {
  position: relative;
  margin-top: 45px;
  padding-left: 80px;
  min-height: 25px;
}

.board_view .view_content .view_attach .tit {
  position: absolute;
  top: 0;
  left: 0;
  display: inline-block;
  width: 64px;
  height: 23px;
  line-height: 24px;
  font-size: 12px;
  color: #333;
  text-align: center;
  border: 1px solid #bfc4c8;
  border-radius: 3px;
}

.board_view .view_content .view_attach .file {
  display: block;
  padding-top: 3px;
}

.board_view .view_content .view_attach .file a {
  margin-right: 10px;
  font-size: 12px;
  color: #6f6f6f;
  text-decoration: underline;
}

.board_view .view_replay {
  padding: 0 21px;
  border-top: 1px solid #f2f3f5;
}

.board_view .view_replay .tit {
  position: relative;
  display: block;
  margin: 20px 0;
  padding-left: 15px;
  line-height: 20px;
  font-size: 12px;
  font-weight: 700;
  color: #333;
}

.board_view .view_replay .tit:before {
  position: absolute;
  top: 6px;
  left: 0;
  content: "";
  display: block;
  width: 7px;
  height: 7px;
  background-color: #2165c1;
  border-radius: 50%;
}

.board_view .view_replay .replay_list {
  overflow-y: auto;
  margin-bottom: 20px;
  height: 150px;
}

.board_view .view_replay .replay_list ul li {
  position: relative;
  padding-left: 13px;
  line-height: 19px;
  font-size: 12px;
  margin-bottom: 8px;
  display: block;
}

.board_view .view_replay .replay_list ul span {
  position: relative;
  padding-left: 13px;
  line-height: 19px;
  font-size: 12px;
}

.board_view .view_replay .replay_list ul li:before {
  position: absolute;
  top: 5px;
  left: 0;
  content: "";
  display: block;
  width: 5px;
  height: 5px;
  background-color: #cbcdd1;
  border-radius: 50%;
}

.board_view .view_replay .replay_list ul li+li {
  margin-top: 18px;
}

.board_view .view_replay .replay_list ul li .date {
  margin-top: 2px;
}

.board_view .view_replay .replay_list::-webkit-scrollbar {
  width: 11px;
  background: transparent;
}

.board_view .view_replay .replay_list::-webkit-scrollbar-track {
  background: transparent;
}

.board_view .view_replay .replay_list::-webkit-scrollbar-thumb {
  border-radius: 11px;
  background: #a2a2a2;
}

.board_view .view_replay .replay_input {
  padding: 15px 0;
  border-top: 1px solid #d2d6db;
}

.board_view .view_replay .replay_input textarea {
  display: block;
  padding: 10px 15px;
  width: 100%;
  height: 86px;
  line-height: 18px;
  border: 1px solid #cfd3d6;
  background-color: #fff;
  resize: none;
}

.board_view .view_replay .replay_input textarea::-webkit-scrollbar {
  width: 15px;
  background: transparent;
}

.board_view .view_replay .replay_input textarea::-webkit-scrollbar-track {
  background: #eee;
  border-radius: 0 3px 3px 0;
}

.board_view .view_replay .replay_input textarea::-webkit-scrollbar-thumb {
  border: 2px solid transparent;
  border-radius: 11px;
  background: #a2a2a2;
  background-clip: padding-box;
}

/* .board_view .view_replay .replay_input .btn_wrap {margin:8px 0 0;} */
.date {

  position: relative;
  bottom: 10px;
  color: #797979;
  display: inline-block;
  margin-left: 8px;
  font-size: 1rem;
}

.btn_wrap {
  display: inline-block;
  margin-top: 0px;
  margin-bottom: 0px;
  margin-left: 10px;
  left: 10px;
}

.btn_wrap .btn {
  padding: 0 12px;
  min-width: 50px;
  height: 23px;
  line-height: 25px;
  color: #4b4b4b;
  background-color: #e9ebec;
  border-color: #29292b;
}


.board_view .view_replay .replay_list ul li .btn_wrap .btn {
  padding: 0 12px;
  min-width: 50px;
  height: 23px;
  line-height: 25px;
  color: #4b4b4b;
  background-color: #e9ebec;
}

.board_view .view_replay .replay_list ul li .btn_wrap .btn+.btn {
  margin-left: 3px;
}

.board_view .view_replay .replay_list ul li .btn_wrap .btn {
  padding: 0 12px;
  min-width: 50px;
  height: 23px;
  line-height: 25px;
  color: #4b4b4b;
  background-color: #e9ebec;
}

.board_view .view_replay .replay_list ul li .btn_wrap .btn+.btn {
  margin-left: 3px;
}

/* .btn {
  margin:4px 2px 10px 2px;
  font-size: 10px;
  padding: 0px;
  height: 22px;
  width: 40px;
  min-width: 60px;
  line-height: 0px;
}  */

.hide {
  display: none;
}

.file {
  cursor: pointer;
  text-decoration: underline;
  line-height: 22px;
}</style>