package com.projectstarter.starter.Dto.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CorsoOrarioRequest {

    /** 1 = Lunedi ... 7 = Domenica (ISO-8601) */
    @NotNull
    @Min(1)
    @Max(7)
    private Integer giornoSettimana;

    @NotNull
    private LocalTime oraInizio;

    @NotNull
    private LocalTime oraFine;

    @Size(max = 150)
    private String sala;
}
