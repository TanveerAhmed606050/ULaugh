package com.example.ulaugh.model;

public class LatestMessage {

    String date, message;
    String type;

    public LatestMessage(String date, String message, String messageType) {
        this.date = date;
        this.message = message;
        this.type = messageType;
    }

    public LatestMessage() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String seen_message) {
        this.type = seen_message;
    }
}
