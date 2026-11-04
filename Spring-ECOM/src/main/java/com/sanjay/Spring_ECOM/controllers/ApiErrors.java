package com.sanjay.Spring_ECOM.controllers;
import org.springframework.web.bind.annotation.*;import org.springframework.http.*;import java.util.Map;
@RestControllerAdvice public class ApiErrors {@ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<?> invalid(){return ResponseEntity.badRequest().body(Map.of("message","Invalid request"));}}
