package com.projectstarter.starter.Util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Operazioni ricorrenti nel calcolo delle statistiche, condivise dai service
 * di presenze e incassi.
 */
public final class Aggregazioni {

    private Aggregazioni() {
    }

    /** Due decimali: importi e percentuali escono dal service gia pronti per la UI. */
    public static double arrotonda(double valore) {
        return Math.round(valore * 100d) / 100d;
    }

    /**
     * Percentuale di {@code parte} su {@code totale}.
     *
     * @return nullo quando non c'e nulla da rapportare: uno zero direbbe
     *         "nessuno presente" invece di "nessuna registrazione"
     */
    public static Double percentuale(long parte, long totale) {
        return totale == 0 ? null : arrotonda((double) parte / totale * 100);
    }

    /**
     * I mesi coperti dal periodo, estremi compresi. Il taglio a {@code max} evita
     * che un intervallo assurdo produca un grafico illeggibile.
     */
    public static List<YearMonth> mesi(LocalDate from, LocalDate to, int max) {
        List<YearMonth> mesi = new ArrayList<>();
        YearMonth ultimo = YearMonth.from(to);
        for (YearMonth mese = YearMonth.from(from);
             !mese.isAfter(ultimo) && mesi.size() < max;
             mese = mese.plusMonths(1)) {
            mesi.add(mese);
        }
        return mesi;
    }
}
