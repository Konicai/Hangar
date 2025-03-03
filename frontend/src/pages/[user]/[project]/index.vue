<script lang="ts" setup>
import { User } from "hangar-api";
import Card from "~/lib/components/design/Card.vue";
import { useI18n } from "vue-i18n";
import ProjectInfo from "~/components/projects/ProjectInfo.vue";
import { HangarProject, PinnedVersion } from "hangar-internal";
import MemberList from "~/components/projects/MemberList.vue";
import MarkdownEditor from "~/components/MarkdownEditor.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import { useRoute, useRouter } from "vue-router";
import { useContext } from "vite-ssr/vue";
import Markdown from "~/components/Markdown.vue";
import ProjectPageList from "~/components/projects/ProjectPageList.vue";
import { ref } from "vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useBackendDataStore } from "~/store/backendData";
import Tag from "~/components/Tag.vue";
import PlatformLogo from "~/components/logos/platforms/PlatformLogo.vue";
import DownloadButton from "~/components/projects/DownloadButton.vue";
import { useOpenProjectPages } from "~/composables/useOpenProjectPages";
import ProjectPageMarkdown from "~/components/projects/ProjectPageMarkdown.vue";

const props = defineProps<{
  user: User;
  project: HangarProject;
}>();
const i18n = useI18n();
const backendData = useBackendDataStore();
const ctx = useContext();
const route = useRoute();
const context = useContext();
const router = useRouter();
const openProjectPages = await useOpenProjectPages(route, props.project);

const sponsors = ref(props.project.settings.sponsors);
const editingSponsors = ref(false);
function saveSponsors(content: string) {
  useInternalApi(`projects/project/${props.project.namespace.owner}/${props.project.namespace.slug}/sponsors`, true, "post", {
    content,
  })
    .then(() => {
      sponsors.value = content;
      editingSponsors.value = false;
    })
    .catch((e) => handleRequestError(e, ctx, i18n, "page.new.error.save"));
}

function createPinnedVersionUrl(version: PinnedVersion): string {
  return `/${props.project.namespace.owner}/${props.project.namespace.slug}/versions/${version.name}`;
}

// useSeo is in ProjectPageMarkdown
</script>

<template>
  <div class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-9/12 flex-grow overflow-auto">
      <ProjectPageMarkdown v-slot="{ page, editingPage, changeEditingPage, savePage }" :project="props.project">
        <Card v-if="page?.contents" class="p-0 pb-6 overflow-clip overflow-hidden">
          <MarkdownEditor
            v-if="hasPerms(NamedPermission.EDIT_PAGE)"
            :editing="editingPage"
            :raw="page.contents"
            :deletable="false"
            :saveable="true"
            :cancellable="true"
            @update:editing="changeEditingPage"
            @save="savePage"
          />
          <!--We have to blow up v-model:editing into :editing and @update:editing as we are inside a scope--->
          <Markdown v-else :raw="page.contents" />
        </Card>
      </ProjectPageMarkdown>
      <Card v-if="sponsors || hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)" class="mt-2 p-0 pb-6 overflow-clip overflow-hidden">
        <MarkdownEditor
          v-if="hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)"
          v-model:editing="editingSponsors"
          :raw="sponsors"
          :deletable="false"
          :saveable="true"
          :cancellable="true"
          :maxlength="500"
          :title="i18n.t('project.sponsors')"
          class="pt-0"
          @save="saveSponsors"
        />
        <template v-else>
          <h2 class="mt-3 ml-5 text-xl">{{ i18n.t("project.sponsors") }}</h2>
          <Markdown :raw="sponsors" class="pt-0" />
        </template>
      </Card>
    </section>
    <section class="basis-full md:basis-3/12 space-y-4 min-w-280px">
      <ProjectInfo :project="project" />
      <Card>
        <template #header>
          <h3>{{ i18n.t("project.pinnedVersions") }}</h3>
        </template>
        <ul class="divide-y divide-blue-500/50">
          <li v-for="(version, index) in project.pinnedVersions" :key="`${index}-${version.name}`" class="p-1 py-2">
            <div class="flex">
              <router-link :to="createPinnedVersionUrl(version)" class="flex-grow truncate">
                <div class="truncate">
                  <span class="font-semibold truncate">{{ version.name }}</span>
                </div>
              </router-link>
              <div class="ml-1 space-y-2 flex flex-col">
                <Tag :name="version.channel.name" :color="{ background: version.channel.color }" />
              </div>
            </div>
            <div class="flex pt-1">
              <router-link :to="createPinnedVersionUrl(version)" class="flex-grow">
                <div class="inline-flex items-center mt-1">
                  <div class="flex flex-col">
                    <div v-for="(v, p) in version.platformDependenciesFormatted" :key="p" class="flex flex-row items-center">
                      <PlatformLogo :key="p" :platform="p" :size="20" class="mr-1 flex-shrink-0" />
                      <span :key="p" class="text-0.875rem light:text-gray-600">{{ v }}</span>
                    </div>
                  </div>
                </div>
              </router-link>
              <div class="ml-1 space-y-2 flex flex-col mt-1">
                <DownloadButton :project="project" :pinned-version="version" small :show-versions="false" class="self-end"></DownloadButton>
              </div>
            </div>
          </li>
        </ul>
      </Card>
      <ProjectPageList :project="project" :open="openProjectPages" />
      <MemberList :members="project.members" :author="project.owner.name" :slug="project.name" :owner="project.owner.userId" class="overflow-visible" />
    </section>
  </div>
</template>
