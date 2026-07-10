# 伙伴匹配系统（智配通）

> 基于 TF-IDF 相似度算法 + AI 大模型的智能组队 / 找伙伴平台。用户可以编辑个性标签、按标签检索志同道合的伙伴、创建与加入队伍，并在队伍内实时聊天。

[![Java](https://img.shields.io/badge/Java-21-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen.svg)]()
[![Vue](https://img.shields.io/badge/Vue-3-42b883.svg)]()
[![License](https://img.shields.io/badge/License-MIT-blue.svg)]()

---

## ✨ 功能特性

- **用户模块**：注册 / 登录（Session + Redis 分布式会话）、个人资料与个性标签管理
- **智能匹配**：基于 **TF-IDF + 余弦相似度** 计算用户标签相似度，返回最匹配的伙伴
- **AI 推荐理由**：接入阿里云百炼 **通义千问（DashScope）**，为匹配结果生成自然语言推荐说明
- **组队功能**：队伍的创建 / 修改 / 解散 / 查询，支持公开、加密（房间密码）、私有三种状态
- **加入 / 退出**：基于 **Redisson 分布式锁** 解决并发抢队伍的超员问题
- **实时聊天**：基于 **WebSocket** 的队伍聊天室，消息持久化
- **接口限流**：自定义 `@RateLimit` 注解 + AOP + Guava RateLimiter 实现方法级限流
- **性能优化**：Redis 缓存 + 定时缓存预热任务，降低热点查询压力
- **可观测性**：Spring Boot Actuator + Micrometer + Prometheus + Grafana 监控大盘
- **接口文档**：Knife4j（OpenAPI 3）在线接口文档

---

## 🛠 技术栈

### 后端

| 分类 | 技术 |
| --- | --- |
| 核心框架 | Spring Boot 3.5.x、Java 21 |
| 持久层 | MyBatis-Plus 3.5.x、MySQL 8.0 |
| 缓存 / 会话 | Redis、Spring Session、Redisson（分布式锁） |
| AI 能力 | Spring AI Alibaba DashScope（通义千问 qwen-plus） |
| 限流 | Guava RateLimiter + Spring AOP |
| 实时通信 | Spring WebSocket |
| 监控 | Actuator + Micrometer + Prometheus + Grafana |
| 接口文档 | Knife4j (OpenAPI 3) |
| 工具库 | Hutool |
| 性能基准 | JMH |

### 前端

| 分类 | 技术 |
| --- | --- |
| 框架 | Vue 3 + TypeScript |
| 构建工具 | Vite |
| UI 组件库 | Vant |
| HTTP | Axios |
| 路由 / 状态 | Vue Router、组合式状态管理 |

---

## 📁 项目结构

```
partner-match-backend/
├── src/main/java/com/zsc/partnermatch/
│   ├── annotation/      # 自定义注解（限流 @RateLimit）
│   ├── aspect/          # AOP 切面（限流实现）
│   ├── commont/         # 通用响应、错误码、分页、枚举
│   ├── config/          # Redis / MyBatis-Plus / CORS / Swagger / WebSocket 等配置
│   ├── controller/      # 控制层（User / Team / Tag / UserTeam）
│   ├── dto/             # 请求参数对象
│   ├── entity/          # 数据库实体
│   ├── event/           # 领域事件与监听
│   ├── exception/       # 全局异常处理
│   ├── job/             # 定时任务（缓存预热）
│   ├── mapper/          # MyBatis-Plus Mapper
│   ├── service/         # 业务逻辑（含匹配算法、AI 推荐）
│   ├── utils/           # 工具类（TF-IDF 算法等）
│   ├── vo/              # 视图对象
│   ├── websocket/       # WebSocket 端点
│   └── sql/course.sql   # 数据库初始化脚本
├── src/main/resources/
│   ├── mapper/                    # MyBatis XML 映射
│   ├── application-example.yml    # 配置模板（复制为 application.yml 使用）
│   └── logback-spring.xml
├── zsc-partner-match/   # 前端工程（Vue3 + Vite）
├── docker/              # Nginx / Prometheus 配置
├── Dockerfile
└── docker-compose.yml   # 一键部署（MySQL + Redis + App + Prometheus + Grafana + Nginx）
```

---

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6+
- Node.js 18+（前端）

### 1. 初始化数据库

导入建表与初始化脚本：

```bash
mysql -u root -p < src/main/java/com/zsc/partnermatch/sql/course.sql
```

### 2. 配置后端

项目的 `application.yml` **不纳入版本管理**（含敏感信息），请复制模板并按需修改：

```bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
```

所有敏感配置通过 **环境变量** 注入（也可在 `application.yml` 中直接填写本地开发值）：

| 环境变量 | 说明 | 默认值 |
| --- | --- | --- |
| `DB_HOST` | MySQL 主机 | localhost |
| `DB_PORT` | MySQL 端口 | 3306 |
| `DB_NAME` | 数据库名 | course |
| `DB_USERNAME` | MySQL 用户名 | root |
| `DB_PASSWORD` | MySQL 密码 | *（必填）* |
| `REDIS_HOST` | Redis 主机 | localhost |
| `REDIS_PORT` | Redis 端口 | 6379 |
| `REDIS_PASSWORD` | Redis 密码 | *（必填）* |
| `AI_API_KEY` | 阿里云百炼 DashScope API Key | *（必填）* |

### 3. 启动后端

```bash
mvn spring-boot:run
```

服务默认运行在 **http://localhost:8083**。

### 4. 启动前端

```bash
cd zsc-partner-match
npm install
npm run dev
```

---

## 🐳 Docker 一键部署

已提供 `docker-compose.yml`，包含 MySQL、Redis、应用、Prometheus、Grafana、Nginx 全套服务：

```bash
# 通过环境变量注入密钥（不要写进代码仓库）
export DB_PASSWORD=your_db_password
export REDIS_PASSWORD=your_redis_password
export AI_API_KEY=your_dashscope_key

docker-compose up -d
```

---

## 📖 接口文档

启动后端后访问 Knife4j 文档：

- Knife4j UI： http://localhost:8083/doc.html
- Swagger UI： http://localhost:8083/swagger-ui.html
- OpenAPI JSON： http://localhost:8083/v3/api-docs

---

## 📊 监控

- Prometheus 指标端点： http://localhost:8083/actuator/prometheus
- Grafana 大盘：`docker-compose` 启动后访问 http://localhost:3000

---

## 💡 核心亮点

1. **匹配算法**：将用户标签向量化，用 TF-IDF 计算词权重、余弦相似度衡量匹配度，并用小顶堆做 Top-N 优选，避免全量排序开销；使用 JMH 对算法做了基准测试。
2. **分布式锁**：加入队伍时用 Redisson 锁住队伍维度的临界区，杜绝并发场景下的超员问题。
3. **AI 融合**：在算法召回的基础上，用通义千问生成「为什么推荐 TA」的自然语言解释，提升产品体验。
4. **稳定性**：接口级限流 + 缓存预热 + 全局异常处理 + Prometheus/Grafana 监控，具备一定生产可用性。

---

## 📌 说明

本项目为个人学习与实践项目，敏感信息（数据库密码、Redis 密码、AI API Key、服务器地址等）均通过环境变量注入，未包含在代码仓库中。
