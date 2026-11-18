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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositery userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public String signUp(SignUpDto signUp) {
        if (userRepo.existsByEmail(signUp.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        User user = new User();
        user.setUsername(signUp.getUsername());
        user.setPassword(passwordEncoder.encode(signUp.getPassword()));
        user.setEmail(signUp.getEmail());
        user.setRole(Role.valueOf(signUp.getRole().toUpperCase()));
        user.setPhone(signUp.getPhone());
        userRepo.save(user);
        return "Profile created successfully";
    }

    public LoginResDto login(LoginReqDto login) {
        Optional<User> existingUser = userRepo.findByEmail(login.getEmail());
        if (existingUser.isPresent() &&
                passwordEncoder.matches(login.getPassword(), existingUser.get().getPassword())) {
            User user = existingUser.get();
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());
            LoginResDto response = new LoginResDto();
            response.setAccessToken(token);
            response.setStatus("Success");
            response.setRole(user.getRole().toString());
            response.setName(user.getUsername());
            response.setUserId(user.getEmail());
            return response;
        }
        throw new UnauthorizedException("Invalid email or password");
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            log.info("Validating token for user: {}", username);
            String role = jwtUtil.extractUserRole(token);
            boolean isValid = !jwtUtil.isTokenExpired(token);
            return new TokenValidationResponse(isValid, username, role);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return new TokenValidationResponse(false, null, null);
        }
    }
}
