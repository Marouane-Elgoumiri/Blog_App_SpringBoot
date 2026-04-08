package com.example.blog_app_springboot.users.dtos;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
	@Size(max = 500, message = "Bio must be less than 500 characters")
	private String bio;

	@Pattern(regexp = "^(https?://)?.+$", message = "Image must be a valid URL")
	private String image;
}
