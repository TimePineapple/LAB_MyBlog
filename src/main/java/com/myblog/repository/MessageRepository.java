package com.myblog.repository;

import com.myblog.dto.PageResult;
import com.myblog.model.Message;
import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message save(Message message);
    List<Message> findAll();
    Optional<Message> findById(Long id);
    PageResult<Message> search(String keyword, long page, long size);
}
