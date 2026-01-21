package com.rto.model;

public class RTOOfficer extends User {
    public RTOOfficer(String id, String username, String password) {
        super(id, username, password, "OFFICER");
    }
}
