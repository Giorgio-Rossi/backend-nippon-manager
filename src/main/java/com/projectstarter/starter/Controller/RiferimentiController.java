package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Response.RiferimentiResponse;
import com.projectstarter.starter.Service.RiferimentiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Voci di dominio per select e legende: il client non le tiene piu in costanti proprie. */
@RestController
@RequestMapping("/api/reference")
@RequiredArgsConstructor
public class RiferimentiController {

    private final RiferimentiService riferimentiService;

    @GetMapping
    public ResponseEntity<RiferimentiResponse> get() {
        return ResponseEntity.ok(riferimentiService.riferimenti());
    }
}
