package com.sanjay.Spring_ECOM.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String greed(){
        return "Welcome to the website";
    }
}
