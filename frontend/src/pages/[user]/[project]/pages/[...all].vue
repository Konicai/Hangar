<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useContext } from "vite-ssr/vue";
import { useRoute, useRouter } from "vue-router";
import { User } from "hangar-api";
import { HangarProject } from "hangar-internal";
import ProjectPageList from "~/components/projects/ProjectPageList.vue";
import Markdown from "~/components/Markdown.vue";
import MarkdownEditor from "~/components/MarkdownEditor.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import Card from "~/lib/components/design/Card.vue";
import ProjectPageMarkdown from "~/components/projects/ProjectPageMarkdown.vue";
import { useOpenProjectPages } from "~/composables/useOpenProjectPages";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();

const i18n = useI18n();
const ctx = useContext();
const route = useRoute();
const router = useRouter();

const open = await useOpenProjectPages(route, props.project);
// useSeo is in ProjectPageMarkdown
</script>

<template>
  <div class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-9/12 flex-grow overflow-auto">
      <ProjectPageMarkdown :key="route.fullPath" v-slot="{ page, editingPage, changeEditingPage, savePage, deletePage }" :project="props.project">
        <Card v-if="page" class="p-0 overflow-clip overflow-hidden">
          <MarkdownEditor
            v-if="hasPerms(NamedPermission.EDIT_PAGE)"
            ref="editor"
            :editing="editingPage"
            :raw="page.contents"
            :deletable="page.deletable"
            :saveable="true"
            :cancellable="true"
            @update:editing="changeEditingPage"
            @save="savePage"
            @delete="deletePage"
          />
          <Markdown v-else :raw="page.contents" />
        </Card>
      </ProjectPageMarkdown>
      <!--We have to blow up v-model:editing into :editing and @update:editing as we are inside a scope--->
    </section>
    <section class="basis-full md:basis-3/12 flex-grow">
      <ProjectPageList :project="project" :open="open" />
    </section>
  </div>
</template>
