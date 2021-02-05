package com.staircase13.apperta.ehrconnector.interfaces;

/**
 * Base data transfer object containing the basic demographics details for a patient.
 */
public interface IDemographicsDto {

    /**
     * Get the numeric id representing the patient demographics record in the remote system
     * @return The patient ID.
     */
    long getPartyId();

    /**
     * Set the numeric id representing the patient demographics record in the remote system
     * @param partyId The patient ID.
     */
    void setPartyId(long partyId);
}
