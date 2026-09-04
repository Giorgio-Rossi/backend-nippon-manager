package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Request.AtletaRequest;
import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Repository.AtletaRepository;
import com.projectstarter.starter.Util.Ricerca;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtletaService {

    private final AtletaRepository atletaRepository;

    private final static String ATHLET_NOT_FOUND = "Atleta non trovato con id: ";

    /**
     * Elenco gia filtrato e ordinato dal database.
     *
     * @param attivo nullo per non filtrare sullo stato
     * @param q      termine di ricerca su nominativo e codice fiscale, nullo per non filtrare
     */
    public List<AtletaResponse> findAll(Boolean attivo, String q) {
        return mappa(atletaRepository.cerca(attivo, Ricerca.normalizza(q)));
    }

    public AtletaResponse findById(Long id) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ATHLET_NOT_FOUND + id));
        return AtletaResponse.from(atleta);
    }

    public List<AtletaResponse> search(String q) {
        return findAll(null, q);
    }

    /** Atleti attivi non ancora iscritti al corso, per la modale di associazione. */
    public List<AtletaResponse> findIscrivibili(Long corsoId, String q) {
        return mappa(atletaRepository.findIscrivibili(corsoId, Ricerca.normalizza(q)));
    }

    /** Certificati gia scaduti o in scadenza entro {@code daysAhead} giorni. */
    public List<AtletaResponse> findExpiringCertificates(int daysAhead) {
        return findExpiringCertificates(daysAhead, false);
    }

    public List<AtletaResponse> findExpiringCertificates(int daysAhead, boolean soloAttivi) {
        LocalDate oggi = LocalDate.now();
        return atletaRepository.findCertificatiInScadenza(oggi.plusDays(daysAhead), soloAttivi)
                .stream().map(atleta -> AtletaResponse.from(atleta, oggi)).toList();
    }

    /** Una sola data di riferimento per tutta la lista: righe coerenti tra loro. */
    private List<AtletaResponse> mappa(List<Atleta> atleti) {
        LocalDate oggi = LocalDate.now();
        return atleti.stream().map(atleta -> AtletaResponse.from(atleta, oggi)).toList();
    }

    public AtletaResponse create(AtletaRequest request) {
        Atleta atleta = mapToEntity(new Atleta(), request);
        return AtletaResponse.from(atletaRepository.save(atleta));
    }

    public AtletaResponse update(Long id, AtletaRequest request) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ATHLET_NOT_FOUND + id));
        mapToEntity(atleta, request);
        return AtletaResponse.from(atletaRepository.save(atleta));
    }

    public void deactivate(Long id) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ATHLET_NOT_FOUND + id));
        atleta.setAttivo(false);
        atletaRepository.save(atleta);
    }

    public void activate(Long id) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ATHLET_NOT_FOUND + id));
        atleta.setAttivo(true);
        atletaRepository.save(atleta);
    }

    private Atleta mapToEntity(Atleta atleta, AtletaRequest request) {
        atleta.setNome(request.getNome());
        atleta.setCognome(request.getCognome());
        atleta.setDataNascita(request.getDataNascita());
        atleta.setCodiceFiscale(request.getCodiceFiscale());
        atleta.setEmail(request.getEmail());
        atleta.setTelefono(request.getTelefono());
        atleta.setIndirizzo(request.getIndirizzo());
        atleta.setCitta(request.getCitta());
        atleta.setDataIscrizione(request.getDataIscrizione());
        atleta.setCintura(request.getCintura());
        atleta.setTipoCertificato(request.getTipoCertificato());
        atleta.setDataRilascioCertificato(request.getDataRilascioCertificato());
        atleta.setDataScadenzaCertificato(request.getDataScadenzaCertificato());
        atleta.setNote(request.getNote());
        return atleta;
    }
}
