package com.zsc.partnermatch.service.impl;

import com.zsc.partnermatch.entity.Tag;
import com.zsc.partnermatch.mapper.TagMapper;
import com.zsc.partnermatch.service.ITagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 周书超
 * @since 2026-04-24
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

    @Override
    public List<Map<String, Object>> getTagTree() {
        // 1. 查询所有标签
        List<Tag> allTags = this.list();

        // 2. 构建 parentId → 子标签列表 的映射
        Map<Long, List<Tag>> childrenMap = new HashMap<>();
        for (Tag tag : allTags) {
            Long parentId = tag.getParentId();
            if (parentId != null) {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(tag);
            }
        }

        // 3. 构建前端 van-tree-select 两层级树
        List<Map<String, Object>> result = new ArrayList<>();

        for (Tag tag : allTags) {
            if (tag.getIsParent() == 1) {
                // 父标签：检查是否有孙子标签
                List<Tag> directChildren = childrenMap.getOrDefault(tag.getId(), Collections.emptyList());
                boolean hasGrandChildren = directChildren.stream()
                        .anyMatch(c -> childrenMap.containsKey(c.getId()));

                if (hasGrandChildren) {
                    // 有孙子 → 把有孙子的子标签提升为分类，无孙子的子标签作为独立标签
                    for (Tag child : directChildren) {
                        List<Tag> grandChildren = childrenMap.get(child.getId());
                        if (grandChildren != null && !grandChildren.isEmpty()) {
                            // 提升为分类节点
                            Map<String, Object> category = new LinkedHashMap<>();
                            category.put("text", child.getTagName());
                            category.put("id", child.getTagName());
                            category.put("children", grandChildren.stream().map(gc -> {
                                Map<String, Object> leaf = new LinkedHashMap<>();
                                leaf.put("text", gc.getTagName());
                                leaf.put("id", gc.getTagName());
                                return leaf;
                            }).collect(Collectors.toList()));
                            result.add(category);
                        } else {
                            // 无孙子 → 独立叶子标签
                            Map<String, Object> leaf = new LinkedHashMap<>();
                            leaf.put("text", child.getTagName());
                            leaf.put("id", child.getTagName());
                            result.add(leaf);
                        }
                    }
                } else {
                    // 无孙子 → 自己作为分类节点，直接子标签作为叶子
                    Map<String, Object> category = new LinkedHashMap<>();
                    category.put("text", tag.getTagName());
                    category.put("id", tag.getTagName());
                    category.put("children", directChildren.stream().map(c -> {
                        Map<String, Object> leaf = new LinkedHashMap<>();
                        leaf.put("text", c.getTagName());
                        leaf.put("id", c.getTagName());
                        return leaf;
                    }).collect(Collectors.toList()));
                    result.add(category);
                }
            } else if (tag.getParentId() == null) {
                // 无父标签的叶子标签 → 独立可选标签
                Map<String, Object> leaf = new LinkedHashMap<>();
                leaf.put("text", tag.getTagName());
                leaf.put("id", tag.getTagName());
                result.add(leaf);
            }
            // 有 parentId 的非父标签已在上面作为子标签处理，跳过
        }

        return result;
    }
}
