package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Response.ImportAtletiResponse;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Repository.AtletaRepository;
import com.projectstarter.starter.Util.Fogli;
import com.projectstarter.starter.Util.Tesserati;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Import degli atleti dall'export tesserati del gestionale FIJLKAM.
 *
 * Il foglio ha una riga per tessera e non per atleta: lo stesso codice fiscale
 * compare piu volte quando alla tessera promozionale di prova segue quella
 * definitiva. L'import raggruppa quindi per codice fiscale prima di scrivere,
 * cosi un atleta con due tessere resta una sola anagrafica.
 *
 * Sui record gia presenti sovrascrive le sole colonne governate dal foglio: una
 * cella vuota non azzera un dato inserito a mano nell'applicativo, e i campi del
 * certificato medico non vengono mai toccati perche l'export non li contiene.
 */
@Service
@RequiredArgsConstructor
public class ImportAtletiService {

    private final AtletaRepository atletaRepository;

    /** Righe entro cui cercare l'intestazione: sopra c'e il titolo dell'estrazione. */
    private static final int RIGHE_INTESTAZIONE = 10;

    private static final String COL_NOME = "nome";
    private static final String COL_COGNOME = "cognome";
    private static final String COL_NASCITA = "nato il";
    private static final String COL_CODICE_FISCALE = "codice fiscale";
    private static final String COL_CITTA = "comune res";
    private static final String COL_INDIRIZZO = "indirizzo res";
    private static final String COL_EMAIL = "e mail";
    private static final String COL_TESSERA = "cod tessera";
    private static final String COL_TIPO_TESSERA = "tipo tessera";
    private static final String COL_EMISSIONE = "emessa il";
    private static final String COL_CINTURA = "cintura";

    /** Il recapito puo stare in una qualsiasi di queste colonne: si prende la prima valorizzata. */
    private static final List<String> COLONNE_TELEFONO = List.of("telefono", "cell");

    /** Senza queste colonne il file non e l'export dei tesserati e non ha senso proseguire. */
    private static final List<String> COLONNE_OBBLIGATORIE =
            List.of(COL_NOME, COL_COGNOME, COL_CODICE_FISCALE);

    /** Lunghezze massime delle colonne di {@code atleti}: i valori piu lunghi vengono troncati. */
    private static final int MAX_NOMINATIVO = 100;
    private static final int MAX_EMAIL = 100;
    private static final int MAX_TELEFONO = 20;
    private static final int MAX_INDIRIZZO = 255;
    private static final int MAX_CINTURA = 50;

    /**
     * Legge il foglio e allinea l'archivio atleti.
     *
     * @param simulazione se vera nulla viene scritto: la risposta descrive solo
     *                    cosa succederebbe, cosi il frontend puo far confermare
     *                    l'operazione prima di modificare l'archivio
     */
    @Transactional
    public ImportAtletiResponse importa(MultipartFile file, boolean simulazione) {
        ImportAtletiResponse esito = new ImportAtletiResponse();
        esito.setSimulazione(simulazione);

        try (InputStream flusso = validaFile(file).getInputStream();
             Workbook cartella = Fogli.apri(flusso)) {

            if (cartella.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("Il file Excel non contiene alcun foglio.");
            }
            Sheet foglio = cartella.getSheetAt(0);

            int rigaIntestazione = trovaIntestazione(foglio);
            Map<String, List<Integer>> colonne = mappaColonne(foglio.getRow(rigaIntestazione));
            verificaColonne(colonne);

            List<Tessera> tessere = leggiTessere(foglio, rigaIntestazione, colonne, esito);
            esito.setRigheLette(tessere.size() + esito.getScartati());

            scrivi(raggruppaPerAtleta(tessere), simulazione, esito);

        } catch (IOException e) {
            throw new IllegalArgumentException("Impossibile leggere il file Excel: " + e.getMessage());
        }

        esito.componiMessaggio();
        return esito;
    }

    private MultipartFile validaFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Nessun file caricato.");
        }
        String nome = file.getOriginalFilename();
        if (nome != null && !nome.toLowerCase().matches(".*\\.(xlsx|xlsm|xls)$")) {
            throw new IllegalArgumentException("Formato non supportato: caricare un file Excel (.xlsx).");
        }
        return file;
    }

    /**
     * La riga di intestazione non e sempre la prima: l'export antepone il titolo
     * dell'estrazione. Si riconosce dalla presenza della colonna del codice fiscale.
     */
    private int trovaIntestazione(Sheet foglio) {
        int ultima = Math.min(foglio.getLastRowNum(), RIGHE_INTESTAZIONE);
        for (int indice = foglio.getFirstRowNum(); indice <= ultima; indice++) {
            if (mappaColonne(foglio.getRow(indice)).containsKey(COL_CODICE_FISCALE)) {
                return indice;
            }
        }
        throw new IllegalArgumentException(
                "Intestazioni non riconosciute: il file non sembra l'export dei tesserati "
                        + "(manca la colonna \"Codice Fiscale\").");
    }

    /**
     * Le colonne si individuano dall'etichetta e non dalla posizione, cosi un
     * export con colonne in ordine diverso resta importabile. Alcune etichette
     * sono ripetute (il foglio ha due colonne "telefono"): si tengono tutte.
     */
    private Map<String, List<Integer>> mappaColonne(Row intestazione) {
        Map<String, List<Integer>> colonne = new HashMap<>();
        if (intestazione == null) {
            return colonne;
        }
        for (int indice = 0; indice < intestazione.getLastCellNum(); indice++) {
            String etichetta = normalizzaEtichetta(Fogli.testo(intestazione, indice));
            if (etichetta != null) {
                colonne.computeIfAbsent(etichetta, chiave -> new ArrayList<>()).add(indice);
            }
        }
        return colonne;
    }

    /** "Cod.Tessera", "e-mail" e "Comune Res." diventano confrontabili tra loro. */
    private String normalizzaEtichetta(String etichetta) {
        if (etichetta == null) {
            return null;
        }
        String pulita = etichetta.toLowerCase().replace('.', ' ').replace('-', ' ')
                .replaceAll("\\s+", " ").trim();
        return pulita.isEmpty() ? null : pulita;
    }

    private void verificaColonne(Map<String, List<Integer>> colonne) {
        List<String> mancanti = COLONNE_OBBLIGATORIE.stream()
                .filter(colonna -> !colonne.containsKey(colonna))
                .toList();
        if (!mancanti.isEmpty()) {
            throw new IllegalArgumentException(
                    "Colonne obbligatorie mancanti nel foglio: " + String.join(", ", mancanti) + ".");
        }
    }

    private int colonna(Map<String, List<Integer>> colonne, String etichetta) {
        List<Integer> indici = colonne.get(etichetta);
        return indici == null || indici.isEmpty() ? -1 : indici.get(0);
    }

    /** Le righe leggibili del foglio; quelle in errore finiscono nell'esito e non bloccano le altre. */
    private List<Tessera> leggiTessere(Sheet foglio, int rigaIntestazione,
                                       Map<String, List<Integer>> colonne, ImportAtletiResponse esito) {
        List<Tessera> tessere = new ArrayList<>();
        for (int indice = rigaIntestazione + 1; indice <= foglio.getLastRowNum(); indice++) {
            Row riga = foglio.getRow(indice);
            if (Fogli.vuota(riga)) {
                continue;
            }
            // In Excel le righe si contano da 1, qui l'indice parte da 0.
            int numeroRiga = indice + 1;
            try {
                tessere.add(leggiTessera(riga, colonne));
            } catch (IllegalArgumentException e) {
                esito.aggiungiErrore(numeroRiga, nominativoDi(riga, colonne),
                        Fogli.testo(riga, colonna(colonne, COL_CODICE_FISCALE)), e.getMessage());
            }
        }
        return tessere;
    }

    /** Il nominativo cosi come si legge nel foglio, per far ritrovare la riga in errore. */
    private String nominativoDi(Row riga, Map<String, List<Integer>> colonne) {
        String nome = Tesserati.nominativo(Fogli.testo(riga, colonna(colonne, COL_NOME)));
        String cognome = Tesserati.nominativo(Fogli.testo(riga, colonna(colonne, COL_COGNOME)));
        if (nome == null && cognome == null) {
            return null;
        }
        return ((cognome == null ? "" : cognome) + " " + (nome == null ? "" : nome)).trim();
    }

    private Tessera leggiTessera(Row riga, Map<String, List<Integer>> colonne) {
        String grezzo = Fogli.testo(riga, colonna(colonne, COL_CODICE_FISCALE));
        if (grezzo == null) {
            throw new IllegalArgumentException("codice fiscale mancante");
        }
        String codiceFiscale = Tesserati.codiceFiscale(grezzo);
        if (codiceFiscale == null) {
            throw new IllegalArgumentException("codice fiscale non valido: \"" + grezzo + "\"");
        }

        String nome = Tesserati.nominativo(Fogli.testo(riga, colonna(colonne, COL_NOME)));
        String cognome = Tesserati.nominativo(Fogli.testo(riga, colonna(colonne, COL_COGNOME)));
        if (nome == null || cognome == null) {
            throw new IllegalArgumentException("nome o cognome mancante");
        }

        return new Tessera(
                tronca(nome, MAX_NOMINATIVO),
                tronca(cognome, MAX_NOMINATIVO),
                Fogli.data(riga, colonna(colonne, COL_NASCITA)),
                codiceFiscale,
                tronca(Fogli.testo(riga, colonna(colonne, COL_EMAIL)), MAX_EMAIL),
                tronca(telefono(riga, colonne), MAX_TELEFONO),
                tronca(Fogli.testo(riga, colonna(colonne, COL_INDIRIZZO)), MAX_INDIRIZZO),
                tronca(Fogli.testo(riga, colonna(colonne, COL_CITTA)), MAX_NOMINATIVO),
                Fogli.data(riga, colonna(colonne, COL_EMISSIONE)),
                Fogli.testo(riga, colonna(colonne, COL_TESSERA)),
                Fogli.testo(riga, colonna(colonne, COL_TIPO_TESSERA)),
                tronca(Fogli.testo(riga, colonna(colonne, COL_CINTURA)), MAX_CINTURA));
    }

    /** Il primo recapito valorizzato tra le colonne telefoniche del foglio. */
    private String telefono(Row riga, Map<String, List<Integer>> colonne) {
        for (String etichetta : COLONNE_TELEFONO) {
            for (int indice : colonne.getOrDefault(etichetta, List.of())) {
                String valore = Fogli.testo(riga, indice);
                if (valore != null) {
                    return valore.replaceAll("[\\s./]", "");
                }
            }
        }
        return null;
    }

    private String tronca(String valore, int massimo) {
        if (valore == null) {
            return null;
        }
        return valore.length() <= massimo ? valore : valore.substring(0, massimo);
    }

    /**
     * Una anagrafica per codice fiscale. I dati si prendono dalla tessera
     * definitiva, ma la data di iscrizione e quella della prima tessera emessa:
     * l'atleta frequenta dalla promozionale, non da quando l'ha rinnovata.
     */
    private Map<String, Anagrafica> raggruppaPerAtleta(List<Tessera> tessere) {
        Map<String, List<Tessera>> perCodiceFiscale = new LinkedHashMap<>();
        for (Tessera tessera : tessere) {
            perCodiceFiscale.computeIfAbsent(tessera.codiceFiscale(), chiave -> new ArrayList<>()).add(tessera);
        }

        Map<String, Anagrafica> anagrafiche = new LinkedHashMap<>();
        perCodiceFiscale.forEach((codiceFiscale, gruppo) -> {
            Tessera principale = gruppo.stream().max(Comparator
                            .comparingInt((Tessera tessera) -> Tesserati.promozionale(tessera.tipoTessera()) ? 0 : 1)
                            .thenComparing(Tessera::emissione, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .orElseThrow();
            LocalDate primaEmissione = gruppo.stream()
                    .map(Tessera::emissione)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
            anagrafiche.put(codiceFiscale, new Anagrafica(principale, primaEmissione));
        });
        return anagrafiche;
    }

    private void scrivi(Map<String, Anagrafica> anagrafiche, boolean simulazione, ImportAtletiResponse esito) {
        esito.setTesseratiUnici(anagrafiche.size());
        if (anagrafiche.isEmpty()) {
            return;
        }

        Map<String, Atleta> esistenti = new HashMap<>();
        atletaRepository.findByCodiceFiscaleIn(anagrafiche.keySet())
                .forEach(atleta -> esistenti.put(atleta.getCodiceFiscale(), atleta));

        List<Atleta> daSalvare = new ArrayList<>();
        for (Map.Entry<String, Anagrafica> voce : anagrafiche.entrySet()) {
            Atleta esistente = esistenti.get(voce.getKey());
            if (esistente == null) {
                esito.setInseriti(esito.getInseriti() + 1);
                if (!simulazione) {
                    Atleta nuovo = new Atleta();
                    nuovo.setCodiceFiscale(voce.getKey());
                    nuovo.setAttivo(true);
                    applica(nuovo, voce.getValue(), true);
                    daSalvare.add(nuovo);
                }
            } else if (applica(esistente, voce.getValue(), !simulazione)) {
                esito.setAggiornati(esito.getAggiornati() + 1);
                if (!simulazione) {
                    daSalvare.add(esistente);
                }
            } else {
                esito.setInvariati(esito.getInvariati() + 1);
            }
        }

        if (!daSalvare.isEmpty()) {
            atletaRepository.saveAll(daSalvare);
        }
    }

    /**
     * Allinea l'atleta ai dati del foglio.
     *
     * @param scrivi se falso l'entita non viene toccata: serve alla simulazione,
     *               che deve contare le modifiche senza che JPA le renda persistenti
     * @return vero se almeno un campo risulta diverso da quanto gia in archivio
     */
    private boolean applica(Atleta atleta, Anagrafica anagrafica, boolean scrivi) {
        Tessera tessera = anagrafica.principale();
        boolean cambiato = imposta(atleta::getNome, atleta::setNome, tessera.nome(), scrivi);
        cambiato |= imposta(atleta::getCognome, atleta::setCognome, tessera.cognome(), scrivi);
        cambiato |= imposta(atleta::getDataNascita, atleta::setDataNascita, tessera.dataNascita(), scrivi);
        cambiato |= imposta(atleta::getEmail, atleta::setEmail, tessera.email(), scrivi);
        cambiato |= imposta(atleta::getTelefono, atleta::setTelefono, tessera.telefono(), scrivi);
        cambiato |= imposta(atleta::getIndirizzo, atleta::setIndirizzo, tessera.indirizzo(), scrivi);
        cambiato |= imposta(atleta::getCitta, atleta::setCitta, tessera.citta(), scrivi);
        cambiato |= imposta(atleta::getCintura, atleta::setCintura, tessera.cintura(), scrivi);
        cambiato |= imposta(atleta::getDataIscrizione, atleta::setDataIscrizione,
                anagrafica.primaEmissione(), scrivi);

        String nota = Tesserati.aggiornaNota(atleta.getNote(),
                Tesserati.nota(tessera.emissione(), tessera.codiceTessera(), tessera.tipoTessera()));
        cambiato |= imposta(atleta::getNote, atleta::setNote, nota, scrivi);
        return cambiato;
    }

    /**
     * Assegna il valore solo se il foglio lo fornisce ed e diverso da quello
     * gia presente: una cella vuota non cancella un dato inserito a mano.
     */
    private <T> boolean imposta(Supplier<T> lettore, Consumer<T> scrittore, T nuovo, boolean scrivi) {
        if (nuovo == null || nuovo.equals(lettore.get())) {
            return false;
        }
        if (scrivi) {
            scrittore.accept(nuovo);
        }
        return true;
    }

    /** Una riga del foglio: una tessera, non necessariamente un atleta distinto. */
    private record Tessera(String nome, String cognome, LocalDate dataNascita,
                           String codiceFiscale, String email, String telefono, String indirizzo,
                           String citta, LocalDate emissione, String codiceTessera,
                           String tipoTessera, String cintura) {
    }

    /** Le tessere di un atleta ridotte a una anagrafica sola. */
    private record Anagrafica(Tessera principale, LocalDate primaEmissione) {
    }
}
