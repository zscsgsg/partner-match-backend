package com.zsc.partnermatch.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.partnermatch.entity.User;
import com.zsc.partnermatch.mapper.UserMapper;
import com.zsc.partnermatch.service.IUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CachePreheatTask {
        //重要用户去缓存  这个是白名单
    private List<Long> userIdList= Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);

    private HttpServletRequest request;

     @Autowired
     private IUserService userService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    // 每天 23:58:00 执行缓存预热
    @Scheduled(cron = "0 58 23 * * ?")
    public void preheatRecommendCache() {
        System.out.println("开始缓存预热");
        RLock lock = redissonClient.getLock("zsc:user:preheat:lock");
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                for (Long userId : userIdList) {
                    // 缓存预热
                    String redisKey = String.format("zsc:user:recommend:%s", userId);
                    // 1. 获取推荐用户列表
                    Page<User> userPage = userService.recommendUsers(1, 20, request);
                    // 2. 缓存推荐用户列表
                    redisTemplate.opsForValue().set(redisKey, userPage, 120, TimeUnit.MINUTES);

                }
            } else {
                System.out.println("获取锁失败");
                return;
            }
        } catch (InterruptedException e) {
            System.out.println("获取锁异常");
            return;
        }finally {
            //只释放自己线程的锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }


    }
}