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
}
