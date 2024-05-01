package com.example.blog_app_springboot.users;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

@Data
@Entity(name = "users")
@Getter
@Setter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    @NonNull
    private String username;
     @Column(nullable = false)
    @NonNull
    private String password;
     @Column(nullable = false)
    @NonNull
    private String email;
     @Column(nullable = true)
    @Nullable
    private String bio;
     @Column(nullable = true)
    @Nullable
    private String image;

}
