package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Response.FoglioPresenzeResponse;
import com.projectstarter.starter.Dto.Response.ProspettoPagamentiResponse;
import com.projectstarter.starter.Dto.Response.ProspettoPagamentiResponse.StatoCella;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Entity.Corso;
import com.projectstarter.starter.Entity.CorsoIscrizione;
import com.projectstarter.starter.Entity.Lezione;
import com.projectstarter.starter.Entity.Pagamento;
import com.projectstarter.starter.Entity.Presenza;
import com.projectstarter.starter.Repository.AtletaRepository;
import com.projectstarter.starter.Repository.CorsoIscrizioneRepository;
import com.projectstarter.starter.Repository.CorsoRepository;
import com.projectstarter.starter.Repository.LezioneRepository;
import com.projectstarter.starter.Repository.PagamentoRepository;
import com.projectstarter.starter.Repository.PresenzaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prospetto pagamenti e foglio presenze arrivano al client gia impaginati:
 * questi test fissano la forma su cui il client fa affidamento, cioe le celle
 * associate alle righe e i totali calcolati dal server.
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:impaginazione;DB_CLOSE_DELAY=-1")
class ImpaginazioneServiceTest {

    @Autowired private PagamentiService pagamentiService;
    @Autowired private PresenzeService presenzeService;
    @Autowired private AtletaRepository atletaRepository;
    @Autowired private CorsoRepository corsoRepository;
    @Autowired private CorsoIscrizioneRepository iscrizioneRepository;
    @Autowired private LezioneRepository lezioneRepository;
    @Autowired private PresenzaRepository presenzaRepository;
    @Autowired private PagamentoRepository pagamentoRepository;

    private static final String STAGIONE = "2025/2026";
    /** Un mese dentro la stagione, cosi le due viste guardano lo stesso periodo. */
    private static final YearMonth MESE = YearMonth.of(2025, 10);

    private Corso karate;
    private Atleta rossi;
    private Atleta bianchi;

    @BeforeEach
    void setUp() {
        karate = new Corso();
        karate.setNome("Karate");
        karate.setAttivo(true);
        karate.setQuotaTessera(30d);
        karate.setQuotaRata(120d);
        karate = corsoRepository.save(karate);

        rossi = salvaAtleta("Mario", "Rossi");
        bianchi = salvaAtleta("Anna", "Bianchi");
        iscrivi(rossi);
        iscrivi(bianchi);
    }

    @Test
    @DisplayName("prospetto: una cella per colonna, con stato e quota suggerita")
    void prospettoImpaginato() {
        // tessera saldata, prima rata registrata ma non ancora incassata
        salvaPagamento(rossi, Pagamento.Tipo.TESSERA, null, 30d, LocalDate.of(2025, 10, 5));
        salvaPagamento(rossi, Pagamento.Tipo.RATA_1, karate, 100d, null);

        ProspettoPagamentiResponse prospetto = pagamentiService.prospetto(karate.getId(), STAGIONE);

        assertThat(prospetto.getColonne()).extracting(ProspettoPagamentiResponse.Colonna::getTipo)
                .containsExactly(Pagamento.Tipo.TESSERA, Pagamento.Tipo.RATA_1, Pagamento.Tipo.RATA_2);
        assertThat(prospetto.getColonne().get(0).getImportoSuggerito()).isEqualTo(30d);
        assertThat(prospetto.getColonne().get(0).isAnnuale()).isTrue();
        assertThat(prospetto.getColonne().get(1).getImportoSuggerito()).isEqualTo(120d);
        assertThat(prospetto.isQuoteImpostate()).isTrue();

        // le righe escono ordinate per cognome: Bianchi prima di Rossi
        ProspettoPagamentiResponse.Riga riga = prospetto.getAtleti().get(1);
        assertThat(riga.getNominativo()).isEqualTo("Rossi Mario");
        assertThat(riga.getCelle()).extracting(ProspettoPagamentiResponse.Cella::getStato)
                .containsExactly(StatoCella.SALDATA, StatoCella.DA_SALDARE, StatoCella.VUOTA);
        // il totale della riga conta solo quello che e stato incassato
        assertThat(riga.getTotale()).isEqualTo(30d);
        // la rata registrata si discosta dalla quota del corso
        assertThat(riga.getCelle().get(1).isImportoDiversoDaQuota()).isTrue();
        // una cella vuota porta comunque la quota con cui precompilarla
        assertThat(riga.getCelle().get(2).getImportoSuggerito()).isEqualTo(120d);

        ProspettoPagamentiResponse.Riepilogo riepilogo = prospetto.getRiepilogo();
        assertThat(riepilogo.getIncassato()).isEqualTo(30d);
        assertThat(riepilogo.getAtteso()).isEqualTo(130d);
        assertThat(riepilogo.getDaIncassare()).isEqualTo(100d);
        assertThat(riepilogo.getCelleTotali()).isEqualTo(6);
        assertThat(riepilogo.getCelleCompilate()).isEqualTo(2);
        assertThat(riepilogo.getCelleSaldate()).isEqualTo(1);
    }

    @Test
    @DisplayName("foglio: celle indicizzate per lezione e totali gia calcolati")
    void foglioImpaginato() {
        Lezione lunedi = salvaLezione(LocalDate.of(2025, 10, 6));
        Lezione mercoledi = salvaLezione(LocalDate.of(2025, 10, 8));

        salvaPresenza(lunedi, rossi, Presenza.Stato.PRESENTE);
        salvaPresenza(lunedi, bianchi, Presenza.Stato.ASSENTE);
        salvaPresenza(mercoledi, rossi, Presenza.Stato.PRESENTE);

        FoglioPresenzeResponse foglio = presenzeService.foglio(karate.getId(), MESE.toString(), null, null);

        assertThat(foglio.getMese()).isEqualTo("2025-10");
        assertThat(foglio.getMeseLabel()).isEqualTo("Ottobre 2025");
        assertThat(foglio.getMesePrecedente()).isEqualTo("2025-09");
        assertThat(foglio.getMeseSuccessivo()).isEqualTo("2025-11");
        assertThat(foglio.getFrom()).isEqualTo(LocalDate.of(2025, 10, 1));
        assertThat(foglio.getTo()).isEqualTo(LocalDate.of(2025, 10, 31));

        // le intestazioni di colonna nascono dalla data memorizzata, non dal client
        assertThat(foglio.getLezioni()).hasSize(2);
        FoglioPresenzeResponse.Colonna prima = foglio.getLezioni().get(0);
        assertThat(prima.getLezione().getGiornoBreve()).isEqualTo("Lun");
        assertThat(prima.getLezione().getDataBreve()).isEqualTo("06/10");
        assertThat(prima.getLezione().getOraLabel()).isEqualTo("18:30");
        assertThat(prima.getPresenti()).isEqualTo(1);
        assertThat(prima.getAssenti()).isEqualTo(1);
        assertThat(prima.getCompilate()).isEqualTo(2);

        FoglioPresenzeResponse.Riga rigaRossi = foglio.getAtleti().get(1);
        assertThat(rigaRossi.getNominativo()).isEqualTo("Rossi Mario");
        assertThat(rigaRossi.getCelle()).containsOnlyKeys(lunedi.getId(), mercoledi.getId());
        assertThat(rigaRossi.getCelle().get(lunedi.getId())).isEqualTo(Presenza.Stato.PRESENTE);
        assertThat(rigaRossi.getPresenti()).isEqualTo(2);
        assertThat(rigaRossi.getAssenti()).isZero();

        FoglioPresenzeResponse.Totali totali = foglio.getTotali();
        assertThat(totali.getLezioni()).isEqualTo(2);
        assertThat(totali.getAtleti()).isEqualTo(2);
        assertThat(totali.getCelleTotali()).isEqualTo(4);
        assertThat(totali.getCelleCompilate()).isEqualTo(3);
        assertThat(totali.getPresenti()).isEqualTo(2);
        assertThat(totali.getAssenti()).isEqualTo(1);
    }

    // ---------- Dati di appoggio ----------

    private Atleta salvaAtleta(String nome, String cognome) {
        Atleta atleta = new Atleta();
        atleta.setNome(nome);
        atleta.setCognome(cognome);
        atleta.setAttivo(true);
        return atletaRepository.save(atleta);
    }

    private void iscrivi(Atleta atleta) {
        CorsoIscrizione iscrizione = new CorsoIscrizione();
        iscrizione.setCorso(karate);
        iscrizione.setAtleta(atleta);
        iscrizione.setAttivo(true);
        iscrizione.setDataIscrizione(LocalDate.of(2025, 9, 1));
        iscrizioneRepository.save(iscrizione);
    }

    private void salvaPagamento(Atleta atleta, Pagamento.Tipo tipo, Corso corso, Double importo, LocalDate pagatoIl) {
        Pagamento pagamento = new Pagamento();
        pagamento.setAtleta(atleta);
        pagamento.setCorso(corso);
        pagamento.setTipo(tipo);
        pagamento.setStagione(STAGIONE);
        pagamento.setImporto(importo);
        pagamento.setDataPagamento(pagatoIl);
        pagamentoRepository.save(pagamento);
    }

    private Lezione salvaLezione(LocalDate data) {
        Lezione lezione = new Lezione();
        lezione.setCorso(karate);
        lezione.setData(data);
        lezione.setOraInizio(LocalTime.of(18, 30));
        lezione.setOraFine(LocalTime.of(20, 0));
        lezione.setAnnullata(false);
        return lezioneRepository.save(lezione);
    }

    private void salvaPresenza(Lezione lezione, Atleta atleta, Presenza.Stato stato) {
        Presenza presenza = new Presenza();
        presenza.setLezione(lezione);
        presenza.setAtleta(atleta);
        presenza.setStato(stato);
        presenzaRepository.save(presenza);
    }
}
