package com.staircase13.apperta.ehrconnector.interfaces;

/**
 * Base EHR details transfer object, representing the common details to be stored for a health record.
 */
public interface IEhrDetailsDto {

    /**
     * Get the identifier representing this electronic health record.
     * @return The EHR identifier.
     */
    String getEhrId();
}
