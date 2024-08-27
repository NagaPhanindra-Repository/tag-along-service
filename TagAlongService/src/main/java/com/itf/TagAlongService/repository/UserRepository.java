package com.itf.TagAlongService.repository;

import com.itf.TagAlongService.model.TagAlongUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<TagAlongUser, Long> {

    Optional<TagAlongUser> findByUserName(String userName);

    Boolean existsByemailAddress(String emailAddress);

    Optional<TagAlongUser> findByUserNameOrEmailAddress(String userName, String emailAddress);

}