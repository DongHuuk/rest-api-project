package org.kuroneko.restapiproject.notification;

import org.kuroneko.restapiproject.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
