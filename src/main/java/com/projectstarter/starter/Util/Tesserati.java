package com.projectstarter.starter.Util;

import java.time.LocalDate;

/**
 * Regole di lettura dell'export tesserati del gestionale FIJLKAM.
 * Il foglio arriva tutto in maiuscolo e con una riga per tessera, non per
 * atleta: la normalizzazione dei nominativi e la nota che tiene traccia della
 * tessera stanno qui perche sono dominio e vanno identiche a ogni import.
 */
public final class Tesserati {

    /** Prefisso della nota generata: identifica le righe da rigenerare a ogni import. */
    public static final String PREFISSO_NOTA = "Tesseramento FIJLKAM ";

    /** Tessera di prova a scadenza breve: non e il tesseramento definitivo dell'atleta. */
    private static final String TESSERA_PROMOZIONALE = "Tessera Promozionale";

    private static final java.util.regex.Pattern CODICE_FISCALE =
            java.util.regex.Pattern.compile("[A-Z0-9]{16}");

    private Tesserati() {
    }

    /**
     * Il nominativo in forma leggibile: l'export lo fornisce tutto maiuscolo
     * ("DI LEVA", "BERNABE'") mentre l'archivio lo mostra come lo si scriverebbe
     * a mano. Le iniziali dopo spazio, apostrofo e trattino restano maiuscole,
     * cosi "DELL'ORTO" diventa "Dell'Orto" e non "Dell'orto".
     */
    public static String nominativo(String grezzo) {
        if (grezzo == null) {
            return null;
        }
        String pulito = grezzo.trim().replaceAll("\s+", " ");
        if (pulito.isEmpty()) {
            return null;
        }
        StringBuilder risultato = new StringBuilder(pulito.length());
        boolean iniziale = true;
        for (char carattere : pulito.toCharArray()) {
            risultato.append(iniziale ? Character.toUpperCase(carattere) : Character.toLowerCase(carattere));
            iniziale = carattere == ' ' || carattere == '\'' || carattere == '-';
        }
        return risultato.toString();
    }

    /** Il codice fiscale normalizzato, o nullo se non ha la forma attesa. */
    public static String codiceFiscale(String grezzo) {
        if (grezzo == null) {
            return null;
        }
        String pulito = grezzo.trim().replace(" ", "").toUpperCase();
        return CODICE_FISCALE.matcher(pulito).matches() ? pulito : null;
    }

    public static boolean promozionale(String tipoTessera) {
        return tipoTessera != null && tipoTessera.equalsIgnoreCase(TESSERA_PROMOZIONALE);
    }

    /**
     * La nota che registra la tessera valida per la stagione, nella stessa forma
     * usata dalla prima valorizzazione dell'archivio.
     */
    public static String nota(LocalDate emissione, String codiceTessera, String tipoTessera) {
        String stagione = Stagioni.breve(Stagioni.da(emissione == null ? LocalDate.now() : emissione));
        StringBuilder nota = new StringBuilder(PREFISSO_NOTA).append(stagione);
        if (codiceTessera != null) {
            nota.append(" n. ").append(codiceTessera);
        }
        if (tipoTessera != null) {
            nota.append(" (").append(tipoTessera).append(")");
        }
        return nota.toString();
    }

    /**
     * Sostituisce nella nota esistente la sola riga di tesseramento, lasciando
     * intatto quanto scritto a mano dalla segreteria: un import non deve far
     * perdere annotazioni che non ha prodotto lui.
     */
    public static String aggiornaNota(String esistente, String nota) {
        if (esistente == null || esistente.isBlank()) {
            return nota;
        }
        String manuale = esistente.lines()
                .filter(riga -> !riga.stripLeading().startsWith(PREFISSO_NOTA))
                .reduce((prima, seconda) -> prima + "\n" + seconda)
                .orElse("")
                .trim();
        return manuale.isEmpty() ? nota : nota + "\n" + manuale;
    }
}
