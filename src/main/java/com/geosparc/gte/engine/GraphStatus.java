package com.geosparc.gte.engine;

/**
 * Class that represents the status of a graph. (Loading, complete)
 *
 * @author Oliver May
 */
public class GraphStatus {

    private long serial = -1;

    private Status status = Status.UNINITIALIZED;

    public long getSerial() {
        return serial;
    }

    public void setSerial(long serial) {
        this.serial = serial;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @author Oliver May
     */
    public enum Status {

        READY, UNINITIALIZED;

    }
}
