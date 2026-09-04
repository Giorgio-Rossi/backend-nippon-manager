package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Pagamento;
import lombok.Data;

import java.util.List;

/**
 * Statistiche degli incassi di una stagione, ricavate dallo storico pagamenti.
 * "Atteso" e la somma degli importi registrati, "incassato" la sola parte con
 * una data di pagamento valorizzata. Le etichette di tipi, metodi e mesi
 * arrivano gia risolte: il client non tiene mappe di enum proprie.
 */
@Data
public class StatisticheIncassiResponse {

    private String stagione;
    /** Nullo quando le statistiche coprono tutti i corsi (tessere comprese). */
    private Long corsoId;
    private String corsoNome;
    /** Titolo dell'ambito: il nome del corso, oppure "Tutti i corsi". */
    private String ambito;

    private double totaleAtteso;
    private double totaleIncassato;
    private double totaleResiduo;
    /** incassato / atteso, in percentuale. Nullo se non c'e nulla di atteso. */
    private Double percentualeIncasso;

    private long pagamentiTotali;
    private long pagamentiSaldati;
    private long pagamentiInSospeso;
    private long atletiCoinvolti;
    private long atletiMorosi;

    private List<RigaTipo> perTipo;
    private List<RigaMetodo> perMetodo;
    private List<RigaCorso> perCorso;
    private List<PuntoMensile> andamentoMensile;
    private List<RigaSospeso> sospesi;

    @Data
    public static class RigaTipo {
        private Pagamento.Tipo tipo;
        private String tipoLabel;
        private double atteso;
        private double incassato;
        private double residuo;
        private long conteggio;
        private long saldati;
    }

    @Data
    public static class RigaMetodo {
        /** Nome dell'enum, oppure "NON_SPECIFICATO" per gli incassi senza metodo. */
        private String metodo;
        private String metodoLabel;
        private double incassato;
        private long conteggio;
    }

    @Data
    public static class RigaCorso {
        /** Nullo per la riga delle tessere associative, che non sono legate a un corso. */
        private Long corsoId;
        private String corsoNome;
        private double atteso;
        private double incassato;
        private double residuo;
    }

    @Data
    public static class PuntoMensile {
        /** Formato "2026-09", basato sulla data di pagamento. */
        private String mese;
        /** "set 26": etichetta dell'asse. */
        private String meseLabel;
        private double incassato;
        private long conteggio;
    }

    @Data
    public static class RigaSospeso {
        private Long pagamentoId;
        private Long atletaId;
        private String nome;
        private String cognome;
        private String nominativo;
        private Long corsoId;
        private String corsoNome;
        private Pagamento.Tipo tipo;
        private String tipoLabel;
        private double importo;
    }
}
