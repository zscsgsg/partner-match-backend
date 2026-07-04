package com.zsc.partnermatch.service;

import com.zsc.partnermatch.entity.Team;
import com.zsc.partnermatch.entity.User;
import com.zsc.partnermatch.entity.UserProfile;
import com.zsc.partnermatch.entity.UserTeam;
import com.zsc.partnermatch.mapper.UserProfileMapper;
import com.zsc.partnermatch.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserProfileService 单元测试
 * 使用 Mockito mock 数据层
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    @Mock
    private ITeamService teamService;
    @Mock
    private IUserTeamService userTeamService;
    @Mock
    private IUserService userService;
    @Mock
    private UserProfileMapper userProfileMapper;

    @Test
    @DisplayName("获取默认画像: 用户不存在时返回默认值")
    void getProfileOrDefault_noProfile_returnsDefault() {
        // mock getOne 返回 null（用户画像不存在）
        when(userProfileMapper.selectOne(any())).thenReturn(null);

        UserProfile profile = userProfileService.getProfileOrDefault(999L);

        assertNotNull(profile);
        assertEquals(999L, profile.getUserId());
        assertEquals(0, profile.getTotalTeamsCreated());
        assertEquals(0, profile.getTotalTeamsJoined());
        assertEquals(0.0, profile.getAvgTeamDuration());
        assertEquals(1.0, profile.getCooperationScore());
    }

    @Test
    @DisplayName("计算画像: 有组队记录时正确统计")
    void calculateProfile_withTeamRecords_correctStats() {
        Long userId = 1L;

        // mock 创建的队伍数
        when(teamService.count(any())).thenReturn(2L);

        // mock 加入的队伍记录
        UserTeam ut1 = new UserTeam().setUserId(userId).setTeamId(10L).setJoinTime(LocalDateTime.now().minusDays(30));
        UserTeam ut2 = new UserTeam().setUserId(userId).setTeamId(20L).setJoinTime(LocalDateTime.now().minusDays(10));
        when(userTeamService.list(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(ut1, ut2));

        // mock 队友查询（每次返回空，简化测试）
        when(userTeamService.list(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(ut1, ut2))   // 第一次: 加入的队伍
                .thenReturn(Collections.emptyList())    // 第二次: 队伍 10 的队友
                .thenReturn(Collections.emptyList());   // 第三次: 队伍 20 的队友

        // mock 用户信息
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setTags("[\"Java\", \"Spring\"]");
        when(userService.getById(userId)).thenReturn(mockUser);

        // mock saveOrUpdate
        when(userProfileMapper.insert(any(UserProfile.class))).thenReturn(1);
        when(userProfileMapper.updateById(any(UserProfile.class))).thenReturn(1);

        UserProfile profile = userProfileService.calculateProfile(userId);

        assertNotNull(profile);
        assertEquals(2, profile.getTotalTeamsCreated());
        assertEquals(2, profile.getTotalTeamsJoined());
        assertTrue(profile.getAvgTeamDuration() >= 10); // 平均至少 10 天
        assertTrue(profile.getCooperationScore() > 1.0); // 有组队记录，评分应高于基础分
    }

    @Test
    @DisplayName("计算画像: 无组队记录时返回基础值")
    void calculateProfile_noTeamRecords_basicValues() {
        Long userId = 1L;

        when(teamService.count(any())).thenReturn(0L);
        when(userTeamService.list(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setTags("[\"Java\"]");
        when(userService.getById(userId)).thenReturn(mockUser);

        when(userProfileMapper.insert(any(UserProfile.class))).thenReturn(1);
        when(userProfileMapper.updateById(any(UserProfile.class))).thenReturn(1);

        UserProfile profile = userProfileService.calculateProfile(userId);

        assertNotNull(profile);
        assertEquals(0, profile.getTotalTeamsCreated());
        assertEquals(0, profile.getTotalTeamsJoined());
        assertEquals(0.0, profile.getAvgTeamDuration());
        assertEquals(1.0, profile.getCooperationScore()); // 基础分
    }
}
