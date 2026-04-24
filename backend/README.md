# Backend

后端基于 Spring Boot 3 与 Java 17，采用分层架构组织：

- `controller`：接收 HTTP 请求
- `service`：承载业务逻辑
- `repository`：数据库访问
- `entity`：持久化实体
- `model`：请求/响应对象
- `common`：统一响应、异常、安全、配置

## 本地启动

```bash
mvn spring-boot:run
```

## 配置文件

- `application.yml`：公共配置
- `application-dev.yml`：开发环境
- `application-prod.yml`：生产环境

## 初始化 SQL

- `src/main/resources/db/seed/mysql-init.sql`

## 初始账号

- `admin / 123456`
- `teacher001 / 123456`
- `student001 / 123456`
