package com.bwagih.analyticsservice.service;

import patient.events.PatientEvent;

public interface HandlingConsumer {
    public PatientEvent handleEvent(byte[] genericEvent) throws Exception;
}
