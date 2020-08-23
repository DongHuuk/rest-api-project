package org.kuroneko.restapiproject.notification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {

    private Long id;

    private Long number;

    private LocalDateTime createTime;

    private boolean checked;

    private Long accountId;

    private String accountUsername;

    private String userEmail;

    private Long articleId;

    private Long articleNumber;

    private Long commentsId;

    private Long commentsNumber;

}
