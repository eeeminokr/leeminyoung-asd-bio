<template>
<div class="modal-mask">
    <div class="modal-wrapper popup pop_target_reg">        
        <div class="modal-container">
			<div class="pop_top">
				<h3>대상자 등록</h3>
				<button type="button" class="btn_close b-close" @click="close">
					<span class="blind">팝업 창닫기</span>
				</button>
			</div>
			<div class="pop_cont">
				<legend>대상자등록 폼</legend>
				<ul class="list_input round_box">
					<li>
						<label for="task_name">대상군 *</label>
						<select name="" id="task_name" class="w_206" v-model="newSubject.projectSeq" >
							<option v-for="op in projects" :value="op.projectSeq" :key="op.projectSeq" >
								{{ op.projectName }}
							</option>
						</select>
					</li>
					<li>
						<label for="task_name">참여기관 *</label>
						<select name="" id="task_name" class="w_206" v-model="newSubject.orgId" >
							<option v-for="op in organizations" :value="op.orgId" :key="op.orgId" >
								{{ op.orgName }}
							</option>
						</select>
					</li>
					<li>
						<label for="">대상자ID<span class="req"><em class="blind">필수</em></span></label>
						<input type="text" class="w_140" v-model="newSubject.subjectId" />
						<button class="btn btn_white" v-on:click="findSubject" > 중복체크 </button>
					</li>
					<li>
						<label for="">성별<span class="req"><em class="blind">필수</em></span></label>
						<div class="radio gender">
							<label><input type="radio" name="radio_gender" value='MALE' v-model="newSubject.gender" /><span>남자</span></label>
							<label><input type="radio" name="radio_gender" value='FEMALE' v-model="newSubject.gender" /><span>여자</span></label>
						</div>
					</li>
					<li>
						<label for="">출생년월<span class="req"><em class="blind">필수</em></span></label>
            			<input type="text" class="w_140" v-model="newSubject.birthday" v-mask="'####-##-##'">
					</li>
					<li class="btns">
						<button class="btn btn_blue w_218" type="button" v-on:click="addNewSubject"> 저장 </button>
					</li>
					<div class="btn_wrap top">
						<button class="btn btn_full" type="button" @click="toggle"> Advanced </button>
					</div>
					<div class="btn_wrap btm" style="display:none;" >
						<form id='frm_file'>
						<ul class="list_input">
							<li>
								<span class="label">대상자파일</span>
								<div class="filebox">
									<input class="upload_name" v-model="subjectUploadExcelFileName" readonly />
									<label for="filename1" class="btn_file" ><span class="blind">파일첨부</span></label>
									<input type="file" id="filename1" class="upload_hidden" accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel" 
											@change="onSubjectExcelFilePicked" />
								</div>
							</li>
							<li>
								<span class="label" title="파일명 형식은 대상자ID.pdf(예:BD-BDP-0001.pdf) 이어야 합니다.">동의서파일</span>
								<div class="filebox">
									<input class="upload_name" readonly v-model="subjectAgreementFileName" />
									<label for="filename2" class="btn_file" ><span class="blind" title="파일명 형식은 대상자ID.pdf(예:BD-BDP-0001.pdf) 이어야 합니다.">파일첨부</span></label>
									<input type="file" multiple id="filename2" class="upload_hidden" @change="onSubjectAgreementFilePicked" />
								</div>
							</li>
							<li class="btns" style="padding-left: 74px;">
								<button style="padding: 0px;" class="btn btn_violet w_218" type="button" v-on:click="uploadFiles" > 업로드 </button>
							</li>
						</ul>
						</form>
					</div>
				</ul>
			</div>
		</div>
	</div>
</div>
</template>

<script>

import AlertService from '../services/alert.service';
import SubjectService from "../services/subject.service";
import moment from 'moment';
import {mask} from 'vue-the-mask';

export default {
  name: "NewSubjectModal",
  directives: {
	  mask
  },
  props: {
    projectId: undefined,
	orgId: undefined,
	projects: {
		type:Array,
	},
	organizations:{
		type:Array,
	},
  },  
  data() {
    return {
      	newSubject: {
			subjectId: undefined,
			gender: 'FEMALE',
        	birthday: this.formatDate(new Date()),
			projectSeq: undefined,
			orgId: undefined,
		},
		subjectUploadExcelFile: undefined,
		subjectAgreementFiles: [],
		newSubjectIdValidated: false,
    };
  },
  computed: {
    subjectUploadExcelFileName() {
      return this.subjectUploadExcelFile?.name;
    },
    subjectAgreementFileName() {
		const count = (this.subjectAgreementFiles || []).length;
        if (count <= 0) return "";

        let filename = this.subjectAgreementFiles[0].name;
        if (count > 1) {
            filename += " (+" + (count - 1) + ")";
        }
        return filename;
    },
  },
  watch: {
	  "newSubject.subjectId"(newval) {
		  // 대상자 ID 수정시 중복체크 미확인으로 재설정
		  this.newSubjectIdValidated = false;
	  }
  },
  emits: ['added', 'addedManySubjects', 'closed'],
  methods: {
    findSubject() {
		const projectid = this.projectId || this.newSubject.projectSeq;
		if (!projectid || !this.newSubject.subjectId) return;

		// 대상자 ID에 '_' 문자 포함 여부 체크
		if (this.newSubject.subjectId.indexOf('_') > 0) {
			alert ("대상자 ID에 '_' 문자를 포함할 수 없습니다.");
			return;
		}

		SubjectService.getSubject(
			projectid,
			this.newSubject.subjectId
		).then(
			(result) => {
				if (
					result.data.succeeded &&
					result.data.data &&
					result.data.data.length > 0
				) {
					alert("동일한 대상자 ID가 존재합니다.");
					this.newSubjectIdValidated = false;
					return;
				}
				
				if (!result.data.succeeded) {
					alert(result.data.message);
					return;
				}
				
				alert("등록가능한 대상자 ID 입니다.");
				this.newSubjectIdValidated = true;
			},
			(error) => {
				console.error(error);
			}
		);
	},
	addNewSubject() {
		let msg = '';
		if (!this.newSubject.projectSeq) msg = '  - 과제명을 선택하세요.\n';
		if (!this.newSubject.orgId) msg += "  - 참여기관을 선택하세요.\n";
		if (!this.newSubject.subjectId) {
			msg += "  - 대상자 ID를 입력하세요.\n";
		}
		else if (this.newSubject.subjectId.indexOf('_') > 0) { // 대상자 ID에 '_' 문자 포함 여부 체크
			msg += "  - 대상자 ID에 '_' 문자를 포함할 수 없습니다.\n";
		}

		if (!this.newSubjectIdValidated) msg += "  - 대상자 ID를 중복체크 확인하세요.\n";
		
		if (msg.length > 0) { alert ("아래 사항들을 확인 후 다시 시도하시기 바랍니다.\n" + msg); return; }

		SubjectService.addNewSubject(
			this.newSubject.projectSeq,
			this.newSubject
		).then(
			(result) => {
				if (result.data.succeeded) {
					this.$emit('added', result.data.data);
					AlertService.info("대상자를 등록했습니다.");

					this.newSubject.subjectId = undefined;
					this.newSubjectIdValidated = false;
				} 
				else {
					AlertService.error("대상자를 등록하지 못했습니다. " + result.data.message);
				}
			},
			(error) => {
				console.error(error);
			}
		);
	},
	resetForm() {
		document.getElementById('frm_file').reset();
	},
    close() {
		this.newSubject.subjectId = undefined;
		this.newSubjectIdValidated = false;

		this.subjectAgreementFiles = [];

        this.$emit('closed');
    },
    onSubjectExcelFilePicked(event) {
		const files = event.target.files;
		this.subjectUploadExcelFile = files[0];
	},
	onSubjectAgreementFilePicked(event) {
		const files = [...event.target.files];
		this.subjectAgreementFiles = files;
	},
	uploadDonationAgreeFiles() {		
		const projectid = this.newSubject.projectSeq;
		if (!projectid) {
			alert ('과제를 선택하세요.');
			return;
		}
		if (this.subjectAgreementFiles.length <= 0) {
			alert ('업로드할 동의서 파일을 선택하세요.');
			return;
		}
		
		const uploadFileForm = new FormData();
		uploadFileForm.append('projectSeq', projectid);
		this.subjectAgreementFiles.forEach(f=>{
			uploadFileForm.append("uploadFiles", f);
		});

		SubjectService.uploadHumanDerivativeDonationAgreementFile(projectid, uploadFileForm).then(response=>{
			if (response.data.succeeded) {
				AlertService.info(`인체유래물 기증 동의서 파일들을 업로드했습니다.`);
			}
			else {
				let error = "";
				(response.data.data || []).forEach(m=>{
					error += m + "\n";
				});
				AlertService.error(error);
			}

			this.subjectAgreementFiles = [];
			this.resetForm();
		}, 
		error=>{ console.error(error); });
	},
	uploadFiles() {
		if ((this.subjectAgreementFiles || []).length > 0) {
			this.uploadDonationAgreeFiles();
		}

		if (this.subjectUploadExcelFile) {
			const form = new FormData();
			form.append("uploadFile", this.subjectUploadExcelFile);

			SubjectService.uploadSubjectExcelFile(form).then(
				(result) => {
					if (result.data.succeeded) {
						const errorCount = result.data.data.errorCount;
                    	let msg = '대상자 목록을 추가했습니다.';

						if (errorCount > 0) {
							let errorMsg = '';
							const cellErrors = result.data.data.errors.slice(0, 20);
							errorMsg = `총 ${errorCount} 오류가 확인되었습니다.<br/>`;
							errorMsg += `상위 ${(cellErrors || []).length}/${errorCount} 오류를 표시합니다.<br/>`;
							(cellErrors || []).forEach(e => {
								errorMsg += `<p>Row: ${e.rowIndex}, Column: ${e.columnName} (${e.message})</p>`;
							});
							AlertService.warn(msg, errorMsg, 1000 * 60 * 5);
						}
						else {
							AlertService.info(msg);
						}

						this.$emit('addedManySubjects');
					}
					else {
						AlertService.error("대상자 파일 업로드 중 오류가 발생했습니다.\n " + result.data.message);
					}
				},
				(error) => {
					console.error(error);
				}
			);
		}
	},  
	formatDate(date) {
			const val = moment(date).format('YYYY-MM-DD');
			return val;
	},
	toggle(event) {
        const el = event.target;
        $(el).parent().siblings('.btm').toggle();
	}
	},
  mounted() {
	  this.newSubjectIdValidated = false;
	  this.rawBirthday = '';
	  this.newSubject.projectSeq = this.projectId;
	  this.newSubject.orgId = this.orgId;
  },
  created() {
  }
};
</script>

<style scoped>
.vdp-datepicker {
		overflow: visible;
		display: inline-block;
	}
.gender {
	font-size: 12px;
}
</style>
