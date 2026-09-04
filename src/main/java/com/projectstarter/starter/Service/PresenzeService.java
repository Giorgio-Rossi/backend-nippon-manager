package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Request.GeneraLezioniRequest;
import com.projectstarter.starter.Dto.Request.LezioneRequest;
import com.projectstarter.starter.Dto.Request.PresenzeBulkRequest;
import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Dto.Response.FoglioPresenzeResponse;
import com.projectstarter.starter.Dto.Response.GenerazioneLezioniResponse;
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
import com.projectstarter.starter.Util.Giorni;
import com.projectstarter.starter.Util.Periodi;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public GenerazioneLezioniResponse generaLezioni(Long corsoId, GeneraLezioniRequest request) {
        Corso corso = getCorso(corsoId);
        Periodi.Intervallo periodo = periodoGenerazione(request);
        LocalDate from = periodo.from();
        LocalDate to = periodo.to();

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

        return GenerazioneLezioniResponse.di(
                lezioneRepository.saveAll(nuove).stream().map(LezioneResponse::from).toList());
    }

    /** Il mese ha la precedenza; in mancanza servono entrambi gli estremi. */
    private Periodi.Intervallo periodoGenerazione(GeneraLezioniRequest request) {
        if (request.getMese() != null && !request.getMese().isBlank()) {
            return Periodi.mese(mese(request.getMese(), null));
        }
        if (request.getFrom() == null || request.getTo() == null) {
            throw new IllegalArgumentException("Indicare il mese oppure entrambe le date del periodo.");
        }
        return new Periodi.Intervallo(request.getFrom(), request.getTo());
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

    /**
     * Il foglio di un mese, gia impaginato: le celle sono associate alla riga
     * dell'atleta e i totali di colonna e di riga sono calcolati sullo stato
     * salvato. Il client somma solo le modifiche che non ha ancora inviato.
     *
     * @param mese formato "2026-09"; in mancanza si usano {@code from}/{@code to},
     *             e in mancanza di entrambi il mese corrente
     */
    public FoglioPresenzeResponse foglio(Long corsoId, String mese, LocalDate from, LocalDate to) {
        Corso corso = getCorso(corsoId);
        YearMonth periodo = mese(mese, from);
        Periodi.Intervallo intervallo = intervallo(periodo, mese, from, to);
        LocalDate inizio = intervallo.from();
        LocalDate fine = intervallo.to();

        List<Lezione> lezioni =
                lezioneRepository.findByCorsoIdAndDataBetweenOrderByDataAscOraInizioAsc(corsoId, inizio, fine);
        List<Presenza> presenze = presenzaRepository.findByCorsoAndPeriodo(corsoId, inizio, fine);

        // Righe: iscritti attivi + eventuali atleti disiscritti ma con presenze storiche nel periodo
        Map<Long, Atleta> atleti = new LinkedHashMap<>();
        Set<Long> iscrittiAttivi = new LinkedHashSet<>();
        for (CorsoIscrizione iscrizione : iscrizioneRepository.findByCorsoIdAndAttivoTrue(corsoId)) {
            Atleta atleta = iscrizione.getAtleta();
            atleti.put(atleta.getId(), atleta);
            iscrittiAttivi.add(atleta.getId());
        }
        for (Presenza presenza : presenze) {
            atleti.putIfAbsent(presenza.getAtleta().getId(), presenza.getAtleta());
        }

        Map<Long, Map<Long, Presenza.Stato>> perAtleta = indicizzaPerAtleta(presenze);

        List<FoglioPresenzeResponse.Riga> righe = atleti.values().stream()
                .map(atleta -> riga(atleta, iscrittiAttivi.contains(atleta.getId()),
                        perAtleta.getOrDefault(atleta.getId(), Map.of())))
                .sorted(Comparator
                        .comparing(FoglioPresenzeResponse.Riga::getCognome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(FoglioPresenzeResponse.Riga::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<FoglioPresenzeResponse.Colonna> colonne = colonne(lezioni, presenze);

        FoglioPresenzeResponse response = new FoglioPresenzeResponse();
        response.setCorsoId(corso.getId());
        response.setCorsoNome(corso.getNome());
        response.setMese(periodo.toString());
        response.setMeseLabel(Giorni.nomeMese(periodo));
        response.setMesePrecedente(periodo.minusMonths(1).toString());
        response.setMeseSuccessivo(periodo.plusMonths(1).toString());
        response.setFrom(inizio);
        response.setTo(fine);
        response.setLezioni(colonne);
        response.setAtleti(righe);
        response.setTotali(totali(colonne, righe));
        return response;
    }

    /** Il mese mostrato dal foglio, che guida le frecce di navigazione. */
    private YearMonth mese(String mese, LocalDate from) {
        if (mese != null && !mese.isBlank()) {
            try {
                return YearMonth.parse(mese.trim());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Mese non valido: atteso nel formato 2026-09.");
            }
        }
        return YearMonth.from(from != null ? from : LocalDate.now());
    }

    /** Estremi espliciti hanno la precedenza, cosi le chiamate per periodo restano valide. */
    private Periodi.Intervallo intervallo(YearMonth periodo, String mese, LocalDate from, LocalDate to) {
        if ((mese == null || mese.isBlank()) && (from != null || to != null)) {
            Periodi.Intervallo predefinito = Periodi.mese(periodo);
            return new Periodi.Intervallo(
                    from != null ? from : predefinito.from(),
                    to != null ? to : predefinito.to());
        }
        return Periodi.mese(periodo);
    }

    private Map<Long, Map<Long, Presenza.Stato>> indicizzaPerAtleta(List<Presenza> presenze) {
        Map<Long, Map<Long, Presenza.Stato>> indice = new LinkedHashMap<>();
        for (Presenza presenza : presenze) {
            indice.computeIfAbsent(presenza.getAtleta().getId(), k -> new LinkedHashMap<>())
                    .put(presenza.getLezione().getId(), presenza.getStato());
        }
        return indice;
    }

    private FoglioPresenzeResponse.Riga riga(Atleta atleta, boolean iscrizioneAttiva,
                                             Map<Long, Presenza.Stato> celle) {
        FoglioPresenzeResponse.Riga riga = new FoglioPresenzeResponse.Riga();
        riga.setAtletaId(atleta.getId());
        riga.setNome(atleta.getNome());
        riga.setCognome(atleta.getCognome());
        riga.setNominativo(AtletaResponse.nominativo(atleta));
        riga.setCintura(atleta.getCintura());
        riga.setIscrizioneAttiva(iscrizioneAttiva);
        riga.setCelle(celle);
        riga.setPresenti(conta(celle.values(), Presenza.Stato.PRESENTE));
        riga.setAssenti(conta(celle.values(), Presenza.Stato.ASSENTE));
        return riga;
    }

    private List<FoglioPresenzeResponse.Colonna> colonne(List<Lezione> lezioni, List<Presenza> presenze) {
        Map<Long, List<Presenza>> perLezione = presenze.stream()
                .collect(Collectors.groupingBy(p -> p.getLezione().getId()));

        return lezioni.stream().map(lezione -> {
            List<Presenza> registrate = perLezione.getOrDefault(lezione.getId(), List.of());
            FoglioPresenzeResponse.Colonna colonna = new FoglioPresenzeResponse.Colonna();
            colonna.setLezione(LezioneResponse.from(lezione));
            colonna.setPresenti(registrate.stream().filter(p -> p.getStato() == Presenza.Stato.PRESENTE).count());
            colonna.setAssenti(registrate.stream().filter(p -> p.getStato() == Presenza.Stato.ASSENTE).count());
            colonna.setCompilate(registrate.size());
            return colonna;
        }).toList();
    }

    private FoglioPresenzeResponse.Totali totali(List<FoglioPresenzeResponse.Colonna> colonne,
                                                 List<FoglioPresenzeResponse.Riga> righe) {
        FoglioPresenzeResponse.Totali totali = new FoglioPresenzeResponse.Totali();
        totali.setLezioni(colonne.size());
        totali.setAtleti(righe.size());
        totali.setCelleTotali((long) colonne.size() * righe.size());
        totali.setCelleCompilate(colonne.stream().mapToLong(FoglioPresenzeResponse.Colonna::getCompilate).sum());
        totali.setPresenti(colonne.stream().mapToLong(FoglioPresenzeResponse.Colonna::getPresenti).sum());
        totali.setAssenti(colonne.stream().mapToLong(FoglioPresenzeResponse.Colonna::getAssenti).sum());
        return totali;
    }

    private long conta(Collection<Presenza.Stato> stati, Presenza.Stato stato) {
        return stati.stream().filter(s -> s == stato).count();
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
