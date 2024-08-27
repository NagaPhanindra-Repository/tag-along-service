package com.itf.TagAlongService.service;

import com.itf.TagAlongService.dto.CreateUserDto;
import com.itf.TagAlongService.dto.LoginDto;
import com.itf.TagAlongService.model.TagAlongUser;

public interface AuthenticationService {
    String login(LoginDto loginDto);
    TagAlongUser createUser(CreateUserDto createUserDto);
}