package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.domain.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, QuerydslPredicateExecutor<Account>{
    Optional<Account> findByEmail(String email);

    List<Account> findAllByUsername(String s);

    @EntityGraph(attributePaths = {"article"}, type = EntityGraph.EntityGraphType.LOAD)
    Account findAccountWithArticleById(Long id);

    @EntityGraph(attributePaths = {"comments"}, type = EntityGraph.EntityGraphType.LOAD)
    Account findAccountWithCommentsById(Long id);

    @EntityGraph(attributePaths = {"notification"}, type = EntityGraph.EntityGraphType.LOAD)
    Account findAccountWithNotificationById(Long id);
}
