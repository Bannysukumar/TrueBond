package com.lucky.truebond;

public class LoveReport {
    private int loveScore;
    private String mood;
    private String suggestion;
    private int coldZones;

    public LoveReport(int loveScore, String mood, String suggestion, int coldZones) {
        this.loveScore = loveScore;
        this.mood = mood;
        this.suggestion = suggestion;
        this.coldZones = coldZones;
    }

    public int getLoveScore() {
        return loveScore;
    }

    public String getMood() {
        return mood;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public int getColdZones() {
        return coldZones;
    }
} 