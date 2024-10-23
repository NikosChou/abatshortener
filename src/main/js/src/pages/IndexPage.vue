<template>
  <q-page class="row items-center justify-evenly">
    <q-card class="q-pa-md">
      <q-card-section class="text-h3 text-weight-bolder">Paste the URL to be shortened</q-card-section>
      <q-card-section class="row full-width">
        <q-input class="col-10" input-class="text-weight-bold text-h5 text-primary" bg-color="white"
                 :label="url ?undefined: 'https://example.com'"
                 @keydown.enter="shortUrl"
                 @click="copyIfShort"
                 :clearable="!longUrl"
                 ref="urlInputRef"
                 :rules="[(val: string) => (longUrl !== undefined || urlRegex.test(val) ) || 'Url should be valid, set protocol and domain correct']"
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
      <q-card-section v-if="validUntil">
        Short url valid until: <span class="text-weight-bold">{{ validUntil }}</span>
      </q-card-section>
      <q-card-section v-if="longUrl && !validUntil">
        Short url valid: <span class="text-weight-bold">for ever</span>
      </q-card-section>
      <q-card-section v-if="moreOptions" class="row justify-between">
        <q-input class="cols-auto self-end" filled input-class="text-weight-bold text-h5 text-white" label-color="white"
                 bg-color="primary"
                 :rules="[() => errorMessage === undefined || errorMessage]"
                 :label="customCode ? undefined: 'code'"
                 ref="customCodeRef"
                 @clear="resetValidation"
                 clearable v-model="customCode"></q-input>
        <div class="cols-auto text-center">
          <q-btn-toggle
            class="q-mb-md"
            v-model="ttlVar"
            push
            toggle-color="primary"
            :options="durationOptions"
          />
          <q-input filled input-class="text-weight-bold text-h5 text-white" label-color="white"
                   bg-color="primary"
                   type="number"
                   @click="setDays"
                   :rules="[() => true]"
                   :label="ttlQuantity ? undefined: '0'"
                   clearable v-model="ttlQuantity"></q-input>
        </div>
      </q-card-section>
      <q-card-section class="row justify-between">
        <q-toggle v-show="!longUrl" class="cols-auto" label="More" v-model="moreOptions"></q-toggle>
        <q-btn class="cols-auto" no-caps color="white" text-color="primary"
               :class="{'full-width': longUrl}"
               @click="reload">reset
        </q-btn>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import {computed, ref} from "vue";
import {api} from "../boot/axios";
import {QInput, useQuasar} from "quasar";

const urlRegex = /^((?:https?|ftp):\/\/)(?:www\.)?[a-z0-9-]+(?:\.[a-z0-9-]+)+[^\s]*$/i;
const url = ref('https://example.com')
const isUrlValid = computed(() => url.value !== undefined && urlRegex.test(url.value))
const longUrl = ref()
const validUntil = ref()
const $q = useQuasar()
const moreOptions = ref(false)
const customCode = ref()
const urlInputRef = ref<QInput>();
const customCodeRef = ref<QInput>();
const ttlVar = ref()
const ttlQuantity = ref()
const ttl = computed(() => {
  if (ttlVar.value && ttlQuantity.value) {
    return `PT${durationOptions.find(d => ttlVar.value === d.value)?.toDuration(ttlQuantity.value)}`
  }
  return undefined
})
const errorMessage = ref();
const durationOptions = [
  {label: 'Sec', value: 'S', toDuration: (v: number) => `${v}${v}S`},
  {label: 'Min', value: 'M', toDuration: (v: number) => `${v}${v}M`},
  {label: 'Hour', value: 'H', toDuration: (v: number) => `${v}${v}H`},
  {label: 'Days', value: 'D', toDuration: (v: number) => `${v}${24 * v}H`}
]

function shortUrl() {
  if (isUrlValid.value) {
    api.post('/api/v1/short', {url: url.value, code: customCode.value, ttl: ttl.value})
      .then(({data}) => {
        longUrl.value = data.url
        validUntil.value = data.validUntil
        url.value = `${location.protocol}//${location.host}/${data.code}`
        customCode.value = undefined
        ttlVar.value = undefined
        ttlQuantity.value = undefined
        moreOptions.value = false
      }).catch((err) => {
      errorMessage.value = err.response.data.message
      customCodeRef.value?.validate()
    })
  }
}

function copyUrl() {
  navigator.clipboard.writeText(url.value)
  $q.notify({message: 'url copied', color: "primary"})
}

function copyIfShort(e: PointerEvent) {
  if (longUrl.value) {
    urlInputRef.value?.blur()
    navigator.clipboard.writeText(url.value)
    $q.notify({message: 'url copied', color: "primary"})
  }
}

function reload() {

  location.reload()
}

function resetValidation() {
  customCodeRef.value?.resetValidation()
  errorMessage.value = undefined
}

function setDays() {
  ttlVar.value = ttlVar.value ? ttlVar.value : 'D'
}
</script>
