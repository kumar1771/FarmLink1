package com.example.Auth.service.Service;

import com.example.Auth.service.Dto.LoginReqDto;
import com.example.Auth.service.Dto.LoginResDto;
import com.example.Auth.service.Dto.SignUpDto;
import com.example.Auth.service.EnumForRoles.Role;
import com.example.Auth.service.Exception.DuplicateResourceException;
import com.example.Auth.service.Exception.UnauthorizedException;
import com.example.Auth.service.Model.TokenValidationResponse;
import com.example.Auth.service.Model.User;
import com.example.Auth.service.Repositery.UserRepositery;
import com.example.Auth.service.Util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepositery userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private UserService userService;

    private SignUpDto signUpDto;
    private LoginReqDto loginReqDto;
    private User existingUser;

    @BeforeEach
    void setUp() {
        signUpDto = new SignUpDto("john", "john@example.com", "password123", "FARMER", "1234567890");
        loginReqDto = new LoginReqDto("john@example.com", "password123");

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("john");
        existingUser.setEmail("john@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setRole(Role.FARMER);
        existingUser.setPhone("1234567890");
    }

    // ─── signUp ───────────────────────────────────────────────────────────────

    @Test
    void signUp_success() {
        when(userRepo.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        String result = userService.signUp(signUpDto);

        assertEquals("Profile created successfully", result);
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void signUp_duplicateEmail_throwsException() {
        when(userRepo.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.signUp(signUpDto));
        verify(userRepo, never()).save(any());
    }

    @Test
    void signUp_invalidRole_throwsException() {
        signUpDto.setRole("INVALID_ROLE");
        when(userRepo.existsByEmail("john@example.com")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.signUp(signUpDto));
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    void login_success() {
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("john@example.com", "FARMER")).thenReturn("mock.jwt.token");

        LoginResDto result = userService.login(loginReqDto);

        assertEquals("mock.jwt.token", result.getAccessToken());
        assertEquals("Success", result.getStatus());
        assertEquals("FARMER", result.getRole());
        assertEquals("john", result.getName());
    }

    @Test
    void login_wrongPassword_throwsException() {
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.login(loginReqDto));
    }

    @Test
    void login_emailNotFound_throwsException() {
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> userService.login(loginReqDto));
    }

    // ─── validateToken ────────────────────────────────────────────────────────

    @Test
    void validateToken_validToken_returnsValid() {
        when(jwtUtil.extractUsername("Bearer valid.token")).thenReturn("john@example.com");
        when(jwtUtil.extractUserRole("Bearer valid.token")).thenReturn("FARMER");
        when(jwtUtil.isTokenExpired("Bearer valid.token")).thenReturn(false);

        TokenValidationResponse result = userService.validateToken("Bearer valid.token");

        assertTrue(result.isValid());
        assertEquals("john@example.com", result.getUsername());
        assertEquals("FARMER", result.getRole());
    }

    @Test
    void validateToken_expiredToken_returnsInvalid() {
        when(jwtUtil.extractUsername("Bearer expired.token"))
                .thenThrow(new RuntimeException("Token expired"));

        TokenValidationResponse result = userService.validateToken("Bearer expired.token");

        assertFalse(result.isValid());
        assertNull(result.getUsername());
        assertNull(result.getRole());
    }
}
