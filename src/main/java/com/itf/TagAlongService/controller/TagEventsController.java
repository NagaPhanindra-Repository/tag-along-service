package com.itf.TagAlongService.controller;

import com.itf.TagAlongService.model.Event;
import com.itf.TagAlongService.model.UserFriendships;
import com.itf.TagAlongService.repository.EventRepository;
import com.itf.TagAlongService.repository.UserFriendsRepositroy;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
@RestController
@RequestMapping("/tag")
public class TagEventsController {

    private EventRepository eventRepository;
    private UserFriendsRepositroy userFriendsRepositroy;

    @GetMapping("/events/{userName}")
    public ResponseEntity<List<Event>> getUserEvents(@PathVariable(value = "userName") String userName){
        System.out.println("inside");

        return ResponseEntity.ok(eventRepository.findByUserName(userName));
    }

    @GetMapping("/events/friends/{userName}")
    public ResponseEntity<List<Event>> getUserFrinedsEvents(@PathVariable(value = "userName") String userName){
        System.out.println("inside");

        List<UserFriendships> friends = userFriendsRepositroy.findByUserName(userName);
        List<String> friendsNames = friends.stream().map(friend -> friend.getFollowerName()).toList();
        return ResponseEntity.ok(eventRepository.findByUserNameIn(friendsNames));
    }

    @PutMapping("/events/update")
    public ResponseEntity<Event> updateEvent(@RequestBody Event event){
        return ResponseEntity.ok( eventRepository.save(event));
    }

    @PostMapping("/events/create")
    public ResponseEntity<Event> createEvent(@RequestBody Event event){
        return ResponseEntity.ok( eventRepository.save(event));
    }


}
