import { createApp } from 'vue';
import App from './App.vue';
import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from './views/LoginPage.vue';
import RegisterPage from './views/RegisterPage.vue';
import EmployeeDashboard from './views/EmployeeDashboard.vue';
import CompanyDashboard from './views/CompanyDashboard.vue';
import CourseDashboard from './views/CourseDashboard.vue';
import './global.css';

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginPage },
    { path: '/register', component: RegisterPage },
    { path: '/employee-dashboard', component: EmployeeDashboard },
    { path: '/course-dashboard', component: CourseDashboard },
    { path: '/company-dashboard', component: CompanyDashboard }  // Remove companyId parameter
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

createApp(App).use(router).mount('#app');
