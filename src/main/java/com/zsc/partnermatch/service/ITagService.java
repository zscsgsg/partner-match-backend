package com.zsc.partnermatch.service;

import com.zsc.partnermatch.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 周书超
 * @since 2026-04-24
 */
public interface ITagService extends IService<Tag> {

    /**
     * 获取标签树（适配前端 van-tree-select 两层级结构）
     * @return 标签树列表
     */
    List<Map<String, Object>> getTagTree();
}
