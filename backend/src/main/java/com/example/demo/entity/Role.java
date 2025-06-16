package com.example.demo.entity;

public enum Role {
    ROLE_ADMIN, ROLE_USER;

	@Override
    public String toString() {
        return name().substring(5); 
    }
}
