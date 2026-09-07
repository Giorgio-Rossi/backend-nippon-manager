package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Request.AtletaRequest;
import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Dto.Response.ImportAtletiResponse;
import com.projectstarter.starter.Service.AtletaService;
import com.projectstarter.starter.Service.ImportAtletiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/athletes")
@RequiredArgsConstructor
public class AtletaController {

    private final AtletaService atletaService;
    private final ImportAtletiService importAtletiService;

    /**
     * @param q filtro su nominativo e codice fiscale: la lista arriva gia
     *          ridotta, cosi il client non filtra in memoria
     */
    @GetMapping
    public ResponseEntity<List<AtletaResponse>> getAll(
            @RequestParam(required = false) Boolean attivo,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(atletaService.findAll(attivo, q));
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

    /**
     * Import degli atleti dall'export tesserati del gestionale FIJLKAM.
     * L'abbinamento e sul codice fiscale: chi c'e gia viene aggiornato, chi
     * manca viene inserito, cosi il file si puo ricaricare piu volte durante
     * l'anno senza duplicare nessuno.
     *
     * @param dryRun con true restituisce solo l'anteprima dell'esito, senza
     *               scrivere nulla: il client mostra il riepilogo e fa
     *               confermare prima dell'import vero
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportAtletiResponse> importa(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean dryRun) {
        return ResponseEntity.ok(importAtletiService.importa(file, dryRun));
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
