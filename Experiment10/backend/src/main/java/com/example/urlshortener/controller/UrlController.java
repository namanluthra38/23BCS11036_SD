package com.example.urlshortener.controller;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody Map<String, String> request) {
        String originalUrl = request.get("url");
        if (originalUrl == null || originalUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("URL is required");
        }
        String shortCode = urlService.shortenUrl(originalUrl);
        return ResponseEntity.ok(Map.of("shortCode", shortCode));
    }

    @GetMapping("/{shortCode}")
    public RedirectView redirectToUrl(@PathVariable String shortCode) {
        Optional<UrlMapping> mapping = urlService.getOriginalUrl(shortCode);
        if (mapping.isPresent()) {
            return new RedirectView(mapping.get().getOriginalUrl());
        }
        return new RedirectView("/");
    }
}
