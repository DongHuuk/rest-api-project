package org.kuroneko.restapiproject.comments;

import org.kuroneko.restapiproject.comments.domain.Comments;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface CommentsRepositoryExtension {

    List<Comments> findByNumber(List<Long> list);
}
