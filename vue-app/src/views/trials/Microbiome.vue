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
                            <option v-for="op in projects" :key="op.projectSeq" :value="op.projectSeq">{{ op.projectName
                                }}
                            </option>
                        </select>
                    </li>
                    <li>
                        <label for="part_oz">참여기관</label>
                        <select class="w_206" v-model="filters.orgId">
                            <option value=''>::전체::</option>
                            <option v-for="op in organizations" :key="op.orgId" :value="op.orgId">{{ op.orgName }}
                            </option>
                        </select>
                    </li>
                    <li class="btns fr">
                        <template v-if="canQuery">
                            <button class="btn btn_violet" type="button" @click="searchMicrobiomeData">검색</button>
                            <button class="btn btn_violet" type="button" @click="searchGender">추가검색</button>
                            <button class="btn btn_gray" type="button" @click="reset">검색 초기화</button>
                        </template>
                    </li>
                </ul>
                <ul v-if="activeGenderTemplate" class="list_input" style="position: relative; left: 554px;">
                    <li>
                        <label for="gender">성별</label>
                        <select @change="searchPick($event)" name="" id="gender" style="width: 100px;"
                            v-model="filters.gender">
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
                <!-- <div v-if="canDownload">
                    <button type="button" class="btn btn_violet"  @click="dataSetDownload">데이터추출 다운로드</button>
                </div> -->
                <div class="fr">
                    <button v-if="canDelete && dataDeletable" type="button" class="btn btn_violet"
                        @click="deleteSelectedItems">데이터
                        삭제</button>
                </div>
            </div>
        </div>
        <div class="cont_body mt_11">
            <div class="badge_wrap">
                <span class="badge"><img src="../../assets/images/badge_not.png" alt=""> 대상아님</span>
                <span class="badge"><img src="../../assets/images/badge_unreg.png" alt=""> 미등록</span>
                <!-- <span class="badge"><img src="../assets/images/badge_unveri.png" alt=""> 미검증</span> -->
                <span class="badge"><img src="../../assets/images/badge_fin.png" alt=""> 완료</span>
            </div>
            <table class="tb_background" data-height="605">
                <tbody>
                    <tr>
                        <td>
                            <div class="scroll_head">
                                <table class="tb_head">
                                    <colgroup>
                                        <col width="20px"><!-- Checkbox -->
                                        <col width="50px"><!-- 연구번호ID -->
                                        <col width="30px"><!-- 성별 -->
                                        <col width="30px"><!-- 연령 -->
                                        <col width="50px"><!-- 참여기관 -->
                                     <!--   <col width="50px">검사수행일-->
                                        <col width="30px"><!-- 대상군 -->
                                        <col width="30px"><!-- 차수 -->
                                        <col width="130px"><!--동공측정 Completed-->
                                        <col width="60px" v-if="canDelete"><!--삭제하기-->
                                        <col width="60px"><!--다운로드-->
                                    </colgroup>
                                    <thead>
                                        <tr>
                                            <th rowspan="2" class="sticky">
                                                <label>
                                                    <!-- <input type="checkbox"
                                                        class="check_all" name="table_check"
                                                        @click="selectAll"> -->
                                                    <span></span></label>
                                            </th>
                                            <th rowspan="2" class="sticky">연구번호 ID</th>
                                            <th rowspan="2" class="sticky">성별</th>
                                            <th rowspan="2" class="sticky">연령</th>
                                            <th rowspan="2" class="sticky">참여기관</th>
                                            <!-- <th rowspan="2" class="sticky">검사수행일</th> -->
                                            <th rowspan="2" class="sticky">대상군</th>
                                            <th rowspan="2" class="ssticky border_dark">차수</th>
                                            <th rowspan="2" class="sticky">장내미생물</th>
                                            <th rowspan="2" class="sticky" v-if="canDelete">삭제하기</th>
                                            <th rowspan="2" class="sticky">다운로드</th>
                                        </tr>
                                    </thead>
                                </table>
                            </div>
                            <div class="scroll_body ps ps--active-x ps--active-y" style="max-height: 605px;">
                                <table class="tb_body">
                                    <colgroup>
                                        <col width="20px"><!-- Checkbox -->
                                        <col width="50px"><!-- 연구번호ID -->
                                        <col width="30px"><!-- 성별 -->
                                        <col width="30px"><!-- 연령 -->
                                        <col width="50px"><!-- 참여기관 -->
                                     <!--   <col width="50px"> 검사수행일-->
                                        <col width="30px"><!-- 대상군 -->
                                        <col width="30px"><!-- 차수 -->
                                        <col width="130px"><!--동공측정-->
                                        <col width="60px" v-if="canDelete"><!--삭제하기-->
                                        <col width="60px"><!--다운로드-->
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
                                            <!-- <td class="sticky">{{ formatDate(item.registDate) }}</td> -->
                                            <td class="sticky">{{ item.projectName }}</td>
                                            <td class="sticky border_dark">{{ item.trialIndex }}</td>
                                            <td><img v-if="canQueryImaging"
                                                    :src="`${getQcStatusIcon('MICROBIOME', item)}`">
                                            </td>
                                            <td v-if="canDelete"><button type="button" class="btn"
                                                    :class="{ 'red-button': isDoneQCData(item['areaQcStatus']) }"
                                                    @click="openCheckDeleteOption(item)">삭제하기</button></td>
                                            <td><button type="button" class="btn" @click="download(item)">다운로드</button>
                                            </td>
                                            <!-- </tr> -->
                                        </tr>
                                        <template v-if="subjects.length <= 0">
                                            <tr>
                                                <td colspan="10">
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
            <pupillometry-delete-modal @closed="onCloseDeleteModal" @check="onCheckModal"
                v-if="showCanDeleteOptionModal" :item=this.item>
            </pupillometry-delete-modal>
        </transition>
        <vue-element-loading :active="working" spinner="bar-fade-scale" color="#FF6700" :text="workingMessage"
            is-full-screen />
    </div>
</template>

<script>
import { FUNCTION_NAMES, constants, utils } from '../../services/constants';
import VueElementLoading from "vue-element-loading";
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
import MicrobiomeService from '../../services/microbiome.service';

import PupillometryDeleteModal from '../../components/PupillometryDeleteModal.vue';

const ROWS = 20;
const FUNCTION_NAME = FUNCTION_NAMES.TRIAL_BIOMARKER_MICROBIOME;
const GROUP_NAME = 'BASC';





export default {
    name: "Microbiome",
    props: {},
    components: {
        VueElementLoading,
        TrialDataTab,
        ListPaging,
        // ListPagingWithPageCount
        PupillometryDeleteModal

    },
    data() {
        return {
            ORG_ID: AuthService.getLoginUserOrganization(),
            MENU_ID: 'TRIAL',
            SUBMENU_ID: 'TRIAL_PSYCH_TEST',
            projectId: undefined,
            projects: [],
            organizations: [],
            filteredData: [],
            filters: {
                projectId: "",
                subjectId: "",
                orgId: "",
                gender: "",
                trialIndex: 1,
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
                queryAllGranted: false
            },

            pupillometryDataCount: 0,
            subjects: [],
            searchsubjectId: false,
            showCanDeleteOptionModal: false,
            item: {},
            genders: [
                { value: "M", key: "남자" },
                { value: "F", key: "여자" },
            ],
            activeGenderTemplate: false,
            vaildgender: false //page reset을 위한 select options vaildation check 변수 boolean
            , userName: ""
            , working: false,
            workingMessage: '다운로드할 파일을 생성 중입니다...',
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
        canDelete() {
            const canMember = ['BASIC12@ASD', 'WON@ASD', 'SEOUL12@ASD','DKLEE02@ASD'];

            if (canMember.includes(this.thePermission.memberKey) && this.thePermission.deleteGranted) {
                return true;
            } else {
                return false;
            }


        },
        /**
         * 다운로드 권한이 있는가?
         */
        canDownload() { return this.thePermission.downloadGranted; },
        dataDeletable() {
            return (this.selectedItems || []).length > 0;
        }
    },
    watch: {
        'filters.projectId'(newval) {
            const id = this.filters.projectId;

            if (id) { // subhjectId 검색 필터의 undfiended 의 check를 위한 id booleanchek
                ProjectService.getAsdOrganizations(id).then(result => {
                    if (result.data.succeeded) {

                        this.organizations = result.data.data;
                        if (this.filters.subjectId) { // 연구번호ID 검색 필터의 select option 값 업데이트를 위한 로직 추가
                            const orgid = result.data.data.map(v => v.orgId);

                            // this.filters.orgId = orgid[0]
                        } else {
                            this.filters.orgId = '';
                            this.filters.gender = '';
                        }

                    }
                });
            }
        }
    },
    methods: {
        isDoneQCData(data) {
            console.log("item['areaQcStatus'] : " + data.PUPILLOMETRY)
            if (data.PUPILLOMETRY === 'DONE_QC') {
                return true;
            }

            return false;


        },

        openCheckDeleteOption(data) {
            console.log("삭제하기 method" + data['areaQcStatus'].PUPILLOMETRY)
            if ((data['areaQcStatus'].PUPILLOMETRY === 'DONE_QC')) {
                this.showCanDeleteOptionModal = true;
                console.log(this.showCanDeleteOptionModal)
                this.item = data;
                if (!this.vaildgender) {
                    this.item['gender'] = '';
                }
            }






        },

        onCloseDeleteModal(data) {
            this.showCanDeleteOptionModal = false;
            // this.searchVideoData();
            console.log("data :", data.data)

            if (data.data != undefined) {
                console.log("successed :", data.data.succeeded)
                console.log("message :", data.data.message)
                
            }


            // console.log("projectSeq :" ,data.data.projectSeq)
            // console.log("orgId :" ,data.data.orgId)
            // if (succeeded) {
            ;
            // }
            if (data == null) {
                this.showCanDeleteOptionModal = false;
                return false;
            }

            // const modalList = JSON.stringify(data.data);

            if (data.data.succeeded) {
                this.filters.subjectId = data.data.subjectId
                AlertService.info(data.data.message, 2000);
                setTimeout(() => {
                this.filters.subjectId = data.data.subjectId
                    this.reloadCheck()
                }, 2000);
            } else {
                AlertService.error(data.data.message)
            }
         this.filters.subjectId = data.data.subjectId
        },
        reloadCheck() {

            this.resetTrialData();

            // this.filters.projectId = data.data.data[0].projectSeq;
            // this.filters.orgId = data.data.data[0].orgId;
        },

        onCheckModal() {
            this.showCanDeleteOptionModal = false;
        },
        /**
         * ExamDate기준으로 나이 계산 (ExamDate - BirthDay)
         */
        calculateAge(birthDay) {
            //return utils.calculateAge(item.birthday, item.mcdData[0].ExamDate);
            return utils.monthSince(birthDay);
        },
        gender(val) {
            return utils.gender(val);
        },
        formatDate(val) {
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

                this.searchMicrobiomeData();
                AlertService.info(`데이터를 삭제했습니다. (${count}/${list.length}건)`);
            }, error => {
                console.error(error);
            });
        },
        searchPick(event) {
            //this.vaildgender = !!event.target.value;
            const index = event.target.options.selectedIndex; // select options index 변수에 담음.
            this.vaildgender = !!event.target.options[index].getAttribute('data-key'); // options data-key로  값의 유무체크로 true /false값 체크하여 변수에 담음.
            if (this.vaildgender) {
                this.filters.page = 1;
            }
        },
        searchGender() {
            this.activeGenderTemplate = true;
            if (this.vaildgender) {
                this.searchMicrobiomeData();
            }
        },
        reset() {
            this.filters = {
                projectId: (this.projects || [])[0].projectSeq,
                subjectId: "",
                orgId: "",
                gender: "",
                page: 1,
                offset: ROWS,
            };
            this.projectId = this.filters.projectId;
            this.searchMicrobiomeData();
        },
        searchMicrobiomeData() {
            this.projectId = this.filters.projectId;
            console.log("this.filters.subjectId"+ this.filters.subjectId)
            this.subjects = [];
            this.selectedItems = [];
            this.VideoDataCount = 0;


            MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAME).then(result => {
                if (result.data.succeeded) {
                    this.thePermission = result.data.data;
                    const canQuery = result.data.data.queryGranted;
                    console.log(this.thePermission)

                    if (!canQuery) {
                        alert('데이터를 조회할 사용권한이 없습니다.');
                        return;
                    }



                    if (this.filters.page <= 0 || !this.filters.page) this.filters.page = 1;

                    if (this.searchsubjectId === true) {
                        this.filters.projectId = undefined;
                        this.filters.orgId = undefined;

                    }



                    TrialService.searchMicrobiomeResource(this.filters).then(result => {
                        if (result.data.succeeded) {
                            this.subjects = result.data.data.items;
                            this.paging = { totalCount: result.data.data.total, pageNo: this.filters.page };
                            this.microDataCount = result.data.data.total;
                            const seq = result.data.data.items.map(v => v.projectSeq); //연구id 값 서칭 후 select option 값 지정
                            const org = result.data.data.items.map(v => v.orgId);
                            // this.userName = result.data.data.items[0].userName;
                            //this.userName = result.data.data.items.map( v => v.userName);
                            console.log(this.subjects)

                            this.userName = result.data.data.items[0].userName;
                            console.log("username" + this.userName)
                            // this.userName = result.data.data.items.map( v => v.userName);

                            if (this.thePermission.memberKey == null) {
                                this.thePermission.memberKey = this.userName;
                            }

                            if (this.searchsubjectId) {
                                this.filters.projectId = seq[0];
                                this.filters.orgId = org[0];
                                this.searchsubjectId = false;
                            }

                        }

                        else {
                            this.pupillometryDataCount = 0;
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
            this.searchMicrobiomeData();
        },
        onOffsetChanged(offset) {
            if (!this.canQuery) return;

            this.filters.page = 1;
            this.filters.offset = offset;
            this.searchMicrobiomeData();
        },

        sortedMergedAreaQcStatus(data) {

            let sortedMergedAreaQcStatus = Object.fromEntries(
                Object.entries(data).sort((a, b) => a[0].localeCompare(b[0]))
            );



            return sortedMergedAreaQcStatus
        }



        , getQcStatusIcon(area, data) {
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
                                    constants.QC_ICONS.NO_DATA;
            }

            return constants.QC_ICONS.NO_SUBJECT;
        },


        resetTrialData() {

            let subjectIdvaild = true;

            subjectIdvaild = utils.validateSubjectIdFormat(this.filters.subjectId)

            if (this.filters.subjectId) {
                if (subjectIdvaild === false) {

                    return AlertService.info("연구번호ID를 정확하게 기입하여 주십시오.");
                } else {
                    this.searchsubjectId = true;
                    this.searchMicrobiomeData();
                }

            } else {
                this.getProjects();
            }

        },

        getProjects() {


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
                this.searchMicrobiomeData();
            },
                error => { console.error(error); });

        },
        download(item) {
            console.log(item)
            var count = 0;
            Object.keys(item['areaQcStatus']).forEach((key) => {
                console.log("item['areaQcStatus'][key] :" + item['areaQcStatus'][key])

                if (item['areaQcStatus'][key] == 'NO_SUBJECT' || item['areaQcStatus'][key] == 'NO_DATA') return;
                count++;
            });
            if (count == 0) {
                // TODO : Message handling - Upload된 데이터가 없습니다.
                console.warn("Upload된 데이터가 없습니다.");
                return;
            }
            console.log(item)
            PupillometryService.download(item['subjectId'], item['projectSeq'], item['trialIndex']).then(response => {
                // attachment; filename=eyetracking_1023041001_4_1_20230707232458965.zip
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
            }).catch(error => {
                // TODO : Exception handling
                const data = utils.decoder(error);
                AlertService.error(data['message']);
            });
        },
        dataSetDownload() {
            if (!this.canDownload) return;
            this.working = true;
            this.workingMessage = "다운로드 할 파일을 생성 중입니다...";
            AlertService.info(
                `선택한 데이터 다운로드 중 입니다. 데이터가 많을 경우 처리 시간이 오래 걸릴 수도 있습니다.`,
                3000,
                '시선추적 데이터 추출  정보');
            const projectSeq = this.filters.projectId;
            console.log("시선추적datSetprojectSEq: " + projectSeq);
            PupillometryService.dataSetDownload(projectSeq).then(response => {
                if (response.status === 200) {
                    AlertService.info(
                        `선택한 데이터 다운로드를 완료 했습니다.`,
                        3000,
                        '시선추적 데이터 추출  정보');
                } else {
                    AlertService.error(response.data.message);
                }
                // attachment; filename=eyetracking_1023041001_4_1_20230707232458965.zip
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

            }).catch(error => {
                // TODO : Exception handling
                const data = utils.decoder(error);
                AlertService.error(data['message']);
            })
                .finally(() => { this.working = false; });;


        }
    },
    mounted() {
    },
    created() {



        MemberService.getAuthMemberFunctionPermission(FUNCTION_NAME).then(result => {
            if (result.data.succeeded) {
                this.rolePermission = result.data.data;
                console.log("rolePermission[pup]==> ", this.rolePermission)
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
    font-family: 'dotum', 'sans-serif';
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