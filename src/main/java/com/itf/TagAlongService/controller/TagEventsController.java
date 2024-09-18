package com.itf.TagAlongService.controller;

import com.itf.TagAlongService.model.Event;
import com.itf.TagAlongService.repository.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
@RestController
@RequestMapping("/tag")
public class TagEventsController {

    private EventRepository eventRepository;

    @GetMapping("/events/{userName}")
    public ResponseEntity<List<Event>> getUserEvents(@PathVariable(value = "userName") String userName){
        System.out.println("inside");

        return ResponseEntity.ok(eventRepository.findByUserName(userName));
    }
}
