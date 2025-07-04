<template>
    <div class="mo_contents">
        <div class="search_input">
            <select @change="searchsPick($event)">
                <option :key="i" :value="d.v" v-for="(d, i) in options">{{ d.t }}</option>
            </select>
            <p></p>
            <input type="text" id="user_id" v-if="searchPick == 'user_id'" v-model="filters.userId">
            <!-- <input type="text" id="user_title" v-else-if="searchPick == 'user_title'" v-model="filters.title"> -->
            <input type="text" id="all" v-else="searchPick == 'all'" v-model="filters.all">
            <button class="btn btn_blue" type="button" v-on:click="search()">검색</button>
        </div>


        <div class="board_wrap">
            <div class="board_head mo_hide">
                <span class="col div_num">번호</span>
                <span class="col div_subject">제목</span>
                <span class="col div_name">대상자 ID</span>
                <span class="col div_name">등록기관</span>
                <span class="col div_date">등록일자</span>
                <span class="col div_answer">처리상태</span>
            </div>
            <ul class="board_list scroll_y">
                <li v-for="(item, index)  in items" :key="item.boardItemId" @click="searchCheck($event, item.boardItemId)"
                    ref="click">
                    <div class="col div_num mo_hide" id="itemIndex">{{ paging.totalCount - index }}</div>
                    <div class="col div_subject" id="titles" style="color: #7d7d7d;">
                        <!--   <router-link :to="`board/${item.boardItemId}`" class="subject" id="titles"> -->
                        {{ nonIidentificationTitle }}
                        <img class="newimg" :src="`${getNewStatusIcon('NEWSTATE', item)}`">
                        <i class="ico_lock"><span class="blind">비공개글</span></i>
                        <!--  </router-link> -->
                    </div>
                    <div class="col div_name">{{ item.userId }}</div>
                    <div class="col div_name">{{ item.orgName }}</div>
                    <div class="col div_date"> {{ formatDate(item.dateCreated) }} </div>
                    <div class="col div_answer">
                        <button type="button" class="btn btn_gwhite" v-if="item.commentCount > 0">답변완료</button>
                        <button type="button" class="btn btn_green" v-else>접수완료</button>
                    </div>
                </li>
                <template v-if="items.length <= 0">
                    <li>
                    <div class="col div_subject" id="titles" style="text-align: center; width: 100%;">데이터가 존재하지 않습니다.</div>
                    </li>
                </template>
            </ul>
        </div>
        <div class="btn_wrap board_btm ta_c">
            <router-link to="board/upload">
                <a class="btn btn_vwhite btn_full">문의등록</a>
            </router-link>
        </div>
    </div>
    <list-paging @click="onPagingClick" :paging="paging" :rowsPerPage="filters.offset"></list-paging>

    <!-- 팝업 : 비밀번호입력 -->
    <div class="popup_bg" v-if="checkparam === true"></div>
    <div class="popup pop_confirmPw" v-if="checkparam === true">
        <button type="button" class="btn_close" @click="cansle()"><span class="blind">팝업 창닫기</span></button>
        <div class="pop_cont">
            <p id="pwdtext">{{ pwdtext }}</p>
            <div class="input">
                <input type="password" name="pasword" id="pasword" v-model="password" maxlength="6"
                    placeholder="숫자[6자리입력]" />
            </div>
        </div>
        <div class="pop_btm">
            <button type="button" class="btn btn_cancel" @click="cansle()">취소</button>
            <button type="button" class="btn btn_confirm" @click="registPassword()">확인</button>
        </div>
    </div>
</template>
  
<script>

import axios from 'axios';
import { jqueryUI } from '../../../services/jquery-ui';
import ListPaging from "../../../components/ListPaging.vue";
import { constants, FUNCTION_NAMES } from '../../../services/constants';
import boardService from '../../../services/board.service';
import dayjs from 'dayjs';
import alertService from '../../../services/alert.service';
import { exportDefaultSpecifier } from '@babel/types';
import { relativeTimeThreshold } from 'moment';

const ROWS = 50;
const FUNCTION_NAME = FUNCTION_NAMES.BOARD_QNA;
export default {
    name: 'MobileOmniBoardList',
    components: {
        ListPaging,
        dayjs,
    },
    data() {
        return {
            itemId: undefined,
            password: undefined,
            checkparam: false, //비밀번호 팝업 USESTATUS //true면 띄운다
            canclecheckbutton: false, // 비밀번호 팝업 확인,취소 STATUS
            pwdtext: '비밀번호를 입력해 주세요.', //비밀번호 팝업 <p>text 값

            options: [
                { v: "all", t: "전체" },
                { v: "user_id", t: "대상자 ID" },
                // { v: "user_title", t: "제목" },

                //  { v: "title", t: "제목" },
            ],
            searchPick: undefined,
            filters: {
                boardItemId: undefined,
                page: 1,
                offset: ROWS,
                title: undefined,
                userId: undefined,
                all: undefined,
            },
            items: [],
            paging: { totalCount: 0, pageNo: 1 },
            nonIidentificationTitle : '[아이AI] 문의사항이 정상적으로 등록되었습니다.'
        }
    },
    computed: {

    },
    watch: {
        password: function () {
            return this.password = this.password.replace(/[^0-9]/g, '');  //정규식 사용    
        }
    },
    methods: {
        searchsPick(event) {
            this.searchPick = event.target.value

            if (this.searchPick == 'user_id') {
                this.filters.title = undefined;
                this.filters.all = undefined;

            }
            //  else if (this.searchPick == 'user_title') {
            //     this.filters.userId = undefined;
            //     this.filters.all = undefined;
            // } 
            else if (this.searchPick == 'all') {

                this.filters.userId = undefined;
                // this.filters.title = undefined;


            }
        },
        search() {
            this.filters.page = 1; // Go to the first page when 'search' button clicks


            this.findAllboardList();
        },
        findAllboardList() {
            // this.items = [];
            // if (this.filters.page <= 0 || !this.filters.page) this.filters.page = 1;
            boardService.searchAllData(this.filters).then(
                result => {
                    if (result.data.succeeded) {
                        this.items = result.data.data.items;
                        this.paging = { totalCount: result.data.data.total, pageNo: this.filters.page };
                    } else {
                        this.items = [];
                        this.total = 0;
                        this.paging = { totalCount: 0, pageNo: this.filters.page };
                    }
                }, error => {

                    console.error(error);
                })


        },
        onPagingClick(page) {
            this.filters.page = page;
            this.findAllboardList();
        },
        formatDate(dateString) {
            const date = dayjs(dateString);
            return date.format("YYYY-MM-DD");
        },
        searchCheck($event, item) {
            this.itemId = item;
            var tagId = $event.target.getAttribute('id');
            if (tagId === 'titles') {
                this.checkparam = true;
            }
            this.checkparam = true;

        }, thisElclick() {

            this.checkparam = true;
            this.$refs.click.$el.click();

        }
        ,
        registPassword() {

            if (!this.password) return;

            this.checkparam = true;
            if (this.password.length < 6 || this.password == undefined) {

                this.pwdtext = "6자리 까지 입력해주세요."
                return false;
            } else if (this.password.length == 6) {

                const formData = new FormData();
                formData.append('boardItemId', this.itemId);
                formData.append('password', this.password);

                boardService.searchPassword(formData).then(result => {

                    if (result.data.succeeded) {

                        const chkpasswrod = result.data.data.password;

                        if (chkpasswrod != null) {
                            this.$router.push("/mobile/omni/board/" + this.itemId) // URL로 이동

                            this.checkparam = false;
                        }

                    } else {
                        this.checkparam = true;
                        this.pwdtext = "비밀번호가 일치하지 않습니다";
                        this.password = '';

                    }


                }, error => {

                    console.error(error);
                });


                this.checkparam = false;

            }



        }, cansle() {
            this.password = '';
            this.checkparam = false;
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


    },
    reset() {

    },
    mounted() {
        //페이지 시작시 자동 함수 실행
        this.search();
        this.$refs.click
    },
    created() {
        this.$refs.click
    }


}






</script>
  
<style scoped>

.btn:before {
    content: "";
    display: inline-block;
    margin: 0 0 0 0;
    width: 5px;
    height: 7px;
    background: none;
    vertical-align: middle;
}
.newimg {
    position: relative;
    left: 5px;
    margin-bottom: 1px;
    
}
</style>
  
  