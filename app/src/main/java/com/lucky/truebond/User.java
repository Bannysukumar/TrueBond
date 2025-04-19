package com.lucky.truebond;

public class User {
    private String name;
    private String email;
    private String gender;
    private String coupleCode;
    private boolean isConnected;
    private String connectedTo;
    private long registrationDate;
    private boolean isSingle;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String name, String email, String gender, String coupleCode, boolean isConnected, String connectedTo, long registrationDate) {
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.coupleCode = coupleCode;
        this.isConnected = isConnected;
        this.connectedTo = connectedTo;
        this.registrationDate = registrationDate;
        this.isSingle = false; // Default to not single
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCoupleCode() {
        return coupleCode;
    }

    public void setCoupleCode(String coupleCode) {
        this.coupleCode = coupleCode;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getConnectedTo() {
        return connectedTo;
    }

    public void setConnectedTo(String connectedTo) {
        this.connectedTo = connectedTo;
    }

    public long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public void setSingle(boolean single) {
        isSingle = single;
    }
} 