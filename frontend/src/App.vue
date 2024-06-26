<template>
  <div id="app">
    <SessionInfo :sessionInfo="sessionInfo" :requestInfo="requestInfo" />
    <router-view @session-info-fetched="updateSessionInfo" @request-info-updated="updateRequestInfo"></router-view>
  </div>
</template>

<script>
import { reactive } from 'vue';
import SessionInfo from './components/SessionInfo.vue';

export default {
  data() {
    return {
      sessionInfo: reactive({
        sessionId: sessionStorage.getItem('sessionId'),
        userId: sessionStorage.getItem('userId'),
        role: sessionStorage.getItem('role'),
        companyId: sessionStorage.getItem('companyId')
      }),
      requestInfo: reactive({
        url: '',
        data: {},
        method: '',
        headers: {}
      })
    };
  },
  components: {
    SessionInfo,
  },
  methods: {
    updateSessionInfo(sessionInfo) {
      this.sessionInfo.sessionId = sessionInfo.sessionId;
      this.sessionInfo.userId = sessionInfo.userId;
      this.sessionInfo.role = sessionInfo.role;
      this.sessionInfo.companyId = sessionInfo.companyId;
      sessionStorage.setItem('sessionId', sessionInfo.sessionId);
      sessionStorage.setItem('userId', sessionInfo.userId);
      sessionStorage.setItem('role', sessionInfo.role);
      sessionStorage.setItem('companyId', sessionInfo.companyId);
    },
    updateRequestInfo(requestInfo) {
      this.requestInfo.url = requestInfo.url;
      this.requestInfo.data = requestInfo.data;
      this.requestInfo.method = requestInfo.method;
      this.requestInfo.headers = requestInfo.headers;
    }
  }
};
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
