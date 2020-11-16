package org.kuroneko.restapiproject.account;

import org.hibernate.annotations.Fetch;
import org.kuroneko.restapiproject.account.domain.Account;
import org.kuroneko.restapiproject.account.domain.AccountDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, QuerydslPredicateExecutor<Account>{

//    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH)
    Optional<Account> findByEmail(String email);

    Optional<AccountDTO> findWithAccountDTOByEmail(String email);

    List<Account> findAllByUsername(String s);

    @EntityGraph(attributePaths = {"article"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Account> findAccountWithArticleById(Long id);

    @EntityGraph(attributePaths = {"comments"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Account> findAccountWithCommentsById(Long id);

    @EntityGraph(attributePaths = {"notification"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Account> findAccountWithNotificationById(Long id);

    Optional<Account> findByUsername(String userName);
}
