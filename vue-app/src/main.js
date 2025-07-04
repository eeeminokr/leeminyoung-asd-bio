import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import store from "./store"
import Header from './components/Header'
import Footer from './components/Footer'
import { library } from "@fortawesome/fontawesome-svg-core";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { fas } from '@fortawesome/free-solid-svg-icons'
library.add(fas);
import { dom } from "@fortawesome/fontawesome-svg-core";
dom.watch();
import VueNumberFormat from 'vue-number-format'

createApp(App)
    .use(router)
    .use(store)
    .use(VueNumberFormat, { prefix: '', decimal: '.', precision: 0, thousand: ','})
    .component("font-awesome-icon", FontAwesomeIcon)
    .component('k-header', Header)
    .component('k-footer', Footer)
    .mount('#app');
