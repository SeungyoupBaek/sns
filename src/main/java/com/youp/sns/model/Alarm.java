package com.youp.sns.model;

import com.youp.sns.model.entity.AlarmEntity;
import com.youp.sns.model.entity.CommentEntity;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Alarm {
    private Integer id;
    private User user;
    private AlarmType alarmType;
    private AlarmArgs args;
    private Timestamp registeredAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public static Alarm fromEntity(AlarmEntity alarmEntity) {
        return new Alarm(
                alarmEntity.getId(),
                User.fromEntity(alarmEntity.getUser()),
                alarmEntity.getAlarmType(),
                alarmEntity.getArgs(),
                alarmEntity.getRegisteredAt(),
                alarmEntity.getUpdatedAt(),
                alarmEntity.getDeletedAt()
        );
    }
}
