<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useResolvedFlags, useUnresolvedFlags } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { ref } from "vue";
import { Flag, HangarFlagNotification } from "hangar-internal";
import { useInternalApi } from "~/composables/useApi";
import UserAvatar from "~/components/UserAvatar.vue";
import Card from "~/lib/components/design/Card.vue";
import Button from "~/lib/components/design/Button.vue";
import VisibilityChangerModal from "~/components/modals/VisibilityChangerModal.vue";
import ReportNotificationModal from "~/components/modals/ReportNotificationModal.vue";
import Pagination from "~/lib/components/design/Pagination.vue";
import { PaginatedResult } from "hangar-api";

const props = defineProps<{
  resolved: boolean;
}>();

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const flags = await (props.resolved ? useResolvedFlags() : useUnresolvedFlags()).catch((e) => handleRequestError(e, ctx, i18n));
const loading = ref<{ [key: number]: boolean }>({});

function resolve(flag: Flag) {
  loading.value[flag.id] = true;
  useInternalApi(`flags/${flag.id}/resolve/${props.resolved ? "false" : "true"}`, false, "POST")
    .catch<any>((e) => handleRequestError(e, ctx, i18n))
    .then(async () => {
      if (flags && flags.value) {
        const newFlags = await useInternalApi<PaginatedResult<Flag>>("flags/" + (props.resolved ? "resolved" : "unresolved"), false).catch((e) =>
          handleRequestError(e, ctx, i18n)
        );
        if (newFlags) {
          flags.value = newFlags;
        }
      }
    })
    .finally(() => {
      loading.value[flag.id] = false;
    });
}

//TODO: bake into hangarflag?
const notifications = ref<HangarFlagNotification[]>([]);
const currentId = ref(-1);
async function getNotifications(flag: Flag) {
  if (currentId.value === flag.id) {
    return;
  }

  notifications.value = (await useInternalApi<HangarFlagNotification[]>(`flags/${flag.id}/notifications`, false, "get").catch((e) =>
    handleRequestError(e, ctx, i18n)
  )) as HangarFlagNotification[];
  currentId.value = flag.id;
}
</script>

<template>
  <template v-if="flags && flags.result && flags.result.length > 0">
    <Pagination :items="flags.result">
      <template #default="{ item }">
        <Card class="mb-2">
          <div class="flex space-x-1 items-center">
            <UserAvatar :username="item.reportedByName" size="sm" class="flex-shrink-0"></UserAvatar>
            <div class="flex flex-col flex-grow">
              <h2>
                {{
                  i18n.t("flagReview.line1", [
                    item.reportedByName,
                    `${item.projectNamespace.owner}/${item.projectNamespace.slug}`,
                    i18n.d(item.createdAt, "time"),
                  ])
                }}
                <router-link :to="`/${item.projectNamespace.owner}/${item.projectNamespace.slug}`" target="_blank">
                  <icon-mdi-open-in-new class="inline ml-1"></icon-mdi-open-in-new>
                </router-link>
              </h2>
              <small>{{ i18n.t("flagReview.line2", [i18n.t(item.reason)]) }}</small>
              <small>{{ i18n.t("flagReview.line3", [item.comment]) }}</small>
            </div>

            <template v-if="resolved">
              <Button v-if="currentId !== item.id" @click="getNotifications(item)">Load notifications</Button>
              <Button :disabled="loading[item.id]" @click="resolve(item)"><IconMdiCheck class="mr-1" /> {{ i18n.t("flagReview.markUnresolved") }}</Button>
            </template>
            <template v-else>
              <div class="flex flex-col space-y-1">
                <ReportNotificationModal :flag="item" :send-to-reporter="false" />
                <ReportNotificationModal :flag="item" :send-to-reporter="true" />
              </div>
              <VisibilityChangerModal :prop-visibility="item.projectVisibility" type="project" :post-url="`projects/visibility/${item.projectId}`" />
              <Button :disabled="loading[item.id]" @click="resolve(item)"><IconMdiCheck class="mr-1" /> {{ i18n.t("flagReview.markResolved") }}</Button>
            </template>
          </div>

          <!-- todo: make this actually look good and work well -->
          <div class="flex-col">
            <Button v-if="currentId !== item.id && !resolved" @click="getNotifications(item)">Load notifications</Button>
            <ul v-else-if="currentId === item.id">
              <li v-if="notifications.length === 0">Empty!</li>
              <li v-for="notification in notifications" v-else :key="notification.id" class="text-xs">
                <span class="inline-flex">
                  <IconMdiInformationOutline v-if="notification.type === 'info'" class="text-blue-400 mr-1" />
                  <IconMdiAlertOutline v-else class="text-red-500 mr-1" />
                  From {{ notification.originUserName }} to {{ notification.userId === item.userId ? "the reporter" : "the project's members" }}:
                  {{ i18n.t(notification.message[0], notification.message.slice(1)).split(":")[1] }}
                </span>
              </li>
            </ul>
          </div>
        </Card>
      </template>
    </Pagination>
  </template>
  <div v-else>
    {{ i18n.t("flagReview.noFlags") }}
  </div>
</template>
