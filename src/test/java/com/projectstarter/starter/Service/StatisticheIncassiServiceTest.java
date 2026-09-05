package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Response.StatisticheIncassiResponse;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Entity.Corso;
import com.projectstarter.starter.Entity.Pagamento;
import com.projectstarter.starter.Repository.CorsoRepository;
import com.projectstarter.starter.Repository.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatisticheIncassiServiceTest {

    @Mock private CorsoRepository corsoRepository;
    @Mock private PagamentoRepository pagamentoRepository;

    @InjectMocks private StatisticheIncassiService service;

    private static final String STAGIONE = "2025/2026";

    private Corso karate;
    private Atleta rossi;
    private Atleta bianchi;

    @BeforeEach
    void setUp() {
        karate = corso(1L, "Karate");
        rossi = atleta(10L, "Mario", "Rossi");
        bianchi = atleta(11L, "Anna", "Bianchi");
    }

    @Test
    @DisplayName("Atteso, incassato e residuo si ricavano dalla data di pagamento")
    void totaliIncassi() {
        when(pagamentoRepository.findPerStatistiche(STAGIONE)).thenReturn(List.of(
                pagamento(1L, rossi, null, Pagamento.Tipo.TESSERA, 30.0, LocalDate.of(2025, 9, 20), Pagamento.Metodo.CONTANTI),
                pagamento(2L, rossi, karate, Pagamento.Tipo.RATA_1, 150.0, LocalDate.of(2025, 10, 5), Pagamento.Metodo.BONIFICO),
                pagamento(3L, rossi, karate, Pagamento.Tipo.RATA_2, 150.0, null, null),
                pagamento(4L, bianchi, karate, Pagamento.Tipo.RATA_1, 150.0, null, null)
        ));

        StatisticheIncassiResponse stats = service.calcola(null, STAGIONE);

        assertThat(stats.getStagione()).isEqualTo(STAGIONE);
        assertThat(stats.getTotaleAtteso()).isEqualTo(480.0);
        assertThat(stats.getTotaleIncassato()).isEqualTo(180.0);
        assertThat(stats.getTotaleResiduo()).isEqualTo(300.0);
        assertThat(stats.getPercentualeIncasso()).isEqualTo(37.5);
        assertThat(stats.getPagamentiTotali()).isEqualTo(4);
        assertThat(stats.getPagamentiSaldati()).isEqualTo(2);
        assertThat(stats.getPagamentiInSospeso()).isEqualTo(2);
        assertThat(stats.getAtletiCoinvolti()).isEqualTo(2);
        assertThat(stats.getAtletiMorosi()).isEqualTo(2);
    }

    @Test
    @DisplayName("Le tessere finiscono in una riga a parte, non attribuita a un corso")
    void tessereFuoriDaiCorsi() {
        when(pagamentoRepository.findPerStatistiche(STAGIONE)).thenReturn(List.of(
                pagamento(1L, rossi, null, Pagamento.Tipo.TESSERA, 30.0, LocalDate.of(2025, 9, 20), Pagamento.Metodo.CONTANTI),
                pagamento(2L, rossi, karate, Pagamento.Tipo.RATA_1, 150.0, LocalDate.of(2025, 10, 5), Pagamento.Metodo.BONIFICO)
        ));

        StatisticheIncassiResponse stats = service.calcola(null, STAGIONE);

        assertThat(stats.getPerCorso()).hasSize(2);
        StatisticheIncassiResponse.RigaCorso tessere = stats.getPerCorso().stream()
                .filter(r -> r.getCorsoId() == null).findFirst().orElseThrow();
        assertThat(tessere.getCorsoNome()).isEqualTo("Tessere associative");
        assertThat(tessere.getIncassato()).isEqualTo(30.0);
        // ordinamento per incassato decrescente: il corso viene prima delle tessere
        assertThat(stats.getPerCorso().get(0).getCorsoNome()).isEqualTo("Karate");
    }

    @Test
    @DisplayName("Un incasso senza metodo confluisce in NON_SPECIFICATO")
    void metodoMancante() {
        when(pagamentoRepository.findPerStatistiche(STAGIONE)).thenReturn(List.of(
                pagamento(1L, rossi, karate, Pagamento.Tipo.RATA_1, 100.0, LocalDate.of(2025, 10, 5), null),
                pagamento(2L, bianchi, karate, Pagamento.Tipo.RATA_1, 50.0, LocalDate.of(2025, 10, 6), Pagamento.Metodo.CONTANTI),
                pagamento(3L, bianchi, karate, Pagamento.Tipo.RATA_2, 999.0, null, Pagamento.Metodo.CONTANTI)
        ));

        StatisticheIncassiResponse stats = service.calcola(null, STAGIONE);

        assertThat(stats.getPerMetodo()).hasSize(2);
        // ordinati per incassato decrescente e calcolati solo sulle voci saldate
        assertThat(stats.getPerMetodo().get(0).getMetodo()).isEqualTo("NON_SPECIFICATO");
        assertThat(stats.getPerMetodo().get(0).getIncassato()).isEqualTo(100.0);
        assertThat(stats.getPerMetodo().get(1).getMetodo()).isEqualTo("CONTANTI");
        assertThat(stats.getPerMetodo().get(1).getIncassato()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Il dettaglio per tipo salta le quote mai registrate")
    void dettaglioPerTipo() {
        when(pagamentoRepository.findPerStatistiche(STAGIONE)).thenReturn(List.of(
                pagamento(1L, rossi, karate, Pagamento.Tipo.RATA_1, 150.0, LocalDate.of(2025, 10, 5), Pagamento.Metodo.BONIFICO),
                pagamento(2L, bianchi, karate, Pagamento.Tipo.RATA_1, 150.0, null, null)
        ));

        StatisticheIncassiResponse stats = service.calcola(null, STAGIONE);

        assertThat(stats.getPerTipo()).hasSize(1);
        StatisticheIncassiResponse.RigaTipo rata = stats.getPerTipo().get(0);
        assertThat(rata.getTipo()).isEqualTo(Pagamento.Tipo.RATA_1);
        assertThat(rata.getAtteso()).isEqualTo(300.0);
        assertThat(rata.getIncassato()).isEqualTo(150.0);
        assertThat(rata.getResiduo()).isEqualTo(150.0);
        assertThat(rata.getConteggio()).isEqualTo(2);
        assertThat(rata.getSaldati()).isEqualTo(1);
    }

    @Test
    @DisplayName("L'andamento mensile degli incassi usa la data di pagamento")
    void andamentoIncassi() {
        when(pagamentoRepository.findPerStatistiche(STAGIONE)).thenReturn(List.of(
                pagamento(1L, rossi, karate, Pagamento.Tipo.RATA_1, 150.0, LocalDate.of(2025, 10, 5), Pagamento.Metodo.CONTANTI),
                pagamento(2L, bianchi, karate, Pagamento.Tipo.RATA_1, 50.0, LocalDate.of(2025, 10, 20), Pagamento.Metodo.CONTANTI),
                pagamento(3L, bianchi, karate, Pagamento.Tipo.RATA_2, 90.0, null, null)
        ));

        StatisticheIncassiResponse stats = service.calcola(null, STAGIONE);

        assertThat(stats.getAndamentoMensile()).hasSize(12);
        StatisticheIncassiResponse.PuntoMensile ottobre = stats.getAndamentoMensile().stream()
                .filter(p -> p.getMese().equals("2025-10")).findFirst().orElseThrow();
        assertThat(ottobre.getIncassato()).isEqualTo(200.0);
        assertThat(ottobre.getConteggio()).isEqualTo(2);
    }

    @Test
    @DisplayName("Un incasso datato fuori stagione compare comunque nell'andamento")
    void incassoFuoriStagione() {
        when(pagamentoRepository.findPerStatistiche(STAGIONE)).thenReturn(List.of(
                pagamento(1L, rossi, karate, Pagamento.Tipo.RATA_1, 100.0, LocalDate.of(2026, 9, 3), Pagamento.Metodo.CONTANTI)
        ));

        StatisticheIncassiResponse stats = service.calcola(null, STAGIONE);

        assertThat(stats.getAndamentoMensile()).hasSize(13);
        assertThat(stats.getAndamentoMensile().get(12).getMese()).isEqualTo("2026-09");
        assertThat(stats.getAndamentoMensile().get(12).getIncassato()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Le quote in sospeso elencano solo importi ancora da incassare")
    void quoteInSospeso() {
        when(pagamentoRepository.findPerStatistiche(STAGIONE)).thenReturn(List.of(
                pagamento(1L, rossi, karate, Pagamento.Tipo.RATA_1, 150.0, LocalDate.of(2025, 10, 5), Pagamento.Metodo.CONTANTI),
                pagamento(2L, bianchi, karate, Pagamento.Tipo.RATA_2, 150.0, null, null),
                // importo a zero: nulla da incassare, quindi fuori dall'elenco
                pagamento(3L, rossi, karate, Pagamento.Tipo.ALTRO, 0.0, null, null)
        ));

        StatisticheIncassiResponse stats = service.calcola(null, STAGIONE);

        assertThat(stats.getSospesi()).hasSize(1);
        assertThat(stats.getSospesi().get(0).getCognome()).isEqualTo("Bianchi");
        assertThat(stats.getSospesi().get(0).getCorsoNome()).isEqualTo("Karate");
        assertThat(stats.getSospesi().get(0).getImporto()).isEqualTo(150.0);
        assertThat(stats.getAtletiMorosi()).isEqualTo(1);
    }

    @Test
    @DisplayName("Filtrando per corso si interroga la query che esclude le tessere")
    void incassiFiltratiPerCorso() {
        when(corsoRepository.findById(1L)).thenReturn(Optional.of(karate));
        when(pagamentoRepository.findPerStatistiche(STAGIONE, 1L)).thenReturn(List.of(
                pagamento(2L, rossi, karate, Pagamento.Tipo.RATA_1, 150.0, LocalDate.of(2025, 10, 5), Pagamento.Metodo.BONIFICO)
        ));

        StatisticheIncassiResponse stats = service.calcola(1L, STAGIONE);

        assertThat(stats.getCorsoId()).isEqualTo(1L);
        assertThat(stats.getCorsoNome()).isEqualTo("Karate");
        assertThat(stats.getTotaleIncassato()).isEqualTo(150.0);
        assertThat(stats.getPerCorso()).hasSize(1);
    }

    @Test
    @DisplayName("Una stagione malformata viene rifiutata")
    void stagioneNonValida() {
        assertThatThrownBy(() -> service.calcola(null, "2025-2026"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stagione non valida");
    }

    @Test
    @DisplayName("Senza pagamenti i totali restano a zero senza percentuale")
    void nessunPagamento() {
        when(pagamentoRepository.findPerStatistiche(STAGIONE)).thenReturn(List.of());

        StatisticheIncassiResponse stats = service.calcola(null, STAGIONE);

        assertThat(stats.getTotaleAtteso()).isZero();
        assertThat(stats.getTotaleIncassato()).isZero();
        assertThat(stats.getPercentualeIncasso()).isNull();
        assertThat(stats.getPerTipo()).isEmpty();
        assertThat(stats.getSospesi()).isEmpty();
        assertThat(stats.getAndamentoMensile()).hasSize(12);
    }

    // ---------- Fixtures ----------

    private Corso corso(Long id, String nome) {
        Corso corso = new Corso();
        corso.setId(id);
        corso.setNome(nome);
        return corso;
    }

    private Atleta atleta(Long id, String nome, String cognome) {
        Atleta atleta = new Atleta();
        atleta.setId(id);
        atleta.setNome(nome);
        atleta.setCognome(cognome);
        return atleta;
    }

    private Pagamento pagamento(Long id, Atleta atleta, Corso corso, Pagamento.Tipo tipo,
                                Double importo, LocalDate dataPagamento, Pagamento.Metodo metodo) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(id);
        pagamento.setAtleta(atleta);
        pagamento.setCorso(corso);
        pagamento.setTipo(tipo);
        pagamento.setStagione(STAGIONE);
        pagamento.setImporto(importo);
        pagamento.setDataPagamento(dataPagamento);
        pagamento.setMetodo(metodo);
        return pagamento;
    }
}
