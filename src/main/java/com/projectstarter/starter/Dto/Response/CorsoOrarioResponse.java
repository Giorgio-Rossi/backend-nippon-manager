package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.CorsoOrario;
import com.projectstarter.starter.Util.Giorni;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CorsoOrarioResponse {

    private Long id;
    /** ISO-8601: 1 = lunedi ... 7 = domenica. */
    private Integer giornoSettimana;
    private String giornoLabel;
    private String giornoBreve;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    /** "18:30–20:00" */
    private String fascia;
    /** "Lun 18:30–20:00": la riga pronta per i badge delle liste. */
    private String descrizione;
    private String sala;

    public static CorsoOrarioResponse from(CorsoOrario orario) {
        CorsoOrarioResponse response = new CorsoOrarioResponse();
        response.setId(orario.getId());
        response.setGiornoSettimana(orario.getGiornoSettimana());
        response.setGiornoLabel(Giorni.nome(orario.getGiornoSettimana()));
        response.setGiornoBreve(Giorni.breve(orario.getGiornoSettimana()));
        response.setOraInizio(orario.getOraInizio());
        response.setOraFine(orario.getOraFine());
        response.setFascia(Giorni.fascia(orario.getOraInizio(), orario.getOraFine()));
        response.setDescrizione(Giorni.descrizioneOrario(
                orario.getGiornoSettimana(), orario.getOraInizio(), orario.getOraFine()));
        response.setSala(orario.getSala());
        return response;
    }
}
