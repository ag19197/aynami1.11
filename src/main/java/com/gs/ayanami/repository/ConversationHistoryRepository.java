package com.gs.ayanami.repository;

import com.gs.ayanami.model.ConversationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, Long> {
    List<ConversationHistory> findByUserIdOrderByCreatedAtDesc(String userId);
}