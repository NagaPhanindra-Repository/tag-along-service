package com.itf.TagAlongService.repository;

import com.itf.TagAlongService.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUserName(String userName);

    List<Event> findByUserNameIn(List<String> userNames);
}
