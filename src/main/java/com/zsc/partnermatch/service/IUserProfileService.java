package com.zsc.partnermatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.partnermatch.entity.UserProfile;

import java.util.List;
import java.util.Map;

/**
 * 用户画像服务
 */
public interface IUserProfileService extends IService<UserProfile> {

    /**
     * 计算或更新指定用户的画像
     * @param userId 用户 ID
     * @return 更新后的画像
     */
    UserProfile calculateProfile(Long userId);

    /**
     * 获取指定用户的画像，不存在则返回默认画像
     * @param userId 用户 ID
     * @return 用户画像
     */
    UserProfile getProfileOrDefault(Long userId);

    /**
     * 批量获取用户画像映射（一次 SQL，避免 N+1）
     * @param userIds 用户 ID 列表
     * @return userId → UserProfile 映射（无画像的 ID 不存在于 map 中）
     */
    Map<Long, UserProfile> getProfileMapByUserIds(List<Long> userIds);
}
