# 高校社团活动管理系统

基于 Spring Boot 3 + Vue 3 的社团活动管理平台，覆盖学生、社团负责人、管理员三类角色的完整业务流：入社审批、活动发布与报名、公告、社团风采、数据统计，并内置基于 RAG 思路的 AI 助手（模型自主调用工具、答案来自数据库）。

**在线演示**：http://42.194.133.158/ （管理端：http://42.194.133.158/admin/ ，初始账号见 `deploy/sql/schema.sql`）

## 技术栈

- **后端**：Java 21、Spring Boot 3、Spring Security Crypto（BCrypt）、MyBatis、MySQL 8、Redis、Spring AI（DeepSeek）、JWT（jjwt）
- **前端**：Vue 3、Vue Router、Element Plus、Vite、Axios、SSE（Fetch + ReadableStream）
- **部署**：Docker、Docker Compose、Nginx（单域名承载双前端 + API 反代）、腾讯云 OpenCloudOS 9

## 功能

| 端 | 能力 |
|---|---|
| 学生端 | 浏览社团/活动/公告/风采、报名与取消、申请入社/退社、个人中心、通知中心、AI 助手 |
| 负责人端 | 本社团工作台、入社/退社审批、活动管理与发布、报名管理、社团信息、数据统计 |
| 管理端 | 全局仪表盘、社团/分类/成员/报名管理、活动审核、公告发布、用户管理、审批中心 |

## 安全设计

- 密码 BCrypt 加盐哈希，历史 MD5 在登录时无感迁移；数据库中不再存在弱哈希
- 三层鉴权：JWT 认证拦截器 → `@RequireRole` 角色拦截器 → 数据归属校验（`Permission.isAdminOrOwner`）
- 登录防撞库：Redis 双维度限流（账号 5 次/分钟、IP 20 次/分钟），统一错误提示防账号枚举
- 敏感名单接口（成员/报名）登录可见，杜绝匿名遍历 id 拉取全校个人信息
- 密钥与凭据全部环境变量注入，无默认值；生产启动自检弱配置（弱密钥/弱口令直接拒绝启动）
- 安全响应头（nosniff / DENY / HSTS / CSP）+ API 响应禁缓存

## 快速启动（本地）

```bash
# 前置：JDK 21、MySQL 8、Redis、Node 18+
mysql -uroot -p < deploy/sql/schema.sql     # 建库建表（含引导管理员）

# 后端：先建本地开发配置 src/main/resources/application-dev.properties
# （dev 口令、spring.cache.type=redis 等，参见 application.properties 中的占位项）
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 前端
cd frontend && npm i && npm run dev     # http://localhost:5173
cd admin && npm i && npm run dev        # http://localhost:5174
```

## Docker 部署（生产）

一条命令起全套（MySQL + Redis + 后端 + Nginx），数据库与 Redis 仅内网可达：
详见 [`deploy/部署清单.md`](deploy/部署清单.md)，含腾讯云 OpenCloudOS 9 初始化、HTTPS 证书、每日备份。

```bash
cd deploy && docker compose up -d --build
```

## 测试

```bash
./mvnw test    # 111 个用例：服务层/权限/JWT/密码/审批流
```

## 目录结构

```
├── src/main/java/com/crisp/demo
│   ├── domain/          # 按领域划分：identity/club/membership/activity/registration/approval/announcement/assistant/analytics
│   ├── infrastructure/  # 拦截器、CORS、全局异常、安全头
│   └── shared/          # JWT、权限、密码、事件、错误码
├── frontend/            # 学生端 Vue 3
├── admin/               # 管理端 Vue 3
├── deploy/              # Docker Compose、Nginx、schema.sql、部署清单
└── sql/                 # 本地演示种子数据
```
