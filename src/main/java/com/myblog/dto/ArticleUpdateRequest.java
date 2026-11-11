package com.myblog.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Size;

// 省略字段表示保留原值；显式 null 属于无效请求。
public class ArticleUpdateRequest {
    @JsonSetter(nulls = Nulls.FAIL)
    @Size(max = 200, message = "标题不能超过 200 个字符")
    public String title;

    @JsonSetter(nulls = Nulls.FAIL)
    public String content;
}
