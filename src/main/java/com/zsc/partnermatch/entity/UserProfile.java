package com.zsc.partnermatch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户画像实体
 * 记录用户的组队行为数据，用于匹配权重优化
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_profile")
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 创建的队伍总数
     */
    private Integer totalTeamsCreated;

    /**
     * 加入的队伍总数（含自己创建的）
     */
    private Integer totalTeamsJoined;

    /**
     * 平均组队时长（天）
     */
    private Double avgTeamDuration;

    /**
     * 最近活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 偏好标签（JSON 数组，Top5 常用标签）
     */
    private String preferredTags;

    /**
     * 协作评分（1.0~5.0）
     * 基于组队次数、组队时长等综合计算
     */
    private Double cooperationScore;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
