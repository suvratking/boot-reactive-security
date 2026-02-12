package com.github.suvratking.bootReactiveSecurity.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record UserRequest(@NotNull @Min(1) Long id,
                          @NotEmpty String username,
                          @NotEmpty @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", flags = Pattern.Flag.CASE_INSENSITIVE) String email,
                          @NotEmpty String password,
                          boolean active,
                          @NotEmpty List<String> roles){
        }
