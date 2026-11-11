package com.myblog.service;

import com.myblog.model.Message;
import com.myblog.model.MessageRequest;
import com.myblog.repository.InMemoryMessageRepository;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageServiceTest {
    private final MessageService service = new MessageService(new InMemoryMessageRepository());

    @Test
    void createsStableIdsAndPreservesContent() {
        Message first = create("hello one");
        Message second = create("hello two");
        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
        assertThat(first.getCreatedAt().getNano()).isZero();
        assertThat(service.getById(first.getId()).getContent()).isEqualTo("hello one");
        // 修改返回值不会破坏教学仓库中已保存的数据。
        first.setContent("changed outside repository");
        assertThat(service.getById(first.getId()).getContent()).isEqualTo("hello one");
    }

    @Test
    void combinesFilteringAndPaginationWithStableTieOrder() {
        create("hello one");
        create("unrelated");
        create("hello two");
        create("hello three");
        var first = service.search("hello", 1, 2);
        var second = service.search("hello", 2, 2);
        assertThat(first.total()).isEqualTo(3);
        assertThat(first.items()).hasSize(2);
        assertThat(second.items()).extracting(Message::getContent).containsExactly("hello one");
        assertThat(second.total()).isEqualTo(3);
        assertThat(service.search("absent", 1, 5).total()).isZero();
        assertThat(service.search("hello", 99, 5).items()).isEmpty();
        assertThat(service.search("hello", 99, 5).total()).isEqualTo(3);
    }

    @Test
    void validatesBeforeSavingAndReportsMissingIds() {
        assertThatThrownBy(() -> create("   ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> create("x".repeat(1001))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(service.search(null, 1, 5).total()).isZero();
        assertThatThrownBy(() -> service.getById(0L)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> service.search(null, 0, 5)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.search(null, 1, 101)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.search(null, Long.MAX_VALUE, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstructingMemoryRepositoryLosesData() {
        create("temporary");
        MessageService restarted = new MessageService(new InMemoryMessageRepository());
        assertThat(restarted.search(null, 1, 5).items()).isEmpty();
    }

    private Message create(String content) {
        MessageRequest request = new MessageRequest();
        request.content = content;
        return service.create(request);
    }
}
