package com.tutorlog.controller;

import com.tutorlog.dto.LessonDto;
import com.tutorlog.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    /**
     * GET /api/lessons
     * Returns all lessons (summaries, no content body).
     * Requires authentication.
     */
    @GetMapping
    public ResponseEntity<List<LessonDto.Response>> getAllLessons() {
        return ResponseEntity.ok(lessonService.getAllLessons());
    }

    /**
     * GET /api/lessons/free
     * Returns free lessons only — public, no auth required.
     */
    @GetMapping("/free")
    public ResponseEntity<List<LessonDto.Response>> getFreeLessons() {
        return ResponseEntity.ok(lessonService.getFreeLessons());
    }

    /**
     * GET /api/lessons/search?q=calculus
     * Full-text search on title and subject.
     */
    @GetMapping("/search")
    public ResponseEntity<List<LessonDto.Response>> searchLessons(
            @RequestParam String q) {
        return ResponseEntity.ok(lessonService.searchLessons(q));
    }

    /**
     * GET /api/lessons/tutor/{tutorId}
     * Returns all lessons for a specific tutor.
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<LessonDto.Response>> getLessonsByTutor(
            @PathVariable String tutorId) {
        return ResponseEntity.ok(lessonService.getLessonsByTutor(tutorId));
    }

    /**
     * GET /api/lessons/{id}
     * Returns full lesson including content.
     * Premium lessons require an active subscription.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonDto.Response> getLessonById(@PathVariable String id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    /**
     * POST /api/lessons
     * Creates a new lesson. Tutors only.
     */
    @PostMapping
    public ResponseEntity<LessonDto.Response> createLesson(
            @Valid @RequestBody LessonDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lessonService.createLesson(request));
    }

    /**
     * PUT /api/lessons/{id}
     * Updates a lesson. Owner tutor or admin only.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LessonDto.Response> updateLesson(
            @PathVariable String id,
            @RequestBody LessonDto.UpdateRequest request) {
        return ResponseEntity.ok(lessonService.updateLesson(id, request));
    }

    /**
     * DELETE /api/lessons/{id}
     * Deletes a lesson. Owner tutor or admin only.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable String id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/lessons/{id}/rate
     * Body: { stars: 1-5 }
     * Adds a rating to a lesson.
     */
    @PostMapping("/{id}/rate")
    public ResponseEntity<LessonDto.Response> rateLesson(
            @PathVariable String id,
            @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(lessonService.rateLesson(id, body.get("stars")));
    }
}
