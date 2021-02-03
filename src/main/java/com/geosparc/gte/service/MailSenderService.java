package com.geosparc.gte.service;

/**
 * @author Kristof Heirwegh
 * @since 27/01/20
 */
public interface MailSenderService {

    void sendSystemMailLoadFail(Throwable ex);

    void sendSystemMailLoadSuccess();

}