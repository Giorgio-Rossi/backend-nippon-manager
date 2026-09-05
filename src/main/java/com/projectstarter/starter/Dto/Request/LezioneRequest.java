package com.projectstarter.starter.Dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class LezioneRequest {

    @NotNull
    private LocalDate data;

    private LocalTime oraInizio;

    private LocalTime oraFine;

    @Size(max = 150)
    private String sala;

    private Boolean annullata;

    private String note;
}
