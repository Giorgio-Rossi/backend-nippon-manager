package com.projectstarter.starter.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AtletaRequest {

    @NotBlank
    @Size(max = 100)
    private String nome;

    @NotBlank
    @Size(max = 100)
    private String cognome;

    private LocalDate dataNascita;

    @Size(max = 16)
    private String codiceFiscale;

    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String telefono;

    @Size(max = 255)
    private String indirizzo;

    @Size(max = 100)
    private String citta;

    private LocalDate dataIscrizione;

    @Size(max = 50)
    private String tipoCertificato;

    private LocalDate dataRilascioCertificato;

    private LocalDate dataScadenzaCertificato;

    private String note;
}
