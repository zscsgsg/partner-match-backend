package com.zsc.partnermatch.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.zsc.partnermatch.commont.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 队伍查询封装类
 */
@Data
public class TeamQuery extends PageRequest {
    /**
     * id
     */

    private Long id;


    /**
     * id列表
     */

    private List< Long> listId;


    /**
     *  搜索关键词（队伍名称、描述）
     */
    private String searchText;




    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;


    /**
     * 创建人 id（队长）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;





}
