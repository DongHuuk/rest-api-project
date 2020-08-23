package org.kuroneko.restapiproject.notification;

import com.querydsl.jpa.JPQLQuery;
import org.kuroneko.restapiproject.domain.Notification;
import org.kuroneko.restapiproject.domain.QNotification;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class NotificationRepositoryExtensionImpl extends QuerydslRepositorySupport implements NotificationRepositoryExtension{

    public NotificationRepositoryExtensionImpl() {
        super(Notification.class);
    }

    @Override
    public List<Notification> findByNumber(List<Long> list) {
        JPQLQuery<Notification> notificationJPQLQuery = getCommentsQuery(list.size(), list);

        if (notificationJPQLQuery == null) {
            throw new NullPointerException("QueryDSL find notification is Null");
        }

        return notificationJPQLQuery.fetch();
    }

    private JPQLQuery<Notification> getCommentsQuery(int size, List<Long> list){
        QNotification notification = QNotification.notification;
        switch (size){
            case 1:
                return from(notification).where(notification.number.eq(list.get(0)));
            case 2:
                return from(notification).where(notification.number.eq(list.get(0))
                        .or(notification.number.eq(list.get(1))));
            case 3:
                return from(notification).where(notification.number.eq(list.get(0))
                        .or(notification.number.eq(list.get(1)))
                        .or(notification.number.eq(list.get(2))));
            case 4:
                return from(notification).where(notification.number.eq(list.get(0))
                        .or(notification.number.eq(list.get(1)))
                        .or(notification.number.eq(list.get(2)))
                        .or(notification.number.eq(list.get(3))));
            case 5:
                return from(notification).where(notification.number.eq(list.get(0))
                        .or(notification.number.eq(list.get(1)))
                        .or(notification.number.eq(list.get(2)))
                        .or(notification.number.eq(list.get(3)))
                        .or(notification.number.eq(list.get(4))));
            case 6:
                return from(notification).where(notification.number.eq(list.get(0))
                        .or(notification.number.eq(list.get(1)))
                        .or(notification.number.eq(list.get(2)))
                        .or(notification.number.eq(list.get(3)))
                        .or(notification.number.eq(list.get(4)))
                        .or(notification.number.eq(list.get(5))));
            case 7:
                return from(notification).where(notification.number.eq(list.get(0))
                        .or(notification.number.eq(list.get(1)))
                        .or(notification.number.eq(list.get(2)))
                        .or(notification.number.eq(list.get(3)))
                        .or(notification.number.eq(list.get(4)))
                        .or(notification.number.eq(list.get(5)))
                        .or(notification.number.eq(list.get(6))));
            case 8:
                return from(notification).where(notification.number.eq(list.get(0))
                        .or(notification.number.eq(list.get(1)))
                        .or(notification.number.eq(list.get(2)))
                        .or(notification.number.eq(list.get(3)))
                        .or(notification.number.eq(list.get(4)))
                        .or(notification.number.eq(list.get(5)))
                        .or(notification.number.eq(list.get(6)))
                        .or(notification.number.eq(list.get(7))));
            case 9:
                return from(notification).where(notification.number.eq(list.get(0))
                        .or(notification.number.eq(list.get(1)))
                        .or(notification.number.eq(list.get(2)))
                        .or(notification.number.eq(list.get(3)))
                        .or(notification.number.eq(list.get(4)))
                        .or(notification.number.eq(list.get(5)))
                        .or(notification.number.eq(list.get(6)))
                        .or(notification.number.eq(list.get(7)))
                        .or(notification.number.eq(list.get(8))));
            case 10:
                return from(notification).where(notification.number.eq(list.get(0))
                        .or(notification.number.eq(list.get(1)))
                        .or(notification.number.eq(list.get(2)))
                        .or(notification.number.eq(list.get(3)))
                        .or(notification.number.eq(list.get(4)))
                        .or(notification.number.eq(list.get(5)))
                        .or(notification.number.eq(list.get(6)))
                        .or(notification.number.eq(list.get(7)))
                        .or(notification.number.eq(list.get(8)))
                        .or(notification.number.eq(list.get(9)))).fetchJoin();
        }
        return null;
    }


}
