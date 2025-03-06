package com.example.lorekeeper.models;

public class User {
    public String userId, nickname, email, imageURL;


    public User() {
    }
    public User(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
    public User(String userId, String nickname, String email, String imageURL) {
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.imageURL= imageURL;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String profileImage) {
        this.imageURL = profileImage;
    }
}
