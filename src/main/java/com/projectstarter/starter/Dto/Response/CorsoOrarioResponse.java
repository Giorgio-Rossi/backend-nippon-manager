package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.CorsoOrario;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CorsoOrarioResponse {

    private Long id;
    private Integer giornoSettimana;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private String sala;

    public static CorsoOrarioResponse from(CorsoOrario orario) {
        CorsoOrarioResponse response = new CorsoOrarioResponse();
        response.setId(orario.getId());
        response.setGiornoSettimana(orario.getGiornoSettimana());
        response.setOraInizio(orario.getOraInizio());
        response.setOraFine(orario.getOraFine());
        response.setSala(orario.getSala());
        return response;
    }
}
