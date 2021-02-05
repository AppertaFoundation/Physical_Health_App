package com.staircase13.apperta;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT2M")
// This needs to be declared at the start - it filters out all of the switchable connectors. Configuration within the connectors
// package will ensure the correct connectors are included for the application configuration.
@ComponentScan(
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.staircase13\\.apperta\\.ehrconnector\\.impls..*"),
		})
public class AppertaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppertaApplication.class, args);
	}

}

