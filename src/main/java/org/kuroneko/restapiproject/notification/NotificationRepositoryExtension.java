package org.kuroneko.restapiproject.notification;

import org.kuroneko.restapiproject.domain.Notification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface NotificationRepositoryExtension {

    List<Notification> findByNumber(List<Long> list);

}
