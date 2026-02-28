package com.qttracker.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> bad(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(409)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String,String>> overSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "이미지 크기는 50MB 이하여야 합니다."));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String,String>> multipart(MultipartException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "파일 업로드에 실패했습니다. 다시 시도해주세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> server(Exception e) {
        return ResponseEntity.internalServerError()
                .body(Map.of("message", "서버 오류가 발생했습니다."));
    }
}
