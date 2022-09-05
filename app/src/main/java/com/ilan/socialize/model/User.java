package com.ilan.socialize.model;

public class User {

    private String email;
    private String name;
    private String username;
    private String bio;
    private String imageurl;
    private String id;

    public User() {}

    public User(String email, String name, String username, String bio, String imageurl, String id) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.bio = bio;
        this.imageurl = imageurl;
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
