import Keycloak from 'keycloak-js';

const keycloakConfig = {
    url: 'http://localhost:8080',
    realm: 'testrealm',
    clientId: 'account',
};

const keycloak = Keycloak(keycloakConfig);

export default keycloak;