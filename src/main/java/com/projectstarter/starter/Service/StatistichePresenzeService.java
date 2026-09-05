package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Request.FiltroPresenzeRequest;
import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Dto.Response.PeriodoResponse;
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
import com.projectstarter.starter.Util.Aggregazioni;
import com.projectstarter.starter.Util.Giorni;
import com.projectstarter.starter.Util.Periodi;
import com.projectstarter.starter.Util.Ricerca;
import com.projectstarter.starter.Util.Stagioni;
import com.projectstarter.starter.Util.Statistiche;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Statistiche delle presenze, di sola lettura: ricalcola tutto dai fogli
 * presenze gia compilati. Le lezioni annullate e le registrazioni fatte su di
 * esse restano fuori da ogni conteggio.
 */
@Service
@RequiredArgsConstructor
public class StatistichePresenzeService {

    private final CorsoRepository corsoRepository;
    private final CorsoIscrizioneRepository iscrizioneRepository;
    private final LezioneRepository lezioneRepository;
    private final PresenzaRepository presenzaRepository;

    private static final String CORSO_NOT_FOUND = "Corso non trovato con id: ";
    /** Ambito mostrato quando le statistiche non sono filtrate su un corso. */
    private static final String TUTTI_I_CORSI = "Tutti i corsi";
    /** Oltre questo numero di mesi l'andamento mensile diventa illeggibile. */
    private static final int MAX_MESI = 36;

    /**
     * @param corsoId  se nullo aggrega tutti i corsi
     * @param stagione usata per ricavare il periodo quando from/to non sono indicati
     */
    public StatistichePresenzeResponse calcola(Long corsoId, String stagione, LocalDate from, LocalDate to) {
        FiltroPresenzeRequest filtro = new FiltroPresenzeRequest();
        filtro.setCorsoId(corsoId);
        filtro.setStagione(stagione);
        filtro.setFrom(from);
        filtro.setTo(to);
        return calcola(filtro);
    }

    /**
     * Calcola le statistiche del periodo richiesto. Periodo, filtro sulla
     * classifica e ordinamento sono risolti qui: il client mostra quello che
     * riceve senza rifiltrare ne riordinare nulla.
     */
    public StatistichePresenzeResponse calcola(FiltroPresenzeRequest filtro) {
        Long corsoId = filtro.getCorsoId();
        Corso corso = corsoId == null ? null : getCorso(corsoId);

        String stagione = Stagioni.normalizza(filtro.getStagione());
        Periodi.Preset preset = Periodi.preset(filtro.getPeriodo(), filtro.getFrom(), filtro.getTo());
        Periodi.Intervallo intervallo =
                Periodi.risolvi(preset, stagione, filtro.getFrom(), filtro.getTo(), LocalDate.now());
        LocalDate inizio = intervallo.from();
        LocalDate fine = intervallo.to();
        Statistiche.Ordine ordine = Statistiche.Ordine.da(filtro.getOrdine());

        List<Lezione> lezioni = corsoId == null
                ? lezioneRepository.findPerStatistiche(inizio, fine)
                : lezioneRepository.findPerStatistiche(inizio, fine, corsoId);
        List<Presenza> presenze = (corsoId == null
                ? presenzaRepository.findPerStatistiche(inizio, fine)
                : presenzaRepository.findPerStatistiche(inizio, fine, corsoId))
                .stream()
                .filter(p -> !Boolean.TRUE.equals(p.getLezione().getAnnullata()))
                .toList();

        List<Lezione> svolte = lezioni.stream()
                .filter(l -> !Boolean.TRUE.equals(l.getAnnullata()))
                .toList();

        ContatorePresenze totale = new ContatorePresenze();
        presenze.forEach(totale::aggiungi);

        StatistichePresenzeResponse response = new StatistichePresenzeResponse();
        response.setFrom(inizio);
        response.setTo(fine);
        response.setPeriodo(PeriodoResponse.di(preset, intervallo));
        if (corso != null) {
            response.setCorsoId(corso.getId());
            response.setCorsoNome(corso.getNome());
        }
        response.setAmbito(corso == null ? TUTTI_I_CORSI : corso.getNome());
        response.setStagione(stagione);
        response.setLezioniSvolte(svolte.size());
        response.setLezioniAnnullate(lezioni.size() - svolte.size());
        response.setRegistrazioni(totale.registrazioni());
        response.setPresenti(totale.presenti);
        response.setAssenti(totale.assenti);
        response.setTassoPresenza(Aggregazioni.percentuale(totale.presenti, totale.registrazioni()));
        response.setMediaPresentiPerLezione(svolte.isEmpty()
                ? null
                : Aggregazioni.arrotonda((double) totale.presenti / svolte.size()));
        response.setAtletiCoinvolti(presenze.stream().map(p -> p.getAtleta().getId()).distinct().count());

        response.setAndamentoMensile(andamentoMensile(inizio, fine, svolte, presenze));
        response.setPerCorso(perCorso(svolte, presenze));

        List<StatistichePresenzeResponse.RigaAtleta> classifica = perAtleta(presenze);
        response.setAtletiInClassifica(classifica.size());
        response.setPerAtleta(ordina(filtra(classifica, filtro.getQ()), ordine));
        response.setOrdine(ordine);
        return response;
    }

    /** Il filtro sulla classifica: cosi il client non tiene in memoria l'elenco intero. */
    private List<StatistichePresenzeResponse.RigaAtleta> filtra(
            List<StatistichePresenzeResponse.RigaAtleta> righe, String q) {
        String termine = Ricerca.normalizza(q);
        if (termine == null) {
            return righe;
        }
        return righe.stream()
                .filter(riga -> riga.getNominativo().toLowerCase().contains(termine))
                .toList();
    }

    /** La classifica esce dal calcolo dal tasso piu alto: il verso opposto la ribalta. */
    private List<StatistichePresenzeResponse.RigaAtleta> ordina(
            List<StatistichePresenzeResponse.RigaAtleta> righe, Statistiche.Ordine ordine) {
        if (ordine != Statistiche.Ordine.PEGGIORI) {
            return righe;
        }
        List<StatistichePresenzeResponse.RigaAtleta> ribaltata = new ArrayList<>(righe);
        Collections.reverse(ribaltata);
        return List.copyOf(ribaltata);
    }

    // ---------- Andamento mensile ----------

    private List<StatistichePresenzeResponse.PuntoMensile> andamentoMensile(
            LocalDate inizio, LocalDate fine, List<Lezione> svolte, List<Presenza> presenze) {

        // I mesi sono preimpostati tutti: un mese senza lezioni resta nel grafico a zero
        Map<YearMonth, ContatorePresenze> perMese = new LinkedHashMap<>();
        for (YearMonth mese : Aggregazioni.mesi(inizio, fine, MAX_MESI)) {
            perMese.put(mese, new ContatorePresenze());
        }
        for (Lezione lezione : svolte) {
            ContatorePresenze contatore = perMese.get(YearMonth.from(lezione.getData()));
            if (contatore != null) contatore.aggiungiLezione();
        }
        for (Presenza presenza : presenze) {
            ContatorePresenze contatore = perMese.get(YearMonth.from(presenza.getLezione().getData()));
            if (contatore != null) contatore.aggiungi(presenza);
        }

        List<StatistichePresenzeResponse.PuntoMensile> punti = new ArrayList<>();
        for (Map.Entry<YearMonth, ContatorePresenze> entry : perMese.entrySet()) {
            ContatorePresenze contatore = entry.getValue();
            StatistichePresenzeResponse.PuntoMensile punto = new StatistichePresenzeResponse.PuntoMensile();
            punto.setMese(entry.getKey().toString());
            punto.setMeseLabel(Giorni.meseBreve(entry.getKey()));
            punto.setLezioniSvolte(contatore.lezioni);
            punto.setPresenti(contatore.presenti);
            punto.setAssenti(contatore.assenti);
            punto.setTassoPresenza(Aggregazioni.percentuale(contatore.presenti, contatore.registrazioni()));
            punti.add(punto);
        }
        return punti;
    }

    // ---------- Dettaglio per corso ----------

    private List<StatistichePresenzeResponse.RigaCorso> perCorso(List<Lezione> svolte, List<Presenza> presenze) {
        Map<Long, ContatoreCorso> perCorso = new LinkedHashMap<>();
        for (Lezione lezione : svolte) {
            contatoreCorso(perCorso, lezione.getCorso()).presenze.aggiungiLezione();
        }
        for (Presenza presenza : presenze) {
            contatoreCorso(perCorso, presenza.getLezione().getCorso()).presenze.aggiungi(presenza);
        }

        Map<Long, Long> iscritti = iscrittiAttiviPerCorso();
        return perCorso.values().stream()
                .map(contatore -> rigaCorso(contatore, iscritti))
                .sorted(Comparator.comparing(StatistichePresenzeResponse.RigaCorso::getCorsoNome,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private ContatoreCorso contatoreCorso(Map<Long, ContatoreCorso> perCorso, Corso corso) {
        return perCorso.computeIfAbsent(corso.getId(), id -> new ContatoreCorso(corso));
    }

    private StatistichePresenzeResponse.RigaCorso rigaCorso(ContatoreCorso contatore, Map<Long, Long> iscritti) {
        ContatorePresenze conteggio = contatore.presenze;
        StatistichePresenzeResponse.RigaCorso riga = new StatistichePresenzeResponse.RigaCorso();
        riga.setCorsoId(contatore.corso.getId());
        riga.setCorsoNome(contatore.corso.getNome());
        riga.setIscrittiAttivi(iscritti.getOrDefault(contatore.corso.getId(), 0L));
        riga.setLezioniSvolte(conteggio.lezioni);
        riga.setPresenti(conteggio.presenti);
        riga.setAssenti(conteggio.assenti);
        riga.setTassoPresenza(Aggregazioni.percentuale(conteggio.presenti, conteggio.registrazioni()));
        riga.setMediaPresentiPerLezione(conteggio.lezioni == 0
                ? null
                : Aggregazioni.arrotonda((double) conteggio.presenti / conteggio.lezioni));
        return riga;
    }

    // ---------- Classifica atleti ----------

    private List<StatistichePresenzeResponse.RigaAtleta> perAtleta(List<Presenza> presenze) {
        Map<Long, ContatoreAtleta> perAtleta = new LinkedHashMap<>();
        for (Presenza presenza : presenze) {
            Atleta atleta = presenza.getAtleta();
            perAtleta.computeIfAbsent(atleta.getId(), id -> new ContatoreAtleta(atleta))
                    .presenze.aggiungi(presenza);
        }

        return perAtleta.values().stream()
                .map(this::rigaAtleta)
                .sorted(Comparator
                        .comparingDouble((StatistichePresenzeResponse.RigaAtleta r) ->
                                r.getTassoPresenza() == null ? -1d : r.getTassoPresenza())
                        .reversed()
                        .thenComparing(StatistichePresenzeResponse.RigaAtleta::getCognome,
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private StatistichePresenzeResponse.RigaAtleta rigaAtleta(ContatoreAtleta contatore) {
        ContatorePresenze conteggio = contatore.presenze;
        StatistichePresenzeResponse.RigaAtleta riga = new StatistichePresenzeResponse.RigaAtleta();
        riga.setAtletaId(contatore.atleta.getId());
        riga.setNome(contatore.atleta.getNome());
        riga.setCognome(contatore.atleta.getCognome());
        riga.setNominativo(AtletaResponse.nominativo(contatore.atleta));
        riga.setPresenti(conteggio.presenti);
        riga.setAssenti(conteggio.assenti);
        riga.setRegistrazioni(conteggio.registrazioni());
        riga.setTassoPresenza(Aggregazioni.percentuale(conteggio.presenti, conteggio.registrazioni()));
        return riga;
    }

    // ---------- Helpers ----------

    private Corso getCorso(Long id) {
        return corsoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CORSO_NOT_FOUND + id));
    }

    /** La GROUP BY garantisce una riga per corso, quindi nessuna chiave duplicata. */
    private Map<Long, Long> iscrittiAttiviPerCorso() {
        return iscrizioneRepository.countIscrittiAttiviPerCorso().stream()
                .collect(Collectors.toMap(ConteggioIscritti::getCorsoId, ConteggioIscritti::getTotale));
    }

    // ---------- Accumulatori ----------
    // Piccole classi mutabili al posto degli array di contatori: il nome del campo
    // dice cosa si sta incrementando, e il compilatore protegge dai refusi.

    private static final class ContatorePresenze {

        private long lezioni;
        private long presenti;
        private long assenti;

        void aggiungiLezione() {
            lezioni++;
        }

        void aggiungi(Presenza presenza) {
            if (presenza.getStato() == Presenza.Stato.PRESENTE) {
                presenti++;
            } else {
                assenti++;
            }
        }

        /** Celle del foglio effettivamente compilate: e il denominatore del tasso. */
        long registrazioni() {
            return presenti + assenti;
        }
    }

    /** Contatore di un corso, con l'entita che serve a comporre la riga di risposta. */
    private static final class ContatoreCorso {

        private final Corso corso;
        private final ContatorePresenze presenze = new ContatorePresenze();

        ContatoreCorso(Corso corso) {
            this.corso = corso;
        }
    }

    private static final class ContatoreAtleta {

        private final Atleta atleta;
        private final ContatorePresenze presenze = new ContatorePresenze();

        ContatoreAtleta(Atleta atleta) {
            this.atleta = atleta;
        }
    }
}
