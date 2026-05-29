package com.zsc.partnermatch.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.partnermatch.commont.BaseResponse;
import com.zsc.partnermatch.commont.ErrorCode;
import com.zsc.partnermatch.dto.*;
import com.zsc.partnermatch.entity.ChatMessage;
import com.zsc.partnermatch.entity.Team;
import com.zsc.partnermatch.entity.User;
import com.zsc.partnermatch.entity.UserTeam;
import com.zsc.partnermatch.exception.BusinessException;
import com.zsc.partnermatch.service.IChatMessageService;
import com.zsc.partnermatch.service.ITeamService;
import com.zsc.partnermatch.service.IUserService;
import com.zsc.partnermatch.service.IUserTeamService;
import com.zsc.partnermatch.utils.ResultUtils;
import com.zsc.partnermatch.vo.TeamUserVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 队伍 前端控制器
 * </p>
 *
 * @author 周书超
 * @since 2026-04-29
 */
@RestController
@RequestMapping("/team")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true")
public class TeamController {
    @Autowired
    private IUserService userService;

    @Autowired
    private ITeamService teamService;
    @Autowired
    private IUserTeamService  userTeamService;
    @Autowired
    private IChatMessageService chatMessageService;
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        User loginUser = userService.getUser(request);
        Long l = teamService.addTeam(team, loginUser);
        return ResultUtils.success(l);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeamr(@RequestBody Long id,HttpServletRequest  request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getUser(request);
        boolean b = teamService.deleteTeam(id, loginUser);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(b);
    }
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest  teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getUser(request);
        boolean b = teamService.updateTeam(teamUpdateRequest,loginUser);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }


    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getUser(request);
        boolean b = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(b);
    }
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getUser(request);
        boolean b = teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultUtils.success(b);
    }











    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestParam Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams1(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getUser(request);
        boolean isAdimin = userService.isAdmin(loginUser);
        //1获取队伍列表
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery,isAdimin);
        //2、判断当前用户用户是否当前加入的队伍
        List<Long> teamIdList = teamList.stream().map(TeamUserVo::getId).toList();
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId,loginUser.getId());
        queryWrapper.in(UserTeam::getTeamId,teamIdList);
        //获取当前用户加入的队伍
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //获取当前用户加入的队伍id
        Set<Long> teamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
        teamList.forEach(teamUserVo -> {
            //判断当前用户是否加入该队伍
            boolean hasJoin = teamIdSet.contains(teamUserVo.getId());
            teamUserVo.setHasJoin(hasJoin);
        });
        //3、获取加入队伍的人数
        LambdaQueryWrapper<UserTeam> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId,loginUser.getId());
        queryWrapper.in(UserTeam::getTeamId,teamIdList);
        List<UserTeam> userTeamList1 = userTeamService.list(queryWrapper1);
        //根据队伍id进行分组 id-> List<UserTeam>加入队伍的用户列表
        Map<Long, List<UserTeam>> listMap = userTeamList1.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(teamUserVo -> {
            teamUserVo.setHasJoinNum(listMap.getOrDefault(teamUserVo.getId(),new ArrayList<>()).size());
        });
        return ResultUtils.success(teamList);
    }










    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeams(TeamQuery teamQuery) {
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 1. 创建 Page 对象
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        Team team=new Team();
        BeanUtils.copyProperties(teamQuery,team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        // 3. 执行分页查询，结果直接存入 page 对象
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(teamPage);
    }


    /**
     * 获取我创建的队伍列表（
     * @param teamQuery
     * @param request
     * @return
     */

    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> listTeams2(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取我加入的队伍列表（
     * @param teamQuery
     * @param request
     * @return
     */

    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> listTeams3(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getUser(request);
       QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
       queryWrapper.eq("userId",loginUser.getId());
       List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //取出不能重复的队伍id
        // teamId -> userid 进行分组队伍id进行分组
        //1 - 2
        //1-3
        //2-4
        //分组
        //1- 2、3
        //2-4
        //key 是队伍ID，value 是该队伍下的所有 UserTeam 记录（每个记录对应一个队员）。
        Map<Long, List<UserTeam>> collect = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> id = new ArrayList<>(collect.keySet());
        teamQuery.setListId(id);
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery,true);
        teamList.forEach(team -> team.setHasJoin(true));
        return ResultUtils.success(teamList);
    }


    /**
     * 获取队伍聊天历史消息（分页）
     * @param teamId 队伍 ID
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP 请求
     * @return 分页消息列表
     */
    @GetMapping("/{teamId}/messages")
    public BaseResponse<Page<ChatMessage>> getChatMessages(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "50") long size,
            HttpServletRequest request) {
        // 验证登录态
        User loginUser = userService.getUser(request);
        // 验证用户是否在该队伍中
        LambdaQueryWrapper<UserTeam> memberCheck = new LambdaQueryWrapper<>();
        memberCheck.eq(UserTeam::getUserId, loginUser.getId())
                   .eq(UserTeam::getTeamId, teamId);
        if (userTeamService.count(memberCheck) == 0) {
            throw new BusinessException(ErrorCode.NO_AUTH, "你不是该队伍的成员");
        }
        // 分页查询历史消息，按时间降序
        Page<ChatMessage> page = new Page<>(current, size);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getTeamId, teamId)
               .orderByDesc(ChatMessage::getCreateTime);
        Page<ChatMessage> messagePage = chatMessageService.page(page, wrapper);
        return ResultUtils.success(messagePage);
    }

    /**
     * 获取用户组队历史（含已解散队伍）
     * @param userId 用户 ID
     * @return 组队历史列表
     */
    @GetMapping("/{userId}/history")
    public BaseResponse<List<Map<String, Object>>> getUserTeamHistory(
            @PathVariable Long userId,
            HttpServletRequest request) {
        // 验证登录态
        User loginUser = userService.getUser(request);

        // 查询用户参加过的所有队伍记录
        LambdaQueryWrapper<UserTeam> utWrapper = new LambdaQueryWrapper<>();
        utWrapper.eq(UserTeam::getUserId, userId);
        List<UserTeam> userTeamRecords = userTeamService.list(utWrapper);

        if (userTeamRecords.isEmpty()) {
            return ResultUtils.success(new ArrayList<>());
        }

        List<Map<String, Object>> history = new ArrayList<>();
        for (UserTeam ut : userTeamRecords) {
            Map<String, Object> record = new HashMap<>();
            Team team = teamService.getById(ut.getTeamId());
            boolean isDissolved = (team == null);

            record.put("teamId", ut.getTeamId());
            record.put("teamName", team != null ? team.getName() : "已解散队伍");
            record.put("teamDescription", team != null ? team.getDescription() : null);
            record.put("isDissolved", isDissolved);
            record.put("isCaptain", team != null && userId.equals(team.getUserId()));
            record.put("joinTime", ut.getJoinTime());

            if (ut.getJoinTime() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(ut.getJoinTime(), LocalDateTime.now());
                record.put("durationDays", days);
            } else {
                record.put("durationDays", 0);
            }

            if (!isDissolved) {
                LambdaQueryWrapper<UserTeam> teammatesWrapper = new LambdaQueryWrapper<>();
                teammatesWrapper.eq(UserTeam::getTeamId, ut.getTeamId())
                               .ne(UserTeam::getUserId, userId);
                List<UserTeam> teammateRecords = userTeamService.list(teammatesWrapper);
                List<Map<String, Object>> teammates = new ArrayList<>();
                for (UserTeam tm : teammateRecords) {
                    User teammate = userService.getById(tm.getUserId());
                    if (teammate != null) {
                        Map<String, Object> tmMap = new HashMap<>();
                        tmMap.put("userId", teammate.getId());
                        tmMap.put("username", teammate.getUsername());
                        tmMap.put("avatarUrl", teammate.getAvatarUrl());
                        teammates.add(tmMap);
                    }
                }
                record.put("teammates", teammates);
            }

            history.add(record);
        }

        history.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("joinTime");
            LocalDateTime timeB = (LocalDateTime) b.get("joinTime");
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        return ResultUtils.success(history);
    }

}
