package com.muhimbili.labnotification.rest;

import com.muhimbili.labnotification.data.response.LabResultsPreviewResponse;
import com.muhimbili.labnotification.service.LabOrdersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ApplicationController {

    private final LabOrdersService labOrdersService;

    public ApplicationController(LabOrdersService labOrdersService) {
        this.labOrdersService = labOrdersService;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/lab-orders/fetch")
    public ResponseEntity<LabResultsPreviewResponse> fetchLabOrders(
            @RequestParam String date,
            @RequestParam String from,
            @RequestParam String to) {
        LabResultsPreviewResponse previewResponse = labOrdersService.fetchOrders(date, from, to);
        return ResponseEntity.ok(previewResponse);
    }
}
