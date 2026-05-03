# 展厅模板素材清单

本文件记录 5 套展厅模板需要的图片资源、尺寸要求与 AI 生图 prompt。

## 目录约定

所有图片放在 **`frontend/public/templates/<template_code>/`** 下，命名严格匹配 SQL 中 `previewUrl` / `backgroundUrl` 字段。Vite dev 与生产构建都会把 `frontend/public/` 下的文件原样发到 `/` 路径。

例如：

```
frontend/public/templates/red_journey_long_march/cover.jpg
frontend/public/templates/red_journey_long_march/ruijin.jpg
frontend/public/templates/red_journey_long_march/snow_mountain.jpg
...
```

数据库里存 `/templates/red_journey_long_march/cover.jpg`（首字母 `/`），浏览器访问时直接拼到当前域名后即可。

## 总览

| # | 模板 | template_code | 封面 | 展区背景 | 合计 |
|---|---|---|---|---|---|
| 1 | 红色记忆长征展 | `red_journey_long_march` | 1 | 5 | 6 |
| 2 | 博物馆青铜文物展 | `museum_bronze_hall` | 1 | 3 | 4 |
| 3 | 研学成果时间轴展 | `timeline_learning` | 1 | 4 | 5 |
| 4 | 地图探索式文化展 | `map_culture_tour` | 1 | 3 | 4 |
| 5 | 简约白板展 | `minimal_whiteboard` | 1 | 1 | 2 |
| | | | **5** | **16** | **21** |

## 尺寸规范

| 用途 | 尺寸 | 比例 | 格式 |
|---|---|---|---|
| **封面** `cover.jpg` | `400 × 240 px` | 5:3（接近 16:9） | JPG / WebP，文件大小建议 < 80KB |
| **展区背景** `<zone>.jpg` | `1920 × 1080 px` | 16:9 | JPG / WebP，文件大小建议 < 600KB |

> 画布逻辑尺寸是 **1920×1080**（见 `useCanvasAutosave` / `EditorCanvas.vue` 默认值）。背景图按这个比例提供，不会被拉伸变形。

## 风格统一原则

- **不带文字、人物**：背景图只做场景，所有标题/讲解词由学生在编辑器里加
- **不要有过强的视觉焦点**：避免压制学生添加的展品
- **色调务必整体统一**：同一个模板内的多张图保持同色系（暖红 / 古铜 / 冷蓝 / 米白 等）

---

## 1. 红色记忆长征展（`red_journey_long_march`）

主题色：**暖红 + 黄褐**。整体偏写实摄影 / 厚重水彩。

### 文件清单

| 文件 | 主题 | AI Prompt（英文，效果最佳） | 中文备选 |
|---|---|---|---|
| `cover.jpg` | 综合封面（缩略图） | `panoramic poster of Chinese long march, snowy mountains and grasslands, warm red sunset, no people, no text, 16:9` | 长征综合封面海报，雪山草地，暖红夕阳 |
| `ruijin.jpg` | 瑞金革命纪念碑夜色 | `revolutionary monument in ruijin at night, red star on top, warm street lights, autumn trees, photorealistic, 1920x1080, no people` | 瑞金革命纪念碑夜景，红星顶塔，秋叶 |
| `snow_mountain.jpg` | 雪山草地空场 | `snow-capped mountain range with grasslands at foreground, dawn light, cinematic wide shot, no people, 1920x1080, photorealistic` | 雪山草地远景，黎明光线，史诗电影感 |
| `luding_bridge.jpg` | 飞夺泸定桥 | `iron chain bridge crossing a misty canyon, mountains in background, dramatic light, 1920x1080, no people, photorealistic` | 铁索桥跨越峡谷，群山背景，戏剧光线 |
| `yan_an.jpg` | 延安窑洞群 | `loess plateau cave dwellings (yaodong), terraced fields, golden afternoon light, 1920x1080, no people, photorealistic` | 黄土高原窑洞群，梯田，金色午后阳光 |
| `exit.jpg` | 升旗仪式日出 | `chinese flag raising ceremony at sunrise, no specific landmark, warm orange sky, 1920x1080, no people in foreground` | 日出升旗仪式，暖橙天空 |

---

## 2. 博物馆青铜文物展（`museum_bronze_hall`）

主题色：**深褐 + 暗金 + 大理石**。室内博物馆调性。

### 文件清单

| 文件 | 主题 | AI Prompt | 中文备选 |
|---|---|---|---|
| `cover.jpg` | 博物馆综合封面 | `chinese bronze artifacts museum poster, dark background, golden bronze ware silhouette, no text, 16:9` | 中国青铜博物馆海报，深底，铜器剪影 |
| `entrance.jpg` | 博物馆门厅 | `museum lobby with grand columns, marble floor, soft warm lighting, empty hall, no people, 1920x1080, architectural photography` | 博物馆大厅柱廊，大理石地面，暖光，无人 |
| `hall.jpg` | 青铜展厅（带展柜） | `museum hall with empty glass display cabinets along the walls, dim warm spotlights, dark wooden floor, 1920x1080, no people` | 博物馆展厅，墙边空玻璃展柜，暖色聚光 |
| `comments.jpg` | 留言廊 | `museum corridor with wooden benches and a wall for visitor messages, soft natural light, no people, 1920x1080, photorealistic` | 博物馆留言长廊，木长椅，自然光 |

---

## 3. 研学成果时间轴展（`timeline_learning`）

主题色：**米白 + 浅木色 + 嫩绿**。学院 / 教室 / 实验场景。

### 文件清单

| 文件 | 主题 | AI Prompt | 中文备选 |
|---|---|---|---|
| `cover.jpg` | 研学综合封面 | `student research project timeline poster, soft pastel colors, hand-drawn icons of beaker / book / chart, no text, 16:9` | 学生研学时间轴海报，淡彩色，烧杯/书本/图表手绘图标 |
| `topic.jpg` | 立题阶段（教室黑板） | `empty classroom with green chalkboard, wooden desks, soft window light, 1920x1080, no people, photorealistic` | 空教室，绿色黑板，木桌，柔和窗光 |
| `research.jpg` | 调研阶段（户外考察） | `outdoor field research site, mountains in background, wooden table with empty notebooks, sunny day, 1920x1080, no people` | 户外考察基地，山地背景，木桌空笔记本 |
| `result.jpg` | 成果阶段（展示桌） | `light wooden display table with empty space for project results, brick wall background, warm lighting, 1920x1080, no people, photorealistic` | 浅色木质展示桌，砖墙背景，暖光 |
| `defense.jpg` | 答辩阶段（阶梯教室） | `empty lecture hall with tiered seating, projector screen, wooden lectern, soft ambient light, 1920x1080, no people` | 空阶梯教室，投影幕，木讲台，柔光 |

---

## 4. 地图探索式文化展（`map_culture_tour`）

主题色：**羊皮纸黄 + 朱砂红 + 古墨黑**。古地图风。

### 文件清单

| 文件 | 主题 | AI Prompt | 中文备选 |
|---|---|---|---|
| `cover.jpg` | 文化地图综合封面 | `ancient chinese silk road map illustration, parchment background, ink wash style, no text, 16:9` | 古中国丝绸之路地图，羊皮纸底，水墨风 |
| `ancient.jpg` | 古建文化（中式建筑） | `traditional chinese ancient architecture courtyard, with red columns and curved roof tiles, soft mist, 1920x1080, no people, photorealistic` | 中式古建院落，红柱黛瓦，薄雾 |
| `silk_road.jpg` | 丝绸之路（沙漠商队远景） | `silk road desert with sand dunes at sunset, distant camel caravan silhouettes, golden light, 1920x1080, photorealistic` | 丝绸之路沙漠日落，远处商队驼影，金色光 |
| `dunhuang.jpg` | 敦煌壁画洞窟 | `dunhuang mogao cave interior with empty wall sections, warm cave lighting, ancient mural style background, 1920x1080, no people` | 敦煌莫高窟内部，墙面留白，暖洞光 |

---

## 5. 简约白板展（`minimal_whiteboard`）

主题色：**纯白 + 米白**。极简，适合 AI 一键生成。

### 文件清单

| 文件 | 主题 | AI Prompt | 中文备选 |
|---|---|---|---|
| `cover.jpg` | 白板综合封面 | `minimal blank canvas poster, pure white background with subtle paper texture, no text, 16:9` | 极简空白海报，纯白带纸张纹理 |
| `canvas.jpg` | 自由画布 | `pure off-white textured paper background, very subtle grid lines, no objects, 1920x1080, professional photography` | 极淡米白纸张纹理，淡色网格线，无物 |

---

## 推荐 AI 生图工具

| 工具 | 免费额度 | 中文 prompt 友好度 | 备注 |
|---|---|---|---|
| **通义万相**（阿里） | 每日有免费额度 | ⭐⭐⭐⭐⭐ | 最适合中文场景，写"国风/红色/水墨"效果好 |
| **即梦**（字节） | 较多免费额度 | ⭐⭐⭐⭐⭐ | 同上 |
| **豆包生图** | 免费 | ⭐⭐⭐⭐ | 简单无门槛 |
| **Midjourney** | 付费 $10/月起 | ⭐⭐⭐ | 英文 prompt，质量最高 |
| **DALL-E 3**（ChatGPT Plus） | 付费 | ⭐⭐⭐⭐ | 英文 prompt，对详细描述理解好 |

## 验收检查表

提交素材前自检：

- [ ] 文件名严格匹配本清单（拼写大小写都要一致）
- [ ] 封面 ~400×240，展区背景 ~1920×1080
- [ ] 文件格式 JPG / WebP（**不要 PNG**，会过大）
- [ ] 文件大小：封面 < 80KB，背景 < 600KB（用 [TinyPNG](https://tinypng.com) 压一下）
- [ ] 图内**不带文字**
- [ ] 图内**不带人物特写**
- [ ] 同一模板内整体色调一致

## 临时占位方案

如果暂时没图，可以先用纯色 PNG / SVG 占位，让模板卡片不至于显示 `404`。生成方式（PowerShell）：

```powershell
# 用 ImageMagick 生成纯色占位图（需要先 choco install imagemagick）
magick convert -size 1920x1080 xc:#8B0000 frontend/public/templates/red_journey_long_march/ruijin.jpg
magick convert -size 400x240 xc:#8B0000 frontend/public/templates/red_journey_long_march/cover.jpg
```

或者直接用一个统一的 1×1 像素 placeholder PNG，扩展到所有路径。
