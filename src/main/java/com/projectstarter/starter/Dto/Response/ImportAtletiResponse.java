package com.projectstarter.starter.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Esito dell'import dei tesserati da Excel. Riporta anche le righe scartate:
 * il file arriva da un gestionale esterno e chi lo carica deve sapere quali
 * atleti non sono entrati e perche, senza andare a rileggere il foglio.
 */
@Data
public class ImportAtletiResponse {

    /** Vera se l'import era una simulazione: nulla e stato scritto in archivio. */
    private boolean simulazione;

    /** Righe di tessera lette dal foglio, escluse quelle vuote. */
    private int righeLette;

    /** Atleti distinti nel foglio: piu tessere dello stesso codice fiscale contano una volta sola. */
    private int tesseratiUnici;

    private int inseriti;
    private int aggiornati;
    private int invariati;
    private int scartati;

    private List<ErroreImport> errori = new ArrayList<>();

    private String messaggio;

    /** Una riga che non e stata importata, con il motivo gia leggibile. */
    @Data
    @AllArgsConstructor
    public static class ErroreImport {
        /** Numero di riga come si vede in Excel, per ritrovarla nel foglio. */
        private int riga;
        private String nominativo;
        private String codiceFiscale;
        private String motivo;
    }

    public void aggiungiErrore(int riga, String nominativo, String codiceFiscale, String motivo) {
        errori.add(new ErroreImport(riga, nominativo, codiceFiscale, motivo));
        scartati++;
    }

    /** Il riepilogo mostrato all'utente a fine import. */
    public void componiMessaggio() {
        if (tesseratiUnici == 0 && scartati == 0) {
            messaggio = "Il foglio non contiene tesserati da importare.";
            return;
        }
        StringBuilder testo = new StringBuilder(simulazione ? "Simulazione: " : "Import completato: ");
        testo.append(inseriti).append(simulazione ? " da inserire, " : " inseriti, ")
                .append(aggiornati).append(simulazione ? " da aggiornare, " : " aggiornati, ")
                .append(invariati).append(" gia allineati");
        if (scartati > 0) {
            testo.append(", ").append(scartati).append(scartati == 1 ? " riga scartata" : " righe scartate");
        }
        messaggio = testo.append(".").toString();
    }
}
