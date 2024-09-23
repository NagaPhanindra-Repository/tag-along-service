package com.itf.TagAlongService.controller;


import com.itf.TagAlongService.dto.UserFollwerDto;
import com.itf.TagAlongService.model.UserFriendships;
import com.itf.TagAlongService.repository.UserFriendsRepositroy;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
@RestController
@RequestMapping("/friends")
public class UserFollowersController {

    private UserFriendsRepositroy userFriendsRepositroy;

    @PostMapping("/request")
    public ResponseEntity<UserFriendships> signIn(@RequestBody UserFollwerDto userFollwerDto){


        UserFriendships userFriendships = userFriendsRepositroy.save(getUserMapper(userFollwerDto));


        return ResponseEntity.ok(userFriendships);
    }

    private UserFriendships getUserMapper(UserFollwerDto userFollwerDto) {

        UserFriendships userFriendships = new UserFriendships();
        userFriendships.setUserName(userFollwerDto.getUserName());
        userFriendships.setFollowerName(userFollwerDto.getFollowerName());
        if(userFollwerDto.getRequestType().equals("follow")){
            userFriendships.setFollowRequest(true);
        }
        userFriendships.setResponse(null);
        userFriendships.setRequestedDateTime(LocalDateTime.now());

        return userFriendships;
    }


}
