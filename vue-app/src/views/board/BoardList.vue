<template>
    <div id="ContentsDiv">
        <div class="cont_top pt_18">
            <h3 class="tit_bul">문의 게시판</h3>          
            <div class="inputbox mt_18">
                <div class="search_input">
                    <!-- <li>
                        <label for="user_name">이름</label>
                        <input type="text" id="user_name" class="w_137" v-model="filters.userId">
                    </li> -->
            <select @change="searchsPick($event)">
                <option :key="i" :value="d.v" v-for="(d, i) in options">{{ d.t }}</option>
            </select>
            <p></p>
            <input type="text" id="user_id" v-if="searchPick == 'user_id'" v-model="filters.userId">
            <input type="text" id="user_title" v-else-if="searchPick == 'user_title'" v-model="filters.title">
            <input type="text" id="all" v-else="searchPick == 'all'" v-model="filters.all">
            <!-- <button class="btn btn_blue" type="button" v-on:click="search()">검색</button> -->
            <button class="btn btn_violet" type="button" v-on:click="search()">검색</button>
                    <!-- <li class="btns">
                   
                    </li> -->
                </div>
            </div>
        </div>      
        <div class="cont_body mt_18">
            <table class="tb_head tb_body">
                <colgroup>
                    <col width="40px">
                    <col>
                    <col width="150px">
                    <col width="150px">   
                    <col width="150px">                     
                    <col width="150px">
                    </colgroup>
                <thead>
                    <tr>
                        <th>번호</th>
                        <th>제목</th>
                        <th>대상자 ID</th>
                        <th>등록기관</th>
                        <th>등록일시</th>
                        <th>처리상태</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in items" :key="item.boardItemId">
                        <td>{{ paging.totalCount - index }}</td>
                        <td style="text-align: left"><router-link :to="makeUrl(`/board/${item.boardItemId}`)">{{item.title}}
                        <img class="newimg" :src="`${getNewStatusIcon('NEWSTATE', item)}`">
                        </router-link>    
                        </td>
                        <td>{{item.userId}}</td>
                        <td>{{ item.orgName }}</td>
                        <td>{{formatDate(item.dateCreated)}}</td>
                        <td>
                            <span v-if="item.commentCount > 0" class="status gwhite">답변완료</span>
                            <span v-else class="status green">접수완료</span>
                        </td>
                    </tr>
                </tbody>
            </table>
            <list-paging @click="onPagingClick" :paging="paging" :rowsPerPage="filters.offset" ></list-paging> 
        </div>
    </div>
</template>
  
<script>

import axios from 'axios';
import ListPaging from "../../components/ListPaging.vue";
import { jqueryUI } from '../../services/jquery-ui';
import { constants, utils, FUNCTION_NAMES } from '../../services/constants';
import ProjectService from '../../services/project.service';
import BoardService from '../../services/board.service';
import MemberService from "../../services/member.service";

const ROWS = 50;
const FUNCTION_NAME = FUNCTION_NAMES.BOARD_QNA;

export default {
    name: 'BoardList',
    components: {
        ListPaging
    },
    data() {
        return {
            options: [
                { v: "all", t: "전체" },
                { v: "user_id", t: "대상자 ID" },
                { v: "user_title", t: "제목" },

                //  { v: "title", t: "제목" },
            ],
            searchPick: undefined,
            filters: {
                userId: undefined,
                title: undefined,
                all : undefined,
                projectId: "",
                page: 1,
                offset: ROWS,
            },
            projects: [],
            items: [],
            paging: { totalCount: 0, pageNo: 1 },
            thePermissionImaging: {
					queryGranted: false,
					createGranted: false,
					deleteGranted: false,
					downloadGranted: false
				},
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
    },
    watch: {
    },
    methods: {
        makeUrl(url) {
      return url + "?project=" + this.filters.projectId;
        },
        searchsPick(event) {
            this.searchPick = event.target.value

            if (this.searchPick == 'user_id') {
                this.filters.title = undefined;
                this.filters.all = undefined;

            } else if (this.searchPick == 'user_title') {
                this.filters.userId = undefined;
                this.filters.all = undefined;
            } else if (this.searchPick == 'all') {

                this.filters.userId = undefined;
                this.filters.title = undefined;


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
        this.search();
    },
        search() {
			this.filters.page = 1; // Go to the first page when 'search' button clicks
            this.doSearch();

		},
        doSearch() {            
            this.projectId = this.filters.projectId;


     axios.all([
      MemberService.getAuthMemberProjectPermission(this.projectId, FUNCTION_NAMES.BOARD_QNA),
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


            BoardService.search(this.filters).then(result=>{
				if (result.data.succeeded) {
					this.items = result.data.data.items;
					this.paging = {totalCount: result.data.data.total, pageNo: this.filters.page};
				}
				else {
					this.items = [];
                    this.total = 0;
					this.paging = {totalCount: 0, pageNo: this.filters.page};
                    this.$router.push('/errors/no-permission');
				}				
			},
			error=>{
              console.error(error);
          }) .finally(()=>{
              jqueryUI.perfectScrollbar.create();
          });
        });
		},
        onPagingClick(page) {
			this.filters.page = page;
			this.doSearch();
		},
        formatDate(date) {
            return utils.formatSimpleDate(date);
        }
        , getNewStatusIcon(area, data) {

            if (data) {

                const status = data.areaNewStatus[area];
                return status == "NEW_UPLOAD_STAGE" ?
                    constants.NEW_STATE_ICON.NEW_STATE :
                    status == "NO_NEW_STAGE" ?
                        constants.NEW_STATE_ICON.NEW_NO_STAGE :
                        constants.NEW_STATE_ICON.NEW_NO_STAGE;
            }

            return constants.QC_ICONS.NO_SUBJECT;
        },
        getProjects(successCallback) {
      if (!this.canQuery) return;

        ProjectService.getProjects().then(
        (result) => {
            if (result.data.succeeded) {
                this.projects = result.data.data;
                // this.filters.projectId = (this.projects || []).length > 0 ? this.projects[0].projectSeq : undefined;
                this.filters.projectId = (this.projects || []).length > 0 ? this.projects[0].projectSeq : undefined;

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
    },
    mounted() {
        // this.search();
    },
    created() {


        this.filters.page = 1;
        this.getProjects(()=>this.search());



    }
}
</script>
  
<style scoped>
.status {
    display: inline-block;
    margin: 0;
    padding: 0 5px;
    min-width: 75px;
    height: 32px;
    color: #fff;
    line-height: 32px;
    border-radius: 25px;
    background-color: #84858b;
    vertical-align: middle;
    white-space: pre;
    box-sizing: border-box;
    text-align: center;
}
.status.green {
    background: #719f4a;
    width: 78px;
    height: 25px;
    line-height: 25px;
}
.status.gwhite {
    color: #629d30;
    background: #fff;
    border: 1px solid #86b162;
    width: 78px;
    height: 25px;
    line-height: 25px;
}
.newimg {
    position: relative;
    left: 5px;
    margin-bottom: 1px;
    
}
</style>
  
  