package com.projectstarter.starter.Dto.Request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class IscrizioneRequest {

    /** Uno o piu atleti da associare al corso. */
    @NotEmpty
    private List<Long> atletaIds;

    private LocalDate dataIscrizione;

    private String note;
}
