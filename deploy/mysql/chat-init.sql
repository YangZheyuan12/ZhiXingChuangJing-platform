-- 聊天对话表
CREATE TABLE IF NOT EXISTS chat_conversations (
    id VARCHAR(36) PRIMARY KEY COMMENT '对话ID，UUID格式',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(255) COMMENT '对话标题',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    message_count INT DEFAULT 0 COMMENT '消息数量',
    KEY idx_user_id (user_id),
    KEY idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天对话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    conversation_id VARCHAR(36) NOT NULL COMMENT '对话ID',
    user_id BIGINT COMMENT '用户ID，为null表示是助手消息',
    content LONGTEXT NOT NULL COMMENT '消息内容',
    role VARCHAR(10) NOT NULL COMMENT '角色：user或assistant',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_conversation_id (conversation_id),
    KEY idx_created_at (created_at),
    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天消息表';
