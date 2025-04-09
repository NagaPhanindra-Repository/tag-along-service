package com.itf.TagAlongService.controller;


import com.itf.TagAlongService.dto.UserFollwerDto;
import com.itf.TagAlongService.model.UserFriendships;
import com.itf.TagAlongService.repository.UserFriendsRepositroy;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
@RestController
@RequestMapping("/tag/friends")
public class UserFollowersController {

    private UserFriendsRepositroy userFriendsRepositroy;

    @PostMapping("/request")
    public ResponseEntity<UserFriendships> followRequest(@RequestBody UserFollwerDto userFollwerDto){


        UserFriendships userFriendships = userFriendsRepositroy.save(getUserMapper(userFollwerDto));


        return ResponseEntity.ok(userFriendships);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserFriendships>> getAllUsersFriendships(){
        List<UserFriendships> userFriendships = userFriendsRepositroy.findAll();
        return ResponseEntity.ok(userFriendships);
    }

    @GetMapping("/{userName}")
    public ResponseEntity<List<UserFriendships>> getPeopleYouMayKnow(@PathVariable(value = "userName") String userName){

        //List<String> followerNames = userFriendsRepositroy.findByUserName(userName).stream().map(userFriendships -> userFriendships.getFollowerName()).toList();
        List<UserFriendships> userFriendships = userFriendsRepositroy.findPeopleYouMayKnow(userName);
        return ResponseEntity.ok(userFriendships);
    }

    @GetMapping("request/{userName}")
    public ResponseEntity<List<UserFriendships>> getRequests(@PathVariable(value = "userName") String userName){
        List<UserFriendships> userFriendships = userFriendsRepositroy.findByUserNameAndResponse(userName, null);
        return ResponseEntity.ok(userFriendships);
    }

    @PutMapping("/respond")
    public ResponseEntity<UserFriendships> responseToRequest(@PathVariable(value = "userName") String userName,@RequestBody UserFollwerDto userFollwerDto){
        UserFriendships user = userFriendsRepositroy.findByUserNameAndFollowerNameAndResponseIsNull(userFollwerDto.getUserName(),userFollwerDto.getFollowerName());
        user.setResponse("Accepted");
        UserFriendships userFriendships = userFriendsRepositroy.save(user);


        return ResponseEntity.ok(userFriendships);
    }

    private UserFriendships getUserMapper(UserFollwerDto userFollwerDto) {
        UserFriendships userFriendships = new UserFriendships();
        userFriendships.setUserName(userFollwerDto.getUserName());
        userFriendships.setFollowerName(userFollwerDto.getFollowerName());
        if(userFollwerDto.getRequestType().equals("follow")){
            userFriendships.setFollowRequest(true);
        }
        userFriendships.setResponse("R");
        userFriendships.setRequestedDateTime(LocalDateTime.now());
        return userFriendships;
    }


}
