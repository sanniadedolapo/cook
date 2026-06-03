package com.tutorlog.service;

import com.tutorlog.dto.LessonDto;
import com.tutorlog.model.Lesson;
import com.tutorlog.model.Subscription;
import com.tutorlog.model.User;
import com.tutorlog.repository.LessonRepository;
import com.tutorlog.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AuthService authService;

    // ── Create ──────────────────────────────────────────────
    public LessonDto.Response createLesson(LessonDto.CreateRequest request) {
        User tutor = authService.getCurrentUser();

        Lesson lesson = Lesson.builder()
                .title(request.getTitle())
                .subject(request.getSubject())
                .description(request.getDescription())
                .content(request.getContent())
                .durationMinutes(request.getDurationMinutes())
                .tutorId(tutor.getId())
                .tutorName(tutor.getName())
                .accessType(request.getAccessType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return LessonDto.Response.from(lessonRepository.save(lesson));
    }

    // ── Read ─────────────────────────────────────────────────
    public List<LessonDto.Response> getAllLessons() {
        return lessonRepository.findAll().stream()
                .map(LessonDto.Response::fromSummary)
                .collect(Collectors.toList());
    }

    public List<LessonDto.Response> getFreeLessons() {
        return lessonRepository.findByAccessType(Lesson.AccessType.FREE).stream()
                .map(LessonDto.Response::fromSummary)
                .collect(Collectors.toList());
    }

    public List<LessonDto.Response> getLessonsByTutor(String tutorId) {
        return lessonRepository.findByTutorId(tutorId).stream()
                .map(LessonDto.Response::fromSummary)
                .collect(Collectors.toList());
    }

    public List<LessonDto.Response> searchLessons(String keyword) {
        return lessonRepository
                .findByTitleContainingIgnoreCaseOrSubjectContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(LessonDto.Response::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * Returns full lesson content if the user is authorised to view it.
     * Free lessons are always accessible; premium lessons require an active subscription.
     */
    public LessonDto.Response getLessonById(String id) {
        Lesson lesson = findOrThrow(id);
        User currentUser = authService.getCurrentUser();

        if (lesson.getAccessType() == Lesson.AccessType.PREMIUM) {
            boolean hasActiveSub = subscriptionRepository
                    .findByUserIdAndStatus(currentUser.getId(), Subscription.Status.ACTIVE)
                    .map(Subscription::isActive)
                    .orElse(false);

            if (!hasActiveSub && !currentUser.getRoles().contains(User.Role.ROLE_TUTOR)) {
                throw new IllegalStateException("An active subscription is required to access this lesson");
            }
        }

        // Increment view count
        lesson.setViewCount(lesson.getViewCount() + 1);
        lessonRepository.save(lesson);

        return LessonDto.Response.from(lesson);
    }

    // ── Update ───────────────────────────────────────────────
    public LessonDto.Response updateLesson(String id, LessonDto.UpdateRequest request) {
        Lesson lesson = findOrThrow(id);
        User currentUser = authService.getCurrentUser();

        if (!lesson.getTutorId().equals(currentUser.getId())
                && !currentUser.getRoles().contains(User.Role.ROLE_ADMIN)) {
            throw new SecurityException("You do not have permission to edit this lesson");
        }

        if (request.getTitle() != null) lesson.setTitle(request.getTitle());
        if (request.getSubject() != null) lesson.setSubject(request.getSubject());
        if (request.getDescription() != null) lesson.setDescription(request.getDescription());
        if (request.getContent() != null) lesson.setContent(request.getContent());
        if (request.getDurationMinutes() != null) lesson.setDurationMinutes(request.getDurationMinutes());
        if (request.getAccessType() != null) lesson.setAccessType(request.getAccessType());
        lesson.setUpdatedAt(LocalDateTime.now());

        return LessonDto.Response.from(lessonRepository.save(lesson));
    }

    // ── Delete ───────────────────────────────────────────────
    public void deleteLesson(String id) {
        Lesson lesson = findOrThrow(id);
        User currentUser = authService.getCurrentUser();

        if (!lesson.getTutorId().equals(currentUser.getId())
                && !currentUser.getRoles().contains(User.Role.ROLE_ADMIN)) {
            throw new SecurityException("You do not have permission to delete this lesson");
        }

        lessonRepository.deleteById(id);
    }

    // ── Rate ─────────────────────────────────────────────────
    public LessonDto.Response rateLesson(String id, int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        Lesson lesson = findOrThrow(id);
        double newRating = ((lesson.getRating() * lesson.getRatingCount()) + stars)
                / (lesson.getRatingCount() + 1);
        lesson.setRating(Math.round(newRating * 10.0) / 10.0);
        lesson.setRatingCount(lesson.getRatingCount() + 1);
        return LessonDto.Response.from(lessonRepository.save(lesson));
    }

    // ── Helpers ──────────────────────────────────────────────
    private Lesson findOrThrow(String id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id));
    }
}
