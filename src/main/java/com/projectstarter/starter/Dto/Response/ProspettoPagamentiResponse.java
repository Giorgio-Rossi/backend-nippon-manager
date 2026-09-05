package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Pagamento;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Il prospetto pagamenti di un corso per una stagione, gia impaginato: le
 * colonne, una riga per atleta con le celle in ordine di colonna, e il
 * riepilogo. Il client non deve incrociare pagamenti e atleti ne sommare nulla.
 */
@Data
public class ProspettoPagamentiResponse {

    private Long corsoId;
    private String corsoNome;
    private String stagione;

    private List<Colonna> colonne;
    private List<Riga> atleti;
    private Riepilogo riepilogo;

    /** false quando il corso non ha quote impostate e gli importi vanno scritti a mano. */
    private boolean quoteImpostate;

    /** Una colonna del prospetto, con l'importo con cui precompilare le sue celle. */
    @Data
    public static class Colonna {

        private Pagamento.Tipo tipo;
        private String label;
        private String breve;
        /** true per la tessera: vale per l'atleta, non per il corso. */
        private boolean annuale;
        private Double importoSuggerito;

        public static Colonna di(Pagamento.Tipo tipo, Double importoSuggerito) {
            Colonna colonna = new Colonna();
            colonna.setTipo(tipo);
            colonna.setLabel(tipo.getLabel());
            colonna.setBreve(tipo.getBreve());
            colonna.setAnnuale(tipo.isAnnuale());
            colonna.setImportoSuggerito(importoSuggerito);
            return colonna;
        }
    }

    @Data
    public static class Riga {

        private Long atletaId;
        private String nome;
        private String cognome;
        private String nominativo;
        private String cintura;
        /** false per un atleta disiscritto in corso di stagione ma con pagamenti registrati. */
        private Boolean iscrizioneAttiva;
        /** Una cella per colonna, nello stesso ordine di {@link #getColonne()}. */
        private List<Cella> celle;
        /** Somma degli importi saldati della riga. */
        private double totale;
    }

    /** Lo stato di una cella: guida il colore e l'azione offerta dal client. */
    public enum StatoCella {

        VUOTA("Da registrare"),
        DA_SALDARE("Da saldare"),
        SALDATA("Saldata");

        private final String label;

        StatoCella(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    @Data
    public static class Cella {

        private Pagamento.Tipo tipo;
        private String tipoLabel;
        private StatoCella stato;
        private String statoLabel;
        /** Nullo finche la voce non e stata registrata. */
        private Long pagamentoId;
        private Double importo;
        /** Quota del corso con cui precompilare la cella vuota. */
        private Double importoSuggerito;
        /** true se l'importo registrato si discosta dalla quota del corso. */
        private boolean importoDiversoDaQuota;
        private LocalDate dataPagamento;
        private Pagamento.Metodo metodo;
        private String metodoLabel;
        private String note;
    }

    @Data
    public static class Riepilogo {

        private double incassato;
        private double atteso;
        private double daIncassare;
        /** Celle del prospetto in totale: righe per colonne. */
        private long celleTotali;
        private long celleCompilate;
        private long celleSaldate;
    }
}
