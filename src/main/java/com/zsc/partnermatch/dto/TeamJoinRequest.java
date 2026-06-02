package com.zsc.partnermatch.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamJoinRequest {


    /**
     * 队伍id
     */

    private Long teamId;


    /**
     * 密码
     */
    private String password;

}
