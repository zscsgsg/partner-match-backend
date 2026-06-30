import myAxios from "../plugins/request";

/**
 * 用户注册
 * @param params
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const userRegister = async (params: any) => {
  return myAxios.request({
    url: "/user/register",
    method: "POST",
    data: params,
  });
};

/**
 * 用户登录
 * @param params
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const userLogin = async (params: any) => {
  return myAxios.request({
    url: "/user/login",
    method: "POST",
    data: params,
  });
};

/**
 * 用户注销
 * @param params
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const userLogout = async (params: any) => {
  return myAxios.request({
    url: "/user/logout",
    method: "POST",
    data: params,
  });
};

/**
 * 获取当前用户
 */
export const getCurrentUser = async () => {
  return myAxios.request({
    url: "/user/current",
    method: "GET",
  });
};

/**
 * 获取用户列表
 * @param username
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const searchUsers = async (username: any) => {
  return myAxios.request({
    url: "/user/search",
    method: "GET",
    params: {
      username,
    },
  });
};

/**
 * 删除用户
 * @param id
 */
export const deleteUser = async (id: string) => {
  return myAxios.request({
    url: "/user/delete",
    method: "POST",
    data: id,
    // 关键点：要传递 JSON 格式的值
    headers: {
      "Content-Type": "application/json",
    },
  });
};
//根据标签查询用户
export const searchUsersByTag = async (tagNameList: string) => {
  return myAxios.request({
    url: "/user/search/tags",
    method: "GET",
    params: {
      tagNameList,
    },
    headers: {
      "Content-Type": "application/json",
    },
  });
};

/**
 * 更新用户
 * @param id
 * @param user
 */
export const updateUser = async (id: number, user: any) => {
  return myAxios.request({
    url: "/user/update",
    method: "POST",
    data: {
      id,
      ...user,
    },
    headers: {
      "Content-Type": "application/json",
    },
  });
};

/**
 * 推荐用户
 */
export const recommendUsers = async (currentPage: number, pageSize: number) => {
  return myAxios.request({
    url: "/user/recommend",
    method: "GET",
    params: {
      currentPage,
      pageSize,
    },
    headers: {
      "Content-Type": "application/json",
    },
  });
};
/**
 * 获取最匹配的用户（伙伴匹配算法推荐）
 * @param num 需要返回的用户数量，默认为 1
 */
export const matchUsers = async (num: number = 1) => {
  return myAxios.request({
    url: "/user/match",
    method: "GET",
    params: {
      num,
    },
    headers: {
      "Content-Type": "application/json",
    },
  });
};

/**
 * 智能匹配用户（余弦相似度 + TF-IDF + AI 推荐理由）
 * @param num 需要返回的用户数量
 */
export const smartMatchUsers = async (num: number = 10) => {
  return myAxios.request({
    url: "/user/match/smart",
    method: "GET",
    timeout: 30000, // 智能匹配含 AI 调用，超时设为 30 秒
    params: {
      num,
    },
    headers: {
      "Content-Type": "application/json",
    },
  });
};

/**
 * 获取用户画像（组队次数、活跃标签、协作评分）
 * @param userId 用户 ID（可选，不传则查看自己）
 */
export const getUserProfile = async (userId?: number) => {
  return myAxios.request({
    url: "/user/profile",
    method: "GET",
    params: userId ? { userId } : {},
  });
};

/**
 * 重新计算当前用户的画像
 */
export const calculateUserProfile = async () => {
  return myAxios.request({
    url: "/user/profile/calculate",
    method: "POST",
  });
};
