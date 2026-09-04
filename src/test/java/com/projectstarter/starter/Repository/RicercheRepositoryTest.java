package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Entity.Corso;
import com.projectstarter.starter.Entity.CorsoIscrizione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Le query di ricerca usano parametri opzionali ({@code :attivo IS NULL}): qui
 * si verifica che vengano davvero eseguite dal database, non solo che il JPQL
 * sia sintatticamente valido.
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:ricerche;DB_CLOSE_DELAY=-1")
class RicercheRepositoryTest {

    @Autowired private AtletaRepository atletaRepository;
    @Autowired private CorsoRepository corsoRepository;
    @Autowired private CorsoIscrizioneRepository iscrizioneRepository;

    private Atleta rossi;
    private Atleta bianchi;
    private Corso karate;

    @BeforeEach
    void setUp() {
        rossi = atletaRepository.save(atleta("Mario", "Rossi", "RSSMRA80A01H501U", true, LocalDate.now().plusDays(10)));
        bianchi = atletaRepository.save(atleta("Anna", "Bianchi", "BNCNNA85B41H501X", false, null));
        karate = corsoRepository.save(corso("Karate avanzato", "Palestra Centrale", true));
        corsoRepository.save(corso("Judo principianti", "Palestra Nord", false));
    }

    @Test
    @DisplayName("atleti: senza filtri torna tutto, ordinato per cognome")
    void atletiSenzaFiltri() {
        List<Atleta> atleti = atletaRepository.cerca(null, null);
        assertThat(atleti).extracting(Atleta::getCognome).containsExactly("Bianchi", "Rossi");
    }

    @Test
    @DisplayName("atleti: il filtro sullo stato e quello testuale si combinano")
    void atletiFiltrati() {
        assertThat(atletaRepository.cerca(true, null)).containsExactly(rossi);
        assertThat(atletaRepository.cerca(false, null)).containsExactly(bianchi);
        assertThat(atletaRepository.cerca(null, "ross")).containsExactly(rossi);
        // il nominativo e cercabile in entrambi i versi, e per codice fiscale
        assertThat(atletaRepository.cerca(null, "anna bianchi")).containsExactly(bianchi);
        assertThat(atletaRepository.cerca(null, "rssmra")).containsExactly(rossi);
        assertThat(atletaRepository.cerca(true, "bianchi")).isEmpty();
    }

    @Test
    @DisplayName("corsi: filtro su stato, nome e luogo")
    void corsiFiltrati() {
        assertThat(corsoRepository.cerca(null, null)).hasSize(2);
        assertThat(corsoRepository.cerca(true, null)).extracting(Corso::getNome).containsExactly("Karate avanzato");
        assertThat(corsoRepository.cerca(null, "judo")).extracting(Corso::getNome).containsExactly("Judo principianti");
        assertThat(corsoRepository.cerca(null, "nord")).extracting(Corso::getNome).containsExactly("Judo principianti");
    }

    @Test
    @DisplayName("iscrivibili: esclude chi e gia iscritto e chi non e attivo")
    void iscrivibili() {
        assertThat(atletaRepository.findIscrivibili(karate.getId(), null)).containsExactly(rossi);

        iscrizioneRepository.save(iscrizione(karate, rossi));
        assertThat(atletaRepository.findIscrivibili(karate.getId(), null)).isEmpty();
    }

    @Test
    @DisplayName("certificati: solo chi ha una scadenza entro la soglia")
    void certificatiInScadenza() {
        LocalDate entro = LocalDate.now().plusDays(30);
        assertThat(atletaRepository.findCertificatiInScadenza(entro, false)).containsExactly(rossi);
        assertThat(atletaRepository.findCertificatiInScadenza(LocalDate.now(), false)).isEmpty();
    }

    private Atleta atleta(String nome, String cognome, String cf, boolean attivo, LocalDate scadenza) {
        Atleta atleta = new Atleta();
        atleta.setNome(nome);
        atleta.setCognome(cognome);
        atleta.setCodiceFiscale(cf);
        atleta.setAttivo(attivo);
        atleta.setDataScadenzaCertificato(scadenza);
        return atleta;
    }

    private Corso corso(String nome, String luogo, boolean attivo) {
        Corso corso = new Corso();
        corso.setNome(nome);
        corso.setLuogo(luogo);
        corso.setAttivo(attivo);
        return corso;
    }

    private CorsoIscrizione iscrizione(Corso corso, Atleta atleta) {
        CorsoIscrizione iscrizione = new CorsoIscrizione();
        iscrizione.setCorso(corso);
        iscrizione.setAtleta(atleta);
        iscrizione.setAttivo(true);
        iscrizione.setDataIscrizione(LocalDate.now());
        return iscrizione;
    }
}
