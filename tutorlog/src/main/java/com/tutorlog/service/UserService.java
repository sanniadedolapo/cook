package com.tutorlog.service;

import com.tutorlog.model.User;
import com.tutorlog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    public User getProfile() {
        return authService.getCurrentUser();
    }

    public User updateProfile(Map<String, String> updates) {
        User user = authService.getCurrentUser();

        if (updates.containsKey("name") && !updates.get("name").isBlank()) {
            user.setName(updates.get("name"));
        }
        if (updates.containsKey("bio")) {
            user.setBio(updates.get("bio"));
        }
        if (updates.containsKey("password") && !updates.get("password").isBlank()) {
            user.setPassword(passwordEncoder.encode(updates.get("password")));
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public List<User> getAllStudents() {
        return userRepository.findByRolesContaining(User.Role.ROLE_STUDENT);
    }

    public List<User> getAllTutors() {
        return userRepository.findByRolesContaining(User.Role.ROLE_TUTOR);
    }
}
