package com.example.urlshortener.service;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class UrlService {

    private final Map<String, String> shortToLongUrl = new ConcurrentHashMap<String, String>();
    private final Map<String, String> longToShortUrl = new ConcurrentHashMap<String, String>();
    private final UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});

    @Value("${rate-limiting.enabled:false}")
    private boolean rateLimitingEnabled;

    @Value("${rate-limiting.permits:10}")
    private int maxPermits;

    private Semaphore semaphore;

    @PostConstruct
    public void init() {
        if (rateLimitingEnabled) {
            this.semaphore = new Semaphore(maxPermits, true);
            System.out.println("✅ Rate limiting ENABLED with " + maxPermits + " permits.");
        } else {
            System.out.println("❌ Rate limiting DISABLED.");
        }
    }

    public String shortenUrl(String longUrl) {
        if (!urlValidator.isValid(longUrl)) {
            throw new IllegalArgumentException("Invalid URL provided: " + longUrl);
        }

        applyRateLimiting();

        if (longToShortUrl.containsKey(longUrl)) {
            return longToShortUrl.get(longUrl);
        }

        String shortCode = generateUniqueShortCode();
        shortToLongUrl.put(shortCode, longUrl);
        longToShortUrl.put(longUrl, shortCode);
        return shortCode;
    }

    private void applyRateLimiting() {
        if (!rateLimitingEnabled) {
            return;
        }

        boolean permitAcquired = false;
        try {
            System.out.println("Applying rate limit delay...");
            permitAcquired = semaphore.tryAcquire(500, TimeUnit.MILLISECONDS);
            if (!permitAcquired) {
                System.err.println("COULD NOT ACQUIRE SEMAPHORE PERMIT - SERVER TOO BUSY");
                throw new RuntimeException("Server is too busy to handle the request.");
            }

            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request was interrupted.", e);
        } finally {
            if (permitAcquired) {
                semaphore.release();
            }
        }
    }

    public Optional<String> getOriginalUrl(String shortCode) {
        return Optional.<String>ofNullable(shortToLongUrl.get(shortCode));
    }

    public Map<String, String> getAllUrls() {
        return shortToLongUrl;
    }

    private String generateUniqueShortCode() {
        String shortCode;
        do {
            shortCode = RandomStringUtils.randomAlphanumeric(7);
        } while (shortToLongUrl.containsKey(shortCode));
        return shortCode;
    }
}
