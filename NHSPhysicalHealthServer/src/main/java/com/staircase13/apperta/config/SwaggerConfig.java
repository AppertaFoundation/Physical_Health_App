package com.staircase13.apperta.config;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

import static com.staircase13.apperta.config.ConfigConstants.SWAGGER_AUTH_SERVER;

// we use the production profile to ensure this isn't bootstrapped
// for integration tests
@Profile("production")
@Configuration
@EnableSwagger2
@Import(springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {

    private static final String AUTH_SERVER = "http://localhost:8080";
    private static final String SECURITY_SCHEMA_NAME = "oauth";

    private final BuildProperties buildProperties;

    private final String authServer;

    @Autowired
    public SwaggerConfig(BuildProperties buildProperties, Environment environment) {
        this.buildProperties = buildProperties;
        authServer = environment.getProperty(SWAGGER_AUTH_SERVER, AUTH_SERVER);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .ignoredParameterTypes(AuthenticationPrincipal.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.staircase13.apperta.api"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiEndPointsInfo())
                .securitySchemes(Arrays.asList(securityScheme()))
                .securityContexts(Arrays.asList(securityContext()));
    }

    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("Apperta Physical Health REST API")
                .description("Apperta Physical Health REST API")
                .contact(new Contact("staircase13 Ltd.", "https://www.staircase13.com", "info@staircase13.com"))
                .license("GNU Affero General Public License")
                .licenseUrl("https://www.gnu.org/licenses/agpl-3.0.en.html")
                .version(buildProperties.getVersion())
                .build();
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                .scopeSeparator(" ")
                .useBasicAuthenticationWithAccessCodeGrant(true)
                .build();
    }

    private SecurityScheme securityScheme() {
        GrantType grantType = new ResourceOwnerPasswordCredentialsGrant(authServer + "/oauth/token");

        SecurityScheme oauth = new OAuthBuilder().name(SECURITY_SCHEMA_NAME)
                .grantTypes(Arrays.asList(grantType))
                .scopes(Arrays.asList(scopes()))
                .build();
        return oauth;
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(
                        Arrays.asList(new SecurityReference(SECURITY_SCHEMA_NAME, scopes())))
                .forPaths(Predicates.or
                        (
                                PathSelectors.regex(regexForNoMatch("user/register")),
                                PathSelectors.regex(regexForNoMatch("user/passwordReset"))
                        ))
                .build();
    }

    private String regexForNoMatch(String notMatching) {
        return String.format("^((?!%s).)*$", notMatching);
    }

    private AuthorizationScope[] scopes() {
        /*
        Scopes is Optional in OAuth Spec, but the scopes element is mandatory in Swagger
        Therefore, we return an empty Scopes list
        See https://github.com/swagger-api/swagger-ui/issues/2150 for a discussion regarding this
         */
        return new AuthorizationScope[0];
    }

}
