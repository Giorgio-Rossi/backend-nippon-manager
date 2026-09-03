package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Entity.CorsoIscrizione;
import lombok.Data;

import java.time.LocalDate;

@Data
public class IscrizioneResponse {

    private Long id;
    private Long corsoId;
    private Long atletaId;
    private String nome;
    private String cognome;
    private String cintura;
    private LocalDate dataNascita;
    private Boolean atletaAttivo;
    private LocalDate dataIscrizione;
    private LocalDate dataDisiscrizione;
    private Boolean attivo;
    private String note;

    public static IscrizioneResponse from(CorsoIscrizione iscrizione) {
        Atleta atleta = iscrizione.getAtleta();
        IscrizioneResponse response = new IscrizioneResponse();
        response.setId(iscrizione.getId());
        response.setCorsoId(iscrizione.getCorso().getId());
        response.setAtletaId(atleta.getId());
        response.setNome(atleta.getNome());
        response.setCognome(atleta.getCognome());
        response.setCintura(atleta.getCintura());
        response.setDataNascita(atleta.getDataNascita());
        response.setAtletaAttivo(atleta.getAttivo());
        response.setDataIscrizione(iscrizione.getDataIscrizione());
        response.setDataDisiscrizione(iscrizione.getDataDisiscrizione());
        response.setAttivo(iscrizione.getAttivo());
        response.setNote(iscrizione.getNote());
        return response;
    }
}
