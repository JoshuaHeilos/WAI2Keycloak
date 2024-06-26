<template>
  <div class="main-content">
    <button @click="goToEmployeeDashboard">Go to Employee Dashboard</button>
    <h1>Course Dashboard</h1>
    <p v-if="!isAdmin">You do not have permission to view this dashboard.</p>
    <div v-if="isAdmin">
      <h2>Add New Course</h2>
      <form @submit.prevent="addCourse">
        <div>
          <label for="name">Course Name:</label>
          <input id="name" v-model="newCourse.name" required />
        </div>
        <div>
          <label for="description">Course Description:</label>
          <input id="description" v-model="newCourse.description" required />
        </div>
        <button type="submit">Add Course</button>
      </form>

      <h2>Company Overview</h2>
      <ul>
        <li v-for="company in companies" :key="company.companyId">
          {{ company.name }} (Max Users: {{ company.maxUser }})
          <form @submit.prevent="updateMaxUser(company.companyId, company.newMaxUser)">
            <input type="number" v-model="company.newMaxUser" placeholder="New max users" required />
            <button type="submit">Update Max Users</button>
          </form>
        </li>
      </ul>

      <h2>Delete Course</h2>
      <ul>
        <li v-for="course in courses" :key="course.courseId">
          {{ course.name }} - {{ course.description }}
          <button @click="deleteCourse(course.courseId)">Delete Course</button>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      newCourse: {
        name: '',
        description: ''
      },
      companies: [],
      courses: [],
      role: sessionStorage.getItem('role')
    };
  },
  computed: {
    isAdmin() {
      return this.role === 'Admin';
    }
  },
  created() {
    if (this.isAdmin) {
      this.fetchCompanies();
      this.fetchCourses();
    }
  },
  methods: {
    addCourse() {
      const requestInfo = {
        url: 'http://localhost:8081/api/courses',
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        data: this.newCourse
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        headers: requestInfo.headers,
        body: JSON.stringify(requestInfo.data),
        credentials: 'include'
      })
          .then(response => response.json())
          .then(data => {
            this.newCourse.name = '';
            this.newCourse.description = '';
            console.log('Course added:', data);
            alert('Course added successfully');
            this.fetchCourses(); // Refresh the list of courses
          })
          .catch(error => {
            console.error('Error adding course:', error);
            alert('Error adding course');
          });
    },
    fetchCompanies() {
      const requestInfo = {
        url: 'http://localhost:8081/api/companies',
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        headers: requestInfo.headers,
        credentials: 'include'
      })
          .then(response => {
            if (!response.ok) {
              throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
          })
          .then(data => {
            console.log('Fetched companies:', data);
            this.companies = data.map(company => ({ ...company, newMaxUser: company.maxUser }));
          })
          .catch(error => {
            console.error('Error fetching companies:', error);
            alert(`Error fetching companies: ${error.message}`);
          });
    },
    fetchCourses() {
      const requestInfo = {
        url: 'http://localhost:8081/api/courses',
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        headers: requestInfo.headers,
        credentials: 'include'
      })
          .then(response => response.json())
          .then(data => {
            this.courses = data;
          })
          .catch(error => {
            console.error('Error fetching courses:', error);
            alert('Error fetching courses');
          });
    },
    updateMaxUser(companyId, newMaxUser) {
      const requestInfo = {
        url: `http://localhost:8081/api/companies/${companyId}/update-max-users`,
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        data: { maxUser: newMaxUser }
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        headers: requestInfo.headers,
        body: JSON.stringify(requestInfo.data),
        credentials: 'include'
      })
          .then(response => response.json())
          .then(data => {
            console.log('Max users updated:', data);
            this.fetchCompanies(); // Refresh the list of companies
          })
          .catch(error => {
            console.error('Error updating max users:', error);
            alert(`Error updating max users: ${error.message}`);
          });
    },
    deleteCourse(courseId) {
      const requestInfo = {
        url: `http://localhost:8081/api/courses/${courseId}`,
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        headers: requestInfo.headers,
        credentials: 'include'
      })
          .then(response => {
            if (!response.ok) {
              throw new Error(`HTTP error! status: ${response.status}`);
            }
            this.fetchCourses(); // Refresh the list of courses
          })
          .catch(error => {
            console.error('Error deleting course:', error);
            alert(`Error deleting course: ${error.message}`);
          });
    },
    goToEmployeeDashboard() {
      this.$router.push('/employee-dashboard');
    }
  }
};
</script>
