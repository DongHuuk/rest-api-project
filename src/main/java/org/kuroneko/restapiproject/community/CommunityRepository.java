package org.kuroneko.restapiproject.community;

import org.kuroneko.restapiproject.community.domain.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long>, QuerydslPredicateExecutor<Community> {

    List<Community> findTop10ByOrderByCreateTimeDesc();

    Community findByTitle(String title);
}
