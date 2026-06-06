package com.zsc.partnermatch.service;

import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.StrUtil;
import com.zsc.partnermatch.entity.User;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * AI 推荐解释服务 —— 基于 Spring AI Alibaba + 通义千问
 * 
 * 为匹配结果生成自然语言的"推荐理由"，提升推荐系统的可解释性。
 */
@Slf4j
@Service
public class MatchExplainService {

    @Autowired
    private ChatModel chatModel;
    @Autowired
    @Qualifier("aiCallSuccessCounter")
    private Counter aiCallSuccessCounter;
    @Autowired
    @Qualifier("aiCallFailCounter")
    private Counter aiCallFailCounter;

    /** AI 单次调用超时（秒） */
    private static final long AI_TIMEOUT_SECONDS = 3;

    /**
     * 为单对匹配生成推荐理由
     *
     * @param currentUser 当前用户
     * @param matchedUser 匹配到的用户
     * @param totalScore  综合匹配得分
     * @param tagSimilarity 标签相似度
     * @param commonTags  共同标签
     * @return AI 生成的推荐理由
     */
    public String generateExplanation(User currentUser, User matchedUser,
                                      double totalScore, double tagSimilarity,
                                      List<String> commonTags) {
        String myTags = formatTags(currentUser.getTags());
        String otherTags = formatTags(matchedUser.getTags());
        String commonTagsStr = commonTags != null && !commonTags.isEmpty()
                ? String.join("、", commonTags)
                : "无明显共同标签";
        String myProfile = StrUtil.isNotBlank(currentUser.getProfile())
                ? currentUser.getProfile() : "暂无简介";
        String otherProfile = StrUtil.isNotBlank(matchedUser.getProfile())
                ? matchedUser.getProfile() : "暂无简介";

        String prompt = String.format("""
                你是组队推荐助手。请用1-2句话简洁地解释为什么推荐这两个用户组队协作。
                要求：语气友好，突出共同点和互补性，不要超过80字。

                - 当前用户：昵称%s，擅长标签：%s，个人简介：%s
                - 匹配用户：昵称%s，擅长标签：%s，个人简介：%s
                - 共同标签：%s
                - 标签匹配度：%.0f%%

                请直接输出推荐理由：""",
                currentUser.getUsername(), myTags, myProfile,
                matchedUser.getUsername(), otherTags, otherProfile,
                commonTagsStr, tagSimilarity * 100);

        try {
            String result = chatModel.call(prompt);
            aiCallSuccessCounter.increment();
            return result;
        } catch (Exception e) {
            aiCallFailCounter.increment();
            // AI 调用失败时返回基于规则的回退理由
            return fallbackExplanation(currentUser.getUsername(),
                    matchedUser.getUsername(), commonTagsStr);
        }
    }

    /**
     * 为批量匹配结果填充推荐理由（并行调用 AI，单条超时 3s 回退到规则理由）
     */
    public void fillExplanations(User currentUser, List<MatchAlgorithmService.MatchResult> results) {
        if (results == null || results.isEmpty()) return;

        // 使用虚拟线程池并行调用 AI
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (MatchAlgorithmService.MatchResult result : results) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    result.explanation = generateExplanation(
                            currentUser, result.user,
                            result.totalScore, result.tagSimilarity,
                            result.commonTags
                    );
                }, executor)
                .orTimeout(AI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("[AI推荐理由] 超时或失败，使用规则回退: {}", ex.getMessage());
                    result.explanation = fallbackExplanation(
                            currentUser.getUsername(),
                            result.user.getUsername(),
                            result.commonTags != null && !result.commonTags.isEmpty()
                                    ? String.join("、", result.commonTags)
                                    : "无明显共同标签"
                    );
                    return null;
                });
                futures.add(future);
            }

            // 等待所有任务完成（最多等待 AI_TIMEOUT_SECONDS + 缓冲）
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(AI_TIMEOUT_SECONDS + 3, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.warn("[AI推荐理由] 批量任务超时，部分结果可能使用规则回退");
                        return null;
                    })
                    .join();
        }
    }

    /**
     * AI 调用失败时的回退解释（基于规则）
     */
    private String fallbackExplanation(String nameA, String nameB, String commonTags) {
        if (!"无明显共同标签".equals(commonTags)) {
            return String.format("你和%s都擅长%s，在技术上有共同语言，很适合一起协作。",
                    nameB, commonTags);
        }
        return String.format("%s的技能方向与你有差异，互补组队可以碰撞出不一样的火花。", nameB);
    }

    private String formatTags(String tagsJson) {
        if (StrUtil.isBlank(tagsJson)) {
            return "暂无";
        }
        try {
            List<String> tags = JSONUtil.toList(tagsJson, String.class);
            if (tags == null || tags.isEmpty()) return "暂无";
            // 最多取前 5 个标签
            return String.join("、", tags.stream().limit(5).toList());
        } catch (Exception e) {
            return "暂无";
        }
    }
}
