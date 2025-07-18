package com.example.urlshortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UrlServiceTest {

    private UrlService urlService;

    @BeforeEach
    void setUp() {
        urlService = new UrlService();
    }

    @Test
    void shortenUrl_withExistingUrl_shouldReturnSameShortCode() {
        // Given
        String longUrl = "https://google.com";
        String firstShortCode = urlService.shortenUrl(longUrl);

        // When
        String secondShortCode = urlService.shortenUrl(longUrl);

        // Then
        assertNotNull(firstShortCode);
        assertEquals(firstShortCode, secondShortCode);
    }

    @Test
    void shortenUrl_withInvalidUrl_shouldThrowException() {
        // Given
        String invalidUrl = "not-a-valid-url";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            urlService.shortenUrl(invalidUrl);
        });
    }

    @Test
    void getOriginalUrl_withExistingCode_shouldReturnUrl() {
        // Given
        String longUrl = "https://example.com/a-very-long-url";
        String shortCode = urlService.shortenUrl(longUrl);

        // When
        String resolvedUrl = urlService.getOriginalUrl(shortCode).orElse(null);

        // Then
        assertEquals(longUrl, resolvedUrl);
    }

    @Test
    void getOriginalUrl_withNonExistentCode_shouldReturnEmpty() {
        // When
        var result = urlService.getOriginalUrl("nonexistent");

        // Then
        assertTrue(result.isEmpty());
    }

    // NOTE: The path for generating a NEW unique short code in `shortenUrl`
    // is intentionally NOT tested to demonstrate code coverage tools.
}