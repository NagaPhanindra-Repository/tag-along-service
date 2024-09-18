package com.itf.TagAlongService.service;
import com.itf.TagAlongService.dto.CreateUserDto;
import com.itf.TagAlongService.dto.LoginDto;
import com.itf.TagAlongService.model.Role;
import com.itf.TagAlongService.model.TagAlongUser;
import com.itf.TagAlongService.repository.UserRepository;
import com.itf.TagAlongService.util.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;


    public AuthenticationServiceImpl(
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String login(LoginDto loginDto) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        return token;
    }

    @Override
    public TagAlongUser createUser(CreateUserDto createUserDto) {
        TagAlongUser tagAlongUser = new TagAlongUser();
        tagAlongUser.setUserName(createUserDto.getUserName());
        tagAlongUser.setCountryCode(createUserDto.getCountryCode());
        tagAlongUser.setEmailAddress(createUserDto.getEmailAddress());
        tagAlongUser.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        tagAlongUser.setFirstName(createUserDto.getFirstName());
        tagAlongUser.setLastName(createUserDto.getLastName());
        tagAlongUser.setMobileNumber(createUserDto.getMobileNumber());
        Set roles= new HashSet<>();
        Role role = new Role();
        role.setName(createUserDto.getRoleName());
        roles.add(role);
        tagAlongUser.setRoles(roles);
        userRepository.save(tagAlongUser);
        return userRepository.save(tagAlongUser);
    }
}
