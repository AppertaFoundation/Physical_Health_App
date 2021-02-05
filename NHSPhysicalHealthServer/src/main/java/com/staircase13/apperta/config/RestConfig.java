package com.staircase13.apperta.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.DispatcherServlet;

import java.time.Duration;
import java.util.Collections;

import static com.staircase13.apperta.ehrconnector.ConfigConstants.REST_CONNECTION_TIMEOUT;
import static com.staircase13.apperta.ehrconnector.ConfigConstants.REST_READ_TIMEOUT;


@Configuration
@EnableRetry
public class RestConfig {

    @Autowired
    private Environment environment;

    @Bean
    // Scope is prototype because rest template consumers store long lived auth information on the rest template
    // interceptor list that is specific to each session.
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Autowired
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        SimpleClientHttpRequestFactory simpleClientFactory = new SimpleClientHttpRequestFactory();

        Duration connectionTimeout = Duration.parse(environment.getProperty(REST_CONNECTION_TIMEOUT));
        simpleClientFactory.setConnectTimeout((int)connectionTimeout.toMillis());

        Duration readTimeout = Duration.parse(environment.getProperty(REST_READ_TIMEOUT));
        simpleClientFactory.setReadTimeout((int)readTimeout.toMillis());

        // We use the BufferingClientHttpRequestFactory to ensure that the body stream is still
        // available once our interceptor has read it.
        //
        // See https://stackoverflow.com/questions/7952154/spring-resttemplate-how-to-enable-full-debugging-logging-of-requests-responses
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(simpleClientFactory);
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setInterceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()));
        return restTemplate;
    }

    @Bean
    public BeanPostProcessor dispatcherServletConfigurer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                setConfig(bean);
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                setConfig(bean);
                return bean;
            }

            private void setConfig(Object bean) {
                if (bean instanceof DispatcherServlet) {
                    DispatcherServlet dispatcherServlet = DispatcherServlet.class.cast(bean);
                    /*
                    Required to ensure 404s throw an exception that we can handle in our exception handler.

                    Note that we can't use 'spring.mvc.throw-exception-if-no-handler-found' because that requires
                    the use of @EnableWebMvc, which 'breaks' the Spring Boot MVC configuration.
                    */
                    dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
                }
            }
        };
    }
}
