<script lang="ts" setup>
import { TranslateResult, useI18n } from "vue-i18n";
import Markdown from "~/components/Markdown.vue";
import Modal from "~/lib/components/modals/Modal.vue";

const props = defineProps<{
  title: string | TranslateResult;
  markdown: string;
}>();

const i18n = useI18n();
</script>

<template>
  <Modal :title="props.title" window-classes="w-250">
    <Markdown :raw="markdown" />
    <template #activator="{ on }">
      <slot name="activator" :on="on"></slot>
    </template>
  </Modal>
</template>
