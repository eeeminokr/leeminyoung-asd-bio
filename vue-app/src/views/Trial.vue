<template>
  <div id="ContentsDiv">
    <div class="cont_top pt_12">

      <trial-data-tab :menuId="MENU_ID" :projectId="projectId"></trial-data-tab>

      <div class="inputbox mt_18">
          <ul class="list_input">
            <li>
                <label for="resh_id">연구번호ID</label>
                <input @input="resetTrialData" type="text" class="w_165" v-model="filters.subjectId">
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
              <li class="btns fr" v-if="canQuery">
                  <button class="btn btn_violet" type="button" @click="search">검색</button>
                  <button class="btn btn_violet" type="button"  @click="searchGender" >추가검색</button>
                  <button class="btn btn_gray" type="button" @click="reset">검색 초기화</button>
              </li>
        </ul>
        <ul  v-if="activeGenderTemplate" class="list_input" style="position: relative; left: 554px;">
					<li>
						<label for="gender">성별</label>
						<select @change="searchPick($event)" name="" id="gender" style="width: 100px;" v-model="filters.gender" >
						<!-- <option :value="'M'" :key="'M'"> </option> -->
            <option :data-key="'ALL'" value="">::전체::</option>
						<option v-for="g in gender" :value="g.value" :key="g.value" :data-key="g.value">
							{{ g.key }}
						</option>
						</select>

					</li>
				 </ul>
      </div>
      <div class="btn_wrap mt_18">
        <div class="fr">
           <button v-if="canCreate && !dataDeletable" type="button" class="btn btn_violet btn_pop_data_reg"
            @click="openDataUploadModal">데이터 등록</button>
          <!-- <button v-if="canDelete && dataDeletable" type="button" class="btn btn_violet" @click="deleteTrialData">데이터
            삭제</button> -->
        </div>
      </div>
    </div>
    <div class="cont_body mt_18">

      <div class="badge_wrap">
        <span class="badge"><img src="../assets/images/badge_not.png" alt=""> 대상아님</span>
        <span class="badge"><img src="../assets/images/badge_unreg.png" alt=""> 미등록</span>
        <!-- <span class="badge"><img src="../assets/images/badge_unveri.png" alt=""> 미검증</span> -->
        <span class="badge"><img src="../assets/images/badge_fin.png" alt=""> 완료</span>
      </div>
      <table class="tb_background" data-height="605">
        <tbody>
          <tr>
            <td>
              <div class="scroll_head">
                <table class="tb_head">
                  <colgroup>
                    <col width="40px" />
                    <col width="150px" />
                    <col width="60px" />
                    <col width="80px" />
                    <col width="150px" /><!--참여기관-->
                    <col width="80px" />
                    <col width="80px" />
                    <col width="90px" /> <!-- 선별 -->
                    <col width="90px" />
                    <col width="90px" /> <!-- M-chat -->
                    <col width="90px" />
                    <col width="120px" /> <!-- 상호작용-->
                    <col width="90px" />
                    <col width="90px" /><!-- 동공측정-->
                    <col width="90px" /><!-- 혈액 -->
                    <col width="90px" />
                    <col width="90px" /><!-- 소변 -->
                    <col width="90px" />
                    <col width="90px" />
                    <col width="90px" />
                 <!--   <col width="90px" /> 장내미생물-->
                    <col width="100px" />
                    <col width="120px" /> <!--DataSet-->
                  </colgroup>
                  <thead>
                    <tr>
                      <th class="sticky" rowspan="2">
                        <label ><span></span></label> 
                          <!-- 
                        <label>
                          <input type="checkbox" class="check_all" name="table_check"
                            @click="selectAll" />
                              <span></span></label>
                            -->
                          
                      </th>
                      <th rowspan="2" class="sticky">연구번호ID</th>
                      <th rowspan="2" class="sticky">성별</th>
                      <th rowspan="2" class="sticky">연령</th>
                      <th rowspan="2" class="sticky">참여기관</th>
                      <th rowspan="2" class="sticky">대상군</th>
                      <th rowspan="2" class="sticky border_dark">차수</th>
                      <th colspan="2">선별/임상</th>
                      <th colspan="5">디지털헬스</th>
                      <th colspan="3">인체유래물</th>
                      <th colspan="3">바이오마커</th>
                      <th rowspan="2"  class="sticky">전체 수집</th>
                      <th rowspan="2"  class="sticky">DataSet</th>
                    </tr>
                    <tr>
                      <th>자폐 평가</th>
                      <th>그외 평가</th>
                      <th>M-CHAT</th>
                      <th>시선추적</th>
                      <th>상호작용영상</th>
                      <!-- <th>생체신호</th> -->
                      <th>음성자료</th>
                      <th>동공측정</th>
                      <th>혈액</th>
                      <th>대변</th>
                      <th>소변</th>
                      <th>fNIRS</th>
                      <th>EEG</th>
                      <th style="border-right:1px solid #c4c9ce">MRI</th>
                      <!-- <th style="border-right:1px solid #c4c9ce">장내미생물</th> -->
                    </tr>
                  </thead>
                </table>
              </div>
              <div class="scroll_body ps ps--active-x ps--active-y" style="max-height: 605px;">
                <table class="tb_body">
                  <colgroup>
                    <col width="40px" />
                    <col width="150px" />
                    <col width="60px" />
                    <col width="80px" />
                    <col width="150px" /><!--참여기관-->
                    <col width="80px" />
                    <col width="80px" />
                    <col width="90px" /> <!-- 선별 -->
                    <col width="90px" />
                    <col width="90px" /> <!-- M-chat -->
                    <col width="90px" />
                    <col width="120px" /> <!-- 상호작용-->
                    <col width="90px" />
                    <col width="90px" /><!-- 동공측정-->
                    <col width="90px" /><!-- 혈액 -->
                    <col width="90px" />
                    <col width="90px" /><!-- 소변 -->
                    <col width="90px" />
                    <col width="90px" />
                    <col width="90px" />
                   <!-- <col width="90px" />장내미생물-->
                    <col width="100px" />
                    <col width="120px" /> <!--DataSet-->
                  </colgroup>
                  <tbody>
                    <tr v-for="item in trialData" :key="item.subjectId">
                      <td class="sticky">
                        <label ><span></span></label>   
                        <!--
                        
                          <input type="checkbox" name="table_check"
                            v-model="selectedItems" :id="item.id" :value="item" /> 
                            -->
                      </td>

                      <td class="sticky">{{ item.subjectId }}</td>
                      <td class="sticky"> {{ item.gender == 'F' ? '여' : '남' }}</td>
                      <td class="sticky">{{ item.month }} 개월</td>
                      <td class="sticky">{{ item.orgName }}</td>
                      <td class="sticky">{{ item.projectName }}</td>
                      <td class="sticky border_dark">{{ item.trialIndex }}</td>
                      <!-- <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('IMAGING', item)}`"></td> -->
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('FIRST', item)}`"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('SECOND', item)}`"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('M_CHAT', item)}`"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('EYE_TRACKING', item)}`"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('VIDEO', item)}`"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('AUDIO', item)}`"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('PUPILLOMETRY', item)}`"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('BLOOD', item)}`"
                          @click="changeStatus(item, 'BLOOD')"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('STOOL', item)}`"
                          @click="changeStatus(item, 'STOOL')"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('URINE', item)}`"
                          @click="changeStatus(item, 'URINE')"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('FNIRS', item)}`"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('EEG', item)}`"></td>
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('MRI', item)}`"></td>
                      <!-- <td><img v-if="canQuery" :src="`${getQcStatusIcon('MICROBIOME', item)}`"></td> -->
                      <td><img v-if="canQuery" :src="`${getQcStatusIcon('ALL', item)}`"></td>
                      <td> <label v-if="canDownload"> <button type="button" class="btn" @click="openDataDownloadModal(item)">다운로드</button></label></td>
                    </tr>
                    <tr v-if="trialData.length <= 0">
                      <td colspan="22">
                        <p>데이터가 존재하지 않습니다</p>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <list-paging @click="onPagingClick" :paging="paging" :rowsPerPage="filters.offset"></list-paging>
    </div>
    <transition name="modal">
      <!-- File Upload Modal -->
      <imaging-data-upload-modal :projectId="filters.projectId" :thePermissionImaging="thePermissionImaging"
        @closed="onCloseDataUploadModal" v-if="showUploadModal"></imaging-data-upload-modal>
    </transition>
    <transition name="modal">
      <!-- Progress Modal -->
      <imaging-data-progress-modal @fileUploaded="onCloseDataProgressModal" :oneFileForm="uploadFileForm"
        :manyFileForms="uploadFileForms" v-if="showProgressModal"></imaging-data-progress-modal>
    </transition>
    <transition name="modal">
      <data-download-modal @closed="onCloseDataDownloadModal" @selected="checkAll" :item="this.item" 
        v-if="showDataDownloadModal"></data-download-modal>
    </transition>
  </div>
</template>

<script>
import axios from 'axios';
import { jqueryUI } from '../services/jquery-ui';
import { constants, FUNCTION_NAMES, utils } from '../services/constants';
import AlertService from '../services/alert.service';
import ProjectService from '../services/project.service';
import SubjectService from '../services/subject.service';
import OrganizationService from "../services/organization.service";
import MemberService from "../services/member.service";
import TrialService from "../services/trial.service";
import ImagingDataUploadModal from '../components/ImagingDataUploadModal.vue';
import ImagingDataProgressModal from "../components/ImagingDataProgressModal.vue";
import TrialDataTab from '../components/TrialDataTab.vue';
import ListPaging from '../components/ListPaging.vue';
import DataDownloadModal from '../components/DataDownloadModal.vue';

const ROWS = 20;

export default {
  name: 'TRIAL_ALL',
  components: {
    TrialDataTab,
    ImagingDataUploadModal,
    ImagingDataProgressModal,
    ListPaging,
    DataDownloadModal

  },
  props: {
  },
  data() {
    return {
        MENU_ID: 'TRIAL',
        pages: [],
        processing: false,
        selectedItems: [],
        projects: [],
        organizations: [],
        subjects: [],
        trialData: [],
        trialIndexx: [],
        item: {},
        projectId: undefined,
        filters: {
            projectId: undefined,
            orgId: undefined,
            subjectId: undefined,
            page: 1,
					  offset: ROWS,
        },
        uploadFileForms:[],
        uploadFileForm: undefined,
        showUploadModal: false,
        showProgressModal: false,
        showDataDownloadModal: false,
        paging: {totalCount: 0, pageNo: 1},
        templateFileDownUrl: undefined,
        thePermissionImaging: {
					queryGranted: false,
					createGranted: false,
					deleteGranted: false,
					downloadGranted: false,
          roleId: undefined,    
				},
        searchsubjectId: false,
        gender: [
                { value: "M", key: "남자" },
                { value: "F", key: "여자" },
	          ],
			 activeGenderTemplate: false,	
       vaildgender : false //page reset을 위한 select options vaildation check 변수 boolean
      }
  },
  watch: {
    'filters.projectId'(newval) {
      const id = this.filters.projectId;

      if (id) { // subhjectId 검색 필터의 undfiended 의 check를 위한 id booleanchek
        ProjectService.getAsdOrganizations(id).then(result => {
          if (result.data.succeeded) {

						this.organizations = result.data.data;
          if(this.filters.subjectId){ // 연구번호ID 검색 필터의 select option 값 업데이트를 위한 로직 추가
            const orgid = result.data.data.map(v => v.orgId);
            // this.filters.orgId = orgid[0]
          }else{
            this.filters.orgId = '';
            this.filters.gender= '';
          }
					
					}
				});
			}
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
    canCreate() { return this.thePermissionImaging.createGranted; },
    /**
    * 삭제 권한이 있는가?
    */
    canDelete() { return this.thePermissionImaging.deleteGranted; },

    /**
     * 삭제 권한이 있는가?
     */
    canDelete() { return this.thePermissionImaging.deleteGranted; },
    /**
     * 다운로드 권한이 있는가?
     */
    canDownload() { return this.thePermissionImaging.downloadGranted; },
    subjectDeletable() {
      return (this.selectedItems || []).length > 0;
    },
    dataDeletable() {
      return (this.selectedItems || []).length > 0;
    },
    canChangePermission() { 
      
      const roleId =  this.thePermissionImaging.roleId;
       if(roleId) {
        return  roleId == "Admin"?
        constants.PERMISSION_ROLEID.ADMIN : 
        roleId == "GeneralManager" ?
        constants.PERMISSION_ROLEID.GENRAL_MANAGER : 
        roleId == "Manager" ? 
        constants.PERMISSION_ROLEID.MANAGER :
        constants.PERMISSION_ROLEID.ELSE;
        // constants.PERMISSION_ROLEID.
        
       }
  
  
      return constants.PERMISSION_ROLEID.ELSE;   
  },
  },
  methods: {
    selectAll(event) {
      if (!this.canDelete) return;

      const checked = event.target.checked;
      if (checked) {
        const items = [];
        this.trialData.forEach(n => {
          items.push(n);
        });
        this.selectedItems = items;
      }
      else {
        this.selectedItems = [];
      }
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
                this.search();
            }
			},
    reset() {
        this.filters = { 
            projectId: (this.projects || []).length > 0 ? this.projects[0].projectSeq : undefined,
            orgId: undefined,
            subjectId: undefined,
            gender: undefined,
            page: 1,
					  offset: ROWS,
            searchsubjectId : false
        };
        this.projectId = this.filters.projectId;
        this.search();
    },
    search() {
      this.trialData = [];
      this.selectedItems = [];
      this.projectId = this.filters.projectId;


      axios.all([
        MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAMES.IMAGING),
        MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAMES.SELECTION),
        MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAMES.DIGITALHEALTH),
        MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAMES.HUMAN_DERIVATIVE),
        MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAMES.BIOMARKER),
        MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAMES.PUPILLOMETRY),


      ]).then(results => {
        if (results[0].data.succeeded) {
          this.thePermissionImaging = results[0].data.data;
        }





        const canQuery = results[0].data.data.queryGranted;

        if (!canQuery) {
          alert('데이터를 조회할 사용권한이 없습니다.');
          return;
        }

        if (this.filters.page <= 0 || !this.filters.page) this.filters.page = 1;




        if (this.searchsubjectId === true) {
          this.filters.projectId = undefined;
          this.filters.orgId = undefined;

        }


        TrialService.searchAllData(this.filters).then(
          result => {

            if (result.data.succeeded) {

              this.trialData = result.data.data.items;
              this.getTrials(this.projectId);
              this.getSubjects(this.projectId);
              this.paging = { totalCount: result.data.data.total, pageNo: this.filters.page };

              const seq = result.data.data.items.map(v => v.projectSeq); //연구id 값 서칭 후 select option 값 지정
              const org = result.data.data.items.map(v => v.orgId);


              if (this.searchsubjectId) {
                this.filters.projectId = seq[0];
                this.filters.orgId = org[0];
                this.searchsubjectId = false;
              }


            }
            else {
              this.paging = { totalCount: 0, pageNo: this.filters.page };
            }
          },
          error => {
            console.error(error);
          })
          .finally(() => {
            jqueryUI.perfectScrollbar.create();
          });
      });
    },
    getProjects(successCallback) {
      if (!this.canQuery) return;

      ProjectService.getProjects().then(
        (result) => {
          if (result.data.succeeded) {
            this.projects = result.data.data;

            // this.filters.projectId = (this.projects || []).length > 0 ? this.projects[0].projectSeq : undefined;
            // this.filters.projectId = (this.projects || []).length > 0 ? this.projects[3].projectSeq : undefined;
		
						// 	if(this.canQuery){
						// 		this.filters.projectId = this.projects.length > 0 ? "" : undefined;
						// 	}else{
						// this.filters.projectId = this.projects.length < 0 ? "" : this.projects[0].projectSeq;
						// 	}
						// this.filters.projectId = this.projects.length > 0 ? "" : undefined;
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
    getTrials(id) {
      if (!this.canQuery) return;

      ProjectService.getTrials(id).then(
        (result) => {
          if (result.data.succeeded) {
            const list = [];
            result.data.data.forEach(e => {
              list.push(e.trialIndex);
            });
            this.trialIndexx = list;
          }
        },
        (error) => {
          console.error(error);
        }
      );
    },
    getSubjects(id) {
      if (!this.canQuery) return;

      const filter = { projectId: id, subjectId: this.filters.subjectId, orgId: this.filters.orgId };

      SubjectService.getAllSubjects(filter).then(
        result => {
          if (result.data.succeeded) {
            this.subjects = result.data.data;
          }
        },
        error => {
          console.error(error);
        });
    },
    deleteTrialData() {
      if (this.selectedItems.length <= 0 || !confirm('선택한 데이터를 삭제하겠습니까?')) return;
      const projectId = this.projectId;

      const list = [];
      this.selectedItems.map(val => { list.push({ projectId: projectId, trialIndex: val.trialIndex, subjectId: val.subjectId }); });
      this.selectedItems = [];

      TrialService.deleteTrialDatas(list).then(results => {
        let count = 0;
        results.map(r => {
          if (r.data.succeeded) count++;
        });

        this.search();
        AlertService.info(`데이터를 삭제했습니다. (${count}/${list.length}건)`);
      }, error => {
        console.error(error);
      });
    },
    openDataUploadModal() {
      if (!this.canCreate) return;

      this.showUploadModal = true;
      this.showProgressModal = false;
      this.uploadFileForm = undefined;
      this.uploadFileForms = [];
    },
    onCloseDataUploadModal(result,form) {
      console.log("closed[data] :" +result)
      console.log("closed[forms] :" +form)
      this.showUploadModal = false; // Close data upload modal
      console.log(result)
      if (result) {
        console.log("resultForm : "+ result.uploadFileForm)
        


        this.uploadFileForm = result.uploadFileForm;
        this.uploadFileForms = result.uploadFileForms;

        if (this.uploadFileForm || this.uploadFileForms) { // Open progress modal when uploading files.
          this.openDataProgressModal();
        }
      }
      else { // result is false when user clicks 'close' button without uploading files.
        this.showProgressModal = false;
      }
    },
    openDataProgressModal() {
      if (!this.canCreate) return;

      this.showProgressModal = true;
      this.showUploadModal = false;
    },
    onCloseDataProgressModal(result,form) {
      console.log("closed[prodata] :" +result)
      console.log("closed[proforms] :" +form)
      if (result === true) {
        this.showProgressModal = false; // Close data progress modal        
      }
      if(form != null){
        this.filters.subjectId = form;
      }
      this.uploadFileForm = undefined;
      this.uploadFileForms = [];
      
      this.search();
    },
    openDataDownloadModal(data) {
      this.item = data;
      this.showDataDownloadModal = true;
    },
    onCloseDataDownloadModal(data) {
      console.log("closed[data] :" +data)
      this.showDataDownloadModal = false;
    },
    checkAll() {

      $('.check_li').each(function () {
        var $el = $(this);
        var $checkAll = $el.find('.checkAll');
        $checkAll.change(function () {
          var $this = $(this);
          var checked = $this.prop('checked');
          $el.find('input[name="data_check"]').prop('checked', checked);
        });

        var boxes = $el.find('input[name="data_check"]');
        boxes.change(function () {
          var boxLength = boxes.length;
          var checkedLength = $el.find('input[name="data_check"]:checked').length;
          var selectAll = (boxLength == checkedLength);
          $checkAll.prop('checked', selectAll);
        });
      });
    },
    onPagingClick(page) {
      this.filters.page = page;
      this.search();
    },
    getQcStatusIcon(area, data) {
      if (data) {

        const status = data.areaQcStatus[area];
        return status == "NO_SUBJECT" ?
          constants.QC_ICONS.NO_SUBJECT :
          status == "NO_DATA" ?
            constants.QC_ICONS.NO_DATA :
            status == "NO_QC" ?
              constants.QC_ICONS.NO_QC :
              status == "DONE_QC" ?
                constants.QC_ICONS.DONE_QC :
                status == "DONE" ?
                  constants.QC_ICONS.DONE :
                  constants.QC_ICONS.NO_SUBJECT;
      }

      return constants.QC_ICONS.NO_SUBJECT;
    },
    getMonthsBetween(birthDay, referenceDate) {
      return utils.getMonthsBetween(birthDay, referenceDate);
    },
    resetTrialData() {

      let subjectIdvaild = true;
      subjectIdvaild = utils.validateSubjectIdFormat(this.filters.subjectId)

      if (this.filters.subjectId) {

        if (subjectIdvaild === false) {
          AlertService.info("연구번호ID를 정확하게 기입하여 주십시오.")
          // return false;
        } else {
          this.searchsubjectId = true;
          this.search();
        }
      } else {
        this.getProjects(() => this.search());
      }
    },
    async changeStatus(item, type) {

      if(!this.canChangePermission) return;
      const id = item.subjectId;
      const projectId = item.projectSeq;
      const trialIndex = item.trialIndex;
      const areaQcStatus = item.areaQcStatus[type];
      
      if (areaQcStatus !== 'NO_SUBJECT') {
        try {
          if (areaQcStatus === 'NO_DATA') {
            await TrialService.changeDataSummaryValue(id, projectId, trialIndex, type, true);
            // 성공했다면 QC 이미지 변경처리 할 것
            item.areaQcStatus[type] = "DONE_QC";
          } else if (areaQcStatus === 'DONE_QC') {
            await TrialService.changeDataSummaryValue(id, projectId, trialIndex, type, false);
            // 성공했다면 QC 이미지 변경처리 할 것
            item.areaQcStatus[type] = "NO_DATA";
          }
        } catch (error) {
          console.error(error);
          // 실패 시
          AlertService.error("해당 항목의 상태 변경에 실패하였습니다.");
        }
      }
    }

  },
  mounted() {
    // 팝업 : 데이터다운로드 : 체크박스 All

  },
  created() {
    this.filters.page = 1;
    this.getProjects(() => this.search());


  }
}
</script>

<style scoped>
.pop_data_download,
.pop_recaptue {
  width: 516px;
}

.pop_data_download .btn_wrap,
.pop_recaptue .btn_wrap {
  margin: 18px 0 0;
}

.pop_data_download .btn.btn_arrow_none {
  padding: 0 10px;
}

.check_ul {
  margin: 0 20px;
  border-top: 2px solid #3a75b1;
  background-color: #f2f3f4;
}

.check_ul li {
  overflow: hidden;
  padding: 6px 0;
  border-bottom: 1px solid #c4c9ce;
}

.check_ul li .check_all,
.check_ul li .check_list {
  float: left;
}

.check_ul li .check_all {
  padding-left: 34px;
  width: 80px;
}

.check_ul li .check_all label {
  display: inline-block;
  margin: 5px 0 4px;
}

.check_ul li .check_all input[type="checkbox"]+span {
  font-size: 16px;
  font-weight: 700;
  color: #5575bc;
}

.check_ul li .check_all input[type="checkbox"]+span:before {
  margin-right: 12px;
}

.check_ul li .check_list {
  width: calc(100% - 114px);
}

.check_ul li .check_list label {
  float: left;
  display: inline-block;
  margin: 5px 18px 4px 0;
  min-width: 72px;
}

.check_ul li .check_list label:last-child {
  margin-right: 0;
}

.check_ul li .check_list .width_s {
  min-width: 0;
  width: 48px;
}

.check_ul li .check_list input[type="checkbox"]+span:before {
  margin-right: 8px;
}
</style>
