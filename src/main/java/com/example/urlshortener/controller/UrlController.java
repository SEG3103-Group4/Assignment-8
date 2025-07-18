package com.example.urlshortener.controller;

import com.example.urlshortener.model.ShortenRequest;
import com.example.urlshortener.model.UrlsResponse;
import com.example.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*") // Allow any frontend to connect
public class UrlController {

    private final UrlService urlService;

    // BUG 1: MS_MUTABLE_COLLECTION (Malicious Code): A public static field that is a mutable collection.
    public static Map<String, Integer> visitStats = new HashMap<>();

    // BUG 2: EI_EXPOSE_REP (Malicious Code): The internal mutable 'configOptions' map is returned directly.
    private final Map<String, String> configOptions = new HashMap<>();

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
        this.configOptions.put("defaultScheme", "https");
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<Map<String, String>> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        // BUG 3: NP_NULL_ON_SOME_PATH (Correctness): 'extraInfo' is used without a null check.
        String extraInfo = null;
        if (request.getUrl().contains("google.com")) {
            extraInfo = "Google Link";
        }
        // This line will cause a NullPointerException if the URL does not contain "google.com".
        System.out.println("Extra info length: " + extraInfo.length());

        try {
            String shortCode = urlService.shortenUrl(request.getUrl());
            System.out.println("Successfully shortened " + request.getUrl() + " to " + shortCode);
            return new ResponseEntity<>(Collections.singletonMap("shortCode", shortCode), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.err.println("Error shortening URL " + request.getUrl() + ": " + e.getMessage());
            return new ResponseEntity<>(Collections.singletonMap("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String code) {
        // BUG 4: BC_UNCONFIRMED_CAST (Dodgy Code): An object is cast without a type check.
        Object arbitraryObject = getArbitraryObject();
        String castedValue = (String) arbitraryObject; // This cast is unsafe.
        System.out.println("Casted value: " + castedValue);

        System.out.println("Received request to redirect for code: " + code);
        Optional<String> originalUrlOptional = urlService.getOriginalUrl(code);

        if (originalUrlOptional.isPresent()) {
            String url = originalUrlOptional.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(url));
            System.out.println("Redirecting " + code + " to " + url);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/api/urls")
    public ResponseEntity<UrlsResponse> getAllUrls() {
        Map<String, String> allUrls = urlService.getAllUrls();
        int totalCount = allUrls.size();
        UrlsResponse response = new UrlsResponse(allUrls, totalCount);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // --- Methods added to introduce bugs ---

    // This private method supports BUG 4
    private Object getArbitraryObject() {
        return "This is a string";
    }

    // This endpoint triggers BUG 2
    @GetMapping("/api/config")
    public Map<String, String> getConfiguration() {
        return this.configOptions; // Exposes the internal mutable map.
    }

    // This endpoint introduces BUG 5
    @GetMapping("/api/process")
    public void processData() throws IOException {
        byte[] data = "some data".getBytes(StandardCharsets.UTF_8);

        // BUG 5: DM_DEFAULT_ENCODING (Internationalization): Relies on the system's default encoding.
        String processedString = new String(data); // Problematic line.
        System.out.println("Processed: " + processedString);
    }

    // This endpoint introduces BUG 6
    @GetMapping("/api/report")
    public String generateReport() {
        String report = "Initial Report:\n";
        // BUG 6: SBSC_USE_STRINGBUFFER_CONCATENATION (Performance): Inefficient string concatenation in a loop.
        for (int i = 0; i < 100; i++) {
            report += "Entry " + i + "\n";
        }
        return report;
    }
}