package com.zsc.partnermatch.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.partnermatch.commont.ErrorCode;
import com.zsc.partnermatch.commont.TeamStatusEnum;
import com.zsc.partnermatch.dto.TeamJoinRequest;
import com.zsc.partnermatch.dto.TeamQuery;
import com.zsc.partnermatch.dto.TeamQuitRequest;
import com.zsc.partnermatch.dto.TeamUpdateRequest;
import com.zsc.partnermatch.entity.Team;
import com.zsc.partnermatch.entity.User;
import com.zsc.partnermatch.entity.UserTeam;
import com.zsc.partnermatch.event.TeamEvent;
import com.zsc.partnermatch.exception.BusinessException;
import com.zsc.partnermatch.mapper.TeamMapper;
import com.zsc.partnermatch.service.ITeamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.partnermatch.service.IUserService;
import com.zsc.partnermatch.service.IUserTeamService;
import com.zsc.partnermatch.vo.TeamUserVo;
import com.zsc.partnermatch.vo.UserVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 队伍 服务实现类
 * </p>
 *
 * @author 周书超
 * @since 2026-04-29
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements ITeamService {

    @Autowired
    private IUserTeamService userTeamService;
    @Autowired
    private IUserService userService;
    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public Long addTeam(Team team, User loginUser) {
        //1.效验信息是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断用户是否登录，没有登录不能创建队伍
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //3.效验队伍名称、描述字段长度是否符合要求
        String teamName = team.getName();
        String teamDescription = team.getDescription();
        if (StrUtil.isBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称长度不符合要求");
        }
        if (StrUtil.isBlank(teamDescription) && teamDescription.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述长度不符合要求");
        }
        //4.判断最大人数是否符合要求
        Integer maxNum = team.getMaxNum();
        if (maxNum == null || maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最大人数不符合要求");
        }
        //5.判断过期时间是否符合要求
        LocalDateTime expireTime = team.getExpireTime();
        if (expireTime == null || expireTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不符合要求");
        }
        //6.判断用户最多创建队伍数量是否符合要求
        // todo 这个逻辑需要优化 可能在并发的情况下，会创建多个队伍会超过5个
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getUserId, loginUser.getId());
        Long l = baseMapper.selectCount(wrapper);
        if (l >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建5个队伍");
        }

        //7.判断状态是否符合要求  如果状态为null 则默认为公开
        Integer status = Optional.ofNullable(team.getStatus()).orElse(TeamStatusEnum.PUBLIC.getValue());
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if (status < 0 || enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不符合要求");
        }
        //8.如果状态为加密，则判断密码是否符合要求
        String password = team.getPassword();
        if (enumByValue.equals(TeamStatusEnum.SECRET)) {
            if (StrUtil.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不符合要求");
            }

        }
        //9.创建队伍
        team.setId(null);
        team.setUserId(loginUser.getId());
        boolean newTeam = this.save(team);
        if (!newTeam) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        //10.插入队伍和用户关联表
        Long teamId = team.getId();
        Long id = loginUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(id);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(LocalDateTime.now());
        boolean save = userTeamService.save(userTeam);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }

        // 发布队伍创建事件（触发画像重算 + 缓存失效）
        applicationEventPublisher.publishEvent(
                new TeamEvent(this, loginUser.getId(), teamId, TeamEvent.EventType.CREATE));

        return team.getId();
    }

    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, Boolean isAdmin) {
        //1.写查询逻辑
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            //根据id查询
            if (id != null) {
                wrapper.eq(true, Team::getId, id);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                wrapper.eq(Team::getUserId, userId);
            }

            List<Long> listId = teamQuery.getListId();
            if (!CollectionUtils.isEmpty(listId)) {
                wrapper.in(Team::getId, listId);
            }

            //根据搜索文本查询
            String searchText = teamQuery.getSearchText();
            if (StrUtil.isNotBlank(searchText)) {
                wrapper.and(w -> w.like(Team::getName, searchText)
                        .or()
                        .like(Team::getDescription, searchText));
            }
            //根据名称查询
            String name = teamQuery.getName();
            if (StrUtil.isNotBlank(name)) {
                wrapper.like(name != null, Team::getName, name);
            }
            //根据描述查询
            String description = teamQuery.getDescription();
            if (StrUtil.isNotBlank(description)) {
                wrapper.like(description != null, Team::getDescription, description);
            }
            //根据最大人数查询
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null) {
                wrapper.eq(Team::getMaxNum, maxNum);
            }
            //根据过期时间查询
            wrapper.and(w -> w.isNull(Team::getExpireTime).or().gt(Team::getExpireTime, LocalDateTime.now()));

            //根据状态查询 0-公开 1-私有 2-加密 只用管理员可以查看加密的房间
//            Integer status = teamQuery.getStatus();
//            TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
//            if (enumByValue == null) {
//                enumByValue = TeamStatusEnum.PUBLIC;
//            }
//            if (!isAdmin && !enumByValue.equals(TeamStatusEnum.PUBLIC)) {
//                throw new BusinessException(ErrorCode.NO_AUTH,"无权查看非公开队伍");
//            }
//            wrapper.eq(Team::getStatus, enumByValue.getValue());
            Integer status = teamQuery.getStatus();
            if (status != null) {
                TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
                if (enumByValue == null) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态值非法");
                }
                // 非管理员无权查看非公开队伍
                if (!isAdmin && !enumByValue.equals(TeamStatusEnum.PUBLIC)) {
                    throw new BusinessException(ErrorCode.NO_AUTH, "无权查看非公开队伍");
                }
                wrapper.eq(Team::getStatus, enumByValue.getValue());
            } else {
                // 未传状态：非管理员只能看公开；管理员（或 isAdmin=true）不限制状态
                if (!isAdmin) {
                    wrapper.in(Team::getStatus, TeamStatusEnum.PUBLIC, TeamStatusEnum.PRIVATE);
                }
                // 如果 isAdmin == true，则不添加 status 条件，可查询所有状态
            }



        }
        List<Team> list = this.list(wrapper);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        //2.关联创建者用户信息
        List<TeamUserVo> teamUserVos = new ArrayList<>();
        for (Team team : list) {
            if (team == null) {
                continue;
            }
            Long userId = team.getUserId();
            User user = userService.getById(userId);
            User user1 = userService.getUser1(user);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            if (user1 != null) {
                UserVo uservo = new UserVo();
                BeanUtils.copyProperties(user1, uservo);
                teamUserVo.setCreateuser(uservo);
            }

            teamUserVos.add(teamUserVo);
        }


        return teamUserVos;
    }



















    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        //1.判断参数是否合法
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断队伍是否存在
        Long id = teamUpdateRequest.getId();
        Team Oldteam = this.getById(id);
        if (Oldteam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //3.判断当前用户是否是队伍的创建者和管理者
        if (!Objects.equals(Oldteam.getId(), loginUser.getId()) && loginUser.getRole() != 1) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //4.判断状态是否为加密 是一定要用密码
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(Oldteam.getStatus());
        if (enumByValue.equals(TeamStatusEnum.SECRET)) {
            if (StrUtil.isBlank(teamUpdateRequest.getPassword()) || teamUpdateRequest.getPassword().length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不符合要求");
            }
        }
        //5.更新数据
        Team UpdateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, UpdateTeam);
        return this.updateById(UpdateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        //1.判断参数是否合法
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断队伍是否存在
        Long teamId = teamJoinRequest.getTeamId();
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //3.判断队伍是否过期
        if (team.getExpireTime() != null && team.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        //4.判断队伍状态是否为私用 是 用户不能加入
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
        if (teamStatusEnum.equals(TeamStatusEnum.PRIVATE)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        //5.判断队伍状态是否为加密 是，要提供密码是否跟存储的密码一致
        if (teamStatusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StrUtil.isBlank(teamJoinRequest.getPassword()) || !teamJoinRequest.getPassword().equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        RLock lock = redissonClient.getLock("sc:user:team:lock");
        try {
            // 尝试等待最多 3 秒获取锁，获取成功后锁持有时间 10 秒（根据需要设置）
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {

                        //6.判断当前用户是否已经加入过这个队伍
                        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("userId", loginUser.getId());
                        queryWrapper.eq("teamId", teamId);
                        long count = userTeamService.count(queryWrapper);
                        if (count > 0) {
                            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入过这个队伍");
                        }
                        //7.判断队伍人数是否大于最大人数
                        long count1 = userTeamService.count(new QueryWrapper<UserTeam>().eq("teamId", teamId));
                        if (count1 >= team.getMaxNum()) {
                            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                        }
                        //8.用户不能加入自己创建的队伍
                        if (team.getUserId().equals(loginUser.getId())) {
                            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能加入自己创建的队伍");
                        }
                        //9.插入数据
                        UserTeam userTeam = new UserTeam();
                        userTeam.setUserId(loginUser.getId());
                        userTeam.setTeamId(teamId);
                        userTeam.setJoinTime(LocalDateTime.now());
                        boolean saved = userTeamService.save(userTeam);
                        if (saved) {
                            log.info("[加入队伍] userId={} 加入 teamId={}", loginUser.getId(), teamId);
                            // 发布队伍加入事件（触发画像重算 + 缓存失效）
                            applicationEventPublisher.publishEvent(
                                    new TeamEvent(this, loginUser.getId(), teamId, TeamEvent.EventType.JOIN));
                        }
                        return saved;
                    }

        } catch (InterruptedException e) {
            log.error("获取锁失败");
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return false;
    }

    @Override
    @Transactional
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        //1.判断参数是否合法
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断队伍是否存在
        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //3.判断用户是否在队伍中
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.eq("teamId", teamId);
        long count = userTeamService.count(queryWrapper);
        if (count <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未加入队伍");
        }
        //4.判断队伍人数 如果只剩一人，队伍解散  还剩其他人 如果是队长，将队长权限移给最早加入的人 非队长直接退出队伍
        long count1 = userTeamService.count(new QueryWrapper<UserTeam>().eq("teamId", teamId));
        if (count1 == 1) {
            //队伍解散 删除队伍 这个是在team表中删除的
            this.removeById(teamId);
            boolean removed = userTeamService.remove(new QueryWrapper<UserTeam>().eq("teamId", teamId));
            if (removed) {
                log.info("[退出队伍-解散] userId={} 解散 teamId={}", loginUser.getId(), teamId);
                // 发布队伍解散事件
                applicationEventPublisher.publishEvent(
                        new TeamEvent(this, loginUser.getId(), teamId, TeamEvent.EventType.QUIT));
            }
            return removed;

        }
        if (count1 > 1) {
            //队伍人数大于一人 是队长
            if (Objects.equals(team.getUserId(), loginUser.getId())) {
                //将队长权限转为 早加入的人 获取最早加入的人
                LambdaQueryWrapper<UserTeam> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.orderByAsc(UserTeam::getId).last("limit 2");
                List<UserTeam> list = userTeamService.list(queryWrapper1);
                if (list.isEmpty() || list.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = list.get(1);
                //下一个队长的id
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                //更新队伍的队长
                boolean update = this.updateById(updateTeam);
                if (!update) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }

            }

        }
        // 无论是否队长，最后都要删除当前用户的加入记录
        boolean removed = userTeamService.remove(
                new LambdaQueryWrapper<UserTeam>()
                        .eq(UserTeam::getTeamId, teamId)
                        .eq(UserTeam::getUserId, loginUser.getId())
        );
        if (!removed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出队伍失败");
        }
        log.info("[退出队伍] userId={} 退出 teamId={}", loginUser.getId(), teamId);
        // 发布队伍退出事件（触发画像重算 + 缓存失效）
        applicationEventPublisher.publishEvent(
                new TeamEvent(this, loginUser.getId(), teamId, TeamEvent.EventType.QUIT));
        return true;
    }

    @Override
    @Transactional
    public boolean deleteTeam(Long id, User loginUser) {
        //1.判断参数是否合法
        if (id == null ||id <= 0 ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断队伍是否存在
        Team team = this.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //3.判断当前用户是否是队伍的创建者
        if (!Objects.equals(team.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        //4.查询队伍中所有成员 ID（用于发布事件，必须在删除关联记录之前）
        List<UserTeam> memberList = userTeamService.list(
                new QueryWrapper<UserTeam>().eq("teamId", id));
        List<Long> memberUserIds = memberList.stream()
                .map(UserTeam::getUserId)
                .toList();

        //5.移除所用加入队伍的关联用户
        boolean removed = userTeamService.remove(new QueryWrapper<UserTeam>().eq("teamId", id));
        if (!removed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }

        //6.删除队伍
        boolean deleted = this.removeById(id);
        if (deleted) {
            log.info("[删除队伍] teamId={} 被 userId={} 删除", id, loginUser.getId());
            // 为队伍中每个成员发布删除事件（触发画像重算 + 缓存失效）
            for (Long memberId : memberUserIds) {
                applicationEventPublisher.publishEvent(
                        new TeamEvent(this, memberId, id, TeamEvent.EventType.DELETE));
            }
        }
        return deleted;
    }

}
