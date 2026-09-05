package com.projectstarter.starter.Util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

/**
 * Periodi rapidi delle statistiche. Risolverli qui e non nel client evita che
 * "ultimi 30 giorni" cambi significato a seconda del fuso orario del browser.
 */
public final class Periodi {

    /** Un intervallo di date con gli estremi compresi. */
    public record Intervallo(LocalDate from, LocalDate to) {

        public Intervallo {
            if (from != null && to != null && to.isBefore(from)) {
                throw new IllegalArgumentException("La data di fine deve essere successiva a quella di inizio.");
            }
        }
    }

    public enum Preset {

        /** L'intera stagione sportiva selezionata. */
        STAGIONE("Stagione intera"),
        MESE_CORRENTE("Mese corrente"),
        ULTIMI_30("Ultimi 30 giorni"),
        ULTIMI_90("Ultimi 90 giorni"),
        /** Estremi indicati a mano dall'utente. */
        PERSONALIZZATO("Periodo personalizzato");

        private final String label;

        Preset(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        /** true quando il client deve mostrare i due campi data. */
        public boolean isRichiedeDate() {
            return this == PERSONALIZZATO;
        }
    }

    private Periodi() {
    }

    public static List<Preset> preset() {
        return Arrays.asList(Preset.values());
    }

    /**
     * Risolve il preset in date concrete.
     *
     * @param stagione stagione gia normalizzata, usata da {@link Preset#STAGIONE}
     * @param from     estremi indicati a mano; usati solo da {@link Preset#PERSONALIZZATO},
     *                 dove un estremo mancante ricade su quello della stagione
     */
    public static Intervallo risolvi(Preset preset, String stagione, LocalDate from, LocalDate to, LocalDate oggi) {
        LocalDate inizioStagione = Stagioni.dataInizio(stagione);
        LocalDate fineStagione = Stagioni.dataFine(stagione);

        return switch (preset == null ? Preset.STAGIONE : preset) {
            case STAGIONE -> new Intervallo(inizioStagione, fineStagione);
            case MESE_CORRENTE -> mese(YearMonth.from(oggi));
            case ULTIMI_30 -> new Intervallo(oggi.minusDays(29), oggi);
            case ULTIMI_90 -> new Intervallo(oggi.minusDays(89), oggi);
            case PERSONALIZZATO -> new Intervallo(
                    from != null ? from : inizioStagione,
                    to != null ? to : fineStagione);
        };
    }

    /** Primo e ultimo giorno di un mese: il periodo del foglio presenze. */
    public static Intervallo mese(YearMonth mese) {
        return new Intervallo(mese.atDay(1), mese.atEndOfMonth());
    }

    /**
     * Il preset da usare per una richiesta.
     * Estremi espliciti senza preset indicato valgono come periodo personalizzato,
     * cosi le chiamate esistenti continuano a funzionare.
     */
    public static Preset preset(String valore, LocalDate from, LocalDate to) {
        if (valore != null && !valore.isBlank()) {
            try {
                return Preset.valueOf(valore.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Periodo non valido: " + valore);
            }
        }
        return (from != null || to != null) ? Preset.PERSONALIZZATO : Preset.STAGIONE;
    }
}
