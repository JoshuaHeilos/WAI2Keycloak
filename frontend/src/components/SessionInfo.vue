<template>
  <div class="session-info">
    <div v-if="sessionInfo">
      <p>Session ID: {{ sessionInfo.sessionId }}</p>
      <p>User ID: {{ sessionInfo.userId }}</p>
      <p>Role: {{ sessionInfo.role }}</p>
      <p>Company ID: {{ sessionInfo.companyId }}</p>
    </div>
    <div v-else>
      <p>Loading session info...</p>
    </div>
    <div v-if="requestInfo.url">
      <p>Last Request URL: {{ requestInfo.url }}</p>
      <p>Request Method: {{ requestInfo.method }}</p>
      <p>Request Headers: {{ JSON.stringify(requestInfo.headers) }}</p>
      <p>Request Data: {{ JSON.stringify(requestInfo.data) }}</p>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    sessionInfo: Object,
    requestInfo: Object
  }
};
</script>

<style>
.session-info {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #f0f0f0;
  padding: 10px;
  border-radius: 5px;
}
</style>
