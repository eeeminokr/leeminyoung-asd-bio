<template>
    <div v-if="userLoggedIn">
      <router-view></router-view>
    </div>
  </template>
  
  <script>  
  import AuthService from '../../../services/auth.service';

  export default {
    name: 'MobileOmniHome',
    data() {
      return {
        systemId: 'ASD',
        username: 'omni',
        password: 'HGbOq9',
        userLoggedIn: false,
      }
    },
    computed: {
    },
    watch:{
    },
    methods:{
      login() {
        this.$store.dispatch("auth/login", {userId: this.username, password: this.password, systemId: this.systemId.toUpperCase()})
        .then(
          (data) => {
            this.userLoggedIn = AuthService.isAuthenticated();
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
                alert (`로그인 시도 중 오류가 발생했습니다.\n잠시 후 다시 시도해 주세요.\n(${msg})`);
              }            
          })
        );
      }
    },
    created() {
      this.login();
    }
}
</script>
  
  <style scoped>
    @import "../../../assets/mobile/css/style.css";
    .btn:before {
    content: "";
    display: inline-block;
    margin: 0 0 0 0;
    width: 5px;
    height: 7px;
    background: none;
    vertical-align: middle;
}
  </style>
  
  