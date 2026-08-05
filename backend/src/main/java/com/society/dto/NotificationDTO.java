package com.society.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationDTO {
    private Long          notificationId;
    private String        message;
    private String        type;
    private Boolean       isRead;
    private LocalDateTime createdAt;
}
