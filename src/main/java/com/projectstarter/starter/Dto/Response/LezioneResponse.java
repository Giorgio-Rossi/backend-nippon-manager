package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Lezione;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class LezioneResponse {

    private Long id;
    private Long corsoId;
    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private String sala;
    private Boolean annullata;
    private String note;

    public static LezioneResponse from(Lezione lezione) {
        LezioneResponse response = new LezioneResponse();
        response.setId(lezione.getId());
        response.setCorsoId(lezione.getCorso().getId());
        response.setData(lezione.getData());
        response.setOraInizio(lezione.getOraInizio());
        response.setOraFine(lezione.getOraFine());
        response.setSala(lezione.getSala());
        response.setAnnullata(lezione.getAnnullata());
        response.setNote(lezione.getNote());
        return response;
    }
}
