package com.zsc.partnermatch.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.partnermatch.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.partnermatch.vo.MatchResultVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 周书超
 * @since 2026-04-24
 */
public interface IUserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     *  用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request 请求
     * @return
     */
    User getLoginUser(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户搜索
     * @param username
     * @param request
     * @return
     */
    List<User> searchUsers(String username, HttpServletRequest request);
    /**
     * 删除用户
     * @param id
     * @param request
     * @return
     */
    boolean delete(Long id, HttpServletRequest request);



    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    List<User>  searchUsersByTags(List<String> tagNameList);
/**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getCurrentUser(HttpServletRequest request);
/**
     * 更新用户信息
     * @param user
     * @param request
     * @return
     */
    int updateUser(User user, HttpServletRequest request);
/**
     * 推荐用户
     * @param request
     * @return
     */
        Page<User> recommendUsers(long current, long size,HttpServletRequest request);

        /**
        * 获取当前登录用户
        * @param request
        * @return
        */
        User getUser(HttpServletRequest request);


    /**
     * 脱敏用户
     * @param user
     * @return
     */
    User getUser1(User user);


    boolean isAdmin(User loginUser);

    /**
     * 匹配用户（编辑距离算法，旧版保留）
     * @param num
     * @param request
     * @return
     */
    List<User> matchUsers(long num, HttpServletRequest request);

    /**
     * 智能匹配用户（余弦相似度+倒排索引+TF-IDF + AI推荐理由）
     * @param num 返回数量
     * @param request HTTP请求
     * @return 匹配结果列表（含AI推荐理由）
     */
    List<MatchResultVO> smartMatchUsers(long num, HttpServletRequest request);
}
