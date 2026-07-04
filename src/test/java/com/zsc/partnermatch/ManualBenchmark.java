package com.zsc.partnermatch;

import cn.hutool.json.JSONUtil;
import com.zsc.partnermatch.entity.User;
import com.zsc.partnermatch.service.MatchAlgorithmService;
import com.zsc.partnermatch.utils.AigorithmUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 手动压测：旧算法（编辑距离 O(n²)）vs 新算法（余弦相似度 + 倒排索引）
 * 
 * 场景1 - 单用户匹配：为一用户找最佳匹配
 * 场景2 - 全量匹配：为所有用户找最佳匹配（模拟生产环境批量推荐）
 * 
 * 直接运行 main 方法即可
 */
public class ManualBenchmark {

    static final int TAG_POOL_SIZE = 500;
    static final int TAGS_PER_USER_MIN = 3;
    static final int TAGS_PER_USER_MAX = 8;
    static final int WARMUP_ROUNDS = 2;
    static final int TEST_ROUNDS = 3;
    static final int TOP_N = 10;

    public static void main(String[] args) {
        String[] tagPool = IntStream.range(0, TAG_POOL_SIZE)
                .mapToObj(i -> String.format("skill_%03d", i))
                .toArray(String[]::new);

        MatchAlgorithmService matchService = new MatchAlgorithmService();

        System.out.println("=".repeat(80));
        System.out.println("伙伴匹配系统 — 算法性能对比测试");
        System.out.println("场景：500 标签池 / 每用户 3~8 个标签（稀疏分布）");
        System.out.println("=".repeat(80));

        // ==================== 场景一：单用户匹配 ====================
        System.out.println("\n" + "=".repeat(80));
        System.out.println("场景一：单用户匹配（为1个用户找最佳匹配）");
        System.out.println("=".repeat(80));

        for (int userCount : new int[]{2000, 5000, 10000}) {
            System.out.println("\n>>> 用户数: " + userCount);
            System.out.println("-".repeat(60));

            Random random = new Random(42);
            List<User> users = generateUsers(userCount, tagPool, random);
            User currentUser = buildCurrentUser(tagPool);

            double oldMs = benchmarkSingleOld(currentUser, users);
            double newMs = benchmarkSingleNew(matchService, currentUser, users);
            printResult("旧算法（编辑距离）", oldMs, "新算法（余弦+倒排）", newMs);
        }

        // ==================== 场景二：全量匹配 ====================
        System.out.println("\n" + "=".repeat(80));
        System.out.println("场景二：全量匹配（为所有用户找最佳匹配 — O(N²) vs 倒排索引召回）");
        System.out.println("=".repeat(80));

        for (int userCount : new int[]{500, 1000, 2000}) {
            System.out.println("\n>>> 用户数: " + userCount + "（共 " + (userCount * userCount) + " 对比较）");
            System.out.println("-".repeat(60));

            Random random = new Random(42);
            List<User> users = generateUsers(userCount, tagPool, random);

            double oldMs = benchmarkAllPairsOld(users);
            double newMs = benchmarkAllPairsNew(matchService, users);
            printResult("旧算法（全量 O(N²)）", oldMs, "新算法（倒排 O(N log N)）", newMs);
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("测试完成。");
    }

    // ============ 场景一：单用户匹配 ============

    static double benchmarkSingleOld(User currentUser, List<User> users) {
        long total = 0;
        for (int r = 0; r < WARMUP_ROUNDS + TEST_ROUNDS; r++) {
            long start = System.nanoTime();
            oldEditDistanceMatch(currentUser, users, TOP_N);
            long elapsed = System.nanoTime() - start;
            if (r >= WARMUP_ROUNDS) total += elapsed;
        }
        return total / (double) TEST_ROUNDS / 1_000_000.0;
    }

    static double benchmarkSingleNew(MatchAlgorithmService svc, User currentUser, List<User> users) {
        long total = 0;
        for (int r = 0; r < WARMUP_ROUNDS + TEST_ROUNDS; r++) {
            long start = System.nanoTime();
            svc.smartMatch(currentUser, users, TOP_N);
            long elapsed = System.nanoTime() - start;
            if (r >= WARMUP_ROUNDS) total += elapsed;
        }
        return total / (double) TEST_ROUNDS / 1_000_000.0;
    }

    // ============ 场景二：全量匹配 ============

    /** 旧算法：为每个用户做全量 O(N²) 扫描 */
    static double benchmarkAllPairsOld(List<User> users) {
        long total = 0;
        for (int r = 0; r < WARMUP_ROUNDS + TEST_ROUNDS; r++) {
            long start = System.nanoTime();
            for (User u : users) {
                oldEditDistanceMatch(u, users, TOP_N);
            }
            long elapsed = System.nanoTime() - start;
            if (r >= WARMUP_ROUNDS) total += elapsed;
        }
        return total / (double) TEST_ROUNDS / 1_000_000.0;
    }

    /** 新算法：一次建索引 + 倒排查召，为每个用户批量匹配 */
    static double benchmarkAllPairsNew(MatchAlgorithmService svc, List<User> users) {
        long total = 0;
        for (int r = 0; r < WARMUP_ROUNDS + TEST_ROUNDS; r++) {
            long start = System.nanoTime();
            // 一次性构建词汇表 + IDF + 倒排索引
            Map<String, Integer> vocab = svc.buildVocabulary(users);
            Map<String, Double> idf = svc.computeIdf(users);
            Map<String, Set<Long>> invertedIndex = svc.buildInvertedIndex(users);
            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

            for (User u : users) {
                // 倒排查召
                Set<Long> candidateIds = svc.recallCandidates(u, invertedIndex);
                double[] myVec = svc.userToVector(u, vocab, idf);
                List<String> myTags = JSONUtil.toList(u.getTags(), String.class);

                List<MatchAlgorithmService.MatchResult> results = new ArrayList<>();
                for (Long cid : candidateIds) {
                    User candidate = userMap.get(cid);
                    if (candidate == null) continue;
                    double[] otherVec = svc.userToVector(candidate, vocab, idf);
                    double sim = svc.cosineSimilarity(myVec, otherVec);
                    Set<String> common = new HashSet<>(myTags);
                    common.retainAll(new HashSet<>(JSONUtil.toList(candidate.getTags(), String.class)));
                    results.add(new MatchAlgorithmService.MatchResult(candidate, sim, sim, 0, new ArrayList<>(common)));
                }
                results.sort((a, b) -> Double.compare(b.totalScore, a.totalScore));
            }
            long elapsed = System.nanoTime() - start;
            if (r >= WARMUP_ROUNDS) total += elapsed;
        }
        return total / (double) TEST_ROUNDS / 1_000_000.0;
    }

    // ============ 工具方法 ============

    static List<User> generateUsers(int count, String[] tagPool, Random random) {
        return IntStream.range(1, count + 1).mapToObj(i -> {
            User u = new User();
            u.setId((long) i);
            int tagNum = TAGS_PER_USER_MIN + random.nextInt(TAGS_PER_USER_MAX - TAGS_PER_USER_MIN + 1);
            Set<String> tagSet = new HashSet<>();
            while (tagSet.size() < tagNum) {
                tagSet.add(tagPool[random.nextInt(TAG_POOL_SIZE)]);
            }
            u.setTags(JSONUtil.toJsonStr(new ArrayList<>(tagSet)));
            return u;
        }).collect(Collectors.toList());
    }

    static User buildCurrentUser(String[] tagPool) {
        User u = new User();
        u.setId(0L);
        u.setTags(JSONUtil.toJsonStr(Arrays.asList(tagPool[0], tagPool[1], tagPool[2])));
        return u;
    }

    static void printResult(String oldLabel, double oldMs, String newLabel, double newMs) {
        double speedup = oldMs / newMs;
        String winner = speedup > 1.0 ? "✅ 新算法胜出" : "旧算法胜出";
        System.out.printf("  %-22s: %8.3f ms%n", oldLabel, oldMs);
        System.out.printf("  %-22s: %8.3f ms%n", newLabel, newMs);
        System.out.printf("  提升倍数             : %.2fx  %s%n", speedup, winner);
    }

    /** 旧算法：编辑距离（全量扫描） */
    static List<User> oldEditDistanceMatch(User currentUser, List<User> allUsers, int topN) {
        List<String> myTags = JSONUtil.toList(currentUser.getTags(), String.class);
        List<long[]> scored = new ArrayList<>();
        for (User u : allUsers) {
            if (u.getId().equals(currentUser.getId())) continue;
            List<String> userTags = JSONUtil.toList(u.getTags(), String.class);
            long dist = AigorithmUtils.minDistance(myTags, userTags);
            scored.add(new long[]{u.getId(), dist});
        }
        scored.sort(Comparator.comparingLong(a -> a[1]));
        Set<Long> topIds = scored.stream().limit(topN).map(a -> a[0]).collect(Collectors.toSet());
        return allUsers.stream().filter(u -> topIds.contains(u.getId())).collect(Collectors.toList());
    }
}
