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

## Junit testing in java:
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
package com.example.blog_app_springboot;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class JpaTestConfig {
    @Bean
    @Profile("test")
    public DataSource dataSource() {
        var dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:test; DB_CLOSE_DELAY=-1");
        return dataSource;
    }
}

```
