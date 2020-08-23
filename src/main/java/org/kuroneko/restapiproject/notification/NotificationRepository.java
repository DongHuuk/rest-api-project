package org.kuroneko.restapiproject.notification;

import org.kuroneko.restapiproject.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, QuerydslPredicateExecutor<Notification>, NotificationRepositoryExtension {
    Page<Notification> findByAccountId(Long id, Pageable pageable);

    @Modifying
    @Query("delete from Notification n where n.id in :ids")
    void deleteAllByIdInQuery(@Param("ids") List<Long> ids);
}
