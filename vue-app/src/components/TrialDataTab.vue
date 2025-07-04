<template>
  <ul class="tabs">
    <li><router-link :to="makeUrl('/trial')">통합</router-link></li>
    <li v-for="page in menu1" :key="page.pageId">
      <template v-if="page.url">
        <router-link :to="makeUrl(page.url)">{{ page.title }}</router-link>
      </template>
      <template v-else>
        <a class="has_sub" @click="toggleSubMenu(page.menuId)"><span>{{ page.title }}</span></a>
      </template>
    </li>
  </ul>
  <div v-for="[key, value] in menu2" class="tab_child" :style="{ display: parentMenuId == key ? 'block' : 'none' }">
    <button type="button" class="" v-for="m in value">
      <template v-if="m.title !== 'EEG' && m.menuId !== 'TRIAL_DIGITALHEALTH_VITALSIGNS' && m.menuId !== 'TRIAL_DIGITALHEALTH_AUDIORESOURCE'">
        <router-link :to="makeUrl(m.url)">{{ m.title }}</router-link>
      </template>
      <template v-else>
        <span>{{ m.title }}</span>
      </template>
    </button>
  </div>
</template>

<script>
import PageService from '../services/page.service';

export default {
  name: 'TrialDataTab',
  components: {
  },
  props: {
    menuId: String,
    subMenuId: String,
    projectId: Number || String,
    activeLink: undefined,
  },
  data() {
    return {
      pages: [],
      parentMenuId: undefined,
    }
  },
  computed: {
    menu1() {
      const list = (this.pages || []).filter(x => x.level == 2);
      return (this.pages || []).filter(x => x.level == 2);
    },
    menu2() {
      const list = (this.pages || []).filter(x => x.level == 3);
      list.sort((a, b) => { return a.sortIndex - b.sortIndex; });
      const map = new Map();
      list.forEach(el => {
        const pmenu = el.pMenuId;

        if (map.has(pmenu)) {
          map.get(pmenu).push(el);
        }
        else {
          map.set(pmenu, [el]);
        }
      });
      return map;
    },
    selectedTabMenu() {
      const path = this.$route.path;

      const submenu = (this.pages || []).filter(x => x.url?.startsWith(path));

      const menu = submenu?.length > 0 ?
        submenu[0].level == 2 ?
          submenu[0].menuId :
          (this.parentMenuId = submenu[0].pMenuId) :
        undefined;
 
      return menu;
    },
  },
  watch: {
    projectId(newVal) {
      this.getPages();
    }
  },
  methods: {
    makeUrl(url) {
      return url + "?project=" + this.projectId;
    },
    toggleSubMenu(menu) {
      this.parentMenuId = !!this.parentMenuId ? undefined : menu;

    },
    handleClick(event) {
      event.preventDefault();
    },
    isActive(url) {
      return this.activeLink == url;
    },
    getPages() {
      this.pages = [];
      PageService.getProjectPages(this.projectId, this.menuId).then(result => {
        if (result.data.succeeded) {

          this.pages = result.data.data;
        }
      }, error => {
        console.error(error);
      });
    },
    handleMenuClick(event, title) {
      if (title === 'EEG' || title === 'NIR') {
        event.stopPropagation(); // 클릭 이벤트 막기
      }
    },
  },
  mounted() {
  }
}
</script>

<style scoped>
/* .router-link-active {
    background-color: #2165c1;
} */
.tabs>li>a.router-link-active {
  background-color: #2165c1;
}

.tab_child {
  display: none;
  overflow: hidden;
  padding: 9px 10px 9px 25px;
  width: 100%;
  border: 1px solid #2165c1;
  border-top: 0;
  border-radius: 0 0 5px 5px;
  box-sizing: border-box;
  text-align: left;
  background-color: #fff;
}

.tab_child button {
  position: relative;
  float: left;
  display: block;
  margin-right: 17px;
  padding-left: 13px;
  font-size: 12px;
  font-weight: 400;
  color: #333;
  line-height: 20px;
  background-color: transparent;
  border-radius: 0;
}

.tab_child button:before {
  position: absolute;
  top: 6px;
  left: 0;
  content: "";
  display: block;
  width: 5px;
  height: 5px;
  background-color: #446fff;
  border-radius: 50%;
}

.tab_child button.active {
  font-weight: 700;
  color: #2b83cd;
}

.has_sub a {
  position: relative;
}

.has_sub span:after {
  position: absolute;
  top: 13px;
  right: 11px;
  content: "";
  display: block;
  width: 7px;
  height: 5px;
  background: url(../assets/images/arrow_updn.png) no-repeat 0 0;
  transform: rotate(180deg);
}

.has_sub.router-link-active span:after {
  top: 12px;
  transform: rotate(0);
}

.has_sub.router-link-active {
  background-color: #2165c1;
}

.tab_child button>a {
  color: #333;
}

.tab_child a.router-link-active {
  font-weight: 700;
  color: #2b83cd;
}

.tabs>li>div a {
  display: block;
  padding: 0 18px;
  color: #fff;
  background: #78808a;
  border-radius: 10px 10px 0 0;
}
</style>
