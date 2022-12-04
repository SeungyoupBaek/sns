package com.youp.sns.model;

import com.youp.sns.model.entity.PostEntity;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Post {
    private Integer id;

    private String title;

    private String body;

    private User user;

    private Timestamp registeredAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;

    public static Post fromEntity(PostEntity postEntity) {
        return new Post(
                postEntity.getId(),
                postEntity.getTitle(),
                postEntity.getBody(),
                User.fromEntity(postEntity.getUser()),
                postEntity.getRegisteredAt(),
                postEntity.getUpdatedAt(),
                postEntity.getDeletedAt()
        );
    }
}
