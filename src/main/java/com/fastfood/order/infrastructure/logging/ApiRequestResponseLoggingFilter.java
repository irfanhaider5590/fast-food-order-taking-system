package com.fastfood.order.infrastructure.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Brief JSON request/response logging for /api/**.
 * Sensitive fields (password, hash, tokens, secrets) are redacted.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@RequiredArgsConstructor
public class ApiRequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_BODY_CHARS = 4000;
    private static final String REDACTED = "***REDACTED***";
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "passwordhash",
            "password_hash",
            "newpassword",
            "oldpassword",
            "confirmpassword",
            "currentpassword",
            "accesstoken",
            "refreshtoken",
            "token",
            "authorization",
            "secret",
            "secretkey",
            "adminsecretkey",
            "hash",
            "apikey",
            "api_key"
    );

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/")) {
            return true;
        }
        // Skip noisy/binary endpoints
        return path.startsWith("/api/files/serve")
                || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, MAX_BODY_CHARS * 2);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long started = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long tookMs = System.currentTimeMillis() - started;
            try {
                logExchange(wrappedRequest, wrappedResponse, tookMs);
            } catch (Exception e) {
                log.debug("API log failed: {}", e.getMessage());
            }
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logExchange(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            long tookMs) {

        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String fullPath = query == null || query.isBlank() ? path : path + "?" + sanitizeQuery(query);

        String requestBody = readBody(request.getContentAsByteArray(), request.getCharacterEncoding(), request.getContentType());
        String responseBody = readBody(response.getContentAsByteArray(), response.getCharacterEncoding(), response.getContentType());

        log.info("API {} {} -> {} ({} ms)\n  request: {}\n  response: {}",
                method,
                fullPath,
                response.getStatus(),
                tookMs,
                requestBody,
                responseBody);
    }

    private String sanitizeQuery(String query) {
        StringBuilder out = new StringBuilder();
        String[] parts = query.split("&");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                out.append('&');
            }
            String part = parts[i];
            int eq = part.indexOf('=');
            if (eq <= 0) {
                out.append(part);
                continue;
            }
            String key = part.substring(0, eq);
            if (isSensitiveKey(key)) {
                out.append(key).append('=').append(REDACTED);
            } else {
                out.append(part);
            }
        }
        return out.toString();
    }

    private String readBody(byte[] bytes, String encoding, String contentType) {
        if (bytes == null || bytes.length == 0) {
            return "{}";
        }
        if (contentType != null && !isTextOrJson(contentType)) {
            return "\"[non-json body omitted]\"";
        }
        Charset charset = StandardCharsets.UTF_8;
        if (encoding != null && !encoding.isBlank()) {
            try {
                charset = Charset.forName(encoding);
            } catch (Exception ignored) {
                // keep UTF-8
            }
        }
        String raw = new String(bytes, charset).trim();
        if (raw.isEmpty()) {
            return "{}";
        }
        if (raw.length() > MAX_BODY_CHARS) {
            raw = raw.substring(0, MAX_BODY_CHARS) + "...[truncated]";
        }
        return redactJson(raw);
    }

    private boolean isTextOrJson(String contentType) {
        String ct = contentType.toLowerCase(Locale.ROOT);
        return ct.contains(MediaType.APPLICATION_JSON_VALUE)
                || ct.contains("application/*+json")
                || ct.contains(MediaType.TEXT_PLAIN_VALUE)
                || ct.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    private String redactJson(String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            redactNode(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            // Form bodies / non-JSON: scrub obvious password=... patterns
            return scrubPlainText(raw);
        }
    }

    private void redactNode(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = obj.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (isSensitiveKey(entry.getKey())) {
                    obj.put(entry.getKey(), REDACTED);
                } else {
                    redactNode(entry.getValue());
                }
            }
        } else if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            for (JsonNode child : arr) {
                redactNode(child);
            }
        }
    }

    private String scrubPlainText(String raw) {
        String scrubbed = raw;
        for (String key : SENSITIVE_KEYS) {
            // password=secret or "password":"secret"
            scrubbed = scrubbed.replaceAll(
                    "(?i)(" + key + "\\s*[:=]\\s*)([^&\\s,;\"']+)",
                    "$1" + REDACTED);
            scrubbed = scrubbed.replaceAll(
                    "(?i)(\"" + key + "\"\\s*:\\s*\")([^\"]*)(\")",
                    "$1" + REDACTED + "$3");
        }
        return scrubbed;
    }

    private boolean isSensitiveKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        if (SENSITIVE_KEYS.contains(normalized) || SENSITIVE_KEYS.contains(key.trim().toLowerCase(Locale.ROOT))) {
            return true;
        }
        // Catch variants like passwordHash, userPassword, tokenValue
        return normalized.contains("password")
                || normalized.endsWith("hash")
                || normalized.contains("secret")
                || normalized.endsWith("token")
                || normalized.equals("authorization");
    }
}
