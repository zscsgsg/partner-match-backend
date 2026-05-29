package com.zsc.partnermatch.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 队伍和用户信息
 */
@Data
public class TeamUserVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */

    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建人 id（队长）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人信息
     */
    private UserVo createuser;
     /**
     * 已加入的队伍人数
     */
    private Integer hasJoinNum;
    /**
     * 是否已加入
     */
    private boolean hasJoin= false;



}
