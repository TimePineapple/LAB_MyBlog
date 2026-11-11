package com.myblog.controller;

import com.myblog.dto.ArticleCreateRequest;
import com.myblog.dto.ArticleUpdateRequest;
import com.myblog.dto.PageResult;
import com.myblog.dto.Result;
import com.myblog.model.Article;
import com.myblog.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
public class ArticleController {
    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public Result<Article> create(@Valid @RequestBody ArticleCreateRequest request) {
        return Result.ok(articleService.create(request));
    }

    @GetMapping
    public Result<PageResult<Article>> list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") long page,
            @RequestParam(name = "size", defaultValue = "5") long size) {
        return Result.ok(articleService.search(keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<Article> getOne(@PathVariable("id") Long id) {
        return Result.ok(articleService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Article> update(@PathVariable("id") Long id,
            @Valid @RequestBody ArticleUpdateRequest request) {
        return Result.ok(articleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        articleService.delete(id);
        return Result.ok(null);
    }
}
