# AI助手功能集成说明

## 功能概述

本文档说明如何在智行创景平台中使用集成的 **Deepseek AI助手** 功能。

## 已完成的修改

### 1. 前端组件
- **文件**: `frontend/src/components/common/AIAssistant.vue`
- **功能**: 浮动AI助手窗口，支持：
  - 消息收发
  - 实时对话
  - 消息历史
  - 最小化/最大化
  - 移动端适配

- **文件**: `frontend/src/api/modules/chat.ts`
- **功能**: 聊天API模块，包含：
  - `sendChatMessage()` - 发送消息
  - `getChatHistory()` - 获取对话历史
  - `createConversation()` - 创建新对话
  - `getConversationList()` - 获取对话列表
  - `deleteConversation()` - 删除对话

- **文件**: `frontend/src/layouts/AppLayout.vue`
- **修改**: 已集成AIAssistant组件到主布局中

### 2. 后端Java代码

#### 实体类
- `backend/src/main/java/.../entity/ChatMessageEntity.java` - 聊天消息实体
- `backend/src/main/java/.../entity/ChatConversationEntity.java` - 对话实体

#### Repository
- `backend/src/main/java/.../repository/ChatMessageRepository.java`
- `backend/src/main/java/.../repository/ChatConversationRepository.java`

#### 模型类
- `backend/src/main/java/.../model/request/ChatRequests.java` - 请求模型
- `backend/src/main/java/.../model/response/ChatResponses.java` - 响应模型

#### 服务层
- `backend/src/main/java/.../service/ChatService.java` - 服务接口
- `backend/src/main/java/.../service/impl/ChatServiceImpl.java` - 服务实现

#### 控制器
- `backend/src/main/java/.../controller/ChatController.java` - REST控制器

#### 配置
- `backend/src/main/java/.../common/config/RestTemplateConfig.java` - RestTemplate配置

### 3. 配置文件
- `backend/src/main/resources/application.yml` - 已添加Deepseek API配置
- `deploy/mysql/chat-init.sql` - 数据库初始化SQL

## 环境配置

### Deepseek API 密钥配置

#### 方式一：环境变量（推荐用于生产环境）
```bash
export DEEPSEEK_API_KEY=sk-0421479e732446ffb4aa6b4f4d6cb2f2
export DEEPSEEK_API_URL=https://api.deepseek.com/chat/completions
```

#### 方式二：application.yml（已配置默认值）
```yaml
deepseek:
  api:
    key: ${DEEPSEEK_API_KEY:sk-0421479e732446ffb4aa6b4f4d6cb2f2}
    url: ${DEEPSEEK_API_URL:https://api.deepseek.com/chat/completions}
```

#### 方式三：application-prod.yml（生产环境）
```yaml
deepseek:
  api:
    key: your-production-key-here
    url: https://api.deepseek.com/chat/completions
```

## API 端点

### 创建新对话
```
POST /api/v1/chat/conversation
Authorization: Bearer <token>
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "conversationId": "uuid-string"
  }
}
```

### 发送聊天消息
```
POST /api/v1/chat/message
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "你好，请帮我解释一下这个概念",
  "conversationId": "uuid-string"
}
```

**响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "message": {
      "id": "123",
      "content": "这个概念是...",
      "role": "assistant",
      "timestamp": 1715000000000
    },
    "conversationId": "uuid-string"
  }
}
```

### 获取对话历史
```
GET /api/v1/chat/history/{conversationId}
Authorization: Bearer <token>
```

### 获取对话列表
```
GET /api/v1/chat/conversations
Authorization: Bearer <token>
```

### 删除对话
```
DELETE /api/v1/chat/conversation/{conversationId}
Authorization: Bearer <token>
```

## 数据库表结构

### chat_conversations（对话表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) | 对话ID（UUID） |
| user_id | BIGINT | 用户ID |
| title | VARCHAR(255) | 对话标题 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| message_count | INT | 消息数量 |

### chat_messages（消息表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 消息ID |
| conversation_id | VARCHAR(36) | 所属对话ID |
| user_id | BIGINT | 用户ID（null表示AI消息） |
| content | LONGTEXT | 消息内容 |
| role | VARCHAR(10) | 角色（user/assistant） |
| created_at | DATETIME | 创建时间 |

## 数据库初始化

### 使用Hibernate自动建表
默认配置下，Spring Boot/JPA会根据实体类自动创建表。

### 手动建表
如需手动建表，执行以下SQL：
```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS zxcyj;
USE zxcyj;

-- 执行初始化脚本
SOURCE deploy/mysql/chat-init.sql;
```

## 部署步骤

### 1. 构建后端
```bash
cd backend
mvn clean package
```

### 2. 构建前端
```bash
cd frontend
npm install
npm run build
```

### 3. 配置数据库
- 确保MySQL服务运行
- 创建数据库（如需要）
- 可选：手动执行 `deploy/mysql/chat-init.sql`

### 4. 启动应用
```bash
# 后端
cd backend
mvn spring-boot:run

# 或使用JAR
java -jar backend/target/platform-backend-0.0.1-SNAPSHOT.jar

# 前端（开发模式）
cd frontend
npm run dev

# 或部署到服务器
```

## 使用流程

1. **登录平台** - 使用现有的用户账号登录
2. **打开AI助手** - 点击右下角的浮动按钮
3. **创建新对话** - 助手会自动创建新的对话
4. **发送消息** - 输入问题，按Enter或点击发送按钮
5. **查看回复** - AI会基于整个对话历史上下文进行回复
6. **管理对话** - 可以查看和删除历史对话

## 常见问题

### Q: AI回复很慢怎么办？
**A**: 
- 检查网络连接
- 检查Deepseek API密钥是否有效
- 查看服务器日志查找错误信息

### Q: 如何更换其他AI模型？
**A**: 
1. 修改 `ChatServiceImpl.java` 中的 `DEEPSEEK_MODEL` 常量
2. 如果使用其他API（如OpenAI），修改请求格式和API端点

### Q: 数据是否被保存？
**A**: 
是的，所有消息都被保存到数据库中，可通过历史记录功能查看

### Q: 如何限制用户的对话？
**A**: 
可在 `ChatServiceImpl.java` 中添加业务逻辑，如：
- 限制每个用户的对话数量
- 限制每个对话的消息数量
- 按时间清理过期对话

## 安全建议

1. **API密钥**
   - 不要在代码中直接写入API密钥
   - 使用环境变量或配置中心
   - 定期轮换API密钥

2. **访问控制**
   - 所有API端点都需要认证（Bearer Token）
   - 用户只能访问自己的对话记录

3. **数据安全**
   - 考虑加密或脱敏敏感信息
   - 定期备份数据库
   - 设置适当的日志级别

4. **API速率限制**
   - 考虑添加速率限制防止滥用
   - 设置请求超时和重试逻辑

## 扩展功能建议

1. **对话分享** - 允许用户分享对话链接
2. **对话导出** - 支持导出为PDF或其他格式
3. **话题标签** - 为对话自动生成和分类标签
4. **AI模型选择** - 允许用户选择不同的AI模型
5. **提示词库** - 内置常用的提示词模板
6. **对话统计** - 显示对话数、消息数等统计信息

## 技术栈

- **前端框架**: Vue.js 3 + TypeScript + Vite
- **UI框架**: Tailwind CSS
- **后端框架**: Spring Boot 3.3.4
- **ORM**: Spring Data JPA (Hibernate)
- **数据库**: MySQL 8.0+
- **HTTP客户端**: RestTemplate (Spring)
- **API**: Deepseek Chat API

## 支持和反馈

如有任何问题或建议，请联系开发团队。
