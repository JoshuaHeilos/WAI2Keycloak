<template>
  <div class="main-content">
    <button @click="goToEmployeeDashboard">Go to Employee Dashboard</button>
    <h1>Company Dashboard</h1>
    <div v-if="company && isTeamLeader">
      <p>Company: {{ company.name }}</p>

      <h2>Booked Courses</h2>
      <ul>
        <li v-for="course in bookedCourses" :key="course.courseId">
          {{ course.name }} - {{ course.description }}
          <button @click="deleteCourseForCompany(course.courseId)">Delete Course</button>
        </li>
      </ul>

      <h2>Available Courses</h2>
      <ul>
        <li v-for="course in availableCourses" :key="course.courseId">
          {{ course.name }} - {{ course.description }}
          <button @click="bookCourseForCompany(course)">Book Course</button>
        </li>
      </ul>
    </div>
    <div v-else>
      <p v-if="!isTeamLeader">You do not have permission to view this dashboard.</p>
      <p v-else>Loading...</p>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      company: null,
      bookedCourses: [],
      availableCourses: [],
      role: sessionStorage.getItem('role')
    };
  },
  computed: {
    isTeamLeader() {
      return this.role === 'TeamLeader';
    }
  },
  created() {
    const companyId = sessionStorage.getItem('companyId');
    if (this.isTeamLeader) {
      this.fetchCompanyData(companyId);
      this.fetchBookedCourses(companyId);
      this.fetchAvailableCourses(companyId);
    }
  },
  methods: {
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
    fetchBookedCourses(companyId) {
      const requestInfo = {
        url: `http://localhost:8081/api/companies/${companyId}/booked-courses`,
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
            this.bookedCourses = data;
          })
          .catch(error => {
            console.error('Error fetching booked courses:', error);
          });
    },
    fetchAvailableCourses(companyId) {
      const requestInfo = {
        url: `http://localhost:8081/api/companies/${companyId}/available-courses`,
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
    bookCourseForCompany(course) {
      const requestInfo = {
        url: `http://localhost:8081/api/companies/${sessionStorage.getItem('companyId')}/book-course`,
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
          .then(response => {
            if (!response.ok) {
              return response.text().then(text => {
                throw new Error(text);
              });
            }
            return response.json();
          })
          .then(data => {
            this.bookedCourses.push(data);
            this.availableCourses = this.availableCourses.filter(c => c.courseId !== course.courseId);
          })
          .catch(error => {
            alert(`Error booking course for company: ${error.message}`);
          });
    },
    deleteCourseForCompany(courseId) {
      const requestInfo = {
        url: `http://localhost:8081/api/companies/${sessionStorage.getItem('companyId')}/delete-course`,
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        data: { courseId }
      };
      this.$emit('request-info-updated', requestInfo);

      fetch(requestInfo.url, {
        method: requestInfo.method,
        headers: requestInfo.headers,
        body: JSON.stringify(requestInfo.data),
        credentials: 'include'
      })
          .then(response => {
            if (!response.ok) {
              return response.text().then(text => {
                throw new Error(text);
              });
            }
            this.bookedCourses = this.bookedCourses.filter(c => c.courseId !== courseId);
            this.fetchAvailableCourses(sessionStorage.getItem('companyId'));
          })
          .catch(error => {
            alert(`Error deleting course for company: ${error.message}`);
          });
    },
    goToEmployeeDashboard() {
      this.$router.push('/employee-dashboard');
    }
  }
};
</script>
