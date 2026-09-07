package com.projectstarter.starter.Util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * La stagione sportiva va da settembre ad agosto ed e identificata da "2026/2027".
 */
public final class Stagioni {

    /** Mese in cui inizia la nuova stagione. */
    private static final int MESE_INIZIO = 9;

    private Stagioni() {
    }

    public static String corrente() {
        return da(LocalDate.now());
    }

    public static String da(LocalDate data) {
        int anno = data.getMonthValue() >= MESE_INIZIO ? data.getYear() : data.getYear() - 1;
        return anno + "/" + (anno + 1);
    }

    /** Forma compatta usata nelle note di tesseramento: "2025/2026" -> "25/26". */
    public static String breve(String stagione) {
        int anno = annoIniziale(stagione);
        return "%02d/%02d".formatted(anno % 100, (anno + 1) % 100);
    }

    /** Le stagioni selezionabili: dalla corrente piu {@code precedenti} indietro e una avanti. */
    public static List<String> intorno(int precedenti) {
        int annoCorrente = Integer.parseInt(corrente().substring(0, 4));
        List<String> stagioni = new ArrayList<>();
        for (int anno = annoCorrente + 1; anno >= annoCorrente - precedenti; anno--) {
            stagioni.add(anno + "/" + (anno + 1));
        }
        return stagioni;
    }

    public static boolean valida(String stagione) {
        return stagione != null && stagione.matches("\\d{4}/\\d{4}");
    }

    /**
     * La stagione indicata, o quella corrente se il parametro manca.
     *
     * @throws IllegalArgumentException se il formato non e "2026/2027"
     */
    public static String normalizza(String stagione) {
        if (stagione == null || stagione.isBlank()) {
            return corrente();
        }
        if (!valida(stagione)) {
            throw new IllegalArgumentException("Stagione non valida: attesa nel formato 2026/2027.");
        }
        return stagione;
    }

    /** Primo giorno della stagione: 1 settembre del primo anno. */
    public static LocalDate dataInizio(String stagione) {
        return LocalDate.of(annoIniziale(stagione), MESE_INIZIO, 1);
    }

    /** Ultimo giorno della stagione: 31 agosto dell'anno successivo. */
    public static LocalDate dataFine(String stagione) {
        return dataInizio(stagione).plusYears(1).minusDays(1);
    }

    private static int annoIniziale(String stagione) {
        if (!valida(stagione)) {
            throw new IllegalArgumentException("Stagione non valida: attesa nel formato 2026/2027.");
        }
        return Integer.parseInt(stagione.substring(0, 4));
    }
}
