<script lang="ts" setup>
import { ProjectApproval } from "hangar-internal";
import Alert from "~/lib/components/design/Alert.vue";
import { useI18n } from "vue-i18n";
import Markdown from "~/components/Markdown.vue";
import Link from "~/lib/components/design/Link.vue";
import VisibilityChangerModal from "~/components/modals/VisibilityChangerModal.vue";

const i18n = useI18n();
const props = defineProps<{
  projects: ProjectApproval[];
}>();
</script>

<template>
  <ul v-if="projects.length">
    <template v-for="project in projects" :key="project.projectId">
      <hr />
      <li>
        <div class="flex <md:flex-col items-center">
          <div class="basis-full md:basis-3/12">
            {{ i18n.t("projectApproval.description", [project.changeRequester, `${project.namespace.owner}/${project.namespace.slug}`]) }}
            <Link :to="`/${project.namespace.owner}/${project.namespace.slug}`" target="_blank">
              <IconMdiOpenInNew />
            </Link>
          </div>
          <div class="basis-full md:basis-6/12 flex-grow">
            <Markdown :raw="project.comment" />
          </div>
          <div class="">
            <VisibilityChangerModal :prop-visibility="project.visibility" type="project" :post-url="`projects/visibility/${project.projectId}`" />
          </div>
        </div>
      </li>
    </template>
  </ul>
  <Alert v-else type="info">
    {{ i18n.t("projectApproval.noProjects") }}
  </Alert>
</template>
