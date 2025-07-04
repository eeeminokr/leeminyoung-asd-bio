<template>
  <div id="ContentsDiv">
    <div class="cont_top pt_12">
      
      <trial-data-tab :menuId="MENU_ID" :projectId="projectId"></trial-data-tab>

      <div class="inputbox mt_18">
          <ul class="list_input">
            <li>
                <label for="resh_id">연구번호ID</label>
                <input type="text" class="w_165" v-model="filters.subjectId">
            </li>
            <li>
                <label for="task_name">대상군</label>
                <select class="w_197" v-model="filters.projectId">
                    <option v-for="op in projects" :key="op.projectSeq" :value="op.projectSeq">{{op.projectName}}</option>
                </select>
            </li>
            <li>
                <label for="part_oz">참여기관</label>
                <select class="w_206" v-model="filters.orgId">
                    <option value=''>::전체::</option>
                    <option v-for="op in organizations" :key="op.orgId" :value="op.orgId">{{op.orgName}}</option>
                </select>
            </li>
              <li class="btns fr">
                  <template v-if="canQuery">
                    <button class="btn btn_violet" type="button" @click="search">검색</button>
                    <button class="btn btn_violet" type="button">추가검색</button>
                    <button class="btn btn_gray" type="button" @click="reset">검색 초기화</button>
                  </template>
              </li>
        </ul>
        <ul  v-if="activeGenderTemplate" class="list_input" style="position: relative; left: 554px;">
					<li>
						<label for="gender">성별</label>
						<select @change="searchPick($event)" name="" id="gender" style="width: 100px;" v-model="filters.gender" >
						<!-- <option :value="'M'" :key="'M'"> </option>  v-model="filters.gender"-->
                        <option :data-key="'ALL'" value="">::전체::</option>
						<option v-for="g in genders" :value="g.value" :key="g.value" :data-key="g.value">
							{{ g.key }}
						</option>	
						</select>

					</li>
		</ul>
      </div>
      <div class="btn_wrap mt_18">
        <div class="dropdown_btns type">
            <button type="button" class="btn btn_violet" v-if="canDownload">다운로드</button>
            <ul>
                <li @click="downloadSelectedImages"><a>영상파일 다운로드</a></li>
                <li><a href="">화면 다운로드</a></li>
            </ul>
        </div>
        <div class="fr">
              <!-- <button v-if="canDelete && dataDeletable" type="button" class="btn btn_violet" @click="deleteSelectedItems">데이터 삭제</button> -->
        </div>
      </div>
  </div>
  <div class="cont_body mt_18">

    <div class="badge_wrap">
        <span class="badge"><img src="../../assets/images/badge_not.png" alt=""> 대상아님</span>
        <span class="badge"><img src="../../assets/images/badge_unreg.png" alt=""> 미등록</span>
        <span class="badge"><img src="../../assets/images/badge_unveri.png" alt=""> 미검증</span>
        <span class="badge"><img src="../../assets/images/badge_fin.png" alt=""> 완료</span>
    </div>

    <table class="tb_background" data-height="605">
    <tbody><tr><td>
        <div class="scroll_head">
            <table class="tb_head ">
                <colgroup>
                    <col width="38px">
                    <col width="130px">
                    <col width="50px">
                    <col width="70px">
                    <col width="130px">
                    <col width="70px">
                    <col width="50px">
                    <col width="80px">
                    <col width="80px">
                    <col width="80px">
                    <col width="80px">
                    <col width="80px">
                    <col width="80px">
                    <col width="95px">
                    <col width="95px">
                </colgroup>
                <thead>
                    <tr class="th_1">
                        <th rowspan="2" class="sticky" style="left: 0px;"><label><input  type="checkbox" class="check_all" name="table_check"><span></span></label></th>
                        <th rowspan="2" class="sticky" style="left: 43px;">연구번호ID</th>
                        <th rowspan="2" class="sticky" style="left: 191px;">성별</th>
                        <th rowspan="2" class="sticky" style="left: 248px;">연령</th>
                        <th rowspan="2" class="sticky" style="left: 327px;">참여기관</th>
                        <th rowspan="2" class="sticky" style="left: 475px;">대상군</th>
                        <th rowspan="2" class="sticky border_dark" style="left: 554px;">차수</th>
                        <th colspan="6">영상</th>
                        <th colspan="2" class="bg_skyblue">생체표준 정보</th>
                    </tr>
                    <tr class="th_2">
                        <th>T1</th>
                        <th>T2</th>
                        <th>SWI</th>
                        <th>ASL</th>
                        <th>DTI</th>
                        <th>fMRI</th>
                        <th class="bg_skyblue">국제표준<br>(BIDS)</th>
                        <th class="bg_skyblue">국내표준<br>(K-BDS)</th>
                    </tr>
                </thead>
            </table>
        </div>
        <div class="scroll_body ps ps--active-x ps--active-y" style="max-height: 605px;">
            <table class="tb_body">
                <colgroup>
                    <col width="38px">
                    <col width="130px">
                    <col width="50px">
                    <col width="70px">
                    <col width="130px">
                    <col width="70px">
                    <col width="50px">
                    <col width="80px">
                    <col width="80px">
                    <col width="80px">
                    <col width="80px">
                    <col width="80px">
                    <col width="80px">
                    <col width="95px">
                    <col width="95px">
                </colgroup>
                <tbody>
                    <tr v-for="data in projectData" :key="data.projectSubjectId" class="tb_body">
                        <td class="sticky"><label >
                            <!-- <input type="checkbox" name="table_check" v-model="selectedItems" :id="data.id" :value="data"><span></span> -->
                        </label>
                        </td>
                        <td class="sticky">{{data.subjectId}}</td>
                        <td class="sticky">{{data.gender}}</td>
                        <td class="sticky">{{calculateAge(data)}}</td>
                        <td class="sticky">{{data.orgName}}</td>
                        <td class="sticky">{{projectName}}</td>
                        <td class="sticky border_dark">{{data.trialIndex}}</td>
                        
                        <td v-for="item in imagingItems" :key=item>
                            <div v-if="data.imageInfoId">
                                <img :src="getQcIcon(data, item)" alt="" @click="openDetail(data)" style="cursor:pointer">
                            </div>
                            <div v-else>
                                <img :src="getQcIcon(data, item)" alt="" >
                            </div>
                        </td>
                        <td><a @click="openBidsDetail(data.imageInfoId)"><img src="../../assets/images/btn_pop.png" alt="BIDS 팝업"></a></td>
                        <td><a><img src="../../assets/images/btn_pop.png" alt="K-BDS 팝업"></a></td>
                    </tr>
                    <tr v-if="projectData.length <= 0">
                        <td colspan="15">
                            <p>데이터가 존재하지 않습니다</p>
                        </td>              
                    </tr>
                </tbody>
            </table>
        </div>
    </td></tr></tbody>
    </table>
    <list-paging 
                @click="onPagingClick" 
                :paging="paging" 
                :rowsPerPage="filters.offset"></list-paging>
    <transition name="modal">
        <trial-imaging-detail :data="selectedItem" :the-item-id="selectedItemId" :thePermission="thePermission" @closed="onDetailClosed" v-if="openDetailModal"></trial-imaging-detail>
    </transition>
    <transition name="modal">
        <trial-bids-detail :itemId="selectedItemId" v-if="openBidsDetailModal" @closed="onBidsDetailClosed"></trial-bids-detail>
    </transition>
  </div>
</div>
</template>

<script>
import { jqueryUI } from '../../services/jquery-ui';
import { constants, utils, FUNCTION_NAMES } from "../../services/constants";
import AlertService from '../../services/alert.service';
import ProjectService from '../../services/project.service'
import MemberService from "../../services/member.service";
import TrialService from "../../services/trial.service";
import ListPaging from '../../components/ListPaging.vue';
import TrialDataTab from '../../components/TrialDataTab.vue';
import TrialImagingDetail from './ImagingDetail.vue';
import TrialBidsDetail from './BidsDetail.vue';

const ROWS = 20;
const FUNCTION_NAME = FUNCTION_NAMES.IMAGING;

export default {
  name: 'TrialImaging',
  components: {
      TrialDataTab,
      TrialImagingDetail,
      ListPaging,
      TrialBidsDetail
  },
  props: {
  },
  data () {
    return {
      MENU_ID: 'TRIAL',
      projectId: undefined,
      projects: [],
      organizations: [],
      trialItems: [],
      projectData: [],
      filters: {
        projectId: undefined,
        orgId: undefined,
        subjectId: undefined,
        page: 1,
        offset: ROWS,
      },
      subjectCount: 0,
      paging: {totalCount: 0, pageNo: 1},
      selectedItems: [],
      openDetailModal: false,
      openBidsDetailModal: false,
      selectedItemId: undefined,
      selectedItem: undefined,
      thePermission: {
            queryGranted: false,
            createGranted: false,
            deleteGranted: false,
            downloadGranted: false
        },
        imagingItems: ['T1', 'T2', 'SWI', 'ASL', 'DTI', 'fMRI'],
        genders: [
                { value: "M", key: "남자" },
                { value: "F", key: "여자" },
	          ],
		activeGenderTemplate: false,	
        vaildgender : false //page reset을 위한 select options vaildation check 변수 boolean

    }
  },
  computed: {
      /**
     * 조회 권한이 있는가?
     */
    canQuery() { return true; },
    /**
     * 등록 권한이 있는가?
     */
    canCreate() { return this.thePermission.createGranted; },
    /**
     * 삭제 권한이 있는가?
     */
    canDelete() { return this.thePermission.deleteGranted; },			
    /**
     * 다운로드 권한이 있는가?
     */
    canDownload() { return this.thePermission.downloadGranted; },
    // imagingItems() {
    //     const items = [];
    //     (this.trialItems || []).forEach(e => {
    //         if (e.researchAreaId == "IMAGING") items.push(e);
    //     });

    //     return items;
    // },
    dataDeletable() {
        return (this.selectedItems || []).length > 0;
    },
    projectName() { // 대상군은 1개만 선택하여 조회하므로 검색목록의 대상군값은 모두 동일.
        const id = this.projectId;
        const project = (this.projects || []).find (f => f.id == id);
        return !!project ? project.projectName : '';
    }
  },
  watch: {
    'filters.projectId'(newval) {
        const id = this.filters.projectId;
        ProjectService.getAsdOrganizations(id).then(result=>{
            if (result.data.succeeded) {
                this.organizations = result.data.data;
                this.filters.orgId = '';
                this.filters.gender = '';
            }
        });
    }
  },
  methods: {
    formatFloat(val) { 
        return utils.formatFloat(val);
    },
    selectAll(event) {
        if (!this.canDelete) return;

        const checked = event.target.checked;
        if (checked) {
            const items = [];
            this.projectData.forEach(n=>{ items.push(n); });
            this.selectedItems = items;
        }
        else {
            this.selectedItems = [];
        }
    },
    deleteSelectedItems() {
      if (!this.canDelete || this.selectedItems.length <= 0 || !confirm('선택한 데이터를 삭제하겠습니까?')) return;
      const projectId = this.projectId;

      const list = [];
      this.selectedItems.map(val=>{ list.push ({projectId: projectId, trialIndex: val.trialIndex, subjectId: val.subjectId}); });
      this.selectedItems = [];

      TrialService.deleteManyImagingData(list).then(results=>{
        let count = 0;
        results.map(r=>{
          if (r.data.succeeded) count++;
        });

        this.doSearch();
        AlertService.info(`데이터를 삭제했습니다. (${count}/${list.length}건)`);
      }, error=>{
        console.error(error);
      });
    },
    searchPick(event){
                  //this.vaildgender = !!event.target.value;
                    const index = event.target.options.selectedIndex; // select options index 변수에 담음.
                    this.vaildgender = !!event.target.options[index].getAttribute('data-key'); // options data-key로  값의 유무체크로 true /false값 체크하여 변수에 담음.
                    if(this.vaildgender){
                        this.filters.page = 1;
                    }
    },
    searchGender() {
			   this.activeGenderTemplate = true;
               if(this.vaildgender){
                this.searchVideoData();
            }
    },
    reset() {
        this.filters = { 
            projectId: (this.projects || []).length > 0 ? this.projects[0].projectSeq : undefined,
            orgId: undefined,
            subjectId: undefined,
            page: 1,
            offset: ROWS,
        };
        this.projectId = this.filters.projectId;
        this.doSearch();
    },
    search() {
        this.projectId = this.filters.projectId;
        this.projectData = [];
        this.selectedItems = [];
        this.subjectCount = 0;
        this.filters.page = 1;
        this.doSearch();
    },
    doSearch() {
        this.projectId = this.filters.projectId;
        this.projectData = [];
        this.selectedItems = [];
        this.subjectCount = 0;

        MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAME).then(result => {
            if (result.data.succeeded) {
                this.thePermission = result.data.data;
                const canQuery = result.data.data.queryGranted;

                if (!canQuery) {
                    alert ('데이터를 조회할 사용권한이 없습니다.');
                    return;
                }

                this.getTrialItems(this.filters.projectId);

                if (this.filters.page <= 0 || !this.filters.page) this.filters.page = 1;

                TrialService.searchImagingData(this.filters).then(
                    result=>{
                        if (result.data.succeeded) {
                            this.projectData = result.data.data.items;
                            this.paging = {totalCount: result.data.data.total, pageNo: this.filters.page};
                        }
                        else {
                            this.paging = {totalCount: 0, pageNo: this.filters.page};
                        }
                    }, 
                    error=>{
                        console.error(error);
                    })
                    .finally(()=>{
                        jqueryUI.perfectScrollbar.create();
                    });

                TrialService.countSubjectsHavingImagingData(this.filters).then(
                    result=>{
                        if (result.data.succeeded) {
                            this.subjectCount = result.data.data;
                        }
                        else {
                            this.subjectCount = 0;
                        }
                    }, 
                    error=>{
                        console.error(error);
                    });
            }
        },
        error => {
            console.error(error);
        });
    },
    getProjects(successCallback) {
        if (!this.canQuery) return;

        ProjectService.getProjects().then(
            (result) => {
                if (result.data.succeeded) {
                    this.projects = result.data.data;
                    this.filters.projectId = (this.projects || []).length > 0 ? this.projects[3].projectSeq : undefined;

                    if (successCallback) {
                        successCallback();
                    }
                }
            },
            (error) => {
                console.error(error);
            }
        );
    },
    downloadSelectedImages() {
        const idx = [];
        (this.selectedItems || []).forEach(el=>{ idx.push(el.id); });
        if (!!!idx || idx.length <= 0) {
            alert('다운로드 할 영상을 선택하세요.');
            return;
        }

        TrialService.downloadImagingData(idx).then(response=>{
            const url = window.URL.createObjectURL(new Blob([response.data]));
            var el = document.createElement('a');
            el.href = url;
            el.setAttribute('download', 'SEOUL_ASD-영상파일.zip');
            document.body.appendChild(el);
            el.click();
            el.remove();
        }, 
        error=>{
            console.error(error);
         });
    },
    getTrialItems() {
        if (!this.canQuery || !this.filters.projectId) return;

        ProjectService.getTrialItems(this.filters.projectId).then(result=>{
            if (result.data.succeeded) {
                this.trialItems = result.data.data;
            }
        }, error=>{
            console.error(error);
        });
    },
    getQcIcon(data, itemName) {
        const status = this.getImagingDataQcStatus(data, itemName.toUpperCase());
        const icon = constants.QC_ICONS[status];

        return icon;
    },
    getImagingDataQcStatus(data, itemName) {
        const status = data.qcStatus[itemName.toUpperCase()];

        return status ? status : "NO_DATA";
    },
    calculateAge(data) {
        if (!data.birthday || !data.studyDate) return "";

        const age = parseInt((new Date(data.studyDate) - new Date(data.birthday)) / (1000 * 60 * 60 * 24 * 365));

        return age;
    },
    openBidsDetail(itemid) {
        this.selectedItemId = itemid;
        this.openDetailModal = false;
        this.openBidsDetailModal = true;
    },
    onBidsDetailClosed() {
        this.selectedItemId = undefined;
        this.openDetailModal = false;
        this.openBidsDetailModal = false;
    },
    onDetailClosed() {
        this.selectedItemId = undefined;
        this.selectedItem = undefined;
        this.openDetailModal = false;
    },
    openDetail(item) {
        if (!this.canQuery) return;

        this.openDetailModal = true;
        this.selectedItemId = item.imageInfoId;
        this.selectedItem = item;
        this.selectedItem.projectName = this.projectName;
    },
    onPagingClick(page) {
        if (!this.canQuery) return;
        
        this.filters.page = page;
        this.doSearch();
    },
  },
  mounted() {
  },
  created() {
    window.addEventListener("resize", function() {jqueryUI.perfectScrollbar.create();});

    this.filters.page = 1;
    this.getProjects(()=>this.search());
  }
}
</script>

<style scoped>
</style>