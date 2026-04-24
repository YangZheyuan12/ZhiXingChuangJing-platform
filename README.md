# 知行创境教育创作平台

知行创境是一个面向中小学场景的教育创作平台，支持课程任务驱动、数字展厅创作、AI 数字人、博物馆资源接入、社区展示与评论互动。

## 目录结构

```text
.
├── backend/            # Spring Boot 后端
├── frontend/           # Vue 3 前端
├── deploy/             # Docker / Nginx / MySQL 部署配置
├── docs/               # 需求、接口与数据库设计文档
├── scripts/            # 开发辅助脚本
└── docker-compose.yml  # 本地联调编排
```

## 技术栈

- 前端：Vue 3、TypeScript、Vite、Tailwind CSS、Pinia、Vue Router、Axios
- 后端：Java 17、Spring Boot、Spring Security、Spring Data JPA、MySQL、Redis
- 部署：Docker、Docker Compose、Nginx、MinIO

## 启动约定

### 1. 数据库

- 默认数据库名：`zhixingchuangjing`
- 初始化脚本：`deploy/mysql/init.sql`

### 2. 后端

后端目录：`backend/`

```bash
cd backend
mvn spring-boot:run
```

### 3. MinIO

- 默认对象存储地址：`http://127.0.0.1:9000`
- 控制台地址：`http://127.0.0.1:9001`
- 默认账号：`minioadmin / minioadmin`

### 4. 前端

前端目录：`frontend/`

```bash
cd frontend
npm install
npm run dev
```

## 默认账号

- `admin / 123456`
- `teacher001 / 123456`
- `student001 / 123456`

## 文档来源

- 数据库设计：`docs/db-design.md`
- 接口文档：`docs/api-doc.md`
- OpenAPI：`docs/openapi.yaml`
- 产品需求：`docs/《知行创境》教育创作平台.docx`
