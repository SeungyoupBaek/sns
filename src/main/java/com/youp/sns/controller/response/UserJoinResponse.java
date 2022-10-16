package com.youp.sns.controller.response;

import com.youp.sns.model.User;
import com.youp.sns.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinResponse {

    private Integer id;
    private String userName;
    private UserRole userRole;

    public static UserJoinResponse fromUser(User user) {
        return new UserJoinResponse(
            user.getId(),
            user.getUserName(),
            user.getUserRole()
        );
    }
}
