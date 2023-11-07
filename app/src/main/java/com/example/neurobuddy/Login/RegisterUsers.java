package com.example.neurobuddy.Login;

import com.example.neurobuddy.Plan.Plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterUsers {
    private String login;
    private String email;
    private Map<String, Plan> plans;
    private String path;
    private int points;

    public RegisterUsers() {

    }

    public RegisterUsers(String login, String email) {
        this.login = login;
        this.email = email;
        this.plans = new HashMap<>();
        this.points = 0;
        this.path = "";
    }

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public Map<String, Plan> getPlans() {
        return plans;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void setPlans(Map<String, Plan> plans) {
        this.plans = plans;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPoints() {
        try {
            return Integer.parseInt(String.valueOf(points));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setPoints(int points) {
        this.points = points;
    }
}