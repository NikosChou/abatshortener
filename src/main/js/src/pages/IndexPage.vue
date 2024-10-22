<template>
  <q-page class="row items-center justify-evenly">
    <q-card class="q-pa-md">
      <q-card-section class="text-h3 text-weight-bolder">Paste the URL to be shortened</q-card-section>
      <q-card-section class="row full-width">
        <q-input class="col-10" input-class="text-weight-bold text-h5 text-primary" bg-color="white"
                 :label="url ?undefined: 'https://example.com'"
                 @keydown.enter="shortUrl"
                 clearable
                 v-model="url"></q-input>
        <q-btn
          v-if="!longUrl"
          class="col-2 self-center" size="lg" color="primary"
          :disable="!isUrlValid"
          @click="shortUrl"
        >Short
        </q-btn>
        <q-btn
          v-if="longUrl"
          class="col-2 self-center" size="lg" color="primary"
          @click="copyUrl"
        >copy
        </q-btn>
      </q-card-section>
      <q-card-section v-if="longUrl">
        Long url is: <span class="text-weight-bold">{{ longUrl }}</span>
      </q-card-section>
      <q-card-actions class="row justify-between">
        <q-toggle class="cols-auto" label="More" v-model="moreOptions"></q-toggle>
        <q-btn class="cols-auto" no-caps flat color="primary" @click="reload">reset</q-btn>
      </q-card-actions>
      <q-card-section v-if="moreOptions" class="row justify-between">
        <q-input class="cols-auto self-end" filled input-class="text-weight-bold text-h5 text-white" label-color="white"
                 bg-color="primary"
                 :label="customCode ? undefined: 'code'"
                 clearable v-model="customCode"></q-input>
        <div class="cols-auto">
          <q-btn-toggle
            class="q-mb-md"
            v-model="ttlVar"
            push
            toggle-color="primary"
            :options="[
          {label: 'Sec', value: 'S'},
          {label: 'Min', value: 'm'},
          {label: 'Hour', value: 'H'},
          {label: 'Days', value: 'D'}
        ]"
          />
          <q-input  filled input-class="text-weight-bold text-h5 text-white" label-color="white"
                   bg-color="primary"
                    type="number"
                   :label="ttlQuantity ? undefined: '10'"
                   clearable v-model="ttlQuantity"></q-input>
        </div>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import {computed, ref} from "vue";
import {api} from "../boot/axios";
import {useQuasar} from "quasar";

const urlRegex = /^((?:https?|ftp):\/\/)(?:www\.)?[a-z0-9-]+(?:\.[a-z0-9-]+)+[^\s]*$/i;
const url = ref('https://example.com')
const isUrlValid = computed(() => url.value !== undefined && urlRegex.test(url.value))
const longUrl = ref()
const $q = useQuasar()
const moreOptions = ref(false)
const customCode = ref()
const ttlVar = ref()
const ttlQuantity = ref()
const ttl = computed(() => {
  if (ttlVar.value && ttlQuantity.value) {
    return `PT${ttlQuantity.value}${ttlVar.value}`
  }
  return undefined
})

function shortUrl() {
  if (isUrlValid.value) {
    api.post('/api/v1/short', {url: url.value, code: customCode.value, ttl: ttl.value})
      .then(({data}) => {
        longUrl.value = data.url
        url.value = `${location.protocol}//${location.host}/${data.code}`
        customCode.value = undefined
        ttlVar.value = undefined
        ttlQuantity.value = undefined
        moreOptions.value = false
      })
  }
}

function copyUrl() {
  navigator.clipboard.writeText(url.value)
  $q.notify({message: 'url copied', color: "primary"})
}

function reload() {
  location.reload()
}
</script>
