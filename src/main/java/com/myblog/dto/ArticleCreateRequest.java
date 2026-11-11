package com.myblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 输入不包含 id / createdAt，系统字段不能通过请求覆盖。
public class ArticleCreateRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过 200 个字符")
    public String title;

    @NotBlank(message = "正文不能为空")
    public String content;
}
