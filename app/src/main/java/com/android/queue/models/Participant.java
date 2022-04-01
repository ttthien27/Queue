package com.android.queue.models;

public class Participant {
    public String getWaiterPhone() {
        return waiterPhone;
    }

    public void setWaiterPhone(String waiterPhone) {
        this.waiterPhone = waiterPhone;
    }

    public String getWaiterName() {
        return waiterName;
    }

    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName;
    }

    public Long getWaiterNumber() {
        return waiterNumber;
    }

    public void setWaiterNumber(Long waiterNumber) {
        this.waiterNumber = waiterNumber;
    }

    public String getWaiterState() {
        return waiterState;
    }

    public void setWaiterState(String waiterState) {
        this.waiterState = waiterState;
    }

    public String waiterPhone;
    public String waiterName;
    public Long waiterNumber;
    public String waiterState;

    public Participant(String waiterPhone, String waiterName, String waiterState) {
        this.waiterPhone = waiterPhone;
        this.waiterName = waiterName;
        this.waiterState = waiterState;
    }

    public Participant(String waiterPhone, String waiterName, Long waiterNumber, String waiterState) {
        this.waiterPhone = waiterPhone;
        this.waiterName = waiterName;
        this.waiterState = waiterState;
        this.waiterNumber = waiterNumber;
    }

    /*public Participant( String waiterName, Long waiterNumber, String waiterPhone, String waiterState) {

        this.waiterName = waiterName;
        this.waiterState = waiterState;
        this.waiterPhone = waiterPhone;
        this.waiterNumber = waiterNumber;
    }*/

    public Participant() {}
}
