package org.example.bootReactiveSecurity.admin.dto;

import org.example.bootReactiveSecurity.auth.entity.User;

import java.util.List;

public record UserResponse(List<User> users) {
}
