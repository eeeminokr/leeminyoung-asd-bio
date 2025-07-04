<template>
  <div class="mo_contents">

    <!-- board_write -->
    <div class="board_write">

      <div class="write_form">
        <form action="">
          <legend>질문등록 폼</legend>
          <ul class="form_list">
            <li style="margin-right: 80px;">
              <label for="id" class="label">대상자 ID</label>
              <div class="wrap">
              <input type="text" name="id" id="writerName" v-model="userId" @input="inputVaildUserId($event)" />

              <a class="status" :class="{ gwhite: !attachGwhite, green: attachGwhite }" @click="vaildIdcheck($event)">{{
                idmessege }}</a>
              <div class="tooltip">
                <p>3자 이상 10자 이하, </p>영어 또는 숫자로 구성
              </div> <!--className="task-tooltip" -->
              </div>
            </li>
            <li>
              <label for="writerName" class="label">성명</label>
              <input type="text" name="writerName" id="id" v-model="writerName" />
            </li>


            <li>
              <label for="password" class="label">비밀번호</label>
              <input type="password" name="password" id="password" v-model="password" maxlength="6"
                placeholder="숫자[6자리입력]" />
            </li>
            <li>
              <label for="org" class="label">등록기관</label>
              <!-- <option :key="i" :value="d.v" v-for="(d, i) in options">{{ d.t }}</option> -->
              <select class="w_206" v-model="filters.orgId">
                <option value="">::선택::</option>
                <option v-for="op in organizations" :key="op.orgId" :value="op.orgId">{{ op.orgName }}</option>
              </select>
            </li>
            <li>
              <label for="title" class="label">제목</label>
              <input type="text" name="title" id="title" v-model="title" />
            </li>
            <li>
              <label for="contents" class="label">내용</label>
              <textarea name="contents" id="contents" cols="30" rows="10" v-model="contents"></textarea>
            </li>
            <li>
              <label for="" class="label">첨부파일</label>
              <div class="filebox">
                <input type="text" class="upload_name" v-bind:value="filename" readonly />
                <label for="file" class="btn btn_file">파일선택</label>
                <input type="file" name="file" id="file" class="upload_hidden" v-on:change="selectFile()" multiple
                  ref="file" />
              </div>
              <p class="text_info">* 첨부파일은 이미지파일(jpg, png, gif)만 등록 가능합니다. <span class="dpy_ib">(최대 5mb)</span></p>
            </li>
          </ul>
        </form>
      </div>

    </div>
    <!-- // board_write -->


    <!-- board_btm -->
    <router-link to="/mobile/omni/board" class="subject">
      <div class="btn_wrap board_btm ta_c" style="float: right;">
        <a class="btn btn_vwhite btn_full" href="">목록</a>
      </div>
    </router-link>
    <!-- // board_btm -->


    <!-- board_btm -->
    <div class="btn_wrap board_btm ta_c">
      <a class="btn btn_vwhite btn_full" @click="uploadBoard()">문의등록</a>
    </div>
    <!-- // board_btm -->

  </div>
  <div v-if="goList === true">

  </div>
  <!-- 팝업 : 비밀번호입력 -->
  <div class="popup_bg" v-if="checkparam == true"></div>
  <div class="popup pop_confirmPw" v-if="checkparam == true">
    <button type="button" class="btn_close" @click="cancle()"><span class="blind">팝업 창닫기</span></button>
    <div class="pop_cont">
      <p id="pwdtext">{{ pwdtext }}</p>
      <div class="input">
        <input type="password" name="pasword" id="pasword" v-model="password" maxlength="6" placeholder="숫자[6자리입력]" />
      </div>
    </div>
    <div class="pop_btm">
      <button type="button" class="btn btn_cancel" @click="cancle()">취소</button>
      <button type="button" class="btn btn_confirm" @click="registboard()">확인</button>
    </div>
  </div>
  <!-- //팝업 : 비밀번호입력 -->


  <!-- 팝업 : 등록완료 
      <div class="popup_bg"></div>
    <div class="popup pop_completed">
        <button type="button" class="btn_close"><span class="blind">팝업 창닫기</span></button>
        <div class="pop_cont">
            <p>문의 등록이 완료 되었습니다.</p>
        </div>
        <div class="pop_btm">
            <button type="button" class="btn btn_confirm">확인</button>
        </div>
    </div>
     //팝업 : 등록완료 -->
</template>
  
<script>
import axios from 'axios';
import { constants, utils, FUNCTION_NAMES } from '../../../services/constants';
import boardService from '../../../services/board.service';
import AlertSevice from '../../../services/alert.service';
import { genPropsAccessExp } from '@vue/shared';
import { processExpression } from '@vue/compiler-core';
import ProjectService from "../../../services/project.service";

export default {
  name: 'MobileOmniBoardEdit',
  data() {
    return {
      userId: '',
      // color: green,
      attachGwhite: false,
      idmessege: 'ID 체크',
      writerName: '',
      password: '',
      title: '',
      contents: '',
      file: '',
      filename: '',
      number: '',
      successed: false,
      checkparam: false, //비밀번호 팝업 USESTATUS
      canclecheckbutton: false, // 비밀번호 팝업 확인,취소 STATUS
      pwdtext: '비밀번호를 입력해 주세요.', //비밀번호 팝업 <p>text 값
      resigterSuccessed: false,
      filters: {
        projectId: "",
        subjectId: "",
        orgId: "",
        orgName: "",
        page: 1,
        // offset: ROWS,
      },
      organizations: [],
      systemId: undefined,
      checkedUserIdinfo: undefined,
      changeVaildUserId: false,
    };
  },
  computed: {
    setParams() {
      const params = {
        userId: this.userId,
        writerName: this.writerName,
        password: this.password,
        title: this.title,
        contents: this.contents,
      };
      return params;
    },
    goList() {
      return this.successed;
    },
    idInput: function () {
      return {
        gwhite: this.attachGwhite,
        green: !this.attachGwhite


      }



    }

  },
  watch: {
    password: function () {
      return this.password = this.password.replace(/[^0-9]/g, '');  //정규식 사용    
    }

    ,
    'filters.orgId'(orgId) {
      const selectedOrg = this.organizations.find(org => org.orgId === orgId);
      if (selectedOrg) {
        this.filters.orgName = selectedOrg.orgName;

      } else {
        this.filters.orgName = '';
      }
    }


  },
  methods: {
    uploadBoard() {


      const formData = new FormData();
      // for(const file of event.target.files){
      //   formData.append('files',file);
      // }

      if (this.setParams.userId <= 0) {
        AlertSevice.info("ID을 기입해주세요")
        return;
      } else if (!this.attachGwhite) {
        AlertSevice.info("ID 체크를 확인해주세요")
        return;
      }
      else if (this.setParams.writerName <= 0) {
        AlertSevice.info("성명을 기입해주세요")
        return;
      } else if (this.setParams.password.length < 6) {
        AlertSevice.info("비밀번호를 기입해주세요")
        this.checkparam = true;
        this.pwdtext = "비밀번호 6자리까지 입력해주세요";
        return;
      } else if (this.setParams.title <= 0) {
        AlertSevice.info("제목을 기입해주세요")
        return;
      } else if (this.setParams.contents <= 0) {
        AlertSevice.info("내용을 기입해주세요")
        return;
      }
      else if (this.filters.orgId <= 0) {
        AlertSevice.info("해당등록 기관을 선택해주세요")
        return;
      }


      formData.append('file', this.file);
      formData.append('userId', this.setParams.userId);
      formData.append('writerName', this.setParams.writerName);
      formData.append('title', this.setParams.title);
      formData.append('contents', this.setParams.contents);
      formData.append('password', this.setParams.password);
      formData.append('orgId', this.filters.orgId)
      formData.append('orgName', this.filters.orgName)



      boardService.addBoard(formData).then(
        result => {
          if (result.data.succeeded) {
            AlertSevice.info("등록이 완료되었습니다.")
            this.successed = true;
            setTimeout(() => {
              this.$router.push("/mobile/omni/board") // URL로 이동
            }, 2000);

          }


        }).catch(error => {
          AlertSevice.info(error.message)

        })

    },
    selectFile() {

      this.file = this.$refs.file.files[0];
      this.filename = this.$refs.file.files[0].name;

    },
    registboard() {

      if (this.setParams.password < 7) {
        this.checkparam = true;
        this.pwdtext = "6자리 까지 입력해주세요"
        return false;
      } else {
        this.checkparam = false;
      }



    }, cancle() {
      this.password = '';
      this.checkparam = false;

    },
    getAsdOrganizations() {

      boardService.getfindAllOrganizations().then(result => {
        if (result.data.succeeded) {


          this.organizations = result.data.data;



        }




      })


    },
    inputVaildUserId(event) {

      if (event.isTrusted) {
        if (this.attachGwhite && this.checkedUserIdinfo != null) {
          this.attachGwhite = false;
        }
        this.attachGwhite === true ? this.idmessege = "ID 사용가능" : this.idmessege = "ID 체크";
      }


    }
    , vaildIdcheck() {

      if (this.userId) {
        this.checkedUserIdinfo = this.userId;
      }

      let idcheck = true;

      idcheck = utils.validatedIdFormat(this.userId)


      if (!idcheck) {
        AlertSevice.info("ID 구성에 맞게 작성해주세요")
        // this.idmessege = 'ID 체크';
        this.attachGwhite = false;
        return;
      } else {
        // this.idmessege = 'ID 사용가능';
        this.attachGwhite = true;
      }

      this.attachGwhite === true ? this.idmessege = "ID 사용가능" : this.idmessege = "ID 체크";


    }



  },
  created() {

    // this.getProjects();
    this.getAsdOrganizations();


  },
}
</script>
  
<style scoped>
@import "../../../assets/mobile/css/style.css";

.filebox .btn_file {
  background: none;
  display: inline-block;
  margin: 0;
  padding: 0 5px;
  min-width: 75px;
  height: 32px;
  font-size: 1.2rem;
  font-family: 'NanumBarunGothic', 'sans-serif';
  color: #fff;
  line-height: 32px;
  border-radius: 25px;
  background-color: #84858b;
  vertical-align: middle;
  white-space: pre;
  box-sizing: border-box;
  text-align: center;
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


body {
  height: auto;
}

.popup {
  /* max-width: 55%;
        height: 70%; */
  /* top: 13vh; */
  /* overflow-y: auto; */
  background-color: #fff;
}


.popup .pop_cont {
  padding-left: 30px;
  text-align: center;
  width: 100%;
  height: 100%;
  border-radius: 10px 10px 10px 10px;
  padding: 50px 50px 50px 50px;
}

.popup .pop_cont p {
  line-height: 3rem;
  font-size: 15px;

}

.popup .pop_cont .tit_s {
  margin-bottom: 30px;
  font-size: 25px;
  font-weight: 700;
  color: #634e42;
  line-height: 1.2em;
  letter-spacing: -1px;
  text-align: center
}

.terms_list>li {
  margin-bottom: 80px;
}

.terms_list>li h4 {
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: 400;
  color: #634e42;
}

.terms_list>li p {
  font-size: 18px;
  line-height: 1.6em;
}

.mb_10 {
  margin-bottom: 10px !important;
}

.mb_20 {
  margin-bottom: 20px !important;
}

.mb_30 {
  margin-bottom: 30px !important;
}


.board_btm.btn_wrap {
  margin: 37px 0;
  text-align: center;
}

.board_btm.btn_wrap .btn.btn_vwhite {
  border: 2px solid #4e56b0;
  width: 126px;
  height: 34px;
  line-height: 30px;
}


.status {
  /* display: inline-block; */
  margin: 5px 10px 0px 10px;
  padding: 0 5px;
  min-width: 30px;
  height: 32px;
  color: #fff;
  line-height: 32px;
  border-radius: 25px;
  background-color: #84858b;
  vertical-align: middle;
  white-space: pre;
  box-sizing: border-box;
  text-align: center;

  position: absolute;
  top: 0;
  /* left: 0; */
  display: inline-block;
  width: 40px;
  line-height: 32px;
  font-size: 1.2rem;
  /* text-align: right; */

}

.status.green {
  background: #505bf0;
  width: 78px;
  height: 25px;
  line-height: 25px;
}

.status.gwhite {
  /* color: #629d30; */
  color: #4e56b0;
  background: #fff;
  border: 1px solid #7689fd;
  width: 78px;
  height: 25px;
  line-height: 25px;
}

span {
  display: block;
  width: 80px;
  padding: 2px 16px;
  cursor: pointer;
}


.wrap {
  position: relative;
  display: inline-block;
  z-index: 1;
}

.box {
  width: 150px;
  background: #242F9B;
  padding: 10px;
  border-radius: 5px;
  color: #ffffff;
  text-align: center;

}

.tooltip {
  position: absolute;
  left: 0px;
  top: 52px;
  font-size: 1.2rem;
  background: #646FD4;
  padding: 10px;
  border-radius: 5px;
  color: #fff;
  text-align: center;
  display: none;

}

.tooltip:after {
  display: block;
  content: '';
  font-size: 1.2rem;
  position: absolute;
  top: -7px;
  left: 15px;
  width: 0px;
  height: 0px;
  border-top: 8px solid none;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-bottom: 8px solid #646FD4;

}

.wrap:hover .tooltip {
  display: block;
}</style>
  
  