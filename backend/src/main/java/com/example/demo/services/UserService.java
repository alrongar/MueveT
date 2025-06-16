package com.example.demo.services;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.demo.entity.User;

public interface UserService extends UserDetailsService{
    
    
    User saveUser(User user);

    boolean existsByEmail(String email);

    User findByEmail(String email);

	void addJwt(User user);
    
    List<User> getAllUsers();

    List<User> getAllClients(String name, String email);

    User findById(Long id);

    User updateUser(User updatedUser);

}
