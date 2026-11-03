package com.sanjay.Spring_ECOM.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    /** Lightweight greeting for checking that the HTTP application is reachable. */
    @GetMapping("/hello")
    public String greed(){
        return "Welcome to the website";
    }
}
