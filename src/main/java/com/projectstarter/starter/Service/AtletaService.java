package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Request.AtletaRequest;
import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Repository.AtletaRepository;
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
    public List<AtletaResponse> findAll(Boolean attivo) {
        List<Atleta> atleti;
        if (attivo == null) {
            atleti = atletaRepository.findAll();
        } else if (attivo) {
            atleti = atletaRepository.findByAttivoTrue();
        } else {
            atleti = atletaRepository.findByAttivoFalse();
        }
        return atleti.stream().map(AtletaResponse::from).toList();
    }

    public AtletaResponse findById(Long id) {
        Atleta atleta = atletaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ATHLET_NOT_FOUND + id));
        return AtletaResponse.from(atleta);
    }

    public List<AtletaResponse> search(String q) {
        return atletaRepository.findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(q, q)
                .stream().map(AtletaResponse::from).toList();
    }

    public List<AtletaResponse> findExpiringCertificates(int daysAhead) {
        LocalDate threshold = LocalDate.now().plusDays(daysAhead);
        return atletaRepository.findByDataScadenzaCertificatoBefore(threshold)
                .stream().map(AtletaResponse::from).toList();
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
        atleta.setTipoCertificato(request.getTipoCertificato());
        atleta.setDataRilascioCertificato(request.getDataRilascioCertificato());
        atleta.setDataScadenzaCertificato(request.getDataScadenzaCertificato());
        atleta.setNote(request.getNote());
        return atleta;
    }
}
