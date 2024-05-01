package com.example.blog_app_springboot.users;

import com.example.blog_app_springboot.users.dtos.CreateUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class UsersServiceTests {
    @Autowired
    UserService userService;

    @Test
    void can_create_users() {
        var user = userService.createUser(new CreateUserRequest(
           "najat Oracle",
           "15062024",
           "najatOracle@gmail.com"
        ));
        Assertions.assertNotNull(user);
        Assertions.assertEquals("najat Oracle", user.getUsername());
    }
}
