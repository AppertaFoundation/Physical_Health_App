package com.staircase13.apperta.auth.client;

import com.staircase13.apperta.auth.server.AuthServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.Filter;

import static com.staircase13.apperta.entities.Authority.MANAGE_CMS;

/**
 * To ensure the configuration of the Auth Server and Resource Service is completely
 * separated, we wire up the various configuration 'by hand' because the spring boot
 * helpers/magic makes assumptions that we don't want
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig {

    public interface ConfigKeys {
        String CMS_USER_INFO_SERVICE_URI = "apperta.cms.auth.server.userInfoServiceUri";
        String CMS_ACCESS_TOKEN_URI = "apperta.cms.auth.client.accessTokenUri";
        String CMS_USER_AUTHORIZATION_URI = "apperta.cms.auth.client.userAuthorizationUri";
    }

    /**
     * Allows access to the password reset and iam user info pages without the need for authentication
     *
     * Also allows access to h2 console in dev
     *
     * We also remove authentication from the root URL so the user sees a 404. This is to avoid
     * a login page appearing (because authentication is turned on by default),  which subsequently
     * results in the user being redirected to 404, which is confusing!
     *
     * If the auth server was ever moved into a separate module, this configuration
     * should be moved with it
     */
    @Configuration
    @Order(0)
    public static class IamConfig  extends ResourceServerConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                        .antMatchers(
                                // user info
                                "/iam/**",
                                // health check
                                "/actuator/**",
                                // ensures 404 appears when accessing root
                                "/",
                                // h2
                                "/h2-console/**",
                                // swagger
                                "/swagger-ui.html","/webjars/**","/swagger-resources/**","/v2/**","/csrf")
                        .and()
                        .authorizeRequests()
                        .antMatchers("**")
                        .permitAll();
        }
    }

    /**
     * Allows access to API endpoints without authentication, but configures
     * support for endpoint specific authorisation rules that delegate to
     * the OAuth token to determine access
     */
    @Configuration
    @Order(1)
    public static class OauthConfig extends ResourceServerConfigurerAdapter {

        private final ResourceServerUserDetailsService resourceServerUserDetailsService;

        private final Environment environment;

        public OauthConfig(ResourceServerUserDetailsService resourceServerUserDetailsService, Environment environment) {
            this.resourceServerUserDetailsService = resourceServerUserDetailsService;
            this.environment = environment;
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                        .antMatchers("/api/**")
                        .and()
                            .authorizeRequests()
                        .antMatchers("**")
                        .permitAll();

            // required for H2 console
            http.headers().frameOptions().disable();
        }

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources
                    .tokenStore(tokenStore());
        }

        private TokenStore tokenStore() throws Exception {
            return new JwtTokenStore(jwtAccessTokenConverter());
        }

        private JwtAccessTokenConverter jwtAccessTokenConverter() throws Exception {
            JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
            /*
             * We use a symmetric signing key as both the server and client are hosted
             * in the same application
             */
            converter.setSigningKey(getJwtSigningKey());
            converter.setAccessTokenConverter(accessTokenConverter());
            /*
             * Because we build the convert manually, we have to invoke the spring callback
             */
            converter.afterPropertiesSet();
            return converter;
        }

        public DefaultAccessTokenConverter accessTokenConverter() {
            DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
            DefaultUserAuthenticationConverter userAuthenticationConverter = new DefaultUserAuthenticationConverter();
            userAuthenticationConverter.setUserDetailsService(resourceServerUserDetailsService);
            accessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
            return accessTokenConverter;
        }

        private String getJwtSigningKey() {
            return environment.getProperty(AuthServerConfig.ConfigKeys.JWT_SIGNING_KEY, AuthServerConfig.ConfigDefaults.DEFAULT_JWT_SIGNING_KEY);
        }

    }

    /**
     * Configures web-based login for the CMS admin pages
     */
    @Configuration
    @EnableWebSecurity
    @EnableOAuth2Client
    // https://spring.io/guides/tutorials/spring-boot-oauth2/#_social_login_manual
    // https://www.baeldung.com/sso-spring-security-oauth2
    @Order(2)
    public static class CmsWebAuthConfig extends WebSecurityConfigurerAdapter {

        private final ResourceServerUserDetailsService resourceServerUserDetailsService;

        private final OAuth2ClientContext oauth2ClientContext;

        private final Environment environment;

        @Autowired
        public CmsWebAuthConfig(ResourceServerUserDetailsService resourceServerUserDetailsService, OAuth2ClientContext oauth2ClientContext, Environment environment) {
            this.resourceServerUserDetailsService = resourceServerUserDetailsService;
            this.oauth2ClientContext = oauth2ClientContext;
            this.environment = environment;
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .requestMatchers()
                    .antMatchers("/cms/**").and()
                        .authorizeRequests()
                        .antMatchers("**")
                            .hasAuthority(MANAGE_CMS.name())
                            .and()
                            // because we register our auth filter manually (see ssoFilter()) success urls in formLogin are not used
                            // so we register the success handler directly with the ssoFilter
                            .formLogin().loginPage("/cms/login")
                            .and()
                            .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);

        }

        private Filter ssoFilter() {
            OAuth2ClientAuthenticationProcessingFilter authFilter = new OAuth2ClientAuthenticationProcessingFilter("/cms/login");

            OAuth2RestTemplate authRestTemplate = new OAuth2RestTemplate(authClientConfig(), oauth2ClientContext);
            authFilter.setRestTemplate(authRestTemplate);

            UserInfoTokenServices tokenServices = new UserInfoTokenServices(environment.getProperty(ConfigKeys.CMS_USER_INFO_SERVICE_URI), authClientConfig().getClientId());
            tokenServices.setRestTemplate(authRestTemplate);
            authFilter.setTokenServices(tokenServices);

            authFilter.setAuthenticationSuccessHandler(new ForwardAuthenticationSuccessHandler("/cms/pages"));

            return authFilter;
        }

        @Bean
        public AuthorizationCodeResourceDetails authClientConfig() {
            AuthorizationCodeResourceDetails resourceDetails = new AuthorizationCodeResourceDetails();
            resourceDetails.setClientId(getCmsClientId());
            resourceDetails.setClientSecret(getCmsClientSecret());
            resourceDetails.setAccessTokenUri(getCmsAccessTokenUri());
            resourceDetails.setUserAuthorizationUri(getCmsUserAuthorizationUri());
            resourceDetails.setPreEstablishedRedirectUri(getCmsServerRedirectUri());
            resourceDetails.setUseCurrentUri(false);
            return resourceDetails;
        }

        @Bean
        public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
            FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(filter);
            registration.setOrder(-100);
            return registration;
        }

        @Override
        protected UserDetailsService userDetailsService() {
            // enhance user details returned from auth server with local authorities/roles
            return resourceServerUserDetailsService;
        }

        private String getCmsAccessTokenUri() {
            return environment.getProperty(ConfigKeys.CMS_ACCESS_TOKEN_URI);
        }

        private String getCmsUserAuthorizationUri() {
            return environment.getProperty(ConfigKeys.CMS_USER_AUTHORIZATION_URI);
        }

        private String getCmsClientId() {
            return environment.getProperty(AuthServerConfig.ConfigKeys.CMS_CLIENT_ID, AuthServerConfig.ConfigDefaults.DEFAULT_CMS_CLIENT_ID);
        }

        private String getCmsClientSecret() {
            return environment.getProperty(AuthServerConfig.ConfigKeys.CMS_CLIENT_SECRET, AuthServerConfig.ConfigDefaults.DEFAULT_CMS_CLIENT_PASSWORD);
        }

        private String getCmsServerRedirectUri() {
            return environment.getProperty(AuthServerConfig.ConfigKeys.CMS_SERVER_REDIRECT_URI);
        }
    }
}
