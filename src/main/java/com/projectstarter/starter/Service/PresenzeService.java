package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Request.GeneraLezioniRequest;
import com.projectstarter.starter.Dto.Request.LezioneRequest;
import com.projectstarter.starter.Dto.Request.PresenzeBulkRequest;
import com.projectstarter.starter.Dto.Response.FoglioPresenzeResponse;
import com.projectstarter.starter.Dto.Response.LezioneResponse;
import com.projectstarter.starter.Dto.Response.PresenzaResponse;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Entity.Corso;
import com.projectstarter.starter.Entity.CorsoIscrizione;
import com.projectstarter.starter.Entity.CorsoOrario;
import com.projectstarter.starter.Entity.Lezione;
import com.projectstarter.starter.Entity.Presenza;
import com.projectstarter.starter.Repository.AtletaRepository;
import com.projectstarter.starter.Repository.CorsoIscrizioneRepository;
import com.projectstarter.starter.Repository.CorsoRepository;
import com.projectstarter.starter.Repository.LezioneRepository;
import com.projectstarter.starter.Repository.PresenzaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PresenzeService {

    private final CorsoRepository corsoRepository;
    private final CorsoIscrizioneRepository iscrizioneRepository;
    private final LezioneRepository lezioneRepository;
    private final PresenzaRepository presenzaRepository;
    private final AtletaRepository atletaRepository;

    private static final String CORSO_NOT_FOUND = "Corso non trovato con id: ";
    private static final String LEZIONE_NOT_FOUND = "Lezione non trovata con id: ";
    /** Limite di sicurezza sulla generazione massiva delle lezioni. */
    private static final long MAX_GIORNI_GENERAZIONE = 400;

    // ---------- Lezioni ----------

    public List<LezioneResponse> findLezioni(Long corsoId, LocalDate from, LocalDate to) {
        getCorso(corsoId);
        List<Lezione> lezioni = (from != null && to != null)
                ? lezioneRepository.findByCorsoIdAndDataBetweenOrderByDataAscOraInizioAsc(corsoId, from, to)
                : lezioneRepository.findByCorsoIdOrderByDataAscOraInizioAsc(corsoId);
        return lezioni.stream().map(LezioneResponse::from).toList();
    }

    /**
     * Crea le lezioni mancanti nel periodo indicato usando i giorni e gli orari
     * ricorrenti del corso. Le lezioni gia presenti non vengono toccate.
     */
    @Transactional
    public List<LezioneResponse> generaLezioni(Long corsoId, GeneraLezioniRequest request) {
        Corso corso = getCorso(corsoId);
        LocalDate from = request.getFrom();
        LocalDate to = request.getTo();

        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La data di fine deve essere successiva a quella di inizio.");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_GIORNI_GENERAZIONE) {
            throw new IllegalArgumentException("Il periodo di generazione non puo superare "
                    + MAX_GIORNI_GENERAZIONE + " giorni.");
        }
        if (corso.getOrari().isEmpty()) {
            throw new IllegalArgumentException("Il corso non ha giorni e orari impostati.");
        }

        Map<Integer, List<CorsoOrario>> perGiorno = new LinkedHashMap<>();
        for (CorsoOrario orario : corso.getOrari()) {
            perGiorno.computeIfAbsent(orario.getGiornoSettimana(), k -> new ArrayList<>()).add(orario);
        }

        List<Lezione> nuove = new ArrayList<>();
        for (LocalDate data = from; !data.isAfter(to); data = data.plusDays(1)) {
            for (CorsoOrario orario : perGiorno.getOrDefault(data.getDayOfWeek().getValue(), List.of())) {
                if (lezioneRepository.findByCorsoIdAndDataAndOraInizio(corsoId, data, orario.getOraInizio()).isPresent()) {
                    continue;
                }
                Lezione lezione = new Lezione();
                lezione.setCorso(corso);
                lezione.setData(data);
                lezione.setOraInizio(orario.getOraInizio());
                lezione.setOraFine(orario.getOraFine());
                lezione.setSala(orario.getSala());
                lezione.setAnnullata(false);
                nuove.add(lezione);
            }
        }

        return lezioneRepository.saveAll(nuove).stream().map(LezioneResponse::from).toList();
    }

    @Transactional
    public LezioneResponse creaLezione(Long corsoId, LezioneRequest request) {
        Corso corso = getCorso(corsoId);
        lezioneRepository.findByCorsoIdAndDataAndOraInizio(corsoId, request.getData(), request.getOraInizio())
                .ifPresent(l -> {
                    throw new IllegalArgumentException("Esiste gia una lezione per questa data e ora.");
                });

        Lezione lezione = new Lezione();
        lezione.setCorso(corso);
        mapToEntity(lezione, request);
        return LezioneResponse.from(lezioneRepository.save(lezione));
    }

    @Transactional
    public LezioneResponse updateLezione(Long lezioneId, LezioneRequest request) {
        Lezione lezione = getLezione(lezioneId);
        mapToEntity(lezione, request);
        return LezioneResponse.from(lezioneRepository.save(lezione));
    }

    @Transactional
    public void deleteLezione(Long lezioneId) {
        Lezione lezione = getLezione(lezioneId);
        presenzaRepository.deleteByLezioneId(lezione.getId());
        lezioneRepository.delete(lezione);
    }

    // ---------- Foglio presenze ----------

    public List<PresenzaResponse> findPresenzeLezione(Long lezioneId) {
        getLezione(lezioneId);
        return presenzaRepository.findByLezioneId(lezioneId).stream().map(PresenzaResponse::from).toList();
    }

    public FoglioPresenzeResponse foglio(Long corsoId, LocalDate from, LocalDate to) {
        Corso corso = getCorso(corsoId);
        LocalDate inizio = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate fine = to != null ? to : inizio.plusMonths(1).minusDays(1);

        List<Lezione> lezioni =
                lezioneRepository.findByCorsoIdAndDataBetweenOrderByDataAscOraInizioAsc(corsoId, inizio, fine);
        List<Presenza> presenze = presenzaRepository.findByCorsoAndPeriodo(corsoId, inizio, fine);

        // Righe: iscritti attivi + eventuali atleti disiscritti ma con presenze storiche nel periodo
        Map<Long, FoglioPresenzeResponse.Riga> righe = new LinkedHashMap<>();
        for (CorsoIscrizione iscrizione : iscrizioneRepository.findByCorsoIdAndAttivoTrue(corsoId)) {
            Atleta atleta = iscrizione.getAtleta();
            righe.put(atleta.getId(), FoglioPresenzeResponse.Riga.from(atleta, true));
        }
        for (Presenza presenza : presenze) {
            Atleta atleta = presenza.getAtleta();
            righe.computeIfAbsent(atleta.getId(), k -> FoglioPresenzeResponse.Riga.from(atleta, false));
        }

        FoglioPresenzeResponse response = new FoglioPresenzeResponse();
        response.setCorsoId(corso.getId());
        response.setCorsoNome(corso.getNome());
        response.setFrom(inizio);
        response.setTo(fine);
        response.setLezioni(lezioni.stream().map(LezioneResponse::from).toList());
        response.setAtleti(righe.values().stream()
                .sorted(Comparator
                        .comparing(FoglioPresenzeResponse.Riga::getCognome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(FoglioPresenzeResponse.Riga::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList());
        response.setPresenze(presenze.stream().map(PresenzaResponse::from).toList());
        return response;
    }

    /**
     * Salva in blocco le celle modificate del foglio. Uno stato nullo cancella
     * la registrazione, riportando la cella a "non compilata".
     */
    @Transactional
    public List<PresenzaResponse> salvaPresenze(Long corsoId, PresenzeBulkRequest request) {
        getCorso(corsoId);
        List<PresenzaResponse> salvate = new ArrayList<>();

        for (PresenzeBulkRequest.Record record : request.getRecords()) {
            Lezione lezione = getLezione(record.getLezioneId());
            if (!lezione.getCorso().getId().equals(corsoId)) {
                throw new IllegalArgumentException(
                        "La lezione " + lezione.getId() + " non appartiene al corso " + corsoId);
            }

            Presenza esistente = presenzaRepository
                    .findByLezioneIdAndAtletaId(lezione.getId(), record.getAtletaId())
                    .orElse(null);

            if (record.getStato() == null) {
                if (esistente != null) {
                    presenzaRepository.delete(esistente);
                }
                continue;
            }

            Presenza presenza = esistente;
            if (presenza == null) {
                Atleta atleta = atletaRepository.findById(record.getAtletaId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Atleta non trovato con id: " + record.getAtletaId()));
                presenza = new Presenza();
                presenza.setLezione(lezione);
                presenza.setAtleta(atleta);
            }
            presenza.setStato(record.getStato());
            presenza.setNote(record.getNote());
            salvate.add(PresenzaResponse.from(presenzaRepository.save(presenza)));
        }
        return salvate;
    }

    // ---------- Helpers ----------

    private Corso getCorso(Long id) {
        return corsoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CORSO_NOT_FOUND + id));
    }

    private Lezione getLezione(Long id) {
        return lezioneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(LEZIONE_NOT_FOUND + id));
    }

    private void mapToEntity(Lezione lezione, LezioneRequest request) {
        lezione.setData(request.getData());
        lezione.setOraInizio(request.getOraInizio());
        lezione.setOraFine(request.getOraFine());
        lezione.setSala(request.getSala());
        lezione.setNote(request.getNote());
        lezione.setAnnullata(Boolean.TRUE.equals(request.getAnnullata()));
    }
}
