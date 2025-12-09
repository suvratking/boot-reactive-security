package org.example.bootReactiveSecurity.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UserRequest(@NotEmpty String id,
                          @NotEmpty String username,
                          @Email String email,
                          @NotEmpty String password,
                          boolean active,
                          @NotEmpty List<String> roles){
        }
