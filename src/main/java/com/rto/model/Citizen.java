package com.rto.model;

public class Citizen extends User {
    private String email;
    public Citizen(String id, String username, String password, String email) {
        super(id, username, password, "CITIZEN");
        this.email = email;
    }
    public String getEmail() { return email; }
}
