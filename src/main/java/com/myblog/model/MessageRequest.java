package com.myblog.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MessageRequest {
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言内容不能超过 1000 个字符")
    public String content;
}
