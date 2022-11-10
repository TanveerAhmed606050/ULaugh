package com.example.ulaugh.model;

public class InboxListModel {
    String conversation_id;
    String other_user_firebase_id;
    String other_user_firebase_name;
    String profile_pic;
    String message_token;
    LatestMessage latest_message;

    public InboxListModel(String conversation_id, String other_user_firebase_id, String other_user_firebase_name, String profile_pic, String message_token, LatestMessage latest_message) {
        this.conversation_id = conversation_id;
        this.other_user_firebase_id = other_user_firebase_id;
        this.other_user_firebase_name = other_user_firebase_name;
        this.profile_pic = profile_pic;
        this.message_token = message_token;
        this.latest_message = latest_message;
    }

    public InboxListModel() {
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(String conversation_id) {
        this.conversation_id = conversation_id;
    }

    public String getOther_user_firebase_id() {
        return other_user_firebase_id;
    }

    public void setOther_user_firebase_id(String other_user_firebase_id) {
        this.other_user_firebase_id = other_user_firebase_id;
    }

    public String getOther_user_firebase_name() {
        return other_user_firebase_name;
    }

    public void setOther_user_firebase_name(String other_user_firebase_name) {
        this.other_user_firebase_name = other_user_firebase_name;
    }

    public LatestMessage getLatest_message() {
        return latest_message;
    }

    public void setLatest_message(LatestMessage latest_message) {
        this.latest_message = latest_message;
    }
}
