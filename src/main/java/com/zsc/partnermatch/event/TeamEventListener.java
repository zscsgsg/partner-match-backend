package com.zsc.partnermatch.event;

import com.zsc.partnermatch.service.IUserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 队伍事件监听器
 * <p>
 * 监听 TeamEvent，自动触发画像重算和缓存失效，
 * 与 TeamServiceImpl 完全解耦，符合开闭原则。
 */
@Component
@Slf4j
public class TeamEventListener {

    @Autowired
    private IUserProfileService userProfileService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 画像重算：队伍变更后重新计算用户画像（组队次数、协作评分等）
     */
    @EventListener
    public void onTeamChangeRecalculateProfile(TeamEvent event) {
        log.info("[事件-画像重算] 用户 {} {} 队伍 {}", event.getUserId(), event.getEventType(), event.getTeamId());
        try {
            userProfileService.calculateProfile(event.getUserId());
            log.info("[事件-画像重算] 用户 {} 画像更新完成", event.getUserId());
        } catch (Exception e) {
            log.warn("[事件-画像重算] 用户 {} 画像更新失败: {}", event.getUserId(), e.getMessage());
        }
    }

    /**
     * 缓存失效：异步清除该用户的推荐缓存和匹配缓存
     * 使用 @Async 避免阻塞主业务线程
     */
    @Async
    @EventListener
    public void onTeamChangeInvalidateCache(TeamEvent event) {
        log.info("[事件-缓存失效] 清除用户 {} 的缓存", event.getUserId());
        try {
            // 清除推荐用户缓存
            String recommendPattern = "zsc:user:recommend:" + event.getUserId() + "*";
            Set<String> recommendKeys = stringRedisTemplate.keys(recommendPattern);
            if (recommendKeys != null && !recommendKeys.isEmpty()) {
                stringRedisTemplate.delete(recommendKeys);
                log.info("[事件-缓存失效] 清除推荐缓存 {} 个 key", recommendKeys.size());
            }

            // 清除智能匹配缓存
            String matchPattern = "zsc:user:smartMatch:" + event.getUserId() + "*";
            Set<String> matchKeys = stringRedisTemplate.keys(matchPattern);
            if (matchKeys != null && !matchKeys.isEmpty()) {
                stringRedisTemplate.delete(matchKeys);
                log.info("[事件-缓存失效] 清除匹配缓存 {} 个 key", matchKeys.size());
            }
        } catch (Exception e) {
            log.warn("[事件-缓存失效] 用户 {} 缓存清除失败: {}", event.getUserId(), e.getMessage());
        }
    }
}
