import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import keycloak from './keycloak';

keycloak.init({ onLoad: 'login-required' }).then(authenticated => {
    if (!authenticated) {
        console.log('User is not authenticated, reloading page.');
        window.location.reload();
    } else {
        console.log('User is authenticated.');

        const app = createApp(App);
        app.use(router);
        app.use(store);
        app.mount('#app');

        store.commit('setKeycloak', keycloak);
    }

    // Token refresh
    setInterval(() => {
        keycloak.updateToken(70).then(refreshed => {
            if (refreshed) {
                console.log('Token refreshed');
            } else {
                console.warn('Token not refreshed, valid for ' + Math.round(keycloak.tokenParsed.exp + keycloak.timeSkew - new Date().getTime() / 1000) + ' seconds');
            }
        }).catch(error => {
            console.error('Failed to refresh token', error);
            keycloak.logout().then(() => {
                window.location.reload();
            });
        });
    }, 60000);

}).catch(error => {
    console.error('Keycloak initialization failed', error);
});
