package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Response.ImportAtletiResponse;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Repository.AtletaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * L'import viene verificato sull'export reale del gestionale: 57 tessere che
 * corrispondono a 50 atleti, gli stessi valorizzati dalla migrazione 008.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportAtletiServiceTest {

    @Mock private AtletaRepository atletaRepository;

    @InjectMocks private ImportAtletiService service;

    /** L'archivio simulato: le anagrafiche che il repository restituisce e riceve. */
    private final Map<String, Atleta> archivio = new java.util.LinkedHashMap<>();

    private List<Atleta> salvati;

    @BeforeEach
    void setUp() {
        salvati = new ArrayList<>();
        when(atletaRepository.findByCodiceFiscaleIn(anyCollection())).thenAnswer(invocazione -> {
            Collection<String> codici = invocazione.getArgument(0);
            return codici.stream().map(archivio::get).filter(java.util.Objects::nonNull).toList();
        });
        when(atletaRepository.saveAll(any())).thenAnswer(invocazione -> {
            Iterable<Atleta> atleti = invocazione.getArgument(0);
            atleti.forEach(atleta -> {
                salvati.add(atleta);
                archivio.put(atleta.getCodiceFiscale(), atleta);
            });
            return salvati;
        });
    }

    @Test
    @DisplayName("Su archivio vuoto inserisce un atleta per codice fiscale, non uno per tessera")
    void inserisceUnAtletaPerCodiceFiscale() {
        ImportAtletiResponse esito = service.importa(export(), false);

        assertThat(esito.getRigheLette()).isEqualTo(57);
        assertThat(esito.getTesseratiUnici()).isEqualTo(50);
        assertThat(esito.getInseriti()).isEqualTo(50);
        assertThat(esito.getAggiornati()).isZero();
        assertThat(esito.getScartati()).isZero();
        assertThat(esito.getErrori()).isEmpty();
        assertThat(esito.isSimulazione()).isFalse();
        assertThat(salvati).hasSize(50);
    }

    @Test
    @DisplayName("I campi corrispondono a quelli della prima valorizzazione")
    void valorizzaGliStessiCampiDellaMigrazione() {
        service.importa(export(), false);

        Atleta claudia = archivio.get("BRNCLD08P41C816V");
        assertThat(claudia.getNome()).isEqualTo("Claudia");
        assertThat(claudia.getCognome()).isEqualTo("Bernabe'");
        assertThat(claudia.getDataNascita()).isEqualTo(LocalDate.of(2008, 9, 1));
        assertThat(claudia.getEmail()).isEqualTo("berna.claudia08@gmail.com");
        assertThat(claudia.getTelefono()).isNull();
        assertThat(claudia.getIndirizzo()).isEqualTo("VIA GARIBALDI");
        assertThat(claudia.getCitta()).isEqualTo("Codogno");
        assertThat(claudia.getDataIscrizione()).isEqualTo(LocalDate.of(2026, 1, 26));
        assertThat(claudia.getCintura()).isEqualTo("Nera (1° DAN)");
        assertThat(claudia.getNote())
                .isEqualTo("Tesseramento FIJLKAM 25/26 n. 572234 (Atleta - AG (Femminile))");
        assertThat(claudia.getAttivo()).isTrue();
    }

    @Test
    @DisplayName("Con due tessere prende la definitiva ma la data della prima emissione")
    void fondeLeTessereDelloStessoAtleta() {
        service.importa(export(), false);

        // Promozionale 1036698 emessa il 08/10/2025, poi "Atleta - PA" 1070465 del 16/01/2026.
        Atleta alessandro = archivio.get("BNVLSN17M14G535Z");
        assertThat(alessandro.getDataIscrizione()).isEqualTo(LocalDate.of(2025, 10, 8));
        assertThat(alessandro.getCintura()).isEqualTo("Bianca");
        assertThat(alessandro.getNote())
                .isEqualTo("Tesseramento FIJLKAM 25/26 n. 1070465 (Atleta - PA)");

        // Qui la promozionale e emessa dopo la definitiva: conta comunque la piu vecchia.
        Atleta ettore = archivio.get("CNTTTR20P01G535V");
        assertThat(ettore.getDataIscrizione()).isEqualTo(LocalDate.of(2026, 2, 23));
        assertThat(ettore.getNote())
                .isEqualTo("Tesseramento FIJLKAM 25/26 n. 1083615 (Atleta - PA)");
    }

    @Test
    @DisplayName("Ricaricare lo stesso file non modifica nulla")
    void importIdempotente() {
        service.importa(export(), false);
        salvati.clear();

        ImportAtletiResponse esito = service.importa(export(), false);

        assertThat(esito.getInseriti()).isZero();
        assertThat(esito.getAggiornati()).isZero();
        assertThat(esito.getInvariati()).isEqualTo(50);
        assertThat(salvati).isEmpty();
    }

    @Test
    @DisplayName("La simulazione conta le modifiche senza scrivere in archivio")
    void simulazioneNonScrive() {
        ImportAtletiResponse esito = service.importa(export(), true);

        assertThat(esito.isSimulazione()).isTrue();
        assertThat(esito.getInseriti()).isEqualTo(50);
        assertThat(esito.getMessaggio()).startsWith("Simulazione:");
        assertThat(archivio).isEmpty();
        verify(atletaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("La simulazione non tocca le entita gia gestite da JPA")
    void simulazioneNonMutaLeEntita() {
        Atleta esistente = inArchivio("BRNCLD08P41C816V", "Claudia", "Vecchio Cognome");

        ImportAtletiResponse esito = service.importa(export(), true);

        assertThat(esito.getAggiornati()).isEqualTo(1);
        assertThat(esistente.getCognome()).isEqualTo("Vecchio Cognome");
        verify(atletaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Aggiorna i record esistenti sulle colonne del foglio")
    void aggiornaGliEsistenti() {
        Atleta esistente = inArchivio("BRNCLD08P41C816V", "Claudia", "Cognome Sbagliato");
        esistente.setCintura("Gialla");

        ImportAtletiResponse esito = service.importa(export(), false);

        assertThat(esito.getInseriti()).isEqualTo(49);
        assertThat(esito.getAggiornati()).isEqualTo(1);
        assertThat(esistente.getCognome()).isEqualTo("Bernabe'");
        assertThat(esistente.getCintura()).isEqualTo("Nera (1° DAN)");
    }

    @Test
    @DisplayName("Una cella vuota non cancella il dato gia presente in archivio")
    void celleVuoteNonAzzerano() {
        // Nel foglio Claudia non ha telefono: quello inserito a mano deve restare.
        Atleta esistente = inArchivio("BRNCLD08P41C816V", "Claudia", "Bernabe'");
        esistente.setTelefono("3331234567");

        service.importa(export(), false);

        assertThat(esistente.getTelefono()).isEqualTo("3331234567");
    }

    @Test
    @DisplayName("I campi del certificato medico non vengono mai toccati")
    void nonToccaIlCertificato() {
        Atleta esistente = inArchivio("BRNCLD08P41C816V", "Claudia", "Bernabe'");
        esistente.setTipoCertificato("Agonistico");
        esistente.setDataScadenzaCertificato(LocalDate.of(2026, 6, 30));

        service.importa(export(), false);

        assertThat(esistente.getTipoCertificato()).isEqualTo("Agonistico");
        assertThat(esistente.getDataScadenzaCertificato()).isEqualTo(LocalDate.of(2026, 6, 30));
    }

    @Test
    @DisplayName("Le annotazioni scritte a mano sopravvivono all'import")
    void conservaLeNoteManuali() {
        Atleta esistente = inArchivio("BRNCLD08P41C816V", "Claudia", "Bernabe'");
        esistente.setNote("Tesseramento FIJLKAM 24/25 n. 111111 (Atleta)\nAllergica al lattice.");

        service.importa(export(), false);

        assertThat(esistente.getNote()).isEqualTo(
                "Tesseramento FIJLKAM 25/26 n. 572234 (Atleta - AG (Femminile))\nAllergica al lattice.");
    }

    @Test
    @DisplayName("Un atleta disattivato non viene riattivato dall'import")
    void nonRiattivaGliArchiviati() {
        Atleta esistente = inArchivio("BRNCLD08P41C816V", "Claudia", "Bernabe'");
        esistente.setAttivo(false);

        service.importa(export(), false);

        assertThat(esistente.getAttivo()).isFalse();
    }

    @Test
    @DisplayName("Un file che non e l'export dei tesserati viene rifiutato")
    void rifiutaUnFoglioSenzaIntestazioni() {
        MultipartFile estraneo = new MockMultipartFile(
                "file", "altro.xlsx", null, fogliettoSenzaIntestazioni());

        assertThatThrownBy(() -> service.importa(estraneo, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Codice Fiscale");
    }

    @Test
    @DisplayName("Un file vuoto o di formato sbagliato viene rifiutato")
    void rifiutaFileNonValidi() {
        assertThatThrownBy(() -> service.importa(
                new MockMultipartFile("file", "vuoto.xlsx", null, new byte[0]), false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nessun file");

        assertThatThrownBy(() -> service.importa(
                new MockMultipartFile("file", "note.txt", null, "ciao".getBytes()), false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Formato non supportato");
    }

    @Test
    @DisplayName("Le righe non valide vengono segnalate senza fermare le altre")
    void scartaLeRigheNonValide() {
        MultipartFile foglio = new MockMultipartFile("file", "parziale.xlsx", null, foglioConRigheRotte());

        ImportAtletiResponse esito = service.importa(foglio, false);

        assertThat(esito.getRigheLette()).isEqualTo(3);
        assertThat(esito.getInseriti()).isEqualTo(1);
        assertThat(esito.getScartati()).isEqualTo(2);
        assertThat(esito.getErrori()).extracting(ImportAtletiResponse.ErroreImport::getRiga)
                .containsExactly(3, 4);
        assertThat(esito.getErrori()).extracting(ImportAtletiResponse.ErroreImport::getMotivo)
                .anySatisfy(motivo -> assertThat(motivo).contains("codice fiscale non valido"))
                .anySatisfy(motivo -> assertThat(motivo).contains("nome o cognome mancante"));
        assertThat(archivio).containsOnlyKeys("RSSMRA10A01F205X");
    }

    private Atleta inArchivio(String codiceFiscale, String nome, String cognome) {
        Atleta atleta = new Atleta();
        atleta.setCodiceFiscale(codiceFiscale);
        atleta.setNome(nome);
        atleta.setCognome(cognome);
        atleta.setAttivo(true);
        archivio.put(codiceFiscale, atleta);
        return atleta;
    }

    /** L'export reale del gestionale, usato come riferimento dei test. */
    private MultipartFile export() {
        try (InputStream flusso = getClass().getResourceAsStream("/tesserati-25-26.xlsx")) {
            assertThat(flusso).as("fixture /tesserati-25-26.xlsx").isNotNull();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            flusso.transferTo(buffer);
            return new MockMultipartFile("file", "Tesserati anno 25-26.xlsx", null, buffer.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] fogliettoSenzaIntestazioni() {
        return cartella(foglio -> {
            org.apache.poi.ss.usermodel.Row riga = foglio.createRow(0);
            riga.createCell(0).setCellValue("Colonna A");
            riga.createCell(1).setCellValue("Colonna B");
            return null;
        });
    }

    /** Un foglio minimo con una riga buona e due da scartare. */
    private byte[] foglioConRigheRotte() {
        return cartella(foglio -> {
            String[] intestazioni = {"Nome", "Cognome", "Nato il", "Codice Fiscale"};
            org.apache.poi.ss.usermodel.Row intestazione = foglio.createRow(0);
            for (int indice = 0; indice < intestazioni.length; indice++) {
                intestazione.createCell(indice).setCellValue(intestazioni[indice]);
            }
            scrivi(foglio.createRow(1), "MARIO", "ROSSI", "01/01/2010", "RSSMRA10A01F205X");
            scrivi(foglio.createRow(2), "LUCA", "VERDI", "02/02/2011", "TROPPO-CORTO");
            scrivi(foglio.createRow(3), null, null, "03/03/2012", "BNCGNN12C03F205K");
            return null;
        });
    }

    private void scrivi(org.apache.poi.ss.usermodel.Row riga, String... valori) {
        for (int indice = 0; indice < valori.length; indice++) {
            if (valori[indice] != null) {
                riga.createCell(indice).setCellValue(valori[indice]);
            }
        }
    }

    private byte[] cartella(Function<org.apache.poi.ss.usermodel.Sheet, Void> riempi) {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook cartella =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            riempi.apply(cartella.createSheet("Foglio1"));
            cartella.write(buffer);
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
