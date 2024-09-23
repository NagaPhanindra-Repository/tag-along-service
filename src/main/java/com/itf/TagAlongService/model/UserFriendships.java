package com.itf.TagAlongService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_friendships")
public class UserFriendships {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String followerName;

    @Column
    private boolean followRequest;

    @Column
    private String response;

    @Column
    private LocalDateTime requestedDateTime;

    @Column
    private LocalDateTime respondedDateTime;
}
