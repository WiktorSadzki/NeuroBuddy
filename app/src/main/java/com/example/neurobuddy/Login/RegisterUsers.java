package com.example.neurobuddy.Login;

import java.util.ArrayList;
import java.util.List;

public class RegisterUsers {
    private String login;
    private String email;
    private List<String> plans;
    private String path;
    private int points;

    public RegisterUsers() {

    }

    public RegisterUsers(String login, String email) {
        this.login = login;
        this.email = email;
        this.plans = new ArrayList<>();
        this.points = 0;
        this.path = "";
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

    public void setLogin(String login) {
        this.login = login;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPlans(List<String> plans) {
        this.plans = plans;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}