<template>
     <router-view></router-view>
  <div id="wrap">
    <div class="login_bg">

    </div>
    <div class="login_bg2">

    </div>
    <div class="login_box">
      <div class="left">

        <h1><img style="margin-left: 40px;" src="../assets/images/login/logo.png" alt="KDRC 치매극복연구개발사업단 로고" /></h1>
        <p class="txt ff_ns">Biomedical Data Science Platform</p>
      </div>
      <div class="right">
        <form action="" method="" @submit.prevent="onSubmit">
          <fieldset>
            <legend>로그인</legend>
            <ul class="login_input">
              <li><label for="login_code"><input type="text" id="login_code" placeholder="ASD" 
                    v-model='systemId' /></label></li>
              <li><label for="login_id"><input type="text" id="login_id" placeholder="ID" v-model='username' /></label>
              </li>
              <li><label for="login_pw"><input type="password" id="login_pw" placeholder="Password"
                    v-model='password' /></label></li>
              <li class="btnwrap"><input class="btn_login ff_ns" type="submit" value="LOGIN" /></li>
            </ul>
          </fieldset>
        </form>
        <a class="privacypolicy" @click="showPolicy()">아이AI 개인정보처리방침</a>

        <div class="w-full bg-white">
      <!-- popup window -->

      <privacy v-if="this.openModal == true"  @onClose="closeModalView"/>
        </div>

      </div>
    </div>
  </div>

  
</template>

<script>

import Privacy from '../components/Privacy.vue'
export default {
  name: 'Login',
  documentTitle: 'LOGIN',
  props: {
    msg: String
  },
  components: {
    Privacy, // 여기에 추가해주기
  },
  data() {
    return {
      username: '',
      password: '',
      systemId: 'ASD',
      openModal: false,
    }
  },
  methods: {
    showPolicy(){
      this.openModal = true 
    },
    closeModalView(data) {
      
      this.openModal = data
    },
    onSubmit() {
      if (!this.username) {
        alert('아이디를 입력해 주세요.');
        return;
      }
      if (!this.password) {
        alert("비밀번호를 입력해주세요.");
        return;
      }
      if (!this.systemId) {
        alert("연구코드를 입력해주세요.");
        return;
      }

      this.$store.dispatch("auth/login", { userId: this.username, password: this.password, systemId: this.systemId.toUpperCase() })
        .then(
          (data) => {
            window.location.replace("/");
          },
          (error => {
            const msg = (error.response &&
              error.response.data &&
              error.response.data.message) ||
              error.message ||
              error.toString();

            if (error.response.status == 401) { // unauthorized
              alert(msg);
            }
            else {
              alert(`로그인 시도 중 오류가 발생했습니다.\n잠시 후 다시 시도해 주세요.\n(${msg})`);
            }
          })
        );
    }
  }
}
</script>

<style scoped>
.privacypolicy {
  position: relative;
  top: 10px;
  float: right;
  font-size: 15px;
  font-family: 'NanumBarunGothic';
	font-style: normal;
	font-weight: 400;
  color: darkmagenta;
}
.privacypolicy:hover {
  color: gainsboro;
}

@import "../assets/css/login.css"

</style>
