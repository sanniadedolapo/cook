package com.tutorlog.repository;

import com.tutorlog.model.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends MongoRepository<Lesson, String> {

    List<Lesson> findByTutorId(String tutorId);

    List<Lesson> findByAccessType(Lesson.AccessType accessType);

    List<Lesson> findBySubjectIgnoreCase(String subject);

    List<Lesson> findByTitleContainingIgnoreCaseOrSubjectContainingIgnoreCase(
            String titleKeyword, String subjectKeyword);
}
