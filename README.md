# 社团活动管理系统 — 项目文档报告

---

## 1. 系统定位

社团活动管理系统是一款面向高校学生社团的 **全栈 Web 应用**，旨在为**学生**和**社团管理者/系统管理员**提供一站式的社团事务管理平台。系统覆盖**社团创建、成员管理（含审批/退出双向流程）、活动发布（含草稿/发布/取消状态机）、活动报名（含人数控制和取消审批）、签到签退、活动总结**等核心业务场景，帮助提升社团运营效率和管理规范性。

---

## 2. 技术栈

### 2.1 技术选型总览

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot (WebMVC) | 4.0.6 |
| **ORM 框架** | MyBatis (Spring Boot Starter) | 4.0.1 |
| **数据库** | MySQL | 8.0 |
| **缓存 & 会话** | Redis + Spring Session + Spring Cache | 7.x |
| **参数校验** | Jakarta Bean Validation (Hibernate Validator) | (Spring Boot 内置) |
| **Java 版本** | JDK 21 (LTS) | 21.0.3 |
| **构建工具** | Maven Wrapper | 3.9.15 |
| **前端 (管理端)** | Vue 3 + Vue Router 5 + Axios + Vite 8 | — |
| **前端 (学生端)** | Vue 3 + Vue Router 5 + Axios + Vite 8 | — |
| **前端 (静态备选)** | 纯 HTML5 + CSS3 + Bootstrap 5.3 (CDN) + ES6 | — |
| **Web 服务器** | Apache Tomcat 11 | (Spring Boot 内置) |

### 2.2 架构模式

采用 **前后端分离 + RESTful API** 架构：

```
┌──────────────────────────────────────────────────┐
│                 前端 (3 种并存)                     │
│  admin/ (Vue 3 SPA)  frontend/ (Vue 3 SPA)        │
│  static/ (纯 HTML + Bootstrap 5)                   │
│       ↕  HTTP REST API (JSON)                      │
└──────────────────┬───────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────┐
│            Spring Boot 后端 (port 8081)            │
│                                                    │
│  ┌─────────────────────────────────────────────┐  │
│  │  拦截器 / 全局异常处理 / 参数校验              │  │
│  │  AuthInterceptor | GlobalExceptionHandler     │  │
│  │  @CurrentUser | Jakarta Validation           │  │
│  └─────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────┐  │
│  │  Controller 层 (portal/ 学生端 + admin/ 管理端)│  │
│  │  11 个 Controller, RESTful JSON API           │  │
│  └─────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────┐  │
│  │  Service 层 (7 接口 + 7 实现)                  │  │
│  │  @Transactional + BusinessException           │  │
│  └─────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────┐  │
│  │  Mapper 层 (7 个 MyBatis 注解接口, 零 XML)     │  │
│  └─────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────┐  │
│  │  缓存层: Redis (Spring Cache @Cacheable)       │  │
│  │  会话层: Redis (Spring Session, 30min TTL)    │  │
│  └─────────────────────────────────────────────┘  │
└──────────────────┬───────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────┐
│            MySQL 8.0 数据库 (club_system)           │
│  7 张表, utf8mb4, 完整外键约束, 唯一索引             │
└──────────────────────────────────────────────────┘
```

### 2.3 项目亮点

- **零 XML MyBatis**: 所有 SQL 通过 `@Select`/`@Insert`/`@Update`/`@Delete` 注解编写，无 XML 映射文件
- **Redis 双用途**: 同时承担 HTTP Session 共享 (Spring Session) 和业务数据缓存 (Spring Cache, 30 分钟 TTL)
- **前后端分离**: API 返回统一 `{code, message, data}` JSON，前端通过 `fetch`/`axios` 调用
- **全局异常处理**: `@RestControllerAdvice` 消除 Controller 层重复 `try-catch`
- **声明式参数注入**: `@CurrentUser` 注解 + `HandlerMethodArgumentResolver` 自动注入当前登录用户
- **统一 Session 管理**: 前后台共享同一 `user` session key，最后登录者覆盖前者，避免身份重叠
- **细粒度权限控制**: 社团审批/编辑操作校验 `ADMIN` 角色或社团负责人身份，学生无法越权
- **F-PolyImage UI 风格**: 参考 `image.ntu17.shop`，Indigo-Purple-Violet 渐变光谱 + 玻璃拟态 + 彩色发光阴影
- **无障碍优化**: `aria-label` 语义标注，`prefers-reduced-motion` 尊重系统动画偏好
- **设计令牌系统**: CSS 自定义属性统一管理颜色/间距/阴影/动画时序
- **微交互动画**: 50-100ms 按压反馈、40ms 交错入场、玻璃卡片 hover 发光
- **三重前端并存**: 管理端 Vue SPA (`admin/`)、学生端 Vue SPA (`frontend/`)、纯 HTML 静态页面 (`static/`)
- **自动化测试**: 5 套 JUnit 5 测试类覆盖用户/社团/成员/活动/报名，53 个用例全部通过
- **问题记录**: `problem.md` 记录 31 个开发中遇到的问题及解决方案

---

## 3. 核心成果

系统实现了 **28 项**功能，按模块分布如下：

| 序号 | 功能模块 | 功能项 | 数量 |
|------|---------|--------|:---:|
| 1 | 用户信息管理 | 注册、登录、注销、个人信息查询、个人信息修改、密码修改、用户列表管理、用户搜索 | 8 |
| 2 | 社团信息管理 | 创建社团、社团列表/搜索、详情查看、编辑、删除、我管理的社团 | 6 |
| 3 | 社团成员管理 | 申请加入、审批通过、审批拒绝、退出社团、申请退出、审批退出通过、审批退出拒绝、查看成员列表、查看待审批列表 | 9 |
| 4 | 活动发布管理 | 创建活动、活动列表/搜索、查看详情、编辑、删除（仅发布人/管理员）、状态变更（发布/取消/结束） | 7 |
| 5 | 活动报名管理 | 报名活动、取消报名、申请取消、审批取消、拒绝取消、报名列表查询、人数限制原子控制 | 7 |
| 6 | 活动签到管理 | 签到、签退、签到记录查询 | 3 |
| 7 | 活动总结管理 | 创建总结、编辑总结、查看总结列表、按活动查看总结 | 4 |

> **总计: 44 个功能点** (含子功能展开)

---

## 4. 用户角色分析

### 4.1 角色定义

| 角色 | 数据库值 | 核心需求 | 权限范围 |
|------|---------|----------|----------|
| **学生** | `STUDENT` | 注册登录、浏览社团/活动、申请加入社团、报名活动、签到签退、管理个人信息、申请退出社团/取消报名 | 查看所有社团和活动；管理自己的报名、签到和成员资格 |
| **管理员** | `ADMIN` | 全局管理权限，可删除任意用户发布的活动，查看所有用户列表 | 学生全部权限 + 删除他人活动 + 用户管理 |

### 4.2 权限矩阵

| 操作 | 未登录用户 | 学生 | 社团负责人* | 管理员 |
|------|:---:|:---:|:---:|:---:|
| 浏览社团/活动列表 | ✅ | ✅ | ✅ | ✅ |
| 注册账号 | ✅ | — | — | — |
| 登录 | ✅ | — | — | — |
| 查看个人信息 | ❌ | ✅ | ✅ | ✅ |
| 修改个人信息/密码 | ❌ | ✅ | ✅ | ✅ |
| 创建社团 | ❌ | ✅ | ✅ | ✅ |
| 审批成员申请 | ❌ | ❌ | ✅ (仅自己负责社团) | ✅ (任意社团) |
| 发布活动 | ❌ | ✅ | ✅ | ✅ |
| 编辑活动 | ❌ | ✅ (仅自己发布) | ✅ (仅自己发布) | ✅ (任意) |
| 删除活动 | ❌ | ✅ (仅自己发布) | ✅ (仅自己发布) | ✅ (任意) |
| 报名活动 | ❌ | ✅ | ✅ | ✅ |
| 签到/签退 | ❌ | ✅ (仅自己) | ✅ (仅自己) | ✅ (仅自己) |
| 创建活动总结 | ❌ | ✅ | ✅ | ✅ |
| 查看用户列表 | ❌ | ❌ | ❌ | ✅ |

> \* 社团负责人通过 `club.leader_id` 字段关联，后端在 Controller 层基于 `currentUser` 与 `club.leaderId` 进行权限校验。

---

## 5. 功能性需求（用例表）

### 5.1 用户注册 (UC-01)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生 |
| **前置条件** | 未登录状态 |
| **基本事件流** | 1. 进入注册页面 2. 填写学号(4-20位)、姓名(≤50字)、密码(6-100位)、手机号(11位)、邮箱、性别 3. 前端表单校验 → 后端 Jakarta Bean Validation 二次校验 4. 系统校验学号唯一性 5. MD5 加密存储密码 6. 自动跳转登录页 |
| **后置条件** | 用户记录写入 `user` 表，默认角色 `STUDENT` |

### 5.2 用户登录 (UC-02)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生、管理员 |
| **前置条件** | 已注册 |
| **基本事件流** | 1. 进入登录页面 2. 输入学号和密码 3. 系统 MD5 加密后与数据库比对 4. 成功后存入 Redis-backed HttpSession 5. 导航栏显示用户名 |
| **后置条件** | HttpSession 中存入当前 `User` 对象，30 分钟超时 |

### 5.3 社团创建 (UC-03)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生、管理员 |
| **前置条件** | 已登录 |
| **基本事件流** | 1. 进入创建社团页面 2. 填写名称(唯一)、简介、Logo URL 3. 系统自动设当前用户为负责人 (`leader_id`) 4. 提交 → 写入数据库 |
| **后置条件** | 社团记录创建，状态默认 `ACTIVE`，创建者自动成为负责人 |

### 5.4 申请加入社团 (UC-04)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生 |
| **前置条件** | 已登录，未加入该社团（或之前已退出/被拒绝） |
| **基本事件流** | 1. 进入社团详情页 2. 点击"申请加入" 3. 填写申请理由(≤500字) 4. 提交后状态为 `PENDING` 5. 若之前为 `QUIT` 或 `REJECTED` 状态，系统复用旧记录而非创建新记录 |
| **后置条件** | 产生待审批成员记录 (status=`PENDING`) |

### 5.5 审批成员 (UC-05)

| 项目 | 描述 |
|------|------|
| **参与角色** | 社团负责人、管理员 |
| **前置条件** | 操作者为该社团负责人或管理员，有待审批申请 |
| **基本事件流** | 1. 进入"我管理的社团" → 成员管理 2. 查看待审批列表 3. 点击"通过" → `status='APPROVED'`, `joined_at=NOW()` 4. 或点击"拒绝" → `status='REJECTED'` |
| **后置条件** | 成员状态更新，通过后正式成为社团成员 |

### 5.6 退出社团 (UC-06)

| 项目 | 描述 |
|------|------|
| **参与角色** | 社团成员 |
| **前置条件** | 已是该社团成员 (status=`APPROVED`) |
| **基本事件流** | 1. 在"我的社团"中点击"退出" 2a. 直接退出 → status=`QUIT`, quit_at=NOW() 2b. 申请退出 → status=`QUIT_PENDING`，等待负责人审批 |
| **后置条件** | 成员资格终止或进入待审批状态 |

### 5.7 发布活动 (UC-07)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生、管理员 |
| **前置条件** | 已登录 |
| **基本事件流** | 1. 进入发布活动页面 2. 选择所属社团 3. 填写标题、描述、地点、开始/结束时间、人数限制(0=不限) 4. 选择状态：草稿(`DRAFT`) 或直接发布(`PUBLISHED`) 5. 提交 → 写入数据库 |
| **后置条件** | 活动记录创建，`current_count=0` |

### 5.8 活动报名 (UC-08)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生 |
| **前置条件** | 已登录，活动状态为 `PUBLISHED` 或 `ONGOING`，未报名，人数未满 |
| **基本事件流** | 1. 进入活动详情页 2. 点击"立即报名" 3. 系统原子操作: `UPDATE activity SET current_count = current_count + 1 WHERE id=? AND (max_participants=0 OR current_count < max_participants)` 4. 创建报名记录 (`REGISTERED`) 5. 自动创建签到记录 (`UNCHECKED`) |
| **后置条件** | 报名记录写入；活动 `current_count +1`；签到记录创建 |

### 5.9 取消报名 (UC-09)

| 项目 | 描述 |
|------|------|
| **参与角色** | 已报名学生 |
| **前置条件** | 已报名该活动，状态为 `REGISTERED` |
| **基本事件流** | 1. 点击"取消报名" 2a. 直接取消 → status=`CANCELLED`, 活动 `current_count -1` 2b. 申请取消 → status=`CANCEL_PENDING`，等待审批 |
| **后置条件** | 报名状态更新，人数回退 |

### 5.10 活动签到 (UC-10)

| 项目 | 描述 |
|------|------|
| **参与角色** | 已报名学生 |
| **前置条件** | 已报名该活动，签到状态为 `UNCHECKED` |
| **基本事件流** | 1. 进入活动详情页 2. 点击"签到" 3. 系统记录签到时间，更新 `checkin_status='CHECKED'` |
| **后置条件** | `checkin_time` 和 `checkin_status` 更新；防止重复签到 |

### 5.11 活动签退 (UC-11)

| 项目 | 描述 |
|------|------|
| **参与角色** | 已签到学生 |
| **前置条件** | 已签到，签退状态为 `UNCHECKED` |
| **基本事件流** | 1. 进入活动详情页 2. 点击"签退" 3. 系统记录签退时间，更新 `checkout_status='CHECKED'` |
| **后置条件** | `checkout_time` 和 `checkout_status` 更新；防止重复签退 |

### 5.12 活动总结 (UC-12)

| 项目 | 描述 |
|------|------|
| **参与角色** | 社团成员/负责人 |
| **前置条件** | 已登录，活动已结束 |
| **基本事件流** | 1. 在活动详情页点击"添加总结" 2. 填写活动内容、效果评估、改进建议、图片 URL 3. 提交 → 写入数据库 |
| **后置条件** | 总结记录写入 `activity_summary` 表 |

---

## 6. 系统设计

### 6.1 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端 (Presentation)                       │
│                                                                  │
│  ┌─────────────────────┐  ┌─────────────────────┐               │
│  │  admin/ (管理端)     │  │ frontend/ (学生端)   │               │
│  │  Vue 3 + Router 5   │  │  Vue 3 + Router 5   │               │
│  │  port 5174 (dev)    │  │  Vite dev server    │               │
│  └──────────┬──────────┘  └──────────┬──────────┘               │
│             │                        │                            │
│  ┌──────────┴────────────────────────┴──────────┐               │
│  │  static/ (纯 HTML 备选前端)                    │               │
│  │  Bootstrap 5.3 + Bootstrap Icons + ES6 fetch  │               │
│  └──────────────────────┬───────────────────────┘               │
│                         │  HTTP REST (JSON)                      │
└─────────────────────────┼───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                    Spring Boot 后端 (Business)                    │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  横切关注点 (Cross-cutting)                                 │ │
│  │  • AuthInterceptor: Session 认证拦截 → 401 JSON             │ │
│  │  • GlobalExceptionHandler: @RestControllerAdvice 统一异常    │ │
│  │  • CorsConfig: 跨域配置                                     │ │
│  │  • @CurrentUser + ArgumentResolver: 参数级用户注入           │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌──────────────────────┐    ┌──────────────────────────────┐  │
│  │ Controller/admin/     │    │ Controller/portal/            │  │
│  │ • AdminActivity       │    │ • PortalUser (含页面路由)     │  │
│  │ • AdminClub           │    │ • PortalClub                  │  │
│  │ • AdminMember         │    │ • PortalActivity              │  │
│  │ • AdminUser           │    │ • PortalMember                │  │
│  │ • AdminSummary        │    │ • PortalRegistration          │  │
│  │                        │    │ • PortalSummary               │  │
│  └───────────┬────────────┘    └──────────────┬───────────────┘  │
│              └────────────────────────────────┘                  │
│                         │                                        │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │  Service 层 (7 接口 + 7 实现)                               │ │
│  │  @Service + @Transactional + @Cacheable/@CacheEvict         │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                        │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │  Mapper 层 (7 个 MyBatis 注解接口, 零 XML)                  │ │
│  │  @Select / @Insert / @Update / @Delete + LEFT JOIN         │ │
│  └──────────────────────┬─────────────────────────────────────┘ │
│                         │                                        │
│  ┌──────────────────────▼─────────────────────────────────────┐ │
│  │  Redis (双角色)                                             │ │
│  │  • Spring Session (session 共享, 30min TTL)                 │ │
│  │  • Spring Cache (@Cacheable club/activity, 30min TTL)       │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────┬───────────────────────────────────────┘
                          │ JDBC
┌─────────────────────────▼───────────────────────────────────────┐
│                  MySQL 8.0 数据库 (Persistence)                  │
│  club_system (utf8mb4) → 7 张表 + 外键约束 + 唯一索引             │
└─────────────────────────────────────────────────────────────────┘
```

### 6.2 功能模块图

```
社团活动管理系统
│
├── 用户模块 (PortalUserController / AdminUserController)
│   ├── 注册 POST /api/user/register
│   ├── 登录 POST /api/user/login
│   ├── 注销 POST /api/user/logout
│   ├── 获取当前用户 GET /api/user/current
│   ├── 个人信息修改 PUT /api/user/update
│   ├── 密码修改 PUT /api/user/updatePassword
│   ├── 用户列表(管理) GET /api/user/list
│   ├── 用户搜索(管理) GET /api/user/search
│   └── 用户删除(管理) DELETE /api/user/{id}
│
├── 社团模块 (PortalClubController / AdminClubController)
│   ├── 社团列表 GET /api/club/list
│   ├── 社团搜索 GET /api/club/search
│   ├── 社团详情 GET /api/club/{id}
│   ├── 我管理的社团 GET /api/club/my-managed
│   ├── 创建社团 POST /api/club/create
│   ├── 编辑社团 PUT /api/club/update
│   └── 删除社团 DELETE /api/club/{id}
│
├── 社团成员模块 (PortalMemberController / AdminMemberController)
│   ├── 申请加入 POST /api/club-member/apply
│   ├── 我的社团 GET /api/club-member/my
│   ├── 退出社团 PUT /api/club-member/quit/{id}
│   ├── 申请退出 PUT /api/club-member/request-quit/{id}
│   ├── 审批通过 PUT /api/club-member/approve/{id}
│   ├── 审批拒绝 PUT /api/club-member/reject/{id}
│   ├── 审批退出通过 PUT /api/club-member/approve-quit/{id}
│   ├── 审批退出拒绝 PUT /api/club-member/reject-quit/{id}
│   ├── 成员列表 GET /api/club-member/club/{clubId}
│   ├── 待审批列表 GET /api/club-member/pending/{clubId}
│   └── 删除成员 DELETE /api/club-member/{id}
│
├── 活动模块 (PortalActivityController / AdminActivityController)
│   ├── 活动列表 GET /api/activity/list
│   ├── 活动搜索 GET /api/activity/search
│   ├── 活动详情 GET /api/activity/{id}
│   ├── 按社团筛选 GET /api/activity/club/{clubId}
│   ├── 按状态筛选 GET /api/activity/status/{status}
│   ├── 我发布的活动 GET /api/activity/my-published
│   ├── 创建活动 POST /api/activity/create
│   ├── 编辑活动 PUT /api/activity/update
│   ├── 变更状态 PUT /api/activity/status/{id}
│   └── 删除活动 DELETE /api/activity/{id}
│
├── 报名模块 (PortalRegistrationController / AdminActivityController)
│   ├── 报名活动 POST /api/registration/register
│   ├── 我的报名 GET /api/registration/my
│   ├── 检查报名状态 GET /api/registration/check
│   ├── 取消报名 PUT /api/registration/cancel/{id}
│   ├── 申请取消 PUT /api/registration/request-cancel/{id}
│   └── 活动报名列表 GET /api/registration/activity/{activityId}
│
└── 总结模块 (PortalSummaryController / AdminSummaryController)
    ├── 总结列表 GET /api/summary/list
    ├── 按活动查看 GET /api/summary/activity/{activityId}
    ├── 总结详情 GET /api/summary/{id}
    ├── 创建总结 POST /api/summary/create
    └── 编辑总结 PUT /api/summary/update
```

---

## 7. 数据库设计

### 7.1 E-R 图

```
  ┌──────────┐         ┌──────────────┐         ┌──────────┐
  │   user   │ 1 ─── N │ club_member  │ N ─── 1 │   club   │
  │          │         │  user_id(FK) │         │          │
  │ id (PK)  │         │  club_id(FK) │         │ id (PK)  │
  │ student_no│        │  role         │        │ name     │
  │ password │         │  status       │        │ leader_id│
  │ name     │         │  apply_reason │        │ status   │
  │ role     │         │  joined_at    │        └──────────┘
  └──────────┘         │  quit_at      │              │
       │               └──────────────┘              │
       │                      │                      │
       │     ┌────────────────┘                      │
       │     │                                       │
       │     │              ┌────────────────────────┘
       │     │              │
       │     ├──────────────────────────────┐
       │     │                              │
       │     ▼                              ▼
       │  ┌──────────────────┐    ┌──────────────────┐
       │  │ activity         │    │ activity_summary  │
       │  │  club_id (FK)    │    │  activity_id (FK) │
       │  │  creator_id (FK) │    │  creator_id (FK)  │
       │  │  title, status   │    │  content, effect  │
       │  └────────┬─────────┘    └──────────────────┘
       │           │ 1
       │           │
       │  ┌────────▼────────────────┐
       │  │ activity_registration    │
       │  │  activity_id (FK)        │
       ├──│  user_id (FK)           │
       │  │  status, registered_at  │
       │  └────────┬────────────────┘
       │           │ 1
       │           │
       │  ┌────────▼────────┐
       │  │ activity_checkin │
       └──│  registration_id │
          │  user_id (FK)    │
          │  checkin/out     │
          └─────────────────┘
```

### 7.2 表结构详解

#### 7.2.1 用户信息表 (`user`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 用户 ID |
| `student_no` | VARCHAR(20) | NOT NULL, UNIQUE | 学号（登录账号） |
| `password` | VARCHAR(255) | NOT NULL | MD5 加密密码 |
| `name` | VARCHAR(50) | NOT NULL | 真实姓名 |
| `phone` | VARCHAR(20) | DEFAULT NULL | 联系电话 |
| `email` | VARCHAR(100) | DEFAULT NULL | 电子邮箱 |
| `gender` | TINYINT | DEFAULT 0 | 0=未知, 1=男, 2=女 |
| `role` | VARCHAR(20) | DEFAULT 'STUDENT' | STUDENT / ADMIN |
| `created_at` | DATETIME | DEFAULT NOW | 注册时间 |
| `updated_at` | DATETIME | ON UPDATE NOW | 更新时间 |

#### 7.2.2 社团信息表 (`club`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 社团 ID |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE | 社团名称 |
| `description` | TEXT | DEFAULT NULL | 社团简介 |
| `logo_url` | VARCHAR(255) | DEFAULT NULL | Logo 图片地址 |
| `leader_id` | BIGINT | FK → user.id (SET NULL) | 负责人 ID |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | ACTIVE / DISBANDED |
| `created_at` | DATETIME | DEFAULT NOW | 创建时间 |
| `updated_at` | DATETIME | ON UPDATE NOW | 更新时间 |

#### 7.2.3 社团成员表 (`club_member`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 记录 ID |
| `user_id` | BIGINT | FK → user.id (CASCADE), NOT NULL | 学生 ID |
| `club_id` | BIGINT | FK → club.id (CASCADE), NOT NULL | 社团 ID |
| `role` | VARCHAR(20) | DEFAULT 'MEMBER' | LEADER / VICE_LEADER / MEMBER |
| `status` | VARCHAR(20) | DEFAULT 'PENDING' | PENDING / APPROVED / REJECTED / QUIT_PENDING / QUIT |
| `apply_reason` | VARCHAR(500) | DEFAULT NULL | 申请理由 |
| `joined_at` | DATETIME | DEFAULT NULL | 加入时间（审批通过后填入） |
| `quit_at` | DATETIME | DEFAULT NULL | 退出时间 |
| `created_at` | DATETIME | DEFAULT NOW | 创建时间 |
| `updated_at` | DATETIME | ON UPDATE NOW | 更新时间 |

> **唯一约束**: `UNIQUE(user_id, club_id)` — 同一学生对同一社团仅有一条记录，支持状态流转复用。

#### 7.2.4 活动信息表 (`activity`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 活动 ID |
| `club_id` | BIGINT | FK → club.id (CASCADE), NOT NULL | 所属社团 ID |
| `title` | VARCHAR(200) | NOT NULL | 活动名称 |
| `description` | TEXT | DEFAULT NULL | 活动简介 |
| `location` | VARCHAR(200) | NOT NULL | 活动地点 |
| `start_time` | DATETIME | NOT NULL | 开始时间 |
| `end_time` | DATETIME | NOT NULL | 结束时间 |
| `max_participants` | INT | DEFAULT 0 | 人数限制 (0=不限制) |
| `current_count` | INT | DEFAULT 0 | 已报名人数 |
| `status` | VARCHAR(20) | DEFAULT 'DRAFT' | DRAFT / PUBLISHED / ONGOING / FINISHED / CANCELLED |
| `creator_id` | BIGINT | FK → user.id (CASCADE), NOT NULL | 发布人 ID |
| `created_at` | DATETIME | DEFAULT NOW | 创建时间 |
| `updated_at` | DATETIME | ON UPDATE NOW | 更新时间 |

> **活动状态机**: `DRAFT → PUBLISHED → ONGOING → FINISHED` (正常流程) / `[任意] → CANCELLED` (取消)

#### 7.2.5 活动报名表 (`activity_registration`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 记录 ID |
| `activity_id` | BIGINT | FK → activity.id (CASCADE), NOT NULL | 活动 ID |
| `user_id` | BIGINT | FK → user.id (CASCADE), NOT NULL | 学生 ID |
| `status` | VARCHAR(20) | DEFAULT 'REGISTERED' | REGISTERED / CANCEL_PENDING / CANCELLED |
| `registered_at` | DATETIME | DEFAULT NOW | 报名时间 |
| `cancelled_at` | DATETIME | DEFAULT NULL | 取消时间 |

> **唯一约束**: `UNIQUE(activity_id, user_id)` — 同一学生对同一活动仅一条报名记录。

#### 7.2.6 活动签到签退表 (`activity_checkin`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 记录 ID |
| `registration_id` | BIGINT | FK → activity_registration.id (CASCADE), UNIQUE | 关联报名记录 |
| `activity_id` | BIGINT | FK → activity.id (CASCADE), NOT NULL | 活动 ID (冗余) |
| `user_id` | BIGINT | FK → user.id (CASCADE), NOT NULL | 学生 ID (冗余) |
| `checkin_time` | DATETIME | DEFAULT NULL | 签到时间 |
| `checkin_status` | VARCHAR(20) | DEFAULT 'UNCHECKED' | UNCHECKED / CHECKED |
| `checkout_time` | DATETIME | DEFAULT NULL | 签退时间 |
| `checkout_status` | VARCHAR(20) | DEFAULT 'UNCHECKED' | UNCHECKED / CHECKED |
| `created_at` | DATETIME | DEFAULT NOW | 创建时间 |
| `updated_at` | DATETIME | ON UPDATE NOW | 更新时间 |

#### 7.2.7 活动总结表 (`activity_summary`)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 记录 ID |
| `activity_id` | BIGINT | FK → activity.id (CASCADE), NOT NULL | 活动 ID |
| `content` | TEXT | NOT NULL | 活动内容总结 |
| `effect` | TEXT | DEFAULT NULL | 活动效果评估 |
| `suggestions` | TEXT | DEFAULT NULL | 改进建议 |
| `images` | VARCHAR(2000) | DEFAULT NULL | 图片 URL，多个逗号分隔 |
| `creator_id` | BIGINT | FK → user.id (CASCADE), NOT NULL | 创建人 ID |
| `created_at` | DATETIME | DEFAULT NOW | 创建时间 |
| `updated_at` | DATETIME | ON UPDATE NOW | 更新时间 |

### 7.3 外键关系汇总

| 子表 | 外键字段 | 父表 | 删除策略 | 更新策略 |
|------|---------|------|---------|---------|
| `club` | `leader_id` | `user.id` | SET NULL | CASCADE |
| `club_member` | `user_id` | `user.id` | CASCADE | CASCADE |
| `club_member` | `club_id` | `club.id` | CASCADE | CASCADE |
| `activity` | `club_id` | `club.id` | CASCADE | CASCADE |
| `activity` | `creator_id` | `user.id` | CASCADE | CASCADE |
| `activity_registration` | `activity_id` | `activity.id` | CASCADE | CASCADE |
| `activity_registration` | `user_id` | `user.id` | CASCADE | CASCADE |
| `activity_checkin` | `registration_id` | `activity_registration.id` | CASCADE | CASCADE |
| `activity_checkin` | `activity_id` | `activity.id` | CASCADE | CASCADE |
| `activity_checkin` | `user_id` | `user.id` | CASCADE | CASCADE |
| `activity_summary` | `activity_id` | `activity.id` | CASCADE | CASCADE |
| `activity_summary` | `creator_id` | `user.id` | CASCADE | CASCADE |

---

## 8. 系统实现

### 8.1 项目源代码结构

```
demo/
├── pom.xml                                         # Maven 构建配置 (Spring Boot 4.0.6)
├── sql/
│   └── club_system.sql                             # 数据库初始化脚本 + 种子数据
├── .mvn/
│   ├── jvm.config                                  # JVM 参数
│   └── wrapper/maven-wrapper.properties            # Maven Wrapper 配置 (3.9.15)
│
├── src/main/java/com/crisp/demo/
│   ├── DemoApplication.java                        # 启动类 (@EnableCaching + @EnableRedisHttpSession)
│   │
│   ├── common/                                     # 通用模块
│   │   ├── Result.java                             # 统一响应封装 {code, message, data}
│   │   ├── BusinessException.java                  # 自定义业务异常
│   │   └── GlobalExceptionHandler.java             # 全局异常处理 (@RestControllerAdvice)
│   │
│   ├── config/                                     # 配置模块
│   │   ├── AuthInterceptor.java                    # Session 认证拦截器 → 401 JSON
│   │   ├── CorsConfig.java                         # CORS 跨域配置
│   │   ├── WebConfig.java                          # 注册拦截器 & 参数解析器
│   │   ├── CacheConfig.java                        # Redis 缓存管理器 (JSON序列化, 30min TTL)
│   │   ├── CurrentUser.java                        # @CurrentUser 注解
│   │   └── CurrentUserArgumentResolver.java        # 自动注入当前登录用户
│   │
│   ├── entity/                                     # 实体层 (7 个)
│   │   ├── User.java                               # 用户实体 (含 Jakarta Validation)
│   │   ├── Club.java                               # 社团实体
│   │   ├── ClubMember.java                         # 社团成员实体
│   │   ├── Activity.java                           # 活动实体
│   │   ├── ActivityRegistration.java               # 活动报名实体
│   │   ├── ActivityCheckin.java                    # 签到签退实体
│   │   └── ActivitySummary.java                    # 活动总结实体
│   │
│   ├── mapper/                                     # 持久层 (7 个 MyBatis 注解接口)
│   │   ├── UserMapper.java
│   │   ├── ClubMapper.java
│   │   ├── ClubMemberMapper.java
│   │   ├── ActivityMapper.java
│   │   ├── ActivityRegistrationMapper.java
│   │   ├── ActivityCheckinMapper.java
│   │   └── ActivitySummaryMapper.java
│   │
│   ├── service/                                    # 服务接口 (7 个)
│   │   ├── UserService.java
│   │   ├── ClubService.java
│   │   ├── ClubMemberService.java
│   │   ├── ActivityService.java
│   │   ├── ActivityRegistrationService.java
│   │   ├── ActivityCheckinService.java
│   │   └── ActivitySummaryService.java
│   │
│   ├── service/impl/                               # 服务实现 (7 个)
│   │   ├── UserServiceImpl.java                    # MD5 密码加密, 登录校验
│   │   ├── ClubServiceImpl.java                    # @Cacheable 社团缓存
│   │   ├── ClubMemberServiceImpl.java              # 成员审批流程, 状态流转复用
│   │   ├── ActivityServiceImpl.java                # @Cacheable 活动缓存
│   │   ├── ActivityRegistrationServiceImpl.java    # 原子报名, 取消审批
│   │   ├── ActivityCheckinServiceImpl.java         # 签到签退防重
│   │   └── ActivitySummaryServiceImpl.java
│   │
│   └── controller/                                 # 控制层 (13 个, 分两组)
│       ├── admin/                                  # 管理端 Controller (6 个)
│       │   ├── AdminActivityController.java
│       │   ├── AdminClubController.java
│       │   ├── AdminMemberController.java
│       │   ├── AdminUserController.java
│       │   └── AdminSummaryController.java
│       └── portal/                                 # 学生端 Controller (7 个)
│           ├── PortalUserController.java           # 含静态页面路由 forward
│           ├── PortalClubController.java
│           ├── PortalActivityController.java
│           ├── PortalMemberController.java
│           ├── PortalRegistrationController.java
│           ├── PortalCheckinController.java
│           └── PortalSummaryController.java
│
├── src/main/resources/
│   ├── application.properties                      # DB/Redis/MyBatis/Session 配置
│   ├── applicationexample.properties               # 配置模板 (脱敏)
│   └── static/                                     # 纯 HTML 前端 (12 个文件)
│       ├── index.html                              # 首页
│       ├── login.html                              # 登录页
│       ├── register.html                           # 注册页
│       ├── css/style.css                           # 全局样式
│       ├── user/profile.html                       # 用户中心
│       ├── club/list.html                          # 社团列表
│       ├── club/detail.html                        # 社团详情
│       ├── member/manage.html                      # 成员管理
│       ├── activity/list.html                      # 活动列表
│       ├── activity/detail.html                    # 活动详情
│       ├── activity/manage.html                    # 活动管理
│       └── activity/create.html                    # 发布活动
│
├── admin/                                          # 管理端 Vue 3 SPA
│   ├── package.json                                # vue 3, vue-router 5, axios, vite 8
│   ├── vite.config.js                              # 开发代理 /api → :8081
│   └── src/
│       ├── main.js                                 # Vue 应用 bootstrap
│       ├── App.vue                                 # 根组件 (导航 + 页脚)
│       ├── style.css                               # 全局样式
│       ├── api/index.js                            # Axios 实例 + 401 拦截
│       ├── router/index.js                         # 7 条路由 + 登录守卫
│       └── views/                                  # 7 个视图组件
│           ├── Dashboard.vue                       # 数据面板 (统计 + 快捷操作)
│           ├── Login.vue                           # 管理员登录
│           ├── ClubManage.vue                      # 社团 CRUD 管理
│           ├── MemberManage.vue                    # 成员审批管理
│           ├── ActivityCreate.vue                  # 创建活动
│           ├── ActivityManage.vue                  # 活动管理
│           └── UserProfile.vue                     # 个人信息
│
├── frontend/                                       # 学生端 Vue 3 SPA
│   ├── package.json                                # vue 3, vue-router 5, axios, vite 8
│   ├── vite.config.js                              # 开发代理 /api → :8081
│   └── src/
│       ├── main.js                                 # Vue 应用 bootstrap
│       ├── App.vue                                 # 根组件 (导航)
│       ├── style.css                               # 全局样式
│       ├── api/index.js                            # Axios 实例 + 401 拦截
│       ├── router/index.js                         # 8 条路由 + 登录守卫
│       └── views/                                  # 9 个视图组件
│           ├── Home.vue                            # 首页
│           ├── Login.vue                           # 学生登录
│           ├── Register.vue                        # 学生注册
│           ├── ClubList.vue                        # 社团列表
│           ├── ClubDetail.vue                      # 社团详情 + 申请加入
│           ├── ActivityList.vue                    # 活动列表
│           ├── ActivityDetail.vue                  # 活动详情 + 报名/签到
│           ├── UserProfile.vue                     # 个人信息
│           └── Notifications.vue                   # 通知中心
│
└── src/test/java/com/crisp/demo/
    └── DemoApplicationTests.java                   # 基础上下文加载测试
```

### 8.2 关键实现细节

#### 8.2.1 密码安全

采用 **MD5 摘要算法**进行密码加密存储，结合 Spring 提供的 `DigestUtils`：

```java
String encryptedPassword = DigestUtils.md5DigestAsHex(
    password.getBytes(StandardCharsets.UTF_8));
```

登录时将用户输入的密码以相同方式加密后与数据库密文比对。

#### 8.2.2 统一响应格式

所有 API 返回统一的 JSON 结构 `Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

状态码约定：`200` 成功，`400` 参数校验失败，`401` 未登录，`500` 服务器错误。

#### 8.2.3 Redis 双用途

- **Spring Session**: 使用 `@EnableRedisHttpSession` 将 `HttpSession` 数据存储到 Redis，支持分布式部署和 30 分钟超时
- **Spring Cache**: 通过 `RedisCacheManager` 对社团和活动查询启用缓存 (`@Cacheable`/`@CacheEvict`)，JSON 序列化，30 分钟 TTL，禁止缓存 null

#### 8.2.4 全局异常处理

通过 `@RestControllerAdvice` 统一处理三层异常，消除 Controller 中的重复 `try-catch`：

| 异常类型 | HTTP 语义 | 返回 |
|---------|----------|------|
| `BusinessException` | 业务错误 | 200 + 错误消息 |
| `MethodArgumentNotValidException` | 参数校验失败 | 400 + 字段级错误 |
| `Exception` (兜底) | 系统错误 | 500 + "系统错误" |

#### 8.2.5 Session 认证拦截

`AuthInterceptor` 实现 `HandlerInterceptor`：
- 从 `HttpSession` 获取 `user` 属性
- 未登录返回 `{"code":401,"message":"请先登录"}` 的 JSON
- 已登录将 `User` 对象存入 `request.setAttribute("currentUser", user)`，供 `@CurrentUser` 解析
- `WebConfig` 中排除 17 个公开 API 路径（登录、注册、浏览社团/活动、签到记录查看等）

#### 8.2.6 @CurrentUser 参数注入

自定义注解 `@CurrentUser` + `HandlerMethodArgumentResolver` 实现 Controller 方法参数的自动注入：

```java
@PutMapping("/update")
public Result<?> update(@RequestBody User user,
                         @CurrentUser User currentUser) {
    // currentUser 为当前登录用户，无需手动从 Session 获取
}
```

#### 8.2.7 活动人数原子控制

报名时采用数据库级别的原子操作，避免并发超卖：

```sql
UPDATE activity
SET current_count = current_count + 1
WHERE id = #{id}
  AND (max_participants = 0 OR current_count < max_participants)
```

MyBatis 返回受影响行数，为 0 则说明名额已满。

#### 8.2.8 权限校验

采用前后端双重校验策略：
- **前端**: 仅发布人和管理员可见删除/编辑按钮
- **后端**: Service 和 Controller 层校验 `user.id === entity.creatorId || "ADMIN".equals(user.role)`

#### 8.2.9 MyBatis 全注解方式

所有数据库操作使用注解，零 XML 配置。联表查询使用 LEFT JOIN 自动填充展示字段：

```java
@Select("SELECT c.*, u.name AS leader_name FROM club c " +
        "LEFT JOIN user u ON c.leader_id = u.id " +
        "WHERE c.leader_id = #{leaderId}")
List<Club> findByLeaderId(Long leaderId);
```

INSERT 使用 `@Options(useGeneratedKeys = true, keyProperty = "id")` 自动回填主键。

#### 8.2.10 成员重新申请机制

`ClubMemberServiceImpl` 对已退出 (`QUIT`) 或被拒绝 (`REJECTED`) 的成员再次申请时，采用 **UPDATE 复用旧记录** 而非 INSERT 新记录，保持 `(user_id, club_id)` 唯一约束：

```sql
UPDATE club_member
SET status = 'PENDING', role = 'MEMBER',
    apply_reason = #{applyReason}, joined_at = NULL,
    quit_at = NULL, created_at = NOW()
WHERE id = #{id}
```

#### 8.2.11 取消审批工作流

取消报名支持两种模式：
- **直接取消**: 用户自行 → status 直接变为 `CANCELLED`
- **审批取消**: 用户申请 → status=`CANCEL_PENDING` → 管理员审批通过/拒绝

---

## 9. 部署与运行

## 9. 自动化测试

| 测试类 | 用例数 | 状态 | 覆盖模块 |
|--------|:---:|:---:|------|
| `UserServiceTest` | 11 | ✅ | 注册、重复注册、登录（成功/错误密码/不存在用户）、修改信息、修改密码、查找、搜索 |
| `ClubServiceTest` | 7 | ✅ | 创建、查询、列表、搜索、更新、删除 |
| `ClubMemberServiceTest` | 14 | ✅ | 申请、审批通过/拒绝、退出申请/审批、重新申请、状态流转 |
| `ActivityServiceTest` | 10 | ✅ | 创建、状态变更（DRAFT→ONGOING→FINISHED）、按社团/状态筛选、删除 |
| `ActivityRegistrationServiceTest` | 10 | ✅ | 报名、重复报名、取消申请/驳回/审批通过、重新报名 |
| **总计** | **53** | ✅ | 0 失败，0 错误 |

```bash
mvn test    # 运行全部 53 个用例
```

测试配置通过 `src/test/resources/application.properties` 禁用 Redis（`spring.cache.type=none`），使用 `TestDemoApplication` 启动类（不含 `@EnableCaching` / `@EnableRedisHttpSession`），避免测试依赖外部 Redis 服务。

---

## 10. 部署与运行

### 10.1 环境要求

| 组件 | 版本要求 |
|------|---------|
| JDK | 21+ |
| MySQL | 8.0+ |
| Redis | 7.x |
| Maven | 3.8+ (或使用项目自带 Maven Wrapper) |
| Node.js (仅前端开发) | 18+ |

### 10.2 快速启动

```bash
# 1. 初始化数据库
mysql -u root -p < sql/club_system.sql

# 2. 修改数据库连接（如需要）
# 编辑 src/main/resources/application.properties
# spring.datasource.password=你的密码
# spring.data.redis.password=你的Redis密码

# 3. 启动后端 (默认端口 8081)
./mvnw spring-boot:run        # Linux/Mac
mvnw.cmd spring-boot:run      # Windows

# 4. (可选) 启动管理端前端开发服务器
cd admin && npm install && npm run dev    # 默认端口 5174

# 5. (可选) 启动学生端前端开发服务器
cd frontend && npm install && npm run dev

# 6. 访问应用
# 纯 HTML 前端:    http://localhost:8081
# 管理端 Vue SPA:  http://localhost:5174
# 学生端 Vue SPA:  由 Vite 自动分配端口
```

### 10.3 测试账号

| 学号 | 密码 | 角色 | 说明 |
|------|------|------|------|
| `admin` | `123456` | 管理员 | 全局管理权限 |
| `2024001` | `123456` | 学生 | 测试学生张三 |
| `2024002` | `123456` | 学生 | 测试学生李四 |

---

## 11. API 接口清单

### 11.1 公开接口（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/login` | 用户登录 |
| POST | `/api/user/register` | 用户注册 |
| POST | `/api/user/logout` | 用户登出 |
| GET | `/api/user/current` | 检查登录状态 |
| GET | `/api/club/list` | 社团列表 |
| GET | `/api/club/search` | 社团搜索 |
| GET | `/api/club/{id}` | 社团详情 |
| GET | `/api/activity/list` | 活动列表 |
| GET | `/api/activity/search` | 活动搜索 |
| GET | `/api/activity/status/{status}` | 按状态筛活动 |
| GET | `/api/activity/club/{clubId}` | 按社团筛活动 |
| GET | `/api/activity/{id}` | 活动详情 |
| GET | `/api/summary/list` | 总结列表 |
| GET | `/api/summary/activity/{activityId}` | 按活动查看总结 |
| GET | `/api/summary/{id}` | 总结详情 |

### 11.2 需登录接口

其余所有 `/api/**` 路径均需登录认证，详见第 6.2 节功能模块图。

---

## 12. 待完善事项

| 序号 | 事项 | 说明 |
|:---:|------|------|
| 1 | ~~单元测试~~ | ✅ 已完成 5 套 53 个 Service 层用例，详见 `src/test/java/com/crisp/demo/service/` |
| 2 | 前端 History 模式 | Vue Router 使用 `createWebHistory()`，后端需配置 fallback 路由 |
| 3 | 密码加密升级 | MD5 已不推荐用于密码存储，建议升级为 BCrypt/Argon2 |
| 4 | 日志系统 | 建议引入 Logback 配置文件，取代默认日志级别 |
| 5 | API 文档 | 建议集成 SpringDoc OpenAPI (Swagger) 自动生成 API 文档 |
| 6 | Docker 支持 | 可添加 Dockerfile + docker-compose.yml 实现一键部署 |
| 7 | 管理端 Vue SPA 构建 | 需配置 `vite build` 输出到 `static/admin/` 目录 |
| 8 | 数据库建表语句 | `club` 表 `logo_url` 字段已从代码移除，`activity_checkin` 表待移除 |
