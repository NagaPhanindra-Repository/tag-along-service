package com.itf.TagAlongService.controller;

import com.itf.TagAlongService.dto.CreateUserDto;
import com.itf.TagAlongService.dto.AuthenticationSuccessResponse;
import com.itf.TagAlongService.dto.LoginDto;
import com.itf.TagAlongService.model.TagAlongUser;
import com.itf.TagAlongService.service.AuthenticationService;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private AuthenticationService authenticationService;

    // Build Login REST API
    @PostMapping("/login")
    public ResponseEntity<AuthenticationSuccessResponse> authenticate(@RequestBody LoginDto loginDto){
        String token = authenticationService.login(loginDto);

        AuthenticationSuccessResponse jwtAuthResponse = new AuthenticationSuccessResponse();
        jwtAuthResponse.setAccessToken(token);

        return ResponseEntity.ok(jwtAuthResponse);
    }

    // Build Login REST API
    @PostMapping("/signin")
    public ResponseEntity<TagAlongUser> signIn(@RequestBody CreateUserDto createUserDto){
        TagAlongUser token = authenticationService.createUser(createUserDto);


        return ResponseEntity.ok(token);
    }
}
