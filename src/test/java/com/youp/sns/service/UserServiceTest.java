package com.youp.sns.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.youp.sns.exception.ErrorCode;
import com.youp.sns.exception.SnsApplicationException;
import com.youp.sns.fixture.UserEntityFixture;
import com.youp.sns.model.entity.UserEntity;
import com.youp.sns.repository.UserEntityRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class UserServiceTest {

    @Autowired
    UserService userService;

    @MockBean
    private UserEntityRepository userEntityRepository;

    @MockBean
    private BCryptPasswordEncoder encoder;

    @Test
    @DisplayName("회원가입이_정상적으로_동작하는_경우")
    void join() {
        String userName = "userName";
        String password = "password";

        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());
        when(encoder.encode(password)).thenReturn("encrypt_password");
        when(userEntityRepository.save(any())).thenReturn(UserEntityFixture.get(userName, password, 1));

        Assertions.assertDoesNotThrow(() -> userService.join(userName, password));
    }

    @Test
    @DisplayName("회원가입시_userName 으로_회원가입한_유저가_이미_있는경우")
    void join_duplicated() {
        String userName = "userName";
        String password = "password";

        UserEntity fixture = UserEntityFixture.get(userName, password, 1);

        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));
        when(encoder.encode(password)).thenReturn("encrypt_password");
        when(userEntityRepository.save(any())).thenReturn(Optional.of(fixture));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class,
                () -> userService.join(userName, password));
        Assertions.assertEquals(ErrorCode.DUPLICATED_USER_NAME, e.getErrorCode());
    }

    @Test
    @DisplayName("로그인이_정상적으로_동작하는_경우")
    void login() {
        String userName = "userName";
        String password = "password";

        UserEntity fixture = UserEntityFixture.get(userName, password, 1);

        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));
        when(encoder.matches(password, fixture.getPassword())).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> userService.login(userName, password));
    }

    @Test
    @DisplayName("로그인시_userName 으로_회원가입한_유저가_없는경우")
    void login_notExist() {
        String userName = "userName";
        String password = "password";

        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class,
                () -> userService.login(userName, password));
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("로그인시_패스워드가_틀린_경우")
    void login_wrongPassword() {
        String userName = "userName";
        String password = "password";
        String wrongPassword = "wrongPassword";

        UserEntity fixture = UserEntityFixture.get(userName, password, 1);

        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));

        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class,
                () -> userService.login(userName, wrongPassword));
        Assertions.assertEquals(ErrorCode.INVALID_PASSWORD, e.getErrorCode());
    }

}