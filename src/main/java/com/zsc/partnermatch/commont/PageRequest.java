package com.zsc.partnermatch.commont;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -4542138087017035315L;
    /**
     * 页面大小
     */
    private int  pageSize ;

    /**
     * 当前页号
     */
    private int pageNum ;
}
