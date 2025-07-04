<template>
  <div class="modal-mask">
    <div class="modal-wrapper popup pop_data_reg">
      <div class="modal-container">
        <div class="pop_top">
          <h3>데이터 등록</h3>
          <button type="button" class="btn_close b-close" @click="closeMe"><span class="blind">팝업 창닫기</span></button>
        </div>
        <div class="pop_cont">
          <legend>데이터등록 폼</legend>
          <form id="data-form">
            <ul class="list_input round_box">
              <li>
                <label for="">연구번호ID<span class="req"></span></label>
                <input type="text" list="subjects-list" v-model="uploadTrialData.subjectId" @input="filterSubjects"
                  placeholder="연구번호ID를 입력하세요" />
                <datalist id="subjects-list">
                  <option v-for="op in filteredSubjects" :key="op.subjectId" :value="op.subjectId">{{ op.subjectId }}
                  </option>
                </datalist>
              </li>
              <li>
                <label for="">데이터종류<span class="req"></span></label>
                <select v-model="uploadTrialData.dataType">
                  <option value="IMAGING" v-if="thePermissionImaging.createGranted">영상</option>
                  <option value="PUPILLOMETRY" v-if="thePermissionImaging.createGranted">동공측정</option>
                  <option value="FNIRS" v-if="thePermissionImaging.createGranted">fNIRS</option>
                  <option value="MICROBIOME" v-if="thePermissionImaging.createGranted">장내미생물</option>
                </select>
              </li>
              <li>
                <label for="">차수<span class="req"></span></label>
                <select v-model="uploadTrialData.trialIndex">
                  <option v-if="uploadTrialData.dataType != 'IMAGING'" :value="`${trialIndexx[0]}`">{{ trialIndexx[0] }}
                  </option>
                  <option v-else v-for="op in trialIndexx" :key="op" :value="op">{{ op }}</option>
                </select>
              </li>
              <li>
                <label for="">파일<span class="label"></span></label>
                <div class="filebox">
                  <input type='text' readonly class="upload_name" v-model='uploadFileName'>
                  <label for="filename1" class="btn_file" v-if="thePermissionImaging.createGranted">
                    <input type="file" id="filename1" class="upload_hidden" v-on:change="onOneDataFilePicked">
                  </label>
                </div>
              </li>
              <li class="btns">
                <button class="btn btn_violet w_218" type="button" @click="uploadOneFile"
                  v-if="thePermissionImaging.createGranted">업로드</button>
              </li>
            </ul>
            <div class="btn_wrap top">
              <button class="btn btn_full"   type="button" @click="toggle" >Advanced</button>
            </div>
            <div class="btn_wrap btm">
              <ul class="list_input">
                <li>
                  <label for="">데이터파일<span class="label"></span></label>
                  <div class="filebox">
                    <div class="upload_name">{{ uploadFileNames }}</div>
                    <label for="filename2" class="btn_file" v-if="thePermissionImaging.createGranted">
                      <input type="file" id="filename2" class="upload_hidden" @change="onDataFilePicked" multiple>
                    </label>
                  </div>
                </li>
                <li class="btns">
                  <button class="btn btn_violet w_218" type="button" @click="uploadFiles"
                    v-if="thePermissionImaging.createGranted">업로드</button>
                </li>
              </ul>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, defineEmits, defineProps, getCurrentInstance, onMounted, ref } from 'vue';
import VueElementLoading from "vue-element-loading";
import SubjectService from '../services/subject.service'
import ProjectService from '../services/project.service'
// import TextAutocomplete from '../components/TextAutoComplete.vue';
export default {
  name: "ImagingDataUploadModal",
  components: {
    VueElementLoading
  },
  props: {
    projectId: undefined,
    thePermissionImaging: {
      queryGranted: false,
      createGranted: false,
      deleteGranted: false,
      downloadGranted: false
    },
  },
  emits: ['closed'],
  data() {
    return {
      subjects: [],
      trialIndexx: [],
      uploadTrialData: {
        subjectId: undefined,
        dataType: 'IMAGING',
        trialIndex: undefined,
        uploadFiles: undefined,
        uploadOneFile: undefined
      },
      searchKeyword: '',  // Add this line
      loading: false,
      optionalTrial: false,
      advanceStatus: false,
    }
  },
  computed: {
    filteredSubjects() {
      if (!this.searchKeyword) return this.subjects;
      return this.subjects.filter(subject =>
        subject.subjectId.toLowerCase().includes(this.searchKeyword.toLowerCase())
      );
    },
    uploadFileName() {
      if (this.uploadTrialData.uploadOneFile) {
        return this.uploadTrialData.uploadOneFile.name;
      }
      return "";
    },
    uploadFileNames() {
      const count = (this.uploadTrialData.uploadFiles || []).length;
      if (count <= 0) return "";
      let filename = this.uploadTrialData.uploadFiles[0].name;
      if (count > 1) {
        filename += " (+" + (count - 1) + ")";
      }
      return filename;
    },
  },
  methods: {
    filterSubjects(event) {

      this.searchKeyword = event.target.value;
      if(this.searchKeyword != null){
        this.advanceStatus = true;
      }

    },
    resetForm() {
      document.getElementById("data-form").reset();
      this.uploadTrialData = {
        subjectId: undefined,
        dataType: 'IMAGING',
        trialIndex: undefined,
        uploadFiles: undefined,
        uploadOneFile: undefined
      };
    },
    closeMe() {
      this.loading = false;
      this.resetForm();
      this.$emit('closed', false);
    },
    getTrials(id) {
      if (!this.thePermissionImaging.queryGranted) return;

      ProjectService.getTrials(id).then(
        (result) => {
          if (result.data.succeeded) {
            const list = [];
            result.data.data.forEach(e => {
              list.push(e.trialIndex);
            });
            this.trialIndexx = list;
            console.log("trialIndexx[0] : " + this.trialIndexx[0]);
          }
        },
        (error) => {
          console.error(error);
        }
      );
    },
    searchSubjects(data) {
      if (!this.thePermissionImaging.createGranted) return;
      console.log(data.subjectId);
      const filter = { projectId: this.projectId, subjectId: this.uploadTrialData.subjectId, page: 1, offset: 10000, sortBy: null };
      console.log("filterSubject: :" + filter.subjectId);
      SubjectService.searchSubjects(filter).then(
        result => {
          console.log("result :" + result);
        }
      );
    },
    subjectSelected(item) {
      console.log("value :" + item.value);
    },
    getSubjects(id) {
      if (!this.thePermissionImaging.queryGranted) return;
      const filter = { projectId: id };
      SubjectService.getAllSubjects(filter).then(
        result => {
          if (result.data.succeeded) {
            this.subjects = result.data.data;
          }
        },
        error => {
          console.error(error);
        }
      );
    },
    onOneDataFilePicked(event) {
      console.log("event : " + event.target.files[0]);
      this.uploadTrialData.uploadOneFile = event.target.files[0];
      this.uploadTrialData.uploadFiles = [];
    },
    onDataFilePicked(event) {
      console.log("evnets: " + event)

      this.uploadTrialData.uploadFiles = event.target.files;
      console.log("evnets: " + this.uploadTrialData.uploadFiles[0])

      this.uploadTrialData.uploadOneFile = undefined;
    },
    uploadOneImagingDataFile() {
      if (!this.thePermissionImaging.createGranted) return;
      const project = this.projectId;
      const subject = this.uploadTrialData.subjectId;
      const trialIndex = this.uploadTrialData.trialIndex;
      const file = this.uploadTrialData.uploadOneFile;
      const dataType = this.uploadTrialData.dataType;
      let err = "";
      if (!project) {
        err = "- 과제를 선택하세요.\n";
      }
      if (!subject) {
        err += "- 연구번호ID를 선택하세요.\n";
      }
      if (!trialIndex) {
        err += "- 차수를 선택하세요.\n";
      }
      if (!file) {
        err += "- 업로드할 파일을 선택하세요.";
      }
      if (!dataType) {
        err = "- 데이터 종류를 선택하세요.\n";
      }
      if (err) {
        alert(err);
        return;
      }
      const uploadFileForm = new FormData();
      uploadFileForm.append('projectSeq', project);
      uploadFileForm.append('subjectId', subject);
      uploadFileForm.append('trialIndex', trialIndex);
      uploadFileForm.append("file", file);
      uploadFileForm.append("dataType", dataType);
      this.$emit('closed', { uploadFileForms: undefined, uploadFileForm: uploadFileForm });
    },
    uploadOnePupillometryDataFile() {
      if (!this.thePermissionImaging.createGranted) return;
      const project = this.projectId;
      const subject = this.uploadTrialData.subjectId;
      const trialIndex = this.uploadTrialData.trialIndex;
      const file = this.uploadTrialData.uploadOneFile;
      const dataType = this.uploadTrialData.dataType;
      let err = "";
      if (!project) {
        err = "- 과제를 선택하세요.\n";
      }
      if (!subject) {
        err += "- 연구번호ID를 선택하세요.\n";
      }
      if (!trialIndex) {
        err += "- 차수를 선택하세요.\n";
      }
      if (!file) {
        err += "- 업로드할 파일을 선택하세요.";
      }
      if (!dataType) {
        err = "- 데이터 종류를 선택하세요.\n";
      }
      if (err) {
        alert(err);
        return;
      }
      console.log("dataType :" + dataType);
      const uploadFileForm = new FormData();
      uploadFileForm.append('projectSeq', project);
      uploadFileForm.append('subjectId', subject);
      uploadFileForm.append('trialIndex', trialIndex);
      uploadFileForm.append("file", file);
      uploadFileForm.append("dataType", dataType);
      this.$emit('closed', { uploadFileForms: undefined, uploadFileForm: uploadFileForm });
    },
    uploadOneFnirsDataFile() {
      if (!this.thePermissionImaging.createGranted) return;
      const project = this.projectId;
      const subject = this.uploadTrialData.subjectId;
      const trialIndex = this.uploadTrialData.trialIndex;
      const file = this.uploadTrialData.uploadOneFile;
      const dataType = this.uploadTrialData.dataType;
      let err = "";
      if (!project) {
        err = "- 과제를 선택하세요.\n";
      }
      if (!subject) {
        err += "- 연구번호ID를 선택하세요.\n";
      }
      if (!trialIndex) {
        err += "- 차수를 선택하세요.\n";
      }
      if (!file) {
        err += "- 업로드할 파일을 선택하세요.";
      }
      if (!dataType) {
        err = "- 데이터 종류를 선택하세요.\n";
      }
      if (err) {
        alert(err);
        return;
      }
      console.log("dataType :" + dataType);
      const uploadFileForm = new FormData();
      uploadFileForm.append('projectSeq', project);
      uploadFileForm.append('subjectId', subject);
      uploadFileForm.append('trialIndex', trialIndex);
      uploadFileForm.append("file", file);
      uploadFileForm.append("dataType", dataType);
      this.$emit('closed', { uploadFileForms: undefined, uploadFileForm: uploadFileForm });
    },
    uploadOneMicrobiomeDataFile() {
      if (!this.thePermissionImaging.createGranted) return;
      const project = this.projectId;
      const subject = this.uploadTrialData.subjectId;
      const trialIndex = this.uploadTrialData.trialIndex;
      const file = this.uploadTrialData.uploadOneFile;
      const dataType = this.uploadTrialData.dataType;
      let err = "";
      if (!project) {
        err = "- 과제를 선택하세요.\n";
      }
      if (!subject) {
        err += "- 연구번호ID를 선택하세요.\n";
      }
      if (!trialIndex) {
        err += "- 차수를 선택하세요.\n";
      }
      if (!file) {
        err += "- 업로드할 파일을 선택하세요.";
      }
      if (!dataType) {
        err = "- 데이터 종류를 선택하세요.\n";
      }
      if (err) {
        alert(err);
        return;
      }
      console.log("dataType :" + dataType);
      const uploadFileForm = new FormData();
      uploadFileForm.append('projectSeq', project);
      uploadFileForm.append('subjectId', subject);
      uploadFileForm.append('trialIndex', trialIndex);
      uploadFileForm.append("file", file);
      uploadFileForm.append("dataType", dataType);
      this.$emit('closed', { uploadFileForms: undefined, uploadFileForm: uploadFileForm });
    },
    uploadOneFile() {
      if (this.thePermissionImaging.createGranted && this.uploadTrialData.dataType == "IMAGING") {
        this.uploadOneImagingDataFile();
      }
      if (this.thePermissionImaging.createGranted && this.uploadTrialData.dataType == "PUPILLOMETRY") {
        this.uploadOnePupillometryDataFile();
      }
      if (this.thePermissionImaging.createGranted && this.uploadTrialData.dataType == "FNIRS") {
        this.uploadOneFnirsDataFile();
      }
      if (this.thePermissionImaging.createGranted && this.uploadTrialData.dataType == "MICROBIOME") {
        this.uploadOneMicrobiomeDataFile();
      }
    },
    uploadFiles() {
      if (!this.thePermissionImaging.createGranted) return;
      const dataType = this.uploadTrialData.dataType;
      console.log("dataTypes :" + dataType)
      const files = this.uploadTrialData.uploadFiles;

      let fileCount = 0;
      if ((fileCount = (files || []).length) <= 0) {
        alert("- 업로드할 파일들을 선택하세요.");
        return;
      }
      const project = this.projectId;
      const subject = this.uploadTrialData.subjectId;
      const trialIndex = this.uploadTrialData.trialIndex;
      const file = this.uploadTrialData.uploadOneFile;

      let err = "";
      if (!project) {
        err = "- 과제를 선택하세요.\n";
      }
      if (!subject) {
        err += "- 연구번호ID를 선택하세요.\n";
      }
      if (!trialIndex) {
        err += "- 차수를 선택하세요.\n";
      }
      if (!dataType) {
        err = "- 데이터 종류를 선택하세요.\n";
      }
      if (err) {
        alert(err);
        return;
      }

      const items = [];
      for (let i = 0; i < fileCount; i++) {
        const form = new FormData();
        form.append('projectSeq', this.projectId);
        form.append('subjectId', this.uploadTrialData.subjectId);
        form.append("dataType", dataType)
        if (dataType === "PUPILLOMETRY" || dataType === "FNIRS") {
          form.append("file", files[i]);
          form.append("trialIndex", trialIndex);

        } else {
          form.append("files", files[i]);
        }
        items.push(form);
      }
      this.$emit('closed', { uploadFileForms: items, undefined });

      // const items = [];
      //     for (let i=0; i<fileCount; i++) {
      //         const form = new FormData();
      //         form.append('projectSeq', this.projectId);
      //         form.append('subjectId', this.uploadTrialData.subjectId);
      //         form.append("dataType",dataType)
      //         if(dataType === "PUPILLOMETRY" || dataType === "FNIRS"){
      //             form.append("file", files[i]);
      //             form.append("trialIndex",trialIndex);

      //         }else{
      //             form.append("files", files[i]);
      //         }
      //         items.push(form);
      //     }

      //     this.$emit('closed', {uploadFileForms: items, undefined});

    },
    toggle(event) {
      let err = "";
      if(!this.advanceStatus){
        const subject = this.uploadTrialData.subjectId;
        if (!subject) {
        err += "- 연구번호ID를 선택하세요.\n";
      }
      }
      if (err) {
        alert(err);
        return;
      }
      const el = event.target;
      $(el).parent().siblings('.btm').toggle();
    },
  },
  mounted() {
    if (this.projectId) {
      this.getSubjects(this.projectId);
      this.getTrials(this.projectId)
    }
    if (this.subjectId) this.uploadTrialData.subjectId = this.subjectId;
    if (this.trialIndex) this.uploadTrialData.trialIndex = this.trialIndex;



  }

}
</script>

<style scoped></style>