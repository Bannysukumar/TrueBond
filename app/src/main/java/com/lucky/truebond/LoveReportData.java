package com.lucky.truebond;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class LoveReportData {
    private String id;
    private String chatId;
    private int loveScore;
    private String mood;
    private int coldZones;
    private String suggestion;
    private long timestamp;

    public LoveReportData() {
        // Default constructor required for Firebase
    }

    public LoveReportData(String chatId, LoveReport report) {
        this.chatId = chatId;
        this.loveScore = report.getLoveScore();
        this.mood = report.getMood();
        this.coldZones = report.getColdZones();
        this.suggestion = report.getSuggestion();
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public int getLoveScore() {
        return loveScore;
    }

    public String getMood() {
        return mood;
    }

    public int getColdZones() {
        return coldZones;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public long getTimestamp() {
        return timestamp;
    }
} 