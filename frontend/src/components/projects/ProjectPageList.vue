<script setup lang="ts">
import { HangarProject } from "hangar-internal";
import Card from "~/lib/components/design/Card.vue";
import { useI18n } from "vue-i18n";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import NewPageModal from "~/components/modals/NewPageModal.vue";
import TreeView from "~/lib/components/design/TreeView.vue";
import Link from "~/lib/components/design/Link.vue";
import { useRoute } from "vue-router";

const props = defineProps<{
  project: HangarProject;
  open: string[];
}>();

const i18n = useI18n();
const route = useRoute();
</script>

<template>
  <Card>
    <template #header>
      <div class="inline-flex w-full flex-cols space-between">
        <h3 class="flex-grow">{{ i18n.t("page.plural") }}</h3>
        <NewPageModal v-if="hasPerms(NamedPermission.EDIT_PAGE)" :pages="project.pages" :project-id="project.id" activator-class="mr-2" />
      </div>
    </template>

    <TreeView :items="project.pages" item-key="slug" :open="open">
      <template #item="{ item }">
        <Link v-if="item.home" :to="`/${route.params.user}/${route.params.project}`" exact class="inline-flex items-center" active-underline>
          <IconMdiHome class="mr-1" />
          {{ item.name }}
        </Link>
        <Link v-else :to="`/${route.params.user}/${route.params.project}/pages/${item.slug}`" exact active-underline> {{ item.name }}</Link>
      </template>
    </TreeView>
  </Card>
</template>
