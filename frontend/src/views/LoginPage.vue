<template>
  <div class="container">
    <div class="content">
      <h1>Login</h1>
      <form @submit.prevent="login" class="login-form">
        <label for="email">Email:</label>
        <input type="email" v-model="email" required />

        <label for="password">Password:</label>
        <input type="password" v-model="password" required />

        <button type="submit">Login</button>
        <button @click.prevent="goToRegister">Register</button>
      </form>
      <p v-if="errorMessage">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      email: '',
      password: '',
      errorMessage: ''
    };
  },
  methods: {
    login() {
      const requestData = {
        companyEmail: this.email,
        password: this.password
      };
      const requestInfo = {
        url: 'http://localhost:8081/api/login',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        data: requestData
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        headers: requestInfo.headers,
        body: JSON.stringify(requestInfo.data),
        credentials: 'include'
      })
          .then(response => {
            if (response.ok) {
              return response.json();
            } else {
              throw new Error('Login failed');
            }
          })
          .then(data => {
            sessionStorage.setItem('userId', data.userId);
            sessionStorage.setItem('role', data.role);
            sessionStorage.setItem('companyId', data.companyId);
            this.fetchSessionInfo();  // Fetch session info immediately after login
          })
          .catch(error => {
            this.errorMessage = error.message;
          });
    },
    fetchSessionInfo() {
      fetch('http://localhost:8081/api/session-info', {
        method: 'GET',
        credentials: 'include'
      })
          .then(response => {
            if (response.ok) {
              return response.json();
            } else {
              throw new Error('Failed to fetch session info');
            }
          })
          .then(data => {
            this.$emit('session-info-fetched', data);  // Emit event to notify parent component
            this.$router.push('/employee-dashboard');  // Navigate to employee dashboard
          })
          .catch(error => {
            console.error('Error fetching session info:', error);
          });
    },
    goToRegister() {
      this.$router.push('/register');
    }
  }
};
</script>

<style>
.login-form {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login-form button {
  margin-top: 10px;
}

.login-form button:last-of-type {
  margin-left: 10px;
}
</style>
