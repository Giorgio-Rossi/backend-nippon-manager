package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Request.CorsoRequest;
import com.projectstarter.starter.Dto.Request.IscrizioneRequest;
import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Dto.Response.CorsoResponse;
import com.projectstarter.starter.Dto.Response.IscrizioneResponse;
import com.projectstarter.starter.Service.CorsoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CorsoController {

    private final CorsoService corsoService;

    /**
     * @param q filtro su nome e luogo: la lista arriva gia ridotta, cosi il
     *          client non filtra in memoria
     */
    @GetMapping
    public ResponseEntity<List<CorsoResponse>> getAll(
            @RequestParam(required = false) Boolean attivo,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(corsoService.findAll(attivo, q));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CorsoResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(corsoService.search(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorsoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(corsoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CorsoResponse> create(@Valid @RequestBody CorsoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corsoService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CorsoResponse> update(@PathVariable Long id, @Valid @RequestBody CorsoRequest request) {
        return ResponseEntity.ok(corsoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        corsoService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        corsoService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        corsoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Iscritti ----------

    @GetMapping("/{id}/members")
    public ResponseEntity<List<IscrizioneResponse>> getIscritti(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean attivo) {
        return ResponseEntity.ok(corsoService.findIscritti(id, attivo));
    }

    /** Atleti attivi non ancora iscritti: la differenza tra insiemi la fa il database. */
    @GetMapping("/{id}/enrollable")
    public ResponseEntity<List<AtletaResponse>> getIscrivibili(
            @PathVariable Long id,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(corsoService.findIscrivibili(id, q));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<List<IscrizioneResponse>> iscrivi(
            @PathVariable Long id,
            @Valid @RequestBody IscrizioneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corsoService.iscrivi(id, request));
    }

    @DeleteMapping("/{id}/members/{atletaId}")
    public ResponseEntity<Void> disiscrivi(@PathVariable Long id, @PathVariable Long atletaId) {
        corsoService.disiscrivi(id, atletaId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/members/{atletaId}/permanent")
    public ResponseEntity<Void> rimuoviIscrizione(@PathVariable Long id, @PathVariable Long atletaId) {
        corsoService.rimuoviIscrizione(id, atletaId);
        return ResponseEntity.noContent().build();
    }

    /** Corsi a cui risulta iscritto un atleta. */
    @GetMapping("/by-athlete/{atletaId}")
    public ResponseEntity<List<IscrizioneResponse>> getByAtleta(@PathVariable Long atletaId) {
        return ResponseEntity.ok(corsoService.findCorsiDiAtleta(atletaId));
    }
}
