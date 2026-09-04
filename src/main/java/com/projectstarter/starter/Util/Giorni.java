package com.projectstarter.starter.Util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

/**
 * Etichette in italiano di giorni, mesi e orari. Stanno qui e non nel client
 * perche sono dominio: il giorno della settimana di una lezione dipende dalla
 * data memorizzata, non dal fuso orario del browser che la mostra.
 * I giorni seguono ISO-8601: 1 = lunedi ... 7 = domenica.
 */
public final class Giorni {

    private static final String[] NOMI = {
            "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica"
    };

    private static final String[] BREVI = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};

    private static final String[] MESI = {
            "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
            "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
    };

    private static final String[] MESI_BREVI = {
            "gen", "feb", "mar", "apr", "mag", "giu", "lug", "ago", "set", "ott", "nov", "dic"
    };

    /** Mostrato al posto di un valore mancante, cosi il client non deve gestire il nullo. */
    private static final String VUOTO = "—";

    private Giorni() {
    }

    public static String nome(Integer giornoIso) {
        return valido(giornoIso) ? NOMI[giornoIso - 1] : VUOTO;
    }

    public static String breve(Integer giornoIso) {
        return valido(giornoIso) ? BREVI[giornoIso - 1] : VUOTO;
    }

    public static String nome(LocalDate data) {
        return data == null ? VUOTO : nome(data.getDayOfWeek().getValue());
    }

    public static String breve(LocalDate data) {
        return data == null ? VUOTO : breve(data.getDayOfWeek().getValue());
    }

    /** "18:30", senza i secondi che il client dovrebbe tagliare da solo. */
    public static String ora(LocalTime ora) {
        return ora == null ? "" : String.format("%02d:%02d", ora.getHour(), ora.getMinute());
    }

    /** "18:30–20:00" */
    public static String fascia(LocalTime inizio, LocalTime fine) {
        return ora(inizio) + "–" + ora(fine);
    }

    /** "Lun 18:30–20:00": l'orario ricorrente di un corso in una riga. */
    public static String descrizioneOrario(Integer giornoIso, LocalTime inizio, LocalTime fine) {
        return breve(giornoIso) + " " + fascia(inizio, fine);
    }

    /** "05/09": intestazione compatta di colonna nel foglio presenze. */
    public static String dataBreve(LocalDate data) {
        return data == null ? VUOTO : String.format("%02d/%02d", data.getDayOfMonth(), data.getMonthValue());
    }

    /** "Settembre 2026" */
    public static String nomeMese(YearMonth mese) {
        return mese == null ? VUOTO : MESI[mese.getMonthValue() - 1] + " " + mese.getYear();
    }

    /** "set 26": etichetta dell'asse dei grafici mensili. */
    public static String meseBreve(YearMonth mese) {
        return mese == null ? VUOTO : MESI_BREVI[mese.getMonthValue() - 1] + " " + (mese.getYear() % 100);
    }

    public static String[] nomi() {
        return NOMI.clone();
    }

    public static String[] brevi() {
        return BREVI.clone();
    }

    private static boolean valido(Integer giornoIso) {
        return giornoIso != null && giornoIso >= 1 && giornoIso <= NOMI.length;
    }
}
