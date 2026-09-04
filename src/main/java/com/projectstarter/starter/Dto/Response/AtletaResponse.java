package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Util.Certificati;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AtletaResponse {

    private Long id;
    private String nome;
    private String cognome;
    /** "Rossi Mario": il modo in cui l'atleta va scritto ovunque nelle liste. */
    private String nominativo;
    private LocalDate dataNascita;
    private String codiceFiscale;
    private String email;
    private String telefono;
    private String indirizzo;
    private String citta;
    private LocalDate dataIscrizione;
    private String cintura;
    private String tipoCertificato;
    private LocalDate dataRilascioCertificato;
    private LocalDate dataScadenzaCertificato;
    /** Negativo se il certificato e gia scaduto, nullo se non c'e una data. */
    private Integer giorniAllaScadenza;
    private Certificati.Stato statoCertificato;
    private String statoCertificatoLabel;
    private Boolean attivo;
    private String note;

    public static AtletaResponse from(Atleta atleta) {
        return from(atleta, LocalDate.now());
    }

    /**
     * @param oggi data di riferimento per lo stato del certificato: passarla
     *             esplicitamente mantiene coerenti tutte le righe di una lista
     */
    public static AtletaResponse from(Atleta atleta, LocalDate oggi) {
        AtletaResponse response = new AtletaResponse();
        response.setId(atleta.getId());
        response.setNome(atleta.getNome());
        response.setCognome(atleta.getCognome());
        response.setNominativo(nominativo(atleta));
        response.setDataNascita(atleta.getDataNascita());
        response.setCodiceFiscale(atleta.getCodiceFiscale());
        response.setEmail(atleta.getEmail());
        response.setTelefono(atleta.getTelefono());
        response.setIndirizzo(atleta.getIndirizzo());
        response.setCitta(atleta.getCitta());
        response.setDataIscrizione(atleta.getDataIscrizione());
        response.setCintura(atleta.getCintura());
        response.setTipoCertificato(atleta.getTipoCertificato());
        response.setDataRilascioCertificato(atleta.getDataRilascioCertificato());
        response.setDataScadenzaCertificato(atleta.getDataScadenzaCertificato());

        Certificati.Stato stato = Certificati.stato(atleta.getDataScadenzaCertificato(), oggi);
        response.setGiorniAllaScadenza(Certificati.giorniAllaScadenza(atleta.getDataScadenzaCertificato(), oggi));
        response.setStatoCertificato(stato);
        response.setStatoCertificatoLabel(stato.getLabel());

        response.setAttivo(atleta.getAttivo());
        response.setNote(atleta.getNote());
        return response;
    }

    /** Usato anche dalle righe di prospetto e foglio presenze, che mostrano lo stesso testo. */
    public static String nominativo(Atleta atleta) {
        return nominativo(atleta.getCognome(), atleta.getNome());
    }

    public static String nominativo(String cognome, String nome) {
        return ((cognome == null ? "" : cognome) + " " + (nome == null ? "" : nome)).trim();
    }
}
