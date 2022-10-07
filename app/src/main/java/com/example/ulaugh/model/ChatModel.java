package com.example.ulaugh.model;

public class ChatModel {
    String message_id, type, content_message, date, sender_firebase_id, sender_user_name, receiver_firebase_id, receiver_user_name;
    boolean seen_message;

    public ChatModel(String message_id, String type, String content_message, String date, String sender_firebase_id, String sender_user_name, String receiver_firebase_id, String receiver_user_name) {
        this.message_id = message_id;
        this.type = type;
        this.content_message = content_message;
        this.date = date;
        this.sender_firebase_id = sender_firebase_id;
        this.sender_user_name = sender_user_name;
        this.receiver_firebase_id = receiver_firebase_id;
        this.receiver_user_name = receiver_user_name;
    }

    public ChatModel() {
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent_message() {
        return content_message;
    }

    public void setContent_message(String content_message) {
        this.content_message = content_message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSender_firebase_id() {
        return sender_firebase_id;
    }

    public void setSender_firebase_id(String sender_firebase_id) {
        this.sender_firebase_id = sender_firebase_id;
    }

    public String getSender_user_name() {
        return sender_user_name;
    }

    public void setSender_user_name(String sender_user_name) {
        this.sender_user_name = sender_user_name;
    }


    public String getReceiver_firebase_id() {
        return receiver_firebase_id;
    }

    public void setReceiver_firebase_id(String receiver_firebase_id) {
        this.receiver_firebase_id = receiver_firebase_id;
    }

    public String getReceiver_user_name() {
        return receiver_user_name;
    }

    public void setReceiver_user_name(String receiver_user_name) {
        this.receiver_user_name = receiver_user_name;
    }

    public boolean isSeen_message() {
        return seen_message;
    }

    public void setSeen_message(boolean seen_message) {
        this.seen_message = seen_message;
    }
}
