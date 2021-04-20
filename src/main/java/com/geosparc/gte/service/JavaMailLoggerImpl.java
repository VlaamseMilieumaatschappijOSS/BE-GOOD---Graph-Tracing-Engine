package com.geosparc.gte.service;

import org.geotools.util.logging.Logging;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.internet.MimeMessage;
import java.util.logging.Logger;

/**
 * Log Mails instead of sending them.
 */

public class JavaMailLoggerImpl extends JavaMailSenderImpl {

    private static final Logger LOGGER = Logging.getLogger(JavaMailLoggerImpl.class);

    private static final String MESSAGE = "If you wish to send a mailmessage instead of just logging, configure following properties:\n" +
            "    mailservice.systemevents.to: me@localhost\n" +
            "    spring.mail.from: tracing_bot@localhost\n" +
            "    spring.mail.host: localhost\n" +
            "    spring.mail.port: 25\n" +
            "    (spring.mail.username: me)\n" +
            "    (spring.mail.password: ***)\n" +
            "    (spring.mail.properties.mail.smtp.auth: false)\n" +
            "    (spring.mail.testConnection: true)";

    @Override
    protected void doSend(MimeMessage[] mimeMessages, @Nullable Object[] originalMessages) throws MailException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
        sb.append(MESSAGE);
        sb.append("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
        try {
            if (mimeMessages != null && mimeMessages.length > 0) {
                sb.append((String) mimeMessages[0].getContent());
                sb.append("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            }
        } catch (Exception ex) {
            LOGGER.fine(ex.getMessage());
        }
        LOGGER.info(sb.toString());
    }
}
