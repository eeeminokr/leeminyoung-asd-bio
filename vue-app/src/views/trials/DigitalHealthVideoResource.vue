<template>
    <div id="ContentsDiv">
        <div class="cont_top pt_12">
            <trial-data-tab :menuId="MENU_ID" :subMenuId="SUBMENU_ID" :projectId="projectId"></trial-data-tab>

            <div class="inputbox mt_18">
                <ul class="list_input">
                    <li>
                        <label for="resh_id">연구번호ID</label>
                        <input @input="resetTrialData" type="text" class="w_165" v-model="filters.subjectId">
                    </li>
                    <li>
                        <label for="task_name">대상군</label>
                        <select class="w_197" v-model="filters.projectId">
                            <option v-for="op in projects" :key="op.projectSeq" :value="op.projectSeq">{{ op.projectName }}
                            </option>
                        </select>
                    </li>
                    <li>
                        <label for="part_oz">참여기관</label>
                        <select class="w_206" v-model="filters.orgId">
                            <option value=''>::전체::</option>
                            <option v-for="op in organizations" :key="op.orgId" :value="op.orgId">{{ op.orgName }}</option>
                        </select>
                    </li>
                    <li class="btns fr">
                        <template v-if="canQuery">
                            <button class="btn btn_violet" type="button" @click="searchVideoData">검색</button>
                            <button class="btn btn_violet" type="button" @click="searchGender">추가검색</button>
                            <button class="btn btn_gray" type="button" @click="reset">검색 초기화</button>
                        </template>
                    </li>
                </ul>
                <ul  v-if="activeGenderTemplate" class="list_input" style="position: relative; left: 554px;">
					<li>
						<label for="gender">성별</label>
						<select @change="searchPick($event)" name="" id="gender" style="width: 100px;" v-model="filters.gender" >
						<!-- <option :value="'M'" :key="'M'"> </option>  v-model="filters.gender"-->
						<option :data-key="'ALL'" value=''>::전체::</option>
						<option v-for="g in genders" :value="g.value" :key="g.value" :data-key="g.value">
							{{ g.key }}
						</option>		
						</select>

					</li>
			</ul>
            </div>
            <div class="btn_wrap mt_18">
                <!-- <div class="dropdown_btns type" v-if="canDownload">
                    <button type="button" class="btn btn_violet">다운로드</button>
                    <ul>
                        <li><a href="">영상파일 다운로드</a></li>
                        <li><a href="">화면 다운로드</a></li>
                    </ul>
                </div> -->
                <div class="fr">
                    <button v-if="canDelete && dataDeletable" type="button" class="btn btn_violet"
                        @click="deleteSelectedItems">데이터 삭제</button>
                </div>
            </div>
        </div>
        <div class="cont_body mt_11">
            <div class="badge_wrap">
                <span class="badge"><img src="../../assets/images/badge_not.png" alt=""> 대상아님</span>
                <span class="badge"><img src="../../assets/images/badge_unreg.png" alt=""> 미등록</span>
                <!-- <span class="badge"><img src="../assets/images/badge_unveri.png" alt=""> 미검증</span> -->
                <!-- <span class="badge"><img src="../../assets/images/badge_fin.png" alt=""> 완료</span> -->
            </div>
            <table class="tb_background" data-height="605">
                <tbody>
                    <tr>
                        <td>
                            <div class="scroll_head">
                                <table class="tb_head">
                                    <colgroup>
                                        <col width="40px"><!-- Checkbox -->
                                        <col width="150px"><!-- 연구번호ID -->
                                        <col width="50px"><!-- 성별 -->
                                        <col width="60px"><!-- 연령 -->
                                        <col width="130px"><!-- 참여기관 -->
                                        <col width="130px"><!-- 검사수행일-->
                                        <col width="70px"><!-- 대상군 -->
                                        <col width="50px"><!-- 차수 -->
                                        <col width="130px" v-for="n in columnCount['TOTAL']" :key="n">
                                    </colgroup>
                                    <thead>
                                        <tr>
                                            <th rowspan="2" class="sticky">
                                                <label>
                                                    <!-- <input type="checkbox"
                                                        class="check_all" name="table_check"
                                                        @click="selectAll"> -->
                                                        <span></span></label></th>
                                            <th rowspan="2" class="sticky">연구번호 ID</th>
                                            <th rowspan="2" class="sticky">성별</th>
                                            <th rowspan="2" class="sticky">연령</th>
                                            <th rowspan="2" class="sticky">참여기관</th>
                                            <th rowspan="2" class="sticky">검사수행일</th>
                                            <th rowspan="2" class="sticky">대상군</th>
                                            <th rowspan="2" class="sticky border_dark">차수</th>
                                            <th :colspan="columnCount['TOTAL']">상호작용영상과제</th>
                                        </tr>
                                        <tr>
                                            <th>과제1</th>
                                            <th>과제2</th>
                                            <th>과제3</th>
                                            <th>과제4</th>
                                            <th>과제5</th>
                                            <th>과제6</th>
                                            <th>재촬영</th>
                                            <th>다운로드</th>
                                        </tr>
                                    </thead>
                                </table>
                            </div>
                            <div class="scroll_body ps ps--active-x ps--active-y" style="max-height: 605px;">
                                <table class="tb_body">
                                    <colgroup>
                                        <col width="40px"><!-- Checkbox -->
                                        <col width="150px"><!-- 연구번호ID -->
                                        <col width="50px"><!-- 성별 -->
                                        <col width="60px"><!-- 연령 -->
                                        <col width="130px"><!-- 참여기관 -->
                                        <col width="130px"><!-- 검사수행일-->
                                        <col width="70px"><!-- 대상군 -->
                                        <col width="50px"><!-- 차수 -->

                                        <col width="130px" v-for="n in columnCount['TOTAL']" :key="n">
                                    </colgroup>
                                    <tbody>
                                        <tr v-for="(item, index) in subjects" :key="item.subjectId">
                                            <!-- <tr v-for="data in item.mcdData" :key="data['_ID']"> -->
                                            <td class="sticky">
                                                <label>
                                                    <!-- <input type="checkbox" name="table_check"
                                                        v-model="selectedItems" :id="item['subjectId']"
                                                        :value="item" /> -->
                                                        <span></span></label>
                                            </td>
                                            <td class="sticky">{{ item.subjectId }}</td>
                                            <td class="sticky">{{ gender(item.gender) }}</td>
                                            <td class="sticky">{{ item.birthDay }}개월</td>
                                            <td class="sticky">{{ item.orgName }}</td>
                                            <td class="sticky">{{ formatDate(item.registDate) }}</td>
                                            <td class="sticky">{{ item.projectName }}</td>
                                            <td class="sticky border_dark">{{ item.trialIndex }}</td>

                                            <td  style="-webkit-text-stroke: 1px #4855C0;" v-for="(key, col, index)  in item['areaQcStatus']" :key="col" >
                                                <template v-if="key != 'NO_DATA' && key != 'NO_SUBJECT'" >
                                                    {{ key }}
                                                </template>
                                                <img v-if="canQueryImaging && key == 'NO_DATA' || key == 'NO_SUBJECT'"
                                                    :src="`${getQcStatusIcon(col, key)}`">
                                            </td>
                                           
                                            <td>
                                                <button type="button" class="btn" :class="{ 'red-button': isNoDataOrNoSubject(item['areaQcStatus'])}"  @click="openVideoRetryModal(item)">재촬영</button>
                                            </td>
                                            <td><button  type="button" class="btn" @click="download(item)">다운로드</button></td>
                                            <!-- </tr> -->
                                        </tr>
                                        <template v-if="subjects.length <= 0">
                                            <tr>
                                                <td colspan="16">
                                                    <p>데이터가 존재하지 않습니다</p>
                                                </td>
                                            </tr>
                                        </template>
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
            <video-retry-modal
            @closed="onCloseVideoRetryModal" @check="onCheckModal" v-if="showVideoRetryModal" :item=this.item></video-retry-modal>
        </transition>
        
    </div>
</template>
    
<script>
import { FUNCTION_NAMES, constants, utils } from '../../services/constants';
import { jqueryUI } from '../../services/jquery-ui';
import AuthService from '../../services/auth.service';
import AlertService from '../../services/alert.service';
import MemberService from "../../services/member.service";
import ProjectService from "../../services/project.service";
import TrialService from '../../services/trial.service';
import TrialDataTab from '../../components/TrialDataTab.vue';
import ListPagingWithPageCount from '../../components/ListPagingWithPageCount.vue';
import ListPaging from '../../components/ListPaging.vue';
import memberService from '../../services/member.service';
import VideoRetryModal from '../../components/VideoRetryModal.vue';
import VideoResourceService from '../../services/videoresource.service';

const ROWS = 20;
const FUNCTION_NAME = FUNCTION_NAMES.TRIAL_DIGITALHEALTH_VIDEORESOURCE;
const GROUP_NAME = 'BASC';

const COLS_TYPE_A = [
    'IF2001',
    'IF2002',
    'IF2003',
    'IF2004',
    'IF2005',
    'IF2006',
    'RETRY',
    'DOWNLOAD'
];


export default {
    name: "TrialDigitalHealthVideoResource",
    props: {},
    components: {
        TrialDataTab,
        ListPaging,
        // ListPagingWithPageCount
        VideoRetryModal
    },
    data() {
        return {
            ORG_ID: AuthService.getLoginUserOrganization(),
            MENU_ID: 'TRIAL',
            SUBMENU_ID: 'TRIAL_DIGITALHEALTH_VIDEORESOURCE',
            projectId: undefined,
            projects: [],
            organizations: [],
            filteredData: [],
            filters: {
                projectId: "",
                subjectId: "",
                orgId: "",
                gender:"",
                page: 1,
                offset: ROWS,
            },

            selectedItems: [],
            paging: { totalCount: 0, pageNo: 1 },
            thePermission: {
                queryGranted: false,
                createGranted: false,
                deleteGranted: false,
                downloadGranted: false,
                queryAllGranted: false,
                roleId : undefined
            },
            searchData: [],
            subjects: [
         

            ],
            VideoDataCount: 0,
            searchsubjectId: false,
            showVideoRetryModal: false,
            item : {},
            sendfilters : undefined,
            genders: [
                { value: "M", key: "남자" },
                { value: "F", key: "여자" },
	          ],
			 activeGenderTemplate: false,	
             vaildgender : false //page reset을 위한 select options vaildation check 변수 boolean
             ,statusInfo: ''
        };
    },
    computed: {


        /**
         * 조회 권한이 있는가?
         */
        canQuery() { return true; },
        canQueryImaging() { return this.thePermission.queryGranted },
        canQueryAll() {
            return this.thePermission.queryAllGranted
        },
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
        dataDeletable() {
            return (this.selectedItems || []).length > 0;
        },
        columns() {
            return COLS_TYPE_A;
        },
        columnCount() {
            if (!this.columns) return { 'TOTAL': 0, 'TypeA': 0 };
            const counts = {};

            let total = 0;
            for (const k in this.columns) {
                total += (counts[k] = this.columns[k].length);
    
            }

            counts['TOTAL'] = total;
            return { 'TOTAL': this.columns.length, 'TypeA': this.columns.length };
        },
        
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
                    this.filters.gender ='';
                }
                            
                            }
                        });
                    }
            }
    },
    methods: {
        isNoDataOrNoSubject(item) {

        //     return (
        // Object.values(item).forEach(
        //   (key) => key != 'NO_DATA' && key != 'NO_SUBJECT'
        // )
        //     )
  
    for (const status of Object.values(item)) {
      if (status !== 'NO_DATA' && status !== 'NO_SUBJECT') {
        return true;
      }
    }
    return false;

        
      

            

},

        /**
         * ExamDate기준으로 나이 계산 (ExamDate - BirthDay)
         */
        calculateAge(item) {
            return utils.calculateAge(item.birthday, item.mcdData[0].ExamDate);
        },
        monthSince(birthDay) {
            return utils.monthSince(birthDay);
        }
        , gender(val) {
            return utils.gender(val);
        },
        formatDate(val){
           return utils.formatSimpleDate(val); 
        },
        ifNull(val) {
            return utils.mcdIfNull(val);
        },
        getData(data, column) {
            if ((data || []).length <= 0) return "";

            const value = data[column];
            return value;
        },
        selectAll(event) {
            if (!this.canDelete) return;

            const checked = event.target.checked;
            if (checked) {
                let items = [];
                this.subjects.forEach(n => {

                    items = items.concat(n);
                });
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
            this.selectedItems.map(val => {
                list.push({
                    projectId: projectId,
                    trialIndex: val['TRIAL_INDEX'],
                    subjectId: val['SUBJECT_ID']
                });
            });
            this.selectedItems = [];

            TrialService.deleteManyMcdData(list, [GROUP_NAME]).then(results => {
                let count = 0;
                results.map(r => {
                    if (r.data.succeeded) count++;
                });

                this.searchVideoData();
                AlertService.info(`데이터를 삭제했습니다. (${count}/${list.length}건)`);
            }, error => {
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
                projectId: (this.projects || [])[0].projectSeq,
                subjectId: "",
                orgId: "",
                gender:"",
                page: 1,
                offset: ROWS,
            };
            this.projectId = this.filters.projectId;
            this.searchVideoData();
        },
        searchVideoData() {
            this.projectId = this.filters.projectId;
            this.subjects = [];
            this.selectedItems = [];
            this.VideoDataCount = 0;


      

            MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAME).then(result => {
                if (result.data.succeeded) {
                    this.thePermission = result.data.data;
                    const canQuery = result.data.data.queryGranted;
                    const canQueryAll = result.data.data.queryAllGranted;
       
                    if (!canQuery) {
                        alert('데이터를 조회할 사용권한이 없습니다.');
                        return;
                    }

                    // if (!canQueryAll) {
                    //     alert('전체데이터를 조회할 사용권한이 없습니다.');
                    //     return;
                    // }


                    if (this.filters.page <= 0 || !this.filters.page) this.filters.page = 1;



                    if(this.searchsubjectId=== true){
                            this.filters.projectId = undefined;
                            this.filters.orgId = undefined;
                            }

                    //     TrialService.videoDataCount(this.filters).then(result => {

                    //         const datas = result.data.data.items;


                    //         if (result.data.succeeded) {


                    //             this.paging = {totalCount: result.data.data.total, pageNo: this.filters.page};   
                    //     } else {
                    //         this.VideoDataCount = 0;
                    //         this.paging = { totalCount: 0, pageNo: this.filters.page };
                    //     }
                    //     },error => {
                    // console.error(error);
                    //      });


                    TrialService.searchVideoResource(this.filters).then(result => {
                              
                        if (result.data.succeeded) {
                            const data = result.data.data.items;           
                            
                            const groupedData = {};
                            for (let item of data) {
                                const key = `${item.subjectId}-${item.trialIndex}-${item.projectSeq}-${item.orgId}`;
                                if (key in groupedData) {

                                    groupedData[key].areaQcStatus = { ...groupedData[key].areaQcStatus, ...item.areaQcStatus };
                                } else {
                                    groupedData[key] = item;
                                }
                            }
                            // const IF_TYPE1 = [
                            //     { name: 'IF2001', value: '말소리에 반응하기' },
                            //     { name: 'IF2002', value: '모방하기A' },
                            //     { name: 'IF2006', value: '간식활동' },
                            //     { name: 'IF2007', value: '자유놀이' },
                            //     { name: 'IF2008', value: 'NO_SUBJECT' },
                            //     { name: 'IF2009', value: 'NO_SUBJECT' },
                            // ];
                            // const IF_TYPE2 = [
                            //     { name: 'IF2001', value: '말소리에 반응하기' },
                            //     { name: 'IF2003', value: '모방하기B' },
                            //     { name: 'IF2004', value: '공놀이' },
                            //     { name: 'IF2006', value: '간식활동' },
                            //     { name: 'IF2007', value: '자유놀이' },
                            //     { name: 'IF2009', value: 'NO_SUBJECT' },
                            // ];
                            // const IF_TYPE3 = [
                            //     { name: 'IF2001', value: '말소리에 반응하기' },
                            //     { name: 'IF2003', value: '모방하기B' },
                            //     { name: 'IF2004', value: '공놀이' },
                            //     { name: 'IF2005', value: '상징놀이' },
                            //     { name: 'IF2006', value: '간식활동' },
                            //     { name: 'IF2007', value: '자유놀이' },
                            // ];
                            const IF_TYPE1 = [
                                { name: 'IF2001', value: 'A' },
                                { name: 'IF2002', value: 'B' },
                                { name: 'IF2006', value: 'F' },
                                { name: 'IF2007', value: 'G' },
                                { name: 'IF2008', value: 'NO_SUBJECT' },
                                { name: 'IF2009', value: 'NO_SUBJECT' },
                            ];
                            const IF_TYPE2 = [
                                { name: 'IF2001', value: 'A' },
                                { name: 'IF2003', value: 'C' },
                                { name: 'IF2004', value: 'D' },
                                { name: 'IF2006', value: 'F' },
                                { name: 'IF2007', value: 'G' },
                                { name: 'IF2009', value: 'NO_SUBJECT' },
                            ];
                            const IF_TYPE3 = [
                                { name: 'IF2001', value: 'A' },
                                { name: 'IF2003', value: 'C' },
                                { name: 'IF2004', value: 'D' },
                                { name: 'IF2005', value: 'E' },
                                { name: 'IF2006', value: 'F' },
                                { name: 'IF2007', value: 'G' },
                            ];

                            const filteredData = Object.values(groupedData).filter(item => {
                                // filter data by birthDay range and select IF_TYPE array
                                const birthDay = item.birthDay;
                                const trialBirthDay = item.trialBirthDay;
                                let IF_TYPE = IF_TYPE1;
                                if (birthDay >= 24 && birthDay <= 35) {
                                    IF_TYPE = IF_TYPE2;
                                } else if (birthDay >= 36 && birthDay <= 48) {
                                    IF_TYPE = IF_TYPE3;
                                }

                               if(birthDay == 0 && trialBirthDay >= 18 && trialBirthDay <= 23 ){
                                IF_TYPE1.forEach( item =>{

                                        item.value = 'NO_DATA'

                                        })
                                        IF_TYPE = IF_TYPE1
                                 }
                               else if(birthDay == 0 && trialBirthDay >= 24 && trialBirthDay<= 35 ){
                                    IF_TYPE2.forEach( item =>{

                                        item.value = 'NO_DATA'

                                    })
                                    IF_TYPE = IF_TYPE2
                                }
                              else  if(birthDay == 0 && trialBirthDay >= 36 && trialBirthDay <= 48 ){
                                    IF_TYPE3.forEach( item =>{

                                        item.value = 'NO_DATA'

                                    })
                                    IF_TYPE = IF_TYPE3
                                }


                                item.IF_TYPE = IF_TYPE;
                                return true;
                            }).map(item => {
                                // merge IF_TYPE array with areaQcStatus
                                const areaQcStatus = item.areaQcStatus;
                                const filteredAreaQcStatus = {};
                                for (let type of item.IF_TYPE) {
                                    const key = type.name;
                                    if (key in areaQcStatus) {
                                        filteredAreaQcStatus[key] = areaQcStatus[key];
                                    } else if (key == 'IF2008') {
                                        filteredAreaQcStatus[key] = type.value
                                    } else if (key == 'IF2009') {
                                        filteredAreaQcStatus[key] = type.value
                                    } else {
                                        filteredAreaQcStatus[key] = 'NO_DATA';
                                    }
                                }
                                return { ...item, areaQcStatus: filteredAreaQcStatus };
                            });



                            const seq = result.data.data.items.map(v => v.projectSeq); //연구id 값 서칭 후 select option 값 지정
                                        const org = result.data.data.items.map(v => v.orgId);


                                        if(this.searchsubjectId){
                                            this.filters.projectId = seq[0];
                                        this.filters.orgId = org[0];
                                        this.searchsubjectId = false;
                                        }



                            const pageSize = result.data.data.offset; 
                      
                            const totalPages = Math.ceil(filteredData.length / pageSize);

       
                      const currentPage = result.data.data.pageNo; 
             
                       let start = (currentPage - 1) * pageSize; 
                      let end = currentPage * pageSize;                 


                          if (currentPage === totalPages &&  filteredData.length % pageSize !== 0) {
                      
                                  
                              end = start + (filteredData.length % pageSize);
           
                          }

                        let items = filteredData.slice(start, end); 
                          this.subjects = items;

                            this.paging = { totalCount: result.data.data.total, pageNo: this.filters.page };
                        }
                        else {
                            this.VideoDataCount = 0;
                            this.paging = { totalCount: 0, pageNo: this.filters.page };
                        }
                    }, error => {
                        console.error(error);
                    })
                        .finally(() => {
                            jqueryUI.perfectScrollbar.create();
                        });




     
                }
            },
                error => {
                    console.error(error);
                });
        },
        onPagingClick(page) {
            if (!this.canQuery) return;

            this.filters.page = page;
            this.searchVideoData();
        },
        onOffsetChanged(offset) {
            if (!this.canQuery) return;

            this.filters.page = 1;
            this.filters.offset = offset;
            this.searchVideoData();
        },

        sortedMergedAreaQcStatus(data) {

            let sortedMergedAreaQcStatus = Object.fromEntries(
                Object.entries(data).sort((a, b) => a[0].localeCompare(b[0]))
            );



            return sortedMergedAreaQcStatus
        }



        , getQcStatusIcon(area, data) {
            if (data) {
                const status = data
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

        resetTrialData() {

            let subjectIdvaild = true;

            subjectIdvaild = utils.validateSubjectIdFormat(this.filters.subjectId)

            if(this.filters.subjectId){
            if(subjectIdvaild===false){

            return  AlertService.info("연구번호ID를 정확하게 기입하여 주십시오.");
            }else{
            this.searchsubjectId = true;
            this.searchVideoData();
            }

            }else{
                this.getProjects();
            }

            },

            getProjects(){


            const projectid = utils.getProjectId();
            if (!!projectid) {
                this.filters.projectId = projectid;
            }



                        ProjectService.getProjects().then(result => {

                if (result.data.succeeded) {
                    this.projects = result.data.data;
                
                    if (!!!this.filters.projectId) {
                        this.filters.projectId = this.projects.length > 0 ? this.projects[3].projectSeq : '';
                    }
                }

                this.filters.page = 1;
                this.searchVideoData();
            },
                error => { console.error(error); });

        },
      openVideoRetryModal(data) {

        Object.values(data['areaQcStatus']).forEach((status) => {
            if(status != 'NO_SUBJECT' && status!= 'NO_DATA'){
                this.showVideoRetryModal = true;
                this.item = data;

               if(!this.vaildgender){
                this.item['gender'] = '';
               }

            }
        });
      },
      download(data) {
        // if(this.thePermission.roleId != constants.PERMISSION_ROLEID.GENRAL_MANAGER || this.thePermission.roleId != constants.PERMISSION_ROLEID.MANAGER ) return;
        var count=0;
        Object.keys(data['areaQcStatus']).forEach((key) => {
            if(data['areaQcStatus'][key] == 'NO_SUBJECT' || data['areaQcStatus'][key] == 'NO_DATA') return;
            count++;
        });

        if(count==0) {
            // TODO : Message handling - Upload된 데이터가 없습니다.
            console.warn("Upload된 데이터가 없습니다.");
            return;
        }
        VideoResourceService.download(data['subjectId'], data['projectSeq'], data['trialIndex']).then(response => {
            const contentDisposition = response.headers['content-disposition'];
            var filename = contentDisposition.split('filename=')[1].split(';')[0];
            
            var blob = new Blob([response.data], { type: response.headers['content-type'] });
            var url = window.URL.createObjectURL(blob);
            var link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();
            link.remove();
        }).catch(error=>{
            // TODO : Exception handling
            const data = utils.decoder(error);
            AlertService.error(data['message']);

        });
      },
      onCloseVideoRetryModal(data) {
        this.showVideoRetryModal = false;
        // this.searchVideoData();

        // if (succeeded) {
        ;                
            // }
            if(data){
            AlertService.info(data,2000);
                setTimeout(()=>{
                    this.reloadCheck()
                }, 2000);
            }
    },
      reloadCheck(){
        this.searchVideoData()
      },
      onCheckModal(){
        this.showVideoRetryModal = false;
      }
    },
    
    mounted() {
    },
    created() {

     
        MemberService.getAuthMemberFunctionPermission(FUNCTION_NAME).then(result => {
            if (result.data.succeeded) {
                this.rolePermission = result.data.data;

                this.getProjects();
            }
        });

    }
};
</script>
    
<style scoped>
.btn.red-button {
  background-color: #4855C0;
  /* Add any other styles you want for the reduced color */
  display: inline-block;
    margin: 0;
    padding: 0 10px;
    min-width: 75px;
    height: 27px;
    font-size: 12px;
    font-family: 'dotum','sans-serif';
    color: #fff;
    line-height: 28px;
    border-radius: 27px;
    /* background-color: #84858b; */
    vertical-align: middle;
    white-space: pre;
    box-sizing: border-box;
    letter-spacing: -0.5px;
}

</style>
    