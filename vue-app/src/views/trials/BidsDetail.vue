<template>
    <div class="modal-mask">
        <div class="modal-wrapper popup" style="margin-top:60px;margin-left:20%;height:960px;width:1374px; z-index: 100; opacity: 1;" >
            <div class="modal-container" style="height:100%;width:100%; ">
            <div class="pop_top">
                <h3>BIDS Description</h3>
                <button type="button" class="btn_close b-close" @click="closeMe"><span class="blind">팝업 창닫기</span></button>
            </div>
            <div class="pop_cont">
                <div class="btn_wrap ta_r">
                    <button type="button" class="btn btn_white btn_down" @click="downloadTags">다운로드</button>                    
                </div>

                <table class="tb_background" data-height="626">
                    <tbody>
                        <tr>
                            <td>
                                <div class="scroll_head">
                                    <table class="tb_head">
                                        <colgroup>
                                            <col width="50px">
                                            <col width="240px">
                                            <col width="102px">
                                            <col width="102px">
                                            <col width="102px">
                                            <col width="102px">
                                            <col width="102px">
                                            <col width="102px">
                                            <col width="522px">
                                        </colgroup>
                                        <thead>
                                            <tr class="th_1">
                                                <th rowspan="2">No</th>
                                                <th rowspan="2">Tag</th>
                                                <th colspan="6">Value</th>
                                                <th rowspan="2">Description</th>
                                            </tr>
                                            <tr class="th_2">
                                                <th>T1</th>
                                                <th>T2</th>
                                                <th>SWI</th>
                                                <th>ASL</th>
                                                <th>DTI</th>
                                                <th class="border_r">fMRI</th>
                                            </tr>
                                        </thead>
                                    </table>
                                </div>
                                <div class="scroll_body" style="max-height: 750px;">
                                    <table class="tb_body">
                                        <colgroup>
                                            <col width="50px">
                                            <col width="240px">
                                            <col width="102px">
                                            <col width="102px">
                                            <col width="102px">
                                            <col width="102px">
                                            <col width="102px">
                                            <col width="102px">
                                            <col width="522px">
                                        </colgroup>
                                        <tbody>
                                            <tr v-for="(item, index) in computedDescription" :key="item.name">
                                                <td>{{ index + 1 }}</td>
                                                <td>{{item.name}}</td>
                                                <td>{{formatText(tags['T1'][item.name])}}</td>
                                                <td>{{formatText(tags['T2'][item.name])}}</td>
                                                <td>{{formatText(tags['SWI'][item.name])}}</td>
                                                <td>{{formatText(tags['ASL'][item.name])}}</td>
                                                <td>{{formatText(tags['DTI'][item.name])}}</td>
                                                <td>{{formatText(tags['fMRI'][item.name])}}</td>
                                                <td class="ta_l">{{ item.value }}</td>
                                            </tr>
                                        </tbody>
                                    </table>                   
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</template>

<script>
import TrialService from "../../services/trial.service";

const SERIES = ['T1', 'T2', 'SWI', 'ASL', 'DTI', 'fMRI'];

export default {
  name: 'TrialBidsDetail',
  components: {
  },
  props: {
    itemId: undefined,
  },
  emits: ['closed'],
  data () {
    return {
        MENU_ID: 'TRIAL',     
        subject: undefined,
        theData: undefined,
        tags: {},
        tagDescription: undefined
    }
  },
  computed: {
    computedDescription() {
        if ((this.tagDescription || []).length <= 0) return [];

        const list = Object.entries(this.tagDescription).map(val => { return { 'name': val[0], 'value': val[1] }; });
        return list;
    },
    computedTagValues() {
        if ((this.tags || []).length <= 0) return [];

        const map = new Map();
        this.tags.forEach((value, key) => {
            const list = Object.entries(value).map(val => { return { 'name': val[0], 'value': val[1] }; });
            map.set(key, list);
        });

        console.debug('computed values ==> ' + JSON.stringify(map));

        return map;
    }
  },
  watch: {    
  },
  methods: {
    formatText(text) {
        if (!text) return '';
        return text.length > 120 ? text.substring(0, 20) + "..." : text;
    },
    closeMe() {
        this.$emit('closed', true);
    },    
    getImagingData(itemId) {
        TrialService.getImagingData(itemId).then(result=>{
            if (result.data.succeeded) {
                this.subject = result.data.data.subject;
                this.theData = result.data.data.imageData;

                // if (result.data.data.mcdData && result.data.data.mcdData.length > 0) {
                //     this.theMcdData = result.data.data.mcdData[0].data;
                // }
            }
        }, error=>{
            console.error(error);
        })
    },
    downloadTags() {
        SERIES.forEach(name => {
            const data = JSON.stringify(this.tags[name]);
            console.debug('download => ' + data);
            if (!!data && data.length > 10) {
                const url = window.URL.createObjectURL(new Blob([data]));
                var el = document.createElement('a');
                el.href = url;
                el.setAttribute('download', 'BIDS-TAG-' + name + '.json');
                document.body.appendChild(el);
                el.click();
                el.remove();
            }
        });
    },
    getBidsTags(itemId) {
        SERIES.forEach(name => {
            TrialService.getBidsTag(itemId, name).then(res => {
                this.tags[name] = res.data;
            },
            error => {
                this.tags[name] = {};
                console.error(error);
            });
        });
    },
    getBidsTagsDescription() {
        TrialService.getBidsTagDescription().then(res => {
            this.tagDescription = res.data;
            this.getBidsTags(this.itemId);
        }, 
        error=>console.error(error));
    }
  },
  mounted() {    
    if (this.itemId) {
        this.getBidsTagsDescription();
        this.getImagingData(this.itemId);
    }
  },
}
</script>

<style scoped>
input[type="text"]:disabled{
   background:  lightgray;
   cursor: no-drop;
}
input[type="button"]:disabled {
   cursor: no-drop;
}
button:disabled {
   cursor: no-drop;
}
.btn.btn_down {
    padding-left: 18px;
    padding-right: 30px;
    width: 120px;
    /* height: 28px; */
    border-color: #5575bc;
    color: #264164;
}
.tb_body td {
    word-break: break-all;
}
</style>