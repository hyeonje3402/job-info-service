package com.hiresync.config;

import com.hiresync.models.User;
import com.hiresync.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserService userService) {
        return args -> {
            if (userService.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setPassword("1234");
                admin.setName("Admin User");
                admin.setRole("ADMIN");
                userService.registerUser(admin);
            }
        };
    }
}
