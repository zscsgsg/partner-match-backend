import myAxios from "../plugins/request";
import type { TeamType } from "../models/team";

/**
 * 创建队伍
 */
export const addTeam = async (teamData: any) => {
  return myAxios.request({
    url: "/team/add",
    method: "POST",
    data: teamData,
  });
};

/**
 * 删除队伍（解散）
 */
export const deleteTeam = async (id: number) => {
  return myAxios.request({
    url: "/team/delete",
    method: "POST",
    data: id,
    headers: {
      "Content-Type": "application/json",
    },
  });
};

/**
 * 更新队伍
 */
export const updateTeam = async (teamData: Partial<TeamType>) => {
  return myAxios.request({
    url: "/team/update",
    method: "POST",
    data: teamData,
    headers: {
      "Content-Type": "application/json",
    },
  });
};

/**
 * 加入队伍
 */
export const joinTeam = async (teamId: number, password?: string) => {
  return myAxios.request({
    url: "/team/join",
    method: "POST",
    data: {
      teamId,
      password: password || "",
    },
    headers: {
      "Content-Type": "application/json",
    },
  });
};

/**
 * 退出队伍
 */
export const quitTeam = async (teamId: number) => {
  return myAxios.request({
    url: "/team/quit",
    method: "POST",
    data: {
      teamId,
    },
    headers: {
      "Content-Type": "application/json",
    },
  });
};

/**
 * 获取队伍列表（带查询条件）
 */
export const listTeams = async (queryParams?: {
  id?: number;
  searchText?: string;
  name?: string;
  description?: string;
  maxNum?: number;
  status?: number;
}) => {
  return myAxios.request({
    url: "/team/list",
    method: "GET",
    params: queryParams,
  });
};

/**
 * 获取队伍详情
 */
export const getTeamById = async (id: number) => {
  return myAxios.request({
    url: "/team/get",
    method: "GET",
    params: { id },
  });
};

/**
 * 获取我创建的队伍列表
 * @param queryParams 查询参数（可选，如 name, status 等）
 */
export const listMyCreateTeams = async (queryParams?: {
  name?: string;
  status?: number;
  // 其他字段根据需要
}) => {
  return myAxios.request({
    url: "/team/list/my/create",
    method: "GET",
    params: queryParams,
  });
};

/**
 * 获取我加入的队伍列表
 * @param queryParams 查询参数（可选）
 */
export const listMyJoinTeams = async (queryParams?: {
  name?: string;
  status?: number;
}) => {
  return myAxios.request({
    url: "/team/list/my/join",
    method: "GET",
    params: queryParams,
  });
};

/**
 * 获取队伍聊天历史消息（分页）
 * @param teamId 队伍 ID
 * @param current 当前页码
 * @param size 每页大小
 */
export const getChatMessages = async (
  teamId: number,
  current: number = 1,
  size: number = 50,
) => {
  return myAxios.request({
    url: `/team/${teamId}/messages`,
    method: "GET",
    params: { current, size },
  });
};

/**
 * 获取用户组队历史
 * @param userId 用户 ID
 */
export const getUserTeamHistory = async (userId: number) => {
  return myAxios.request({
    url: `/team/${userId}/history`,
    method: "GET",
  });
};
