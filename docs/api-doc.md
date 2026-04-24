# 《知行创境》教育创作平台接口文档

## 1. 文档说明

- 文档依据：`《知行创境》教育创作平台 V2.0 产品需求文档`
- 接口风格：RESTful JSON
- 基础路径：`/api/v1`
- 认证方式：`Authorization: Bearer <token>`
- 数据库：MySQL 8.0
- 文件存储：建议使用对象存储（如 MinIO / OSS / COS），数据库仅保存元数据与访问地址

## 2. 统一约定

### 2.1 通用响应结构

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "requestId": "4f9b6e24e2ef4f4f9d6c"
}
```

- `code = 0` 表示成功
- 非 0 表示业务异常，建议结合 HTTP 状态码返回

### 2.2 分页结构

```json
{
  "list": [],
  "page": 1,
  "pageSize": 20,
  "total": 120
}
```

### 2.3 通用状态字典

- 用户角色：`student`、`teacher`、`parent`、`admin`、`visitor`
- 任务状态：`draft`、`published`、`closed`、`archived`
- 展厅状态：`draft`、`published`、`archived`
- 展厅可见性：`private`、`class`、`public`
- 提交状态：`submitted`、`reviewed`、`returned`
- 评论状态：`normal`、`deleted`
- 互动类型：`like`、`favorite`、`share`

## 3. 核心对象

### 3.1 用户 User

```json
{
  "id": 10001,
  "role": "student",
  "realName": "张小明",
  "nickname": "小明",
  "avatarUrl": "https://cdn.example.com/avatar/10001.png",
  "schoolId": 1,
  "classInfo": {
    "id": 101,
    "name": "七年级(1)班"
  }
}
```

### 3.2 任务 Task

```json
{
  "id": 20001,
  "title": "创建红色记忆展厅",
  "coverUrl": "https://cdn.example.com/task/red-history.jpg",
  "description": "围绕长征精神完成主题创作",
  "teacher": {
    "id": 9001,
    "name": "李老师"
  },
  "startTime": "2026-04-18 08:00:00",
  "dueTime": "2026-05-10 23:59:59",
  "status": "published",
  "targetClasses": [
    {
      "id": 101,
      "name": "七年级(1)班"
    }
  ]
}
```

### 3.3 展厅 Exhibition

```json
{
  "id": 30001,
  "taskId": 20001,
  "title": "长征精神数字展厅",
  "coverUrl": "https://cdn.example.com/exhibition/30001-cover.png",
  "summary": "以红军长征为主线的叙事展厅",
  "status": "draft",
  "visibility": "class",
  "groupName": "星火小组",
  "ownerId": 10001,
  "latestVersionNo": 6,
  "publishedVersionNo": 5,
  "stats": {
    "viewCount": 112,
    "likeCount": 36,
    "favoriteCount": 18,
    "commentCount": 12
  }
}
```

### 3.4 AI 数字人 DigitalHuman

```json
{
  "id": 40001,
  "exhibitionId": 30001,
  "name": "小红军阿勇",
  "avatar2dUrl": "https://cdn.example.com/dh/40001.png",
  "model3dUrl": "https://cdn.example.com/dh/40001.glb",
  "persona": "1935年随军宣传员",
  "storyScript": "大家好，我是阿勇，今天由我带你们重走长征路。",
  "equippedItems": [
    {
      "id": 50001,
      "slotCode": "uniform",
      "resourceId": 70001,
      "resourceTitle": "红军军装（1935年款）",
      "museumName": "延安革命纪念馆"
    }
  ]
}
```

## 4. 认证与账户

### 4.1 用户登录

- 方法：`POST /auth/login`

请求体：

```json
{
  "account": "student001",
  "password": "123456"
}
```

响应体：

```json
{
  "token": "jwt-token",
  "refreshToken": "refresh-token",
  "expiresIn": 7200,
  "user": {
    "id": 10001,
    "role": "student",
    "nickname": "小明"
  }
}
```

### 4.2 获取当前用户信息

- 方法：`GET /auth/me`

### 4.3 修改个人资料

- 方法：`PUT /users/me/profile`

请求体：

```json
{
  "nickname": "小明同学",
  "avatarUrl": "https://cdn.example.com/avatar/new.png",
  "bio": "喜欢历史与创作"
}
```

### 4.4 修改密码

- 方法：`PUT /users/me/password`

请求体：

```json
{
  "oldPassword": "123456",
  "newPassword": "new123456"
}
```

## 5. 首页工作台

### 5.1 获取首页工作台数据

- 方法：`GET /dashboard/overview`

查询参数：

- `role`：可选，前端切换视图时传入

响应体：

```json
{
  "ongoingTasks": [],
  "recentExhibitions": [],
  "announcements": [],
  "activityFeeds": [],
  "recommendedResources": [],
  "featuredExhibitions": []
}
```

字段说明：

- `ongoingTasks`：进行中的任务
- `recentExhibitions`：最近编辑的展厅
- `announcements`：教师公告
- `activityFeeds`：同学发布、点赞、评论等动态
- `recommendedResources`：首页推荐素材
- `featuredExhibitions`：社区精选展厅

## 6. 班级与公告

### 6.1 获取我的班级列表

- 方法：`GET /classes`

### 6.2 获取班级详情

- 方法：`GET /classes/{classId}`

### 6.3 获取班级成员

- 方法：`GET /classes/{classId}/members`

### 6.4 发布班级公告

- 方法：`POST /classes/{classId}/announcements`
- 角色：`teacher`

请求体：

```json
{
  "title": "本周项目检查",
  "content": "请各小组在周五前完成展厅初稿。",
  "pinned": true
}
```

### 6.5 获取班级公告列表

- 方法：`GET /classes/{classId}/announcements`

## 7. 课程 / 任务中心

### 7.1 获取任务列表

- 方法：`GET /tasks`

查询参数：

- `status`：`draft/published/closed/archived`
- `classId`
- `keyword`
- `page`
- `pageSize`

### 7.2 创建任务

- 方法：`POST /tasks`
- 角色：`teacher`

请求体：

```json
{
  "title": "创建红色记忆展厅",
  "coverUrl": "https://cdn.example.com/task/cover.png",
  "description": "围绕长征精神完成数字展厅创作",
  "backgroundMaterials": [
    {
      "title": "长征历史资料",
      "materialType": "link",
      "url": "https://example.com/history"
    }
  ],
  "evaluationCriteria": "内容真实性40%，叙事结构30%，创意表现30%",
  "startTime": "2026-04-18 08:00:00",
  "dueTime": "2026-05-10 23:59:59",
  "targetClassIds": [101, 102]
}
```

### 7.3 更新任务

- 方法：`PUT /tasks/{taskId}`
- 角色：`teacher`

### 7.4 获取任务详情

- 方法：`GET /tasks/{taskId}`

### 7.5 获取任务优秀作品

- 方法：`GET /tasks/{taskId}/excellent-exhibitions`

### 7.6 教师查看任务进度

- 方法：`GET /tasks/{taskId}/progress`
- 角色：`teacher`

响应体：

```json
{
  "taskId": 20001,
  "submittedCount": 18,
  "reviewedCount": 9,
  "groups": [
    {
      "exhibitionId": 30001,
      "groupName": "星火小组",
      "leaderName": "张小明",
      "memberCount": 4,
      "progressPercent": 80,
      "submissionStatus": "submitted",
      "updatedAt": "2026-04-20 20:18:09"
    }
  ]
}
```

## 8. 创作中心

### 8.1 获取我的展厅列表

- 方法：`GET /exhibitions`

查询参数：

- `taskId`
- `status`
- `keyword`
- `page`
- `pageSize`

### 8.2 创建展厅

- 方法：`POST /exhibitions`

请求体：

```json
{
  "taskId": 20001,
  "title": "长征精神数字展厅",
  "summary": "围绕长征路线和精神展开",
  "coverUrl": "https://cdn.example.com/exhibition/cover.png",
  "visibility": "class",
  "groupName": "星火小组"
}
```

### 8.3 获取展厅详情

- 方法：`GET /exhibitions/{exhibitionId}`

### 8.4 更新展厅基础信息

- 方法：`PUT /exhibitions/{exhibitionId}`

请求体：

```json
{
  "title": "长征精神数字展厅",
  "summary": "更新后的摘要",
  "coverUrl": "https://cdn.example.com/exhibition/new-cover.png",
  "visibility": "public"
}
```

### 8.5 邀请协作者

- 方法：`POST /exhibitions/{exhibitionId}/members`

请求体：

```json
{
  "memberUserIds": [10002, 10003],
  "role": "editor"
}
```

### 8.6 获取协作者列表

- 方法：`GET /exhibitions/{exhibitionId}/members`

### 8.7 保存展厅版本

- 方法：`POST /exhibitions/{exhibitionId}/versions`

请求体：

```json
{
  "saveType": "autosave",
  "versionNote": "完成第一面墙内容编排",
  "canvasConfig": {
    "width": 1920,
    "height": 1080,
    "background": "#f5f0e6",
    "zoom": 1
  },
  "versionData": {
    "elements": [
      {
        "componentType": "showcase",
        "x": 120,
        "y": 160,
        "width": 320,
        "height": 240,
        "props": {
          "title": "红军军装",
          "description": "1935年款"
        }
      }
    ]
  }
}
```

### 8.8 获取版本历史

- 方法：`GET /exhibitions/{exhibitionId}/versions`

### 8.9 获取观众预览数据

- 方法：`GET /exhibitions/{exhibitionId}/viewer`

响应体：

```json
{
  "exhibition": {},
  "renderData": {
    "canvasConfig": {},
    "elements": []
  },
  "digitalHuman": {},
  "teacherReviews": [],
  "comments": []
}
```

### 8.10 发布展厅

- 方法：`POST /exhibitions/{exhibitionId}/publish`

请求体：

```json
{
  "versionNo": 6,
  "visibility": "public"
}
```

## 9. 媒体素材

### 9.1 上传素材

- 方法：`POST /assets/upload`
- Content-Type：`multipart/form-data`

表单字段：

- `file`
- `folder`：如 `exhibition`、`avatar`、`audio`
- `bizType`：如 `exhibition_material`

响应体：

```json
{
  "assetId": 60001,
  "fileName": "red-army.png",
  "fileUrl": "https://cdn.example.com/upload/red-army.png",
  "mimeType": "image/png",
  "fileSize": 245678
}
```

### 9.2 获取我的素材列表

- 方法：`GET /assets`

查询参数：

- `assetType`：`image/video/audio/document/model`
- `sourceType`：`upload/platform/museum`
- `page`
- `pageSize`

## 10. AI 数字人

### 10.1 获取展厅数字人详情

- 方法：`GET /exhibitions/{exhibitionId}/digital-human`

### 10.2 创建或更新数字人

- 方法：`PUT /exhibitions/{exhibitionId}/digital-human`

请求体：

```json
{
  "name": "小红军阿勇",
  "avatar2dUrl": "https://cdn.example.com/dh/avatar.png",
  "model3dUrl": "https://cdn.example.com/dh/model.glb",
  "persona": "1935年随军宣传员",
  "voiceType": "teen-boy",
  "storyScript": "大家好，我是阿勇。",
  "storyTimeline": [
    {
      "anchorCode": "uniform_001",
      "startSecond": 3,
      "endSecond": 8,
      "content": "这是我们当时穿的军装。"
    }
  ]
}
```

### 10.3 绑定博物馆装备

- 方法：`POST /digital-humans/{digitalHumanId}/equipments`

请求体：

```json
{
  "slotCode": "uniform",
  "museumResourceId": 70001,
  "displayOrder": 1,
  "anchorCode": "uniform_001"
}
```

### 10.4 删除已绑定装备

- 方法：`DELETE /digital-humans/{digitalHumanId}/equipments/{equipmentId}`

## 11. 博物馆资源

### 11.1 获取博物馆资源列表

- 方法：`GET /museum/resources`

查询参数：

- `providerCode`
- `category`
- `keyword`
- `page`
- `pageSize`

### 11.2 获取博物馆资源详情

- 方法：`GET /museum/resources/{resourceId}`

响应体：

```json
{
  "id": 70001,
  "providerCode": "yanan_memorial",
  "title": "红军军装（1935年款）",
  "category": "uniform",
  "museumName": "延安革命纪念馆",
  "coverUrl": "https://cdn.example.com/museum/70001.jpg",
  "detailUrl": "https://museum.example.com/resource/70001",
  "description": "1935年红军常见军装样式",
  "metadata": {
    "period": "1935",
    "sourceNo": "YA-1935-001"
  }
}
```

### 11.3 同步博物馆资源

- 方法：`POST /museum/resources/sync`
- 角色：`admin`

请求体：

```json
{
  "providerCode": "yanan_memorial",
  "category": "uniform",
  "keyword": "红军军装"
}
```

说明：

- 该接口由后台管理或定时任务触发
- 前台学生查询优先读本地缓存表，避免频繁调用外部 API

## 12. 提交与教师评价

### 12.1 提交任务作品

- 方法：`POST /tasks/{taskId}/submit`

请求体：

```json
{
  "exhibitionId": 30001,
  "submitRemark": "已完成第一版展厅与数字人故事"
}
```

### 12.2 获取任务提交记录

- 方法：`GET /tasks/{taskId}/submissions`
- 角色：`teacher`

### 12.3 获取提交详情

- 方法：`GET /submissions/{submissionId}`

### 12.4 教师评分与点评

- 方法：`POST /submissions/{submissionId}/reviews`
- 角色：`teacher`

请求体：

```json
{
  "score": 92.5,
  "commentText": "叙事结构完整，历史素材引用准确。",
  "commentAudioUrl": "https://cdn.example.com/review/audio-1.mp3",
  "isPublic": true
}
```

## 13. 社区展厅

### 13.1 获取社区展厅列表

- 方法：`GET /community/exhibitions`

查询参数：

- `keyword`
- `grade`
- `theme`
- `tag`
- `sortBy`：`latest/hot/likes`
- `page`
- `pageSize`

### 13.2 获取社区展厅详情

- 方法：`GET /community/exhibitions/{exhibitionId}`

### 13.3 点赞展厅

- 方法：`POST /community/exhibitions/{exhibitionId}/like`

### 13.4 取消点赞

- 方法：`DELETE /community/exhibitions/{exhibitionId}/like`

### 13.5 收藏展厅

- 方法：`POST /community/exhibitions/{exhibitionId}/favorite`

### 13.6 取消收藏

- 方法：`DELETE /community/exhibitions/{exhibitionId}/favorite`

### 13.7 记录分享

- 方法：`POST /community/exhibitions/{exhibitionId}/share`

请求体：

```json
{
  "channel": "wechat"
}
```

### 13.8 获取评论列表

- 方法：`GET /community/exhibitions/{exhibitionId}/comments`

查询参数：

- `page`
- `pageSize`

响应体：

```json
{
  "teacherReviews": [],
  "studentComments": []
}
```

### 13.9 发表评论

- 方法：`POST /community/exhibitions/{exhibitionId}/comments`

请求体：

```json
{
  "content": "这个数字人的讲述很有代入感。",
  "parentCommentId": null,
  "mentionUserIds": [10002]
}
```

### 13.10 教师推荐到社区首页

- 方法：`POST /community/exhibitions/{exhibitionId}/feature`
- 角色：`teacher/admin`

请求体：

```json
{
  "featured": true,
  "featuredReason": "主题鲜明，素材考据扎实"
}
```

## 14. 个人中心与通知

### 14.1 获取个人主页

- 方法：`GET /users/{userId}/homepage`

响应体：

```json
{
  "user": {},
  "stats": {
    "exhibitionCount": 12,
    "likeCount": 90,
    "teacherPraiseCount": 6
  },
  "portfolio": []
}
```

### 14.2 获取我的作品集

- 方法：`GET /users/me/portfolio`

### 14.3 获取通知列表

- 方法：`GET /notifications`

查询参数：

- `readStatus`：`unread/read`
- `page`
- `pageSize`

### 14.4 标记通知已读

- 方法：`POST /notifications/read`

请求体：

```json
{
  "notificationIds": [1, 2, 3]
}
```

## 15. 错误码建议

| 错误码 | 说明 |
| --- | --- |
| 40001 | 参数错误 |
| 40100 | 未登录或 token 失效 |
| 40300 | 无权限访问 |
| 40400 | 数据不存在 |
| 40900 | 数据状态冲突 |
| 42200 | 业务校验失败 |
| 50000 | 系统内部错误 |

## 16. 实现建议

- 展厅编辑保存采用“版本快照 + 当前版本号”模式，便于自动保存与历史回溯
- 博物馆资源查询优先走本地缓存表，再由定时任务增量同步外部机构 API
- 社区详情页评论区建议同时返回教师点评与普通评论，便于前端分区渲染
- 统计字段如点赞数、评论数、浏览数建议在主表冗余，异步校正
