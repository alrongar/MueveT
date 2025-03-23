package com.example.demo.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Serializable>{
    

    boolean existsByEmail(String email);

    User findByEmail(String email);
    
    List<User> findAll();


}
