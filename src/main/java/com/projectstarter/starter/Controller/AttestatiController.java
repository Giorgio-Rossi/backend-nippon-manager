package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Dto.Request.AttestatiRequest;
import com.projectstarter.starter.Service.AttestatiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attestati")
@RequiredArgsConstructor
public class AttestatiController {

    private final AttestatiService attestatiService;

    @PostMapping("/genera")
    public ResponseEntity<byte[]> genera(@Valid @RequestBody AttestatiRequest request) throws Exception {
        byte[] zip = attestatiService.generateZip(request);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"attestati.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
}
