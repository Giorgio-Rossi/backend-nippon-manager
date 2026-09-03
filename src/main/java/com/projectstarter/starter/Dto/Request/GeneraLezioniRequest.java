package com.projectstarter.starter.Dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Genera le lezioni del corso nel periodo indicato, a partire dalle fasce
 * ricorrenti settimanali impostate sul corso.
 */
@Data
public class GeneraLezioniRequest {

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;
}
