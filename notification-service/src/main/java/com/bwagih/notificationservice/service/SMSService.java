package com.bwagih.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SMSService implements Notify {
    public void notify(String message) {
        log.info("SMS service received");
    }
}
