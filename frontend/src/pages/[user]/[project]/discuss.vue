<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { useRoute } from "vue-router";
import { HangarProject } from "hangar-internal";

const route = useRoute();
const props = defineProps<{
  project: HangarProject;
}>();

useHead(
  useSeo("Discuss | " + props.project.name, props.project.description, route, projectIconUrl(props.project.namespace.owner, props.project.namespace.slug))
);
</script>

<template>
  <h1>discuss</h1>
</template>
