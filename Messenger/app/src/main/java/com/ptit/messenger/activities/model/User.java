package com.ptit.messenger.activities.model;

public class User {
    String uid,name,address,phone,profileImage;

    public User(){

    }

    public User(String uid, String name, String address, String phone, String profileImage) {
        this.uid = uid;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
