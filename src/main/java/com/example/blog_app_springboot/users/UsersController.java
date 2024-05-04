package com.example.blog_app_springboot.users;

import com.example.blog_app_springboot.common.dtos.ErrorResponse;
import com.example.blog_app_springboot.users.dtos.CreateUserRequest;
import com.example.blog_app_springboot.users.dtos.UserResponse;
import com.example.blog_app_springboot.users.dtos.LoginUserRequest;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserService userService;
    private final ModelMapper modelMapper;
    public UsersController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("")
    ResponseEntity<UserResponse> signup(@RequestBody CreateUserRequest createUserRequest) {
       UserEntity savedUser = userService.createUser(createUserRequest);
       URI savedUserUri = URI.create("/users/" + savedUser.getId());
       return ResponseEntity.created(savedUserUri)
               .body(modelMapper.map(savedUser, UserResponse.class));
    }

    @PostMapping("/login")
    ResponseEntity<UserResponse> loginUser(@RequestBody LoginUserRequest request){
        UserEntity savedUser = userService.loginUser(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(modelMapper.map(savedUser, UserResponse.class));
    }

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
}
