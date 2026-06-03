package com.tutorlog.controller;

import com.tutorlog.model.User;
import com.tutorlog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * GET /api/users/me
     * Returns the authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<User> getProfile() {
        User user = userService.getProfile();
        user.setPassword(null); // never expose password hash
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/users/me
     * Body: { name?, bio?, password? }
     * Updates the authenticated user's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@RequestBody Map<String, String> updates) {
        User user = userService.updateProfile(updates);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/users/students
     * Returns all students. Tutors and admins only.
     */
    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ResponseEntity<List<User>> getAllStudents() {
        List<User> students = userService.getAllStudents();
        students.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(students);
    }

    /**
     * GET /api/users/tutors
     * Returns all tutors. Authenticated users.
     */
    @GetMapping("/tutors")
    public ResponseEntity<List<User>> getAllTutors() {
        List<User> tutors = userService.getAllTutors();
        tutors.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(tutors);
    }
}
