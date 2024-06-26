import { createStore } from 'vuex';

const store = createStore({
    state: {
        keycloak: null,
    },
    mutations: {
        setKeycloak(state, keycloak) {
            state.keycloak = keycloak;
        },
    },
    actions: {},
    modules: {},
});

export default store;
