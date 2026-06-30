import myAxios from "../plugins/request";

/**
 * 获取标签树（适配 van-tree-select 两层级结构）
 */
export const getTagTree = async () => {
  return myAxios.request({
    url: "/tag/tree",
    method: "GET",
  });
};
