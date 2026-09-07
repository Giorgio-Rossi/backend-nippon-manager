package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Util.Statistiche;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Statistiche delle presenze in un periodo, per l'intera palestra o per un solo corso.
 * Le lezioni annullate e le presenze registrate su di esse sono escluse dai conteggi.
 * Le etichette dei mesi e il periodo effettivo sono risolti qui: dipendono dal
 * calendario dei dati, non dall'orologio del client.
 */
@Data
public class StatistichePresenzeResponse {

    private LocalDate from;
    private LocalDate to;
    /** Il periodo risolto, con il preset che lo ha prodotto. */
    private PeriodoResponse periodo;
    /** Nullo quando le statistiche coprono tutti i corsi. */
    private Long corsoId;
    private String corsoNome;
    /** Titolo dell'ambito: il nome del corso, oppure "Tutti i corsi". */
    private String ambito;
    /** Stagione di riferimento del periodo. */
    private String stagione;
    /** Giorno ISO su cui e filtrato il calcolo; nullo quando li copre tutti. */
    private Integer giorno;
    /** "Lunedi", oppure "Tutti i giorni" quando non c'e filtro. */
    private String giornoLabel;

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
    /**
     * Un elemento per ogni giorno con almeno una lezione nel periodo, in ordine
     * da lunedi a domenica. Con il filtro su un giorno resta la sola riga scelta.
     */
    private List<RigaGiorno> perGiorno;

    /** Classifica gia filtrata e ordinata secondo {@link #getOrdine()}. */
    private List<RigaAtleta> perAtleta;
    private Statistiche.Ordine ordine;
    /** Atleti con almeno una registrazione, prima del filtro di ricerca. */
    private long atletiInClassifica;

    @Data
    public static class PuntoMensile {
        /** Formato "2026-09". */
        private String mese;
        /** "set 26": etichetta dell'asse. */
        private String meseLabel;
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
    public static class RigaGiorno {
        /** ISO-8601: 1 = lunedi ... 7 = domenica. */
        private Integer giorno;
        /** "Lunedi" */
        private String giornoLabel;
        /** "Lun": etichetta compatta di legende e fette. */
        private String giornoBreve;
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
        private String nominativo;
        private long presenti;
        private long assenti;
        private long registrazioni;
        private Double tassoPresenza;
    }
}
