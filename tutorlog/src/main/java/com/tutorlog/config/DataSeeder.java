package com.tutorlog.config;

import com.tutorlog.model.Lesson;
import com.tutorlog.model.User;
import com.tutorlog.repository.LessonRepository;
import com.tutorlog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Seeds the database with demo data on startup.
 * Only runs when the "dev" Spring profile is active.
 * Run with: --spring.profiles.active=dev
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired private UserRepository userRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Database already seeded, skipping.");
            return;
        }

        logger.info("Seeding database with demo data...");

        // Users
        User student = userRepository.save(User.builder()
                .name("Alex Johnson").email("student@demo.com")
                .password(passwordEncoder.encode("demo1234"))
                .roles(Set.of(User.Role.ROLE_STUDENT))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build());

        User tutor = userRepository.save(User.builder()
                .name("Dr. James Wright").email("tutor@demo.com")
                .password(passwordEncoder.encode("demo1234"))
                .bio("PhD in Mathematics, 10 years teaching experience")
                .roles(Set.of(User.Role.ROLE_TUTOR))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build());

        // Lessons
        lessonRepository.saveAll(List.of(
            Lesson.builder().title("Introduction to Calculus").subject("Mathematics")
                .description("Foundational concepts of differential and integral calculus.")
                .content("<h3>What is Calculus?</h3><p>Calculus is the mathematical study of continuous change.</p>")
                .durationMinutes(52).tutorId(tutor.getId()).tutorName(tutor.getName())
                .accessType(Lesson.AccessType.FREE).viewCount(234).rating(4.8).ratingCount(12)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            Lesson.builder().title("Quantum Mechanics Basics").subject("Physics")
                .description("An introduction to wave-particle duality and quantum theory.")
                .content("<h3>Wave-Particle Duality</h3><p>Particles like electrons exhibit both wave-like and particle-like behaviour.</p>")
                .durationMinutes(68).tutorId(tutor.getId()).tutorName(tutor.getName())
                .accessType(Lesson.AccessType.PREMIUM).viewCount(189).rating(4.9).ratingCount(8)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

            Lesson.builder().title("Python Programming Intro").subject("Computer Science")
                .description("Get started with Python — syntax, variables, and control flow.")
                .content("<h3>Getting Started with Python</h3><p>Python is a high-level, interpreted language.</p><p><code>print('Hello, World!')</code></p>")
                .durationMinutes(65).tutorId(tutor.getId()).tutorName(tutor.getName())
                .accessType(Lesson.AccessType.FREE).viewCount(421).rating(4.9).ratingCount(20)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        ));

        logger.info("Seeded 2 users and 3 lessons.");
        logger.info("Student login: student@demo.com / demo1234");
        logger.info("Tutor login:   tutor@demo.com  / demo1234");
    }
}
