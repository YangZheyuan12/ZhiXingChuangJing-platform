# 📦 资料库功能增强 - 部署说明

## 🎯 功能概述

本次更新为"知行创境"教育创作平台添加了**资料库管理增强功能**，包括文博资源多维筛选、素材版本管理、文件夹标签系统、教师素材包等核心功能。

---

## 📋 部署步骤

### 方式一：使用 MySQL 命令行（推荐）

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 执行数据库迁移
source e:\ZhiXingChuangJing-platform-master\deploy\mysql\01_asset_library_enhancement.sql

# 3. 导入示例数据（可选）
source e:\ZhiXingChuangJing-platform-master\deploy\mysql\02_museum_sample_data.sql

# 4. 退出
exit
```

### 方式二：使用 Navicat/MySQL Workbench 等工具

1. 打开 SQL 文件：`deploy/mysql/01_asset_library_enhancement.sql`
2. 执行 SQL 脚本
3. 打开并执行：`deploy/mysql/02_museum_sample_data.sql`（可选）

### 方式三：直接复制粘贴 SQL

如果上述方式都不方便，可以：

1. 打开 `01_asset_library_enhancement.sql` 文件
2. 全选复制所有 SQL 内容
3. 在 MySQL 命令行或工具中粘贴执行
4. 同样方式执行 `02_museum_sample_data.sql`

---

## ✅ 验证部署成功

执行以下 SQL 检查表是否创建成功：

```sql
USE zhixingchuangjing;

-- 检查新表
SHOW TABLES LIKE 'asset_%';
SHOW TABLES LIKE 'material_%';

-- 检查字段
DESC museum_resources;
DESC media_assets;

-- 检查示例数据（如果执行了 02 脚本）
SELECT COUNT(*) FROM museum_resources WHERE dynasty IS NOT NULL;
```

**预期结果**：
- 看到 `asset_folders`, `asset_tags`, `asset_versions`, `material_packs`, `material_pack_assets` 表
- `museum_resources` 表有 `dynasty`, `material`, `region` 字段
- `media_assets` 表有 `folder`, `checksum_sha256` 字段

---

## 📊 新增功能清单

### 1. 文博资源多维筛选
- ✅ 按朝代筛选（如：唐代、宋代）
- ✅ 按材质筛选（如：陶瓷、青铜器）
- ✅ 按地区筛选（如：陕西、北京）
- ✅ 按分类筛选（如：文物、书画）

### 2. 素材版本管理
- ✅ 上传新版本自动递增版本号
- ✅ 查看历史版本列表
- ✅ 版本备注和创建人记录
- ✅ MD5 校验防止文件损坏

### 3. 文件夹 + 标签管理
- ✅ 多级文件夹（支持无限层级）
- ✅ 素材标签（一个素材多个标签）
- ✅ 快速检索和过滤

### 4. 素材去重（MD5）
- ✅ 上传时自动计算 MD5
- ✅ 检测到重复文件直接返回已有记录
- ✅ 节省存储空间

### 5. 教师素材包
- ✅ 创建教学素材包
- ✅ 批量管理常用素材
- ✅ 支持公开/私有设置
- ✅ 素材包内素材排序

### 6. 文博资源示例数据
- ✅ 15 件真实文物数据
- ✅ 包含朝代、材质、地区信息
- ✅ 涵盖各历史时期代表作

---

## 📡 API 接口列表

### 文博资源接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/museum/resources/filter` | 多维筛选（朝代/材质/地区） |
| POST | `/api/v1/museum/resources/import` | CSV 批量导入 |
| POST | `/api/v1/museum/resources/{id}/favorite` | 收藏到个人素材库 |

### 素材版本接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/museum/assets/{assetId}/versions` | 查看历史版本 |
| POST | `/api/v1/museum/assets/{assetId}/versions` | 上传新版本 |

### 文件夹接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/museum/folders` | 获取文件夹列表 |
| POST | `/api/v1/museum/folders` | 创建文件夹 |
| DELETE | `/api/v1/museum/folders/{folderId}` | 删除文件夹 |

### 标签接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/museum/assets/{assetId}/tags` | 获取标签列表 |
| POST | `/api/v1/museum/assets/{assetId}/tags` | 添加标签 |
| DELETE | `/api/v1/museum/assets/{assetId}/tags/{tagName}` | 删除标签 |

### 素材包接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/museum/packs` | 获取素材包列表 |
| GET | `/api/v1/museum/packs/{packId}` | 获取素材包详情 |
| POST | `/api/v1/museum/packs` | 创建素材包 |
| POST | `/api/v1/museum/packs/{packId}/assets/{assetId}` | 添加素材到包 |
| DELETE | `/api/v1/museum/packs/{packId}/assets/{assetId}` | 从包中移除素材 |
| DELETE | `/api/v1/museum/packs/{packId}` | 删除素材包 |

---

## 📝 CSV 导入格式

CSV 文件格式（UTF-8 编码，首行为表头）：

```csv
标题，副标题，博物馆名称，分类，朝代，材质，地区，描述，封面图 URL,详情 URL
蛋壳黑陶高柄杯，龙山文化代表作，中国国家博物馆，文物，新石器时代，陶器，山东，壁厚仅 0.2-0.3 毫米，https://example.com/image.jpg,https://example.com/detail
```

---

## 🎯 15 件示例文物清单

1. **蛋壳黑陶高柄杯** - 新石器时代·陶器·山东
2. **后母戊鼎** - 商代·青铜器·河南（世界最大青铜器）
3. **利簋** - 西周·青铜器·陕西（记载武王伐纣）
4. **秦兵马俑** - 秦代·陶器·陕西（世界第八大奇迹）
5. **金缕玉衣** - 西汉·玉器·河北
6. **马踏飞燕** - 东汉·青铜器·甘肃（中国旅游标志）
7. **唐三彩载乐驼** - 唐代·陶瓷·陕西（丝绸之路见证）
8. **清明上河图** - 北宋·书画·北京（中国十大传世名画）
9. **元青花萧何月下追韩信梅瓶** - 元代·陶瓷·江西
10. **明金翼善冠** - 明代·金银器·北京
11. **清乾隆各种釉彩大瓶** - 清代·陶瓷·北京（瓷母）
12. **良渚玉琮王** - 新石器时代·玉器·浙江
13. **三星堆青铜面具** - 商代·青铜器·四川（古蜀文明）
14. **镶金兽首玛瑙杯** - 唐代·金银器·陕西
15. **越王勾践剑** - 战国·青铜器·湖北（天下第一剑）

---

## ⚠️ 注意事项

1. **数据库版本**：MySQL >= 8.0
2. **字符集**：utf8mb4
3. **执行顺序**：先执行 01 脚本，再执行 02 脚本
4. **权限要求**：需要 CREATE TABLE 和 ALTER TABLE 权限
5. **示例数据**：02 脚本会检查是否已存在数据，避免重复

---

## 🐛 常见问题

### Q1: 表已存在错误
**A**: 如果提示表已存在，说明之前创建过，可以忽略或先 DROP TABLE 再执行。

### Q2: 字段已存在错误
**A**: 脚本已做判断，如果字段存在会跳过。如仍有错误，手动检查表结构。

### Q3: 示例数据重复
**A**: 02 脚本已做去重判断，不会重复插入。

### Q4: 执行后看不到数据
**A**: 确认执行了 `USE zhixingchuangjing;` 切换到正确数据库。

---

## 📞 技术支持

如遇问题，请检查：
1. MySQL 版本是否 >= 8.0
2. 数据库字符集是否为 utf8mb4
3. 是否有足够的数据库权限
4. SQL 执行顺序是否正确

---

**文档版本**: v1.0  
**更新日期**: 2026-05-06  
**适用版本**: 知行创境教育创作平台 v1.0+
