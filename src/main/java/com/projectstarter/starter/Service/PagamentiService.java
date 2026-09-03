package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Request.PagamentoRequest;
import com.projectstarter.starter.Dto.Response.PagamentoResponse;
import com.projectstarter.starter.Dto.Response.ProspettoPagamentiResponse;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Entity.Corso;
import com.projectstarter.starter.Entity.CorsoIscrizione;
import com.projectstarter.starter.Entity.Pagamento;
import com.projectstarter.starter.Repository.AtletaRepository;
import com.projectstarter.starter.Repository.CorsoIscrizioneRepository;
import com.projectstarter.starter.Repository.CorsoRepository;
import com.projectstarter.starter.Repository.PagamentoRepository;
import com.projectstarter.starter.Util.Stagioni;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PagamentiService {

    private final PagamentoRepository pagamentoRepository;
    private final CorsoRepository corsoRepository;
    private final CorsoIscrizioneRepository iscrizioneRepository;
    private final AtletaRepository atletaRepository;

    private static final String PAGAMENTO_NOT_FOUND = "Pagamento non trovato con id: ";
    private static final String CORSO_NOT_FOUND = "Corso non trovato con id: ";
    private static final String ATLETA_NOT_FOUND = "Atleta non trovato con id: ";

    // ---------- Letture ----------

    public List<PagamentoResponse> findByAtleta(Long atletaId) {
        if (!atletaRepository.existsById(atletaId)) {
            throw new EntityNotFoundException(ATLETA_NOT_FOUND + atletaId);
        }
        return pagamentoRepository.findByAtletaIdOrderByStagioneDescTipoAsc(atletaId)
                .stream().map(PagamentoResponse::from).toList();
    }

    public PagamentoResponse findById(Long id) {
        return PagamentoResponse.from(getPagamento(id));
    }

    /** Stagioni gia usate nei pagamenti piu quelle intorno a oggi, senza duplicati. */
    public List<String> stagioni() {
        Set<String> stagioni = new LinkedHashSet<>(Stagioni.intorno(4));
        stagioni.addAll(pagamentoRepository.findStagioni());
        return stagioni.stream().sorted(Comparator.reverseOrder()).toList();
    }

    public ProspettoPagamentiResponse prospetto(Long corsoId, String stagione) {
        Corso corso = getCorso(corsoId);
        String periodo = normalizzaStagione(stagione);

        Map<Long, ProspettoPagamentiResponse.Riga> righe = new LinkedHashMap<>();
        for (CorsoIscrizione iscrizione : iscrizioneRepository.findByCorsoIdAndAttivoTrue(corsoId)) {
            Atleta atleta = iscrizione.getAtleta();
            righe.put(atleta.getId(), ProspettoPagamentiResponse.Riga.from(atleta, true));
        }

        List<Pagamento> pagamenti = righe.isEmpty()
                ? List.of()
                : pagamentoRepository.findProspetto(corsoId, periodo, righe.keySet());

        // Un atleta disiscritto durante la stagione resta in elenco se ha pagamenti registrati
        for (Pagamento pagamento : pagamenti) {
            Atleta atleta = pagamento.getAtleta();
            righe.computeIfAbsent(atleta.getId(), k -> ProspettoPagamentiResponse.Riga.from(atleta, false));
        }

        double incassato = pagamenti.stream()
                .filter(Pagamento::isPagato)
                .mapToDouble(p -> p.getImporto() == null ? 0d : p.getImporto())
                .sum();
        double atteso = pagamenti.stream()
                .mapToDouble(p -> p.getImporto() == null ? 0d : p.getImporto())
                .sum();

        ProspettoPagamentiResponse response = new ProspettoPagamentiResponse();
        response.setCorsoId(corso.getId());
        response.setCorsoNome(corso.getNome());
        response.setStagione(periodo);
        response.setQuotaTessera(corso.getQuotaTessera());
        response.setQuotaRata(corso.getQuotaRata());
        response.setAtleti(righe.values().stream()
                .sorted(Comparator
                        .comparing(ProspettoPagamentiResponse.Riga::getCognome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ProspettoPagamentiResponse.Riga::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList());
        response.setPagamenti(pagamenti.stream().map(PagamentoResponse::from).toList());
        response.setTotaleIncassato(incassato);
        response.setTotaleAtteso(atteso);
        return response;
    }

    // ---------- Scritture ----------

    @Transactional
    public PagamentoResponse create(PagamentoRequest request) {
        Pagamento pagamento = new Pagamento();
        mapToEntity(pagamento, request);
        verificaDuplicato(pagamento, null);
        return PagamentoResponse.from(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public PagamentoResponse update(Long id, PagamentoRequest request) {
        Pagamento pagamento = getPagamento(id);
        mapToEntity(pagamento, request);
        verificaDuplicato(pagamento, id);
        return PagamentoResponse.from(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public void delete(Long id) {
        pagamentoRepository.delete(getPagamento(id));
    }

    /**
     * Crea o aggiorna in un colpo solo la cella del prospetto identificata da
     * atleta + tipo + stagione (+ corso per le rate).
     */
    @Transactional
    public PagamentoResponse salvaCella(PagamentoRequest request) {
        Pagamento esistente = trovaEsistente(
                request.getAtletaId(),
                request.getTipo(),
                normalizzaStagione(request.getStagione()),
                request.getTipo() == Pagamento.Tipo.TESSERA ? null : request.getCorsoId(),
                null);
        return esistente == null ? create(request) : update(esistente.getId(), request);
    }

    // ---------- Helpers ----------

    private Pagamento getPagamento(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PAGAMENTO_NOT_FOUND + id));
    }

    private Corso getCorso(Long id) {
        return corsoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CORSO_NOT_FOUND + id));
    }

    private String normalizzaStagione(String stagione) {
        if (stagione == null || stagione.isBlank()) {
            return Stagioni.corrente();
        }
        if (!Stagioni.valida(stagione)) {
            throw new IllegalArgumentException("Stagione non valida: attesa nel formato 2026/2027.");
        }
        return stagione;
    }

    private void mapToEntity(Pagamento pagamento, PagamentoRequest request) {
        Atleta atleta = atletaRepository.findById(request.getAtletaId())
                .orElseThrow(() -> new EntityNotFoundException(ATLETA_NOT_FOUND + request.getAtletaId()));
        pagamento.setAtleta(atleta);
        pagamento.setTipo(request.getTipo());
        pagamento.setStagione(normalizzaStagione(request.getStagione()));

        if (request.getTipo() == Pagamento.Tipo.TESSERA) {
            // La tessera associativa e annuale e vale per l'atleta, non per il singolo corso
            pagamento.setCorso(null);
        } else if (request.getCorsoId() == null) {
            throw new IllegalArgumentException("Il corso e obbligatorio per le rate.");
        } else {
            pagamento.setCorso(getCorso(request.getCorsoId()));
        }

        pagamento.setImporto(request.getImporto());
        pagamento.setDataPagamento(request.getDataPagamento());
        pagamento.setMetodo(request.getMetodo());
        pagamento.setNote(request.getNote());
    }

    /** Tessera e rate sono uniche per atleta, stagione e corso; ALTRO puo ripetersi. */
    private void verificaDuplicato(Pagamento pagamento, Long idCorrente) {
        if (pagamento.getTipo() == Pagamento.Tipo.ALTRO) {
            return;
        }
        Long corsoId = pagamento.getCorso() == null ? null : pagamento.getCorso().getId();
        Pagamento duplicato = trovaEsistente(
                pagamento.getAtleta().getId(), pagamento.getTipo(), pagamento.getStagione(), corsoId, idCorrente);
        if (duplicato != null) {
            throw new IllegalArgumentException(
                    "Esiste gia un pagamento di tipo " + pagamento.getTipo()
                            + " per questo atleta nella stagione " + pagamento.getStagione() + ".");
        }
    }

    private Pagamento trovaEsistente(Long atletaId, Pagamento.Tipo tipo, String stagione,
                                     Long corsoId, Long idDaEscludere) {
        if (tipo == Pagamento.Tipo.ALTRO) {
            return null;
        }
        List<Pagamento> candidati = new ArrayList<>(
                pagamentoRepository.findByAtletaIdAndStagioneAndTipo(atletaId, stagione, tipo));
        for (Pagamento candidato : candidati) {
            if (idDaEscludere != null && idDaEscludere.equals(candidato.getId())) {
                continue;
            }
            Long candidatoCorso = candidato.getCorso() == null ? null : candidato.getCorso().getId();
            if (Objects.equals(candidatoCorso, corsoId)) {
                return candidato;
            }
        }
        return null;
    }
}
