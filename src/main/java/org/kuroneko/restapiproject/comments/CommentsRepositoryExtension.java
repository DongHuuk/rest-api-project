package org.kuroneko.restapiproject.comments;

import org.kuroneko.restapiproject.comments.domain.Comments;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CommentsRepositoryExtension {

    List<Comments> findByNumber(List<Long> list);
}
