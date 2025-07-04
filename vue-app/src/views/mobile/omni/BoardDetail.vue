<template>
  <div class="mo_contents">
    <div class="board_view">
      <div>
        <div class="view_title">
          <p>{{ title }}</p>
          <span class="date">{{ formatDate(boardDate) }}</span>
          <span class="user_id">ID: {{ BoardWriterId }}</span>
        </div>

        <div class="view_content">
          <p>{{ boardContents }}</p>
          <!--  <p v-model="this.boardContents.length >= 35">{{ boardContents }} <br /></p>-->

          <div class="view_attach">
            <span class="tit">첨부파일</span>
            <span class="file" v-for="fitem in boardFileData" :key="fitem.fileId"><a @click="downloadBoardFile">{{
              fitem.fileName
            }}</a></span>
            <span class="file" v-if="boardFileData.length <= 0"><a href="javascript:downloadBoardFile();">"파일첨부
                해당없음"</a></span>
          </div>
        </div>
      </div>
      <div class="view_replay">
        <span class="tit">답변사항 리스트</span>
        <div class="replay_list" v-if="canShowcommentList == true">

          <div v-for="citem in baordCommentData" :key="citem.boardCommentId">
            <ul v-if="citem.writerName != boardWriterName && citem.parentCommentId == 0">
              <li>
                <span style="color: #0094FA; "> 관리자 : </span>
                <span v-html="formatContent(citem.contents)"></span>
                <span class="date">{{ formatDate(citem.dateCreated) }}</span>
              </li>
            </ul>
            <ul class="childreplyAdmin" v-if="citem.writerName != boardWriterName && citem.parentCommentId > 0">
              <li>
                <span style="color: #0094FA; "> 관리자 : </span>
                <span v-html="formatContent(citem.contents)"></span>
                <span class="date">{{ formatDate(citem.dateCreated) }}</span>
              </li>
            </ul>

            <ul class="childreplyUser" v-if="citem.writerName == boardWriterName && citem.parentCommentId > 0"><span
                style="color: #41B883; ">{{ citem.userId }} 사용자 : </span>
              <span v-html="formatContent(citem.contents)"></span>
              <span class="date">{{ formatDate(citem.dateCreated) }}</span>

            </ul>


          </div>
          <div class="btn_wrap" v-show="commentLength == true">
            <button type="button" class="btn " @click="uploadPossibleStatus()">댓글달기</button>
          </div>

        </div>
        <div class="replay_input" v-if="canUploadComment === true">
          <textarea name="" id="" cols="30" rows="10" @change="input"></textarea>
          <div class="btn_wrap">
            <button type="button" class="btn btn_blue2" @click="uploadBoardComment()">답변등록</button>
            <button type="button" class="btn btn_blue2" @click="cancelNewComment()">취소</button>
          </div>
        </div>
      </div>

    </div>
    <!-- // board_view -->


    <!-- board_btm -->
    <router-link to="/mobile/omni/board" class="subject">
      <div class="btn_wrap board_btm">
        <a class="btn btn_vwhite btn_full" href="">목록</a>
      </div>
    </router-link>
    <!-- // board_btm -->

  </div>
</template>
  
<script>

import axios from 'axios';
import { jqueryUI } from '../../../services/jquery-ui';
import { utils } from '../../../services/constants';
import { constants, FUNCTION_NAMES } from '../../../services/constants';
import dayjs from 'dayjs';
import boardService from '../../../services/board.service';
//import jsfiledownload from 'js-file-download';
import AlertSevice from '../../../services/alert.service';


export default {
  name: 'MobileOmniBoardDetail',
  components: {
    dayjs,
    //jsfiledownload,
  }
  , data() {
    return {
      filters: {
        boardItemId: this.$route.params.boardItemId,


      },
      boardItemData: [],
      boardFileData: [],
      baordCommentData: [],
      uerId : undefined,
      title: undefined,
      boardContents: undefined,
      boardDate: undefined,
      boardItemId: undefined,
      baordWriterId: undefined, // Board 게시판에서 user사용자[대상자 ID]를 UserId컬럼값이 BoardComment에서 boardWriterId에 insert한 것.[2026/06/21]
      boardWriterName: undefined,
      boardFileName: undefined,
      boardCode: undefined,
      fileId: undefined,
      fileName: undefined,
      theCommentPermission: false,
      data: [], //코멘트 댓글조회리스트 담는 배열부분
      pCommentId: undefined,
      inputText: '',
      commentLength: false,  //댓글달기 버튼의 활성화를 위한 Commentlist counting을 하여 활성화 여부 조건을 위한 변수 선언

      searchCommentData: {
        boardItemId: undefined,
        boardCommentId: undefined,
        contents: undefined,
        userId: undefined,
        writerId: undefined,
        writerName: undefined,
        parentCommentId: undefined,
      }


    }
  },
  computed: {
    canUploadComment() {
      return this.theCommentPermission;
    },
    canShowcommentList() {
      return this.commentLength;
    }
  },
  watch: {

  },
  methods: {
    finadByBoardItemId() {

      boardService.getOneBoard(this.filters.boardItemId).then(
        result => {

          if (result.data.succeeded) {

            
            this.boardCode = result.data.data.boardCode;
            this.boardItemId = result.data.data.boardItemId;
            // this.userId = result.data.data.userId;
            this.title = result.data.data.title;
            this.boardContents = result.data.data.contents;
            this.boardDate = result.data.data.dateCreated;
            //this.BoardWriterId = result.data.data.writerId;
              this.BoardWriterId = result.data.data.userId;  // 모바일 게시판문의 등록한 사용자의 대상자 ID => 대상자의 BoardComment의 writerId 및 userId에 넣기위한 변수
            
            this.boardWriterName = result.data.data.writerName;
            this.searhAll(this.boardItemId)

          }
        }, error => {
          console.error(error);
        })

    },


    formatDate(dateString) {
      return utils.formatSimpleDateKorean(dateString)
    },

    formatContent(content) {
      return !content ? "" : content.replace(/\n/g, '<br/>');
    },
    getBoardFiles() {
      boardService.getMobileBoardFiles(this.filters.boardItemId).then(
        result => {

          if (result.data.succeeded) {
            this.boardFileData = result.data.data;
            result.data.data.forEach((n, v) => {  
              this.fileName = n.fileName;
              this.fileId = n.fileId;



            })
          }
        }, error => {
          console.error(error);
        })
    },
    downloadBoardFile() {
      if (!this.fileId || isNaN(this.fileId)) return;

      boardService.downloadingBoardFile(this.filters.boardItemId, this.fileId).then(response => {

          const url = window.URL.createObjectURL(new Blob([response.data]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', this.fileName);
          document.body.appendChild(link);
          link.click();
          link.remove();
        },
          error => {
            console.error(error);
          })
    },
    uploadPossibleStatus() {
      boardService.searchCommentStatus(this.boardItemId).then(result => {

        //query findoneComment 
        if (result.data.succeeded) {

          this.searchCommentData.boardCommentId = result.data.data.boardCommentId; //부모댓글 boardCommentId seq pk 값
          this.searchCommentData.parentCommentId = result.data.data.parentCommentId; //부모댓글 parentCommentId 값
          this.searchCommentData.boardItemId = result.data.data.boardItemId; //부모댓글[연구자] 게시판시퀀스아이디값넣기   this.baordItemId, this.searchCommentData.boarItemIds 같은 값임 
          this.searchCommentData.writerId = result.data.data.writerId; // 부모댓글[연구자] 아이디값넣기 ==> userId 값 넣기 
          this.searchCommentData.userId = result.data.data.userId;


          if (this.baordCommentData <= 0) {

            AlertSevice.info('관리자답변 미상태로 답변등록이 불가능합니다');
            return;
          }
          this.theCommentPermission = true;
        }

      });
    },
    searhAll(id) {
      boardService.searchAllComment(this.boardItemId).then(result => {

        if (result.data.succeeded) {

          this.baordCommentData = result.data.data;

          if (this.baordCommentData.length > 0) {
            this.commentLength = true;
          }

        }

      });

    },
    input(e) {
      this.inputText = e.target.value;
    }, 
    uploadBoardComment() {    
      this.searchCommentData.contents = this.inputText;

      if(this.BoardWriterId != this.searchCommentData.writerId ){

          
        var data = {
        boardCommentId: this.searchCommentData.boardCommentId,
        boardItemId: this.searchCommentData.boardItemId,
        contents: this.searchCommentData.contents,
        writerId: this.BoardWriterId, // boardService.getOneBoard 에서 사용자 userId 인서트하기 
        userId : this.BoardWriterId,
        writerName: this.boardWriterName,
        parentCommentId: this.searchCommentData.parentCommentId,

      };
    

      }

      boardService.addBoardComment(this.searchCommentData.boardItemId, this.searchCommentData.boardCommentId, data).then(result => {
        if (result.data.succeeded) {

          AlertSevice.info("댓글 등록이 완료되었습니다.")

        }

      });
      setTimeout(() => {

        this.searhAll(this.boardItemId)
        this.finadByBoardItemId();
        this.theCommentPermission = false;

      }, 2000);
    },
        cancelNewComment() {
          this.inputText = undefined;
          this.theCommentPermission = false;
        }

  },
  mounted() {
    this.finadByBoardItemId();
    this.getBoardFiles();

  },
  created() {

  },
}
</script>
  
<style scoped>
.childreplyUser {
  display: inline-block;
  margin-left: 12px;
  font-size: 1.2rem;
}

.childreplyAdmin {
  display: inline-block;
  /* margin-left: 25px; */

}

.date {
  display: inline-block;
  margin-left: 8px;
  font-size: 1rem;
}

.btn:before {
    content: "";
    display: inline-block;
    margin: 0 0 0 0;
    width: 5px;
    height: 7px;
    background: none;
    vertical-align: middle;
}

.user_id{
    display: block;
    margin-left: 8px;
    font-size: 1.1rem;
    float: right;
    color: #797979;
    line-height: 2rem;
}


</style>
  
  