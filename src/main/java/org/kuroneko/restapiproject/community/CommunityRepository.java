package org.kuroneko.restapiproject.community;

import org.kuroneko.restapiproject.article.domain.Article;
import org.kuroneko.restapiproject.community.domain.Community;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Set;

public interface CommunityRepository extends JpaRepository<Community, Long>, QuerydslPredicateExecutor<Community>, CommunityRepositoryExtension {
    Community findByTitle(String title);
}
