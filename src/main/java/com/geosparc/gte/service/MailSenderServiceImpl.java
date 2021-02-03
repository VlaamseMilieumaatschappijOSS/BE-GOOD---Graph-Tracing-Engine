package com.geosparc.gte.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
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

    @Value("${mailservice.systemevents.to}")
    private String[] systemeventMailReceivers;

    @Value("${spring.mail.from}")
    private String from;

    @Override
    public void sendSystemMailLoadFail(Throwable ex) {
        if (systemeventMailReceivers == null || systemeventMailReceivers.length == 0) {
            LOGGER.info("Kan geen mail verzenden: geen 'to:' email-adressen opgegeven (parameter: mailservice.systemevents.to)");
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Hallo,\n\n");
            sb.append("Er is een fout opgetreden bij het herladen van de datasets. \n\n");
            sb.append("Dit is een geautomatiseerd bericht.\nMet vriendelijke groeten.\nTracing Server.\n\n\n");
            sb.append("Stacktrace:\n===========\n");
            sb.append(ExceptionUtils.getStackTrace(ex));

            StringBuilder sub = new StringBuilder();
            sub.append("[FAILED] Tracingserver [");
            sub.append(InetAddress.getLocalHost().getHostName());
            sub.append("] Herladen datasets.");

            sendSystemMail(sub.toString(), sb.toString(), systemeventMailReceivers);
        } catch (Exception e) {
            LOGGER.warning("Failed sending mail!\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void sendSystemMailLoadSuccess() {
        if (systemeventMailReceivers == null || systemeventMailReceivers.length == 0) {
            LOGGER.info("Kan geen mail verzenden: geen 'to:' email-adressen opgegeven (parameter: mailservice.systemevents.to)");
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Hallo,\n\n");
            sb.append("Datasets zijn correct geladen.\n\n");
            sb.append("Dit is een geautomatiseerd bericht.\nMet vriendelijke groeten.\nTracing Server.\n\n\n");

            StringBuilder sub = new StringBuilder();
            sub.append("[SUCCESS] Tracingserver [");
            sub.append(InetAddress.getLocalHost().getHostName());
            sub.append("] Herladen datasets.");

            sendSystemMail(sub.toString(), sb.toString(), systemeventMailReceivers);
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