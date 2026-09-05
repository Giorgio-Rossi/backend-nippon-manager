package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Request.FiltroPresenzeRequest;
import com.projectstarter.starter.Dto.Response.StatisticheIncassiResponse;
import com.projectstarter.starter.Dto.Response.StatistichePresenzeResponse;
import com.projectstarter.starter.Service.StatisticheIncassiService;
import com.projectstarter.starter.Service.StatistichePresenzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticheController {

    private final StatistichePresenzeService presenzeService;
    private final StatisticheIncassiService incassiService;

    /**
     * Statistiche presenze. Il periodo si indica con {@code periodo} (un preset
     * come {@code ULTIMI_30}) oppure con {@code from}/{@code to}; senza nessuno
     * dei due copre l'intera stagione. {@code q} e {@code ordine} filtrano e
     * ordinano la classifica atleti lato server.
     */
    @GetMapping("/attendance")
    public ResponseEntity<StatistichePresenzeResponse> presenze(@ModelAttribute FiltroPresenzeRequest filtro) {
        return ResponseEntity.ok(presenzeService.calcola(filtro));
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
