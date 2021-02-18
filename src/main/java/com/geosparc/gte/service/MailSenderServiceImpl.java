package com.geosparc.gte.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * @author Kristof Heirwegh
 * @since 27/01/20
 */
@Service
public class MailSenderServiceImpl implements MailSenderService {

    private static final Logger LOGGER = Logging.getLogger(MailSenderServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mailservice.systemevents.to:me@localhost}")
    private String[] systemeventMailReceivers;

    @Value("${mailservice.systemevents.locale:nl}")
    private String systemLocaleString;

    @Value("${spring.mail.from:me@localhost}")
    private String from;

    private Locale systemLocale;

    @Autowired
    private MessageSource messageSource;

    @PostConstruct
    private void init() {
        if (!StringUtils.isEmpty(systemLocaleString)) {
            systemLocale = Locale.forLanguageTag(systemLocaleString);
        }
        if (systemLocale == null) {
            systemLocale = Locale.getDefault();
        }
        LOGGER.info("Locale for mail set to: " + systemLocale.toString());
    }

    @Override
    public void sendSystemMailLoadFail(Throwable ex) {
        if (systemeventMailReceivers == null || systemeventMailReceivers.length == 0) {
            LOGGER.info("Cannot send mail: no 'to:' email-addresses provided (parameter: mailservice.systemevents.to)");
            return;
        }

        try {
            final String subject = messageSource.getMessage("mail_load_fail_subject", new Object[] {InetAddress.getLocalHost().getHostName()}, systemLocale);
            final String body = messageSource.getMessage("mail_load_fail_body", new Object[] {ExceptionUtils.getStackTrace(ex)}, systemLocale);
            sendSystemMail(subject, body, systemeventMailReceivers);
        } catch (Exception e) {
            LOGGER.warning("Failed sending mail!\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void sendSystemMailLoadSuccess() {
        if (systemeventMailReceivers == null || systemeventMailReceivers.length == 0) {
            LOGGER.info("Cannot send mail: no 'to:' email-addresses provided (parameter: mailservice.systemevents.to)");
            return;
        }

        try {
            final String subject = messageSource.getMessage("mail_load_success_subject", new Object[] {InetAddress.getLocalHost().getHostName()}, systemLocale);
            final String body = messageSource.getMessage("mail_load_success_body", new Object[] {}, systemLocale);
            sendSystemMail(subject, body, systemeventMailReceivers);
        } catch (Exception ex) {
            LOGGER.warning("Failed sending mail!\n" + ExceptionUtils.getStackTrace(ex));
        }
    }


    // Use it to send Simple text emails
    protected void sendSystemMail(String subject, String body, String[] to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);

        LOGGER.fine(subject);
        LOGGER.fine(body);
    }

}