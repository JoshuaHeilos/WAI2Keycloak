<template>
  <div class="main-content">
    <div class="nav-buttons">
      <button @click="goToCompanyDashboard">Go to Company Dashboard</button>
      <button @click="goToCourseDashboard">Go to Course Dashboard</button>
    </div>
    <h1>Employee Dashboard</h1>
    <div v-if="user && company">
      <p>Company: {{ company.name }}</p>
      <p>User: {{ user.name }}</p>
      <p>Email: {{ user.companyEmail }}</p>
      <p>Password: {{ user.password }}</p>
      <h2>Courses</h2>
      <ul>
        <li v-for="courseProgress in courses" :key="courseProgress.courseId">
          {{ courseProgress.courseName }} - Progress: {{ courseProgress.progress }}%
          <button @click="incrementProgress(courseProgress)">Increment Progress</button>
        </li>
      </ul>
      <h2>Available Courses</h2>
      <ul>
        <li v-for="course in availableCourses" :key="course.courseId">
          {{ course.name }} - {{ course.description }}
          <button @click="bookCourse(course)">Book Course</button>
        </li>
      </ul>
      <h2>Update Info</h2>
      <div>
        <label for="name">Name:</label>
        <input id="name" v-model="updatedName" placeholder="Enter new name" />
        <button @click="updateUserName">Update Name</button>
      </div>
      <div>
        <label for="password">Password:</label>
        <input id="password" type="password" v-model="updatedPassword" placeholder="Enter new password" />
        <button @click="updateUserPassword">Update Password</button>
      </div>
    </div>
    <div v-else>
      <p>Loading...</p>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      user: null,
      company: null,
      courses: [],
      availableCourses: [],
      updatedName: '',
      updatedPassword: '',
      role: ''
    };
  },
  created() {
    this.fetchSessionInfo();
  },
  methods: {
    fetchSessionInfo() {
      const requestInfo = {
        url: 'http://localhost:8081/api/session-info',
        method: 'GET',
        headers: {}
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        credentials: 'include'
      })
          .then(response => response.json())
          .then(data => {
            sessionStorage.setItem('sessionId', data.sessionId);
            sessionStorage.setItem('userId', data.userId);
            sessionStorage.setItem('role', data.role);
            sessionStorage.setItem('companyId', data.companyId);
            this.role = data.role;
            this.fetchUserData(data.userId);
            this.fetchCompanyData(data.companyId);
            this.fetchUserCourses(data.userId);
            this.fetchAvailableCourses(data.companyId, data.userId);
          })
          .catch(error => {
            console.error('Error fetching session info:', error);
          });
    },
    fetchUserData(userId) {
      const requestInfo = {
        url: `http://localhost:8081/api/users/${userId}`,
        method: 'GET',
        headers: {}
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        credentials: 'include'
      })
          .then(response => response.json())
          .then(data => {
            this.user = data;
            this.updatedName = data.name;
            this.updatedPassword = data.password;
          })
          .catch(error => {
            console.error('Error fetching user info:', error);
          });
    },
    fetchCompanyData(companyId) {
      const requestInfo = {
        url: `http://localhost:8081/api/companies/${companyId}`,
        method: 'GET',
        headers: {}
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        credentials: 'include'
      })
          .then(response => response.json())
          .then(data => {
            this.company = data;
          })
          .catch(error => {
            console.error('Error fetching company data:', error);
          });
    },
    fetchUserCourses(userId) {
      const requestInfo = {
        url: `http://localhost:8081/api/user-progress/${userId}`,
        method: 'GET',
        headers: {}
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        credentials: 'include'
      })
          .then(response => response.json())
          .then(data => {
            console.log('User courses data:', data);  // Log the received data
            this.courses = data;
          })
          .catch(error => {
            console.error('Error fetching user courses:', error);
          });
    },
    fetchAvailableCourses(companyId, userId) {
      const requestInfo = {
        url: `http://localhost:8081/api/companies/${companyId}/available-courses/${userId}`,
        method: 'GET',
        headers: {}
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        credentials: 'include'
      })
          .then(response => response.json())
          .then(data => {
            this.availableCourses = data;
          })
          .catch(error => {
            console.error('Error fetching available courses:', error);
          });
    },
    updateUserName() {
      const userId = sessionStorage.getItem('userId');
      const updatedData = { name: this.updatedName };
      const requestInfo = {
        url: `http://localhost:8081/api/users/${userId}`,
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        data: updatedData
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
            this.user.name = data.name;
          })
          .catch(error => {
            console.error('Error updating user name:', error);
          });
    },
    updateUserPassword() {
      const userId = sessionStorage.getItem('userId');
      const updatedData = { password: this.updatedPassword };
      const requestInfo = {
        url: `http://localhost:8081/api/users/${userId}`,
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        data: updatedData
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
            this.user.password = data.password;
          })
          .catch(error => {
            console.error('Error updating user password:', error);
          });
    },
    incrementProgress(courseProgress) {
      if (courseProgress.progress < 100) {
        const updatedProgress = courseProgress.progress + 1;
        const requestInfo = {
          url: `http://localhost:8081/api/user-progress/${courseProgress.id}`,
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          data: { progress: updatedProgress }
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
              courseProgress.progress = data.progress;
            })
            .catch(error => {
              console.error('Error updating course progress:', error);
            });
      }
    },
    bookCourse(course) {
      const userId = sessionStorage.getItem('userId');
      const requestInfo = {
        url: `http://localhost:8081/api/users/${userId}/book-course`,
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        data: { courseId: course.courseId }
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
            this.courses.push(data); // Add the newly booked course to the user's courses
            this.availableCourses = this.availableCourses.filter(c => c.courseId !== course.courseId); // Remove the booked course from the available courses
          })
          .catch(error => {
            console.error('Error booking course:', error);
          });
    },
    goToCompanyDashboard() {
      this.$router.push('/company-dashboard');
    },
    goToCourseDashboard() {
      this.$router.push('/course-dashboard');
    }
  }
};
</script>

<style>
.nav-buttons {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.nav-buttons button {
  margin: 0 10px;
}
</style>
