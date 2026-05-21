package com.zsc.partnermatch.service.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.partnermatch.commont.ErrorCode;
import com.zsc.partnermatch.entity.User;
import com.zsc.partnermatch.exception.BusinessException;
import com.zsc.partnermatch.mapper.UserMapper;
import com.zsc.partnermatch.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.partnermatch.service.MatchAlgorithmService;
import com.zsc.partnermatch.service.MatchExplainService;
import com.zsc.partnermatch.service.IUserProfileService;
import com.zsc.partnermatch.entity.UserProfile;
import com.zsc.partnermatch.utils.AigorithmUtils;
import com.zsc.partnermatch.vo.MatchResultVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
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
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private static final String SALT = "shuchao";
    private static final String USER_LOGIN_STATE = "userLoginState";
    // 缓存空值标记，用于防穿透
    private static final String EMPTY_CACHE_MARKER = "EMPTY_PAGE";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MatchAlgorithmService matchAlgorithmService;
    @Autowired
    private MatchExplainService matchExplainService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    @Lazy
    private IUserProfileService userProfileService;

    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.效验输入是否为空
        if(StrUtil.hasBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }
        //2.效验账户长度和密码长度
        if( userAccount.length()<4||userPassword.length()<6||checkPassword.length()<6){
            throw new BusinessException("账号和密码长度不符合");
        }
        //效验账号不包含特殊字符 定义账户名校验规则：只允许包含字母、数字和下划线
        String validPattern ="^[a-zA-Z0-9_]+$";
        if(!ReUtil.isMatch(validPattern,userAccount)){
            throw new BusinessException("账号不符合");
        }
        //3.密码和校验密码相同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException("密码不一致");
        }
        //4.账户不能重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount,userAccount);
        if(baseMapper.selectCount(wrapper)>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }

        //5.对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        User user = new User()
                .setUserAccount(userAccount)
                .setUserPassword(encryptPassword);

        //6.保存用户
        if(this.save(user)){
            log.info("[用户注册] 注册成功 userAccount={}", userAccount);
            return user.getId();
        }
        throw new BusinessException("注册失败");
    }

    @Override
    public User getLoginUser(String userAccount, String userPassword, HttpServletRequest request) {
        //1.效验输入是否为空
        if(StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException("参数为空");
        }
        //2.效验账户长度和密码长度
        if( userAccount.length()<4||userPassword.length()<6){
            throw new BusinessException("账号和密码长度不符合");
        }
        //3.效验账号不包含特殊字符
        String validPattern ="^[a-zA-Z0-9_]+$";
        if(!ReUtil.isMatch(validPattern,userAccount)){
            throw new BusinessException("账号不符合");
        }
        //4.密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount,userAccount)
                .eq(User::getUserPassword,encryptPassword);
        //5.查询用户
        User user = this.getOne(wrapper);
        //6.是否存在
        if(user==null){
            return null;
        }
        //7.对用户脱敏
        User safeUser = getUser(user);
        // 8.记录用户登录态 用作session 如果做的是其他的可以用Redis进行存储
        request.getSession().setAttribute(USER_LOGIN_STATE,safeUser);
        log.info("[用户登录] 登录成功 userAccount={} userId={}", userAccount, safeUser.getId());
        //9.返回脱敏后的用户信息
        return safeUser;
    }

    @Override
    public List<User> searchUsers(String username, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        // 仅管理员可查询
        if(loginUser==null || loginUser.getRole()!=1){
            return new ArrayList<>();
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null,User::getUsername,username);
        List<User> userList = this.list(wrapper);
        return userList.stream().map(UserServiceImpl::getUser).toList();
    }

    @Override
    public boolean delete(Long id, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if(loginUser==null || loginUser.getRole()!=1){
            return false;
        }
        return this.removeById(id);

    }
//    /**
//     * 根据标签搜索用户 用SQL语句进行查询
//     * @param tagNameList
//     * @return
//     */
//    @Override
//    public List<User> searchUsersByTags(List<String> tagNameList) {
//        //1.判断标签列表是否为空
//        if(tagNameList==null||tagNameList.isEmpty()){
//           throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        //2.根据like 查询 and 查询
//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//        tagNameList.forEach(tagName -> {
//            wrapper.like(tagName!=null,User::getTags,tagName);
//        });
//        //3.查询用户列表
//        List<User> userList = this.list(wrapper);
//        //对用户列表进行脱敏
//        return userList.stream().map(UserServiceImpl::getUser).toList();
//    }



//    /**
//     * 根据标签搜索用户 用内存查询
//     * @param tagNameList
//     * @return
//     */
//    @Override
//    public List<User> searchUsersByTags(List<String> tagNameList) {
//        //1.判断标签列表是否为空
//        if(tagNameList==null||tagNameList.isEmpty()){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        //2.先查所用 用户
//        List<User> userList = this.list();
//        //3.在内存中判断是否包含
//        return userList.stream().filter(user -> {
//           //获取当前用户的标签
//           String tags = user.getTags();
//           if(StrUtil.isBlank( tags)){
//               return false;
//           }
//           //把 JSON 数组样子的字符串，变成一个真正的 Java List
//           List<String> tagList = JSONUtil.toList(tags, String.class);
//           //如果集合为空 返回一个空集合 否则返回一个非空的集合
//           tagList= Optional.ofNullable(tagList).orElse(new ArrayList<>())
// 这个and 判断条件是判断用户标签是否包含所有的标签
//           for(String tagName:tagNameList){
//               if(!tagList.contains(tagName)){
//                   return false;
//               }
//           }
//           return true;
//       }).map(UserServiceImpl::getUser).toList();
//
////        //对用户列表进行脱敏
////        return userList.stream().map(UserServiceImpl::getUser).toList();
//    }



    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        // 1. 判空
        if (tagNameList == null || tagNameList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 查所有用户
        List<User> userList = this.list();

        // 3. 内存过滤：任意匹配
        return userList.stream()
                .filter(user -> {
                    String tags = user.getTags();
                    if (StrUtil.isBlank(tags)) {
                        return false;
                    }
                    List<String> tagList = JSONUtil.toList(tags, String.class);
                    tagList= Optional.ofNullable(tagList).orElse(new ArrayList<>());
                    // 关键修改：任意匹配 只要一个标签匹配 就返回 true
                    return tagNameList.stream().anyMatch(tagList::contains);
                })
                .map(UserServiceImpl::getUser)
                .collect(Collectors.toList());
    }

    @Override
    public User getCurrentUser(HttpServletRequest request) {
        // 1. 从 Session 中获取登录用户的完整信息
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        // 2. 如果 Session 中没有，表示用户未登录，返回 null
        if (currentUser == null) {
            return null;
        }
        // 3. 【关键】信息脱敏：根据 userId 从数据库重新查询最新的用户信息，并过滤掉密码等敏感字段
        long userId = currentUser.getId();
        User safeUser = this.getById(userId);
        return getUser(safeUser);
    }

    @Override
    public int updateUser(User user, HttpServletRequest request) {
      //1. 校验参数用户id是否有效
        Long id = user.getId();
        if (id == null ||id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断用是否为登录
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //3.判断是否为管理权限 是，可以修改任意用户，不是只允许修改自己的用户信息
        if(loginUser.getRole() != 1 && !user.getId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //4.判断用户是否存在
        User user1 = baseMapper.selectById(id);
        if(user1 == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        //5.修改用户
        return this.baseMapper.updateById(user);


    }




    public Page<User> recommendUsers(long current, long size, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        String redisKey = String.format("zsc:user:recommend:%s:%s:%s", loginUser.getId(), current, size);

        // 1. 尝试读缓存
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            // 防穿透：命中空值标记，直接返回空页
            if (EMPTY_CACHE_MARKER.equals(cached)) {
                log.info("[缓存防穿透] 命中空值标记 userId={}", loginUser.getId());
                return new Page<>(current, size);
            }
            log.info("[缓存命中] recommendUsers userId={}", loginUser.getId());
            return (Page<User>) cached;
        }

        // 2. 缓存未命中，尝试获取互斥锁（防击穿）
        String lockKey = "zsc:cache:lock:recommend:" + loginUser.getId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 等待最多 3 秒，锁持有时间 10 秒
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                // Double-check: 再次检查缓存（可能其他线程已重建）
                Object doubleCheck = redisTemplate.opsForValue().get(redisKey);
                if (doubleCheck != null) {
                    if (EMPTY_CACHE_MARKER.equals(doubleCheck)) {
                        return new Page<>(current, size);
                    }
                    return (Page<User>) doubleCheck;
                }

                // 3. 查询 DB 重建缓存
                log.info("[缓存未命中] 重建缓存 userId={}", loginUser.getId());
                Page<User> page = new Page<>(current, size);
                LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
                Page<User> userPage = this.baseMapper.selectPage(page, wrapper);
                List<User> records = userPage.getRecords()
                        .stream()
                        .map(UserServiceImpl::getUser)
                        .collect(Collectors.toList());
                userPage.setRecords(records);

                // 4. 防雪崩：TTL 加随机偏移
                long ttlMinutes = 120 + ThreadLocalRandom.current().nextInt(0, 30);

                // 5. 防穿透：空结果也缓存，但 TTL 较短
                if (records.isEmpty()) {
                    log.info("[缓存防穿透] 空结果缓存 userId={}", loginUser.getId());
                    redisTemplate.opsForValue().set(redisKey, EMPTY_CACHE_MARKER, 5, TimeUnit.MINUTES);
                    return new Page<>(current, size);
                }

                redisTemplate.opsForValue().set(redisKey, userPage, ttlMinutes, TimeUnit.MINUTES);
                log.info("[缓存重建] userId={} ttl={}min records={}", loginUser.getId(), ttlMinutes, records.size());
                return userPage;
            } else {
                // 获取锁失败，返回旧数据或空页
                log.warn("[缓存击穿防护] 获取锁失败 userId={}", loginUser.getId());
                return new Page<>(current, size);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[缓存重建] 获取锁被中断 userId={}", loginUser.getId());
            return new Page<>(current, size);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public User getUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        return loginUser;
    }


    /**
     * 获取脱敏用户
     * @param user
     * @return
     */
    private static  User getUser(User user) {
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setGender(user.getGender());
        safeUser.setProfile(user.getProfile());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setRole(user.getRole());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setCreateTime(user.getCreateTime());
//        safeUser.setPlanetCode(user.getPlanetCode());
        safeUser.setTags(user.getTags());
        return safeUser;
    }





    /**
     * 获取脱敏用户
     * @param user
     * @return
     */
    public  User getUser1(User user) {
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setGender(user.getGender());
        safeUser.setProfile(user.getProfile());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setRole(user.getRole());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setCreateTime(user.getCreateTime());
//        safeUser.setPlanetCode(user.getPlanetCode());
        safeUser.setTags(user.getTags());
        return safeUser;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getRole() == 1;
    }

//    @Override
//    public List<User> matchUsers(long num, HttpServletRequest request) {
//        // 1. 获取当前用户
//        User currentUser = getUser(request);
//        // 2. 获取当前用户的标签
//        String currentUserTags = currentUser.getTags();
//        if(StrUtil.isBlank(currentUserTags)){
//            return new ArrayList<>();
//        }
//        //转为list集合
//        List<String> tagList = JSONUtil.toList(currentUserTags, String.class);
//        // 3. 搜索所有用户
//        QueryWrapper< User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.select("id", "tags").isNotNull("tags");
//        List<User> list = this.list(queryWrapper);
//        //用户列表下标 -> 分数（相似度）
//        SortedMap<Integer ,Long> tagSimilarityMap = new TreeMap<>();
//         for(int i = 0; i < list.size(); i++){
//             User user = list.get(i);
//             String tags = user.getTags();
//             if(StrUtil.isBlank(tags) || Objects.equals(user.getId(),currentUser.getId())){
//                 continue;
//             }
//             List<String> userTagList = JSONUtil.toList(tags, String.class);
//             Long similarity = (long) AigorithmUtils.minDistance(tagList, userTagList);
//             // 4.计算出相似度添加进map
//             tagSimilarityMap.put(i, similarity);
//         }
//         //5.根据num取出最相似的num个用户
//          List<Integer> ids = tagSimilarityMap.keySet().stream().limit(num).toList();
//         //6.返回用户
//          return ids.stream().map(u-> getUser1(list.get(u))).collect(Collectors.toList());
//
//    }

    /**
     * 匹配用户 这个是优化版
     * @param num
     * @param request
     * @return
     */
    @Override
    public List<User> matchUsers(long num, HttpServletRequest request) {
        // 1. 获取当前用户
        User currentUser = getUser(request);
        // 2. 获取当前用户的标签
        String currentUserTags = currentUser.getTags();
        if(StrUtil.isBlank(currentUserTags)){
            return new ArrayList<>();
        }
        //转为list集合
        List<String> tagList = JSONUtil.toList(currentUserTags, String.class);
        // 3. 搜索所有用户
        QueryWrapper< User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags").isNotNull("tags");
        List<User> list = this.list(queryWrapper);

        List<Pair<User, Long>> tagSimilarityMap = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            User user = list.get(i);
            String tags = user.getTags();
            if(StrUtil.isBlank(tags) || Objects.equals(user.getId(),currentUser.getId())){
                continue;
            }
            List<String> userTagList = JSONUtil.toList(tags, String.class);
            Long similarity = (long) AigorithmUtils.minDistance(tagList, userTagList);
            // 4.计算出相似度添加进list
            tagSimilarityMap.add(new Pair<>(user, similarity));

        }
        //5.根据num取出最相似的num个用户 同时排序
        List<Pair<User, Long>> list1 = tagSimilarityMap.stream().sorted((a, b) -> (int) (a.getValue() - b.getValue())).limit(num).toList();

        //6.返回用户
        return list1.stream().map(u-> getUser1(u.getKey())).collect(Collectors.toList());

    }

    /**
     * 智能匹配用户（余弦相似度 + 倒排索引 + TF-IDF + AI推荐理由）+ Redis 缓存防护
     * @param num 返回匹配用户数量
     * @param request HTTP请求
     * @return 匹配结果（含AI推荐理由）
     */
    @Override
    public List<MatchResultVO> smartMatchUsers(long num, HttpServletRequest request) {
        // 1. 获取当前用户
        User currentUser = getUser(request);
        String currentUserTags = currentUser.getTags();
        if (StrUtil.isBlank(currentUserTags)) {
            return new ArrayList<>();
        }

        String redisKey = String.format("zsc:user:smartMatch:%s:%s", currentUser.getId(), num);

        // 2. 尝试读缓存
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            if (EMPTY_CACHE_MARKER.equals(cached)) {
                log.info("[智能匹配缓存] 命中空值标记 userId={}", currentUser.getId());
                return new ArrayList<>();
            }
            log.info("[智能匹配缓存] 命中 userId={}", currentUser.getId());
            return (List<MatchResultVO>) cached;
        }

        // 3. 缓存未命中，尝试获取互斥锁（防击穿）
        String lockKey = "zsc:cache:lock:smartMatch:" + currentUser.getId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                // Double-check
                Object doubleCheck = redisTemplate.opsForValue().get(redisKey);
                if (doubleCheck != null) {
                    if (EMPTY_CACHE_MARKER.equals(doubleCheck)) {
                        return new ArrayList<>();
                    }
                    return (List<MatchResultVO>) doubleCheck;
                }

                log.info("[智能匹配缓存] 重建缓存 userId={}", currentUser.getId());
                long startTime = System.currentTimeMillis();

                // 4. 执行匹配计算
                List<MatchResultVO> voList = doSmartMatch(currentUser, num);

                // 5. 防雪崩：TTL 加随机偏移（10~20分钟）
                long ttlMinutes = 10 + ThreadLocalRandom.current().nextInt(0, 11);

                // 6. 防穿透：空结果也缓存，TTL 较短
                if (voList.isEmpty()) {
                    log.info("[智能匹配缓存] 空结果缓存 userId={}", currentUser.getId());
                    redisTemplate.opsForValue().set(redisKey, EMPTY_CACHE_MARKER, 3, TimeUnit.MINUTES);
                    return voList;
                }

                redisTemplate.opsForValue().set(redisKey, voList, ttlMinutes, TimeUnit.MINUTES);
                long elapsed = System.currentTimeMillis() - startTime;
                log.info("[智能匹配缓存] 重建完成 userId={} result={} ttl={}min elapsed={}ms",
                        currentUser.getId(), voList.size(), ttlMinutes, elapsed);
                return voList;
            }
        } catch (InterruptedException e) {
            log.error("[智能匹配锁] 获取锁失败", e);
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        // 7. 降级：拿不到锁时直接计算（不缓存，避免重复写）
        log.warn("[智能匹配] 获取锁超时，降级为直接计算 userId={}", currentUser.getId());
        return doSmartMatch(currentUser, num);
    }

    /**
     * 执行智能匹配计算（纯算法逻辑，不含缓存）
     */
    private List<MatchResultVO> doSmartMatch(User currentUser, long num) {
        long startTime = System.currentTimeMillis();

        // 2. 查询所有有标签的用户（排除当前用户）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "username", "avatarUrl", "profile", "tags")
                .isNotNull("tags")
                .ne("id", currentUser.getId());
        List<User> allUsers = this.list(queryWrapper);
        allUsers.add(currentUser);

        // 3. 构建用户画像映射（一次批量查询，避免 N+1）
        List<Long> userIds = allUsers.stream().map(User::getId).distinct().toList();
        Map<Long, UserProfile> profileMap = userProfileService.getProfileMapByUserIds(userIds);
        // 补充默认画像（无画像记录的用户）
        profileMap.putIfAbsent(currentUser.getId(), buildDefaultProfile(currentUser.getId()));
        for (User u : allUsers) {
            profileMap.putIfAbsent(u.getId(), buildDefaultProfile(u.getId()));
        }

        // 4. 调用智能匹配算法
        List<MatchAlgorithmService.MatchResult> results =
                matchAlgorithmService.smartMatch(currentUser, allUsers, (int) num, profileMap);

        // 5. 并行填充 AI 推荐理由
        matchExplainService.fillExplanations(currentUser, results);

        // 6. 转换为 VO
        List<MatchResultVO> voList = results.stream().map(r -> {
            MatchResultVO vo = new MatchResultVO();
            vo.setUserId(r.user.getId());
            vo.setUsername(r.user.getUsername());
            vo.setAvatarUrl(r.user.getAvatarUrl());
            vo.setProfile(r.user.getProfile());
            vo.setTags(JSONUtil.toList(r.user.getTags(), String.class));
            vo.setTotalScore(Math.round(r.totalScore * 10000.0) / 10000.0);
            vo.setTagSimilarity(Math.round(r.tagSimilarity * 10000.0) / 10000.0);
            vo.setComplementScore(Math.round(r.complementScore * 10000.0) / 10000.0);
            vo.setCommonTags(r.commonTags);
            vo.setExplanation(r.explanation);
            return vo;
        }).collect(Collectors.toList());

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[智能匹配] userId={} 匹配到 {} 个用户，耗时 {}ms", currentUser.getId(), voList.size(), elapsed);
        return voList;
    }

    /**
     * 构建默认画像（无画像记录的用户使用）
     */
    private UserProfile buildDefaultProfile(Long userId) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setTotalTeamsCreated(0);
        profile.setTotalTeamsJoined(0);
        profile.setAvgTeamDuration(0.0);
        profile.setPreferredTags("[]");
        profile.setCooperationScore(1.0);
        return profile;
    }

}
