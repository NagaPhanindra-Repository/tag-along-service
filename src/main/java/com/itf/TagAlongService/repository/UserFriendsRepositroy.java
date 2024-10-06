package com.itf.TagAlongService.repository;

import com.itf.TagAlongService.model.UserFriendships;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserFriendsRepositroy extends JpaRepository<UserFriendships, Long> {
    List<UserFriendships> findByUserName(String userName);
    List<UserFriendships> findByUserNameIn(List<String> userName);

    UserFriendships findByUserNameAndFollowerNameAndResponseIsNull(String userName, String followerName);

    List<UserFriendships> findByUserNameAndResponse(String userName, String response);

    @Query("select u from UserFriendships u where u.userName in( :userName) and u.followerName != :followerName")
    List<UserFriendships> findPeopleYouMayKnow(List<String> userName, String followerName);
}
