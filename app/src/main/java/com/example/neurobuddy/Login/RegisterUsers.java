package com.example.neurobuddy.Login;

import java.util.ArrayList;
import java.util.List;

public class RegisterUsers {
    private String login;
    private String email;
    private List<String> plans; // Add this line

    public RegisterUsers() {
        // Default constructor required for calls to DataSnapshot.getValue(RegisterUsers.class)
    }

    public RegisterUsers(String login, String email) {
        this.login = login;
        this.email = email;
        this.plans = new ArrayList<>(); // Initialize the plans list
    }

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getPlans() {
        return plans;
    }
}


