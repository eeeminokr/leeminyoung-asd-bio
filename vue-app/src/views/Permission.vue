<template>
    <div id="ContentsDiv">
      <div class="cont_top pt_18">
          <h3 class="tit_bul">과제설정</h3>
          <ul class="tabs mt_20">
              <li><router-link to="/organization">참여기관관리</router-link></li>
              <li class="active"><router-link to="/permission">권한관리</router-link></li>
              <li><router-link to="/member">참여연구원 관리</router-link></li>
          </ul>
          <div class="inputbox mt_18">
              <ul class="list_input">
                  <li>
                      <label for="power">권한</label>
                      <input type="text" id="power" class="w_206" v-model="newRole.roleId">
                  </li>
                  <li class="last">
                      <label for="user_group">사용자 그룹</label>
                      <input type="text" id="user_group" class="w_206" v-model="newRole.description">
                  </li>
                  <li class="btns">
                      <button v-if="canCreate" class="btn btn_violet" type="button" v-on:click="createNewRole()">등록</button>
                      <button v-if="canDelete" class="btn btn_gray" type="button" v-on:click="removeRole()">삭제</button>
                      <button v-if="canQuery" class="btn btn_gray" type="button" v-on:click="refresh()">초기화</button>
                  </li>
              </ul>
          </div>
      </div>
      
      <div class="cont_body mt_18">
          <div class="box_power">
              <div class="fl">
                  <table class="tb_background" data-height="645">
                      <tbody>
                          <tr>
                              <td>
                                  <div class="scroll_head">
                                      <table class="tb_head">
                                            <colgroup>
                                                <col width="40px">
                                                <col width="140px">
                                                <col width="213px">
                                            </colgroup>
                                            <thead>
                                                <tr>
                                                    <th><label v-if="canDelete"><input type="checkbox" class="check_all" name="table_check" @click="selectAll"><span></span></label></th>
                                                    <th>권한</th>
                                                    <th>사용자 그룹</th>
                                                </tr>
                                            </thead>
                                        </table>
                                    </div>
                                    <div class="scroll_body" style="height: 645px;">
                                        <table class="tb_body">
                                            <colgroup>
                                                <col width="40px">
                                                <col width="140px">
                                                <col width="213px">
                                            </colgroup>
                                            <tbody>
                                                <tr v-for="item in roles" :key="item.roleId" >
                                                    <td>
                                                        <label v-if="canDelete" :class="{'usage-application': item.usage == 'APPLICATION'}">
                                                            <input type="checkbox" name="table_check" v-model="selectedItems" :id="item.roleId" :value="item.roleId" ><span></span>
                                                        </label>
                                                    </td>
                                                    <td>
                                                        <div v-on:click="clickedRole = item" 
                                                            :title = "item.usage == 'APPLICATION' ? '삭제불가능한 권한입니다.' : ''"
                                                            :class="{active: clickedRole?.roleId == item.roleId, application:item.usage == 'APPLICATION'}"
                                                            style="cursor:pointer" class="permission">
                                                            {{item.roleId}}
                                                        </div>
                                                    </td>
                                                    <td>{{item.description}}</td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
              </div>
              
              <div class="fr">
                  <table class="tb_background" data-height="605">
                      <tbody>
                          <tr>
                              <td>                                  
                                    <table class="tb_head">
                                        <colgroup>
                                            <col width="125px">
                                            <col width="115px">
                                            <col width="95px">
                                            <col width="95px">
                                            <col width="95px">
                                            <col width="95px">
                                            <col width="25px">
                                        </colgroup>
                                        <thead>
                                            <tr>
                                                <th rowspan="2">폴더명</th>
                                                <th rowspan="2">프로그램</th>
                                                <th colspan="5">사용권한</th>
                                            </tr>
                                            <tr>
                                                <th>조회</th>
                                                <th>등록</th>
                                                <th>삭제</th>
                                                <th :colspan="2">다운로드</th>
                                            </tr>
                                        </thead>
                                        <tbody v-if="clickedRole" class="tb_body">
                                            <tr>
                                                <td rowspan="2">데이터 등록</td>
                                                <td>대상자 정보</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SUBJECT_MNG', 'queryGranted')">{{showPermission('SUBJECT_MNG', 'queryGranted')}}</span></td>
                                                <!-- <td class="bg_gray"></td> -->
                                                <td><span class="btn_ox" v-on:click="changePermission('SUBJECT_MNG', 'createGranted')">{{showPermission('SUBJECT_MNG', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SUBJECT_MNG', 'deleteGranted')">{{showPermission('SUBJECT_MNG', 'deleteGranted')}}</span></td>
                                                <td colspan="2"><span class="btn_ox" v-on:click="changePermission('SUBJECT_MNG', 'downloadGranted')">{{showPermission('SUBJECT_MNG', 'downloadGranted')}}</span></td>
                                            </tr>
                                            <tr>
                                                <td>임상정보/영상</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('IMAGING', 'queryGranted')">{{showPermission('IMAGING', 'queryGranted')}}</span></td>
                                                <!-- <td><span class="btn_ox" v-on:click="changePermission('IMAGING', 'qcGranted')">{{showPermission('IMAGING', 'qcGranted')}}</span></td> -->
                                                <td><span class="btn_ox" v-on:click="changePermission('IMAGING', 'createGranted')">{{showPermission('IMAGING', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('IMAGING', 'deleteGranted')">{{showPermission('IMAGING', 'deleteGranted')}}</span></td>
                                                <td  colspan="2"><span class="btn_ox" v-on:click="changePermission('IMAGING', 'downloadGranted')">{{showPermission('IMAGING', 'downloadGranted')}}</span></td>
                                            </tr>
                                            <!-- <tr>
                                                <td rowspan="5">데이터 분양</td>
                                                <td>대상자 정보</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_SUBJECT', 'queryGranted')">{{showPermission('DIST_SUBJECT', 'queryGranted')}}</span></td>
                                                <td class="bg_gray"></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_SUBJECT', 'createGranted')">{{showPermission('DIST_SUBJECT', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_SUBJECT', 'deleteGranted')">{{showPermission('DIST_SUBJECT', 'deleteGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_SUBJECT', 'downloadGranted')">{{showPermission('DIST_SUBJECT', 'downloadGranted')}}</span></td>
                                            </tr>
                                            <tr>
                                                <td>영상 정보</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_IMAGING', 'queryGranted')">{{showPermission('DIST_IMAGING', 'queryGranted')}}</span></td>
                                                <td class="bg_gray"></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_IMAGING', 'createGranted')">{{showPermission('DIST_IMAGING', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_IMAGING', 'deleteGranted')">{{showPermission('DIST_IMAGING', 'deleteGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_IMAGING', 'downloadGranted')">{{showPermission('DIST_IMAGING', 'downloadGranted')}}</span></td>
                                            </tr>
                                            
                                            <tr>
                                                <td>데이터 통합</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_MCDDATA', 'queryGranted')">{{showPermission('DIST_MCDDATA', 'queryGranted')}}</span></td>
                                                <td class="bg_gray"></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_MCDDATA', 'createGranted')">{{showPermission('DIST_MCDDATA', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_MCDDATA', 'deleteGranted')">{{showPermission('DIST_MCDDATA', 'deleteGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_MCDDATA', 'downloadGranted')">{{showPermission('DIST_MCDDATA', 'downloadGranted')}}</span></td>
                                            </tr>
                                            <tr>
                                                <td>다운로드내역</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_DOWNLOADHIST', 'queryGranted')">{{showPermission('DIST_DOWNLOADHIST', 'queryGranted')}}</span></td>
                                                <td class="bg_gray"></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_DOWNLOADHIST', 'createGranted')">{{showPermission('DIST_DOWNLOADHIST', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_DOWNLOADHIST', 'deleteGranted')">{{showPermission('DIST_DOWNLOADHIST', 'deleteGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_DOWNLOADHIST', 'downloadGranted')">{{showPermission('DIST_DOWNLOADHIST', 'downloadGranted')}}</span></td>
                                            </tr>
                                            <tr>
                                                <td>시료 관리</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_SAMPLE', 'queryGranted')">{{showPermission('DIST_SAMPLE', 'queryGranted')}}</span></td>
                                                <td class="bg_gray"></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_SAMPLE', 'createGranted')">{{showPermission('DIST_SAMPLE', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_SAMPLE', 'deleteGranted')">{{showPermission('DIST_SAMPLE', 'deleteGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('DIST_SAMPLE', 'downloadGranted')">{{showPermission('DIST_SAMPLE', 'downloadGranted')}}</span></td>
                                            </tr> -->
                                            <tr>
                                                <td rowspan="5">기본설정</td>
                                                <td>과제관리</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_PROJECT', 'queryGranted')">{{showPermission('SETTING_PROJECT', 'queryGranted')}}</span></td>
                                                <!-- <td class="bg_gray"></td> -->
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_PROJECT', 'createGranted')">{{showPermission('SETTING_PROJECT', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_PROJECT', 'deleteGranted')">{{showPermission('SETTING_PROJECT', 'deleteGranted')}}</span></td>
                                                <td  colspan="2"><span class="btn_ox" v-on:click="changePermission('SETTING_PROJECT', 'downloadGranted')">{{showPermission('SETTING_PROJECT', 'downloadGranted')}}</span></td>
                                            </tr>
                                            <tr>
                                                <td>참여기관 관리</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_ORG', 'queryGranted')">{{showPermission('SETTING_ORG', 'queryGranted')}}</span></td>
                                                <!-- <td class="bg_gray"></td> -->
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_ORG', 'createGranted')">{{showPermission('SETTING_ORG', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_ORG', 'deleteGranted')">{{showPermission('SETTING_ORG', 'deleteGranted')}}</span></td>
                                                <td  colspan="2"><span class="btn_ox" v-on:click="changePermission('SETTING_ORG', 'downloadGranted')">{{showPermission('SETTING_ORG', 'downloadGranted')}}</span></td>
                                            </tr>
                                            <tr>
                                                <td>권한 관리</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_PERMISSION', 'queryGranted')">{{showPermission('SETTING_PERMISSION', 'queryGranted')}}</span></td>
                                                <!-- <td class="bg_gray"></td> -->
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_PERMISSION', 'createGranted')">{{showPermission('SETTING_PERMISSION', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_PERMISSION', 'deleteGranted')">{{showPermission('SETTING_PERMISSION', 'deleteGranted')}}</span></td>
                                                <td  colspan="2"><span class="btn_ox" v-on:click="changePermission('SETTING_PERMISSION', 'downloadGranted')">{{showPermission('SETTING_PERMISSION', 'downloadGranted')}}</span></td>
                                            </tr>
                                            <tr>
                                                <td>참여연구원 관리</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_MEMBER', 'queryGranted')">{{showPermission('SETTING_MEMBER', 'queryGranted')}}</span></td>
                                                <!-- <td class="bg_gray"></td> -->
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_MEMBER', 'createGranted')">{{showPermission('SETTING_MEMBER', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_MEMBER', 'deleteGranted')">{{showPermission('SETTING_MEMBER', 'deleteGranted')}}</span></td>
                                                <td  colspan="2"><span class="btn_ox" v-on:click="changePermission('SETTING_MEMBER', 'downloadGranted')">{{showPermission('SETTING_MEMBER', 'downloadGranted')}}</span></td>
                                            </tr>
                                            <tr>
                                                <td>사용자 활동로그</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_ACT_LOG', 'queryGranted')">{{showPermission('SETTING_ACT_LOG', 'queryGranted')}}</span></td>
                                                <!-- <td class="bg_gray"></td> -->
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_ACT_LOG', 'createGranted')">{{showPermission('SETTING_ACT_LOG', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('SETTING_ACT_LOG', 'deleteGranted')">{{showPermission('SETTING_ACT_LOG', 'deleteGranted')}}</span></td>
                                                <td  colspan="2"><span class="btn_ox" v-on:click="changePermission('SETTING_ACT_LOG', 'downloadGranted')">{{showPermission('SETTING_ACT_LOG', 'downloadGranted')}}</span></td>
                                            </tr>
                                            <tr>
                                                <td rowspan="1">Q&A 게시판</td>
                                                <td>Q&A 목록</td>
                                                <td><span class="btn_ox" v-on:click="changePermission('BOARD_QNA', 'queryGranted')">{{showPermission('BOARD_QNA', 'queryGranted')}}</span></td>
                                                <!-- <td class="bg_gray"></td> -->
                                                <td><span class="btn_ox" v-on:click="changePermission('BOARD_QNA', 'createGranted')">{{showPermission('BOARD_QNA', 'createGranted')}}</span></td>
                                                <td><span class="btn_ox" v-on:click="changePermission('BOARD_QNA', 'deleteGranted')">{{showPermission('BOARD_QNA', 'deleteGranted')}}</span></td>
                                                <td  colspan="2"><span class="btn_ox" v-on:click="changePermission('BOARD_QNA', 'downloadGranted')">{{showPermission('BOARD_QNA', 'downloadGranted')}}</span></td>
                                            </tr>
                                        </tbody>
                                        <tbody v-if="!clickedRole">
                                            <tr>
                                                <td colspan="7" class="no-group">
                                                    <div>수정할 권한을 선택하세요.</div>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>                                    
                                </td>
                            </tr>
                        </tbody>
                    </table>
              </div>
          </div>
      </div>
  </div>
</template>

<script>
import AlertService from '../services/alert.service';
import { constants, FUNCTION_NAMES } from '../services/constants';
import RoleService from '../services/role.service';
import MemberService from "../services/member.service";

const FUNCTION_NAME = FUNCTION_NAMES.BOARD_QNA;

export default {
  name: 'Permission',
  props: {
  },
  data () {
    return {
        newRole:{roleId:'', description:''},
        roles:[],
        selectedItems:[],
        selectedFunctions:[],
        clickedRole:null,
        thePermission: {
            queryGranted: false,
            createGranted: false,
            deleteGranted: false,
            downloadGranted: false
        }
    }
  },
  computed: {
    /**
     * 조회 권한이 있는가?
     */
    canQuery() { return this.thePermission.queryGranted; },
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
    canChangePermission() { return this.clickedRole.roleId != 'Admin'; },
    permissions() {
        const items = {};
        (this.selectedFunctions || []).forEach(f=>{
            items[f.functionId] = f;
        });

        return items;
    }
  },
  watch: {
      clickedRole(newVal, oldVal) {
          if (!newVal || newVal?.roleId == oldVal?.roleId) return;

          const id = newVal.roleId;
          this.getFunctions(id);
      }
  },
  methods: {
      selectAll(event) {
          if (!this.canDelete) return;

          const checked = event.target.checked;
          if (checked) {
              const items = [];              
              this.roles.forEach(n=>{
                  // 사용자가 등록한 권한만 (용도 = USER) 삭제 가능.
                  if (n.usage == constants.ROW_USAGE_USER) items.push(n.roleId);
              });
              this.selectedItems = items;
          }
          else {
              this.selectedItems = [];
          }
      },
      createNewRole(){
          if (!this.canCreate) return;

          if (!this.newRole.roleId) {
              alert ('권한명을 입력하세요.');
              return;
          }
          if (!this.newRole.description) {
              alert ('사용자 그룹명을 입력하세요.');
              return;
          }
          RoleService.createRole(this.newRole).then((result)=>{
              if (result.data.succeeded) {
                  this.roles.push(result.data.data);
                  this.newRole = {roleId: '', description: ''};
                
                  AlertService.info('신규 권한을 등록했습니다.');
                  return;
              }
              else {
                  AlertService.error("신규 권한을 등록하지 못했습니다. (" + result.data.message + ")");
                  return;
              }
          }, error=>{
              console.error(error);
          });

      },
      removeRole() {
          if (!this.canDelete || this.selectedItems.length <= 0 || !confirm('선택한 권한을 삭제하겠습니까?')) return;

          const items = this.selectedItems.slice();
          this.selectedItems = [];
          this.clickedRole = undefined;

          RoleService.removeRoles(items).then(responses=>{
              let count = 0;
              responses.forEach(res=>{
                if (res.data.succeeded) {
                    count++;
                }
              });

            this.refresh();
            AlertService.info(`선택한 권한을 삭제했습니다. (${count}/${items.length}건)`);
          });
      },
      refresh() {
          if (!this.canQuery) return;

          this.clickedRole = undefined;
          this.newRole = {roleId: '', description: ''};
          this.selectedItems = [];

          RoleService.searchRoles().then((result)=>{
              if (result.data.succeeded) {
                  this.roles = (result.data.data || []).sort((a, b) => a.level - b.level);
              }
          }, error=>{
              console.error(error);
          });
      },
      onRoleClicked(event, group) {          
          this.getFunctions(group);
      },
      changePermission(functionId, permission) {
          if (!this.canCreate || !this.canChangePermission) return;

          if (this.clickedRole.usage == constants.ROW_USAGE_APPLICATION 
            && !confirm(this.clickedRole.roleId + ' 권한을 변경하면 다수의 사용자에게 심각한 영향을 끼칠 수 있습니다.\n권한을 수정하겠습니까?')) return;
            
            const roleId = this.clickedRole.roleId;
            const fnc = this.permissions[functionId] 
                        || {
                            roleId: roleId,
                            functionId: functionId,
                            queryGranted: false,
                            createGranted: false,
                            deleteGranted: false,
                            downloadGranted: false,
                            qcGranted: false,
                        };                        

            const val = fnc[permission];
            fnc[permission] = !val;

            RoleService.changePermissions(roleId, functionId, fnc).then((result)=>{
                if (result.data.succeeded) {
                    this.getFunctions(roleId);
                }
            }, 
            error=>{
                console.error(error);
            });
      },
      showPermission(functionId, permission) {
          const fnc = this.permissions[functionId];
          if (!fnc) return 'X';

          const val = fnc[permission];
          return val ? 'O' : 'X';
      },
      getFunctions(roleId) {
          this.selectedFunctions = [];
          RoleService.searchFunctions(roleId).then(
              (result) => {
                  if (result.data.succeeded) {
                      this.selectedFunctions = result.data.data;
                  }
              }, 
              error=>{
              console.error(error);
          });
      },
  },
  mounted() {
  },
  created() {
    MemberService.getAuthMemberFunctionPermission(FUNCTION_NAME).then(result => {      
      if (result.data.succeeded) {
          this.thePermission = result.data.data;

          this.refresh();
      }
    },
    error => {
        console.error(error);
    });
  }
}
</script>

<style scoped>
    .permission.application {
        font-style: italic;
    }
    .permission.active {
        font-weight: bold;
        color: red;
    }
    .no-group {
        text-align: center;
        font-size: 12px;
        padding: 2em;
    }
    label.usage-application {
        display: none;
    }
</style>
