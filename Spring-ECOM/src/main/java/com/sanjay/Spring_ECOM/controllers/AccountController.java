package com.sanjay.Spring_ECOM.controllers;
import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;import java.util.Map;
@RestController public class AccountController {@GetMapping("/api/me") public Map<String,Object> me(Authentication a){return Map.of("username",a.getName(),"admin",a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")));}}
