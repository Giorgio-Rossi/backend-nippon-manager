package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Response.StatisticheIncassiResponse;
import com.projectstarter.starter.Dto.Response.StatistichePresenzeResponse;
import com.projectstarter.starter.Service.StatisticheIncassiService;
import com.projectstarter.starter.Service.StatistichePresenzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticheController {

    private final StatistichePresenzeService presenzeService;
    private final StatisticheIncassiService incassiService;

    /**
     * Statistiche presenze. Senza {@code from}/{@code to} copre l'intera stagione;
     * senza {@code corsoId} aggrega tutti i corsi.
     */
    @GetMapping("/attendance")
    public ResponseEntity<StatistichePresenzeResponse> presenze(
            @RequestParam(required = false) Long corsoId,
            @RequestParam(required = false) String stagione,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(presenzeService.calcola(corsoId, stagione, from, to));
    }

    /**
     * Statistiche incassi della stagione. Con {@code corsoId} restano le sole rate
     * del corso: la tessera associativa non e attribuibile a un corso singolo.
     */
    @GetMapping("/revenue")
    public ResponseEntity<StatisticheIncassiResponse> incassi(
            @RequestParam(required = false) Long corsoId,
            @RequestParam(required = false) String stagione) {
        return ResponseEntity.ok(incassiService.calcola(corsoId, stagione));
    }
}
