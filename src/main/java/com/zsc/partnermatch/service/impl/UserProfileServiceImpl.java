package com.zsc.partnermatch.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.partnermatch.entity.Team;
import com.zsc.partnermatch.entity.User;
import com.zsc.partnermatch.entity.UserProfile;
import com.zsc.partnermatch.entity.UserTeam;
import com.zsc.partnermatch.mapper.UserProfileMapper;
import com.zsc.partnermatch.service.ITeamService;
import com.zsc.partnermatch.service.IUserProfileService;
import com.zsc.partnermatch.service.IUserTeamService;
import com.zsc.partnermatch.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户画像服务实现
 * 基于组队历史数据计算用户行为画像，反馈到匹配算法
 */
@Service
@Slf4j
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile>
        implements IUserProfileService {

    @Autowired
    private ITeamService teamService;
    @Autowired
    private IUserTeamService userTeamService;
    @Autowired
    private IUserService userService;

    @Override
    public UserProfile calculateProfile(Long userId) {
        log.info("[用户画像] 开始计算 userId={}", userId);

        // 1. 统计创建的队伍数
        LambdaQueryWrapper<Team> createdWrapper = new LambdaQueryWrapper<>();
        createdWrapper.eq(Team::getUserId, userId);
        long totalCreated = teamService.count(createdWrapper);

        // 2. 统计加入的队伍数（含自己创建的）
        LambdaQueryWrapper<UserTeam> joinedWrapper = new LambdaQueryWrapper<>();
        joinedWrapper.eq(UserTeam::getUserId, userId);
        List<UserTeam> joinedTeams = userTeamService.list(joinedWrapper);
        long totalJoined = joinedTeams.size();

        // 3. 计算平均组队时长（天）
        double avgDuration = 0.0;
        if (!joinedTeams.isEmpty()) {
            long totalDays = 0;
            for (UserTeam ut : joinedTeams) {
                LocalDateTime joinTime = ut.getJoinTime();
                if (joinTime != null) {
                    totalDays += ChronoUnit.DAYS.between(joinTime, LocalDateTime.now());
                }
            }
            avgDuration = (double) totalDays / joinedTeams.size();
        }

        // 4. 统计偏好标签（从自己 + 队友的标签中提取 Top5）
        String preferredTags = calculatePreferredTags(userId, joinedTeams);

        // 5. 计算协作评分（1.0~5.0）
        // 公式: min(5.0, 基础分1.0 + 组队次数*0.3 + 平均时长*0.05)
        double cooperationScore = Math.min(5.0,
                1.0 + totalJoined * 0.3 + avgDuration * 0.05);

        // 6. 保存或更新
        LambdaQueryWrapper<UserProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserProfile::getUserId, userId);
        UserProfile existing = this.getOne(queryWrapper);

        UserProfile profile;
        if (existing != null) {
            profile = existing;
        } else {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setCreateTime(LocalDateTime.now());
        }

        profile.setTotalTeamsCreated((int) totalCreated);
        profile.setTotalTeamsJoined((int) totalJoined);
        profile.setAvgTeamDuration(Math.round(avgDuration * 100.0) / 100.0);
        profile.setLastActiveTime(LocalDateTime.now());
        profile.setPreferredTags(preferredTags);
        profile.setCooperationScore(Math.round(cooperationScore * 100.0) / 100.0);
        profile.setUpdateTime(LocalDateTime.now());

        this.saveOrUpdate(profile);

        log.info("[用户画像] 计算完成 userId={} created={} joined={} avgDuration={} score={}",
                userId, totalCreated, totalJoined, avgDuration, cooperationScore);
        return profile;
    }

    @Override
    public UserProfile getProfileOrDefault(Long userId) {
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = this.getOne(wrapper);
        if (profile == null) {
            // 返回默认画像
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setTotalTeamsCreated(0);
            profile.setTotalTeamsJoined(0);
            profile.setAvgTeamDuration(0.0);
            profile.setPreferredTags("[]");
            profile.setCooperationScore(1.0);
        }
        return profile;
    }

    @Override
    public Map<Long, UserProfile> getProfileMapByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserProfile::getUserId, userIds);
        List<UserProfile> profiles = this.list(wrapper);
        return profiles.stream()
                .collect(Collectors.toMap(UserProfile::getUserId, p -> p));
    }

    /**
     * 计算偏好标签：统计用户自身 + 所有队友的标签出现频率，取 Top5
     */
    private String calculatePreferredTags(Long userId, List<UserTeam> joinedTeams) {
        Map<String, Integer> tagCount = new HashMap<>();

        // 统计自己的标签
        User currentUser = userService.getById(userId);
        if (currentUser != null && currentUser.getTags() != null) {
            countTags(currentUser.getTags(), tagCount);
        }

        // 统计队友的标签
        for (UserTeam ut : joinedTeams) {
            LambdaQueryWrapper<UserTeam> teammates = new LambdaQueryWrapper<>();
            teammates.eq(UserTeam::getTeamId, ut.getTeamId())
                     .ne(UserTeam::getUserId, userId);
            List<UserTeam> teammateRecords = userTeamService.list(teammates);
            for (UserTeam tm : teammateRecords) {
                User teammate = userService.getById(tm.getUserId());
                if (teammate != null && teammate.getTags() != null) {
                    countTags(teammate.getTags(), tagCount);
                }
            }
        }

        // 取 Top5
        List<String> top5 = tagCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return JSONUtil.toJsonStr(top5);
    }

    private void countTags(String tagsJson, Map<String, Integer> tagCount) {
        try {
            List<String> tags = JSONUtil.toList(tagsJson, String.class);
            if (tags != null) {
                for (String tag : tags) {
                    tagCount.merge(tag, 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
