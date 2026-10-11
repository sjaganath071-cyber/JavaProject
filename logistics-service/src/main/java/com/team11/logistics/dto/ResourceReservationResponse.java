package com.team11.logistics.dto;

public class ResourceReservationResponse {

    private boolean success;
    private String reservationReference;
    private String message;

    public ResourceReservationResponse() {
    }

    public ResourceReservationResponse(boolean success, String reservationReference, String message) {
        this.success = success;
        this.reservationReference = reservationReference;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReservationReference() {
        return reservationReference;
    }

    public void setReservationReference(String reservationReference) {
        this.reservationReference = reservationReference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
