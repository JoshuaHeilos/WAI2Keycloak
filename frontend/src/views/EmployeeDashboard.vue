<template>
  <div class="main-content">
    <button v-if="isTeamLeader" @click="goToCompanyDashboard">Go to Company Dashboard</button>
    <button v-if="isAdmin" @click="goToCourseDashboard">Go to Course Dashboard</button>

    <h1>Employee Dashboard</h1>

    <p v-if="!keycloak.authenticated">Please log in to view this page.</p>

    <div v-else-if="user && company">
      <p>Welcome, {{ keycloak.tokenParsed.preferred_username }}!</p>
      <p>Company: {{ company.name }}</p>
      <p>Role: {{ role }}</p>

      <h2>Your Courses</h2>
      <ul>
        <li v-for="courseProgress in courses" :key="courseProgress.id">
          {{ courseProgress.courseName }} - Progress: {{ courseProgress.progress }}%
          <button @click="incrementProgress(courseProgress.id)">Increment Progress</button>
        </li>
      </ul>

      <h2>Available Courses</h2>
      <ul>
        <li v-for="course in availableCourses" :key="course.courseId">
          {{ course.name }} - {{ course.description }}
          <button @click="bookCourse(course.courseId)">Book Course</button>
        </li>
      </ul>
    </div>

    <div v-else>
      <p>Loading...</p>
    </div>
  </div>
</template>

<script>
import keycloak from '@/keycloak';

export default {
  data() {
    return {
      user: null,
      company: null,
      courses: [],
      availableCourses: [],
      role: '',
      loading: true,
    };
  },
  computed: {
    isAdmin() {
      return this.role === 'Admin';
    },
    isTeamLeader() {
      return this.role === 'TeamLeader';
    }
  },
  created() {
    if (!keycloak.authenticated) {
      keycloak.login();
      return;
    }
    keycloak.loadUserInfo()
        .then(userInfo => {
          this.role = userInfo.role;
          this.fetchInitialData();
        })
        .catch(error => {
          console.error('Failed to load user info:', error);
          this.loading = false;
        });
  },
  methods: {
    fetchInitialData() {
      this.fetchUserData()
          .then(() => this.fetchCompanyData())
          .then(() => this.fetchUserCourses())
          .then(() => this.fetchAvailableCourses())
          .finally(() => {
            this.loading = false;
          });
    },

    fetchUserData() {
      return fetch(`http://localhost:8081/api/users/${keycloak.tokenParsed.sub}`, {
        method: 'GET',
        headers: {'Authorization': `Bearer ${keycloak.token}`}
      })
          .then(response => {
            if (!response.ok) {
              throw new Error('Error fetching user data');
            }
            return response.json();
          })
          .then(data => {
            this.user = data;
          });
    },
    fetchCompanyData() {
      return fetch(`http://localhost:8081/api/companies/${keycloak.tokenParsed.companyId}`, {
        method: 'GET',
        headers: {'Authorization': `Bearer ${keycloak.token}`}
      })
          .then(response => {
            if (!response.ok) {
              throw new Error('Error fetching company data');
            }
            return response.json();
          })
          .then(data => {
            this.company = data;
          });
    },

    fetchUserCourses() {
      return fetch(`http://localhost:8081/api/user-progress/${keycloak.tokenParsed.sub}`, {
        method: 'GET',
        headers: {'Authorization': `Bearer ${keycloak.token}`}
      })
          .then(response => {
            if (!response.ok) {
              throw new Error('Error fetching user courses');
            }
            return response.json();
          })
          .then(data => {
            this.courses = data;
          });
    },

    fetchAvailableCourses() {
      return fetch(`http://localhost:8081/api/companies/${keycloak.tokenParsed.companyId}/available-courses/${keycloak.tokenParsed.sub}`, {
        method: 'GET',
        headers: {'Authorization': `Bearer ${keycloak.token}`}
      })
          .then(response => {
            if (!response.ok) {
              throw new Error('Error fetching available courses');
            }
            return response.json();
          })
          .then(data => {
            this.availableCourses = data;
          });
    },
    incrementProgress(progressId) {
      fetch(`http://localhost:8081/api/user-progress/${progressId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${keycloak.token}`
        },
        body: JSON.stringify({progress: this.courses.find(c => c.id === progressId).progress + 1})
      })
          .then(response => {
            if (!response.ok) {
              throw new Error('Error updating course progress');
            }
            return response.json();
          })
          .then(updatedProgress => {
            // Update the progress in the local courses data
            const courseIndex = this.courses.findIndex(c => c.id === progressId);
            if (courseIndex > -1) {
              this.courses[courseIndex] = updatedProgress;
            }
          })
          .catch(error => {
            console.error('Error updating course progress:', error);
            // Handle the error appropriately (e.g., show an error message to the user)
          });
    },
    bookCourse(courseId) {
      fetch(`http://localhost:8081/api/users/${keycloak.tokenParsed.sub}/book-course`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${keycloak.token}`
        },
        body: JSON.stringify({courseId: courseId})
      })
          .then(response => {
            if (!response.ok) {
              return response.text().then(text => {
                throw new Error(text);
              });
            }
            return response.json();
          })
          .then(data => {
            this.courses.push(data);
            this.availableCourses = this.availableCourses.filter(c => c.courseId !== courseId);
          })
          .catch(error => {
            console.error('Error booking course:', error);
          });
    },
    goToCompanyDashboard() {
      if (this.isTeamLeader) { // Check if the user is a TeamLeader
        this.$router.push('/company-dashboard');
      } else {
        // Handle unauthorized access (e.g., display an error message)
      }
    },

    goToCourseDashboard() {
      if (this.isAdmin) { // Check if the user is an Admin
        this.$router.push('/course-dashboard');
      } else {
        // Handle unauthorized access (e.g., display an error message)
      }
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
