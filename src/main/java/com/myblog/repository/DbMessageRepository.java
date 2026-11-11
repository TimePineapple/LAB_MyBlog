package com.myblog.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myblog.dto.PageResult;
import com.myblog.mapper.MessageMapper;
import com.myblog.model.Message;
import com.myblog.support.QuerySupport;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class DbMessageRepository implements MessageRepository {
    private final MessageMapper messageMapper;

    public DbMessageRepository(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public Message save(Message message) {
        messageMapper.insert(message);
        return message;
    }

    @Override
    public List<Message> findAll() {
        return messageMapper.selectList(new LambdaQueryWrapper<Message>().orderByAsc(Message::getId));
    }

    @Override
    public Optional<Message> findById(Long id) {
        return Optional.ofNullable(messageMapper.selectById(id));
    }

    @Override
    public PageResult<Message> search(String keyword, long page, long size) {
        QuerySupport.validatePage(page, size);
        LambdaQueryWrapper<Message> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            query.like(Message::getContent, QuerySupport.literalKeyword(keyword));
        }
        query.orderByDesc(Message::getCreatedAt).orderByDesc(Message::getId);
        Page<Message> result = messageMapper.selectPage(new Page<>(page, size), query);
        return PageResult.of(result);
    }
}
