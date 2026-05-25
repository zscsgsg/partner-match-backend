package com.zsc.partnermatch.controller;


import com.zsc.partnermatch.commont.BaseResponse;
import com.zsc.partnermatch.service.ITagService;
import com.zsc.partnermatch.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>

 * @author 周书超
 * @since 2026-04-24
 */
@RestController
@RequestMapping("/tag")
@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true")
public class TagController {

    @Autowired
    private ITagService tagService;

    /**
     * 获取标签树（适配前端 van-tree-select 两层级结构）
     */
    @GetMapping("/tree")
    public BaseResponse<List<Map<String, Object>>> getTagTree() {
        List<Map<String, Object>> tree = tagService.getTagTree();
        return ResultUtils.success(tree);
    }
}
