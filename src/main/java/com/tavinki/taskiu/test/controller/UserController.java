package com.tavinki.taskiu.test.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @RequestMapping("/test")
    public String getUsers() {
        return "List of users";
    }
}
