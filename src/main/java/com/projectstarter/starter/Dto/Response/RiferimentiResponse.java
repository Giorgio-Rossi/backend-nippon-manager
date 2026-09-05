package com.projectstarter.starter.Dto.Response;

import lombok.Data;

import java.util.List;

/**
 * Le voci di dominio che il client mostra nelle select e nelle legende: giorni,
 * tipi e metodi di pagamento, stati di presenza, periodi delle statistiche.
 * Stanno in un'unica risposta cosi le etichette hanno una sola definizione e
 * aggiungere un valore non richiede di toccare il client.
 */
@Data
public class RiferimentiResponse {

    private List<Voce> giorni;
    private List<Voce> tipiPagamento;
    private List<Voce> metodiPagamento;
    /** Gli stati compilabili di una cella del foglio presenze, nell'ordine del ciclo. */
    private List<Voce> statiPresenza;
    private List<Voce> periodiStatistiche;
    private List<Voce> ordinamentiClassifica;

    /** Un valore selezionabile: il codice che viaggia e le etichette da mostrare. */
    @Data
    public static class Voce {

        private String valore;
        private String label;
        /** Forma abbreviata per intestazioni e badge; assente quando coincide con label. */
        private String breve;

        public static Voce di(String valore, String label) {
            return di(valore, label, null);
        }

        public static Voce di(String valore, String label, String breve) {
            Voce voce = new Voce();
            voce.setValore(valore);
            voce.setLabel(label);
            voce.setBreve(breve);
            return voce;
        }
    }
}
