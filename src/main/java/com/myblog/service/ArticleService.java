package com.myblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myblog.dto.ArticleCreateRequest;
import com.myblog.dto.ArticleUpdateRequest;
import com.myblog.dto.PageResult;
import com.myblog.mapper.ArticleMapper;
import com.myblog.model.Article;
import com.myblog.support.QuerySupport;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;

    public ArticleService(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    public Article create(ArticleCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        validateTitle(request.title);
        validateContent(request.content);
        Article article = new Article();
        article.setTitle(request.title);
        article.setContent(request.content);
        article.setCreatedAt(LocalDateTime.now().withNano(0));
        articleMapper.insert(article);
        return article;
    }

    public Article getById(Long id) {
        return Optional.ofNullable(articleMapper.selectById(id))
                .orElseThrow(() -> new NoSuchElementException("article not found"));
    }

    public PageResult<Article> search(String keyword, long page, long size) {
        QuerySupport.validatePage(page, size);
        LambdaQueryWrapper<Article> query = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String literal = QuerySupport.literalKeyword(keyword);
            query.and(group -> group.like(Article::getTitle, literal)
                    .or().like(Article::getContent, literal));
        }
        query.orderByDesc(Article::getCreatedAt).orderByDesc(Article::getId);
        Page<Article> result = articleMapper.selectPage(new Page<>(page, size), query);
        return PageResult.of(result);
    }

    @Transactional
    public Article update(Long id, ArticleUpdateRequest request) {
        if (request == null || (request.title == null && request.content == null)) {
            throw new IllegalArgumentException("至少提供 title 或 content 中的一个字段");
        }
        if (request.title != null) {
            validateTitle(request.title);
        }
        if (request.content != null) {
            validateContent(request.content);
        }
        Article existing = getById(id);
        if (request.title != null) {
            existing.setTitle(request.title);
        }
        if (request.content != null) {
            existing.setContent(request.content);
        }
        articleMapper.updateById(existing);
        // 回读真实保存结果；重复提交相同内容也可以成功。
        return getById(id);
    }

    public void delete(Long id) {
        if (articleMapper.deleteById(id) == 0) {
            throw new NoSuchElementException("article not found");
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank() || title.length() > 200) {
            throw new IllegalArgumentException("标题不能为空，且不能超过 200 个字符");
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("正文不能为空");
        }
        if (content.getBytes(StandardCharsets.UTF_8).length > 65535) {
            throw new IllegalArgumentException("正文不能超过 MySQL TEXT 的 65535 字节容量");
        }
    }
}
