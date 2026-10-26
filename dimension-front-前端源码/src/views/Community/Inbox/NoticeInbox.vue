<template>
  <div class="ib-page">
    <div class="ib-head">
      <h2>通知</h2>
      <p class="ib-hint">系统公告、关注画师的作品更新、活动推送</p>
    </div>
    <inbox-list-view kind="notice"/>
  </div>
</template>

<script>
import InboxListView from './InboxListView.vue'

export default {
  name: 'NoticeInbox',
  components: { InboxListView }
}
</script>

<style scoped>
.ib-page { max-width: 900px; margin: 0 auto; padding: 24px; min-height: 60vh; }
.ib-head h2 { margin: 0 0 6px; font-size: 20px; color: var(--text-primary); }
.ib-hint { margin: 0 0 16px; font-size: 13px; color: var(--text-muted); }
</style>
