package com.zsc.partnermatch.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.partnermatch.annotation.RateLimit;
import com.zsc.partnermatch.commont.BaseResponse;
import com.zsc.partnermatch.commont.ErrorCode;
import com.zsc.partnermatch.dto.UserLoginRequest;
import com.zsc.partnermatch.dto.UserRegisterRequest;
import com.zsc.partnermatch.entity.Team;
import com.zsc.partnermatch.entity.User;
import com.zsc.partnermatch.entity.UserProfile;
import com.zsc.partnermatch.exception.BusinessException;
import com.zsc.partnermatch.service.ITeamService;
import com.zsc.partnermatch.service.IUserService;
import com.zsc.partnermatch.service.IUserProfileService;
import com.zsc.partnermatch.utils.ResultUtils;
import com.zsc.partnermatch.vo.MatchResultVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 周书超
 * @since 2026-04-24
 */
@RestController
@RequestMapping("/user")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true")
public class UserController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserProfileService userProfileService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        Long id = userService.userRegister(
                userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserPassword(),
                userRegisterRequest.getCheckPassword()
        );
        return ResultUtils.success(id);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        User user = userService.getLoginUser(
                userLoginRequest.getUserAccount(),
                userLoginRequest.getUserPassword(),
                request
        );
        return ResultUtils.success(user);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(
            @RequestParam(defaultValue = "1") long current,   // 当前页码
            @RequestParam(defaultValue = "10") long size,     // 每页大小
            HttpServletRequest request) {
        Page<User> userPage = userService.recommendUsers(current, size, request);
        return ResultUtils.success(userPage);
    }



    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        List<User> users = userService.searchUsers(username, request);
        return ResultUtils.success(users);
    }










    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody Long id, HttpServletRequest request) {
        boolean deleted = userService.delete(id, request);
        return ResultUtils.success(deleted);
    }

//    @GetMapping("/current")
//    public BaseResponse<User> getCurrentUser(HttpSession session) {
//        User user = (User) session.getAttribute("userLoginState");
//        if (user == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
//        }
//        System.out.println("当前用户：" + user);
//        return ResultUtils.success(user);
//    }


    /**
     * 获取当前登录用户信息
     * @param request HttpServletRequest对象，用于获取用户的登录态（即Session）
     * @return 统一返回类 BaseResponse，其中包装了脱敏后的用户信息
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        // 1. 从请求中获取当前登录用户信息
        User currentUser = userService.getCurrentUser(request);
        // 2. 如果用户未登录或查询失败，返回错误信息
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 3. 返回成功结果，数据为脱敏后的用户对象
        return ResultUtils.success(currentUser);
    }



    @GetMapping("/search/tags")
    // @RequestParam(required = false) 表示该参数是可选的，如果不提供，则使用默认值（null）
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        log.info("tagNameList: {} " , tagNameList);
        List<User> users = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(users);
    }



    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        log.info("user: {}", user);
        // 1. 校验参数
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 校验权限 这个在业务层开发中，这里省略
        //3. 更新用户信息
        int result = userService.updateUser(user, request);
        return ResultUtils.success(result);


    }
    /**
     * 获取最匹配的用户信息
     * @param request HttpServletRequest对象，用于获取用户的登录态（即Session）
     * @return 统一返回类 BaseResponse，其中包装了脱敏后的用户信息
     */

    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(
            @RequestParam(defaultValue = "1") long num,
            HttpServletRequest request) {
        List<User> users = userService.matchUsers(num, request);
        return ResultUtils.success(users);
    }

    /**
     * 智能匹配用户（余弦相似度 + 倒排索引 + TF-IDF + AI 推荐理由）
     * 限流保护：每秒最多 5 个请求，防止 AI 接口被打爆
     */
    @GetMapping("/match/smart")
    @RateLimit(qps = 5, fallbackMsg = "智能匹配请求过于频繁，请稍后再试")
    public BaseResponse<List<MatchResultVO>> smartMatchUsers(
            @RequestParam(defaultValue = "10") long num,
            HttpServletRequest request) {
        if (num <= 0 || num > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "num 需在 1~100 之间");
        }
        List<MatchResultVO> results = userService.smartMatchUsers(num, request);
        return ResultUtils.success(results);
    }

    /**
     * 获取用户画像（组队次数、活跃标签、协作评分）
     */
    @GetMapping("/profile")
    public BaseResponse<UserProfile> getUserProfile(
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        User loginUser = userService.getUser(request);
        // 未指定 userId 则查看自己的画像
        Long targetId = (userId != null && userId > 0) ? userId : loginUser.getId();
        UserProfile profile = userProfileService.getProfileOrDefault(targetId);
        return ResultUtils.success(profile);
    }

    /**
     * 重新计算用户画像
     */
    @PostMapping("/profile/calculate")
    public BaseResponse<UserProfile> calculateProfile(HttpServletRequest request) {
        User loginUser = userService.getUser(request);
        UserProfile profile = userProfileService.calculateProfile(loginUser.getId());
        return ResultUtils.success(profile);
    }

}
