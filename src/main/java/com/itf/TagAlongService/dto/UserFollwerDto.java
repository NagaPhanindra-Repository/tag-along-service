package com.itf.TagAlongService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserFollwerDto {


    private String userName;

    private String followerName;

    private String requestType;
}
