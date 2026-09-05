package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Lezione;
import com.projectstarter.starter.Util.Giorni;
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
    /** "Lun": il giorno della settimana dipende dalla data, non dal fuso del client. */
    private String giornoBreve;
    /** "05/09" */
    private String dataBreve;
    /** "18:30" */
    private String oraLabel;
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
        response.setGiornoBreve(Giorni.breve(lezione.getData()));
        response.setDataBreve(Giorni.dataBreve(lezione.getData()));
        response.setOraLabel(Giorni.ora(lezione.getOraInizio()));
        response.setSala(lezione.getSala());
        response.setAnnullata(lezione.getAnnullata());
        response.setNote(lezione.getNote());
        return response;
    }
}
