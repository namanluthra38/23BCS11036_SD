package com.example.urlshortener.service;

import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final Random random = new Random();

    private static final String CACHE_QUEUE_KEY = "url_cache_queue";
    private static final int MAX_CACHE_SIZE = 10;

    private void addToCache(String shortCode, String originalUrl) {
        redisTemplate.opsForValue().set(shortCode, originalUrl, 24, TimeUnit.HOURS);
        redisTemplate.opsForList().remove(CACHE_QUEUE_KEY, 0, shortCode);
        redisTemplate.opsForList().rightPush(CACHE_QUEUE_KEY, shortCode);
        
        Long size = redisTemplate.opsForList().size(CACHE_QUEUE_KEY);
        if (size != null && size > MAX_CACHE_SIZE) {
            String oldest = redisTemplate.opsForList().leftPop(CACHE_QUEUE_KEY);
            if (oldest != null) {
                redisTemplate.delete(oldest);
            }
        }
    }

    public String shortenUrl(String originalUrl) {
        Optional<UrlMapping> existing = urlRepository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {
            return existing.get().getShortCode();
        }

        String shortCode = generateShortCode();
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortCode(shortCode);
        mapping.setCreatedAt(LocalDateTime.now());
        urlRepository.save(mapping);

        // Cache the new mapping
        addToCache(shortCode, originalUrl);

        return shortCode;
    }

    public Optional<UrlMapping> getOriginalUrl(String shortCode) {
        // Check cache first
        String cachedUrl = redisTemplate.opsForValue().get(shortCode);
        if (cachedUrl != null) {
            UrlMapping mapping = new UrlMapping();
            mapping.setShortCode(shortCode);
            mapping.setOriginalUrl(cachedUrl);
            return Optional.of(mapping);
        }

        // Fallback to database
        Optional<UrlMapping> mapping = urlRepository.findByShortCode(shortCode);
        if (mapping.isPresent()) {
            // Cache for future requests
            addToCache(shortCode, mapping.get().getOriginalUrl());
        }
        return mapping;
    }

    private String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        String code = sb.toString();
        // Check for collision
        if (urlRepository.findByShortCode(code).isPresent()) {
            return generateShortCode();
        }
        return code;
    }
}
