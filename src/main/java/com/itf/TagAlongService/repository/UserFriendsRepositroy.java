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
    List<UserFriendships> findPeopleYouMayKnowUnique(List<String> userName, String followerName);

    @Query("select u from UserFriendships u where u.userName in(  " +
            "select uf.followerName from UserFriendships uf where uf.userName = :userName" +
            ") and u.followerName != :userName and u.followerName not in (" +
            "select uff.followerName from UserFriendships uff where uff.userName = :userName )")
    List<UserFriendships> findPeopleYouMayKnow(String userName);
}
