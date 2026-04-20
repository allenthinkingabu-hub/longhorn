# 错题分析器 MVP — 微信小程序 + Spring Boot

## 项目概述

构建一个 MVP 应用：用户通过 **微信小程序** 拍照/选择错题图片上传，**Spring Boot 后端** 接收图片后调用 **Spring AI（多模态视觉模型）** 分析错题内容（识别题目、错误原因、正确解法），并将分析结果返回给用户展示，同时持久化到 **PostgreSQL**。

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | 微信小程序（原生） | 基础库 ≥ 2.25.0 |
| 后端框架 | Spring Boot | 3.3.x |
| AI 集成 | Spring AI + OpenAI Starter | BOM 1.1.x |
| 视觉模型 | GPT-4o（可配置替换） | — |
| 数据库 | PostgreSQL | 15+ |
| ORM | Spring Data JPA + Hibernate | — |
| 文件存储 | 本地磁盘（MVP，后续可换 OSS） | — |
| 构建工具 | Maven | 3.9+ |

> [!IMPORTANT]
> **大模型选择**：默认使用 OpenAI GPT-4o（支持视觉多模态）。如果你希望用其他模型（如 Google Gemini、通义千问、百度文心等），请告知，我会调整 starter 依赖和配置。

---

## 项目目录结构

```
longfeng/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml
│   ├── src/main/java/com/longfeng/wrongbook/
│   │   ├── WrongBookApplication.java        # 启动类
│   │   ├── config/
│   │   │   └── AiConfig.java                # Spring AI 配置
│   │   ├── controller/
│   │   │   └── QuestionController.java      # REST API
│   │   ├── service/
│   │   │   ├── ImageAnalysisService.java    # AI 分析服务
│   │   │   └── QuestionService.java         # 业务逻辑
│   │   ├── model/
│   │   │   ├── entity/
│   │   │   │   └── WrongQuestion.java       # JPA 实体
│   │   │   └── dto/
│   │   │       ├── AnalysisResult.java      # AI 返回结构化结果
│   │   │       └── QuestionResponse.java    # API 响应 DTO
│   │   └── repository/
│   │       └── WrongQuestionRepository.java # JPA Repository
│   └── src/main/resources/
│       ├── application.yml                   # 应用配置
│       └── schema.sql                        # 数据库建表脚本
│
├── miniprogram/                      # 微信小程序前端
│   ├── app.js
│   ├── app.json
│   ├── app.wxss
│   ├── project.config.json
│   └── pages/
│       ├── index/                    # 首页（上传 + 历史列表）
│       │   ├── index.js
│       │   ├── index.wxml
│       │   ├── index.wxss
│       │   └── index.json
│       └── detail/                   # 详情页（分析结果展示）
│           ├── detail.js
│           ├── detail.wxml
│           ├── detail.wxss
│           └── detail.json
│
└── README.md
```

---

## 数据库设计

### `wrong_questions` 表

```sql
CREATE TABLE IF NOT EXISTS wrong_questions (
    id              BIGSERIAL PRIMARY KEY,
    image_path      VARCHAR(500) NOT NULL,          -- 图片存储路径
    subject         VARCHAR(50),                     -- 学科（AI 识别）
    question_text   TEXT,                            -- 题目原文（AI 识别）
    wrong_answer    TEXT,                            -- 错误答案（AI 识别）
    correct_answer  TEXT,                            -- 正确答案（AI 给出）
    analysis        TEXT,                            -- 详细解析（AI 给出）
    knowledge_point VARCHAR(200),                    -- 涉及知识点（AI 识别）
    difficulty      VARCHAR(20),                     -- 难度评级
    status          VARCHAR(20) DEFAULT 'ANALYZING', -- ANALYZING / COMPLETED / FAILED
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

> [!NOTE]
> MVP 阶段不做用户系统，所有记录共享。如需多用户，后续加 `user_id` 字段即可。

---

## API 设计

### 1. 上传图片并触发分析

```
POST /api/questions/upload
Content-Type: multipart/form-data
参数: file (图片文件)

响应 (200):
{
  "id": 1,
  "status": "ANALYZING",
  "message": "图片已上传，正在分析中..."
}
```

> [!IMPORTANT]
> **同步 vs 异步**：MVP 阶段采用 **同步调用**（请求等待 AI 返回后再响应）。AI 分析通常需要 5-15 秒，小程序端需要做 loading 提示。如果你希望用异步（先返回 ID，轮询结果），请告知。

### 2. 查询分析结果

```
GET /api/questions/{id}

响应 (200):
{
  "id": 1,
  "subject": "数学",
  "questionText": "求解方程 2x + 3 = 7",
  "wrongAnswer": "x = 3",
  "correctAnswer": "x = 2",
  "analysis": "将 3 移到等号右边得 2x = 4，两边除以 2 得 x = 2。错误原因：减法计算错误。",
  "knowledgePoint": "一元一次方程",
  "difficulty": "简单",
  "status": "COMPLETED",
  "createdAt": "2026-04-18T03:10:00"
}
```

### 3. 查询历史记录列表

```
GET /api/questions?page=0&size=20

响应 (200):
{
  "content": [ ... ],
  "totalElements": 42,
  "totalPages": 3
}
```

---

## 后端核心实现

### 1. Spring AI 图片分析服务

```java
@Service
public class ImageAnalysisService {

    private final ChatClient chatClient;

    public ImageAnalysisService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public AnalysisResult analyzeImage(Resource imageResource) {
        String prompt = """
            你是一位资深教育专家。请分析这张错题图片，识别以下信息并以 JSON 格式返回：
            1. subject: 学科名称
            2. questionText: 题目原文
            3. wrongAnswer: 图中的错误答案
            4. correctAnswer: 正确答案
            5. analysis: 详细的解题过程和错误原因分析
            6. knowledgePoint: 涉及的知识点
            7. difficulty: 难度（简单/中等/困难）
            """;

        return chatClient.prompt()
            .user(u -> u.text(prompt)
                        .media(MimeTypeUtils.IMAGE_PNG, imageResource))
            .call()
            .entity(AnalysisResult.class);
    }
}
```

### 2. AnalysisResult DTO（结构化输出）

```java
public record AnalysisResult(
    String subject,
    String questionText,
    String wrongAnswer,
    String correctAnswer,
    String analysis,
    String knowledgePoint,
    String difficulty
) {}
```

### 3. Controller 层

```java
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @PostMapping("/upload")
    public QuestionResponse upload(@RequestParam("file") MultipartFile file) { ... }

    @GetMapping("/{id}")
    public QuestionResponse getById(@PathVariable Long id) { ... }

    @GetMapping
    public Page<QuestionResponse> list(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) { ... }
}
```

---

## 微信小程序前端

### 页面结构

| 页面 | 功能 |
|------|------|
| **首页 (index)** | 拍照/选择图片上传按钮 + 历史错题列表 |
| **详情页 (detail)** | 展示原图 + AI 分析结果（题目、错因、正解、知识点） |

### 首页核心逻辑

```javascript
// 选择图片
wx.chooseMedia({
  count: 1,
  mediaType: ['image'],
  sourceType: ['album', 'camera'],
  success(res) {
    const tempFilePath = res.tempFiles[0].tempFilePath;
    // 上传到后端
    wx.uploadFile({
      url: `${baseUrl}/api/questions/upload`,
      filePath: tempFilePath,
      name: 'file',
      success(uploadRes) {
        const data = JSON.parse(uploadRes.data);
        // 跳转到详情页
        wx.navigateTo({ url: `/pages/detail/detail?id=${data.id}` });
      }
    });
  }
});
```

### 详情页展示

- 顶部：上传的原图
- 中间卡片区域：
  - 📚 学科 + 知识点标签
  - 📝 题目原文
  - ❌ 错误答案（红色标注）
  - ✅ 正确答案（绿色标注）
  - 💡 详细解析
  - ⭐ 难度等级

---

## 需要用户确认的决策点

> [!WARNING]
> 以下问题会影响实现方案，请确认或调整：

### 1. 大模型选择
- **当前方案**：使用 OpenAI GPT-4o（需要 OpenAI API Key）
- **替代方案**：
  - Google Gemini（`spring-ai-vertex-ai-gemini-spring-boot-starter`）
  - 阿里通义千问（需要自定义 HTTP 调用）
  - 本地部署 Ollama + LLaVA（`spring-ai-ollama-spring-boot-starter`）
- **请确认你打算使用哪个模型？**

### 2. 同步 vs 异步处理
- **当前方案**：同步处理（用户等待 5-15 秒，小程序显示 loading）
- **替代方案**：异步处理（立即返回，后台处理，小程序轮询/WebSocket）
- MVP 建议先用同步，简单直接。**是否同意？**

### 3. 用户体系
- **当前方案**：MVP 不做用户登录，所有数据共享
- **后续可加**：微信登录（`wx.login` + openid）
- **是否 MVP 就需要区分用户？**

### 4. 图片存储
- **当前方案**：后端本地磁盘存储（`uploads/` 目录）
- **替代方案**：阿里云 OSS / 腾讯 COS
- MVP 用本地存储即可。**是否同意？**

### 5. 开发环境
- **Java 版本**：Java 17（Spring Boot 3 最低要求）
- **PostgreSQL**：你本地是否已安装？或需要用 Docker？
- **微信开发者工具**：是否已安装？

---

## 开放问题

> [!CAUTION]
> 微信小程序需要在微信公众平台配置**服务器域名**（uploadFile 合法域名），且必须是 HTTPS。
> - **本地开发**：可以在微信开发者工具中勾选「不校验合法域名」来绕过
> - **线上部署**：需要域名 + SSL 证书 + 备案

---

## 验证计划

### 自动化测试
1. 后端单元测试：`ImageAnalysisService` mock AI 响应测试
2. 后端集成测试：上传接口 + 数据库读写
3. `mvn test` 验证全部测试通过

### 手动验证
1. 启动 PostgreSQL + Spring Boot 后端
2. 用 curl/Postman 测试上传接口
3. 微信开发者工具中运行小程序，完成拍照→上传→查看分析结果的完整流程
4. 验证 PostgreSQL 中数据正确持久化
