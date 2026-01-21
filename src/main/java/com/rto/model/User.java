package com.rto.model;

import java.io.Serializable;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String id;
    protected String username;
    protected String password;
    protected String role;

    public User(String id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getRole() { return role; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getId() { return id; }
}
