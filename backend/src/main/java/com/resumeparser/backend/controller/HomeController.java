package com.resumeparser.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @GetMapping("/")
    public String index() {
        // Automatically redirect to the React frontend dashboard URL
        return "redirect:" + frontendUrl;
    }
}
