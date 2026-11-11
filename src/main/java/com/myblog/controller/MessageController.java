package com.myblog.controller;

import com.myblog.dto.PageResult;
import com.myblog.model.Message;
import com.myblog.model.MessageRequest;
import com.myblog.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Message create(@Valid @RequestBody MessageRequest request) {
        return messageService.create(request);
    }

    @GetMapping
    public PageResult<Message> list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") long page,
            @RequestParam(name = "size", defaultValue = "5") long size) {
        return messageService.search(keyword, page, size);
    }

    @GetMapping("/{id}")
    public Message getOne(@PathVariable("id") Long id) {
        return messageService.getById(id);
    }
}
