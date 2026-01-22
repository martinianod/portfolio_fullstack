package com.martiniano.crm.config;

import com.martiniano.crm.entity.User;
import com.martiniano.crm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin user exists
            if (userRepository.findByUsername("admin").isEmpty() && 
                userRepository.findByEmail("admin@martiniano.dev").isEmpty()) {
                
                log.info("Creating default admin user for development...");
                
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@martiniano.dev");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setFullName("Admin User");
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                
                userRepository.save(admin);
                
                log.info("Default admin user created successfully");
                log.info("Email: admin@martiniano.dev");
                log.info("Password: admin123");
                log.warn("IMPORTANT: Change the default password in production!");
            } else {
                log.info("Admin user already exists, skipping initialization");
            }
        };
    }
}
