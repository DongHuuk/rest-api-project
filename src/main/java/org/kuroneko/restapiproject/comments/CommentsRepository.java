package org.kuroneko.restapiproject.comments;

import org.kuroneko.restapiproject.domain.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long>, QuerydslPredicateExecutor<Comments>, CommentsRepositoryExtension {
    Page<Comments> findByAccountId(Long id, Pageable pageable);

    @Modifying
    @Query("delete from Comments c where c.id in :ids")
    void deleteAllByIdInQuery(@Param("ids") List<Long> ids);
}
