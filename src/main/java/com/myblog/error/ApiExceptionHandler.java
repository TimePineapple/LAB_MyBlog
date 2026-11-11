package com.myblog.error;

import com.myblog.dto.Result;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Result<Void>> notFound(NoSuchElementException exception) {
        return ResponseEntity.status(404).body(Result.fail(404, exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Result.fail(400, exception.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(value -> value != null)
                .findFirst().orElse("请求参数不正确");
        return new ResponseEntity<>(Result.fail(400, message), headers, status);
    }

    // 包括 JSON 解析、参数类型、405、415 和不存在的静态资源等 MVC 错误。
    // 保留 Spring 的真实状态码及 Allow 等响应头，同时替换响应体骨架。
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = switch (status.value()) {
            case 400 -> "请求参数或 JSON 请求体不正确";
            case 404 -> "资源不存在";
            case 405 -> "请求方法不支持";
            case 415 -> "请求体需要 Content-Type: application/json";
            default -> "请求失败";
        };
        return super.handleExceptionInternal(exception,
                Result.fail(status.value(), message), headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> unexpected(Exception exception) {
        LOG.error("Unhandled API exception", exception);
        return ResponseEntity.internalServerError().body(Result.fail(500, "服务暂时无法处理请求"));
    }
}
