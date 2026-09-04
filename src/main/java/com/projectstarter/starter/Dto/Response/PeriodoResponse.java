package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Util.Periodi;
import lombok.Data;

import java.time.LocalDate;

/** Il periodo effettivamente considerato, risolto dal server e non dal browser. */
@Data
public class PeriodoResponse {

    private Periodi.Preset preset;
    private String label;
    private LocalDate from;
    private LocalDate to;

    public static PeriodoResponse di(Periodi.Preset preset, Periodi.Intervallo intervallo) {
        PeriodoResponse response = new PeriodoResponse();
        response.setPreset(preset);
        response.setLabel(preset.getLabel());
        response.setFrom(intervallo.from());
        response.setTo(intervallo.to());
        return response;
    }
}
