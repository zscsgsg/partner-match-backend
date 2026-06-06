package com.zsc.partnermatch.service;

import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.StrUtil;
import com.zsc.partnermatch.entity.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能匹配算法服务 —— 基于余弦相似度 + TF-IDF 加权 + 倒排索引召回
 * 
 * 核心优化：
 * 1. 标签向量化：构建全局词汇表，将标签转为加权向量（TF-IDF）
 * 2. 倒排索引召回：Map<tag, List<userId>>，避免 O(n²) 全量比较
 * 3. 余弦相似度精排：对候选集做精确相似度计算
 * 4. 多维度打分：标签相似度(70%) + 行为相似度(20%) + 互补性(10%)
 */
@Service
public class MatchAlgorithmService {

    // ==================== 标签向量化 ====================

    /**
     * 构建全局标签词汇表（标签 → 向量维度索引）
     */
    public Map<String, Integer> buildVocabulary(List<User> allUsers) {
        Map<String, Integer> vocab = new LinkedHashMap<>();
        int index = 0;
        for (User user : allUsers) {
            List<String> tags = parseTags(user.getTags());
            for (String tag : tags) {
                if (!vocab.containsKey(tag)) {
                    vocab.put(tag, index++);
                }
            }
        }
        return vocab;
    }

    /**
     * 计算 IDF 权重：IDF(t) = log(总用户数 / 包含该标签的用户数)
     */
    public Map<String, Double> computeIdf(List<User> allUsers) {
        Map<String, Integer> docFreq = new HashMap<>();
        Map<String, Double> idf = new HashMap<>();

        for (User user : allUsers) {
            List<String> tags = parseTags(user.getTags());
            Set<String> uniqueTags = new HashSet<>(tags);
            for (String tag : uniqueTags) {
                docFreq.merge(tag, 1, Integer::sum);
            }
        }

        int N = allUsers.size();
        for (Map.Entry<String, Integer> entry : docFreq.entrySet()) {
            // IDF = log(N / df)，加 1 平滑避免除零
            idf.put(entry.getKey(), Math.log((double) N / (entry.getValue() + 1)) + 1.0);
        }
        return idf;
    }

    /**
     * 将用户的标签列表转为 TF-IDF 加权的向量
     */
    public double[] userToVector(User user, Map<String, Integer> vocab, Map<String, Double> idf) {
        double[] vector = new double[vocab.size()];
        List<String> tags = parseTags(user.getTags());
        if (tags.isEmpty()) {
            return vector;
        }

        // 计算 TF（词频）
        Map<String, Double> tf = new HashMap<>();
        for (String tag : tags) {
            tf.merge(tag, 1.0, Double::sum);
        }
        // 归一化 TF
        int totalTags = tags.size();
        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            double tfNorm = entry.getValue() / totalTags;
            Integer idx = vocab.get(entry.getKey());
            if (idx != null) {
                double idfVal = idf.getOrDefault(entry.getKey(), 1.0);
                vector[idx] = tfNorm * idfVal;
            }
        }
        return vector;
    }

    // ==================== 倒排索引 ====================

    /**
     * 构建倒排索引：标签 → 拥有该标签的用户 ID 列表
     */
    public Map<String, Set<Long>> buildInvertedIndex(List<User> allUsers) {
        Map<String, Set<Long>> invertedIndex = new HashMap<>();
        for (User user : allUsers) {
            List<String> tags = parseTags(user.getTags());
            for (String tag : tags) {
                invertedIndex.computeIfAbsent(tag, k -> new HashSet<>()).add(user.getId());
            }
        }
        return invertedIndex;
    }

    /**
     * 通过倒排索引快速召回候选用户（与当前用户至少共享一个标签的用户）
     */
    public Set<Long> recallCandidates(User currentUser, Map<String, Set<Long>> invertedIndex) {
        Set<Long> candidates = new HashSet<>();
        List<String> myTags = parseTags(currentUser.getTags());
        for (String tag : myTags) {
            Set<Long> userIds = invertedIndex.get(tag);
            if (userIds != null) {
                candidates.addAll(userIds);
            }
        }
        candidates.remove(currentUser.getId()); // 排除自己
        return candidates;
    }

    // ==================== 余弦相似度 ====================

    /**
     * 计算两个向量的余弦相似度
     */
    public double cosineSimilarity(double[] v1, double[] v2) {
        if (v1.length != v2.length || v1.length == 0) {
            return 0.0;
        }
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        if (denominator == 0.0) {
            return 0.0;
        }
        return dotProduct / denominator;
    }

    // ==================== 多维度打分 ====================

    /**
     * 行为相似度：基于创建队伍数、加入队伍数的相似程度
     */
    public double behavioralSimilarity(int myCreated, int myJoined, int otherCreated, int otherJoined) {
        double[] myVec = {myCreated, myJoined};
        double[] otherVec = {otherCreated, otherJoined};
        return cosineSimilarity(myVec, otherVec);
    }

    /**
     * 互补性得分：标签重叠越少，互补性越高（鼓励异质组队）
     */
    public double complementarityScore(List<String> myTags, List<String> otherTags) {
        Set<String> mySet = new HashSet<>(myTags);
        Set<String> otherSet = new HashSet<>(otherTags);

        // 计算共有标签和独有标签
        Set<String> intersection = new HashSet<>(mySet);
        intersection.retainAll(otherSet);

        Set<String> union = new HashSet<>(mySet);
        union.addAll(otherSet);

        if (union.isEmpty()) return 0.0;

        // Jaccard 相似度
        double jaccard = (double) intersection.size() / union.size();
        // 互补性 = 1 - Jaccard（越不像越互补）
        return 1.0 - jaccard;
    }

    // ==================== 综合匹配入口 ====================

    /**
     * 多维度智能匹配
     *
     * @param currentUser  当前用户
     * @param allUsers     所有用户
     * @param topN         返回 topN 个最匹配的结果
     * @return 匹配结果列表（按综合得分降序）
     */
    public List<MatchResult> smartMatch(User currentUser, List<User> allUsers, int topN) {
        return smartMatch(currentUser, allUsers, topN, null);
    }

    /**
     * 多维度智能匹配（支持用户画像）
     *
     * @param currentUser  当前用户
     * @param allUsers     所有用户
     * @param topN         返回 topN 个最匹配的结果
     * @param profileMap   用户画像映射（userId -> UserProfile），可为 null
     * @return 匹配结果列表（按综合得分降序）
     */
    public List<MatchResult> smartMatch(User currentUser, List<User> allUsers, int topN,
                                        Map<Long, com.zsc.partnermatch.entity.UserProfile> profileMap) {
        if (allUsers == null || allUsers.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 构建词汇表和 IDF
        Map<String, Integer> vocab = buildVocabulary(allUsers);
        Map<String, Double> idf = computeIdf(allUsers);

        // 2. 构建倒排索引
        Map<String, Set<Long>> invertedIndex = buildInvertedIndex(allUsers);

        // 3. 召回候选集（只计算与当前用户有共同标签的用户）
        Set<Long> candidateIds = recallCandidates(currentUser, invertedIndex);

        // 4. 构建 ID → User 映射，方便快速查找
        Map<Long, User> userIdToUser = allUsers.stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        // 5. 当前用户向量
        double[] myVector = userToVector(currentUser, vocab, idf);
        List<String> myTags = parseTags(currentUser.getTags());

        // 6. 对候选集做精确余弦相似度 + 多维度打分
        List<MatchResult> results = new ArrayList<>();
        for (Long candidateId : candidateIds) {
            User candidate = userIdToUser.get(candidateId);
            if (candidate == null) continue;

            double[] otherVector = userToVector(candidate, vocab, idf);
            List<String> otherTags = parseTags(candidate.getTags());

            // 标签相似度
            double tagSim = cosineSimilarity(myVector, otherVector);

            // 行为相似度（使用真实画像数据，若无则默认 0）
            double behaviorSim;
            if (profileMap != null) {
                com.zsc.partnermatch.entity.UserProfile myProfile = profileMap.get(currentUser.getId());
                com.zsc.partnermatch.entity.UserProfile otherProfile = profileMap.get(candidateId);
                int myCreated = myProfile != null ? myProfile.getTotalTeamsCreated() : 0;
                int myJoined = myProfile != null ? myProfile.getTotalTeamsJoined() : 0;
                int otherCreated = otherProfile != null ? otherProfile.getTotalTeamsCreated() : 0;
                int otherJoined = otherProfile != null ? otherProfile.getTotalTeamsJoined() : 0;
                behaviorSim = behavioralSimilarity(myCreated, myJoined, otherCreated, otherJoined);
            } else {
                behaviorSim = behavioralSimilarity(0, 0, 0, 0);
            }

            // 协作评分维度
            double cooperationSim = 0.0;
            if (profileMap != null) {
                com.zsc.partnermatch.entity.UserProfile otherProfile = profileMap.get(candidateId);
                if (otherProfile != null && otherProfile.getCooperationScore() != null) {
                    // 归一化到 0~1（评分范围 1.0~5.0）
                    cooperationSim = (otherProfile.getCooperationScore() - 1.0) / 4.0;
                }
            }

            // 互补性
            double complement = complementarityScore(myTags, otherTags);

            // 综合得分（权重可调：标签 65% + 行为 20% + 互补 10% + 协作 5%）
            double totalScore = tagSim * 0.65 + behaviorSim * 0.20 + complement * 0.10 + cooperationSim * 0.05;

            // 找到共同标签
            Set<String> commonTags = new HashSet<>(myTags);
            commonTags.retainAll(new HashSet<>(otherTags));

            results.add(new MatchResult(
                    candidate,
                    totalScore,
                    tagSim,
                    complement,
                    new ArrayList<>(commonTags)
            ));
        }

        // 7. 按综合得分降序排序，返回 topN
        results.sort((a, b) -> Double.compare(b.totalScore, a.totalScore));
        return results.stream().limit(topN).collect(Collectors.toList());
    }

    // ==================== 工具方法 ====================

    private List<String> parseTags(String tagsJson) {
        if (StrUtil.isBlank(tagsJson)) {
            return Collections.emptyList();
        }
        try {
            return Optional.ofNullable(JSONUtil.toList(tagsJson, String.class))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // ==================== 匹配结果内部类 ====================

    /**
     * 匹配结果，包含匹配用户和各项得分
     */
    public static class MatchResult {
        public User user;                 // 匹配的用户
        public double totalScore;         // 综合得分
        public double tagSimilarity;      // 标签相似度
        public double complementScore;    // 互补性得分
        public List<String> commonTags;   // 共同标签
        public String explanation;        // AI 推荐理由（由 AI 服务填充）

        public MatchResult(User user, double totalScore, double tagSimilarity,
                           double complementScore, List<String> commonTags) {
            this.user = user;
            this.totalScore = totalScore;
            this.tagSimilarity = tagSimilarity;
            this.complementScore = complementScore;
            this.commonTags = commonTags;
        }
    }
}
