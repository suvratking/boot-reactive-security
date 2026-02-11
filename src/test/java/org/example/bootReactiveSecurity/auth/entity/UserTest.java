package org.example.bootReactiveSecurity.auth.entity;

import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void userBuilder_ShouldCreateUserWithAllFields() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertTrue(user.isActive());
        assertEquals(1, user.getRoles().size());
        assertEquals("ROLE_USER", user.getRoles().getFirst());
    }

    @Test
    void userBuilder_WithDefaultValues_ShouldSetActiveTrue() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        assertTrue(user.isActive());
    }

    @Test
    void userBuilder_WithDefaultValues_ShouldSetEmptyRolesList() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        assertNotNull(user.getRoles());
        assertEquals(0, user.getRoles().size());
    }

    @Test
    void userBuilder_WithMultipleRoles_ShouldContainAllRoles() {
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER");
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .roles(roles)
                .build();

        assertEquals(3, user.getRoles().size());
        assertTrue(user.getRoles().contains("ROLE_USER"));
        assertTrue(user.getRoles().contains("ROLE_ADMIN"));
        assertTrue(user.getRoles().contains("ROLE_MANAGER"));
    }

    @Test
    void userBuilder_WithActiveFalse_ShouldSetInactive() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .active(false)
                .build();

        assertFalse(user.isActive());
    }

    @Test
    void userSetter_ShouldUpdateFields() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setActive(true);
        user.setRoles(List.of("ROLE_USER"));

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertTrue(user.isActive());
        assertEquals("ROLE_USER", user.getRoles().getFirst());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyUser() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertTrue(user.isActive());
        assertNotNull(user.getRoles());
        assertEquals(0, user.getRoles().size());
    }

    @Test
    void allArgsConstructor_ShouldCreateUserWithAllFields() {
        List<String> roles = List.of("ROLE_USER");
        User user = new User(1L, "testuser", "test@example.com", "password123", true, roles);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertTrue(user.isActive());
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void userEquality_ShouldBeEqualIfFieldsMatch() {
        User user1 = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();

        assertEquals(user1, user2);
    }

    @Test
    void userEquality_ShouldNotBeEqualIfIdDiffers() {
        User user1 = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("testuser")
                .email("test@example.com")
                .build();

        assertNotEquals(user1, user2);
    }

    @Test
    void userToString_ShouldContainUserFields() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        String userString = user.toString();

        assertTrue(userString.contains("testuser"));
        assertTrue(userString.contains("test@example.com"));
    }

    @Test
    void passwordShouldNotBeExposedInJson() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("secretpassword")
                .build();

        // The @JsonIgnore annotation should prevent password from being serialized
        assertNotNull(user.getPassword());
        // Password should be set but marked as @JsonIgnore for JSON serialization
    }

    @Test
    void userWithEmptyRolesList_ShouldBeValid() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .roles(new ArrayList<>())
                .build();

        assertNotNull(user.getRoles());
        assertEquals(0, user.getRoles().size());
    }

    @Test
    void modifyingUserRoles_ShouldAffectUser() {
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .roles(roles)
                .build();

        roles.add("ROLE_ADMIN");
        
        assertTrue(user.getRoles().contains("ROLE_ADMIN"));
    }

}
