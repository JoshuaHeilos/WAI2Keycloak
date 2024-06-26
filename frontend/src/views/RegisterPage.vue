<template>
  <div class="container">
    <div class="content">
      <h1>Register</h1>
      <form @submit.prevent="register">
        <label for="name">Name:</label>
        <input type="text" v-model="name" required />

        <label for="email">Email:</label>
        <input type="email" v-model="email" required />

        <label for="password">Password:</label>
        <input type="password" v-model="password" required/>

        <label for="role">Role:</label>
        <select v-model="role">
          <option value="Employee">Employee</option>
          <option value="TeamLeader">TeamLeader</option>
          <option value="Admin">Admin</option>
        </select>

        <button type="submit">Register</button>
      </form>
      <p v-if="errorMessage">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      name: '',
      email: '',
      password: '',
      role: 'Employee',
      errorMessage: ''
    };
  },
  methods: {
    register() {
      const requestData = {
        name: this.name,
        companyEmail: this.email,
        password: this.password,
        role: this.role
      };
      this.$emit('request-info-updated', {url: 'http://localhost:8081/api/register', data: requestData});

      fetch('http://localhost:8081/api/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
      })
          .then(response => {
            if (response.ok) {
              this.$router.push('/login');
            } else {
              return response.text().then(text => {
                throw new Error(text);
              });
            }
          })
          .catch(error => {
            this.errorMessage = error.message;
          });
    }
  }
};
</script>