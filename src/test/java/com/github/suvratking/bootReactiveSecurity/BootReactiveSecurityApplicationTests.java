package com.github.suvratking.bootReactiveSecurity;

import com.github.suvratking.bootReactiveSecurity.auth.repository.UserRepository;
import com.github.suvratking.bootReactiveSecurity.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BootReactiveSecurityApplicationTests {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthService authService;

    @Test
    void contextLoads() {
    }

}
