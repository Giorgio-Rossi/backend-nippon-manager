package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Request.GeneraLezioniRequest;
import com.projectstarter.starter.Dto.Request.LezioneRequest;
import com.projectstarter.starter.Dto.Request.PresenzeBulkRequest;
import com.projectstarter.starter.Dto.Response.FoglioPresenzeResponse;
import com.projectstarter.starter.Dto.Response.LezioneResponse;
import com.projectstarter.starter.Dto.Response.PresenzaResponse;
import com.projectstarter.starter.Service.PresenzeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PresenzeController {

    private final PresenzeService presenzeService;

    // ---------- Lezioni ----------

    @GetMapping("/courses/{corsoId}/lessons")
    public ResponseEntity<List<LezioneResponse>> getLezioni(
            @PathVariable Long corsoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(presenzeService.findLezioni(corsoId, from, to));
    }

    @PostMapping("/courses/{corsoId}/lessons/generate")
    public ResponseEntity<List<LezioneResponse>> generaLezioni(
            @PathVariable Long corsoId,
            @Valid @RequestBody GeneraLezioniRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presenzeService.generaLezioni(corsoId, request));
    }

    @PostMapping("/courses/{corsoId}/lessons")
    public ResponseEntity<LezioneResponse> creaLezione(
            @PathVariable Long corsoId,
            @Valid @RequestBody LezioneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presenzeService.creaLezione(corsoId, request));
    }

    @PutMapping("/lessons/{lezioneId}")
    public ResponseEntity<LezioneResponse> updateLezione(
            @PathVariable Long lezioneId,
            @Valid @RequestBody LezioneRequest request) {
        return ResponseEntity.ok(presenzeService.updateLezione(lezioneId, request));
    }

    @DeleteMapping("/lessons/{lezioneId}")
    public ResponseEntity<Void> deleteLezione(@PathVariable Long lezioneId) {
        presenzeService.deleteLezione(lezioneId);
        return ResponseEntity.noContent().build();
    }

    // ---------- Foglio presenze ----------

    @GetMapping("/lessons/{lezioneId}/attendance")
    public ResponseEntity<List<PresenzaResponse>> getPresenzeLezione(@PathVariable Long lezioneId) {
        return ResponseEntity.ok(presenzeService.findPresenzeLezione(lezioneId));
    }

    @GetMapping("/courses/{corsoId}/attendance")
    public ResponseEntity<FoglioPresenzeResponse> getFoglio(
            @PathVariable Long corsoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(presenzeService.foglio(corsoId, from, to));
    }

    @PutMapping("/courses/{corsoId}/attendance")
    public ResponseEntity<List<PresenzaResponse>> salvaPresenze(
            @PathVariable Long corsoId,
            @Valid @RequestBody PresenzeBulkRequest request) {
        return ResponseEntity.ok(presenzeService.salvaPresenze(corsoId, request));
    }
}
