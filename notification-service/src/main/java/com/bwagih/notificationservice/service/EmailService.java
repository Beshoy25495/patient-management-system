package com.bwagih.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService implements Notify{
    @Override
    public void notify(String message) {
        log.info("Email service received");
    }
}
