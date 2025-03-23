package com.example.demo.servicesImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.UserService;

@Service("userService")
public class UserServiceImpl implements UserService {


    @Autowired
    @Qualifier("userRepository")
    private UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public User saveUser(User user) {
        if (!user.getPassword().startsWith("$2a$10$")) {
            user.setPassword(passwordEncoder().encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void addJwt(User user) {
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
         User user = userRepository.findByEmail(username);
        if (user == null) 
            throw new UsernameNotFoundException("Usuario no encontrado");
        if (!user.getActive()) {
            throw new DisabledException("El usuario está desactivado");
        }
        return (UserDetails) user;
    }
    
}
