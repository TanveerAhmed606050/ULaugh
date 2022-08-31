package com.example.ulaugh.model;

public class LatestMessage {

    String date, message;
    boolean seen_message;

    public LatestMessage(String date, String message, boolean seen_message) {
        this.date = date;
        this.message = message;
        this.seen_message = seen_message;
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

    public boolean isSeen_message() {
        return seen_message;
    }

    public void setSeen_message(boolean seen_message) {
        this.seen_message = seen_message;
    }
}
