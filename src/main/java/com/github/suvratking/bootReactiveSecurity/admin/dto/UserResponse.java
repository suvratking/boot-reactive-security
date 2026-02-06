package com.github.suvratking.bootReactiveSecurity.admin.dto;

import com.github.suvratking.bootReactiveSecurity.auth.entity.User;

import java.util.List;

public record UserResponse(List<User> users) {
}
