package com.lucky.truebond;

public class DisconnectRequest {
    private String requesterId;
    private String partnerId;
    private boolean accepted;

    // Default constructor required for Firebase
    public DisconnectRequest() {}

    public DisconnectRequest(String requesterId, String partnerId, boolean accepted) {
        this.requesterId = requesterId;
        this.partnerId = partnerId;
        this.accepted = accepted;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
} 