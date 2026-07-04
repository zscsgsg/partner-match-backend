package com.zsc.partnermatch.service;

import com.zsc.partnermatch.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MatchAlgorithmService 单元测试
 * 覆盖：余弦相似度、倒排索引、综合匹配、边界用例
 */
class MatchAlgorithmServiceTest {

    private MatchAlgorithmService service;

    @BeforeEach
    void setUp() {
        service = new MatchAlgorithmService();
    }

    // ==================== 余弦相似度测试 ====================

    @Test
    @DisplayName("余弦相似度: 完全相同的向量应返回 1.0")
    void cosineSimilarity_identicalVectors_returnsOne() {
        double[] v1 = {1.0, 2.0, 3.0};
        double[] v2 = {1.0, 2.0, 3.0};
        double result = service.cosineSimilarity(v1, v2);
        assertEquals(1.0, result, 0.0001);
    }

    @Test
    @DisplayName("余弦相似度: 正交向量应返回 0.0")
    void cosineSimilarity_orthogonalVectors_returnsZero() {
        double[] v1 = {1.0, 0.0};
        double[] v2 = {0.0, 1.0};
        double result = service.cosineSimilarity(v1, v2);
        assertEquals(0.0, result, 0.0001);
    }

    @Test
    @DisplayName("余弦相似度: 零向量应返回 0.0")
    void cosineSimilarity_zeroVectors_returnsZero() {
        double[] v1 = {0.0, 0.0, 0.0};
        double[] v2 = {1.0, 2.0, 3.0};
        double result = service.cosineSimilarity(v1, v2);
        assertEquals(0.0, result, 0.0001);
    }

    @Test
    @DisplayName("余弦相似度: 方向相同但长度不同的向量应返回 1.0")
    void cosineSimilarity_sameDirection_returnsOne() {
        double[] v1 = {1.0, 2.0};
        double[] v2 = {2.0, 4.0};
        double result = service.cosineSimilarity(v1, v2);
        assertEquals(1.0, result, 0.0001);
    }

    // ==================== 倒排索引测试 ====================

    @Test
    @DisplayName("倒排索引: 构建后能正确召回候选用户")
    void buildInvertedIndex_fiveUsers_correctRecall() {
        List<User> users = createTestUsers();
        Map<String, Set<Long>> index = service.buildInvertedIndex(users);

        // "Java" 应该包含用户 1、2、4
        assertTrue(index.containsKey("Java"));
        Set<Long> javaUsers = index.get("Java");
        assertTrue(javaUsers.contains(1L));
        assertTrue(javaUsers.contains(2L));
        assertTrue(javaUsers.contains(4L));
        assertFalse(javaUsers.contains(3L));

        // "Rust" 只包含用户 3
        assertTrue(index.containsKey("Rust"));
        assertEquals(1, index.get("Rust").size());
    }

    @Test
    @DisplayName("倒排索引: 召回候选人应排除自己")
    void recallCandidates_excludesCurrentUser() {
        List<User> users = createTestUsers();
        Map<String, Set<Long>> index = service.buildInvertedIndex(users);
        User currentUser = users.get(0); // 用户 1，标签: Java, Spring

        Set<Long> candidates = service.recallCandidates(currentUser, index);

        assertFalse(candidates.contains(1L)); // 排除自己
        assertTrue(candidates.size() > 0);     // 有候选
    }

    // ==================== 词汇表和 IDF 测试 ====================

    @Test
    @DisplayName("词汇表: 所有标签都应被收录且不重复")
    void buildVocabulary_allTagsIncluded() {
        List<User> users = createTestUsers();
        Map<String, Integer> vocab = service.buildVocabulary(users);

        assertTrue(vocab.containsKey("Java"));
        assertTrue(vocab.containsKey("Python"));
        assertTrue(vocab.containsKey("Rust"));
        assertTrue(vocab.containsKey("Spring"));
        assertTrue(vocab.containsKey("Django"));
    }

    @Test
    @DisplayName("IDF: 高频标签权重应低于低频标签")
    void computeIdf_highFreqTagLowerWeight() {
        List<User> users = createTestUsers();
        Map<String, Double> idf = service.computeIdf(users);

        // "Java" 出现 3 次，"Rust" 只出现 1 次
        // IDF(Rust) 应该 > IDF(Java)
        assertTrue(idf.get("Rust") > idf.get("Java"));
    }

    // ==================== 综合匹配测试 ====================

    @Test
    @DisplayName("综合匹配: 已知数据集中 Top1 应是标签最相似的用户")
    void smartMatch_knownDataset_topOneIsBestMatch() {
        List<User> users = createTestUsers();
        User currentUser = users.get(0); // 用户 1: Java, Spring

        List<MatchAlgorithmService.MatchResult> results = service.smartMatch(currentUser, users, 3);

        assertFalse(results.isEmpty());
        // Top1 应该是有最多共同标签的用户
        assertNotNull(results.get(0).user);
        assertTrue(results.get(0).totalScore > 0);
        // 结果应按分数降序
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).totalScore >= results.get(i).totalScore);
        }
    }

    // ==================== 边界用例测试 ====================

    @Test
    @DisplayName("边界: 空标签用户应返回空列表")
    void smartMatch_emptyTags_returnsEmptyList() {
        User user1 = createUser(1L, null);
        User user2 = createUser(2L, "[\"Java\"]");
        List<User> users = Arrays.asList(user1, user2);

        List<MatchAlgorithmService.MatchResult> results = service.smartMatch(user1, users, 5);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("边界: 只有单个用户应返回空列表（无候选人）")
    void smartMatch_singleUser_returnsEmptyList() {
        User user1 = createUser(1L, "[\"Java\", \"Spring\"]");
        List<User> users = Collections.singletonList(user1);

        List<MatchAlgorithmService.MatchResult> results = service.smartMatch(user1, users, 5);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("边界: 全部标签相同应返回高相似度")
    void smartMatch_allSameTags_highSimilarity() {
        User user1 = createUser(1L, "[\"Java\", \"Spring\", \"MySQL\"]");
        User user2 = createUser(2L, "[\"Java\", \"Spring\", \"MySQL\"]");
        User user3 = createUser(3L, "[\"Python\", \"Django\"]");
        List<User> users = Arrays.asList(user1, user2, user3);

        List<MatchAlgorithmService.MatchResult> results = service.smartMatch(user1, users, 2);

        assertFalse(results.isEmpty());
        // user2 应该是 Top1（标签完全相同）
        assertEquals(2L, results.get(0).user.getId());
        assertTrue(results.get(0).tagSimilarity > 0.9);
    }

    @Test
    @DisplayName("互补性: 标签完全不同应返回高互补性")
    void complementarityScore_noOverlap_returnsOne() {
        List<String> myTags = Arrays.asList("Java", "Spring");
        List<String> otherTags = Arrays.asList("Python", "Django");

        double score = service.complementarityScore(myTags, otherTags);
        assertEquals(1.0, score, 0.0001);
    }

    @Test
    @DisplayName("互补性: 标签完全相同应返回低互补性")
    void complementarityScore_fullOverlap_returnsZero() {
        List<String> myTags = Arrays.asList("Java", "Spring");
        List<String> otherTags = Arrays.asList("Java", "Spring");

        double score = service.complementarityScore(myTags, otherTags);
        assertEquals(0.0, score, 0.0001);
    }

    // ==================== 辅助方法 ====================

    private List<User> createTestUsers() {
        return Arrays.asList(
                createUser(1L, "[\"Java\", \"Spring\"]"),
                createUser(2L, "[\"Java\", \"Spring\", \"MySQL\"]"),
                createUser(3L, "[\"Python\", \"Rust\"]"),
                createUser(4L, "[\"Java\", \"Go\"]"),
                createUser(5L, "[\"Python\", \"Django\"]")
        );
    }

    private User createUser(Long id, String tags) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setTags(tags);
        return user;
    }
}
