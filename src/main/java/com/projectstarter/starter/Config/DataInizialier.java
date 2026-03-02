package com.projectstarter.starter.Config;

import com.projectstarter.starter.Entity.User;
import com.projectstarter.starter.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInizialier implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInizialier(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin@example.com", passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin user created: admin/admin123");
        }

        if (!userRepository.existsByUsername("user")) {
            User user = new User("user", "user@example.com", passwordEncoder.encode("user123"));
            user.setRole(User.Role.USER);
            userRepository.save(user);
            System.out.println("Regular user created: user/user123");
        }
    }
}
