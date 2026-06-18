package com.hiresync.config;

import com.hiresync.models.JobPosting;
import com.hiresync.models.User;
import com.hiresync.repositories.JobPostingRepository;
import com.hiresync.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserService userService, JobPostingRepository jobPostingRepository) {
        return args -> {
            if (userService.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setPassword("1234");
                admin.setName("Admin User");
                admin.setRole("ADMIN");
                userService.registerUser(admin);
            }

            if (userService.findByEmail("newuser@example.com").isEmpty()) {
                User user = new User();
                user.setEmail("newuser@example.com");
                user.setPassword("1234");
                user.setName("General User");
                user.setRole("GENERAL");
                userService.registerUser(user);
            }

            if (userService.findByEmail("jihoon.kim@example.com").isEmpty()) {
                User user = new User();
                user.setEmail("jihoon.kim@example.com");
                user.setPassword("1234");
                user.setName("김지훈");
                user.setRole("GENERAL");
                userService.registerUser(user);
            }

            if (userService.findByEmail("tester@example.com").isEmpty()) {
                User user = new User();
                user.setEmail("tester@example.com");
                user.setPassword("1234!");
                user.setName("일반사용자");
                user.setRole("GENERAL");
                userService.registerUser(user);
            }

            if (jobPostingRepository.count() == 0) {
                // The record for jobs.spec.ts integration test
                JobPosting target = new JobPosting();
                target.setCompanyName("SK키파운드리");
                target.setPosition("Frontend Engineer");
                target.setLocation("Cheongju");
                target.setRequirements("React, TypeScript");
                target.setStatus("PENDING");
                jobPostingRepository.save(target);
            }
        };
    }
}
