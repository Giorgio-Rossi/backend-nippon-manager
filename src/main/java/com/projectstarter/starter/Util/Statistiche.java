package com.projectstarter.starter.Util;

/** Opzioni di presentazione delle statistiche che il server risolve per il client. */
public final class Statistiche {

    /** Verso della classifica presenze. */
    public enum Ordine {

        MIGLIORI("Dal piu presente"),
        PEGGIORI("Dal meno presente");

        private final String label;

        Ordine(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static Ordine da(String valore) {
            if (valore == null || valore.isBlank()) {
                return MIGLIORI;
            }
            try {
                return valueOf(valore.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Ordinamento non valido: " + valore);
            }
        }
    }

    private Statistiche() {
    }
}
