package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Response.DashboardResponse;
import com.projectstarter.starter.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** Conteggi e certificati in scadenza in una sola chiamata. */
    @GetMapping
    public ResponseEntity<DashboardResponse> get(@RequestParam(required = false) Integer giorni) {
        return ResponseEntity.ok(dashboardService.riepilogo(giorni));
    }
}
