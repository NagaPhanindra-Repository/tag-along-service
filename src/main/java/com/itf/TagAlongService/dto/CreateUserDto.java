package com.itf.TagAlongService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {


    private String userName;

    private String password;
    private String emailAddress;
    private String mobileNumber;
    private String countryCode;
    private String firstName;
    private String lastName;

    private String roleName;
}
