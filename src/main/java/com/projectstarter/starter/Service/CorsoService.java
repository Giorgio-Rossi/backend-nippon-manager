package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Request.CorsoOrarioRequest;
import com.projectstarter.starter.Dto.Request.CorsoRequest;
import com.projectstarter.starter.Dto.Request.IscrizioneRequest;
import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Dto.Response.CorsoResponse;
import com.projectstarter.starter.Dto.Response.IscrizioneResponse;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Entity.Corso;
import com.projectstarter.starter.Entity.CorsoIscrizione;
import com.projectstarter.starter.Entity.CorsoOrario;
import com.projectstarter.starter.Repository.AtletaRepository;
import com.projectstarter.starter.Repository.CorsoIscrizioneRepository;
import com.projectstarter.starter.Repository.CorsoIscrizioneRepository.ConteggioIscritti;
import com.projectstarter.starter.Repository.CorsoRepository;
import com.projectstarter.starter.Repository.LezioneRepository;
import com.projectstarter.starter.Repository.PagamentoRepository;
import com.projectstarter.starter.Repository.PresenzaRepository;
import com.projectstarter.starter.Util.Ricerca;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CorsoService {

    private final CorsoRepository corsoRepository;
    private final CorsoIscrizioneRepository iscrizioneRepository;
    private final AtletaRepository atletaRepository;
    private final LezioneRepository lezioneRepository;
    private final PresenzaRepository presenzaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final AtletaService atletaService;

    private static final String CORSO_NOT_FOUND = "Corso non trovato con id: ";
    private static final String ATLETA_NOT_FOUND = "Atleta non trovato con id: ";

    /**
     * Elenco gia filtrato e ordinato dal database, con il numero di iscritti
     * risolto in una sola query invece che corso per corso.
     *
     * @param attivo nullo per non filtrare sullo stato
     * @param q      termine di ricerca su nome e luogo, nullo per non filtrare
     */
    public List<CorsoResponse> findAll(Boolean attivo, String q) {
        List<Corso> corsi = corsoRepository.cerca(attivo, Ricerca.normalizza(q));
        Map<Long, Long> iscritti = iscrittiAttiviPerCorso();
        return corsi.stream()
                .map(corso -> CorsoResponse.from(corso, iscritti.getOrDefault(corso.getId(), 0L)))
                .toList();
    }

    public CorsoResponse findById(Long id) {
        return toResponse(getCorso(id));
    }

    public List<CorsoResponse> search(String q) {
        return findAll(null, q);
    }

    @Transactional
    public CorsoResponse create(CorsoRequest request) {
        Corso corso = new Corso();
        mapToEntity(corso, request);
        return toResponse(corsoRepository.save(corso));
    }

    @Transactional
    public CorsoResponse update(Long id, CorsoRequest request) {
        Corso corso = getCorso(id);
        mapToEntity(corso, request);
        return toResponse(corsoRepository.save(corso));
    }

    @Transactional
    public void deactivate(Long id) {
        Corso corso = getCorso(id);
        corso.setAttivo(false);
        corsoRepository.save(corso);
    }

    @Transactional
    public void activate(Long id) {
        Corso corso = getCorso(id);
        corso.setAttivo(true);
        corsoRepository.save(corso);
    }

    @Transactional
    public void delete(Long id) {
        Corso corso = getCorso(id);
        pagamentoRepository.deleteAll(pagamentoRepository.findByCorsoId(corso.getId()));
        presenzaRepository.deleteAll(presenzaRepository.findByCorso(corso.getId()));
        lezioneRepository.deleteByCorsoId(corso.getId());
        iscrizioneRepository.deleteByCorsoId(corso.getId());
        corsoRepository.delete(corso);
    }

    // ---------- Iscrizioni ----------

    public List<IscrizioneResponse> findIscritti(Long corsoId, Boolean attivo) {
        getCorso(corsoId);
        List<CorsoIscrizione> iscrizioni = Boolean.TRUE.equals(attivo)
                ? iscrizioneRepository.findByCorsoIdAndAttivoTrue(corsoId)
                : iscrizioneRepository.findByCorsoId(corsoId);
        return iscrizioni.stream()
                .sorted(Comparator
                        .comparing((CorsoIscrizione i) -> i.getAtleta().getCognome(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(i -> i.getAtleta().getNome(), String.CASE_INSENSITIVE_ORDER))
                .map(IscrizioneResponse::from)
                .toList();
    }

    /** Atleti attivi non ancora iscritti al corso, per la modale di associazione. */
    public List<AtletaResponse> findIscrivibili(Long corsoId, String q) {
        getCorso(corsoId);
        return atletaService.findIscrivibili(corsoId, q);
    }

    /** Corsi a cui un atleta risulta iscritto. */
    public List<IscrizioneResponse> findCorsiDiAtleta(Long atletaId) {
        return iscrizioneRepository.findByAtletaId(atletaId).stream().map(IscrizioneResponse::from).toList();
    }

    @Transactional
    public List<IscrizioneResponse> iscrivi(Long corsoId, IscrizioneRequest request) {
        Corso corso = getCorso(corsoId);
        List<IscrizioneResponse> result = new ArrayList<>();

        for (Long atletaId : request.getAtletaIds()) {
            CorsoIscrizione iscrizione = iscrizioneRepository
                    .findByCorsoIdAndAtletaId(corsoId, atletaId)
                    .orElseGet(() -> {
                        Atleta atleta = atletaRepository.findById(atletaId)
                                .orElseThrow(() -> new EntityNotFoundException(ATLETA_NOT_FOUND + atletaId));
                        CorsoIscrizione nuova = new CorsoIscrizione();
                        nuova.setCorso(corso);
                        nuova.setAtleta(atleta);
                        return nuova;
                    });

            // Una re-iscrizione riattiva il record esistente invece di duplicarlo
            iscrizione.setAttivo(true);
            iscrizione.setDataDisiscrizione(null);
            if (request.getDataIscrizione() != null) {
                iscrizione.setDataIscrizione(request.getDataIscrizione());
            } else if (iscrizione.getDataIscrizione() == null) {
                iscrizione.setDataIscrizione(LocalDate.now());
            }
            if (request.getNote() != null) {
                iscrizione.setNote(request.getNote());
            }
            result.add(IscrizioneResponse.from(iscrizioneRepository.save(iscrizione)));
        }
        return result;
    }

    /** Disiscrive mantenendo lo storico (attivo = false). */
    @Transactional
    public void disiscrivi(Long corsoId, Long atletaId) {
        CorsoIscrizione iscrizione = iscrizioneRepository.findByCorsoIdAndAtletaId(corsoId, atletaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Iscrizione non trovata per corso " + corsoId + " e atleta " + atletaId));
        iscrizione.setAttivo(false);
        iscrizione.setDataDisiscrizione(LocalDate.now());
        iscrizioneRepository.save(iscrizione);
    }

    /** Rimuove definitivamente il record di iscrizione. */
    @Transactional
    public void rimuoviIscrizione(Long corsoId, Long atletaId) {
        CorsoIscrizione iscrizione = iscrizioneRepository.findByCorsoIdAndAtletaId(corsoId, atletaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Iscrizione non trovata per corso " + corsoId + " e atleta " + atletaId));
        iscrizioneRepository.delete(iscrizione);
    }

    // ---------- Helpers ----------

    private Corso getCorso(Long id) {
        return corsoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CORSO_NOT_FOUND + id));
    }

    private CorsoResponse toResponse(Corso corso) {
        return CorsoResponse.from(corso, iscrizioneRepository.countByCorsoIdAndAttivoTrue(corso.getId()));
    }

    /** La GROUP BY garantisce una riga per corso, quindi nessuna chiave duplicata. */
    private Map<Long, Long> iscrittiAttiviPerCorso() {
        return iscrizioneRepository.countIscrittiAttiviPerCorso().stream()
                .collect(Collectors.toMap(ConteggioIscritti::getCorsoId, ConteggioIscritti::getTotale));
    }

    private void mapToEntity(Corso corso, CorsoRequest request) {
        corso.setNome(request.getNome());
        corso.setDescrizione(request.getDescrizione());
        corso.setLuogo(request.getLuogo());
        corso.setDataInizio(request.getDataInizio());
        corso.setDataFine(request.getDataFine());
        corso.setQuotaMensile(request.getQuotaMensile());
        corso.setQuotaTessera(request.getQuotaTessera());
        corso.setQuotaRata(request.getQuotaRata());
        corso.setNote(request.getNote());

        // Gli orari arrivano sempre come lista completa: sostituzione in blocco
        corso.getOrari().clear();
        if (request.getOrari() != null) {
            for (CorsoOrarioRequest o : request.getOrari()) {
                CorsoOrario orario = new CorsoOrario();
                orario.setGiornoSettimana(o.getGiornoSettimana());
                orario.setOraInizio(o.getOraInizio());
                orario.setOraFine(o.getOraFine());
                orario.setSala(o.getSala());
                corso.addOrario(orario);
            }
        }
    }
}
