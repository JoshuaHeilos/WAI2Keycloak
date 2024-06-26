import { createRouter, createWebHistory } from 'vue-router';

import store from '@/store';
import keycloak from '@/keycloak';
import CompanyDashboard from "@/views/CompanyDashboard.vue";
import CourseDashboard from "@/views/CourseDashboard.vue";
import EmployeeDashboard from "@/views/EmployeeDashboard.vue"; // Import Keycloak instance


const routes = [
    {
        path: '/',
        redirect: '/employee-dashboard' // Redirect root to home, will handle auth check in beforeEach
    },
    {
        path: '/login',
        redirect: '/employee-dashboard' // Redirect root to home, will handle auth check in beforeEach
    },
    {
        path: '/employee-dashboard',
        name: 'EmployeeDashboard',
        component: EmployeeDashboard,
        meta: { requiresAuth: true }
    },
    {
        path: '/company-dashboard',
        name: 'CompanyDashboard',
        component: CompanyDashboard,
        meta: { requiresAuth: true }
    },
    {
        path: '/course-dashboard',
        name: 'CourseDashboard',
        component: CourseDashboard,
        meta: { requiresAuth: true }
    }



];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!store.state.keycloak || !store.state.keycloak.authenticated) {
            // Redirect to Keycloak login page if not authenticated
            keycloak.login();
        } else {
            next();
        }
    } else {
        next();
    }
});

export default router;
