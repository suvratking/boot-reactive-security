package org.example.bootReactiveSecurity.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record UserRequest(@NotEmpty String id,
                          @NotEmpty String username,
                          @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", flags = Pattern.Flag.CASE_INSENSITIVE) String email,
                          @NotEmpty String password,
                          boolean active,
                          @NotEmpty List<String> roles){
        }
