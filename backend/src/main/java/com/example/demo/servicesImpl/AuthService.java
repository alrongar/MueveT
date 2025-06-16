package com.example.demo.servicesImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.UserService;

@Service("userAuthService")
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

	@Autowired
	@Qualifier("userRepository")
	private UserRepository usuarioRepository;

	@Autowired
	@Qualifier("userService")
    private UserService userService;
	
	@Autowired
	@Qualifier("jwtService")
    private JwtService jwtService;


    public String login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );
        
        User usuario = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(usuario);
        usuario.setJwt(token);
        userService.addJwt(usuario);
        
        return token;
    }
    

	public String register(User user) throws RuntimeException{
		if (userService.findByEmail(user.getEmail()) != null)
			throw new RuntimeException("Usuario ya registrado");
		
		String token = jwtService.generateToken(user);
		user.setJwt(token);
		
		userService.saveUser(user);
		return token;
	}
}
