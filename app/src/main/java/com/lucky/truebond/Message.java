package com.lucky.truebond;

public class Message {
    private String id;
    private String text;
    private String senderId;
    private String receiverId;
    private String time;
    private long timestamp;
    private boolean isRead;
    private boolean isTyping;
    private boolean isDeleted;

    public Message() {
        // Default constructor required for Firebase
    }

    public Message(String text, String senderId, String receiverId) {
        this.text = text;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = System.currentTimeMillis();
        this.time = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(new java.util.Date(this.timestamp));
        this.isRead = false;
        this.isTyping = false;
        this.isDeleted = false;
    }

    public Message(String id, String senderId, String text, String time, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.time = time;
        this.isRead = isRead;
        this.timestamp = System.currentTimeMillis();
        this.isTyping = false;
        this.isDeleted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
} 