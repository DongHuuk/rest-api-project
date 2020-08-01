package org.kuroneko.restapiproject.account;

import org.kuroneko.restapiproject.domain.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    List<Account> findAllByUsername(String s);

    @EntityGraph(attributePaths = {"article"}, type = EntityGraph.EntityGraphType.LOAD)
    Account findAccountWithArticleById(Long id);
}
