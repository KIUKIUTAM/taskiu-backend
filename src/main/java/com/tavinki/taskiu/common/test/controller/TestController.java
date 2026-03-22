package com.tavinki.taskiu.common.test.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/users")
    public String getUsers() {
        return "Hello, this is a test endpoint for users!";
    }
}
