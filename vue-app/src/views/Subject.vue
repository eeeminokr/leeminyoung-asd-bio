<template>
	<div id="ContentsDiv">
		<div class="cont_top pt_18">
			<h3 class="tit_bul">대상자정보</h3>
			<div class="inputbox mt_20">
				<ul class="list_input">
					<li>
						<label for="resh_id">연구번호ID</label>
						<input @input="resetTrialData" type="text" id="resh_id" class="w_165" v-model="filters.subjectId" />
					</li>
					<li>
						<label for="task_name">대상군</label>
						<select name="" id="task_name" class="w_206" v-model="filters.projectId">
							<option  value="" v-if="canQuery">::전체::</option>
							<option v-for="op in projects" :value="op.projectSeq" :key="op.projectSeq" >
								{{ op.projectName }}
							</option>
						</select>
					</li>
					<li>
						<label for="part_oz">참여기관</label>
						<select name="" id="part_oz" class="w_206" v-model="filters.orgId" >
							<option value="">::전체::</option>
							<option v-for="op in organizations" :value="op.orgId" :key="op.orgId" >
								{{ op.orgName }}
							</option>
						</select>
					</li>
					<li class="btns fr" v-if="canQuery">
						<button class="btn btn_violet" type="button" v-on:click="searchSubjects">검색</button>
						<button class="btn btn_violet" type="button" @click="searchGender" >추가검색</button>
						<button class="btn btn_gray" type="button" @click="refresh">검색 초기화</button>
					</li>	
				</ul>
				 <ul  v-if="activeGenderTemplate" class="list_input" style="position: relative; left: 563px;">
					<li>
						<label for="gender">성별</label>
						<select @change="searchPick($event)" name="" id="gender" style="width: 100px;" v-model="filters.gender" >
						<!-- <option :value="'M'" :key="'M'"> </option> -->
						<option  :data-key="'ALL'" value="">::전체::</option>
						<option  v-for="g in gender" :value="g.value" :key="g.value" :data-key="g.value">
							{{ g.key }}
						</option>	
						</select>

					</li>
				 </ul>
			</div>
			<div class="btn_wrap mt_18">
				<!-- <div style="display:inline-block" v-if="templateFileDownUrl">
					<a class="btn btn_violet" target="_blank" :href="templateFileDownUrl" title="업로드 파일 양식을 다운로드 합니다.">대상자 업로드 양식</a>
				</div> -->
				<div class="fr">	
					<!-- <button type="button" class="btn btn_violet btn_reg btn_pop_target_reg" v-if="!subjectDeletable && canCreate" v-on:click="openSubjectRegisterPopup">대상자등록</button>
					<button type="button" class="btn btn_violet" v-if="subjectDeletable && canDelete" @click="removeSubjects">대상자삭제</button>					 -->
				</div>
			</div>			
		</div>

		<div class="cont_body mt_18">
			<!-- 테이블이 1900px을 넘는 경우 셀 너비를 px로 지정해야 함 -->
			<table class="tb_background" data-height="605">
				<tbody>
					<tr>
						<td>
							<div class="scroll_head" id="subject_scroll_head">
								<table class="tb_head">
									<colgroup>
										<col width="40px" />
										<col width="140px" />
										<col width="50px" />
										<col width="140px" />
										<col width="165px" />
										<col  />
									</colgroup>
									<thead>
										<tr>
											<th class="sticky" style="left: 0px;">
												<label v-if="canDelete"><input type="checkbox" class="check_all" name="table_check" @click="selectAll" /><span></span></label>
												<!-- <label v-if="canDelete"><input type="checkbox" name="table_check" v-model="selectedItems" :id="item.id" :value="item" /><span></span></label> -->
											</th>
											<th class="sticky">연구번호ID</th>
											<th class="sticky">성별</th>
											<th class="sticky">생년월일</th>
											<th class="sticky border_dark">참여기관</th>
											<th class="sticky">비  고</th>
										</tr>
									</thead>
								</table>
							</div>
							<div class="scroll_body ps ps--active-x ps--active-y" style="max-height: 605px;" v-on:scroll="onTableBodyScroll">
								<table class="tb_body">
									<colgroup>
										<col width="40px" />
										<col width="140px" />
										<col width="50px" />
										<col width="140px" />
										<col width="165px" />
										<col  />
									</colgroup>
									<tbody>
										<tr v-for="item in subjects" :key="item.subjectId">
											<td class="sticky" style="left: 0px;">
												<label v-if="canDelete"><input type="checkbox" name="table_check" v-model="selectedItems" :id="item.id" :value="item" /><span></span></label>
											</td>
											<td class="sticky">{{item.subjectId}}</td>
											<td class="sticky">{{item.gender == 'F' ? '여' : '남'}}</td>
											<td class="sticky">{{item.birthDay}}</td>											
											<td class="sticky border_dark">{{item.orgName}}</td>
												<td>
												<input type="text" style="width:90%;" v-model="item.note">
												<button style="margin:4px;" @click="saveNote(item)"><font-awesome-icon icon="save" size="lg" /></button>
											</td>
										</tr>
										<tr v-if="subjects.length <= 0">
											<td :colspan="7">
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
			<new-subject-modal v-if="canCreate && showNewSubjectModal" 
				:projects="projects" 
				:organizations="organizations" 
				:orgId="filters.orgId" 
				:projectId="filters.projectId" 
				@closed="onSubjectRegisterPopupClosed"
				@added="onSubjectAdded" 
				@addedManySubjects="onAddedManySubjects">
			</new-subject-modal>
		</transition>
	</div>
</template>

<script>
	import axios from 'axios';
	import PerfectScrollbar from '../assets/js/perfect-scrollbar';
	import { constants, utils, FUNCTION_NAMES } from '../services/constants';
	import AlertService from '../services/alert.service';
	import AppService from '../services/app.service';
	import OrganizationService from '../services/organization.service';
	import MemberService from "../services/member.service";
	import SubjectService from "../services/subject.service";
	import ProjectService from "../services/project.service";
	import NewSubjectModal from '../components/NewSubjectModal.vue';
	import ListPaging from '../components/ListPaging.vue';

	const subjectRegisterPopupFnc = function () {
		$("#subject_reg_popup").bPopup();
	};

	const initScrollbar = function() {
		$('.scroll_body').each(function(){
			const ps = new PerfectScrollbar($(this)[0]);
		});
	}

	const tableBodyScrollFnc = function() {
		$('.scroll_body').scroll(function () {
			var xPoint = $('.scroll_body').scrollLeft();
			$('.scroll_head').scrollLeft(xPoint);
		});
	}

	const INT_VALUE_COLS = [
		"Guardian_Age",
		"Guardian_countOfMeetings",
	];

	const ROWS = 20;
	const FUNCTION_NAME = FUNCTION_NAMES.SUBJECT_MNG;

	export default {
		name: "Subject",
		props: {},
		components: {
			NewSubjectModal,
			ListPaging
		},
		data() {
			return {				
				projects: [],
				organizations: [],
				subjects: [],
				donationAgreePdfFiles: [],
				filters: {
					projectId: "",
					subjectId: "",
					orgId: "",
					gender: "",
					page: 1,
					offset: ROWS,
				},
				selectedItems: [],
				paging: {totalCount: 0, pageNo: 1},
				theProjectId: undefined,
				templateFileDownUrl: undefined,
				showNewSubjectModal: false,
				thePermission: {
					queryGranted: false,
					createGranted: false,
					deleteGranted: false,
					downloadGranted: false,
					queryAllGranted: false,
				},
				rolePermission: {
					queryGranted: false,
					createGranted: false,
					deleteGranted: false,
					downloadGranted: false,
					queryAllGranted: false,
				},
				searchsubjectId: false,
				gender: [
                { value: "M", key: "남자" },
                { value: "F", key: "여자" },
	          ],
			 activeGenderTemplate: false,	
			 vaildgender : false //page reset을 위한 select options vaildation check 변수 boolean
			};
		},
		computed: {			
			/**
			 * 조회 권한이 있는가?
			 */
			canQuery() {
				return true;
			},
			canQueryAll() {
				return this.rolePermission.queryAllGranted;
			},
			canOrgDataAll() {
				return this.rolePermission.queryOrgDataGranted;
			},
			/**
			 * 등록 권한이 있는가?
			 */
			canCreate() {
				return this.thePermission.createGranted;
			},
			/**
			 * 삭제 권한이 있는가?
			 */
			canDelete() {
				return this.thePermission.deleteGranted;
			},			
			/**
			 * 다운로드 권한이 있는가?
			 */
			canDownload() {
				return this.thePermission.downloadGranted;
			},
			subjectDeletable() {
				return (this.selectedItems || []).length > 0;
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
		methods: {
			getData(item, column){
				if ((item.mcdData || []).length <= 0 || !item.mcdData[0]) return "";

				let value = item.mcdData[0][column];

				if (INT_VALUE_COLS.includes(column)) {
					value = utils.formatInt(value);
				}
				
				return value;
			},
			selectAll(event) {
				if (!this.canDelete) return;

				const checked = event.target.checked;
				if (checked) {
					const items = [];
					this.subjects.forEach(n=>{
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
                this.searchSubjects();
           		 }		
			},
			refresh() {
				this.filters = {
					projectId: "",
					subjectId: "",
					orgId: "",
					gender:"",
					page: 1,
					offset: ROWS,
				};
				this.searchSubjects();
			},
			searchSubjects() {
				this.subjects = [];
				this.selectedItems = [];
				this.theProjectId = this.filters.projectId;


				if (this.theProjectId) {
					MemberService.getAuthMemberProjectPermission(this.theProjectId, FUNCTION_NAME).then(result => {
						if (result.data.succeeded) {
							this.thePermission = result.data.data;

							const canQuery = result.data.data.queryGranted;

						
							if (!canQuery) {
								alert ('데이터를 조회할 사용권한이 없습니다.');
								return;
							}
							
							if (this.filters.page <= 0 || !this.filters.page) this.filters.page = 1;
					
							if(this.searchsubjectId=== true){
                            this.filters.projectId = undefined;
                            this.filters.orgId = undefined;
                        
                            }

							SubjectService.searchSubjects(this.filters).then(
								result=>{
									if (result.data.succeeded) {
										this.subjects = result.data.data.items;


										const seq = result.data.data.items.map(v => v.projectSeq); //연구id 값 서칭 후 select option 값 지정
                                        const org = result.data.data.items.map(v => v.orgId);

                                        if(this.searchsubjectId){
                                            this.filters.projectId = seq[0];
                                        this.filters.orgId = org[0];
                                        this.searchsubjectId = false;
                                        }

										this.paging = {totalCount: result.data.data.total, pageNo: this.filters.page};
									}
									else {
										this.paging = {totalCount: 0, pageNo: this.filters.page};
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
				}
				else {
					this.thePermission = {
						queryGranted: false,
						createGranted: false,
						deleteGranted: false,
						downloadGranted: true,
						queryAllGranted: false,
					};
					
					if (this.filters.page <= 0 || !this.filters.page) this.filters.page = 1;	
					
					if(this.searchsubjectId=== true){
                            this.filters.projectId = undefined;
                            this.filters.orgId = undefined;
                        
                            }

					SubjectService.searchSubjects(this.filters).then(
						result=>{
							if (result.data.succeeded) {
								this.subjects = result.data.data.items;


									const seq = result.data.data.items.map(v => v.projectSeq); //연구id 값 서칭 후 select option 값 지정
                                	const org = result.data.data.items.map(v => v.orgId);
                                        

                                        if(this.searchsubjectId){
                                        
										this.filters.projectId = seq[0];
                                        this.filters.orgId = org[0];
                                        this.searchsubjectId = false;
                                        
										}


								this.paging = {totalCount: result.data.data.total, pageNo: this.filters.page};
							}
							else {
								this.paging = {totalCount: 0, pageNo: this.filters.page};
							}
						}, 
						error=>{
							console.error(error);
					});
				}
			},
			removeSubjects() {
				console.debug(`${this.canDelete} ${this.selectedItems}`);
				if (!this.canDelete || !this.selectedItems || !confirm('선택한 대상자를 삭제하겠습니까?')) return;

				const list = this.selectedItems.slice();

				SubjectService.deleteSubjects(list).then(
					results=>{
						let count = 0;
						results.forEach(val=>{
							if (val.data.succeeded) count++;
						});

						this.searchSubjects();
						AlertService.info(`선택한 대상자를 삭제했습니다. (${count}/${list.length}건)`);
					}, 
					error=>{
						console.error(error);
					});
				this.selectedItems = [];
			},
			saveNote(subject) {
				const data = {
					//projectSeq: this.theProjectId,
					projectSeq: subject.projectSeq,
					subjectId: subject.subjectId,
					orgId: subject.orgId,
					note: subject.note
				};
				 
				
				SubjectService.changeSubject(subject.projectSeq, subject.subjectId, data).then(result=>{
						if (result.data.succeeded) {
							AlertService.info('메모를 저장했습니다.');
						}
						else {
							AlertService.error('메모를 저장하지 못했습니다. (' + result.data.message + ")");
						}
					}, 
					error=>{
						console.error(error);
					});
			},
			openSubjectRegisterPopup() {
				this.showNewSubjectModal = true;
			},
			onSubjectRegisterPopupClosed() {
				this.showNewSubjectModal = false;
			},
			onTableBodyScroll() {
				tableBodyScrollFnc();
			},
			onSubjectAdded(data) {
				this.searchSubjects();
				this.showNewSubjectModal = false;
			},
			onAddedManySubjects() {
				this.searchSubjects();
				this.showNewSubjectModal = false;
			},
			onPagingClick(page) {
				this.filters.page = page;
				this.searchSubjects();
			},
			resetTrialData() {

			let subjectIdvaild = true;

			subjectIdvaild = utils.validateSubjectIdFormat(this.filters.subjectId)

			if(this.filters.subjectId){
			if(subjectIdvaild===false){

			return  AlertService.info("연구번호ID를 정확하게 기입하여 주십시오.");
			}else{
			this.searchsubjectId = true;
			this.searchSubjects();
			}

			}else{
				this.getProjects();
			}

			},
			

			getProjects(){
				const requests = [
						ProjectService.getAsdProjects(),
						AppService.getSystemSetting(constants.SUBJECT_UPLOAD_TEMPLATE_FILE_URL),
						// QC 유저 권한 임시 수정
						ProjectService.getAsdOrganizations(9999),
					];
					axios.all(requests).then(results=>{		
						if (results[0].data.succeeded) {
							this.projects = results[0].data.data;
						// 	if(this.canQuery){
						// 		this.filters.projectId = this.projects.length > 0 ? "" : undefined;
						// 	}else{
						// this.filters.projectId = this.projects.length < 0 ? "" : this.projects[0].projectSeq;
						// 	}
						this.filters.projectId = this.projects.length > 0 ? "" : undefined;
						}						
						if (results[1].data.succeeded) {
							this.templateFileDownUrl = results[1].data.data.value;
						}
						if (results[2].data.succeeded) {
							this.organizations = results[2].data.data;
						}
						this.filters.page = 1;
						this.searchSubjects();
					}, 
					error=>{ console.error(error); });
			}
		},
		mounted() {
			initScrollbar();
		},
		created() {
			
			MemberService.getAuthMemberFunctionPermission(FUNCTION_NAME).then(result => {
				if (result.data.succeeded) {
					this.rolePermission = result.data.data;

					console.log(this.rolePermission)
					this.canQueryAll = this.rolePermission.queryAllGranted
					this.canOrgDataAll = this.rolePermission.queryOrgDataGranted
				
					console.log(this.canQueryAll)
					console.log(this.canOrgDataAll)
					this.getProjects();
				
				}
			});
		}
	};
</script>

<style scoped>
	
</style>
