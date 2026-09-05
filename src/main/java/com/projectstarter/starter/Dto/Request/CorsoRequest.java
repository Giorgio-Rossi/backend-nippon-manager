package com.projectstarter.starter.Dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CorsoRequest {

    @NotBlank
    @Size(max = 150)
    private String nome;

    private String descrizione;

    @Size(max = 150)
    private String luogo;

    private LocalDate dataInizio;

    private LocalDate dataFine;

    private Double quotaMensile;

    private Double quotaTessera;

    private Double quotaRata;

    private String note;

    @Valid
    private List<CorsoOrarioRequest> orari = new ArrayList<>();
}
