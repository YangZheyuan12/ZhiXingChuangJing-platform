# AI 助手功能 - 快速开始指南

## 📋 集成清单

以下是为智行创景平台添加 **Deepseek AI 助手**所做的所有修改：

### ✅ 前端文件（Frontend）

| 文件路径 | 说明 | 状态 |
|---------|------|------|
| `src/api/modules/chat.ts` | 聊天API模块 | ✅ 新建 |
| `src/components/common/AIAssistant.vue` | AI助手浮窗组件 | ✅ 新建 |
| `src/layouts/AppLayout.vue` | 主布局（已集成AI助手） | ✅ 修改 |

### ✅ 后端文件（Backend）

#### 实体类（Entity）
| 文件路径 | 说明 | 状态 |
|---------|------|------|
| `src/main/java/.../entity/ChatMessageEntity.java` | 聊天消息实体 | ✅ 新建 |
| `src/main/java/.../entity/ChatConversationEntity.java` | 对话实体 | ✅ 新建 |

#### 数据访问层（Repository）
| 文件路径 | 说明 | 状态 |
|---------|------|------|
| `src/main/java/.../repository/ChatMessageRepository.java` | 消息Repository | ✅ 新建 |
| `src/main/java/.../repository/ChatConversationRepository.java` | 对话Repository | ✅ 新建 |

#### 模型类（Model）
| 文件路径 | 说明 | 状态 |
|---------|------|------|
| `src/main/java/.../model/request/ChatRequests.java` | 请求模型 | ✅ 新建 |
| `src/main/java/.../model/response/ChatResponses.java` | 响应模型 | ✅ 新建 |

#### 业务逻辑层（Service）
| 文件路径 | 说明 | 状态 |
|---------|------|------|
| `src/main/java/.../service/ChatService.java` | 逻辑接口 | ✅ 新建 |
| `src/main/java/.../service/impl/ChatServiceImpl.java` | 逻辑实现 | ✅ 新建 |

#### 控制层（Controller）
| 文件路径 | 说明 | 状态 |
|---------|------|------|
| `src/main/java/.../controller/ChatController.java` | 聊天控制器 | ✅ 新建 |

#### 配置（Config）
| 文件路径 | 说明 | 状态 |
|---------|------|------|
| `src/main/java/.../common/config/RestTemplateConfig.java` | RestTemplate配置 | ✅ 新建 |
| `src/main/resources/application.yml` | 应用配置（已加入Deepseek） | ✅ 修改 |

### ✅ 数据库文件（Database）

| 文件路径 | 说明 | 状态 |
|---------|------|------|
| `deploy/mysql/chat-init.sql` | 建表SQL脚本 | ✅ 新建 |

### ✅ 文档文件（Documentation）

| 文件路径 | 说明 | 状态 |
|---------|------|------|
| `docs/AI_ASSISTANT_GUIDE.md` | 完整功能说明文档 | ✅ 新建 |
| `docs/AI_ASSISTANT_QUICKSTART.md` | 快速开始指南（本文件） | ✅ 新建 |

---

## 🚀 快速开始

### 第1步：数据库配置

数据库表会通过 Hibernate 自动创建，无需手动操作。
（如需手动建表，可参考 `deploy/mysql/chat-init.sql`）

### 第2步：环境配置

在启动应用前，设置 Deepseek API 密钥：

**方式A：环境变量（推荐）**
```bash
# Windows (命令行)
set DEEPSEEK_API_KEY=sk-0421479e732446ffb4aa6b4f4d6cb2f2

# Linux/Mac
export DEEPSEEK_API_KEY=sk-0421479e732446ffb4aa6b4f4d6cb2f2
```

**方式B：修改配置文件**
编辑 `backend/src/main/resources/application-prod.yml`：
```yaml
deepseek:
  api:
    key: sk-0421479e732446ffb4aa6b4f4d6cb2f2
    url: https://api.deepseek.com/chat/completions
```

### 第3步：后端构建和启动

```bash
# 进入后端目录
cd backend

# Maven 构建
mvn clean package

# 启动应用
mvn spring-boot:run

# 或使用 JAR
java -jar target/platform-backend-0.0.1-SNAPSHOT.jar
```

应用启动后，访问：
- API 文档：http://localhost:8080/swagger-ui.html
- 聊天 API：http://localhost:8080/api/v1/chat/conversation

### 第4步：前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端会在 http://localhost:5173 启动

### 第5步：使用 AI 助手

1. 打开浏览器，访问 http://localhost:5173
2. 使用现有账号登录
3. 看右下角的蓝色浮动按钮 💬 - 点击打开 AI 助手
4. 输入问题，按 Enter 或点击发送按钮
5. 等待 Deepseek AI 的回复！

---

## 📱 UI 预览

### AI 助手浮窗
- **位置**：屏幕右下角
- **状态**：展开/最小化切换
- **特性**：
  - ✨ 实时对话
  - 💾 自动保存聊天记录
  - 📱 完全响应式（手机端适配）
  - ⌨️ 支持 Enter 快速发送
  - ⏱️ 显示时间戳

---

## 🔧 API 端点速览

### 聊天相关

| 方法 | 端点 | 说明 |
|------|------|------|
| POST | `/api/v1/chat/conversation` | 创建新对话 |
| POST | `/api/v1/chat/message` | 发送消息并获取回复 |
| GET | `/api/v1/chat/history/{conversationId}` | 获取对话历史 |
| GET | `/api/v1/chat/conversations` | 获取所有对话 |
| DELETE | `/api/v1/chat/conversation/{conversationId}` | 删除对话 |

所有端点需要 Bearer Token 认证。

---

## 🐛 调试

### 查看服务器日志
```bash
# 后端日志会打印 API 调用情况
# 查找关键词：ChatServiceImpl, ChatController
```

### 前端浏览器控制台
按 `F12` 打开开发者工具，在 Console 标签可看到：
- API 请求/响应
- 错误信息
- 调试日志

### 常见问题排查

**问题**：AI 没有回复
- ☑️ 检查网络连接
- ☑️ 检查 API Key 是否正确
- ☑️ 查看后端日志
- ☑️ 确保 Deepseek 账户有余额

**问题**：页面提示 401 Unauthorized
- ☑️ 确保已登录
- ☑️ 检查 Token 是否过期
- ☑️ 注销后重新登录

**问题**：样式错乱
- ☑️ 清除浏览器缓存
- ☑️ 重新运行 `npm run build`

---

## 📦 文件结构总览

```
ZhiXingChuangJing-platform/
├── backend/
│   └── src/main/java/com/zhixingchuangjing/platform/
│       ├── entity/
│       │   ├── ChatMessageEntity.java          ✨ 新
│       │   └── ChatConversationEntity.java     ✨ 新
│       ├── repository/
│       │   ├── ChatMessageRepository.java      ✨ 新
│       │   └── ChatConversationRepository.java ✨ 新
│       ├── model/
│       │   ├── request/ChatRequests.java       ✨ 新
│       │   └── response/ChatResponses.java     ✨ 新
│       ├── service/
│       │   ├── ChatService.java                ✨ 新
│       │   └── impl/ChatServiceImpl.java        ✨ 新
│       ├── controller/
│       │   └── ChatController.java             ✨ 新
│       └── common/config/
│           └── RestTemplateConfig.java         ✨ 新
│
├── frontend/
│   └── src/
│       ├── api/modules/
│       │   └── chat.ts                         ✨ 新
│       ├── components/common/
│       │   └── AIAssistant.vue                 ✨ 新
│       └── layouts/
│           └── AppLayout.vue                   📝 已修改
│
├── deploy/mysql/
│   └── chat-init.sql                           ✨ 新
│
└── docs/
    ├── AI_ASSISTANT_GUIDE.md                   ✨ 新
    └── AI_ASSISTANT_QUICKSTART.md              ✨ 本文件
```

---

## ✅ 验证清单

启动应用后，验证功能是否正常工作：

- [ ] 后端成功启动，无错误信息
- [ ] 前端无构建错误
- [ ] 能够登录
- [ ] 右下角看到蓝色 AI 助手按钮
- [ ] 点击按钮能打开聊天窗口
- [ ] 能够输入消息并发送
- [ ] AI 返回了回复
- [ ] 消息显示正确的时间戳
- [ ] 刷新页面后仍能打开历史消息

---

## 🎯 下一步

### 可选：增强功能

1. **对话标题自动生成**
   - 修改 `ChatServiceImpl.java` 中的标题逻辑

2. **添加消息编辑/删除**
   - 在 Controller 添加编辑/删除端点

3. **实现文件上传**
   - 允许用户上传文件给 AI 分析

4. **多语言支持**
   - 在前端添加语言切换

5. **深色模式**
   - 为 AIAssistant.vue 添加主题支持

---

## 📞 支持

有问题？检查以下资源：

1. **完整文档**: `docs/AI_ASSISTANT_GUIDE.md`
2. **Deepseek API 文档**: https://api.deepseek.com/docs
3. **项目 README**: `README.md`

---

**祝你使用愉快！🎉**
