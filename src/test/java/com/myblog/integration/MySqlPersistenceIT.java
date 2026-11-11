package com.myblog.integration;

import com.myblog.dto.ArticleCreateRequest;
import com.myblog.dto.ArticleUpdateRequest;
import com.myblog.model.Message;
import com.myblog.model.MessageRequest;
import com.myblog.service.ArticleService;
import com.myblog.service.MessageService;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 只由显式 -Pmysql-it verify 执行。需要预先在独立测试库中创建两张表。
@SpringBootTest
@ActiveProfiles("integration")
@Transactional
class MySqlPersistenceIT {
    @Autowired private MessageService messages;
    @Autowired private ArticleService articles;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void databaseFilteringPagingAndDirectInsertUseOneSourceOfTruth() {
        String prefix = "it-" + UUID.randomUUID();
        for (int i = 0; i < 10; i++) {
            MessageRequest request = new MessageRequest();
            request.content = prefix + (i < 3 ? "-hello-" : "-other-") + i;
            Message saved = messages.create(request);
            assertThat(saved.getId()).isPositive();
            assertThat(messages.getById(saved.getId()).getContent()).isEqualTo(request.content);
        }
        var hello = messages.search(prefix + "-hello", 1, 5);
        assertThat(hello.total()).isEqualTo(3);
        assertThat(hello.items()).allMatch(message -> message.getContent().contains(prefix + "-hello"));
        assertThat(messages.search(prefix + "-absent", 1, 5).items()).isEmpty();
        assertThat(messages.search(prefix + "-absent", 1, 5).total()).isZero();
        var first = messages.search(prefix, 1, 5);
        var second = messages.search(prefix, 2, 5);
        assertThat(first.items()).hasSize(5);
        assertThat(second.items()).hasSize(5);
        assertThat(first.total()).isEqualTo(10);
        assertThat(second.total()).isEqualTo(10);
        HashSet<Long> ids = new HashSet<>();
        first.items().forEach(message -> ids.add(message.getId()));
        second.items().forEach(message -> assertThat(ids.add(message.getId())).isTrue());
        assertThat(messages.search(prefix, 99, 5).items()).isEmpty();
        assertThat(messages.search(prefix, 99, 5).total()).isEqualTo(10);
        assertThat(messages.search(prefix + "-hello", 1, 2).items()).hasSize(2);
        assertThat(messages.search(prefix + "-hello", 2, 2).items()).hasSize(1);
        assertThat(messages.search(prefix + "-hello", 2, 2).total()).isEqualTo(3);

        jdbc.update("INSERT INTO message(content) VALUES (?)", prefix + "-direct-db-insert");
        assertThat(messages.search(prefix + "-direct-db-insert", 1, 5).total()).isEqualTo(1);
        jdbc.update("INSERT INTO message(content) VALUES (?)", prefix + "-100%_literal");
        jdbc.update("INSERT INTO message(content) VALUES (?)", prefix + "-100xxliteral");
        assertThat(messages.search(prefix + "-100%_literal", 1, 5).total()).isEqualTo(1);
    }

    @Test
    void articleLifecyclePersistsAndLeavesMessagesUntouched() {
        String prefix = "article-it-" + UUID.randomUUID();
        MessageRequest message = new MessageRequest();
        message.content = prefix;
        messages.create(message);
        var before = messages.search(prefix, 1, 5);
        ArticleCreateRequest create = new ArticleCreateRequest();
        create.title = prefix;
        create.content = "original body";
        var saved = articles.create(create);
        assertThat(saved.getId()).isPositive();
        assertThat(articles.getById(saved.getId()).getTitle()).isEqualTo(prefix);
        assertThat(articles.search(prefix, 1, 5).total()).isEqualTo(1);
        ArticleUpdateRequest update = new ArticleUpdateRequest();
        update.content = "updated body " + prefix;
        var updated = articles.update(saved.getId(), update);
        assertThat(updated.getTitle()).isEqualTo(prefix);
        assertThat(articles.getById(saved.getId()).getContent()).isEqualTo(update.content);
        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt());
        // 相同值重复 PUT 应成功。
        assertThat(articles.update(saved.getId(), update).getContent()).isEqualTo(update.content);
        articles.delete(saved.getId());
        assertThatThrownBy(() -> articles.getById(saved.getId())).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> articles.delete(saved.getId())).isInstanceOf(NoSuchElementException.class);
        assertThat(articles.search(prefix, 1, 5).total()).isZero();
        assertThat(messages.search(prefix, 1, 5)).usingRecursiveComparison().isEqualTo(before);
    }
}
