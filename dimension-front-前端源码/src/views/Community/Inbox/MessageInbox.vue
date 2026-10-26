<template>
  <div class="ib-page">
    <div class="ib-head">
      <h2>消息</h2>
      <p class="ib-hint">私信提醒、评论回复、@提及</p>
    </div>
    <inbox-list-view kind="message"/>
  </div>
</template>

<script>
import InboxListView from './InboxListView.vue'

export default {
  name: 'MessageInbox',
  components: { InboxListView }
}
</script>

<style scoped>
.ib-page { max-width: 900px; margin: 0 auto; padding: 24px; min-height: 60vh; }
.ib-head h2 { margin: 0 0 6px; font-size: 20px; color: var(--text-primary); }
.ib-hint { margin: 0 0 16px; font-size: 13px; color: var(--text-muted); }
</style>
