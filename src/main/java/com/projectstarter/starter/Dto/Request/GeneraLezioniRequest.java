package com.projectstarter.starter.Dto.Request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * Genera le lezioni del corso a partire dalle fasce ricorrenti settimanali.
 * Il caso normale e un intero mese ({@code mese = "2026-09"}); {@code from} e
 * {@code to} restano per generare un periodo qualsiasi.
 */
@Data
public class GeneraLezioniRequest {

    @Pattern(regexp = "\\d{4}-\\d{2}", message = "Mese non valido: atteso nel formato 2026-09.")
    private String mese;

    private LocalDate from;

    private LocalDate to;
}
