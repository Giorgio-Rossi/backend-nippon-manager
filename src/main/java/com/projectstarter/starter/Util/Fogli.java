package com.projectstarter.starter.Util;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lettura difensiva delle celle di un foglio Excel. Il file arriva da un
 * gestionale esterno: la stessa colonna puo essere testo in un export e numero
 * o data vera in quello successivo, quindi il tipo della cella non e un dato su
 * cui fare affidamento e va normalizzato qui una volta sola.
 */
public final class Fogli {

    /** Formati ammessi per le date: il gestionale usa il primo, gli altri coprono i file rimaneggiati a mano. */
    private static final List<DateTimeFormatter> FORMATI_DATA = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    /**
     * Soglie della protezione anti "zip bomb" di POI, allentate quel tanto che
     * basta a leggere gli export reali: il loro {@code styles.xml} e talmente
     * ripetitivo da comprimersi a un rapporto di 0,0099, appena sotto il minimo
     * di 0,01 imposto da POI, che altrimenti rifiuta un file legittimo. Il tetto
     * sulla dimensione della singola voce tiene comunque il limite significativo
     * anche con il rapporto piu permissivo.
     */
    private static final double RAPPORTO_MINIMO_COMPRESSIONE = 0.001;
    private static final long DIMENSIONE_MASSIMA_VOCE = 256L * 1024 * 1024;

    static {
        ZipSecureFile.setMinInflateRatio(RAPPORTO_MINIMO_COMPRESSIONE);
        ZipSecureFile.setMaxEntrySize(DIMENSIONE_MASSIMA_VOCE);
    }

    private Fogli() {
    }

    /**
     * Apre la cartella di lavoro. L'apertura passa da qui e non da
     * {@code WorkbookFactory} perche e questo il punto in cui le soglie di
     * sicurezza qui sopra risultano gia applicate: sono impostate
     * nell'inizializzatore della classe, che senza questo metodo verrebbe
     * eseguito solo alla prima lettura di una cella, cioe a file gia aperto.
     */
    public static Workbook apri(InputStream flusso) throws IOException {
        return WorkbookFactory.create(flusso);
    }

    /**
     * Il contenuto della cella come testo ripulito, oppure nullo se vuota.
     * I numeri interi non tornano mai in notazione scientifica ne con il ".0"
     * finale: un codice tessera o un numero di telefono letti come numero
     * devono restare la stessa stringa che si vede nel foglio.
     */
    public static String testo(Row riga, int colonna) {
        if (riga == null || colonna < 0) {
            return null;
        }
        return testo(riga.getCell(colonna));
    }

    public static String testo(Cell cella) {
        if (cella == null) {
            return null;
        }
        String valore = switch (cella.getCellType()) {
            case STRING -> cella.getStringCellValue();
            case BOOLEAN -> String.valueOf(cella.getBooleanCellValue());
            case NUMERIC -> numerico(cella);
            case FORMULA -> daFormula(cella);
            default -> null;
        };
        if (valore == null) {
            return null;
        }
        // Gli export contengono spesso spazi unificatori non separabili.
        String pulito = valore.replace(' ', ' ').trim();
        return pulito.isEmpty() ? null : pulito;
    }

    /**
     * La cella come data. Accetta sia le date vere di Excel sia il testo
     * "gg/mm/aaaa" con cui il gestionale le esporta.
     *
     * @return nullo se la cella e vuota
     * @throws IllegalArgumentException se il contenuto non e una data riconoscibile
     */
    public static LocalDate data(Row riga, int colonna) {
        Cell cella = riga == null || colonna < 0 ? null : riga.getCell(colonna);
        if (cella == null) {
            return null;
        }
        if (cella.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cella)) {
            return cella.getLocalDateTimeCellValue().toLocalDate();
        }
        String testo = testo(cella);
        if (testo == null) {
            return null;
        }
        for (DateTimeFormatter formato : FORMATI_DATA) {
            try {
                return LocalDate.parse(testo, formato);
            } catch (java.time.format.DateTimeParseException ignorata) {
                // formato successivo
            }
        }
        throw new IllegalArgumentException("data non valida: \"" + testo + "\" (atteso gg/mm/aaaa)");
    }

    /** Vera se la riga non esiste o non ha nemmeno una cella valorizzata. */
    public static boolean vuota(Row riga) {
        if (riga == null) {
            return true;
        }
        for (Cell cella : riga) {
            if (testo(cella) != null) {
                return false;
            }
        }
        return true;
    }

    private static String numerico(Cell cella) {
        if (DateUtil.isCellDateFormatted(cella)) {
            return cella.getLocalDateTimeCellValue().toLocalDate()
                    .format(FORMATI_DATA.get(0));
        }
        return BigDecimal.valueOf(cella.getNumericCellValue()).stripTrailingZeros().toPlainString();
    }

    private static String daFormula(Cell cella) {
        return switch (cella.getCachedFormulaResultType()) {
            case STRING -> cella.getStringCellValue();
            case NUMERIC -> numerico(cella);
            case BOOLEAN -> String.valueOf(cella.getBooleanCellValue());
            default -> null;
        };
    }
}
