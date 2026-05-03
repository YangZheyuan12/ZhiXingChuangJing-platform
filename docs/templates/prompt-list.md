# 模板素材生成 · 提示词清单（21 张）

> 本文件给 AI 生图任务排了序号 01–21，每张独立可复制的**英文 prompt**、**中文 prompt** 和 **negative prompt**。
>
> **生成完成后**：把图片按 `01.jpg`、`02.jpg` ... `21.jpg` 命名，全部丢进
> `template-assets-inbox/` 目录（仓库根目录），告诉我一句"图传完了"，
> 我会跑 `tools/place-template-assets.ps1` 自动按映射重命名到 `frontend/public/templates/<code>/<file>.jpg` 对应位置。

## 通用要求（所有 21 张共用）

- **格式**：`.jpg`（**不要 .png**，太大）
- **质量**：JPG 质量 80-90，封面 < 100KB，背景 < 700KB
- **尺寸**：
  - **封面**（序号 01、07、11、16、20）：`400 × 240 px`
  - **背景**（其余 16 张）：`1920 × 1080 px`
- **画面**：不带文字、不带人物、色调整洁
- **风格统一**：同组内色调一致（同一个模板的多张图同色系）

## 通用 Negative Prompt（适用全部）

```
text, watermark, logo, signature, people, faces, human figures, low quality, blurry, noisy, jpeg artifacts, distorted, deformed, bad anatomy, overexposed, underexposed, cartoon, anime
```

中文 Midjourney/ComfyUI 派：

```
文字、水印、Logo、签名、人物、人脸、人形、低质量、模糊、噪点、压缩失真、变形、过曝、欠曝、卡通、动漫
```

---

# 📦 组 1 · 红色记忆长征展（`red_journey_long_march`）

**组内色调**：暖红 + 黄褐。尽量写实摄影或厚重国风水彩。

## 【01】综合封面海报 (`cover.jpg`)

- **尺寸**：`400 × 240 px`
- **最终文件**：`frontend/public/templates/red_journey_long_march/cover.jpg`

**英文 prompt**：

```
panoramic poster collage of chinese long march journey, snow mountains, red star banner, mountain range at warm sunset, cinematic wide shot, dramatic sky, oil painting style, rich red and ochre tones, no text, no people, no watermark, aspect ratio 5:3, high detail
```

**中文 prompt**（通义万相 / 即梦）：

```
中国长征主题全景海报拼贴，雪山山脉，红星旗帜，暖色夕阳下的群山，电影感广角镜头，戏剧性天空，油画风格，浓郁红色与赭石色调，无文字，无人物，无水印，宽高比 5:3，高细节
```

## 【02】瑞金革命纪念碑夜色 (`ruijin.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/red_journey_long_march/ruijin.jpg`

**英文 prompt**：

```
revolutionary monument in ruijin at dusk, tall stone obelisk with a glowing red star on top, warm street lamps, surrounding autumn trees with golden leaves, soft mist, photorealistic, cinematic lighting, 1920x1080 landscape, no text, no people, no watermark
```

**中文 prompt**：

```
瑞金革命纪念碑傍晚景色，高耸的石质方尖碑顶端发光的红星，暖色路灯，周围金黄色秋叶的树木，轻薄雾气，照片级写实，电影感光线，1920x1080 横版，无文字，无人物，无水印
```

## 【03】雪山草地空场远景 (`snow_mountain.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/red_journey_long_march/snow_mountain.jpg`

**英文 prompt**：

```
snow-capped mountain range with vast green-brown grasslands in the foreground, early morning dawn light, epic cinematic wide shot, warm sunrise tones on peaks, no people, no text, photorealistic nature photography, 1920x1080 landscape, high detail
```

**中文 prompt**：

```
积雪覆盖的山脉，前景大片青褐色草原，清晨黎明光线，史诗电影感广角，山峰被暖色日出染色，无人物，无文字，照片级写实自然摄影，1920x1080 横版，高细节
```

## 【04】飞夺泸定桥峡谷 (`luding_bridge.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/red_journey_long_march/luding_bridge.jpg`

**英文 prompt**：

```
iron chain bridge crossing a deep misty canyon, raging river below, rocky cliffs on both sides, dramatic overcast sky, low angle shot, cold bluish-grey tones with warm red rust on chains, photorealistic, 1920x1080 landscape, no people, no text
```

**中文 prompt**：

```
铁索桥跨越深峡谷，下方奔腾河流，两侧岩壁，阴沉戏剧天空，低角度镜头，冷蓝灰色调中带暖红锈色铁链，照片级写实，1920x1080 横版，无人物，无文字
```

## 【05】延安窑洞群 (`yan_an.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/red_journey_long_march/yan_an.jpg`

**英文 prompt**：

```
loess plateau cave dwellings (yaodong) on terraced hillside, golden afternoon light, persimmon trees with orange fruits, soft haze, warm earthy tones, traditional chinese rural landscape, photorealistic, 1920x1080 landscape, no people, no text
```

**中文 prompt**：

```
黄土高原梯田山坡上的窑洞群，金色午后阳光，挂满橙色果实的柿子树，柔和薄雾，暖色大地色调，传统中国乡村风景，照片级写实，1920x1080 横版，无人物，无文字
```

## 【06】胜利升旗日出 (`exit.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/red_journey_long_march/exit.jpg`

**英文 prompt**：

```
empty sunrise scene with a lone flagpole silhouette against a brilliant orange-red sunrise sky, wisps of cloud, mountain range on horizon, epic cinematic composition, no flag details (just pole), photorealistic, 1920x1080 landscape, no people, no text
```

**中文 prompt**：

```
空旷日出场景，孤零零的旗杆剪影映衬在绚丽橙红色日出天空下，几缕云彩，地平线上的山脉，史诗电影构图，旗杆不要显示具体细节，照片级写实，1920x1080 横版，无人物，无文字
```

---

# 🏛️ 组 2 · 博物馆青铜文物展（`museum_bronze_hall`）

**组内色调**：深褐 + 暗金 + 大理石灰白。室内博物馆打光。

## 【07】博物馆综合封面 (`cover.jpg`)

- **尺寸**：`400 × 240 px`
- **最终文件**：`frontend/public/templates/museum_bronze_hall/cover.jpg`

**英文 prompt**：

```
chinese ancient bronze artifact museum poster, dark gradient background, silhouettes of ding tripod and zun vessel in warm bronze tones, subtle ornamental taotie pattern, museum brochure aesthetic, no text, aspect ratio 5:3, high detail
```

**中文 prompt**：

```
中国古代青铜器博物馆海报，深色渐变背景，暖色青铜色的鼎与尊剪影，细致的饕餮纹装饰，博物馆宣传册美学，无文字，宽高比 5:3，高细节
```

## 【08】博物馆大厅门廊 (`entrance.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/museum_bronze_hall/entrance.jpg`

**英文 prompt**：

```
grand museum lobby interior with massive stone columns, polished marble floor reflecting warm overhead lighting, high coffered ceiling, soft natural light through tall windows, empty space, architectural photography, photorealistic, 1920x1080 landscape, no people, no text
```

**中文 prompt**：

```
宏伟博物馆大厅内部，巨大石柱，抛光大理石地面反射暖顶光，高格状藻井天花，高窗射入柔和自然光，空旷空间，建筑摄影，照片级写实，1920x1080 横版，无人物，无文字
```

## 【09】青铜展厅（空展柜） (`hall.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/museum_bronze_hall/hall.jpg`

**英文 prompt**：

```
museum hall interior with a row of empty glass display cabinets along dark wood walls, dim warm directional spotlights inside cabinets, polished dark wood floor, cinematic chiaroscuro lighting, symmetrical composition, photorealistic, 1920x1080 landscape, no people, no text, no objects inside cabinets
```

**中文 prompt**：

```
博物馆展厅内部，深木色墙边一排空玻璃展柜，柜内暖色定向聚光灯柔和照亮，抛光深木地板，电影感明暗对比，对称构图，照片级写实，1920x1080 横版，无人物，无文字，展柜内不要有物品
```

## 【10】参观留言廊 (`comments.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/museum_bronze_hall/comments.jpg`

**英文 prompt**：

```
museum corridor with long wooden benches against a wall with empty cork board space for visitor messages, soft natural daylight from high windows on the right, warm beige and wood tones, peaceful atmosphere, photorealistic, 1920x1080 landscape, no people, no text, empty message board
```

**中文 prompt**：

```
博物馆长廊，墙边长条木椅，墙上留白软木留言板，右侧高窗射入柔和自然日光，暖米色与木色调，宁静氛围，照片级写实，1920x1080 横版，无人物，无文字，留言板留空
```

---

# 🧪 组 3 · 研学成果时间轴展（`timeline_learning`）

**组内色调**：米白 + 浅木色 + 嫩绿。学院 / 实验 / 教室调性。

## 【11】研学综合封面 (`cover.jpg`)

- **尺寸**：`400 × 240 px`
- **最终文件**：`frontend/public/templates/timeline_learning/cover.jpg`

**英文 prompt**：

```
student research project timeline poster, soft pastel colors (cream, mint, light wood), hand-drawn icons of beaker, open book, line chart, compass, playful but minimal layout, no text, aspect ratio 5:3, high detail, editorial illustration style
```

**中文 prompt**：

```
学生研学项目时间轴海报，淡彩色调（米白、薄荷绿、浅木色），手绘图标：烧杯、打开的书、折线图、指南针，俏皮但极简布局，无文字，宽高比 5:3，高细节，杂志插画风格
```

## 【12】立题教室 (`topic.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/timeline_learning/topic.jpg`

**英文 prompt**：

```
empty classroom with large green chalkboard in the center, wooden desks and chairs arranged in rows, soft morning sunlight through tall windows on the left, warm dust motes in the air, clean academic aesthetic, photorealistic, 1920x1080 landscape, no people, no text on chalkboard
```

**中文 prompt**：

```
空教室，中央大块绿色黑板，木质课桌椅整齐排列，左侧高窗射入柔和晨光，空气中漂浮暖色尘埃颗粒，干净学院气息，照片级写实，1920x1080 横版，无人物，黑板无文字
```

## 【13】户外调研场地 (`research.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/timeline_learning/research.jpg`

**英文 prompt**：

```
outdoor field research site on a grassy hillside, wooden folding table with empty open notebooks, a pair of binoculars, small plant specimens in clear jars, distant mountains, clear sunny afternoon, photorealistic nature photography, 1920x1080 landscape, no people, no text
```

**中文 prompt**：

```
户外山坡草地上的野外考察基地，木折叠桌上摊开的空白笔记本，一副双筒望远镜，透明罐中的小植物标本，远处群山，晴朗阳光明媚的午后，照片级写实自然摄影，1920x1080 横版，无人物，无文字
```

## 【14】成果展示桌 (`result.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/timeline_learning/result.jpg`

**英文 prompt**：

```
light wooden display table centered against a warm red-brick wall with creeping ivy, table empty ready for project showcase items, soft warm overhead gallery lighting, inviting modern-rustic exhibition aesthetic, photorealistic, 1920x1080 landscape, no people, no objects on table, no text
```

**中文 prompt**：

```
浅色木质展示桌居中放置于暖红色砖墙前，墙上攀爬常春藤，桌面留空等待放置成果作品，顶部暖色柔光画廊灯光，温馨现代乡村式展览美学，照片级写实，1920x1080 横版，无人物，桌上无物品，无文字
```

## 【15】阶梯教室答辩 (`defense.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/timeline_learning/defense.jpg`

**英文 prompt**：

```
empty tiered lecture hall with wooden seats, large blank projector screen at front, wooden podium on the side, soft ambient ceiling lighting, warm academic atmosphere, clean and modern, photorealistic architectural photography, 1920x1080 landscape, no people, no text on screen
```

**中文 prompt**：

```
空阶梯教室，木质座椅，前方大块空白投影幕布，侧方木讲台，柔和顶部环境灯，温馨学院氛围，干净现代，照片级写实建筑摄影，1920x1080 横版，无人物，幕布无文字
```

---

# 🗺️ 组 4 · 地图探索式文化展（`map_culture_tour`）

**组内色调**：羊皮纸黄 + 朱砂红 + 古墨黑。古地图 / 国风水墨调。

## 【16】文化地图综合封面 (`cover.jpg`)

- **尺寸**：`400 × 240 px`
- **最终文件**：`frontend/public/templates/map_culture_tour/cover.jpg`

**英文 prompt**：

```
ancient chinese silk road map illustration on aged parchment, ink wash painting style, flowing route lines, compass rose, subtle mountain and camel silhouettes, warm yellow-ochre and cinnabar tones, no text, aspect ratio 5:3, high detail
```

**中文 prompt**：

```
泛黄羊皮纸上的古中国丝绸之路地图插画，水墨画风格，流畅路线，罗盘玫瑰，山脉与骆驼剪影，暖色赭黄与朱砂色调，无文字，宽高比 5:3，高细节
```

## 【17】中式古建院落 (`ancient.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/map_culture_tour/ancient.jpg`

**英文 prompt**：

```
traditional chinese ancient courtyard with red vermillion columns, curved grey tile roofs, stone pathway, soft morning mist in background, lanterns hanging, warm red and dark green tones, photorealistic, 1920x1080 landscape, no people, no text
```

**中文 prompt**：

```
中式古建院落，朱红色立柱，青灰色曲瓦屋顶，石板道，背景柔和晨雾，悬挂灯笼，暖红色与深绿色调，照片级写实，1920x1080 横版，无人物，无文字
```

## 【18】丝绸之路沙漠 (`silk_road.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/map_culture_tour/silk_road.jpg`

**英文 prompt**：

```
silk road desert landscape with rolling sand dunes at sunset, distant camel caravan silhouettes on the horizon (small and far), warm golden-orange light casting long shadows, epic cinematic wide shot, photorealistic, 1920x1080 landscape, no text
```

**中文 prompt**：

```
丝绸之路沙漠风景，起伏沙丘日落时分，地平线上远处小小的商队驼影剪影（小而远），暖色金橙色光投下长影，史诗电影感广角，照片级写实，1920x1080 横版，无文字
```

## 【19】敦煌壁画洞窟 (`dunhuang.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/map_culture_tour/dunhuang.jpg`

**英文 prompt**：

```
interior of a dunhuang mogao cave with richly painted mural walls and ceiling (aged colors, lotus patterns, flying apsaras hints), warm cave spotlight lighting, empty center wall section ready for showcase, reverent atmospheric lighting, photorealistic, 1920x1080 landscape, no people, no text
```

**中文 prompt**：

```
敦煌莫高窟洞窟内部，墙壁与穹顶布满精美壁画（陈旧色彩，莲花图案，飞天暗示），暖色洞窟聚光灯，中央墙面留白适合展示，虔敬氛围光线，照片级写实，1920x1080 横版，无人物，无文字
```

---

# 📝 组 5 · 简约白板展（`minimal_whiteboard`）

**组内色调**：纯白 + 米白。极简，AI 一键出图。

## 【20】极简封面 (`cover.jpg`)

- **尺寸**：`400 × 240 px`
- **最终文件**：`frontend/public/templates/minimal_whiteboard/cover.jpg`

**英文 prompt**：

```
minimal blank canvas poster, pure white background with very subtle paper fiber texture, clean and airy, one tiny pastel accent dot in corner, no text, aspect ratio 5:3, professional editorial minimalism
```

**中文 prompt**：

```
极简空白画布海报，纯白背景带极淡纸张纤维质感，干净通透，角落一个微小淡彩点缀，无文字，宽高比 5:3，专业杂志极简风
```

## 【21】自由创作画布 (`canvas.jpg`)

- **尺寸**：`1920 × 1080 px`
- **最终文件**：`frontend/public/templates/minimal_whiteboard/canvas.jpg`

**英文 prompt**：

```
pure off-white textured paper background with very subtle cream gradient from top to bottom, barely visible horizontal ruled lines, clean minimal aesthetic, perfect for placing content on top, photorealistic scan of premium art paper, 1920x1080 landscape, no objects, no text
```

**中文 prompt**：

```
纯米白色带纹理纸张背景，从上到下极淡米白色渐变，几乎不可见的横向线条，干净极简美学，适合放置内容，照片级写实高级画纸扫描质感，1920x1080 横版，无物品，无文字
```

---

# 📤 交付流程

## 第 1 步：生成

1. 把每个 prompt 粘贴到你用的 AI 生图工具
2. 把 negative prompt 也粘进去（如果工具支持）
3. 按编号顺序保存：`01.jpg`、`02.jpg` ... `21.jpg`
4. **确保尺寸大致正确**：封面 400×240 左右，背景 1920×1080 左右（不精确也没关系，稍后可以用工具批量 resize）

## 第 2 步：放置

在仓库根目录新建文件夹 `template-assets-inbox/`（已在 `.gitignore` 里，不会污染 git），把 21 张图全部丢进去：

```
d:\Desktop\计算机设计大赛\ZhiXingChuangJing-Platform\template-assets-inbox\
├── 01.jpg
├── 02.jpg
├── 03.jpg
... (一直到 21.jpg)
```

## 第 3 步：告诉我"图传完了"

我执行 `tools/place-template-assets.ps1`，它会：

1. 自动把 `01.jpg` → `frontend/public/templates/red_journey_long_march/cover.jpg`
2. 自动把 `02.jpg` → `frontend/public/templates/red_journey_long_march/ruijin.jpg`
3. ... 按下面映射全部重命名放入

## 编号到路径的完整映射表

| 编号 | 目标路径 | 尺寸 | 模板 |
|---|---|---|---|
| 01 | `templates/red_journey_long_march/cover.jpg` | 400×240 | 长征 |
| 02 | `templates/red_journey_long_march/ruijin.jpg` | 1920×1080 | 长征 |
| 03 | `templates/red_journey_long_march/snow_mountain.jpg` | 1920×1080 | 长征 |
| 04 | `templates/red_journey_long_march/luding_bridge.jpg` | 1920×1080 | 长征 |
| 05 | `templates/red_journey_long_march/yan_an.jpg` | 1920×1080 | 长征 |
| 06 | `templates/red_journey_long_march/exit.jpg` | 1920×1080 | 长征 |
| 07 | `templates/museum_bronze_hall/cover.jpg` | 400×240 | 文博 |
| 08 | `templates/museum_bronze_hall/entrance.jpg` | 1920×1080 | 文博 |
| 09 | `templates/museum_bronze_hall/hall.jpg` | 1920×1080 | 文博 |
| 10 | `templates/museum_bronze_hall/comments.jpg` | 1920×1080 | 文博 |
| 11 | `templates/timeline_learning/cover.jpg` | 400×240 | 研学 |
| 12 | `templates/timeline_learning/topic.jpg` | 1920×1080 | 研学 |
| 13 | `templates/timeline_learning/research.jpg` | 1920×1080 | 研学 |
| 14 | `templates/timeline_learning/result.jpg` | 1920×1080 | 研学 |
| 15 | `templates/timeline_learning/defense.jpg` | 1920×1080 | 研学 |
| 16 | `templates/map_culture_tour/cover.jpg` | 400×240 | 地图 |
| 17 | `templates/map_culture_tour/ancient.jpg` | 1920×1080 | 地图 |
| 18 | `templates/map_culture_tour/silk_road.jpg` | 1920×1080 | 地图 |
| 19 | `templates/map_culture_tour/dunhuang.jpg` | 1920×1080 | 地图 |
| 20 | `templates/minimal_whiteboard/cover.jpg` | 400×240 | 白板 |
| 21 | `templates/minimal_whiteboard/canvas.jpg` | 1920×1080 | 白板 |

## 允许的交付方式

- **全部 21 张一起交**：最干净
- **分批交**：也可以，比如"先交 1-6 长征系列，其他后续补"，缺哪张就先用占位图
- **其他格式**：如果生成工具只能出 PNG，放进来也行，我脚本里会自动转 JPG

## 如果某张图工具反复生成不满意

告诉我编号，我把这张**单独跳过**，用占位图先顶上。后续你有时间再补。
