-- 文博资源示例数据（15 件真实文物）
-- 执行前确保 museum_resources 表已创建

INSERT INTO museum_resources (provider_id, external_id, resource_type, category, dynasty, material, region, title, subtitle, museum_name, cover_url, detail_url, description, tags_json, metadata_json, cache_status, synced_at, created_at, updated_at) VALUES
(1, 'NMCH_001', 'image', '文物', '新石器时代', '陶器', '山东', '蛋壳黑陶高柄杯', '龙山文化代表作', '中国国家博物馆', 'https://example.com/museum/eggshell_pot.jpg', 'https://example.com/museum/detail/001', '龙山文化时期制陶工艺巅峰之作，壁厚仅 0.2-0.3 毫米', '["陶器","龙山文化","新石器"]', '{"era": "新石器时代", "level": "一级文物"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_002', 'image', '文物', '商代', '青铜器', '河南', '后母戊鼎', '世界最大青铜器', '中国国家博物馆', 'https://example.com/museum/houmuwu_ding.jpg', 'https://example.com/museum/detail/002', '商代后期青铜器代表作，重 832.84 公斤', '["青铜器","商代","礼器"]', '{"era": "商代", "level": "一级文物", "weight": "832.84kg"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_003', 'image', '文物', '西周', '青铜器', '陕西', '利簋', '记载武王伐纣', '中国国家博物馆', 'https://example.com/museum/li_gui.jpg', 'https://example.com/museum/detail/003', '内底铭文记载了武王伐纣的具体日期', '["青铜器","西周","铭文"]', '{"era": "西周", "level": "一级文物"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_004', 'image', '文物', '秦代', '陶器', '陕西', '秦兵马俑', '世界第八大奇迹', '秦始皇帝陵博物院', 'https://example.com/museum/terracotta_warrior.jpg', 'https://example.com/museum/detail/004', '秦始皇陵陪葬陶俑，展现了秦军阵容', '["陶俑","秦代","军事"]', '{"era": "秦代", "level": "世界文化遗产"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_005', 'image', '文物', '西汉', '玉器', '河北', '金缕玉衣', '汉代丧葬制度代表', '河北博物院', 'https://example.com/museum/jade_suit.jpg', 'https://example.com/museum/detail/005', '汉代皇帝和高级贵族的殓服，用金丝编缀玉片', '["玉器","汉代","丧葬"]', '{"era": "西汉", "level": "一级文物"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_006', 'image', '文物', '东汉', '青铜器', '甘肃', '马踏飞燕', '中国旅游标志', '甘肃省博物馆', 'https://example.com/museum/flying_horse.jpg', 'https://example.com/museum/detail/006', '东汉青铜器，展现了骏马奔腾的姿态', '["青铜器","东汉","雕塑"]', '{"era": "东汉", "level": "一级文物"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_007', 'image', '文物', '唐代', '陶瓷', '陕西', '唐三彩载乐驼', '丝绸之路见证', '陕西历史博物馆', 'https://example.com/museum/tricolor_camel.jpg', 'https://example.com/museum/detail/007', '唐代三彩陶器，展现了丝路文化交流', '["陶瓷","唐代","丝绸之路"]', '{"era": "唐代", "level": "一级文物"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_008', 'image', '文物', '北宋', '书画', '北京', '清明上河图', '中国十大传世名画', '故宫博物院', 'https://example.com/museum/qingming_shanghe.jpg', 'https://example.com/museum/detail/008', '张择端绘，描绘北宋汴京城市风貌', '["书画","北宋","风俗画"]', '{"era": "北宋", "level": "国宝", "artist": "张择端"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_009', 'image', '文物', '元代', '陶瓷', '江西', '元青花萧何月下追韩信梅瓶', '元青花代表作', '南京博物院', 'https://example.com/museum/blue_white_vase.jpg', 'https://example.com/museum/detail/009', '元代青花瓷器精品，绘有历史故事', '["陶瓷","元代","青花瓷"]', '{"era": "元代", "level": "一级文物"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_010', 'image', '文物', '明代', '金银器', '北京', '明金翼善冠', '明代皇帝礼冠', '明十三陵博物馆', 'https://example.com/museum/golden_crown.jpg', 'https://example.com/museum/detail/010', '明神宗万历皇帝金冠，金丝编织工艺', '["金银器","明代","冠冕"]', '{"era": "明代", "level": "一级文物"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_011', 'image', '文物', '清代', '陶瓷', '北京', '清乾隆各种釉彩大瓶', '瓷母', '故宫博物院', 'https://example.com/museum/qianlong_vase.jpg', 'https://example.com/museum/detail/011', '集历代釉彩之大成，工艺登峰造极', '["陶瓷","清代","乾隆"]', '{"era": "清代", "level": "国宝"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_012', 'image', '文物', '新石器时代', '玉器', '浙江', '良渚玉琮王', '良渚文化代表', '浙江省博物馆', 'https://example.com/museum/liangzhu_cong.jpg', 'https://example.com/museum/detail/012', '良渚文化玉器，刻有神人兽面纹', '["玉器","良渚文化","礼器"]', '{"era": "新石器时代", "level": "一级文物"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_013', 'image', '文物', '商代', '青铜器', '四川', '三星堆青铜面具', '古蜀文明', '三星堆博物馆', 'https://example.com/museum/bronze_mask.jpg', 'https://example.com/museum/detail/013', '三星堆遗址出土，造型奇特神秘', '["青铜器","商代","古蜀"]', '{"era": "商代", "level": "一级文物"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_014', 'image', '文物', '唐代', '金银器', '陕西', '镶金兽首玛瑙杯', '唐代中外交流见证', '陕西历史博物馆', 'https://example.com/museum/agate_cup.jpg', 'https://example.com/museum/detail/014', '唐代酒器，造型受西方影响', '["金银器","唐代","酒器"]', '{"era": "唐代", "level": "一级文物"}', 'fresh', NOW(), NOW(), NOW()),

(1, 'NMCH_015', 'image', '文物', '战国', '青铜器', '湖北', '越王勾践剑', '天下第一剑', '湖北省博物馆', 'https://example.com/museum/goujian_sword.jpg', 'https://example.com/museum/detail/015', '越王勾践自用剑，千年不锈', '["青铜器","战国","兵器"]', '{"era": "战国", "level": "国宝"}', 'fresh', NOW(), NOW(), NOW());
