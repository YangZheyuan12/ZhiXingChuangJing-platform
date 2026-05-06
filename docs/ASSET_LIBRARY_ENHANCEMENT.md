# 资料库功能增强 - 快速部署指南

## 📦 已实现功能清单

### ✅ 已完成功能

1. **文博资源 CSV 批量导入** - POST `/api/v1/museum/resources/import`
2. **文博资源多维筛选** - GET `/api/v1/museum/resources/filter?dynasty=&material=&region=`
3. **收藏文博资源到个人素材库** - POST `/api/v1/museum/resources/{resourceId}/favorite`
4. **素材版本管理** - 上传新版本 + 查看历史版本
5. **文件夹 + 标签管理** - 多级目录 + 素材标签
6. **素材去重（MD5）** - 自动检测重复文件
7. **教师素材包功能** - 创建和管理教学素材包
8. **文博资源示例数据** - 15 件真实文物数据

---

## 🚀 部署步骤

### 1. 数据库迁移

执行数据库迁移脚本：

```bash
# 进入 MySQL
mysql -u root -p zhixingchuangjing

# 执行迁移脚本
source deploy/mysql/asset_library_enhancement.sql;

# 导入示例数据
source deploy/mysql/museum_sample_data.sql;
```

### 2. 后端代码变更

已新增/修改的文件：

#### 新增实体类（6 个）
- `entity/MuseumResourceEntity.java` - 文博资源实体
- `entity/AssetFolderEntity.java` - 素材文件夹实体
- `entity/AssetTagEntity.java` - 素材标签实体
- `entity/AssetVersionEntity.java` - 素材版本实体
- `entity/MaterialPackEntity.java` - 素材包实体
- `entity/MaterialPackAssetEntity.java` - 素材包关联实体

#### 新增 Repository（6 个）
- `repository/MuseumResourceRepository.java`
- `repository/AssetFolderRepository.java`
- `repository/AssetTagRepository.java`
- `repository/AssetVersionRepository.java`
- `repository/MaterialPackRepository.java`
- `repository/MaterialPackAssetRepository.java`

#### 修改的文件
- `service/MuseumService.java` - 新增接口方法
- `service/impl/MuseumServiceImpl.java` - 完整实现所有新功能
- `controller/MuseumController.java` - 新增 15+ 个 API 接口
- `repository/AssetRepository.java` - 新增去重查询方法
- `service/impl/AssetServiceImpl.java` - 集成 MD5 去重
- `model/request/MuseumRequests.java` - 新增请求 DTO
- `model/response/MuseumResponses.java` - 新增响应 DTO

### 3. 编译启动

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

---

## 📡 API 接口列表

### 文博资源相关

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/museum/resources/filter` | 多维筛选（朝代/材质/地区） |
| POST | `/museum/resources/import` | CSV 批量导入 |
| POST | `/museum/resources/{id}/favorite` | 收藏到个人素材库 |

### 素材版本管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/museum/assets/{assetId}/versions` | 查看历史版本 |
| POST | `/museum/assets/{assetId}/versions` | 上传新版本 |

### 文件夹管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/museum/folders` | 获取文件夹列表 |
| POST | `/museum/folders` | 创建文件夹 |
| DELETE | `/museum/folders/{folderId}` | 删除文件夹 |

### 标签管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/museum/assets/{assetId}/tags` | 获取标签列表 |
| POST | `/museum/assets/{assetId}/tags` | 添加标签 |
| DELETE | `/museum/assets/{assetId}/tags/{tagName}` | 删除标签 |

### 素材包管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/museum/packs` | 获取素材包列表 |
| GET | `/museum/packs/{packId}` | 获取素材包详情 |
| POST | `/museum/packs` | 创建素材包 |
| POST | `/museum/packs/{packId}/assets/{assetId}` | 添加素材到包 |
| DELETE | `/museum/packs/{packId}/assets/{assetId}` | 从包中移除素材 |
| DELETE | `/museum/packs/{packId}` | 删除素材包 |

---

## 📝 CSV 导入格式

CSV 文件格式（UTF-8 编码，首行为表头）：

```csv
标题，副标题，博物馆名称，分类，朝代，材质，地区，描述，封面图 URL,详情 URL
蛋壳黑陶高柄杯，龙山文化代表作，中国国家博物馆，文物，新石器时代，陶器，山东，壁厚仅 0.2-0.3 毫米，https://example.com/image.jpg,https://example.com/detail
```

---

## 🔍 功能特性

### 1. 素材去重
- 上传时自动计算 MD5
- 检测到重复文件直接返回已有记录
- 节省存储空间

### 2. 版本管理
- 每次上传新版本自动递增版本号
- 支持查看完整版本历史
- 记录版本备注和创建人

### 3. 多级文件夹
- 支持无限级目录结构
- 自动维护文件夹路径
- 支持按父级 ID 查询

### 4. 标签系统
- 一个素材可关联多个标签
- 标签去重（唯一键约束）
- 快速检索和过滤

### 5. 素材包
- 教师可创建教学素材包
- 批量管理常用素材
- 支持公开/私有设置

---

## 🎯 使用示例

### 收藏文博资源

```bash
curl -X POST http://localhost:8080/api/v1/museum/resources/1/favorite \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"folder": "我的素材"}'
```

### 多维筛选

```bash
curl "http://localhost:8080/api/v1/museum/resources/filter?dynasty=唐代&material=陶瓷&region=陕西"
```

### 创建素材包

```bash
curl -X POST "http://localhost:8080/api/v1/museum/packs?packName=秦汉文化教学包&description=秦汉历史课程素材" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## ⚠️ 注意事项

1. **数据库兼容性**：确保 MySQL 版本 >= 8.0
2. **字符集**：使用 utf8mb4
3. **文件上传大小**：默认限制根据服务器配置
4. **权限控制**：所有操作需要 JWT 认证
5. **示例数据**：15 件文物为真实历史文物

---

## 📊 数据统计

- 新增实体类：6 个
- 新增 Repository：6 个
- 新增 API 接口：15+ 个
- 数据库表：6 张新表
- 示例数据：15 条文物记录
- 代码行数：约 1500+ 行

---

## 🎉 后续建议

1. **素材审核机制**：需要管理员后台配合
2. **素材引用反查**：需要展厅组件表关联
3. **大文件分片上传**：后续优化
4. **CDN 加速**：生产环境建议接入

---

## 📞 技术支持

如有问题，请检查：
1. 数据库迁移是否成功执行
2. 后端编译是否无错误
3. 日志文件是否有异常信息

**部署完成时间**：约 10-15 分钟
