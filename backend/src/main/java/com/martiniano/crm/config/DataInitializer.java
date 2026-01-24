package com.martiniano.crm.config;

import com.martiniano.crm.entity.User;
import com.martiniano.crm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Data initializer for development environment
 * Creates default admin user if it doesn't exist
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    @Value("${app.admin.email:admin@martiniano.dev}")
    private String adminEmail;
    
    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin user exists
            if (userRepository.findByUsername("admin").isEmpty() && 
                userRepository.findByEmail(adminEmail).isEmpty()) {
                
                log.info("Creating default admin user...");
                
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail(adminEmail);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setFullName("Admin User");
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                
                userRepository.save(admin);
                
                log.info("Default admin user created successfully");
                log.info("Email: {}", adminEmail);
                // SECURITY: Never log passwords in plain text
                log.warn("IMPORTANT: Change the default admin password in production!");
            } else {
                log.info("Admin user already exists, skipping initialization");
            }
        };
    }
}
