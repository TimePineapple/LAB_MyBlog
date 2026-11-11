package com.myblog.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblog.dto.ArticleCreateRequest;
import com.myblog.dto.ArticleUpdateRequest;
import com.myblog.dto.PageResult;
import com.myblog.error.ApiExceptionHandler;
import com.myblog.model.Article;
import com.myblog.model.Message;
import com.myblog.model.MessageRequest;
import com.myblog.service.ArticleService;
import com.myblog.service.MessageService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ArticleController.class, MessageController.class,
        ProfileController.class, HelloController.class})
@Import(ApiExceptionHandler.class)
class ApiContractTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @MockitoBean private MessageService messages;
    @MockitoBean private ArticleService articles;

    @Test
    void identityKeepsItsOriginalUnwrappedShape() throws Exception {
        mvc.perform(get("/hello")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        var response = mvc.perform(get("/profile")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").doesNotExist())
                .andReturn().getResponse();
        JsonNode profile = json.readTree(response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        List<String> keys = new ArrayList<>();
        profile.fieldNames().forEachRemaining(keys::add);
        assertThat(keys).containsExactlyInAnyOrder("blogName", "owner", "introduction", "interests", "vision");
        mvc.perform(get("/me")).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(profile.get("blogName").asText()))
                .andExpect(jsonPath("$.owner").value(profile.get("owner").asText()))
                .andExpect(jsonPath("$.introduction").value(profile.get("introduction").asText()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void messagesKeepUnwrappedResultsAndUseDatabaseIds() throws Exception {
        Message saved = new Message();
        saved.setId(23L);
        saved.setContent("hello");
        saved.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(messages.create(any(MessageRequest.class))).thenReturn(saved);
        when(messages.getById(23L)).thenReturn(saved);
        when(messages.search(null, 1, 5)).thenReturn(new PageResult<>(List.of(saved), 1, 1, 5));
        mvc.perform(post("/messages").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"hello\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(23))
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.code").doesNotExist());
        mvc.perform(get("/messages/23")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(23));
        mvc.perform(get("/messages")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(23))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void articleActionsHaveTheSameThreeFieldContract() throws Exception {
        Article article = new Article();
        article.setId(8L);
        article.setTitle("title");
        article.setContent("body");
        when(articles.create(any(ArticleCreateRequest.class))).thenReturn(article);
        when(articles.getById(8L)).thenReturn(article);
        when(articles.update(eq(8L), any(ArticleUpdateRequest.class))).thenReturn(article);
        when(articles.search(null, 1, 5)).thenReturn(new PageResult<>(List.of(article), 1, 1, 5));
        mvc.perform(post("/articles").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"title\",\"content\":\"body\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.id").value(8));
        mvc.perform(get("/articles/8")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("title"));
        mvc.perform(get("/articles")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(8))
                .andExpect(jsonPath("$.data.total").value(1));
        mvc.perform(put("/articles/8").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"updated\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        mvc.perform(delete("/articles/8")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andExpect(jsonPath("$.data").value(nullValue()));
        verify(articles).delete(8L);
    }

    @Test
    void missingResourcesUseActual404AndResultSkeleton() throws Exception {
        when(articles.getById(999L)).thenThrow(new NoSuchElementException("article not found"));
        when(messages.getById(999L)).thenThrow(new NoSuchElementException("message not found"));
        mvc.perform(get("/articles/999")).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("article not found"))
                .andExpect(jsonPath("$.data").hasJsonPath());
        mvc.perform(get("/messages/999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void malformedInputAndMvcErrorsUseRealStatusCodes() throws Exception {
        mvc.perform(post("/messages").contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"wrong field\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        mvc.perform(post("/articles").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\" \",\"content\":\"body\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        mvc.perform(put("/articles/8").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":null}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        mvc.perform(post("/messages").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        mvc.perform(get("/messages/abc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
        mvc.perform(post("/messages").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.code").value(415));
        mvc.perform(put("/messages").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed()).andExpect(jsonPath("$.code").value(405));
        mvc.perform(get("/no-such-route"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void pagingValidationErrorsAre400() throws Exception {
        when(messages.search(null, 0, 5)).thenThrow(new IllegalArgumentException("invalid page"));
        mvc.perform(get("/messages?page=0"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
    }
}
