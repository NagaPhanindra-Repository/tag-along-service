package com.itf.TagAlongService.repository;

import com.itf.TagAlongService.model.UserFriendships;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFriendsRepositroy extends JpaRepository<UserFriendships, Long> {
    List<UserFriendships> findByUserName(String userName);
}
