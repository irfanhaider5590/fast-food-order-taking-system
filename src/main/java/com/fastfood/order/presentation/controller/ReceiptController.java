package com.fastfood.order.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    @Value("${app.receipt.auto-print-enabled:false}")
    private boolean autoPrintEnabled;

    @GetMapping("/auto-print-status")
    public ResponseEntity<Map<String, Object>> getAutoPrintStatus() {
        log.info("GET /api/receipt/auto-print-status - Getting auto-print status");
        Map<String, Object> response = new HashMap<>();
        response.put("autoPrintEnabled", autoPrintEnabled);
        return ResponseEntity.ok(response);
    }
}
