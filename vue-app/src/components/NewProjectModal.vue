<!-- Modal for creating and changing project information. -->
<template>
<div class="modal-mask">
    <div class="modal-wrapper popup pop_project">
      <div class="modal-container">
      <div class="pop_top">
        <h3>과제관리</h3>
        <button
          @click="closeMe"
          type="button"
          class="btn_close b-close">
          <span class="blind">팝업 창닫기</span>
        </button>
      </div>
      <div class="pop_cont">
        <form action="">
          <legend>과제관리 폼</legend>
          <ul class="list_input">
            <li>
              <label for=""><input type="text" v-model="newProject.projectName"/></label>
            </li>
            <li>
              <textarea
                name=""
                id=""
                cols="30"
                rows="10"
                v-model="newProject.description"
              ></textarea>
            </li>
            <!-- <li> 과제타입의 제거 요청으로 인한 임시 주석
              <label for="projecttype">연구과제 타입 *</label> 
              <select name="" id="projecttype" class="w_120" v-model="newProject.projectType" >
                <option v-for="op in options.projectTypes" :value="op.value" :key="op.value" >
                  {{ op.label }}
                </option>
              </select>
            </li> -->
          </ul>
          <div class="btn_wrap ta_c">
            <button
              class="btn btn_sblue btn_lh31 btn_arrow_none"
              type="button"
              v-on:click="addNewProject()"
            >
              저장
            </button>
            <button class="btn btn_lh31 btn_arrow_none" type="button" @click="reset">
              초기화
            </button>
          </div>
        </form>
      </div>
      </div>
    </div>
</div>    
</template>

<script>
import ProjectService from "../services/project.service";

export default {
  name: "NewProjectModal",
  components:{
  },
  props: {
    project: undefined, // project to be changed.
  },
  data() {
    return {
      options: {
        projectTypes: [{value:'', label:"선택하세요."}, {value:'DIAGNOSIS', label:"진단, 예측"}, {value:'TREATMENT', label:"치료, 예방"}],
      },
      newProject: { projectName: "", description: "", id: "" }, // , projectType: "" 과제타입의 제거 요청으로 임시 배열에서 제거
    };
  },
  computed: {
  },
  emits: ['added', 'changed', 'closed'],
  methods: {
    projectTypeSelected(val) {
      this.newProject.projectType = val;
    },
    addNewProject() {
      if (!this.newProject.projectName) {
        alert("과제명을 입력하세요.");
        return;
      }
      /* 과제타입의 제거 요청으로 인한 임시 주석 */
      // if (!this.newProject.projectType) {
      //   alert("과제타입을 선택하세요.");
      //   return;
      // }

      if (this.newProject.id) { // change
        ProjectService.changeProject(this.newProject.id, this.newProject).then(
          (result) => {
            if (result.data.succeeded) {
              this.newProject = { projectName: "", description: "", id: "" },
              this.$emit('changed', result.data.data);
              this.closeMe();            
            }
          },
          (error) => {
            console.error(error);
          }
        );
      }
      else { // add
        ProjectService.createProject(this.newProject).then(
          (result) => {
            if (result.data.succeeded) {
              this.newProject = { projectName: "", description: "", id: "" },
              this.$emit('added', result.data.data);
              this.closeMe();            
            }
          },
          (error) => {
            console.error(error);
          }
        );
      }
    },
    reset() {
        this.newProject = { projectName: "", description: "" };
    },
    closeMe() {
        this.$emit('closed');
    }
  },
  mounted() {
    const projectId = this.project.id;
    this.newProject = this.project;

      /*과제타입의 제거 요청으로 인한 임시 주석 */
    // if (projectId) {
    //   ProjectService.getProjectType(projectId).then((response) => {
    //     if (response.data.succeeded) {
    //       this.newProject.projectType = response.data.data.value;
    //     }
    //   }, 
    //   (error) => {
    //     console.error(error);
    //   });
    // }
  },
};
</script>

<style scoped>
</style>
