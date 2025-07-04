<template>
  <div>
    <k-header></k-header>
    <div id="gnb">
      <h1 class="logo"><a href="/"><img src="../assets/images/logos.png" alt="KDRC 치매극복연구개발사업단"></a></h1>
      <div class="gnb_wrap ff_nbg">
        <ul class="menu">
          <li v-for='menu in topMenus' :key="menu.menuId" :class="{ current: topMenuName == menu.menuId }">
            <router-link :to="menu.link" v-if="menu.menuId == 'BOARD_QNA'"><img :src="menu.icon"
                alt="">{{ menu.title }}</router-link>
            <!-- <span :name="menu.menuId" style="cursor:pointer; display: none;" v-on:click="showSubMenu" v-if="menu.level == 0 && menu.menuId =='BOARD_QNA'"><img :src="menu.icon" alt="">{{menu.title}}</span> -->
            <span :name="menu.menuId" style="cursor:pointer; display: show;" v-on:click="showSubMenu"
              v-if="menu.level == 0 && menu.menuId != 'BOARD_QNA'"><img :src="menu.icon" alt="">{{ menu.title }}</span>
            <ul class="submenu">
              <li v-bind:class="{ current: selectedMenu == sub.link }" v-for='sub in getSubMenus(menu.menuId)'
                :key="sub.menuId">
                <router-link :to="sub.link">{{ sub.title }}</router-link>
              </li>
            </ul>
          </li>
        </ul>
      </div>
      <div id="container">
    <button class="learn-more">
      <span class="circle" aria-hidden="true">
        <span class="icon arrow"></span>
      </span>
      <span class="button-text" @click="linkEcrf">대상자포탈<br>바로가기 </span>
    </button>
  </div>
    </div>

    <div id="bodycontainer">
      <router-view></router-view>
    </div>
    <br><br>
    <!-- <k-footer>
      <slot name="footer"></slot>
    </k-footer> -->
  </div>
</template>

<script>
import router from '../router';
import MenuService from '../services/menu.service';
// import PerfectScrollbar from '../assets/js/perfect-scrollbar';
// import PerfectScrollbara from '../assets/js/dashbaordtable-scrollbar';
import authService from '../services/auth.service';
import alertService from '../services/alert.service';
import { constants ,GUEST_URL} from '../services/constants'
const initMenuFnc = function (topMenu) {
  $(document).ready(function () {
    const el = (document.getElementsByName(topMenu) || [])[0];
    showSubMenuFnc(el);
  });
}

const showSubMenuFnc = function (element) {
  const parent = $(element).parent();
  var hasSubmenu = parent.parent().find('.submenu').length > 0;

  if (hasSubmenu === true) {
    parent.siblings().removeClass('current').find('.submenu').slideUp();
    parent.addClass('current').find('.submenu').slideDown();

    return false;
  }
}

export default {
  name: 'Home',
  components: {
    // PerfectScrollbar,
    // PerfectScrollbara

  },
  documentTitle: 'Home',
  data() {
    return {
      publicPath: process.env.BASE_URL,
      menus: [],
      selectedMenu: '',
      topMenuName: '',
      tokenkey: undefined
    }
  },
  computed: {
    topMenus() {
      return this.menus.filter(x => x.level == 0) || [];
    },
  },
  watch: {
    $route(to) {
      this.selectedMenu = to.path;
      const menu = (this.menus || []).filter(x => x.level == 1 || x.level == 2).sort(x => x.sortIndex)[0];
      if (menu) {
        this.topMenuName = menu.pMenuId;
      }
    }
  },
  methods: {
    showSubMenu(event) {
      showSubMenuFnc(event.target);
    },
    getSubMenus(menuId) {
      return this.menus.filter(x => x.pMenuId == menuId && x.level > 0) || [];
    },
    getMenu() {
      MenuService.getUserMenu().then(
        result => {
          if (result.data.succeeded) {
            this.menus = result.data.data;
            router.replace({path: '/dashboard'});
           /*
            let url = router.currentRoute.value.path;


            if (url == '/home') {
              // Jump to the page of the first menu.
              const menu = (this.menus || []).filter(x => x.level == 1 || x.level == 2).sort(x => x.sortIndex)[0];
              if (menu) {
                url = menu.link;
                this.topMenuName = menu.pMenuId;
                router.replace({ path: url });
              }
            }
            else {


              const menu = (this.menus.filter(x => url == x.link) || [])[0];
              if (menu) {

                this.topMenuName = menu.pMenuId;

              }
            }

            this.selectedMenu = url;
            initMenuFnc(this.topMenuName);
            */
          }
        },
        error => {
          console.error(error);
        });
    },
    linkEcrf() {
  authService.redirectEcrf().then(response => {
    console.log(response);
    if (response.status === 200) { 
      const redirectUrl = response.data; 
      console.log("targetUrl =>", redirectUrl);
      if (redirectUrl) {
        window.open(redirectUrl, '_blank');
      }
    }
  }).catch(error => {
    console.error('Error:', error);
  });
}

  },
  created() {
    this.tokenkey = sessionStorage.getItem(constants.STORAGE_KEY_TOKEN);
    console.log(this.tokenkey)
    this.getMenu();
  },
}
</script>

<style scoped>
@import "../assets/css/style.css";
@import "../assets/css/perfect-scrollbar.css";
@import "../assets/css/comboTree.css";

.gnb_wrap .menu>li>span {
  position: relative;
  display: block;
  padding-left: 28px;
  color: #fff;
}

.gnb_wrap .menu>li>span:before {
  position: absolute;
  top: 8px;
  right: 17px;
  content: "";
  display: block;
  width: 5px;
  height: 8px;
  background: url(../assets/images/arrow_r.png) no-repeat 0 0;
}

.gnb_wrap .menu>li>span img {
  display: inline-block;
  margin: -2px 10px 0 0;
  vertical-align: middle;
}





button {
  position: relative;
  display: inline-block;
  cursor: pointer;
  outline: none;
  border: 0;
  vertical-align: middle;
  text-decoration: none;
  background: transparent;
  padding: 0;
  font-size: 16px;
  font-family: inherit;
}
button.learn-more {
  width: 100%;
  height: auto;
}
button.learn-more .circle {
  transition: all 0.45s cubic-bezier(0.65, 0, 0.076, 1);
  position: relative;
  display: block;
  margin: 0;
  width: 0rem;
  height: 5rem;
  background: #ffffff;
  border-radius: 1.625rem;
}



button.learn-more .circle .icon {
  transition: all 0.45s cubic-bezier(0.65, 0, 0.076, 1);
  position: absolute;
  top: 0;
  bottom: 0;
  margin: auto;
  background: #fff;
}
button.learn-more .circle .icon.arrow {
  transition: all 0.45s cubic-bezier(0.65, 0, 0.076, 1);
  left: 0.625rem;
  width: 1.125rem;
  height: 0.125rem;
  top: 4%;
  background: none;
}
button.learn-more .circle .icon.arrow::before {
  position: absolute;
  content: "";
  top: -0.25rem;

  width: 0.625rem;
  height: 0.625rem;
  border-top: 0.125rem solid #ffffff;
  border-right: 0.125rem solid #ffffff;
  transform: rotate(45deg);

}
button.learn-more .circle .icon.arrow::after {
  position: absolute;
  content: "";
  top: -0.25rem;
  right: 0.0625rem;
  width: 0.625rem;
  height: 0.625rem;
  border-top: 0.125rem solid #282936;
  border-right: 0.125rem solid #282936;
  transform: rotate(45deg);
  z-index: 2;
}
button.learn-more .button-text {
  transition: all 0.45s cubic-bezier(0.65, 0, 0.076, 1);
  position: absolute;
  top: 3;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 0.75rem 0;
  margin: 0 0 0 1.85rem;
  color: #ffffff;
  font-weight: 700;
  line-height: 1.6;
  text-align: center;
  text-transform: uppercase;
}
button:hover .circle {
  width: 100%;
}
button:hover .circle .icon.arrow {
  top: 0.5%;
  background: #282936;
  transform: translate(1rem, 0);
}
button:hover .button-text {
  color: #282936;
}

#container {
  position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 15px;
    box-sizing: border-box;
}

</style>

