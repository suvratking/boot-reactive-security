package com.github.suvratking.bootReactiveSecurity.admin.dto;

import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseTest {

    @Test
    void userResponse_WithUsers_ShouldContainAllUsers() {
        User user1 = User.builder()
                .id("user1")
                .username("testuser1")
                .email("test1@example.com")
                .active(true)
                .build();

        User user2 = User.builder()
                .id("user2")
                .username("testuser2")
                .email("test2@example.com")
                .active(true)
                .build();

        List<User> users = List.of(user1, user2);
        UserResponse response = new UserResponse(users);

        assertNotNull(response.users());
        assertEquals(2, response.users().size());
        assertTrue(response.users().contains(user1));
        assertTrue(response.users().contains(user2));
    }

    @Test
    void userResponse_WithEmptyList_ShouldBeValid() {
        UserResponse response = new UserResponse(List.of());

        assertNotNull(response.users());
        assertEquals(0, response.users().size());
    }

    @Test
    void userResponse_WithSingleUser_ShouldContainThatUser() {
        User user = User.builder()
                .id("user1")
                .username("testuser")
                .email("test@example.com")
                .active(true)
                .build();

        UserResponse response = new UserResponse(List.of(user));

        assertEquals(1, response.users().size());
        assertEquals("testuser", response.users().get(0).getUsername());
    }

    @Test
    void userResponse_Equality_ShouldBeEqual() {
        User user = User.builder()
                .id("user1")
                .username("testuser")
                .email("test@example.com")
                .active(true)
                .build();

        UserResponse response1 = new UserResponse(List.of(user));
        UserResponse response2 = new UserResponse(List.of(user));

        assertEquals(response1, response2);
    }

    @Test
    void userResponse_Equality_ShouldNotBeEqual() {
        User user1 = User.builder()
                .id("user1")
                .username("testuser1")
                .email("test1@example.com")
                .active(true)
                .build();

        User user2 = User.builder()
                .id("user2")
                .username("testuser2")
                .email("test2@example.com")
                .active(true)
                .build();

        UserResponse response1 = new UserResponse(List.of(user1));
        UserResponse response2 = new UserResponse(List.of(user2));

        assertNotEquals(response1, response2);
    }

    @Test
    void userResponse_ToString_ShouldContainUserInfo() {
        User user = User.builder()
                .id("user1")
                .username("testuser")
                .email("test@example.com")
                .active(true)
                .build();

        UserResponse response = new UserResponse(List.of(user));

        String toString = response.toString();

        assertTrue(toString.contains("testuser") || toString.contains("test@example.com"));
    }

    @Test
    void userResponse_WithMultipleUsers_ShouldMaintainOrder() {
        User user1 = User.builder()
                .id("user1")
                .username("testuser1")
                .email("test1@example.com")
                .active(true)
                .build();

        User user2 = User.builder()
                .id("user2")
                .username("testuser2")
                .email("test2@example.com")
                .active(true)
                .build();

        User user3 = User.builder()
                .id("user3")
                .username("testuser3")
                .email("test3@example.com")
                .active(true)
                .build();

        List<User> users = List.of(user1, user2, user3);
        UserResponse response = new UserResponse(users);

        assertEquals("testuser1", response.users().get(0).getUsername());
        assertEquals("testuser2", response.users().get(1).getUsername());
        assertEquals("testuser3", response.users().get(2).getUsername());
    }

    @Test
    void userResponse_CanAccessUsersMultipleTimes() {
        User user = User.builder()
                .id("user1")
                .username("testuser")
                .email("test@example.com")
                .active(true)
                .build();

        UserResponse response = new UserResponse(List.of(user));

        // Access users multiple times
        assertEquals(1, response.users().size());
        assertEquals(1, response.users().size());
        assertEquals("testuser", response.users().get(0).getUsername());
    }

}
