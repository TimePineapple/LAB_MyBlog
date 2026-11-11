package com.myblog.service;

import com.myblog.dto.PageResult;
import com.myblog.model.Message;
import com.myblog.model.MessageRequest;
import com.myblog.repository.MessageRepository;
import com.myblog.support.QuerySupport;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message create(MessageRequest request) {
        if (request == null || request.content == null || request.content.isBlank()
                || request.content.length() > 1000) {
            throw new IllegalArgumentException("留言内容不能为空，且不能超过 1000 个字符");
        }
        Message message = new Message();
        message.setContent(request.content);
        // 与数据库 DATETIME 秒精度一致，避免重启前后 JSON 时间精度变化。
        message.setCreatedAt(LocalDateTime.now().withNano(0));
        return messageRepository.save(message);
    }

    public Message getById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("message not found"));
    }

    public PageResult<Message> search(String keyword, long page, long size) {
        QuerySupport.validatePage(page, size);
        return messageRepository.search(keyword, page, size);
    }
}
