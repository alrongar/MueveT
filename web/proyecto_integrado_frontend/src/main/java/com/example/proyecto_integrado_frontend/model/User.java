package com.example.proyecto_integrado_frontend.model;

public class User {
    private long id;
    private String email;
    private String name;
    private String surname;
    private String role;
    private boolean active;
    private String password;
    private String confirmPassword;

    public User(String email, String name, String surname, String role, boolean active, String password,
            String confirmPassword) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.role = role;
        this.active = active;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public User() {
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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @Override
    public String toString() {
        return "User [email=" + email + ", name=" + name + ", surname=" + surname + ", role=" + role + ", active="
                + active + ", password=" + password + ", confirmPassword=" + confirmPassword + "]";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    

    
    
}