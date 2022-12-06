package com.youp.sns.model;

import com.youp.sns.model.entity.CommentEntity;
import com.youp.sns.model.entity.PostEntity;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Comment {
    private Integer id;
    private String comment;
    private String userName;
    private Integer postId;
    private Timestamp registeredAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public static Comment fromEntity(CommentEntity commentEntity) {
        return new Comment(
                commentEntity.getId(),
                commentEntity.getComment(),
                commentEntity.getUser().getUserName(),
                commentEntity.getPost().getId(),
                commentEntity.getRegisteredAt(),
                commentEntity.getUpdatedAt(),
                commentEntity.getDeletedAt()
        );
    }
}
