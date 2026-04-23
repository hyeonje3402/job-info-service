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

            if (jobPostingRepository.count() == 0) {
                // Insert 6 dummy records to make the next one ID 7
                for (int i = 1; i <= 6; i++) {
                    JobPosting dummy = new JobPosting();
                    dummy.setCompanyName("Dummy " + i);
                    dummy.setPosition("Position " + i);
                    dummy.setStatus("PENDING");
                    jobPostingRepository.save(dummy);
                }
                
                // The 7th record for test-3.json
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
