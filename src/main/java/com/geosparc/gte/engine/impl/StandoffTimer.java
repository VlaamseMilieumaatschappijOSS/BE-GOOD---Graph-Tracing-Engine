package com.geosparc.gte.engine.impl;

import org.geotools.util.logging.Logging;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

/**
 * @author Kristof Heirwegh
 * @since 27/01/20
 */
public class StandoffTimer {

    private static final Logger LOGGER = Logging.getLogger(StandoffTimer.class);

    private long retrytimeMillis;
    private long retrytimeMaxMillis;
    private Double standoffMultiplier;

    private long waitTime;
    private Thread waitingThread;
    private boolean cancelled = false;
    private int retryCount = 0;
    private boolean notificationSent;

    public boolean isCancelled() {
        return cancelled;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public boolean isNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(boolean sent) {
        this.notificationSent = sent;
    }

    public StandoffTimer(@Nonnull Long retrytimeMillis, @Nonnull Integer retrytimeMaxMinutes, Double standoffMultiplier) {
        this.retrytimeMaxMillis = (retrytimeMaxMinutes * 60000);
        this.retrytimeMillis = retrytimeMillis;
        this.standoffMultiplier = standoffMultiplier;
        this.waitTime = retrytimeMillis;
    }

    public void cancel() {
        this.waitTime = retrytimeMillis;
        this.retryCount = 0;
        this.notificationSent = false;
        this.cancelled = true;
        if (waitingThread != null && waitingThread.isAlive()) {
           waitingThread.interrupt();
        }
    }

    public void delay() {
        try {
            LOGGER.info("Waiting for: " + (waitTime / 1000) + " seconds...");
            cancelled = false;
            waitingThread = Thread.currentThread();
            waitingThread.sleep(waitTime);

            // -- update waittime only on succesful wait
            waitTime = (long) (standoffMultiplier * waitTime);
            if (waitTime > retrytimeMaxMillis) {
                waitTime = retrytimeMaxMillis;
            }
            retryCount++;

        } catch (InterruptedException ex) {
            // interrupted, no biggie
            LOGGER.fine("interrupted");
            cancelled = true;

        } finally {
            waitingThread = null;
        }
    }
}
