package com.geosparc.gte.service;

import org.geotools.util.logging.Logging;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * @author Kristof Heirwegh
 * @since 27/01/20
 */
public class TestMailSenderService implements MailSenderService {

    private static final Logger LOGGER = Logging.getLogger(TestMailSenderService.class);

    public int fail;
    public int success;

    @Override
    public void sendSystemMailLoadFail(Throwable ex) {
        fail++;
        LOGGER.info("Sending [sendSystemMailLoadFail] mail (if this wasn't a test)");
    }

    @Override
    public void sendSystemMailLoadSuccess() {
        success++;
        LOGGER.info("Sending [sendSystemMailLoadSuccess] mail (if this wasn't a test)");
    }

}