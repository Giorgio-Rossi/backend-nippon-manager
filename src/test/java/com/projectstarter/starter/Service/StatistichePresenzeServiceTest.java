package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Response.StatistichePresenzeResponse;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Entity.Corso;
import com.projectstarter.starter.Entity.Lezione;
import com.projectstarter.starter.Entity.Presenza;
import com.projectstarter.starter.Repository.CorsoIscrizioneRepository;
import com.projectstarter.starter.Repository.CorsoIscrizioneRepository.ConteggioIscritti;
import com.projectstarter.starter.Repository.CorsoRepository;
import com.projectstarter.starter.Repository.LezioneRepository;
import com.projectstarter.starter.Repository.PresenzaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatistichePresenzeServiceTest {

    @Mock private CorsoRepository corsoRepository;
    @Mock private CorsoIscrizioneRepository iscrizioneRepository;
    @Mock private LezioneRepository lezioneRepository;
    @Mock private PresenzaRepository presenzaRepository;

    @InjectMocks private StatistichePresenzeService service;

    private static final String STAGIONE = "2025/2026";

    private Corso karate;
    private Corso judo;
    private Atleta rossi;
    private Atleta bianchi;

    @BeforeEach
    void setUp() {
        karate = corso(1L, "Karate");
        judo = corso(2L, "Judo");
        rossi = atleta(10L, "Mario", "Rossi");
        bianchi = atleta(11L, "Anna", "Bianchi");

        when(iscrizioneRepository.countIscrittiAttiviPerCorso())
                .thenReturn(List.of(new Iscritti(1L, 4L), new Iscritti(2L, 2L)));
    }

    @Test
    @DisplayName("Le lezioni annullate e le loro presenze restano fuori dai conteggi")
    void escludeLezioniAnnullate() {
        Lezione svolta = lezione(100L, karate, LocalDate.of(2025, 9, 8), false);
        Lezione annullata = lezione(101L, karate, LocalDate.of(2025, 9, 15), true);

        when(lezioneRepository.findPerStatistiche(any(), any())).thenReturn(List.of(svolta, annullata));
        when(presenzaRepository.findPerStatistiche(any(), any())).thenReturn(List.of(
                presenza(svolta, rossi, Presenza.Stato.PRESENTE),
                presenza(svolta, bianchi, Presenza.Stato.ASSENTE),
                presenza(annullata, rossi, Presenza.Stato.PRESENTE)
        ));

        StatistichePresenzeResponse stats = service.calcola(null, STAGIONE, null, null);

        assertThat(stats.getLezioniSvolte()).isEqualTo(1);
        assertThat(stats.getLezioniAnnullate()).isEqualTo(1);
        assertThat(stats.getRegistrazioni()).isEqualTo(2);
        assertThat(stats.getPresenti()).isEqualTo(1);
        assertThat(stats.getAssenti()).isEqualTo(1);
        assertThat(stats.getTassoPresenza()).isEqualTo(50.0);
        assertThat(stats.getMediaPresentiPerLezione()).isEqualTo(1.0);
        assertThat(stats.getAtletiCoinvolti()).isEqualTo(2);
    }

    @Test
    @DisplayName("Il periodo predefinito e l'intera stagione e copre tutti i suoi mesi")
    void andamentoMensileCopreLaStagione() {
        Lezione settembre = lezione(100L, karate, LocalDate.of(2025, 9, 8), false);
        Lezione ottobre = lezione(101L, karate, LocalDate.of(2025, 10, 6), false);

        when(lezioneRepository.findPerStatistiche(any(), any())).thenReturn(List.of(settembre, ottobre));
        when(presenzaRepository.findPerStatistiche(any(), any())).thenReturn(List.of(
                presenza(settembre, rossi, Presenza.Stato.PRESENTE),
                presenza(settembre, bianchi, Presenza.Stato.PRESENTE),
                presenza(ottobre, rossi, Presenza.Stato.ASSENTE)
        ));

        StatistichePresenzeResponse stats = service.calcola(null, STAGIONE, null, null);

        assertThat(stats.getFrom()).isEqualTo(LocalDate.of(2025, 9, 1));
        assertThat(stats.getTo()).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(stats.getAndamentoMensile()).hasSize(12);
        assertThat(stats.getAndamentoMensile().get(0).getMese()).isEqualTo("2025-09");
        assertThat(stats.getAndamentoMensile().get(0).getPresenti()).isEqualTo(2);
        assertThat(stats.getAndamentoMensile().get(0).getTassoPresenza()).isEqualTo(100.0);
        assertThat(stats.getAndamentoMensile().get(1).getMese()).isEqualTo("2025-10");
        assertThat(stats.getAndamentoMensile().get(1).getTassoPresenza()).isEqualTo(0.0);
        // un mese senza registrazioni non ha un tasso: resta nullo, non zero
        assertThat(stats.getAndamentoMensile().get(2).getTassoPresenza()).isNull();
    }

    @Test
    @DisplayName("Il dettaglio per corso porta gli iscritti attivi e la media per lezione")
    void dettaglioPerCorso() {
        Lezione lezioneKarate = lezione(100L, karate, LocalDate.of(2025, 9, 8), false);
        Lezione lezioneJudo = lezione(200L, judo, LocalDate.of(2025, 9, 9), false);

        when(lezioneRepository.findPerStatistiche(any(), any()))
                .thenReturn(List.of(lezioneKarate, lezioneJudo));
        when(presenzaRepository.findPerStatistiche(any(), any())).thenReturn(List.of(
                presenza(lezioneKarate, rossi, Presenza.Stato.PRESENTE),
                presenza(lezioneKarate, bianchi, Presenza.Stato.PRESENTE),
                presenza(lezioneJudo, rossi, Presenza.Stato.ASSENTE)
        ));

        StatistichePresenzeResponse stats = service.calcola(null, STAGIONE, null, null);

        assertThat(stats.getPerCorso()).hasSize(2);
        StatistichePresenzeResponse.RigaCorso judoRiga = stats.getPerCorso().stream()
                .filter(r -> r.getCorsoId().equals(2L)).findFirst().orElseThrow();
        StatistichePresenzeResponse.RigaCorso karateRiga = stats.getPerCorso().stream()
                .filter(r -> r.getCorsoId().equals(1L)).findFirst().orElseThrow();

        assertThat(karateRiga.getIscrittiAttivi()).isEqualTo(4);
        assertThat(karateRiga.getPresenti()).isEqualTo(2);
        assertThat(karateRiga.getMediaPresentiPerLezione()).isEqualTo(2.0);
        assertThat(karateRiga.getTassoPresenza()).isEqualTo(100.0);
        assertThat(judoRiga.getIscrittiAttivi()).isEqualTo(2);
        assertThat(judoRiga.getTassoPresenza()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("La classifica atleti parte dal tasso di presenza piu alto")
    void classificaAtleti() {
        Lezione l1 = lezione(100L, karate, LocalDate.of(2025, 9, 8), false);
        Lezione l2 = lezione(101L, karate, LocalDate.of(2025, 9, 15), false);

        when(lezioneRepository.findPerStatistiche(any(), any())).thenReturn(List.of(l1, l2));
        when(presenzaRepository.findPerStatistiche(any(), any())).thenReturn(List.of(
                presenza(l1, rossi, Presenza.Stato.PRESENTE),
                presenza(l2, rossi, Presenza.Stato.PRESENTE),
                presenza(l1, bianchi, Presenza.Stato.PRESENTE),
                presenza(l2, bianchi, Presenza.Stato.ASSENTE)
        ));

        StatistichePresenzeResponse stats = service.calcola(null, STAGIONE, null, null);

        assertThat(stats.getPerAtleta()).hasSize(2);
        assertThat(stats.getPerAtleta().get(0).getCognome()).isEqualTo("Rossi");
        assertThat(stats.getPerAtleta().get(0).getTassoPresenza()).isEqualTo(100.0);
        assertThat(stats.getPerAtleta().get(1).getCognome()).isEqualTo("Bianchi");
        assertThat(stats.getPerAtleta().get(1).getTassoPresenza()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Con from/to indicati vince il periodo esplicito")
    void periodoEsplicito() {
        when(lezioneRepository.findPerStatistiche(any(), any(), anyLong())).thenReturn(List.of());
        when(presenzaRepository.findPerStatistiche(any(), any(), anyLong())).thenReturn(List.of());
        when(corsoRepository.findById(1L)).thenReturn(Optional.of(karate));

        StatistichePresenzeResponse stats = service.calcola(
                1L, STAGIONE, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31));

        assertThat(stats.getFrom()).isEqualTo(LocalDate.of(2025, 10, 1));
        assertThat(stats.getTo()).isEqualTo(LocalDate.of(2025, 10, 31));
        assertThat(stats.getCorsoNome()).isEqualTo("Karate");
        assertThat(stats.getTassoPresenza()).isNull();
        assertThat(stats.getAndamentoMensile()).hasSize(1);
    }

    @Test
    @DisplayName("Indicando solo il from la fine resta quella della stagione")
    void soloDataIniziale() {
        when(lezioneRepository.findPerStatistiche(any(), any())).thenReturn(List.of());
        when(presenzaRepository.findPerStatistiche(any(), any())).thenReturn(List.of());

        StatistichePresenzeResponse stats = service.calcola(null, STAGIONE, LocalDate.of(2026, 1, 1), null);

        assertThat(stats.getFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(stats.getTo()).isEqualTo(LocalDate.of(2026, 8, 31));
    }

    @Test
    @DisplayName("Un periodo rovesciato viene rifiutato")
    void periodoRovesciato() {
        assertThatThrownBy(() -> service.calcola(
                null, STAGIONE, LocalDate.of(2026, 1, 1), LocalDate.of(2025, 12, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("successiva");
    }

    @Test
    @DisplayName("Una stagione malformata viene rifiutata")
    void stagioneNonValida() {
        assertThatThrownBy(() -> service.calcola(null, "2025-2026", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stagione non valida");
    }

    // ---------- Fixtures ----------

    /** Stub della projection restituita dal repository. */
    private record Iscritti(Long corsoId, long totale) implements ConteggioIscritti {

        @Override
        public Long getCorsoId() {
            return corsoId;
        }

        @Override
        public long getTotale() {
            return totale;
        }
    }

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

    private Lezione lezione(Long id, Corso corso, LocalDate data, boolean annullata) {
        Lezione lezione = new Lezione();
        lezione.setId(id);
        lezione.setCorso(corso);
        lezione.setData(data);
        lezione.setAnnullata(annullata);
        return lezione;
    }

    private Presenza presenza(Lezione lezione, Atleta atleta, Presenza.Stato stato) {
        Presenza presenza = new Presenza();
        presenza.setLezione(lezione);
        presenza.setAtleta(atleta);
        presenza.setStato(stato);
        return presenza;
    }
}
