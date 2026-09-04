package com.projectstarter.starter.Dto.Response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Statistiche delle presenze in un periodo, per l'intera palestra o per un solo corso.
 * Le lezioni annullate e le presenze registrate su di esse sono escluse dai conteggi.
 */
@Data
public class StatistichePresenzeResponse {

    private LocalDate from;
    private LocalDate to;
    /** Nullo quando le statistiche coprono tutti i corsi. */
    private Long corsoId;
    private String corsoNome;

    /** Lezioni svolte, cioe non annullate, nel periodo. */
    private long lezioniSvolte;
    private long lezioniAnnullate;
    /** Celle del foglio presenze effettivamente compilate. */
    private long registrazioni;
    private long presenti;
    private long assenti;
    /** presenti / registrazioni, in percentuale. Nullo se non c'e nulla di registrato. */
    private Double tassoPresenza;
    private Double mediaPresentiPerLezione;
    private long atletiCoinvolti;

    private List<PuntoMensile> andamentoMensile;
    private List<RigaCorso> perCorso;
    private List<RigaAtleta> perAtleta;

    @Data
    public static class PuntoMensile {
        /** Formato "2026-09". */
        private String mese;
        private long lezioniSvolte;
        private long presenti;
        private long assenti;
        private Double tassoPresenza;
    }

    @Data
    public static class RigaCorso {
        private Long corsoId;
        private String corsoNome;
        private long iscrittiAttivi;
        private long lezioniSvolte;
        private long presenti;
        private long assenti;
        private Double tassoPresenza;
        private Double mediaPresentiPerLezione;
    }

    @Data
    public static class RigaAtleta {
        private Long atletaId;
        private String nome;
        private String cognome;
        private long presenti;
        private long assenti;
        private Double tassoPresenza;
    }
}
