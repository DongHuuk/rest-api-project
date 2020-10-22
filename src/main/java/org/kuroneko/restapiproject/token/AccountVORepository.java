package org.kuroneko.restapiproject.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountVORepository extends JpaRepository<AccountVO, Long> {
    AccountVO findByEmailAndPassword(String userId, String userPw);

    Optional<AccountVO> findByEmail(String userEmail);
}
