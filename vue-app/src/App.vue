<template>
  <div>
    <router-view></router-view>
  </div>
</template>

<script>

global.jQuery = require("jquery");
var $ = global.jQuery;
window.$ = $;

require('jquery-ui');
require("./assets/js/jquery.bpopup.min.js");
require("./assets/js/common.js");
require("./assets/js/perfect-scrollbar.js");

import AuthService from './services/auth.service';

import Home from './views/Home.vue';
import Login from "./views/Login.vue";


export default {
  name: 'App',
  components: {
    Home,
    Login
  },
  computed: {
    loggedIn() {
      return AuthService.isAuthenticated();
    }
  },
  created: function() {    
    if (!process.env.VUE_APP_DEBUG) {
      // No right click allowed.
      window.oncontextmenu = function () {
        return false;
      }
    }

    const prevUrl = location.pathname;

    if ('/mobile/omni/board' == prevUrl) {
      this.$router.replace(prevUrl);
      return;
    }

    console.debug("prevUrl: "+prevUrl)
    if ('/policypage' == prevUrl) {
      this.$router.replace(prevUrl);
      return;
    }

    if (this.loggedIn) {
      if(prevUrl=='/mriviewer') {

      } else if (prevUrl != '/' && prevUrl != '/home') {
        this.$router.replace(prevUrl);
      }
      else {
        this.$router.replace('/home');
      }
    }
    else this.$router.replace('/login');
  }
}
</script>

<style scoped>

</style>
