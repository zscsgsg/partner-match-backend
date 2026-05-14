/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : localhost:3306
 Source Schema         : course

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 02/06/2026 16:38:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tagName` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签名称',
  `userId` bigint NULL DEFAULT NULL COMMENT '用户id',
  `parentId` bigint NULL DEFAULT NULL COMMENT '父标签id ',
  `isParent` tinyint NULL DEFAULT NULL COMMENT '0-不是父标签 1-是',
  `createTime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `isDelete` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_unique_tagName`(`tagName`) USING BTREE,
  INDEX `idx_unique_userId`(`userId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag
-- ----------------------------
INSERT INTO `tag` VALUES (1, '技术', NULL, NULL, 1, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (2, '前端', 3, 1, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (3, '后端', 4, 1, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (4, 'Java', 1, 3, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (5, 'Python', 2, 3, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (6, 'Go', 5, 3, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (7, '运动', 8, NULL, 1, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (8, '篮球', 1, 7, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (9, '足球', 2, 7, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (10, '游泳', 4, 7, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (11, '游戏', NULL, NULL, 1, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (12, '王者荣耀', 3, 11, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (13, 'LOL', 6, 11, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (14, '音乐', 2, NULL, 1, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (15, '吉他', 6, 14, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (16, '钢琴', 7, 14, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (17, '阅读', 3, NULL, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (18, '旅行', 5, NULL, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (19, '摄影', 1, NULL, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);
INSERT INTO `tag` VALUES (20, '电影', 4, NULL, 0, '2026-04-24 14:47:11', '2026-04-24 14:47:11', 0);

-- ----------------------------
-- Table structure for team
-- ----------------------------
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '队伍名称',
  `description` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `maxNum` int NOT NULL DEFAULT 1 COMMENT '最大人数',
  `expireTime` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `userId` bigint NOT NULL COMMENT '创建人 id（队长）',
  `status` int NOT NULL DEFAULT 0 COMMENT '0 - 公开，1 - 私有，2 - 加密',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除（逻辑删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '队伍' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of team
-- ----------------------------
INSERT INTO `team` VALUES (1, 'Java王者', '来Java大佬一起上分', 5, '2026-09-11 07:59:59', 1, 1, '', '2026-04-29 17:00:47', '2026-05-01 16:36:58', 0);
INSERT INTO `team` VALUES (2, '篮球周末约战', '每周六下午篮球', 10, '2026-12-31 23:59:59', 2, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (3, '代码自习室', '一起刷题学习', 4, '2026-05-30 23:59:59', 3, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (4, '王者荣耀车队', '冲王者段位', 5, NULL, 4, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (5, '游泳爱好者', '夏天游泳约伴', 8, '2026-08-01 23:59:59', 5, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (6, '私密技术群', '内部交流，不对外', 3, NULL, 6, 1, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (7, '加密音乐组', '需要密码进入', 6, NULL, 7, 2, 'music123', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (8, '已过期队伍', '这个队伍已经过期', 4, '2025-01-01 00:00:00', 8, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (9, '满员队伍', '人已满，不能加入', 2, NULL, 9, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (10, '旅行摄影团', '边走边拍', 6, '2026-10-01 23:59:59', 10, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (11, 'Go语言学习组', 'Gopher集合', 5, NULL, 11, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (12, '前端模拟面试', '互相模拟面试', 4, '2026-06-15 23:59:59', 12, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (13, '跑步打卡群', '每天5公里', 20, NULL, 13, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (14, '考研互助组', '考研资料共享', 10, '2026-12-01 23:59:59', 14, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (15, '配音练习室', '配音爱好者', 7, NULL, 15, 0, NULL, '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `team` VALUES (16, '周小队', '加密测试队伍', 4, '2026-09-01 23:59:59', 1, 2, '123456', '2026-04-30 18:30:49', '2026-05-01 20:20:43', 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `userAccount` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户账号',
  `avatarUrl` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别',
  `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `userStatus` int NULL DEFAULT 0 COMMENT '状态 0-正常 1-不正常',
  `createTime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `isDelete` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  `role` int NULL DEFAULT 0 COMMENT '用户角色 0-普通用户 1-管理员',
  `planetCode` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '星球编号',
  `tags` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签列表',
  `profile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '周书超', 'zhoushuchao', 'https://picsum.photos/200/200?random=1', 1, '317bc264b9ca847c9de29c0b953f1157', '12345678900', 'zhoushuchao@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["Java","篮球","摄影"]', '热爱编程和运动的全栈开发者');
INSERT INTO `user` VALUES (2, '李四', 'lisi', 'https://picsum.photos/200/200?random=2', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000002', 'lisi@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["Python","足球","音乐"]', 'Python爱好者和音乐发烧友');
INSERT INTO `user` VALUES (3, '王芳', 'wangfang', 'https://picsum.photos/200/200?random=3', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000003', 'wangfang@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["前端","王者荣耀","阅读"]', '前端开发工程师，喜欢阅读');
INSERT INTO `user` VALUES (4, '赵磊', 'zhaolei', 'https://picsum.photos/200/200?random=4', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000004', 'zhaolei@example.com', 1, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["后端","游泳","电影"]', '后端开发，喜欢游泳和看电影');
INSERT INTO `user` VALUES (5, '陈静', 'chenjing', 'https://picsum.photos/200/200?random=5', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000005', 'chenjing@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["Go","篮球","旅行"]', 'Go语言爱好者和旅行达人');
INSERT INTO `user` VALUES (6, '周强', 'zhouqiang', 'https://picsum.photos/200/200?random=6', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000006', 'zhouqiang@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["Java","LOL","吉他"]', 'Java开发+LOL上分+吉他弹唱');
INSERT INTO `user` VALUES (7, '吴迪', 'wudi', 'https://picsum.photos/200/200?random=7', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000007', 'wudi@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["Python","钢琴","摄影"]', 'Python+钢琴+摄影三修选手');
INSERT INTO `user` VALUES (8, '郑爽', 'zhengshuang', 'https://picsum.photos/200/200?random=8', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000008', 'zhengshuang@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["前端","王者荣耀","运动"]', '前端开发、王者荣耀、运动健身');
INSERT INTO `user` VALUES (9, '孙阳', 'sunyang', 'https://picsum.photos/200/200?random=9', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000009', 'sunyang@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["后端","足球","阅读"]', '后端开发和足球爱好者，书虫一枚');
INSERT INTO `user` VALUES (10, '林娜', 'linna', 'https://picsum.photos/200/200?random=10', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000010', 'linna@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["Java","篮球","音乐"]', 'Java开发，喜欢打篮球听音乐');
INSERT INTO `user` VALUES (11, '郭峰', 'guofeng', 'https://picsum.photos/200/200?random=11', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000011', 'guofeng@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["Go","游泳","LOL"]', 'Go开发，游泳健身，LOL电竞');
INSERT INTO `user` VALUES (12, '唐雅', 'tangya', 'https://picsum.photos/200/200?random=12', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000012', 'tangya@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["Python","吉他","电影"]', 'Python开发，弹吉他，看电影');
INSERT INTO `user` VALUES (13, 'admin', 'admin', 'https://picsum.photos/200/200?random=13', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000888', 'admin@example.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 1, NULL, '["Java","王者荣耀","旅行"]', '系统管理员，热爱技术和生活');
INSERT INTO `user` VALUES (14, '测试用户1', 'test1', 'https://picsum.photos/200/200?random=14', 1, '317bc264b9ca847c9de29c0b953f1157', '13900000001', 'test1@test.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["前端","篮球","钢琴"]', '测试账号一号');
INSERT INTO `user` VALUES (15, '测试用户2', 'test2', 'https://picsum.photos/200/200?random=15', 0, '317bc264b9ca847c9de29c0b953f1157', '13900000002', 'test2@test.com', 0, '2026-04-22 16:59:49', '2026-04-22 16:59:49', 0, 0, NULL, '["后端","足球","摄影"]', '测试账号二号');
INSERT INTO `user` VALUES (16, '丁建凯', 'dingjiankai', NULL, NULL, '4e602d8e4ef62738f34305ad4b28d858', NULL, NULL, 0, '2026-04-22 17:08:20', '2026-04-22 17:08:20', 0, 0, '9', '["Java","运动","阅读"]', '喜欢运动和阅读的Java开发者');
INSERT INTO `user` VALUES (17, '李明', 'liming', 'https://picsum.photos/200/200?random=17', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000017', 'liming@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P001', '["Java","篮球","王者荣耀"]', 'Java全栈、篮球、王者荣耀');
INSERT INTO `user` VALUES (18, '王红', 'wanghong', 'https://picsum.photos/200/200?random=18', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000018', 'wanghong@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P002', '["Python","足球","LOL"]', 'Python开发，爱踢足球和LOL');
INSERT INTO `user` VALUES (19, '张伟', 'zhangwei', 'https://picsum.photos/200/200?random=19', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000019', 'zhangwei@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P003', '["前端","游泳","吉他"]', '前端开发，游泳和吉他爱好者');
INSERT INTO `user` VALUES (20, '刘丽', 'liuli', 'https://picsum.photos/200/200?random=20', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000020', 'liuli@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P004', '["Go","篮球","钢琴"]', 'Go开发，篮球+钢琴');
INSERT INTO `user` VALUES (21, '陈浩', 'chenhao', 'https://picsum.photos/200/200?random=21', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000021', 'chenhao@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P005', '["Java","王者荣耀","摄影"]', 'Java+王者+摄影');
INSERT INTO `user` VALUES (22, '赵敏', 'zhaomin', 'https://picsum.photos/200/200?random=22', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000022', 'zhaomin@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P006', '["后端","LOL","旅行"]', '后端开发，LOL和旅行达人');
INSERT INTO `user` VALUES (23, '孙涛', 'suntao', 'https://picsum.photos/200/200?random=23', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000023', 'suntao@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P007', '["Python","足球","电影"]', 'Python+足球+电影');
INSERT INTO `user` VALUES (24, '周娟', 'zhoujuan', 'https://picsum.photos/200/200?random=24', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000024', 'zhoujuan@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P008', '["前端","篮球","阅读"]', '前端开发，篮球+阅读');
INSERT INTO `user` VALUES (25, '吴刚', 'wugang', 'https://picsum.photos/200/200?random=25', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000025', 'wugang@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P009', '["Go","游泳","音乐"]', 'Go+游泳+音乐');
INSERT INTO `user` VALUES (26, '郑爽', 'zhengshuang2', 'https://picsum.photos/200/200?random=26', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000026', 'zhengshuang2@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P010', '["Java","LOL","吉他"]', 'Java开发，LOL+吉他');
INSERT INTO `user` VALUES (27, '刘洋', 'liuyang', 'https://picsum.photos/200/200?random=27', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000027', 'liuyang@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P011', '["Python","篮球","钢琴"]', 'Python+篮球+钢琴');
INSERT INTO `user` VALUES (28, '黄娟', 'huangjuan', 'https://picsum.photos/200/200?random=28', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000028', 'huangjuan@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P012', '["后端","王者荣耀","摄影"]', '后端开发+王者+摄影');
INSERT INTO `user` VALUES (29, '徐强', 'xuqiang', 'https://picsum.photos/200/200?random=29', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000029', 'xuqiang@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P013', '["前端","足球","旅行"]', '前端开发+足球+旅行');
INSERT INTO `user` VALUES (30, '胡静', 'hujing', 'https://picsum.photos/200/200?random=30', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000030', 'hujing@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P014', '["Go","LOL","电影"]', 'Go+LOL+电影');
INSERT INTO `user` VALUES (31, '林晨', 'linchen', 'https://picsum.photos/200/200?random=31', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000031', 'linchen@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P015', '["Java","篮球","阅读"]', 'Java+篮球+阅读');
INSERT INTO `user` VALUES (32, '郭霞', 'guoxia', 'https://picsum.photos/200/200?random=32', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000032', 'guoxia@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P016', '["Python","游泳","音乐"]', 'Python+游泳+音乐');
INSERT INTO `user` VALUES (33, '唐龙', 'tanglong', 'https://picsum.photos/200/200?random=33', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000033', 'tanglong@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P017', '["前端","篮球","吉他"]', '前端开发+篮球+吉他');
INSERT INTO `user` VALUES (34, '彭丽', 'pengli', 'https://picsum.photos/200/200?random=34', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000034', 'pengli@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P018', '["后端","王者荣耀","钢琴"]', '后端开发+王者荣耀+钢琴');
INSERT INTO `user` VALUES (35, '沈飞', 'shenfei', 'https://picsum.photos/200/200?random=35', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000035', 'shenfei@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P019', '["Go","足球","摄影"]', 'Go+足球+摄影');
INSERT INTO `user` VALUES (36, '宋佳', 'songjia', 'https://picsum.photos/200/200?random=36', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000036', 'songjia@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P020', '["Java","LOL","旅行"]', 'Java+LOL+旅行');
INSERT INTO `user` VALUES (37, '蔡康', 'caikang', 'https://picsum.photos/200/200?random=37', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000037', 'caikang@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P021', '["Python","篮球","电影"]', 'Python+篮球+电影');
INSERT INTO `user` VALUES (38, '魏敏', 'weimin', 'https://picsum.photos/200/200?random=38', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000038', 'weimin@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P022', '["前端","游泳","阅读"]', '前端开发，喜欢游泳和阅读');
INSERT INTO `user` VALUES (39, '蒋涛', 'jiangtao', 'https://picsum.photos/200/200?random=39', 1, '317bc264b9ca847c9de29c0b953f1157', '13800000039', 'jiangtao@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P023', '["后端","王者荣耀","音乐"]', '后端开发，喜欢王者荣耀和音乐');
INSERT INTO `user` VALUES (40, '丁宁', 'dingning', 'https://picsum.photos/200/200?random=40', 0, '317bc264b9ca847c9de29c0b953f1157', '13800000040', 'dingning@example.com', 0, '2026-04-24 14:47:46', '2026-04-24 14:47:46', 0, 0, 'P024', '["Java","足球","吉他"]', 'Java开发，喜欢足球和吉他');

-- ----------------------------
-- Table structure for user_team
-- ----------------------------
DROP TABLE IF EXISTS `user_team`;
CREATE TABLE `user_team`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userId` bigint NOT NULL COMMENT '用户 id',
  `teamId` bigint NOT NULL COMMENT '队伍 id',
  `joinTime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `createTime` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NULL DEFAULT 0 COMMENT '是否删除（逻辑删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 76 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_team
-- ----------------------------
-- 队伍1: Java王者 (私有, 队长:用户1, maxNum=5) - 成员: 1,6,17,21,31
INSERT INTO `user_team` VALUES (1, 1, 1, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (2, 6, 1, '2026-04-29 17:30:00', '2026-04-29 17:30:00', '2026-04-29 17:30:00', 0);
INSERT INTO `user_team` VALUES (3, 17, 1, '2026-04-30 10:00:00', '2026-04-30 10:00:00', '2026-04-30 10:00:00', 0);
INSERT INTO `user_team` VALUES (4, 21, 1, '2026-05-01 14:00:00', '2026-05-01 14:00:00', '2026-05-01 14:00:00', 0);
INSERT INTO `user_team` VALUES (5, 31, 1, '2026-05-02 09:00:00', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0);
-- 队伍2: 篮球周末约战 (公开, 队长:用户2, maxNum=10) - 成员: 2,1,5,10,17,20,24,27,33,37
INSERT INTO `user_team` VALUES (6, 2, 2, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (7, 1, 2, '2026-04-30 08:00:00', '2026-04-30 08:00:00', '2026-04-30 08:00:00', 0);
INSERT INTO `user_team` VALUES (8, 5, 2, '2026-04-30 09:00:00', '2026-04-30 09:00:00', '2026-04-30 09:00:00', 0);
INSERT INTO `user_team` VALUES (9, 10, 2, '2026-05-01 10:00:00', '2026-05-01 10:00:00', '2026-05-01 10:00:00', 0);
INSERT INTO `user_team` VALUES (10, 17, 2, '2026-05-01 11:00:00', '2026-05-01 11:00:00', '2026-05-01 11:00:00', 0);
INSERT INTO `user_team` VALUES (11, 20, 2, '2026-05-02 10:00:00', '2026-05-02 10:00:00', '2026-05-02 10:00:00', 0);
INSERT INTO `user_team` VALUES (12, 24, 2, '2026-05-02 11:00:00', '2026-05-02 11:00:00', '2026-05-02 11:00:00', 0);
INSERT INTO `user_team` VALUES (13, 27, 2, '2026-05-03 08:00:00', '2026-05-03 08:00:00', '2026-05-03 08:00:00', 0);
INSERT INTO `user_team` VALUES (14, 33, 2, '2026-05-03 09:00:00', '2026-05-03 09:00:00', '2026-05-03 09:00:00', 0);
INSERT INTO `user_team` VALUES (15, 37, 2, '2026-05-04 10:00:00', '2026-05-04 10:00:00', '2026-05-04 10:00:00', 0);
-- 队伍3: 代码自习室 (公开, 队长:用户3, maxNum=4) - 成员: 3,7,9,12
INSERT INTO `user_team` VALUES (16, 3, 3, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (17, 7, 3, '2026-04-30 15:00:00', '2026-04-30 15:00:00', '2026-04-30 15:00:00', 0);
INSERT INTO `user_team` VALUES (18, 9, 3, '2026-05-01 08:00:00', '2026-05-01 08:00:00', '2026-05-01 08:00:00', 0);
INSERT INTO `user_team` VALUES (19, 12, 3, '2026-05-02 14:00:00', '2026-05-02 14:00:00', '2026-05-02 14:00:00', 0);
-- 队伍4: 王者荣耀车队 (公开, 队长:用户4, maxNum=5) - 成员: 4,3,8,21,34
INSERT INTO `user_team` VALUES (20, 4, 4, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (21, 3, 4, '2026-04-30 16:00:00', '2026-04-30 16:00:00', '2026-04-30 16:00:00', 0);
INSERT INTO `user_team` VALUES (22, 8, 4, '2026-05-01 09:00:00', '2026-05-01 09:00:00', '2026-05-01 09:00:00', 0);
INSERT INTO `user_team` VALUES (23, 21, 4, '2026-05-02 10:00:00', '2026-05-02 10:00:00', '2026-05-02 10:00:00', 0);
INSERT INTO `user_team` VALUES (24, 34, 4, '2026-05-03 11:00:00', '2026-05-03 11:00:00', '2026-05-03 11:00:00', 0);
-- 队伍5: 游泳爱好者 (公开, 队长:用户5, maxNum=8) - 成员: 5,4,11,25,32,38
INSERT INTO `user_team` VALUES (25, 5, 5, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (26, 4, 5, '2026-05-01 08:00:00', '2026-05-01 08:00:00', '2026-05-01 08:00:00', 0);
INSERT INTO `user_team` VALUES (27, 11, 5, '2026-05-02 09:00:00', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0);
INSERT INTO `user_team` VALUES (28, 25, 5, '2026-05-03 10:00:00', '2026-05-03 10:00:00', '2026-05-03 10:00:00', 0);
INSERT INTO `user_team` VALUES (29, 32, 5, '2026-05-04 11:00:00', '2026-05-04 11:00:00', '2026-05-04 11:00:00', 0);
INSERT INTO `user_team` VALUES (30, 38, 5, '2026-05-05 12:00:00', '2026-05-05 12:00:00', '2026-05-05 12:00:00', 0);
-- 队伍6: 私密技术群 (私有, 队长:用户6, maxNum=3) - 成员: 6,1,7
INSERT INTO `user_team` VALUES (31, 6, 6, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (32, 1, 6, '2026-05-01 10:00:00', '2026-05-01 10:00:00', '2026-05-01 10:00:00', 0);
INSERT INTO `user_team` VALUES (33, 7, 6, '2026-05-02 11:00:00', '2026-05-02 11:00:00', '2026-05-02 11:00:00', 0);
-- 队伍7: 加密音乐组 (加密, 密码:music123, 队长:用户7, maxNum=6) - 成员: 7,2,10,25,32,12
INSERT INTO `user_team` VALUES (34, 7, 7, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (35, 2, 7, '2026-05-01 08:00:00', '2026-05-01 08:00:00', '2026-05-01 08:00:00', 0);
INSERT INTO `user_team` VALUES (36, 10, 7, '2026-05-02 09:00:00', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0);
INSERT INTO `user_team` VALUES (37, 25, 7, '2026-05-03 10:00:00', '2026-05-03 10:00:00', '2026-05-03 10:00:00', 0);
INSERT INTO `user_team` VALUES (38, 32, 7, '2026-05-04 11:00:00', '2026-05-04 11:00:00', '2026-05-04 11:00:00', 0);
INSERT INTO `user_team` VALUES (39, 12, 7, '2026-05-05 12:00:00', '2026-05-05 12:00:00', '2026-05-05 12:00:00', 0);
-- 队伍8: 已过期队伍 (公开, 已过期, 队长:用户8, maxNum=4) - 成员: 8,3
INSERT INTO `user_team` VALUES (40, 8, 8, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (41, 3, 8, '2026-04-30 10:00:00', '2026-04-30 10:00:00', '2026-04-30 10:00:00', 0);
-- 队伍9: 满员队伍 (公开, 已满, 队长:用户9, maxNum=2) - 成员: 9,23
INSERT INTO `user_team` VALUES (42, 9, 9, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (43, 23, 9, '2026-05-01 08:00:00', '2026-05-01 08:00:00', '2026-05-01 08:00:00', 0);
-- 队伍10: 旅行摄影团 (公开, 队长:用户10, maxNum=6) - 成员: 10,5,22,29,36
INSERT INTO `user_team` VALUES (44, 10, 10, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (45, 5, 10, '2026-05-01 09:00:00', '2026-05-01 09:00:00', '2026-05-01 09:00:00', 0);
INSERT INTO `user_team` VALUES (46, 22, 10, '2026-05-02 10:00:00', '2026-05-02 10:00:00', '2026-05-02 10:00:00', 0);
INSERT INTO `user_team` VALUES (47, 29, 10, '2026-05-03 11:00:00', '2026-05-03 11:00:00', '2026-05-03 11:00:00', 0);
INSERT INTO `user_team` VALUES (48, 36, 10, '2026-05-04 12:00:00', '2026-05-04 12:00:00', '2026-05-04 12:00:00', 0);
-- 队伍11: Go语言学习组 (公开, 队长:用户11, maxNum=5) - 成员: 11,20,25,30
INSERT INTO `user_team` VALUES (49, 11, 11, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (50, 20, 11, '2026-05-01 08:00:00', '2026-05-01 08:00:00', '2026-05-01 08:00:00', 0);
INSERT INTO `user_team` VALUES (51, 25, 11, '2026-05-02 09:00:00', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0);
INSERT INTO `user_team` VALUES (52, 30, 11, '2026-05-03 10:00:00', '2026-05-03 10:00:00', '2026-05-03 10:00:00', 0);
-- 队伍12: 前端模拟面试 (公开, 队长:用户12, maxNum=4) - 成员: 12,3,14,8
INSERT INTO `user_team` VALUES (53, 12, 12, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (54, 3, 12, '2026-05-01 09:00:00', '2026-05-01 09:00:00', '2026-05-01 09:00:00', 0);
INSERT INTO `user_team` VALUES (55, 14, 12, '2026-05-02 10:00:00', '2026-05-02 10:00:00', '2026-05-02 10:00:00', 0);
INSERT INTO `user_team` VALUES (56, 8, 12, '2026-05-03 11:00:00', '2026-05-03 11:00:00', '2026-05-03 11:00:00', 0);
-- 队伍13: 跑步打卡群 (公开, 队长:用户13, maxNum=20) - 成员: 13,8,14,19,24,26,38
INSERT INTO `user_team` VALUES (57, 13, 13, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (58, 8, 13, '2026-05-01 08:00:00', '2026-05-01 08:00:00', '2026-05-01 08:00:00', 0);
INSERT INTO `user_team` VALUES (59, 14, 13, '2026-05-02 09:00:00', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0);
INSERT INTO `user_team` VALUES (60, 19, 13, '2026-05-03 10:00:00', '2026-05-03 10:00:00', '2026-05-03 10:00:00', 0);
INSERT INTO `user_team` VALUES (61, 24, 13, '2026-05-04 11:00:00', '2026-05-04 11:00:00', '2026-05-04 11:00:00', 0);
INSERT INTO `user_team` VALUES (62, 26, 13, '2026-05-05 12:00:00', '2026-05-05 12:00:00', '2026-05-05 12:00:00', 0);
INSERT INTO `user_team` VALUES (63, 38, 13, '2026-05-06 13:00:00', '2026-05-06 13:00:00', '2026-05-06 13:00:00', 0);
-- 队伍14: 考研互助组 (公开, 队长:用户14, maxNum=10) - 成员: 14,15,24,28,31,40
INSERT INTO `user_team` VALUES (64, 14, 14, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (65, 15, 14, '2026-05-01 08:00:00', '2026-05-01 08:00:00', '2026-05-01 08:00:00', 0);
INSERT INTO `user_team` VALUES (66, 24, 14, '2026-05-02 09:00:00', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0);
INSERT INTO `user_team` VALUES (67, 28, 14, '2026-05-03 10:00:00', '2026-05-03 10:00:00', '2026-05-03 10:00:00', 0);
INSERT INTO `user_team` VALUES (68, 31, 14, '2026-05-04 11:00:00', '2026-05-04 11:00:00', '2026-05-04 11:00:00', 0);
INSERT INTO `user_team` VALUES (69, 40, 14, '2026-05-05 12:00:00', '2026-05-05 12:00:00', '2026-05-05 12:00:00', 0);
-- 队伍15: 配音练习室 (公开, 队长:用户15, maxNum=7) - 成员: 15,26,38
INSERT INTO `user_team` VALUES (70, 15, 15, '2026-04-29 17:00:47', '2026-04-29 17:00:47', '2026-04-29 17:00:47', 0);
INSERT INTO `user_team` VALUES (71, 26, 15, '2026-05-01 08:00:00', '2026-05-01 08:00:00', '2026-05-01 08:00:00', 0);
INSERT INTO `user_team` VALUES (72, 38, 15, '2026-05-02 09:00:00', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0);
-- 队伍16: 周小队 (加密, 密码:123456, 队长:用户1, maxNum=4) - 成员: 1,7,10
INSERT INTO `user_team` VALUES (73, 1, 16, '2026-04-30 18:30:49', '2026-04-30 18:30:49', '2026-04-30 18:30:49', 0);
INSERT INTO `user_team` VALUES (74, 7, 16, '2026-05-01 09:00:00', '2026-05-01 09:00:00', '2026-05-01 09:00:00', 0);
INSERT INTO `user_team` VALUES (75, 10, 16, '2026-05-02 10:00:00', '2026-05-02 10:00:00', '2026-05-02 10:00:00', 0);

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teamId` bigint NOT NULL COMMENT '队伍 ID',
  `senderId` bigint NOT NULL COMMENT '发送者用户 ID',
  `senderName` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送者昵称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_teamId` (`teamId`),
  INDEX `idx_teamId_createTime` (`teamId`, `createTime`)
) ENGINE=InnoDB CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';

-- ----------------------------
-- Table structure for user_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint NOT NULL COMMENT '用户 ID',
  `totalTeamsCreated` int NOT NULL DEFAULT 0 COMMENT '创建的队伍总数',
  `totalTeamsJoined` int NOT NULL DEFAULT 0 COMMENT '加入的队伍总数',
  `avgTeamDuration` double NOT NULL DEFAULT 0 COMMENT '平均组队时长（天）',
  `lastActiveTime` datetime NULL DEFAULT NULL COMMENT '最近活跃时间',
  `preferredTags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '偏好标签（JSON 数组）',
  `cooperationScore` double NOT NULL DEFAULT 1.0 COMMENT '协作评分（1.0~5.0）',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_userId` (`userId`)
) ENGINE=InnoDB CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户画像表';
