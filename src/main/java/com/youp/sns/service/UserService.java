package com.youp.sns.service;

import com.youp.sns.exception.ErrorCode;
import com.youp.sns.exception.SnsApplicationException;
import com.youp.sns.model.Alarm;
import com.youp.sns.model.User;
import com.youp.sns.model.entity.UserEntity;
import com.youp.sns.repository.AlarmEntityRepository;
import com.youp.sns.repository.UserCacheRepository;
import com.youp.sns.repository.UserEntityRepository;
import com.youp.sns.util.JwtTokenUtils;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserEntityRepository userEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final BCryptPasswordEncoder encoder;
    private final UserCacheRepository userCacheRepository;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private Long expiredTimeMs;

    public User loadUserByUserName(String userName) {
        return userCacheRepository.getUser(userName).orElseGet(() ->
                userEntityRepository.findByUserName(userName).map(User::fromEntity).orElseThrow(
                        () -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND,
                                String.format("%s is not founded.", userName)))
        );
    }

    public User loadUserByUserNameNotCached(String userName) {
        return userEntityRepository.findByUserName(userName).map(User::fromEntity).orElseThrow(
                () -> new SnsApplicationException(ErrorCode.USER_NOT_FOUND,
                        String.format("%s is not founded", userName))
        );
    }

    @Transactional
    public User join(String userName, String password) {
        // 회원가입하려는 userName 으로 회원가입된 user 가 있는지
        userEntityRepository.findByUserName(userName).ifPresent(it -> {
            throw new SnsApplicationException(ErrorCode.DUPLICATED_USER_NAME,
                    String.format("%s is duplicated", userName));
        });

        // 회원가입 진행 = user 를 등록
        UserEntity userEntity = userEntityRepository.save(UserEntity.of(userName, encoder.encode(password)));

        return User.fromEntity(userEntity);
    }

    @Transactional
    public void withdraw(String userName, String password) {
        UserEntity userEntity = userEntityRepository.findByUserName(userName).orElseThrow(() -> {
            throw new SnsApplicationException(ErrorCode.USER_NOT_FOUND,
                    String.format("User not founded : %s", userName));
        });

        if (!encoder.matches(password, userEntity.getPassword())) {
            throw new SnsApplicationException(ErrorCode.INVALID_PASSWORD);
        }

        userEntityRepository.delete(userEntity);
    }

    public String login(String userName, String password) {
        // 회운가입 여부 체크
        User user = loadUserByUserName(userName);
        userCacheRepository.setUser(user);

        // 비밀번호 체크
        if (!encoder.matches(password, user.getPassword())) {
            throw new SnsApplicationException(ErrorCode.INVALID_PASSWORD);
        }

        return JwtTokenUtils.generateToken(userName, secretKey, expiredTimeMs);
    }

    public Page<Alarm> alarmList(Integer userId, Pageable pageable) {
        return alarmEntityRepository.findAllByUserId(userId, pageable).map(Alarm::fromEntity);
    }
}
