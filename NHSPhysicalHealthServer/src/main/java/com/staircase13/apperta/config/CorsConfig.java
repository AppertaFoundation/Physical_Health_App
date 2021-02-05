package com.staircase13.apperta.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

import static com.staircase13.apperta.config.ConfigConstants.CORS_DOMAIN_LIST;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorsConfig.class);

    private final Environment environment;

    @Autowired
    public CorsConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public FilterRegistrationBean addCorsFilter() {
        String[] corsOrigins = environment.getProperty(CORS_DOMAIN_LIST, String[].class);
        List<String> corsOriginsList = Arrays.asList(corsOrigins);
        LOGGER.info("Allowed CORS Origins are '{}'", Arrays.asList(corsOrigins));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(corsOriginsList);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Add handlers for other content
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
