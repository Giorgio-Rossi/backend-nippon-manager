package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Atleta;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AtletaResponse {

    private Long id;
    private String nome;
    private String cognome;
    private LocalDate dataNascita;
    private String codiceFiscale;
    private String email;
    private String telefono;
    private String indirizzo;
    private String citta;
    private LocalDate dataIscrizione;
    private String tipoCertificato;
    private LocalDate dataRilascioCertificato;
    private LocalDate dataScadenzaCertificato;
    private Boolean attivo;
    private String note;

    public static AtletaResponse from(Atleta atleta) {
        AtletaResponse response = new AtletaResponse();
        response.setId(atleta.getId());
        response.setNome(atleta.getNome());
        response.setCognome(atleta.getCognome());
        response.setDataNascita(atleta.getDataNascita());
        response.setCodiceFiscale(atleta.getCodiceFiscale());
        response.setEmail(atleta.getEmail());
        response.setTelefono(atleta.getTelefono());
        response.setIndirizzo(atleta.getIndirizzo());
        response.setCitta(atleta.getCitta());
        response.setDataIscrizione(atleta.getDataIscrizione());
        response.setTipoCertificato(atleta.getTipoCertificato());
        response.setDataRilascioCertificato(atleta.getDataRilascioCertificato());
        response.setDataScadenzaCertificato(atleta.getDataScadenzaCertificato());
        response.setAttivo(atleta.getAttivo());
        response.setNote(atleta.getNote());
        return response;
    }
}
