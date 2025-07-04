<template>
    <div class="modal-mask">
        <div class="modal-wrapper popup pop_target_reg">
            <div class="modal-container">
                <!-- 팝업 : 상호작용 영상 재촬영 -->
                <div class="popup pop_recaptue" v-if="showCanDeleteOptionModal">
                    <div class="pop_top">
                        <h3>동공측정 삭제</h3>
                        <button type="button" class="btn_close b-close" @click="close"><span class="blind">팝업
                                창닫기</span></button>
                    </div>
                    <div class="pop_cont">
                        <form action="">
                            <legend>동공측정 삭제 폼</legend>
                            <ul class="check_ul">
                                <li class="check_li">
                                    <div class="check_list">
                                        <label v-for="(key, col)  in item['areaQcStatus']" :key="col">
                                            <template v-if="key != 'NO_DATA'">
                                                <input type="checkbox" v-model="categories" :value="col" /><span><span
                                                        style="font-weight: bold;">{{ col }}</span><span
                                                        style="position: relative; left:10px; bottom: 2px; font-size: 13px;">TYPE</span></span>
                                            </template>
                                        </label>
                                    </div>
                                </li>
                            </ul>
                            <div class="btn_wrap ta_c">
                                <button class="btn btn_sblue btn_lh31 btn_arrow_none" type="button" @click="check">삭제
                                    처리</button>
                            </div>
                        </form>
                    </div>
                </div>
                <!-- 팝업 : 재활영 처리 진행여부 -->
                <div class="popup pop_recaptue2" v-if="showCheckModal">
                    <button type="button" class="btn_close b-close" @click="close"><span class="blind">팝업
                            창닫기</span></button>

                    <div class="pop_cont">
                        <p class="ta_c">선택한 과제의 재촬영 처리를 <br />진행하시겠습니까?</p>
                        <div class="btn_wrap ta_c">
                            <button class="btn btn_sblue btn_lh31 btn_arrow_none" type="button"
                                @click="closeModalAndPassData">예</button>
                            <button class="btn btn_lh31 btn_arrow_none" type="button" @click="close">아니오</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>


<script>

require("../assets/js/jquery.bpopup.min.js");

// import VideoResourceService from '../services/videoresource.service';
// import EyetrackingService from '../services/eyetracking.service';
import { FUNCTION_NAMES, constants, utils } from '../services/constants';
import PupillometryService from '../services/pupillometry.service';
import AlertService from '../services/alert.service';

export default {
    name: "PupillometryDeleteModal",
    components: {

    },
    props: {
        item: {
            type: Object,
            required: true,
        }
    },
    data() {
        return {
            showCanDeleteOptionModal: true,
            showCheckModal: false,
            categories: [],
            subjects: [],
            message: undefined,
            data: []
        };
    },
    computed: {

    },
    emits: ['close', 'check'],
    methods: {

        check() {
            this.showCheckModal = !this.showCheckModal;
            this.showCanDeleteOptionModal = !this.showCanDeleteOptionModal;
        },
        execute() {
            // 시선추적 삭제  실행 로직 
            // subjectId , projectSeq, trialIndex , type
            try {
                console.log("this.item['subjectId'] :"+ this.item['subjectId'])
                PupillometryService.postDeletePupillometry(this.item['subjectId'], this.item['projectSeq'], this.item['trialIndex'])
                    // VideoResourceService.postRetryVideoResource(this.item['subjectId'], this.item['projectSeq'], this.item['trialIndex'], this.categories)
                    .then(response => {
                        // this.subjects = response.data[0]['subjects'];
                        //  this.message = response.data[0]['message'];
                        console.log("result: " + JSON.stringify(response.data));
                        console.log("message: " + JSON.stringify(response.data.message));
                        // console.log("message[noJson]: " + response.data.message);
                        this.data = response

                        // Assuming this.close() is a method to close something
                        this.close();

                    })
                    .catch(error => {
                        if (error.response && error.response.data) {
                            const errorMessage = error.response.data.message;
                            AlertService.error(errorMessage);
                        } else {
                            AlertService.error('error가 발생했습니다. 다시 한번 더 시도 해주세요.');
                        }
                    });
            } catch (error) {
                console.error('An error occurred:', error);
                AlertService.error('error가 발생했습니다. 다시 한번 더 시도 해주세요.');
            }
        },
        closeModalAndPassData() {
            this.execute();
        },
        close() {
            this.$emit('closed', this.data);
        },



    },
    mounted() {

    },
    created() {

    }
}


</script>
<style>
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

.pop_recaptue .btn.btn_arrow_none {
    padding: 0 24px;
}

.pop_recaptue .check_ul li .check_list {
    float: none;
    margin: 0 auto;
    padding: 6px 0 7px;
    width: 150px;
}

.pop_recaptue .check_ul li .check_list label {
    float: none;
    display: block;
    margin: 9px 0 8px;
}

.pop_recaptue2 {
    width: 400px;
}

.pop_recaptue2 .pop_cont {
    padding-top: 60px;
    padding-bottom: 55px;
}

.pop_recaptue2 p {
    margin-bottom: 24px;
    line-height: 25px;
}

.pop_recaptue2 .btn_wrap {
    margin: 0;
}
</style>