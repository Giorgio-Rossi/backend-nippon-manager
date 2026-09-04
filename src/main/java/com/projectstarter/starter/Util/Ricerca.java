package com.projectstarter.starter.Util;

/**
 * Normalizzazione dei filtri di ricerca testuale, condivisa da tutte le liste.
 * Le query li confrontano gia in minuscolo: normalizzare qui evita di ripetere
 * {@code LOWER()} e {@code TRIM()} in ogni JPQL e di sbagliarne uno.
 */
public final class Ricerca {

    private Ricerca() {
    }

    /** Il termine in minuscolo e senza spazi ai bordi, oppure nullo se vuoto. */
    public static String normalizza(String q) {
        if (q == null) {
            return null;
        }
        String pulito = q.trim().toLowerCase();
        return pulito.isEmpty() ? null : pulito;
    }
}
