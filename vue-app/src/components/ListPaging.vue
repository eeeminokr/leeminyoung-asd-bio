<template>
    <div class="paging" v-if="paging.totalCount>0">
        <span v-if="!prevBundleNo" class="btn_first disabled"><em class="blind">first</em></span>
        <a v-else class="btn_first" href="#" @click="click(prevBundleNo)"><em class="blind">first</em></a>

        <span v-if="!prevPageNo" class="btn_prev disabled"><em class="blind">prev</em></span>        
        <a v-else class="btn_prev" href="#" @click="click(prevPageNo)"><em class="blind">prev</em></a>

        <template v-for="n in count" :key="n">
            <span class="on" v-if="((firstPageNo + n - 1) == pageNo)">
                {{firstPageNo + n - 1}}
            </span>
            <a v-else href="#" @click="click(firstPageNo + n - 1)">{{firstPageNo + n - 1}}</a>
        </template>

        <span v-if="!nextPageNo" class="btn_next disabled"><em class="blind">first</em></span>
        <a v-else class="btn_next" href="#" @click="click(nextPageNo)"><em class="blind">next</em></a>

        <span v-if="!nextBundleNo" class="btn_last disabled"><em class="blind">first</em></span>
        <a v-else class="btn_last" href="#" @click="click(nextBundleNo)"><em class="blind">last</em></a>
    </div>
</template>
<script>

export default {
    name: "ListPaging",
    props: {
        paging: {totalCount: 0, pageNo: 0},
        rowsPerPage: 0,
    },
    emits: ['click'],
    data() {
        return {            
            firstPageNo: 0,
            lastPageNo: 0,
            pageNo: 0,
            pagingCount: 10,
            prevPageNo: undefined,
            nextPageNo: undefined,
            prevBundleNo: undefined,
            nextBundleNo: undefined
        }
    },
    watch: {        
        paging(newval) {
            

            const totalPages = Math.ceil(newval.totalCount / this.rowsPerPage);

            if ((this.pageNo = newval.pageNo) < 1) {
                this.pageNo = 1;
            }
            else if (this.pageNo > totalPages) {
                this.pageNo = totalPages;
            }

            if (this.totalPages <= this.pagingCount) {
                this.firstPageNo = 1;
                this.lastPageNo = totalPages;
            }
            else {
                const bundle = Math.ceil(this.pageNo / this.pagingCount);
                this.firstPageNo = (bundle - 1) * this.pagingCount + 1;
                this.lastPageNo = this.firstPageNo + this.pagingCount - 1;
                if (this.lastPageNo > totalPages) this.lastPageNo = totalPages;
            }

            this.prevBundleNo = this.firstPageNo > this.pagingCount && this.firstPageNo * this.rowsPerPage < newval.totalCount ? this.firstPageNo - this.pagingCount : undefined;
            this.nextBundleNo = this.lastPageNo >= this.pagingCount && this.lastPageNo * this.rowsPerPage < newval.totalCount ? this.lastPageNo + 1 : undefined;
            this.prevPageNo = this.pageNo - 1 <= 0 ? undefined : this.pageNo - 1;
            this.nextPageNo = (this.pageNo) * this.rowsPerPage >= newval.totalCount ? undefined : this.pageNo + 1;
        }
    },
    computed: {
        count() {
            return this.lastPageNo - this.firstPageNo + 1;
        }
    },
    methods:{
        click(page) {
            this.$emit('click', page);
        }
    },
    mounted() {
    }    
}
</script>
<style scoped>

.disabled {
    border: 1px solid #d6d6d6;
    background-color: #fff;
}

.disabled:hover {
    border: 1px solid #d6d6d6;
}
</style>
