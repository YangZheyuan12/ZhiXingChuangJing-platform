-- ============================================================
-- V3：补充 5 套展厅模板种子数据
-- 适用场景：已经跑过 V2__exhibition_upgrade.sql 的老库（之前只有 1 个 immersive_2.5d_default）
-- 全部使用 INSERT IGNORE，重复执行幂等。
-- 图片资源约定路径：frontend/public/templates/<template_code>/<file>.jpg
-- 详情见 docs/templates/asset-manifest.md
-- ============================================================

INSERT IGNORE INTO exhibition_templates
  (template_code, template_name, template_type, difficulty_level, description, preview_url, zones_config, suitable_subjects, suitable_grades, status)
VALUES
('red_journey_long_march', '红色记忆长征展', 'immersive_2.5d', 'intermediate',
 '沿革命路线漫游 5 个历史场景，适合党史学习、思政课、爱国主义教育主题展示。',
 '/templates/red_journey_long_march/cover.jpg',
 '{"zones":[
   {"zoneCode":"ruijin","zoneType":"entrance","title":"瑞金·星火","backgroundUrl":"/templates/red_journey_long_march/ruijin.jpg","transitionIn":"fade","layoutConfig":{"slots":[{"code":"title","x":30,"y":18,"w":40,"h":12,"label":"展厅标题"},{"code":"intro","x":25,"y":42,"w":50,"h":22,"label":"展厅简介"}]},"hotspots":[{"type":"navigation","targetZoneCode":"snow_mountain","icon":"arrow-right","x":85,"y":50,"w":8,"h":12}]},
   {"zoneCode":"snow_mountain","zoneType":"gallery","title":"雪山草地","backgroundUrl":"/templates/red_journey_long_march/snow_mountain.jpg","transitionIn":"slide-left","layoutConfig":{"slots":[{"code":"exhibit-1","x":8,"y":22,"w":24,"h":52,"label":"展品 1"},{"code":"exhibit-2","x":38,"y":22,"w":24,"h":52,"label":"展品 2"},{"code":"exhibit-3","x":68,"y":22,"w":24,"h":52,"label":"展品 3"}]},"hotspots":[{"type":"navigation","targetZoneCode":"ruijin","icon":"arrow-left","x":4,"y":50,"w":8,"h":12},{"type":"navigation","targetZoneCode":"luding_bridge","icon":"arrow-right","x":88,"y":50,"w":8,"h":12}]},
   {"zoneCode":"luding_bridge","zoneType":"gallery","title":"飞夺泸定桥","backgroundUrl":"/templates/red_journey_long_march/luding_bridge.jpg","transitionIn":"fade","layoutConfig":{"slots":[{"code":"exhibit-main","x":22,"y":18,"w":56,"h":64,"label":"主展品"},{"code":"caption","x":15,"y":86,"w":70,"h":10,"label":"展品说明"}]},"hotspots":[{"type":"navigation","targetZoneCode":"snow_mountain","icon":"arrow-left","x":4,"y":50,"w":8,"h":12},{"type":"navigation","targetZoneCode":"yan_an","icon":"arrow-right","x":88,"y":50,"w":8,"h":12}]},
   {"zoneCode":"yan_an","zoneType":"gallery","title":"延安会师","backgroundUrl":"/templates/red_journey_long_march/yan_an.jpg","transitionIn":"slide-right","layoutConfig":{"slots":[{"code":"exhibit-l","x":12,"y":24,"w":34,"h":52,"label":"展品左"},{"code":"exhibit-r","x":54,"y":24,"w":34,"h":52,"label":"展品右"}]},"hotspots":[{"type":"navigation","targetZoneCode":"luding_bridge","icon":"arrow-left","x":4,"y":50,"w":8,"h":12},{"type":"navigation","targetZoneCode":"exit","icon":"arrow-right","x":88,"y":50,"w":8,"h":12}]},
   {"zoneCode":"exit","zoneType":"exit","title":"胜利升旗","backgroundUrl":"/templates/red_journey_long_march/exit.jpg","transitionIn":"fade","layoutConfig":{"slots":[{"code":"summary","x":20,"y":18,"w":60,"h":24,"label":"参观总结"},{"code":"credits","x":25,"y":52,"w":50,"h":30,"label":"鸣谢"}]},"hotspots":[{"type":"navigation","targetZoneCode":"yan_an","icon":"arrow-left","x":4,"y":50,"w":8,"h":12}]}
 ]}',
 JSON_ARRAY('思政','历史','语文'),
 JSON_ARRAY('小学高年级','初中','高中'),
 'active'),

('museum_bronze_hall', '博物馆青铜文物展', 'basic_gallery', 'beginner',
 '简洁三段式画廊布局，聚焦文物本体展示，适合博物馆研学、文物鉴赏类展示。',
 '/templates/museum_bronze_hall/cover.jpg',
 '{"zones":[
   {"zoneCode":"entrance","zoneType":"entrance","title":"博物馆大厅","backgroundUrl":"/templates/museum_bronze_hall/entrance.jpg","transitionIn":"fade","layoutConfig":{"slots":[{"code":"title","x":28,"y":20,"w":44,"h":14,"label":"展厅标题"},{"code":"intro","x":25,"y":46,"w":50,"h":24,"label":"展厅简介"}]},"hotspots":[{"type":"navigation","targetZoneCode":"hall","icon":"arrow-right","x":86,"y":50,"w":8,"h":12}]},
   {"zoneCode":"hall","zoneType":"gallery","title":"青铜展厅","backgroundUrl":"/templates/museum_bronze_hall/hall.jpg","transitionIn":"slide-left","layoutConfig":{"slots":[{"code":"exhibit-1","x":6,"y":24,"w":20,"h":52,"label":"展品 1"},{"code":"exhibit-2","x":30,"y":24,"w":20,"h":52,"label":"展品 2"},{"code":"exhibit-3","x":54,"y":24,"w":20,"h":52,"label":"展品 3"},{"code":"exhibit-4","x":78,"y":24,"w":20,"h":52,"label":"展品 4"}]},"hotspots":[{"type":"navigation","targetZoneCode":"entrance","icon":"arrow-left","x":4,"y":50,"w":8,"h":12},{"type":"navigation","targetZoneCode":"comments","icon":"arrow-right","x":88,"y":50,"w":8,"h":12}]},
   {"zoneCode":"comments","zoneType":"exit","title":"参观留言廊","backgroundUrl":"/templates/museum_bronze_hall/comments.jpg","transitionIn":"fade","layoutConfig":{"slots":[{"code":"summary","x":20,"y":18,"w":60,"h":24,"label":"参观感想"},{"code":"credits","x":25,"y":52,"w":50,"h":30,"label":"鸣谢"}]},"hotspots":[{"type":"navigation","targetZoneCode":"hall","icon":"arrow-left","x":4,"y":50,"w":8,"h":12}]}
 ]}',
 JSON_ARRAY('历史','美术','语文'),
 JSON_ARRAY('小学','初中','高中'),
 'active'),

('timeline_learning', '研学成果时间轴展', 'timeline', 'intermediate',
 '按研学项目阶段顺序漫游 4 个时间节点，适合综合实践、跨学科项目成果汇报。',
 '/templates/timeline_learning/cover.jpg',
 '{"zones":[
   {"zoneCode":"topic","zoneType":"timeline","title":"立题","backgroundUrl":"/templates/timeline_learning/topic.jpg","transitionIn":"fade","layoutConfig":{"slots":[{"code":"title","x":28,"y":20,"w":44,"h":14,"label":"研究主题"},{"code":"hypothesis","x":25,"y":46,"w":50,"h":24,"label":"研究假设"}]},"hotspots":[{"type":"navigation","targetZoneCode":"research","icon":"arrow-right","x":86,"y":50,"w":8,"h":12}]},
   {"zoneCode":"research","zoneType":"timeline","title":"调研","backgroundUrl":"/templates/timeline_learning/research.jpg","transitionIn":"slide-left","layoutConfig":{"slots":[{"code":"data-1","x":10,"y":22,"w":36,"h":54,"label":"调研资料 1"},{"code":"data-2","x":54,"y":22,"w":36,"h":54,"label":"调研资料 2"}]},"hotspots":[{"type":"navigation","targetZoneCode":"topic","icon":"arrow-left","x":4,"y":50,"w":8,"h":12},{"type":"navigation","targetZoneCode":"result","icon":"arrow-right","x":88,"y":50,"w":8,"h":12}]},
   {"zoneCode":"result","zoneType":"timeline","title":"成果","backgroundUrl":"/templates/timeline_learning/result.jpg","transitionIn":"slide-left","layoutConfig":{"slots":[{"code":"main-result","x":22,"y":18,"w":56,"h":50,"label":"主成果"},{"code":"sub-1","x":10,"y":72,"w":36,"h":18,"label":"副成果 1"},{"code":"sub-2","x":54,"y":72,"w":36,"h":18,"label":"副成果 2"}]},"hotspots":[{"type":"navigation","targetZoneCode":"research","icon":"arrow-left","x":4,"y":50,"w":8,"h":12},{"type":"navigation","targetZoneCode":"defense","icon":"arrow-right","x":88,"y":50,"w":8,"h":12}]},
   {"zoneCode":"defense","zoneType":"exit","title":"答辩","backgroundUrl":"/templates/timeline_learning/defense.jpg","transitionIn":"fade","layoutConfig":{"slots":[{"code":"slides","x":20,"y":18,"w":60,"h":48,"label":"答辩 PPT"},{"code":"feedback","x":25,"y":70,"w":50,"h":20,"label":"教师反馈"}]},"hotspots":[{"type":"navigation","targetZoneCode":"result","icon":"arrow-left","x":4,"y":50,"w":8,"h":12}]}
 ]}',
 JSON_ARRAY('综合实践','科学','信息技术'),
 JSON_ARRAY('小学高年级','初中','高中'),
 'active'),

('map_culture_tour', '地图探索式文化展', 'map_exploration', 'intermediate',
 '以中华文化地理脉络为主线串联 3 个展区，适合文化、地理、历史综合主题展示。',
 '/templates/map_culture_tour/cover.jpg',
 '{"zones":[
   {"zoneCode":"ancient","zoneType":"gallery","title":"古建文化","backgroundUrl":"/templates/map_culture_tour/ancient.jpg","transitionIn":"fade","layoutConfig":{"slots":[{"code":"main","x":18,"y":18,"w":52,"h":56,"label":"主展品"},{"code":"intro","x":74,"y":24,"w":22,"h":50,"label":"地图标注"}]},"hotspots":[{"type":"navigation","targetZoneCode":"silk_road","icon":"arrow-right","x":88,"y":80,"w":8,"h":12}]},
   {"zoneCode":"silk_road","zoneType":"gallery","title":"丝绸之路","backgroundUrl":"/templates/map_culture_tour/silk_road.jpg","transitionIn":"slide-left","layoutConfig":{"slots":[{"code":"caravan","x":8,"y":24,"w":40,"h":50,"label":"商队"},{"code":"route","x":52,"y":24,"w":40,"h":50,"label":"沿线节点"}]},"hotspots":[{"type":"navigation","targetZoneCode":"ancient","icon":"arrow-left","x":4,"y":80,"w":8,"h":12},{"type":"navigation","targetZoneCode":"dunhuang","icon":"arrow-right","x":88,"y":80,"w":8,"h":12}]},
   {"zoneCode":"dunhuang","zoneType":"exit","title":"敦煌壁画","backgroundUrl":"/templates/map_culture_tour/dunhuang.jpg","transitionIn":"fade","layoutConfig":{"slots":[{"code":"mural-main","x":18,"y":14,"w":64,"h":54,"label":"主壁画"},{"code":"caption","x":20,"y":72,"w":60,"h":18,"label":"题记"}]},"hotspots":[{"type":"navigation","targetZoneCode":"silk_road","icon":"arrow-left","x":4,"y":80,"w":8,"h":12}]}
 ]}',
 JSON_ARRAY('历史','地理','美术'),
 JSON_ARRAY('小学高年级','初中','高中'),
 'active'),

('minimal_whiteboard', '简约白板展', 'basic_gallery', 'beginner',
 '单一空白画布，给学生最大自由度，适合自由创作、便签墙、创意发散类作业。',
 '/templates/minimal_whiteboard/cover.jpg',
 '{"zones":[
   {"zoneCode":"canvas","zoneType":"gallery","title":"自由创作画布","backgroundUrl":"/templates/minimal_whiteboard/canvas.jpg","transitionIn":"fade","layoutConfig":{"slots":[]},"hotspots":[]}
 ]}',
 JSON_ARRAY('美术','信息技术','综合实践'),
 JSON_ARRAY('小学','初中','高中'),
 'active');
