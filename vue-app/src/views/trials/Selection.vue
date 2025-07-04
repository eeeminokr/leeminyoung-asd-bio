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
                  <button class="btn btn_violet" type="button">추가검색</button>
                  <button class="btn btn_gray" type="button" @click="reset">검색 초기화</button>
              </li>
        </ul>
      </div>
      <div class="btn_wrap mt_18">
        <div class="dropdown_btns type" v-if="canDownloadImaging">
            <div v-if="canDownloadImaging">

                    <button type="button" class="btn btn_violet"  @click="dataSetSurveyDownload">데이터추출 다운로드</button>
                </div>
            </div> 
        <div class="fr">
            <!-- <button v-if="canCreateImaging && !dataDeletable" type="button" class="btn btn_violet btn_pop_data_reg" @click="openDataUploadModal">데이터 등록</button> -->
            <!-- <button v-if="canDeleteImaging && dataDeletable" type="button" class="btn btn_violet" @click="deleteTrialData">데이터 삭제</button> -->
        </div>
      </div>
  </div>
  <div class="cont_body mt_18">

    <div class="badge_wrap">
        <span class="badge"><img src="../../assets/images/badge_not.png" alt=""> 대상아님</span>
        <span class="badge"><img src="../../assets/images/badge_unreg.png" alt=""> 미등록</span>
        <!-- <span class="badge"><img src="../assets/images/badge_unveri.png" alt=""> 미검증</span> -->
        <span class="badge"><img src="../../assets/images/badge_fin.png" alt=""> 완료</span>
    </div>
    <table class="tb_background" data-height="605">
    <tbody><tr><td>
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
              <col width="90px" />
              <col width="90px" />
              <col width="90px" /> <!--생체신호-->
              <col width="90px" />
              <col width="90px" />
              <col width="90px" /><!-- 소변 -->
              <col width="90px" />
              <col width="90px" />
              <col width="90px" />
               <!--아래부터 기타평가-->
              <col width="90px" />
              <col width="90px" />
              <col width="90px" />
              <col width="90px" />
              
              
            </colgroup>
            <thead>
              <tr>
                  <th class="sticky" rowspan="2">
                    <label>
                      <!-- <input type="checkbox" class="check_all" name="table_check" @click="selectAll" /> -->
                      <span></span></label>
                  </th>
                  <th rowspan="2">연구번호ID</th>
                  <th rowspan="2">성별</th>
                  <th rowspan="2">연령</th>
                  <th rowspan="2">참여기관</th>
                  <th rowspan="2">대상군</th>
                  <th rowspan="2" class="border_dark">차수</th>
                  <th colspan="13">자폐평가</th>
                  <th colspan="4" class="border_dark">그외평가</th>
              </tr>
              <tr>
                  
                  <th>K-DST</th>
                  <th>M-CHAT</th>
                  <th>Q-CHAT</th>
                  <th>BeDevel-Q</th>
                  <th>BeDevel-I</th>
                  <th>BeDevel-P</th>
                  <th>K-Vineland</th>
                  <th>SCQ(lifetime)</th>
                  <th>SRS-2</th>
                  <th>CARS</th>
                  <th>ADOS</th>        
                  <!-- <th>ADOS-2(Mod T)</th>
                  <th>ADOS-2(Mod 1)</th>
                  <th>ADOS-2(Mod 2)</th> -->
                  <th>SELSI/PRES</th>      
                  <th>CBCL</th>
                  <!--아래부터 기타평가-->
                  <th>ADI-R</th>
                  <th>Bayley</th>
                  <th>WPPSI</th>
                  <th>SCQ(current)</th>  
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
              <col width="90px" /> <!-- 선별 자폐평가 -->
              <col width="90px" />
              <col width="90px" /> <!-- M-chat -->
              <col width="90px" />
              <col width="90px" />
              <col width="90px" />
              <col width="90px" />
              <col width="90px" /> <!--생체신호-->
              <col width="90px" />
              <col width="90px" />
              <col width="90px" /><!-- 소변 -->
              <col width="90px" />
              <col width="90px" />
              <col width="90px" /> <!--그외 평가-->
              <col width="90px" />
              <col width="90px" />
              <col width="90px" />
    
            </colgroup>
            <tbody>
                <tr v-for="item in trialData" :key="item.imageInfoId">
                    <td class="sticky">
                      <label >
                        <!-- <input type="checkbox" name="table_check" v-model="selectedItems" :id="item.id" :value="item" /> -->
                        <span></span></label>
                    </td>
                    
                    <td class="">{{item.subjectId}}</td>
                    <td>{{item.gender == 'F' ? '여' : '남'}}</td>
                    <td>{{item.month}} 개월</td> 
                    <td>{{item.orgName}}</td>
                    <td>{{item.projectName}}</td>
                    <td class="border_dark">{{item.trialIndex}}</td>
                    <!-- <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('IMAGING', item)}`"></td> -->
  
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('kdst', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('kmchat', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('kqchat', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('bedevelq', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('bedeveli', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('bedevelp', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('kvineland', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('scqlifetime', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('srs2', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('kcars2', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('ados', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('selsi/pres', item)}`"></td>   
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('cbcl', item)}`"></td>

                    <!--기타평가-->
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('adir', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('kbayley', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('kwppsi', item)}`"></td>
                    <td><img v-if="canQueryImaging" :src="`${getQcStatusIcon('scqcurrent', item)}`"></td>            
                </tr>
                <tr v-if="trialData.length <= 0">
                  <td colspan="23">
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
    <imaging-data-upload-modal 
      :projectId="filters.projectId" 
      :thePermissionImaging="thePermissionImaging"
      @closed="onCloseDataUploadModal" 
      v-if="showUploadModal"></imaging-data-upload-modal>
  </transition>
  <transition name="modal">
      <!-- Progress Modal -->
      <imaging-data-progress-modal 
        @fileUploaded="onCloseDataProgressModal"         
        :oneFileForm="uploadFileForm" 
        :manyFileForms="uploadFileForms"
        v-if="showProgressModal"></imaging-data-progress-modal>
    </transition>    
    <vue-element-loading 
			:active="working" 
			spinner="bar-fade-scale"
			color="#FF6700"
			:text="workingMessage" 
			is-full-screen />    
</div>
</template>

<script>
import axios from 'axios';
import VueElementLoading from "vue-element-loading";
import { jqueryUI } from '../../services/jquery-ui';
import { constants, FUNCTION_NAMES, utils } from '../../services/constants';
import AlertService from '../../services/alert.service';
import ProjectService from '../../services/project.service';
import SubjectService from '../../services/subject.service';
import OrganizationService from "../../services/organization.service";
import MemberService from "../../services/member.service";
import TrialService from "../../services/trial.service";
import ImagingDataUploadModal from '../../components/ImagingDataUploadModal.vue';
import ImagingDataProgressModal from "../../components/ImagingDataProgressModal.vue";
import TrialDataTab from '../../components/TrialDataTab.vue';
import ListPaging from '../../components/ListPaging.vue';
import trialService from '../../services/trial.service';



const ROWS = 20;

export default {
  name: 'TRIAL_DIGITALHEALTH_SELECTION',
  components: {
    VueElementLoading,
      TrialDataTab,
      ImagingDataUploadModal,
      ImagingDataProgressModal,
      ListPaging
  },
  props: {      
  },
  data () {
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
        paging: {totalCount: 0, pageNo: 1},
        templateFileDownUrl: undefined,
        thePermissionImaging: {
					queryGranted: false,
					createGranted: false,
					deleteGranted: false,
					downloadGranted: false
				},
        searchsubjectId: false,
        working: false,
		workingMessage: '다운로드할 파일을 생성 중입니다...',
    }
  },
  watch: {
    'filters.projectId'(newval) {
				const id = this.filters.projectId;
        
        if(id){ // subhjectId 검색 필터의 undfiended 의 check를 위한 id booleanchek
				ProjectService.getAsdOrganizations(id).then(result=>{
					if (result.data.succeeded) {
						this.organizations = result.data.data;
          if(this.filters.subjectId){ // 연구번호ID 검색 필터의 select option 값 업데이트를 위한 로직 추가
            const orgid = result.data.data.map(v => v.orgId);
            // this.filters.orgId = orgid[0]
          }else{
            this.filters.orgId = '';
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
      canQueryImaging() { return this.thePermissionImaging.queryGranted },
			/**
			 * 등록 권한이 있는가?
			 */
			canCreateImaging() { return this.thePermissionImaging.createGranted; },
			/**
			 * 삭제 권한이 있는가?
			 */
			canDeleteImaging() { return this.thePermissionImaging.deleteGranted; },
			/**
			 * 다운로드 권한이 있는가?
			 */
			canDownloadImaging() { return this.thePermissionImaging.downloadGranted; },
      canDownload() { return this.thePermission.downloadGranted; },
			subjectDeletable() {
				return (this.selectedItems || []).length > 0;
			},
			dataDeletable() {
				return (this.selectedItems || []).length > 0;
			}
		},
  methods: {    
    selectAll(event) {
      if (!this.canDeleteImaging) return;

      const checked = event.target.checked;
      if (checked) {
        const items = [];
        this.trialData.forEach(n=>{
          items.push(n);
        });
        this.selectedItems = items;
      }
      else {
        this.selectedItems = [];
      }
    },
    reset() {
        this.filters = { 
            projectId: (this.projects || []).length > 0 ? this.projects[0].projectSeq : undefined,
            orgId: undefined,
            subjectId: undefined,
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

      MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAMES.SELECTION)
      ]).then(results=>{
        if (results[0].data.succeeded) {
          this.thePermissionImaging = results[0].data.data;
        }



        const canQuery = results[0].data.data.queryGranted;

        if (!canQuery) {
            alert ('데이터를 조회할 사용권한이 없습니다.');
            return;
        }

        if (this.filters.page <= 0 || !this.filters.page) this.filters.page = 1;


        if(this.searchsubjectId=== true){
          this.filters.projectId = undefined;
          this.filters.orgId = undefined;
      
        }


        TrialService.searchSelectionData(this.filters).then(
          result=>{

              if (result.data.succeeded) {
                  this.trialData = result.data.data.items;
                  this.getTrials(this.projectId);
                  this.getSubjects(this.projectId); 
                  this.paging = {totalCount: result.data.data.total, pageNo: this.filters.page};

                  const seq = result.data.data.items.map(v => v.projectSeq); //연구id 값 서칭 후 select option 값 지정
                  const org = result.data.data.items.map(v => v.orgId);
  

                  if(this.searchsubjectId){
                    this.filters.projectId = seq[0];
                  this.filters.orgId = org[0];
                  this.searchsubjectId = false;
                  }
                
                  
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
      });
    },
    getProjects(successCallback) {
      if (!this.canQuery) return;

        ProjectService.getProjects().then(
        (result) => {
            if (result.data.succeeded) {
                this.projects = result.data.data;

                // this.filters.projectId = (this.projects || []).length > 0 ? this.projects[0].projectSeq : undefined;
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
            result.data.data.forEach(e=>{
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
            result=>{
                if (result.data.succeeded) {
                    this.subjects = result.data.data;
                }
            }, 
            error=>{
                console.error(error);
        });
    },
    deleteTrialData() {
      if (this.selectedItems.length <= 0 || !confirm('선택한 데이터를 삭제하겠습니까?')) return;
      const projectId = this.projectId;

      const list = [];
      this.selectedItems.map(val=>{ list.push ({projectId: projectId, trialIndex: val.trialIndex, subjectId: val.subjectId}); });
      this.selectedItems = [];

      TrialService.deleteTrialDatas(list).then(results=>{
        let count = 0;
        results.map(r=>{
          if (r.data.succeeded) count++;
        });

        this.search();
        AlertService.info(`데이터를 삭제했습니다. (${count}/${list.length}건)`);
      }, error=>{
        console.error(error);
      });
    },
    openDataUploadModal() {
      if (!this.canCreateImaging) return;

        this.showUploadModal = true;
        this.showProgressModal = false;
        this.uploadFileForm = undefined;
        this.uploadFileForms = [];
    },
    onCloseDataUploadModal(result) {      
      this.showUploadModal = false; // Close data upload modal

      if (result) {        
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
      if (!this.canCreateImaging) return;

        this.showProgressModal = true;
        this.showUploadModal = false;        
    },
    onCloseDataProgressModal(result) {
      if (result === true) {
        this.showProgressModal = false; // Close data progress modal        
      }

      this.uploadFileForm = undefined;
      this.uploadFileForms = [];        
      this.search();
    },
    onPagingClick(page) {
      this.filters.page = page;
      this.search();
    },
    getQcStatusIcon(area, data) {
        if (data) {
            let status;
            if(area == 'ados'){
              const ados = ['ados2mt', 'ados2m1', 'ados2m2','ados2m3'];
              // status = ados.every(key => data[key] === 'N/A')
              //   ? 'N/A'
              //   : ados.every(key => data[key] === 'Y')
              //   ? 'Y'
              //   : 'N';
              status = ados.some(key => data[key] === 'Y') ? 'Y' :
              ados.every(key => data[key] === 'N/A') ? 'N/A' : 'N';
            }else if(area == 'selsi/pres'){
              const sel_pres = ['selsi', 'pres']
              status = sel_pres.some(key => data[key] === 'Y') ? 'Y' :
              sel_pres.every(key => data[key] === 'N/A') ? 'N/A' : 'N';
              // status = sel_pres.every(key => data[key] === 'N/A')
              //   ? 'N/A'
              //   : sel_pres.every(key => data[key] === 'Y')
              //   ? 'Y'
              //   : 'N';
            }else{
              status = data[area];
            }

            //if "N/A" Data ICON -> SUBJECT_NO[대상아님]
            //else if "N" Data ICON ->  
            return status == "N/A" ? 
                    constants.QC_ICONS.NO_SUBJECT :
                    status == "N" ?
                    constants.QC_ICONS.NO_DATA :
                    status == "Y" ?
                    constants.QC_ICONS.DONE:
                    constants.QC_ICONS.NO_DATA;
        }
        
        return constants.QC_ICONS.NO_SUBJECT;
    },
    getMonthsBetween(birthDay, referenceDate){
      return utils.getMonthsBetween(birthDay, referenceDate);  
    },
    resetTrialData() {

      let subjectIdvaild = true;
      subjectIdvaild = utils.validateSubjectIdFormat(this.filters.subjectId)

      if(this.filters.subjectId){

        if(subjectIdvaild===false){
          AlertService.info("연구번호ID를 정확하게 기입하여 주십시오.")
        // return false;
        }else{
          this.searchsubjectId = true;
          this.search();
        }
      }else{
        this.getProjects(()=>this.search());
      }    
    },
    changeStatus(item, type){
      
      const id = item.subjectId;
      const projectId = item.projectSeq;
      const trialIndex = item.trialIndex;
      
      TrialService.changeDataSummaryValue(id, projectId, trialIndex, type).then((result)=>{
        
        // 성공했다면 QC 이미지 변경처리 할 것 
        return item.areaQcStatus[type] = "DONE";  
        
      }, (error)=>{
        console.error(error);
        // 실패 시 
        alert("해당 항목의 상태 변경에 실패하였습니다.")
      });
     
    },
    dataSetSurveyDownload() {
    // Other logic here
    if(!this.canDownloadImaging) return;
    
      this.working = true;
			this.workingMessage = "다운로드 할 파일을 생성 중입니다...";
      AlertService.info(						
						`선택한 데이터 다운로드 중 입니다. 데이터가 많을 경우 처리 시간이 오래 걸릴 수도 있습니다.`, 
						3000,
						'설문지 데이터 추출  정보');
    
            trialService.dataSetSurveyDownload(this.filters)
  .then((response) => {
    // 파일 다운로드를 위한 응답 처리
    if (response.status === 200) {
      // 응답이 성공이면 파일 다운로드 처리
      const contentDisposition = response.headers['content-disposition'];
      const filename = contentDisposition.split('filename=')[1].split(';')[0];
      const blob = new Blob([response.data], { type: response.headers['content-type'] });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); // 사용한 링크 요소 제거
      window.URL.revokeObjectURL(url); // URL 객체 해제
      // 파일 다운로드 성공 알림
      AlertService.info(
        `선택한 데이터 다운로드를 완료 했습니다.`,
        3000,
        '설문지 데이터 추출 정보'
      );
    } else {
      // 파일 다운로드 실패 시 오류 메시지 출력
      AlertService.error('파일을 다운로드하는 동안 오류가 발생했습니다.');
    }
  })
  .catch((error) => {
    // 파일 다운로드 중 오류 처리
    AlertService.error('파일을 다운로드하는 동안 오류가 발생했습니다.');
  })
  .finally(() => {
    // 작업 완료 후 로딩 상태 변경
    this.working = false;
  });
  },
  updateProgressBar(progress) {
    console.log("PROCESS LOADING: ", progress);
    // Update progressBarValue
    this.progressBarValue = progress;
  }

  },
  mounted() {
  },
  created() {
    this.filters.page = 1;
    this.getProjects(()=>this.search());
  }
}
</script>

<style scoped>
/* Styles for the progress bar */
.progress-container {
  width: 100%;
  background-color: #f3f3f3;
  border-radius: 5px;
  margin-top: 20px;
}

.progress-bar {
  height: 20px;
  background-color: #4caf50;
  border-radius: 5px;
  transition: width 0.3s ease;
}
</style>
