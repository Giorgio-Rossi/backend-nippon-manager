package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Presenza;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Il foglio presenze di un corso in un mese, gia impaginato: colonne (lezioni),
 * righe (atleti) con le celle gia associate e i totali dello stato salvato.
 * Il client somma solo le modifiche che ha in sospeso.
 */
@Data
public class FoglioPresenzeResponse {

    private Long corsoId;
    private String corsoNome;

    /** Mese mostrato, formato "2026-09". */
    private String mese;
    /** "Settembre 2026" */
    private String meseLabel;
    /** Mesi adiacenti, cosi le frecce non devono calcolare date nel client. */
    private String mesePrecedente;
    private String meseSuccessivo;
    private LocalDate from;
    private LocalDate to;

    private List<Colonna> lezioni;
    private List<Riga> atleti;
    private Totali totali;

    /** Una lezione con i totali gia salvati per quella colonna. */
    @Data
    public static class Colonna {

        private LezioneResponse lezione;
        private long presenti;
        private long assenti;
        private long compilate;
    }

    @Data
    public static class Riga {

        private Long atletaId;
        private String nome;
        private String cognome;
        private String nominativo;
        private String cintura;
        /** false per un atleta disiscritto ma con presenze storiche nel periodo. */
        private Boolean iscrizioneAttiva;
        /** Stato salvato di ogni cella, indicizzato per id lezione. */
        private Map<Long, Presenza.Stato> celle;
        private long presenti;
        private long assenti;
    }

    @Data
    public static class Totali {

        private long lezioni;
        private long atleti;
        private long celleTotali;
        private long celleCompilate;
        private long presenti;
        private long assenti;
    }
}
