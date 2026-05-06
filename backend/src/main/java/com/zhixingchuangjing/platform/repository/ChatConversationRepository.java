package com.zhixingchuangjing.platform.repository;

import com.zhixingchuangjing.platform.entity.ChatConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversationEntity, String> {
    List<ChatConversationEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
