package com.martiniano.crm.service;

import com.martiniano.crm.dto.LoginRequest;
import com.martiniano.crm.dto.LoginResponse;
import com.martiniano.crm.entity.User;
import com.martiniano.crm.repository.UserRepository;
import com.martiniano.crm.security.CustomUserDetailsService;
import com.martiniano.crm.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(CustomUserDetailsService userDetailsService,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder,
                       UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public LoginResponse authenticate(LoginRequest loginRequest) {
        String principal = loginRequest.getPrincipal();
        log.debug("Attempting authentication for principal: {}", principal);
        
        try {
            // loadUserByUsername now supports both username and email for backward compatibility
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal);
            log.debug("User found in database for principal: {}", principal);
            
            if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
                log.warn("Password mismatch for principal: {}", principal);
                throw new BadCredentialsException("Invalid credentials");
            }
            
            log.debug("Password verified for principal: {}", principal);

            // Find user by email (primary) or username (fallback for internal users)
            // The API accepts email, but users may have username != email
            User user = userRepository.findByEmail(principal)
                    .or(() -> userRepository.findByUsername(principal))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            log.debug("User entity retrieved for principal: {}", principal);
            
            String token = jwtUtil.generateToken(userDetails.getUsername());
            log.debug("JWT token generated for user: {}", userDetails.getUsername());

            return new LoginResponse(token, user.getUsername(), user.getEmail(), user.getRole());
            
        } catch (UsernameNotFoundException e) {
            log.warn("User not found for principal: {}", principal);
            throw new BadCredentialsException("Invalid credentials");
        } catch (Exception e) {
            log.error("Unexpected error during authentication for principal: {}", principal, e);
            throw e;
        }
    }
}
