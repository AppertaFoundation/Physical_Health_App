package com.staircase13.apperta.auth.server;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.time.Duration;

/**
 * To ensure the configuration of the Auth Server and Resource Service is completely
 * separated, we wire up the various configuration 'by hand' instead of relying on
 * injection
 */
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    public interface ConfigDefaults {
        String DEFAULT_CMS_CLIENT_ID = "AppertaCms";
        String DEFAULT_CMS_CLIENT_PASSWORD = "secret";
        String DEFAULT_CLIENT_PASSWORD = "secret";
        String DEFAULT_CLIENT_USERNAME = "my-trusted-client";
        String DEFAULT_JWT_SIGNING_KEY = "123456789";
    }

    public interface ConfigKeys {
        String CMS_CLIENT_ID           = "apperta.cms.auth.client.clientId";
        String CMS_CLIENT_SECRET       = "apperta.cms.auth.client.clientSecret";
        String CMS_SERVER_REDIRECT_URI = "apperta.cms.auth.server.redirectUri";

        String OAUTH_CLIENT_PASSWORD = "apperta.oauth.client.password";
        String OAUTH_CLIENT_USERNAME = "apperta.oauth.client.name";
        String OAUTH_TOKEN_ACCESS_VALIDITY_DURATION = "apperta.oauth.token.access.validity.duration";
        String OAUTH_TOKEN_REFRESH_VALIDITY_DURATION = "apperta.oauth.token.refresh.validity.duration";
        String JWT_SIGNING_KEY = "apperta.oauth.jwt.signing.key";
    }

    private static final Logger LOG = LoggerFactory.getLogger(AuthServerConfig.class);

    private static final String REALM = "APPERTA_REALM";

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final Environment environment;

    @Autowired
    public AuthServerConfig(PasswordEncoder passwordEncoder,
                            AuthenticationManager authenticationManager,
                            Environment environment) {
        this.authenticationManager = authenticationManager;
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener({ContextRefreshedEvent.class})
    public void warnIfUsingDefaultCredentials() {
        if(isUsingDefaultCredentials()) {
            LOG.error(" ");
            LOG.error(StringUtils.repeat('!',70));
            LOG.error(StringUtils.repeat('!',70));
            LOG.error(" ");
            LOG.error("You are using the default configuration for one or more of the following properties. This is potentially insecure:");
            LOG.error(" ");
            LOG.error("* " + ConfigKeys.CMS_CLIENT_ID);
            LOG.error("* " + ConfigKeys.CMS_CLIENT_SECRET);
            LOG.error("* " + ConfigKeys.OAUTH_CLIENT_USERNAME);
            LOG.error("* " + ConfigKeys.OAUTH_CLIENT_PASSWORD);
            LOG.error("* " + ConfigKeys.JWT_SIGNING_KEY);
            LOG.error(" ");
            LOG.error(StringUtils.repeat('!',70));
            LOG.error(StringUtils.repeat('!',70));
            LOG.error(" ");
        }
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer.realm(REALM + "/client");

        // required for CMS login
        oauthServer.tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                /*
                This configuration defines a the Basic Auth credentials that must be used
                when retrieving a token for password grant
                 */
                .withClient(getClientUsername())
                    .authorizedGrantTypes("password", "refresh_token", "client_credentials")
                    .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
                    .scopes("read", "write", "trust")
                    .secret(passwordEncoder.encode(getClientPassword()))
                    .accessTokenValiditySeconds(getDurationConfigInSeconds(ConfigKeys.OAUTH_TOKEN_ACCESS_VALIDITY_DURATION))
                    .refreshTokenValiditySeconds(getDurationConfigInSeconds(ConfigKeys.OAUTH_TOKEN_REFRESH_VALIDITY_DURATION))
                .and()
                /*
                This configuration provides an OAuth web login page for CMS
                 */
                .withClient(getCmsClientId())
                    .secret(passwordEncoder.encode(getCmsClientSecret()))
                    .authorizedGrantTypes("authorization_code")
                    .scopes("user_info")
                    .autoApprove(true)
                // we return users to the cms login page to ensure the client filter can complete
                // authentication on the client side
                .redirectUris(getCmsServerRedirectUri());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .tokenStore(tokenStore())
                .accessTokenConverter(accessTokenConverter())
                .authenticationManager(authenticationManager);
    }

    public TokenStore tokenStore() throws Exception {
        return new JwtTokenStore(accessTokenConverter());
    }

    public JwtAccessTokenConverter accessTokenConverter() throws Exception {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        /*
         * We use a symmetric signing key as both the server and client are hosted
         * in the same application
         */
        converter.setSigningKey(getJwtSigningKey());
        /*
         * Because we build the convert manually, we have to invoke the spring callback
         */
        converter.afterPropertiesSet();
        return converter;
    }

    private String getCmsClientId() {
        return environment.getProperty(ConfigKeys.CMS_CLIENT_ID, ConfigDefaults.DEFAULT_CMS_CLIENT_ID);
    }

    private String getCmsClientSecret() {
        return environment.getProperty(ConfigKeys.CMS_CLIENT_SECRET, ConfigDefaults.DEFAULT_CMS_CLIENT_PASSWORD);
    }

    private String getCmsServerRedirectUri() {
        return environment.getProperty(ConfigKeys.CMS_SERVER_REDIRECT_URI);
    }

    private String getClientPassword() {
        return environment.getProperty(ConfigKeys.OAUTH_CLIENT_PASSWORD, ConfigDefaults.DEFAULT_CLIENT_PASSWORD);
    }

    private String getClientUsername() {
        return environment.getProperty(ConfigKeys.OAUTH_CLIENT_USERNAME, ConfigDefaults.DEFAULT_CLIENT_USERNAME);
    }

    private int getDurationConfigInSeconds(String configName) {
        return (int)environment.getProperty(configName, Duration.class).getSeconds();
    }

    private String getJwtSigningKey() {
        return environment.getProperty(ConfigKeys.JWT_SIGNING_KEY, ConfigDefaults.DEFAULT_JWT_SIGNING_KEY);
    }

    private boolean isUsingDefaultCredentials() {
        return ConfigDefaults.DEFAULT_CLIENT_USERNAME.equals(getClientUsername()) ||
               ConfigDefaults.DEFAULT_CLIENT_PASSWORD.equals(getClientPassword()) ||
               ConfigDefaults.DEFAULT_JWT_SIGNING_KEY.equals(getJwtSigningKey()) ||
               ConfigDefaults.DEFAULT_CMS_CLIENT_ID.equals(getJwtSigningKey()) ||
               ConfigDefaults.DEFAULT_CMS_CLIENT_PASSWORD.equals(getJwtSigningKey());
    }


}
