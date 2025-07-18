package com.example.urlshortener.model;

import jakarta.validation.constraints.NotEmpty;

/**
 * A DTO representing the incoming request to shorten a URL.
 */
public class ShortenRequest {

    @NotEmpty
    private String url;

    // No-argument constructor
    public ShortenRequest() {
        this.url = "";
    }

    // All-argument constructor
    public ShortenRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
