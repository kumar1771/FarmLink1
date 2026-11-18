package com.example.Auth.service.Controller;

import com.example.Auth.service.Dto.LoginReqDto;
import com.example.Auth.service.Dto.LoginResDto;
import com.example.Auth.service.Dto.SignUpDto;
import com.example.Auth.service.Model.TokenValidationResponse;
import com.example.Auth.service.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/Users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @Value("${apikey}")
    private String api_key;
    @PostMapping("signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpDto signUpDto){
        return ResponseEntity.ok(userService.signUp(signUpDto));
    }
    @PostMapping("login")
    public ResponseEntity<LoginResDto> login(@Valid @RequestBody LoginReqDto login){
        return ResponseEntity.ok(userService.login(login));
    }
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Api-Key") String apikey) {

        log.info("Received token validation request");

        if(!apikey.equals(api_key)) {
            log.warn("API key mismatch");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(token == null || !token.startsWith("Bearer ")) {
            log.warn("Token missing or invalid format");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TokenValidationResponse validateToken = userService.validateToken(token);
        log.info("Validation result: {}", validateToken);

        return ResponseEntity.ok(validateToken);
    }
}
