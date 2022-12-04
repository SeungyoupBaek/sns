package com.youp.sns.service;

import com.youp.sns.exception.ErrorCode;
import com.youp.sns.exception.SnsApplicationException;
import com.youp.sns.model.Post;
import com.youp.sns.model.entity.PostEntity;
import com.youp.sns.model.entity.UserEntity;
import com.youp.sns.repository.PostEntityRepository;
import com.youp.sns.repository.UserEntityRepository;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    public static final String NOT_FOUNDED_STRING = "%s not founded";
    private final PostEntityRepository postEntityRepository;
    private final UserEntityRepository userEntityRepository;

    @Transactional
    public void create(String title, String body, String userName) {
        UserEntity userEntity = userEntityRepository.findByUserName(userName).orElseThrow(
                () -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND,
                        String.format(NOT_FOUNDED_STRING, userName)));
        postEntityRepository.save(PostEntity.of(title, body, userEntity));
    }

    public Post modify(String title, String body, String userName, Integer postId) {
        UserEntity userEntity = userEntityRepository.findByUserName(userName).orElseThrow(
                () -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format(NOT_FOUNDED_STRING, userName)));

        PostEntity postEntity = postEntityRepository.findById(postId).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format(NOT_FOUNDED_STRING, postId)));

        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION,
                    String.format("%s has no permission with %s", userName, postId));
        }

        postEntity.setTitle(title);
        postEntity.setBody(body);

        return Post.fromEntity(postEntityRepository.saveAndFlush(postEntity));
    }

    @Transactional
    public void delete(String userName, Integer postId) {
        UserEntity userEntity = userEntityRepository.findByUserName(userName).orElseThrow(
                () -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format(NOT_FOUNDED_STRING, userName)));

        PostEntity postEntity = postEntityRepository.findById(postId).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format(NOT_FOUNDED_STRING, postId)));

        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION,
                    String.format("%s has no permission with %s", userName, postId));
        }

        postEntityRepository.delete(postEntity);
    }

    public Page<Post> list(Pageable pageable) {
        return postEntityRepository.findAll(pageable).map(Post::fromEntity);
    }

    public Page<Post> my(String userName, Pageable pageable) {
        UserEntity userEntity = userEntityRepository.findByUserName(userName).orElseThrow(
                () -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format(NOT_FOUNDED_STRING, userName)));

        return postEntityRepository.findAllByUser(userEntity, pageable).map(Post::fromEntity);
    }
}
