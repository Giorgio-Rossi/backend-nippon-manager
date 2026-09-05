package com.projectstarter.starter.Util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Politica di scadenza dei certificati medici. Le soglie sono una regola della
 * societa, non una scelta grafica: vivono qui e il client si limita a colorare
 * lo stato che riceve.
 */
public final class Certificati {

    /** Sotto questa soglia il rinnovo e urgente. */
    public static final int GIORNI_CRITICI = 7;
    /** Finestra di preavviso predefinita. */
    public static final int GIORNI_PREAVVISO = 30;

    public enum Stato {

        /** Nessuna data di scadenza registrata. */
        ASSENTE("Nessun certificato"),
        SCADUTO("Scaduto"),
        CRITICO("In scadenza"),
        IN_SCADENZA("In scadenza"),
        VALIDO("Valido");

        private final String label;

        Stato(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private Certificati() {
    }

    /** Giorni mancanti alla scadenza: negativi se gia scaduto, nullo senza data. */
    public static Integer giorniAllaScadenza(LocalDate scadenza, LocalDate oggi) {
        return scadenza == null ? null : (int) ChronoUnit.DAYS.between(oggi, scadenza);
    }

    public static Stato stato(LocalDate scadenza, LocalDate oggi) {
        Integer giorni = giorniAllaScadenza(scadenza, oggi);
        if (giorni == null) {
            return Stato.ASSENTE;
        }
        if (giorni < 0) {
            return Stato.SCADUTO;
        }
        if (giorni <= GIORNI_CRITICI) {
            return Stato.CRITICO;
        }
        return giorni <= GIORNI_PREAVVISO ? Stato.IN_SCADENZA : Stato.VALIDO;
    }
}
