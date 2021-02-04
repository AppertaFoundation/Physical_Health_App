package com.staircase13.apperta.ehrconnector;

import com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.EhrTemplate;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.interfaces.IAppRegistration;
import com.staircase13.apperta.repository.TemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EhrHealthChecker {

    private static final Logger LOG = LoggerFactory.getLogger(EhrHealthChecker.class);

    @Autowired
    private List<IAppRegistration> appRegistrations;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private EhrTemplate ehrTemplate;

    private boolean ehrOk;
    private boolean ehrConnectionOk;
    private boolean ehrTemplateSupported;

    @EventListener({ContextRefreshedEvent.class})
    @Scheduled(cron = "${apperta.ehr.healthcheck.schedule.cron}")
    public void checkHealth() {

        LOG.debug("EhrTemplateCheck scheduled health check");
        checkAppRegistrations();

        ehrOk = checkEhrTemplates();

        if (!ehrOk){
            LOG.error("Connected EHR not available or does not support app");
        }
    }

    public void checkEhrHealth() {
        ehrOk = checkEhrTemplates();
    }

    public boolean getHealth() {
        return ehrOk;
    }

    public boolean getConnectionHealth() {
        return ehrConnectionOk;
    }

    public boolean checkTemplateSupport() {
        return ehrTemplateSupported;
    }

    private boolean checkEhrTemplates(){
        boolean ok = true;

        try {
            List<String> requiredTemplates = templateRepository.findDistinctTemplates();

            List<String> templates = ehrTemplate.fetchTemplates();

            List<String> missingTemplates = new ArrayList<>();

            boolean templateMissing = false;
            for (String required : requiredTemplates) {
                if (!templates.contains(required)) {
                    LOG.info("Ehr server missing template " + required);
                    missingTemplates.add(required);
                    templateMissing = true;
                }
            }

            ehrTemplateSupported = !templateMissing;
            ehrConnectionOk = true;
        } catch (EhrOperationException eoe) {
            LOG.warn("Health check ehr connection failed", eoe);
            ehrConnectionOk = false;
        } catch (EhrAuthenticationException ae) {
            LOG.warn("Health check ehr failed on authentication exception");
            ehrConnectionOk = false;
        }

        ok = ehrConnectionOk && ehrTemplateSupported;

        return ok;
    }

    public void checkAppRegistrations() {
        for(IAppRegistration registration : appRegistrations) {
            try {
                registration.runAppRegistration();
            } catch (Exception e) {
                LOG.warn("Issue running app registration", e);
            }
        }
    }

    private void loadMissingTemplates(List<String> templates) {
        // TODO: if we have the opts available then we can load the templates and recheck
    }

}
