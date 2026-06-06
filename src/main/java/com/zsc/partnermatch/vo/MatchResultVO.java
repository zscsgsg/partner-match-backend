package com.zsc.partnermatch.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 智能匹配结果 VO，包含匹配用户、各项得分和 AI 推荐理由
 */
@Data
public class MatchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 头像 */
    private String avatarUrl;

    /** 个人简介 */
    private String profile;

    /** 用户标签 */
    private List<String> tags;

    /** 综合匹配得分 (0~1) */
    private Double totalScore;

    /** 标签相似度 (0~1) */
    private Double tagSimilarity;

    /** 互补性得分 (0~1) */
    private Double complementScore;

    /** 共同标签 */
    private List<String> commonTags;

    /** AI 推荐理由 */
    private String explanation;
}
