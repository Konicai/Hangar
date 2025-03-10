<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import { Review, ReviewQueueEntry } from "hangar-internal";
import { ReviewAction } from "~/types/enums";
import { useContext } from "vite-ssr/vue";
import { useVersionApprovals } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import Card from "~/lib/components/design/Card.vue";
import Link from "~/lib/components/design/Link.vue";
import Tag from "~/components/Tag.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useRoute } from "vue-router";
import Button from "~/lib/components/design/Button.vue";

const i18n = useI18n();
const ctx = useContext();
const route = useRoute();
const data = await useVersionApprovals().catch((e) => handleRequestError(e, ctx, i18n));

const actions = {
  ongoing: [ReviewAction.START, ReviewAction.MESSAGE, ReviewAction.UNDO_APPROVAL, ReviewAction.REOPEN],
  stopped: [ReviewAction.STOP],
  approved: [ReviewAction.APPROVE, ReviewAction.PARTIALLY_APPROVE],
};

const underReviewHeaders: Header[] = [
  { title: i18n.t("versionApproval.project") as string, name: "project", sortable: false },
  { title: i18n.t("versionApproval.version") as string, name: "version", sortable: false },
  { title: i18n.t("versionApproval.queuedBy") as string, name: "queuedBy", sortable: true },
  { title: i18n.t("versionApproval.status") as string, name: "status", sortable: true },
  { title: "", name: "reviewLogs", sortable: false },
];

const notStartedHeaders: Header[] = [
  { title: i18n.t("versionApproval.project") as string, name: "project", sortable: false },
  { title: i18n.t("versionApproval.date") as string, name: "date", sortable: true },
  { title: i18n.t("versionApproval.version") as string, name: "version", sortable: false },
  { title: i18n.t("versionApproval.queuedBy") as string, name: "queuedBy", sortable: true },
  { title: "", name: "startBtn", sortable: false },
];

useHead(useSeo(i18n.t("versionApproval.title"), null, route, null));

function getRouteParams(entry: ReviewQueueEntry) {
  return {
    user: entry.namespace.owner,
    project: entry.namespace.slug,
    version: entry.versionString,
    platform: entry.platforms[0].toLowerCase(),
  };
}

function isOngoing(review: Review) {
  return actions.ongoing.includes(review.lastAction);
}

function isStopped(review: Review) {
  return actions.stopped.includes(review.lastAction);
}

function isApproved(review: Review) {
  return actions.approved.includes(review.lastAction);
}

function getOngoingCount(entry: ReviewQueueEntry) {
  return getCount(entry, ...actions.ongoing);
}

function getStoppedCount(entry: ReviewQueueEntry) {
  return getCount(entry, ...actions.stopped);
}

function getApprovedCount(entry: ReviewQueueEntry) {
  return getCount(entry, ...actions.approved);
}

function getCount(entry: ReviewQueueEntry, ..._actions: ReviewAction[]) {
  let count = 0;
  for (const review of entry.reviews) {
    if (_actions.includes(review.lastAction)) {
      count++;
    }
  }
  return count;
}
</script>

<template>
  <Card>
    <template #header>{{ i18n.t("versionApproval.approvalQueue") }}</template>

    <SortableTable v-if="data" :headers="notStartedHeaders" :items="data.notStarted">
      <template #item_project="{ item }">
        <Link :to="`/${item.namespace.owner}/${item.namespace.slug}`">
          {{ `${item.namespace.owner}/${item.namespace.slug}` }}
        </Link>
      </template>
      <template #item_date="{ item }">
        <span class="start-date">{{ i18n.d(item.versionCreatedAt, "time") }}</span>
      </template>
      <template #item_version="{ item }">
        <Link :to="{ name: 'user-project-versions-version-platform', params: getRouteParams(item) }">
          <Tag :color="{ background: item.channelColor }" :name="item.channelName" :data="item.versionString" />
        </Link>
      </template>
      <template #item_queuedBy="{ item }">
        <Link :to="`/${item.versionAuthor}`">
          {{ item.versionAuthor }}
        </Link>
      </template>
      <template #item_startBtn="{ item }">
        <Link :to="{ name: 'user-project-versions-version-platform-reviews', params: getRouteParams(item) }" nuxt>
          <Button>
            <IconMdiPlay />
            {{ i18n.t("version.page.reviewStart") }}
          </Button>
        </Link>
      </template>
    </SortableTable>
  </Card>

  <Card class="mt-4">
    <template #header>{{ i18n.t("versionApproval.inReview") }}</template>

    <SortableTable v-if="data" :headers="underReviewHeaders" :items="data.underReview" expandable>
      <template #item_project="{ item }">
        <Link :to="`/${item.namespace.owner}/${item.namespace.slug}`">
          {{ `${item.namespace.owner}/${item.namespace.slug}` }}
        </Link>
      </template>
      <template #item_version="{ item }">
        <Link :to="{ name: 'user-project-versions-version-platform', params: getRouteParams(item) }">
          <Tag :color="{ background: item.channelColor }" :name="item.channelName" :data="item.versionString" />
        </Link>
      </template>
      <template #item_queuedBy="{ item }">
        <Link :to="`/${item.versionAuthor}`">
          {{ item.versionAuthor }}
        </Link>
        <br />
        <small>{{ i18n.d(item.versionCreatedAt, "time") }}</small>
      </template>
      <template #item_status="{ item }">
        <span class="text-yellow-400">
          {{ i18n.t("versionApproval.statuses.ongoing", [getOngoingCount(item)]) }}
        </span>
        <br />
        <span class="text-red-400">
          {{ i18n.t("versionApproval.statuses.stopped", [getStoppedCount(item)]) }}
        </span>
        <br />
        <span class="text-green-400"> {{ i18n.t("versionApproval.statuses.approved", [getApprovedCount(item)]) }}</span>
      </template>
      <template #item_reviewLogs="{ item }">
        <Link :to="{ name: 'user-project-versions-version-platform-reviews', params: getRouteParams(item) }">
          <IconMdiListStatus />
          {{ i18n.t("version.page.reviewLogs") }}
        </Link>
      </template>
      <template #expanded-item="{ item, headers }">
        <td :colspan="headers.length">
          <ul>
            <li v-for="entry in item.reviews" :key="entry.reviewerName" class="ml-4">
              <span
                class="font-bold mr-2"
                :class="{ 'text-yellow-400': isOngoing(entry), 'text-red-400': isStopped(entry), 'text-green-400': isApproved(entry) }"
                >{{ entry.reviewerName }}</span
              >
              <span>{{ i18n.t("versionApproval.started", [i18n.d(entry.reviewStarted, "time")]) }}</span>
              <span v-if="entry.reviewEnded" class="ml-4" :class="{ 'text-red-400': isStopped(entry), 'text-green-400': isApproved(entry) }">{{
                i18n.t("versionApproval.ended", [i18n.d(entry.reviewEnded, "time")])
              }}</span>
            </li>
          </ul>
        </td>
      </template>
    </SortableTable>
  </Card>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["REVIEWER"]
</route>
