package com.staircase13.apperta.actuator;

import com.staircase13.apperta.ehrconnector.EhrHealthChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.DefaultHealthIndicatorRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthIndicatorConfig {

    private final EhrHealthChecker ehrHealthChecker;

    @Autowired
    public HealthIndicatorConfig(EhrHealthChecker ehrHealthChecker) {
        this.ehrHealthChecker = ehrHealthChecker;
    }

    @Bean(name = "ehr")
    CompositeHealthIndicator compositeEhrHealthIndicator() {
        DefaultHealthIndicatorRegistry registry = new DefaultHealthIndicatorRegistry();

        registry.register("connection", () -> {
            if(ehrHealthChecker.getConnectionHealth()) {
                return Health.up().build();
            } else {
                return Health.down().build();
            }
        });

        registry.register("templateSupport", () -> {
            if(ehrHealthChecker.checkTemplateSupport()) {
                return Health.up().build();
            } else {
                return Health.down().build();
            }
        });

        return new CompositeHealthIndicator(new OrderedHealthAggregator(), registry);
    }

}
