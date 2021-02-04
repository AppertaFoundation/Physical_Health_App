package com.staircase13.apperta.ehrconnector.interfaces;

/**
 * Interface which needs to be implemented in a bean to be run during context refreshes to allow any inbuilt
 * applications to register themselves with ehr templates or query templates. These are registered prior to
 * performing health checks to see if the ehr server supports the necessary templates.
 */
public interface IAppRegistration {

    void runAppRegistration();
}
