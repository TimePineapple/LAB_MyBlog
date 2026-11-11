package com.myblog.service;

import com.myblog.dto.ArticleCreateRequest;
import com.myblog.dto.ArticleUpdateRequest;
import com.myblog.mapper.ArticleMapper;
import com.myblog.model.Article;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @Mock private ArticleMapper mapper;
    @InjectMocks private ArticleService service;

    @Test
    void creationAssignsServerMetadataAndReceivesGeneratedId() {
        when(mapper.insert(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            assertThat(saved.getId()).isNull();
            saved.setId(42L);
            return 1;
        });
        Article result = service.create(createRequest("title", "content"));
        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getCreatedAt().getNano()).isZero();
    }

    @Test
    void partialUpdatePreservesUnspecifiedFieldsAndCreationTime() {
        Article existing = new Article();
        existing.setId(7L);
        existing.setTitle("old title");
        existing.setContent("keep this body");
        existing.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 0, 0));
        when(mapper.selectById(7L)).thenReturn(existing);
        ArticleUpdateRequest update = new ArticleUpdateRequest();
        update.title = "new title";
        Article result = service.update(7L, update);
        ArgumentCaptor<Article> saved = ArgumentCaptor.forClass(Article.class);
        verify(mapper).updateById(saved.capture());
        assertThat(saved.getValue().getTitle()).isEqualTo("new title");
        assertThat(result.getContent()).isEqualTo("keep this body");
        assertThat(result.getCreatedAt()).isEqualTo(existing.getCreatedAt());
    }

    @Test
    void invalidInputAndMissingResourcesAreRejected() {
        assertThatThrownBy(() -> service.create(createRequest(" ", "body")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(createRequest("x".repeat(201), "body")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(createRequest("title", "中".repeat(22000))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.update(1L, new ArticleUpdateRequest()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getById(999L)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> service.delete(999L)).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> service.search(null, 1, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    private ArticleCreateRequest createRequest(String title, String content) {
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.title = title;
        request.content = content;
        return request;
    }
}
