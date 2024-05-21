# Blog App with Spring Boot (Backend)

<div align="center">

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](Link)
[![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](Link)
[![Postgres](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](Link)
[![Intellij Idea](https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)](Link)
[![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)](Link)
</div>

## In Progress...
![combined](https://github.com/Marouane-Elgoumiri/Blog_App_SpringBoot/assets/96888594/51f3c7a0-a153-4d22-8d40-48edcdc809a6)

## Junit testing in Spring Boot:

![Postman](https://img.shields.io/badge/Testing%20Library-E33332.svg?style=for-the-badge&logo=Testing-Library&logoColor=white)
![Postman](https://img.shields.io/badge/JUnit5-25A162.svg?style=for-the-badge&logo=JUnit5&logoColor=white)

<span style="font-size: large">In this example we'll write test for the Users package</span>
### Package com.example.blog_app_springboot.users:

```java
  package com.example.blog_app_springboot.users;


import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public class UsersRepoTests {
    @Autowired
    private UserRepository userRepository;
    @Test
    @Order(1)
    void can_create_user() {
        var user = UserEntity.builder()
                .username("admin")
                .password("admin")
                .email("admin@gmail.com").build();
        userRepository.save(user);
    }
}

```

### Setting up the JpaTestConfig:
```java
@Configuration
public class JpaTestConfig {
    @Bean
    @Profile("test")
    public DataSource dataSource() {
        var dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        return dataSource;
    }
}

```
### Test Result:
![Screenshot from 2024-05-01 16-38-13](https://github.com/Marouane-Elgoumiri/Blog_App_SpringBoot/assets/96888594/2d8567aa-ef2f-4877-9b3a-cfe7014105d4)


### UsersServiceTests.java:
```java
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
```
#### Test Result:
![Screenshot from 2024-05-01 16-37-50](https://github.com/Marouane-Elgoumiri/Blog_App_SpringBoot/assets/96888594/82941dde-ab08-4ab2-b390-bc2372e5e67d)

## Setting up Error Exception Handler

### Create an ErrorResponse class:

```java
  package com.example.blog_app_springboot.common.dtos;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ErrorResponse {
    private String message;
    private String details;
}

```
### Creating the Response entity
```java
  @ExceptionHandler({
            UserService.UserNotFoundException.class
    })
    ResponseEntity<ErrorResponse> handleUSerNotFoundException(Exception ex){
        String message;
        HttpStatus status;

        if(ex instanceof UserService.UserNotFoundException){
           message = ex.getMessage();
           status = HttpStatus.NOT_FOUND;
        }else{
            message = "Something went wrong";
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .build();
        return ResponseEntity.status(status).body(response);
    }
```
### Example of use:
```JSON
{
    "message": "User with Username: saidox not found",
    "details": null
}
```
![Screenshot from 2024-05-04 23-35-32](https://github.com/Marouane-Elgoumiri/Blog_App_SpringBoot/assets/96888594/a1272369-bee0-4513-a77b-2b8a59b4e5e4)

