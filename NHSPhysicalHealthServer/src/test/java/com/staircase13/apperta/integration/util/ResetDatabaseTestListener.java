package com.staircase13.apperta.integration.util;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import javax.sql.DataSource;

public class ResetDatabaseTestListener implements TestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetDatabaseTestListener.class);

    @Override
    public void beforeTestMethod(TestContext testContext) {
        LOGGER.debug("Resetting Database before method '{}'",testContext.getTestMethod().getName());

        resetDatabase(testContext);
        loadTestData(testContext);

        LOGGER.debug("Database reset complete");
    }

    private void resetDatabase(TestContext testContext) {
        Flyway flyway = getBean(Flyway.class, testContext);
        flyway.clean();
        flyway.migrate();
    }

    private void loadTestData(TestContext testContext) {
        DataSource dataSource = getBean(DataSource.class, testContext);

        ResourceDatabasePopulator pop = new ResourceDatabasePopulator();
        pop.addScript(new ClassPathResource("data.sql"));
        pop.execute(dataSource);
    }

    private <T> T getBean(Class<T> requiredType, TestContext testContext) {
        return testContext.getApplicationContext().getBean(requiredType);
    }
}
