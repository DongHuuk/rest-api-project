package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long>, QuerydslPredicateExecutor<Article>, ArticleRepositoryExtension{

    Optional<Article> findByNumber(Long valueOf);

    Page<Article> findByAccountId(Long id, Pageable pageable);

    @Modifying
    @Query("delete from Article a where a.id in :ids")
    void deleteAllByIdInQuery(@Param("ids") List<Long> collect);
}
