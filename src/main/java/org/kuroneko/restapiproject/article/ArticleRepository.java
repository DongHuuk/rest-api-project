package org.kuroneko.restapiproject.article;

import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.article.domain.ArticleThema;
import org.kuroneko.restapiproject.community.domain.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Article> findByAccountIdAndCommunityId(Long accountId, Long communityId, Pageable pageable);

    Page<Article> findTop10ByOrderByCreateTimeDesc(Pageable pageable);

    List<Article> findByCommunity(Community community);

    Page<Article> findByCommunity(Community community, Pageable pageable);
}
