package com.example.ulaugh.model;

public class Users {

    String user_name ;
    String firebase_id;
    String personal_user_id;
    String phone_number;
    String user_email;
    String status;

//    public Users(String user_name, String firebase_id, String personal_user_id, String phone_number, String user_email, String status) {
//        this.user_name = user_name;
//        this.firebase_id = firebase_id;
//        this.personal_user_id = personal_user_id;
//        this.phone_number = phone_number;
//        this.user_email = user_email;
//        this.status=status;
//    }

    public Users() {
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }


    public String getFirebase_id() {
        return firebase_id;
    }

    public void setFirebase_id(String firebase_id) {
        this.firebase_id = firebase_id;
    }

    public String getPersonal_user_id() {
        return personal_user_id;
    }

    public void setPersonal_user_id(String personal_user_id) {
        this.personal_user_id = personal_user_id;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
