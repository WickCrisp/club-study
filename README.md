# 社团活动管理系统 — 项目文档报告

---

## 1. 系统定位

社团活动管理系统是一款面向高校学生社团的 Web 应用，旨在为**学生**和**社团管理者**提供一站式的社团事务管理平台。系统覆盖社团创建、成员管理、活动发布、活动报名、签到签退及活动总结等核心业务场景，帮助提升社团运营效率和管理规范性。

---

## 2. 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 4.0.6 |
| **ORM 框架** | MyBatis | 4.0.1 (Spring Boot Starter) |
| **数据库** | MySQL | 8.0.40 |
| **数据库连接池** | HikariCP | (Spring Boot 内置) |
| **Java 版本** | JDK 21 | 21.0.3 LTS |
| **构建工具** | Maven | Maven Wrapper |
| **前端** | 纯静态 HTML5 + CSS3 + ES6 | — |
| **UI 框架** | Bootstrap 5.3 | CDN |
| **图标库** | Bootstrap Icons 1.11 | CDN |
| **Web 服务器** | Apache Tomcat 11 | (Spring Boot 内置) |

### 架构模式

采用 **HTML + API 前后端分离轻量架构**：

```
浏览器 (HTML/JS)
    ↕  HTTP REST API (JSON)
Spring Boot Controller (@RestController)
    ↕
Service 层
    ↕
MyBatis Mapper (注解方式)
    ↕
MySQL 数据库
```

- 前端纯静态 HTML 部署于 `static/`，通过 `fetch` 调用 REST API
- Controller 返回 `forward:` 到静态页面，或返回 `@ResponseBody` JSON
- Mapper 全部使用 **MyBatis 注解**方式，无需 XML 配置文件

---

## 3. 核心成果

系统实现了 **16 项**功能：

| 序号 | 功能模块 | 功能数 |
|------|---------|:---:|
| 1 | 用户信息管理 | 注册、登录、个人信息查询与修改、密码修改 | 4 |
| 2 | 社团信息管理 | 创建社团、列表/搜索、详情查看、编辑、删除 | 5 |
| 3 | 社团成员管理 | 申请加入、审批通过/拒绝、退出、查看成员列表 | 4 |
| 4 | 活动发布管理 | 创建活动、活动列表/搜索、查看详情、编辑、删除（仅发布人/管理员） | 5 |
| 5 | 活动报名管理 | 用户报名、取消报名、人数限制控制、报名列表 | 4 |
| 6 | 活动签到管理 | 签到、签退、签到记录查询 | 3 |
| 7 | 活动总结管理 | 创建总结、编辑总结、查看总结 | 3 |

---

## 4. 用户角色分析

| 角色 | 代码 | 核心需求 | 权限范围 |
|------|------|----------|----------|
| **学生** | `STUDENT` | 注册登录、加入社团、报名活动、签到签退、管理个人信息 | 查看所有社团/活动；管理自己的报名和签到 |
| **管理员** | `ADMIN` | 全局管理权限，可删除任意用户发布的活动 | 学生全部权限 + 删除他人活动 |

### 权限矩阵

| 操作 | 学生 | 社团负责人 | 管理员 |
|------|:---:|:---:|:---:|
| 创建社团 | ✅ | ✅ | ✅ |
| 审批成员 | ❌ | ✅ | ✅ |
| 发布活动 | ✅ | ✅ | ✅ |
| 删除自己的活动 | ✅ | ✅ | ✅ |
| 删除别人的活动 | ❌ | ❌ | ✅ |

---

## 5. 功能性需求（用例表）

### 5.1 用户注册 (UC-01)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生 |
| **前置条件** | 未登录状态 |
| **基本事件流** | 1. 进入注册页面 2. 填写学号、姓名、密码、手机号、邮箱、性别 3. 点击注册 4. 系统校验学号唯一性 5. MD5 加密存储密码 6. 自动跳转登录页 |
| **后置条件** | 用户记录写入数据库 |

### 5.2 用户登录 (UC-02)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生、管理员 |
| **前置条件** | 已注册 |
| **基本事件流** | 1. 进入登录页面 2. 输入学号和密码 3. 系统 MD5 加密后比对 4. 成功后存入 Session，跳转首页 5. 导航栏显示用户名 |
| **后置条件** | HttpSession 中存入当前用户对象 |

### 5.3 社团创建 (UC-03)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生、管理员 |
| **前置条件** | 已登录 |
| **基本事件流** | 1. 点击创建社团 2. 填写名称、简介、负责人ID 3. 提交 → 写入数据库 |
| **后置条件** | 社团记录创建，默认状态为 ACTIVE |

### 5.4 加入社团 (UC-04)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生 |
| **前置条件** | 已登录，未加入该社团 |
| **基本事件流** | 1. 进入社团详情页 2. 点击"申请加入" 3. 可选填写申请理由 4. 提交后状态为 PENDING |
| **后置条件** | 产生待审批成员记录 |

### 5.5 审批成员 (UC-05)

| 项目 | 描述 |
|------|------|
| **参与角色** | 社团负责人 |
| **前置条件** | 自己是该社团负责人，有待审批申请 |
| **基本事件流** | 1. 进入"我管理的社团" 2. 查看待审批列表 3. 点击"通过"或"拒绝" 4. 通过时自动设置 joined_at |
| **后置条件** | 成员状态更新为 APPROVED 或 REJECTED |

### 5.6 发布活动 (UC-06)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生、管理员 |
| **前置条件** | 已登录 |
| **基本事件流** | 1. 进入发布活动页 2. 选择所属社团 3. 填写标题、描述、地点、时间、人数限制 4. 选择草稿或直接发布 5. 提交 |
| **后置条件** | 活动记录创建 |

### 5.7 活动报名 (UC-07)

| 项目 | 描述 |
|------|------|
| **参与角色** | 学生 |
| **前置条件** | 已登录，活动状态为 PUBLISHED 或 ONGOING |
| **基本事件流** | 1. 进入活动详情页 2. 点击"立即报名" 3. 系统检查人数限制 4. 报名成功，自动创建签到记录 |
| **后置条件** | 报名记录写入；活动 current_count +1；签到记录创建 |

### 5.8 活动签到 (UC-08)

| 项目 | 描述 |
|------|------|
| **参与角色** | 已报名学生 |
| **前置条件** | 已报名该活动，未签到 |
| **基本事件流** | 1. 进入活动详情页 2. 点击"签到" 3. 系统记录签到时间 |
| **后置条件** | checkin_time 和 checkin_status 更新 |

### 5.9 活动签退 (UC-09)

| 项目 | 描述 |
|------|------|
| **参与角色** | 已报名学生 |
| **前置条件** | 已签到，未签退 |
| **基本事件流** | 1. 进入活动详情页 2. 点击"签退" 3. 系统记录签退时间 |
| **后置条件** | checkout_time 和 checkout_status 更新 |

### 5.10 活动总结 (UC-10)

| 项目 | 描述 |
|------|------|
| **参与角色** | 社团成员/负责人 |
| **前置条件** | 已登录，活动存在 |
| **基本事件流** | 1. 在活动详情页点击"添加总结" 2. 填写活动内容、效果评估、改进建议 3. 提交 |
| **后置条件** | 总结记录写入 |

---

## 6. 系统设计

### 6.1 系统架构图

```
┌─────────────────────────────────────────────────────┐
│                     表现层 (View)                     │
│  static/ *.html + Bootstrap 5 + Bootstrap Icons      │
│  index.html  login.html  register.html               │
│  user/profile.html  club/list.html  club/detail.html │
│  member/manage.html  activity/manage.html            │
│  activity/create.html  activity/detail.html          │
└───────────────┬─────────────────────────────────────┘
                │  fetch() JSON API
┌───────────────▼─────────────────────────────────────┐
│                   控制层 (Controller)                  │
│  UserController  ClubController  ClubMemberController│
│  ActivityController  ActivityRegistrationController  │
│  ActivityCheckinController  ActivitySummaryController│
│  @Controller + @ResponseBody                         │
└───────────────┬─────────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────────┐
│                   服务层 (Service)                     │
│  UserService/Impl  ClubService/Impl                  │
│  ClubMemberService/Impl  ActivityService/Impl        │
│  ActivityRegistrationService/Impl                    │
│  ActivityCheckinService/Impl                         │
│  ActivitySummaryService/Impl                         │
│  @Service + @Transactional                           │
└───────────────┬─────────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────────┐
│                 持久层 (Mapper)                       │
│  7 个 Mapper 接口 + @Select/@Insert/@Update/@Delete  │
│  MyBatis 注解方式，零 XML 配置                         │
└───────────────┬─────────────────────────────────────┘
                │
┌───────────────▼─────────────────────────────────────┐
│              数据库 (MySQL 8.0)                       │
│  7 张表：user、club、club_member、activity、           │
│  activity_registration、activity_checkin、            │
│  activity_summary                                    │
└─────────────────────────────────────────────────────┘
```

### 6.2 功能模块图

```
社团活动管理系统
│
├── 用户模块 (UserController)
│   ├── 注册 /register
│   ├── 登录 /login
│   ├── 注销 /logout
│   ├── 个人信息 /user/profile
│   │   ├── 查看信息
│   │   ├── 修改信息
│   │   └── 修改密码
│   └── 用户管理 API (CRUD)
│
├── 社团模块 (ClubController + ClubMemberController)
│   ├── 社团管理
│   │   ├── 社团列表 /club/list
│   │   ├── 社团详情 /club/detail/{id}
│   │   ├── 创建社团
│   │   └── 编辑/删除社团
│   └── 成员管理 /member/manage
│       ├── 已有社团目录
│       │   └── 申请加入
│       └── 我管理的社团
│           ├── 审批加入申请
│           └── 查看成员列表
│
├── 活动模块 (ActivityController)
│   ├── 活动管理 /activity/manage
│   │   ├── 已有活动目录
│   │   └── 我发布的活动（含报名列表+签到记录）
│   ├── 发布活动 /activity/create
│   ├── 活动详情 /activity/detail/{id}
│   │   ├── 报名/取消报名
│   │   ├── 签到/签退
│   │   └── 活动总结
│   └── 删除活动（仅发布人/管理员）
│
├── 报名模块 (ActivityRegistrationController)
│   ├── 报名活动
│   ├── 取消报名
│   └── 报名列表查询
│
├── 签到模块 (ActivityCheckinController)
│   ├── 签到
│   ├── 签退
│   └── 签到记录查询
│
└── 总结模块 (ActivitySummaryController)
    ├── 创建总结
    ├── 编辑总结
    └── 查询总结
```

---

## 7. 数据库设计

### 7.1 E-R 图

```
  ┌──────────┐         ┌──────────────┐         ┌──────────┐
  │   user   │ 1 ─── N │ club_member  │ N ─── 1 │   club   │
  │          │         │  user_id      │        │          │
  │ id (PK)  │         │  club_id      │        │ id (PK)  │
  │ student_no│        │  role         │        │ name     │
  │ password │         │  status       │        │ leader_id│
  │ name     │         │  apply_reason │        │ status   │
  │ phone    │         └──────────────┘        └──────────┘
  │ email    │                                         │
  │ gender   │          ┌──────────────────┐           │
  │ role     │ 1 ─── N │ activity          │ N ─── 1 ──┘
  └──────────┘         │  club_id (FK)     │
       │               │  creator_id (FK)  ─────────────┘
       │               │  title, location  │
       │               │  start/end_time   │
       │               │  max_participants │
       │               │  current_count    │
       │               └──┬───────────────┘
       │                  │ 1
       │                  │
       │     ┌────────────▼────────────────┐
       │     │ activity_registration        │
       │     │  activity_id (FK)            │
       ├─────│  user_id (FK)               │
       │     │  status (REGISTERED/CANCEL)  │
       │     └────────────┬────────────────┘
       │                  │ 1
       │                  │
       │     ┌────────────▼────────┐
       │     │ activity_checkin     │
       └─────│  registration_id(FK) │
             │  activity_id (FK)    │
             │  user_id (FK)        │
             │  checkin/out_status  │
             └─────────────────────┘

  ┌──────────┐         ┌──────────────────┐
  │   user   │ 1 ─── N │ activity_summary  │
  └──────────┘         │  activity_id (FK) │
                       │  content/effect   │
                       │  creator_id (FK)  │
                       └──────────────────┘
```

### 7.2 表结构

#### 7.2.1 用户信息表 (user)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 用户 ID |
| `student_no` | VARCHAR(20) | NOT NULL, UNIQUE | 学号（登录账号） |
| `password` | VARCHAR(255) | NOT NULL | MD5 加密密码 |
| `name` | VARCHAR(50) | NOT NULL | 真实姓名 |
| `phone` | VARCHAR(20) | — | 联系电话 |
| `email` | VARCHAR(100) | — | 电子邮箱 |
| `gender` | TINYINT | DEFAULT 0 | 0未知 1男 2女 |
| `role` | VARCHAR(20) | DEFAULT 'STUDENT' | STUDENT/ADMIN |
| `created_at` | DATETIME | DEFAULT NOW | 注册时间 |
| `updated_at` | DATETIME | ON UPDATE | 更新时间 |

#### 7.2.2 社团信息表 (club)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK | 社团 ID |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE | 社团名称 |
| `description` | TEXT | — | 简介 |
| `logo_url` | VARCHAR(255) | — | Logo 地址 |
| `leader_id` | BIGINT | FK→user.id | 负责人 ID |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | ACTIVE/DISBANDED |

#### 7.2.3 社团成员表 (club_member)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK | — |
| `user_id` | BIGINT | FK→user.id, NOT NULL | 学生 ID |
| `club_id` | BIGINT | FK→club.id, NOT NULL | 社团 ID |
| `role` | VARCHAR(20) | DEFAULT 'MEMBER' | LEADER/VICE_LEADER/MEMBER |
| `status` | VARCHAR(20) | DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| `apply_reason` | VARCHAR(500) | — | 申请理由 |
| `joined_at` | DATETIME | — | 加入时间 |
| `quit_at` | DATETIME | — | 退出时间 |

#### 7.2.4 活动信息表 (activity)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK | — |
| `club_id` | BIGINT | FK→club.id | 所属社团 |
| `title` | VARCHAR(200) | NOT NULL | 活动名称 |
| `description` | TEXT | — | 简介 |
| `location` | VARCHAR(200) | NOT NULL | 地点 |
| `start_time` | DATETIME | NOT NULL | 开始时间 |
| `end_time` | DATETIME | NOT NULL | 结束时间 |
| `max_participants` | INT | DEFAULT 0 | 人数限制（0=不限） |
| `current_count` | INT | DEFAULT 0 | 已报名人数 |
| `status` | VARCHAR(20) | DEFAULT 'DRAFT' | DRAFT/PUBLISHED/ONGOING/FINISHED/CANCELLED |
| `creator_id` | BIGINT | FK→user.id | 发布人 ID |

#### 7.2.5 活动报名表 (activity_registration)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK | — |
| `activity_id` | BIGINT | FK→activity.id | 活动 ID |
| `user_id` | BIGINT | FK→user.id | 报名学生 ID |
| `status` | VARCHAR(20) | DEFAULT 'REGISTERED' | REGISTERED/CANCELLED |
| `registered_at` | DATETIME | DEFAULT NOW | 报名时间 |
| `cancelled_at` | DATETIME | — | 取消时间 |
| *(唯一约束)* | `(activity_id, user_id)` | UNIQUE | — |

#### 7.2.6 活动签到签退表 (activity_checkin)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK | — |
| `registration_id` | BIGINT | FK, UNIQUE | 关联报名记录 |
| `activity_id` | BIGINT | FK, 冗余 | 活动 ID |
| `user_id` | BIGINT | FK, 冗余 | 学生 ID |
| `checkin_time` | DATETIME | — | 签到时间 |
| `checkin_status` | VARCHAR(20) | DEFAULT 'UNCHECKED' | — |
| `checkout_time` | DATETIME | — | 签退时间 |
| `checkout_status` | VARCHAR(20) | DEFAULT 'UNCHECKED' | — |

#### 7.2.7 活动总结表 (activity_summary)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK | — |
| `activity_id` | BIGINT | FK→activity.id | 活动 ID |
| `content` | TEXT | NOT NULL | 内容总结 |
| `effect` | TEXT | — | 效果评估 |
| `suggestions` | TEXT | — | 改进建议 |
| `images` | VARCHAR(2000) | — | 图片 URL |
| `creator_id` | BIGINT | FK→user.id | 创建人 ID |

键盘关系：
- `club.leader_id` → `user.id` (SET NULL)
- `club_member.user_id` → `user.id` (CASCADE)
- `club_member.club_id` → `club.id` (CASCADE)
- `activity.club_id` → `club.id` (CASCADE)
- `activity.creator_id` → `user.id` (CASCADE)
- `activity_registration.activity_id` → `activity.id` (CASCADE)
- `activity_registration.user_id` → `user.id` (CASCADE)
- `activity_checkin.registration_id` → `activity_registration.id` (CASCADE)
- `activity_summary.activity_id` → `activity.id` (CASCADE)
- `activity_summary.creator_id` → `user.id` (CASCADE)

---

## 8. 系统实现

### 8.1 项目结构

```
demo/
├── pom.xml                                  # Maven 配置
├── sql/club_system.sql                      # 数据库初始化脚本
├── src/main/java/com/crisp/demo/
│   ├── DemoApplication.java                 # 启动类
│   ├── common/
│   │   └── Result.java                      # 统一响应封装 {code, message, data}
│   ├── entity/                              # 7 个实体
│   │   ├── User.java
│   │   ├── Club.java
│   │   ├── ClubMember.java
│   │   ├── Activity.java
│   │   ├── ActivityRegistration.java
│   │   ├── ActivityCheckin.java
│   │   └── ActivitySummary.java
│   ├── mapper/                              # 7 个 MyBatis 注解 Mapper
│   ├── service/                             # 7 个接口 + 7 个实现类
│   └── controller/                          # 7 个 Controller
└── src/main/resources/
    ├── application.properties               # 数据源 + MyBatis 配置
    └── static/                              # 前端页面
        ├── index.html                       # 首页
        ├── login.html                       # 登录页
        ├── register.html                    # 注册页
        ├── css/style.css                    # 全局样式
        ├── user/profile.html                # 用户中心
        ├── club/list.html                   # 社团列表
        ├── club/detail.html                 # 社团详情
        ├── member/manage.html               # 成员管理（书页布局）
        ├── activity/manage.html             # 活动管理（书页布局）
        ├── activity/list.html               # 活动列表
        ├── activity/create.html             # 发布活动
        └── activity/detail.html             # 活动详情
```

### 8.2 关键实现细节

#### 8.2.1 密码加密

采用 **MD5 摘要算法**进行密码加密存储：

```java
String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
```

#### 8.2.2 统一响应格式

所有 API 返回统一的 JSON 结构：

```json
{
  "code": 200,       // 200 成功，401 未登录，403 无权限，500 错误
  "message": "操作成功",
  "data": { ... }
}
```

#### 8.2.3 Session 管理

使用 `HttpSession` 进行用户身份维持，登录时将 `User` 对象存入 Session：

```java
session.setAttribute("user", user);
```

#### 8.2.4 活动人数控制

报名时采用 DATABASE 级别的原子操作：

```java
@Update("UPDATE activity SET current_count = current_count + 1 
         WHERE id = #{id} AND (max_participants = 0 OR current_count < max_participants)")
int incrementCount(Long id);
```

#### 8.2.5 权限校验

删除活动时前后端双重校验：

- **前端**: 仅发布人和管理员可见删除按钮
- **后端**: Controller 中校验 `user.id === activity.creatorId || user.role === 'ADMIN'`

#### 8.2.6 MyBatis 全注解方式

所有数据库操作使用注解，零 XML 配置：

```java
@Select("SELECT c.*, u.name AS leader_name FROM club c 
         LEFT JOIN user u ON c.leader_id = u.id WHERE c.leader_id = #{leaderId}")
List<Club> findByLeaderId(Long leaderId);
```

---

## 9. 部署与运行

### 环境要求

- JDK 21
- MySQL 8.0
- Maven 3.8+

### 运行步骤

```bash
# 1. 执行数据库脚本
mysql -u root -p < sql/club_system.sql

# 2. 修改 application.properties 中的数据库密码

# 3. 启动项目
mvnw spring-boot:run

# 4. 访问
http://localhost:8081
```

### 测试账号

| 学号 | 密码 | 角色 |
|------|------|------|
| `admin` | `123456` | 管理员 |
| `2024001` | `123456` | 学生 |
| `2024002` | `123456` | 学生 |
