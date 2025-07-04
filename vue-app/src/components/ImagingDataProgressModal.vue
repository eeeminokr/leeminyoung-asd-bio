<template>
    <div class="modal-mask">
        <div class="modal-wrapper popup" style="margin-left:70%; margin-top:5%;width:400px; z-index: 9999; opacity: 1;">
            <div class="modal-container">
                <div class="pop_top">
                    <h3>데이터 처리 진행율</h3>
                    <button type="button" class="btn_close b-close" @click="close(true)">
                        <span class="blind">팝업 창닫기</span>
                    </button>
                </div>
                <div class="pop_cont">
                    <legend>데이터 파일 목록</legend>
                    <div v-for="(item, index) in progress" :key="index">
                        <span class='file-name'>{{ item.fileName }} <button v-if="item.error" type="button" title="재시도"
                                class="btn_retry retry" @click="retry(index)"><font-awesome-icon
                                    icon="redo" /></button></span>
                        <div class="progress">
                            <span :class="{ 'error': item.error, 'done': item.done }" role="progressbar"
                                :aria-valuenow="item.percentage" aria-valuemin="0" aria-valuemax="100"
                                :title="item.error" :style="{ width: item.percentage + '%' }">
                                <span v-if="item.error">오류: {{ item.error }}</span>
                                <span v-else-if="item.done">완 료</span>
                                <span v-else>{{ item.percentage }}%</span>
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import TrialService from "../services/trial.service";

export default {
    name: "ImagingDataProgressModal",
    components: {
        FontAwesomeIcon,
    },
    props: {
        oneFileForm: undefined,
        manyFileForms: {
            type: Array,
        }
    },
    data() {
        return {
            progress: [],
            progressIds: {},
            fileForms: [],
            multipleFiles: false,
            subject: undefined
        }
    },
    computed: {
    },
    watch: {
    },
    emits: ['fileUploaded'],
    methods: {
        close(result,subject) {
            // Clear interval (getting progress) when error raises.
            for (let k in this.progressIds) {
                clearInterval(this.progressIds[k]);
            }
            if(subject == null){
                subject = this.subject;
            }
            this.fileForms = [];
            this.progress = [];
            this.$emit('fileUploaded', result,subject);
        },
        updateProgress(index, percentage, error) {
            if (!this.progress[index]) return;

            this.progress[index].percentage = parseInt(percentage, 10);

            if (error) {
                this.progress[index].error = error;
            }

            // Check if all upload completed without errors.
            if (percentage == 100) {
                // Clear interval if it is done.
                const id = this.progressIds[index];
                if (id) {
                    clearInterval(id);
                }

                let succeeded = true;
                for (let i = this.progress.length - 1; i >= 0; i--) {
                    const val = this.progress[i];
                    if (val.percentage !== 100 || val.error) {
                        succeeded = false;
                    }
                }

                if (succeeded) {
                    setTimeout(() => {
                        this.close(true,this.subject);
                    }, 2000);
                }
            }
        },
        retry(index) {
            const form = this.fileForms[index];
            this.uploadOneFile(form, index);
        },
        uploadOneFile(form, index) {
            console.log(form.get('subjectId'))
            this.subject = form.get('subjectId');
            console.log(form.get('file'))
            console.log(this.multipleFiles)
            console.log(form.get('dataType'))
            let file = '';
            if (form.get('dataType') == 'IMAGING') {
                file = this.multipleFiles ? form.get('files') : form.get('file');
            } else {
                const pickfile = this.multipleFiles ? form.get('file') : form.get('file');
                console.log("pickfile :" + pickfile.name)
                file = this.multipleFiles ? form.get('file') : form.get('file');
            }


            console.log(file.name)

            // const file = this.multipleFiles ? form.get('files') : form.get('file');
            const taskId = file.name + index + Date.now();

            const dataType = form.get('dataType');
            console.log("dataType[promodal]" + dataType)


            this.progress[index] = {
                fileName: file.name,
                percentage: 0,
                taskId: taskId,
                error: undefined,
                done: false,
            };

            if (dataType === 'PUPILLOMETRY') {
                this.uploadPupillometryDataFile(taskId, form,index);
            } else if (dataType === 'FNIRS') {
                this.uploadFnirsDataFile(taskId, form,index);
            } else if (dataType === 'MICROBIOME') {
                this.uploadMicrobiomeDataFile(taskId, form,index);
            } else {
                this.uploadImagingDataFile(taskId, form,index);
            }
        },
        uploadPupillometryDataFile(taskId, form,index) {
            if ((form || []).length <= 0) return;
            if (taskId == null) return;

            TrialService.uploadPupillometryDataFile(
                taskId,
                form,
                this.multipleFiles,
                (event) => {
                    const percentage = Math.round(100 * event.loaded / event.total / 10);

                    // Reset session timeout due to this upload might take long time.
                    this.$store.dispatch('session/reset');

                    this.updateProgress(index, percentage);

                    console.log("percentage :" + percentage);
                    // Start getting process status after uploading.
                    if (event.loaded >= event.total) {
                        setTimeout(() => { this.getProgress(taskId, index); }, 3000);
                    }
                })

                .then(
                    result => {

                        if (result.data.succeeded) {
                            setTimeout(() => {
                                if (this.progress[index]) this.progress[index].done = true;
                                this.updateProgress(index, 100);
                            }, 3000);
                        }
                        else {
                            setTimeout(() => {
                                this.updateProgress(index, 100, result.data.message);
                            }, 3000);
                        }
                    },
                    error => {
                        this.updateProgress(index, 100, error.message);
                        console.error(error);
                    });
        },
        uploadFnirsDataFile(taskId, form,index) {
            if ((form || []).length <= 0) return;
            if (taskId == null) return;
            TrialService.uploadFnirsDataFile(
                taskId,
                form,
                this.multipleFiles,
                (event) => {
                    const percentage = Math.round(100 * event.loaded / event.total / 10);

                    // Reset session timeout due to this upload might take long time.
                    this.$store.dispatch('session/reset');

                    this.updateProgress(index, percentage);

                    console.log("percentage :" + percentage);
                    // Start getting process status after uploading.
                    if (event.loaded >= event.total) {
                        setTimeout(() => { this.getProgress(taskId, index); }, 3000);
                    }
                })

                .then(
                    result => {

                        if (result.data.succeeded) {
                            setTimeout(() => {
                                if (this.progress[index]) this.progress[index].done = true;
                                this.updateProgress(index, 100);
                            }, 3000);
                        }
                        else {
                            setTimeout(() => {
                                this.updateProgress(index, 100, result.data.message);
                            }, 3000);
                        }
                    },
                    error => {
                        this.updateProgress(index, 100, error.message);
                        console.error(error);
                    });
        }, uploadMicrobiomeDataFile(taskId, form,index) {
            if ((form || []).length <= 0) return;
            if (taskId == null) return;
            TrialService.uploadMicrobiomeDataFile(
                taskId,
                form,
                this.multipleFiles,
                (event) => {
                    const percentage = Math.round(100 * event.loaded / event.total / 10);

                    // Reset session timeout due to this upload might take long time.
                    this.$store.dispatch('session/reset');

                    this.updateProgress(index, percentage);

                    console.log("percentage :" + percentage);
                    // Start getting process status after uploading.
                    if (event.loaded >= event.total) {
                        setTimeout(() => { this.getProgress(taskId, index); }, 3000);
                    }
                })

                .then(
                    result => {

                        if (result.data.succeeded) {
                            setTimeout(() => {
                                if (this.progress[index]) this.progress[index].done = true;
                                this.updateProgress(index, 100);
                            }, 3000);
                        }
                        else {
                            setTimeout(() => {
                                this.updateProgress(index, 100, result.data.message);
                            }, 3000);
                        }
                    },
                    error => {
                        this.updateProgress(index, 100, error.message);
                        console.error(error);
                    });
        }, uploadImagingDataFile(taskId,form,index) {
            if ((form || []).length <= 0) return;
            if(taskId == null) return;
            TrialService.uploadDataFiles(
                taskId,
                form,
                this.multipleFiles,
                (event) => {
                    const percentage = Math.round(100 * event.loaded / event.total / 10);

                    // Reset session timeout due to this upload might take long time.
                    this.$store.dispatch('session/reset');

                    this.updateProgress(index, percentage);

                    // Start getting process status after uploading.
                    if (event.loaded >= event.total) {
                        setTimeout(() => { this.getProgress(taskId, index); }, 3000);
                    }
                })
                .then(
                    result => {
                        // process completed.
                        if (result.data.succeeded) {
                            setTimeout(() => {
                                if (this.progress[index]) this.progress[index].done = true;
                                this.updateProgress(index, 100);
                            }, 3000);
                        }
                        else {
                            setTimeout(() => {
                                this.updateProgress(index, 100, result.data.message);
                            }, 3000);
                        }
                    },
                    error => {
                        this.updateProgress(index, 100, error.message);
                        console.error(error);
                    });
        },

        uploadFiles() {
            if ((this.fileForms || []).length <= 0) return;

            this.errorCount = 0;
            console.log(this.fileForms)
            this.fileForms.forEach((form, index) => {
                this.uploadOneFile(form, index);
            });
        },
        getProgress(taskId, index) {
            const interval = 500;
            const updateCallback = this.updateProgress;

            // 이미 작업을 시작했으면 리턴.
            if (this.progressIds[index] > 0) return;

            const id = setInterval(function () {
                TrialService.getProgress(taskId).then(result => {
                    if (result.data.succeeded) {
                        const percentage = result.data.data.data.processedTask / result.data.data.data.totalTask * 100;
                        updateCallback(index, percentage > 100 ? 100 : percentage);
                    }
                },
                    error => {
                        console.error(error);
                    });
            }, interval);

            this.progressIds[index] = id;
        },
    },
    mounted() {
        if (this.oneFileForm) {
            this.errorCount = 0;
            this.multipleFiles = false;
            this.fileForms.push(this.oneFileForm);
            this.uploadFiles();
            //this.uploadOneFile(this.oneFileForm, 0);
        }
        else if ((this.manyFileForms || []).length > 0) {
            this.errorCount = 0;
            this.multipleFiles = true;
            this.fileForms = this.manyFileForms;
            this.uploadFiles();
        }
    }
};
</script>

<style scoped>
.file-name {
    font-size: 12pt;
}

.progress {
    box-sizing: content-box;
    height: 25px;
    line-height: 25px;
    position: relative;
    margin: 10px 0 20px 0;
    background: #555;
    border-radius: 25px;
    padding: 0px;
    font-size: small;
    text-align: center;
    box-shadow: inset 0 -1px 1px rgba(255, 255, 255, 0.3);
}

.progress>span {
    display: block;
    height: 100%;
    border-top-right-radius: 8px;
    border-bottom-right-radius: 8px;
    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    background-color: rgb(43, 194, 83);
    background-image: linear-gradient(center bottom,
            rgb(43, 194, 83) 37%,
            rgb(84, 240, 84) 69%);
    box-shadow: inset 0 2px 9px rgba(255, 255, 255, 0.3),
        inset 0 -2px 6px rgba(0, 0, 0, 0.4);
    position: relative;
    overflow: hidden;
}

.progress>span:after,
.animate>span>span {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    bottom: 0;
    right: 0;
    background-image: linear-gradient(-45deg,
            rgba(255, 255, 255, 0.2) 25%,
            transparent 25%,
            transparent 50%,
            rgba(255, 255, 255, 0.2) 50%,
            rgba(255, 255, 255, 0.2) 75%,
            transparent 75%,
            transparent);
    z-index: 1;
    background-size: 50px 50px;
    animation: move 2s linear infinite;
    border-top-right-radius: 8px;
    border-bottom-right-radius: 8px;
    border-top-left-radius: 20px;
    border-bottom-left-radius: 20px;
    overflow: hidden;
}

.animate>span:after {
    display: none;
}

@keyframes move {
    0% {
        background-position: 0 0;
    }

    100% {
        background-position: 50px 50px;
    }
}

.retry {
    padding: 8px;
    min-width: 20 px;
    height: 21 px;
    line-height: 0px;
}

.progress>span.error {
    background-color: red;
    font-style: italic;
    color: yellow;
    font-weight: 800;
}

.progress>span.done {
    background-color: darkgreen;
    color: bule;
    font-weight: 800;
}

.btn_retry {
    width: 16px;
    height: 100%;
    background: transparent;
    color: '#2165c1';
}
</style>
