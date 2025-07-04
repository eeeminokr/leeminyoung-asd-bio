<template>
    <div class="autocomplete-wrapper">
        <input type="search" placeholder="Enter keyword" class="text" v-model="text" list="items" @focus="finalized = false" @keydown="keyDown">

        <ul class="items" v-if="opened === true">
            <li v-for="(item, i) in computedItems" :value="item.text" :key="item.id" 
                :class="{ 'is-active': i === itemIndex }"
                @click="selected(item)">
                {{ item.text }}
            </li>
        </ul>
    </div>
</template>

<script>
import { computed, defineEmits, defineProps, ref, watchEffect, onMounted, watch } from 'vue';

const LIMIT = 8;

const emits = defineEmits(["selected", 'changed']);

export default {
    props: {
        items: {
            type: Array || undefined,
            required: true
        },
    },
    setup(props) {
        const opened = ref(false);
        const finalized = ref(false);
        const text = ref('');
        const itemIndex = ref(-1);

        const computedItems = computed(() => {
            return (props.items || []).length > LIMIT ? (props.items || []).slice(0, LIMIT) : (props.items || []);
        });

        watch(text, async (newValue, oldValue) => {
            if (newValue.length > 1) {
                if (finalized.value === false) {
                    emits('changed', newValue);
                }
            } else {
                opened.value = false;
            }
            if (newValue !== oldValue) {
                finalized.value = false;
                itemIndex.value = -1;
            }
        });

        watch(computedItems, async (newValue, oldValue) => {
            console.log('items changed', newValue);

            if (newValue.length > 0) {
                opened.value = true;
            } else {
                opened.value = false;
            }
            finalized.value = false;
            itemIndex.value = -1;
        });

        async function selected(item) {
            emits('selected', item);

            text.value = item.text;
            opened.value = false;
            finalized.value = true;
            itemIndex.value = -1;
        }

        async function keyDown(event) {
            if (opened.value === false && event.key === 'Enter') {
                event.preventDefault();
                return;
            }

            if (opened.value === false) {
                return;
            }

            if (event.key === 'ArrowDown') {
                itemIndex.value++;
                if (itemIndex.value >= computedItems.value.length) {
                    itemIndex.value = computedItems.value.length - 1;
                }
            } else if (event.key === 'ArrowUp') {
                itemIndex.value--;
                if (itemIndex.value < 0) {
                    itemIndex.value = 0;
                }
            } else if (event.key === 'Enter') {
                if (itemIndex.value >= 0 && itemIndex.value < computedItems.value.length) {
                    const val = computedItems.value[itemIndex.value];
                    selected(val);
                    event.preventDefault();
                }
            }
        }

        onMounted(() => {
            //console.log('mounted');
        });

        return {
            opened,
            finalized,
            text,
            itemIndex,
            computedItems,
            selected,
            keyDown
        };
    }
};
</script>

<style scoped>
    .autocomplete-wrapper {
        display: grid;
    }
    
    .autocomplete-wrapper .items {
        padding: 4px;
        border: 1px solid #ccc;
        border-radius: 5px;
        background-color: #dfdfdf;
        box-sizing: border-box;
        width: 88%;
        margin-left: 10px;
        height: 200px; 
        line-height: 14px;
        overflow:hidden;
    }

    .autocomplete-wrapper .items li {
        list-style: none;
        padding: 5px;
        cursor: pointer;
        color: #333;
    }

    .autocomplete-wrapper .items li.is-active, 
    .autocomplete-wrapper .items li:hover {
        font-size: 13px;
        font-weight: bold;
        color: #133;
    }
</style>