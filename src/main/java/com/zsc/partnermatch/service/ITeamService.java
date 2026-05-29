package com.zsc.partnermatch.service;

import com.zsc.partnermatch.dto.TeamJoinRequest;
import com.zsc.partnermatch.dto.TeamQuery;
import com.zsc.partnermatch.dto.TeamQuitRequest;
import com.zsc.partnermatch.dto.TeamUpdateRequest;
import com.zsc.partnermatch.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.partnermatch.entity.User;
import com.zsc.partnermatch.vo.TeamUserVo;

import java.util.List;

/**
 * <p>
 * 队伍 服务类
 * </p>
 *
 * @author 周书超
 * @since 2026-04-29
 */
public interface ITeamService extends IService<Team> {
    /**
     * 创建队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    Long addTeam(Team team, User loginUser);

    /**
     * 获取队伍列表
     *
     * @param teamQuery
     * @param loginUser
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery,Boolean isAdmin);
    /**
     * 更新队伍
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);
    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);
    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);
    /**
     * 删除队伍
     *
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(Long id, User loginUser);
}
