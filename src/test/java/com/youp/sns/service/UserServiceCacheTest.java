package com.youp.sns.service;

import com.youp.sns.model.User;
import com.youp.sns.repository.UserCacheRepository;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceCacheTest {

    @Autowired
    UserService userService;

    @Autowired
    UserCacheRepository userCacheRepository;

    @Test
    @DisplayName("Cache 사용 속도 테스트")
    void loadUser_cacheOrNot() {
        String userName = "test11";
        String password = "1234";
        User user = userService.join(userName, password);
        userCacheRepository.setUser(user);

        long start = System.currentTimeMillis();
        IntStream.range(0, 10000).forEach(n -> userService.loadUserByUserNameNotCached(userName));
        long end = System.currentTimeMillis();
        long executionTime = end - start;
        System.out.printf("loadUserByUserNameNotCached execution time : %d%n", executionTime);

        start = System.currentTimeMillis();
        IntStream.range(0, 10000).forEach(n -> userService.loadUserByUserName(userName));
        end = System.currentTimeMillis();
        long cacheExecutionTime = end - start;
        System.out.printf("loadUserByUserName execution time : %d%n", cacheExecutionTime);

        userService.withdraw(userName, password);
        Assertions.assertTrue(cacheExecutionTime < executionTime);
    }
}
