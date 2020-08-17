package org.kuroneko.restapiproject.comments;

import org.kuroneko.restapiproject.domain.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    Optional<Comments> findByNumber(Long number);
}
