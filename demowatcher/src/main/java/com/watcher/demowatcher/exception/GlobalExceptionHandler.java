package com.watcher.demowatcher.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleFileProcessingException(FileProcessingException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "File Processing Error");
        errorResponse.put("message", ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
	
	@ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
        errorResponse.put("error", "File Too Large");
        errorResponse.put("message", "File size exceeds the maximum limit of 50MB");
        
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }
	
	
	 @ExceptionHandler(Exception.class)
	    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
	        Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("timestamp", LocalDateTime.now());
	        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
	        errorResponse.put("error", "Internal Server Error");
	        errorResponse.put("message", ex.getMessage());
	        
	        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
}
