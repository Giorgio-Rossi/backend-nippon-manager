package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Request.PagamentoRequest;
import com.projectstarter.starter.Dto.Response.PagamentoResponse;
import com.projectstarter.starter.Dto.Response.ProspettoPagamentiResponse;
import com.projectstarter.starter.Service.PagamentiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PagamentiController {

    private final PagamentiService pagamentiService;

    /** Prospetto del corso: righe atleti, colonne tessera e due rate. */
    @GetMapping("/courses/{corsoId}/payments")
    public ResponseEntity<ProspettoPagamentiResponse> getProspetto(
            @PathVariable Long corsoId,
            @RequestParam(required = false) String stagione) {
        return ResponseEntity.ok(pagamentiService.prospetto(corsoId, stagione));
    }

    /** Storico completo dei pagamenti di un atleta, tutte le stagioni. */
    @GetMapping("/athletes/{atletaId}/payments")
    public ResponseEntity<List<PagamentoResponse>> getStoricoAtleta(@PathVariable Long atletaId) {
        return ResponseEntity.ok(pagamentiService.findByAtleta(atletaId));
    }

    @GetMapping("/payments/seasons")
    public ResponseEntity<List<String>> getStagioni() {
        return ResponseEntity.ok(pagamentiService.stagioni());
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<PagamentoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentiService.findById(id));
    }

    @PostMapping("/payments")
    public ResponseEntity<PagamentoResponse> create(@Valid @RequestBody PagamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamentiService.create(request));
    }

    /** Crea o aggiorna la cella del prospetto senza doverne conoscere l'id. */
    @PutMapping("/payments")
    public ResponseEntity<PagamentoResponse> salvaCella(@Valid @RequestBody PagamentoRequest request) {
        return ResponseEntity.ok(pagamentiService.salvaCella(request));
    }

    @PutMapping("/payments/{id}")
    public ResponseEntity<PagamentoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PagamentoRequest request) {
        return ResponseEntity.ok(pagamentiService.update(id, request));
    }

    @DeleteMapping("/payments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pagamentiService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
