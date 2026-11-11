package com.myblog.repository;

import com.myblog.dto.PageResult;
import com.myblog.model.Message;
import com.myblog.support.QuerySupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// 仅供 Week05 教学与独立测试。没有 @Repository / @Component，不进入生产链路。
// 内存匹配区分大小写，不用于模拟 MySQL 排序规则或验收数据库分页。
public class InMemoryMessageRepository implements MessageRepository {
    private final Map<Long, Message> messages = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public synchronized Message save(Message message) {
        message.setId(nextId++);
        messages.put(message.getId(), copy(message));
        return copy(message);
    }

    @Override
    public synchronized List<Message> findAll() {
        return messages.values().stream().map(InMemoryMessageRepository::copy).toList();
    }

    @Override
    public synchronized Optional<Message> findById(Long id) {
        return Optional.ofNullable(messages.get(id)).map(InMemoryMessageRepository::copy);
    }

    @Override
    public synchronized PageResult<Message> search(String keyword, long page, long size) {
        QuerySupport.validatePage(page, size);
        List<Message> matching = messages.values().stream()
                .filter(message -> keyword == null || keyword.isBlank()
                        || message.getContent().contains(keyword))
                .sorted(Comparator.comparing(Message::getCreatedAt)
                        .thenComparing(Message::getId).reversed())
                .toList();
        long offset = (page - 1) * size;
        List<Message> items = new ArrayList<>();
        if (offset < matching.size()) {
            int from = (int) offset;
            int to = from + (int) Math.min(size, matching.size() - from);
            matching.subList(from, to).forEach(message -> items.add(copy(message)));
        }
        return new PageResult<>(items, matching.size(), page, size);
    }

    private static Message copy(Message original) {
        Message copy = new Message();
        copy.setId(original.getId());
        copy.setContent(original.getContent());
        copy.setCreatedAt(original.getCreatedAt());
        return copy;
    }
}
