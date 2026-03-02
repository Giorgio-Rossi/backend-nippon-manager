package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Request.AtletaRequest;
import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Service.AtletaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/athletes")
@RequiredArgsConstructor
public class AtletaController {

    private final AtletaService atletaService;

    @GetMapping
    public ResponseEntity<List<AtletaResponse>> getAll(
            @RequestParam(required = false) Boolean attivo) {
        return ResponseEntity.ok(atletaService.findAll(attivo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtletaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(atletaService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AtletaResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(atletaService.search(q));
    }

    @GetMapping("/expiring-certificates")
    public ResponseEntity<List<AtletaResponse>> getExpiringCertificates(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(atletaService.findExpiringCertificates(days));
    }

    @PostMapping
    public ResponseEntity<AtletaResponse> create(@Valid @RequestBody AtletaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atletaService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtletaResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AtletaRequest request) {
        return ResponseEntity.ok(atletaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        atletaService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        atletaService.activate(id);
        return ResponseEntity.noContent().build();
    }
}
