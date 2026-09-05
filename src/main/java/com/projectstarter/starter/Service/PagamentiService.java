package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Request.PagamentoRequest;
import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Dto.Response.PagamentoResponse;
import com.projectstarter.starter.Dto.Response.ProspettoPagamentiResponse;
import com.projectstarter.starter.Dto.Response.StagioniResponse;
import com.projectstarter.starter.Entity.Atleta;
import com.projectstarter.starter.Entity.Corso;
import com.projectstarter.starter.Entity.CorsoIscrizione;
import com.projectstarter.starter.Entity.Pagamento;
import com.projectstarter.starter.Repository.AtletaRepository;
import com.projectstarter.starter.Repository.CorsoIscrizioneRepository;
import com.projectstarter.starter.Repository.CorsoRepository;
import com.projectstarter.starter.Repository.PagamentoRepository;
import com.projectstarter.starter.Util.Aggregazioni;
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
    public StagioniResponse stagioni() {
        Set<String> stagioni = new LinkedHashSet<>(Stagioni.intorno(4));
        stagioni.addAll(pagamentoRepository.findStagioni());
        return StagioniResponse.di(
                stagioni.stream().sorted(Comparator.reverseOrder()).toList(),
                Stagioni.corrente());
    }

    /**
     * Il prospetto gia impaginato: colonne, righe con una cella per colonna e
     * riepilogo. Tutte le somme e gli stati sono calcolati qui, cosi il client
     * non deve incrociare atleti e pagamenti per disegnare la tabella.
     */
    public ProspettoPagamentiResponse prospetto(Long corsoId, String stagione) {
        Corso corso = getCorso(corsoId);
        String periodo = Stagioni.normalizza(stagione);

        Map<Long, Atleta> atleti = new LinkedHashMap<>();
        Set<Long> iscrittiAttivi = new LinkedHashSet<>();
        for (CorsoIscrizione iscrizione : iscrizioneRepository.findByCorsoIdAndAttivoTrue(corsoId)) {
            Atleta atleta = iscrizione.getAtleta();
            atleti.put(atleta.getId(), atleta);
            iscrittiAttivi.add(atleta.getId());
        }

        List<Pagamento> pagamenti = atleti.isEmpty()
                ? List.of()
                : pagamentoRepository.findProspetto(corsoId, periodo, atleti.keySet());

        // Un atleta disiscritto durante la stagione resta in elenco se ha pagamenti registrati
        for (Pagamento pagamento : pagamenti) {
            atleti.putIfAbsent(pagamento.getAtleta().getId(), pagamento.getAtleta());
        }

        List<ProspettoPagamentiResponse.Colonna> colonne = colonne(corso);
        Map<Long, Map<Pagamento.Tipo, Pagamento>> perAtleta = indicizza(pagamenti);

        List<ProspettoPagamentiResponse.Riga> righe = atleti.values().stream()
                .map(atleta -> riga(atleta, iscrittiAttivi.contains(atleta.getId()), colonne,
                        perAtleta.getOrDefault(atleta.getId(), Map.of())))
                .sorted(Comparator
                        .comparing(ProspettoPagamentiResponse.Riga::getCognome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ProspettoPagamentiResponse.Riga::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        ProspettoPagamentiResponse response = new ProspettoPagamentiResponse();
        response.setCorsoId(corso.getId());
        response.setCorsoNome(corso.getNome());
        response.setStagione(periodo);
        response.setColonne(colonne);
        response.setAtleti(righe);
        response.setRiepilogo(riepilogo(righe, colonne.size(), pagamenti));
        response.setQuoteImpostate(corso.getQuotaTessera() != null || corso.getQuotaRata() != null);
        return response;
    }

    private List<ProspettoPagamentiResponse.Colonna> colonne(Corso corso) {
        return Pagamento.Tipo.colonneProspetto().stream()
                .map(tipo -> ProspettoPagamentiResponse.Colonna.di(tipo, quotaSuggerita(corso, tipo)))
                .toList();
    }

    /** La tessera usa la quota associativa, le rate quella di rata. */
    private Double quotaSuggerita(Corso corso, Pagamento.Tipo tipo) {
        return tipo == Pagamento.Tipo.TESSERA ? corso.getQuotaTessera() : corso.getQuotaRata();
    }

    /** I pagamenti indicizzati per atleta e tipo: una cella non ha piu di una voce. */
    private Map<Long, Map<Pagamento.Tipo, Pagamento>> indicizza(List<Pagamento> pagamenti) {
        Map<Long, Map<Pagamento.Tipo, Pagamento>> indice = new LinkedHashMap<>();
        for (Pagamento pagamento : pagamenti) {
            indice.computeIfAbsent(pagamento.getAtleta().getId(), k -> new LinkedHashMap<>())
                    .putIfAbsent(pagamento.getTipo(), pagamento);
        }
        return indice;
    }

    private ProspettoPagamentiResponse.Riga riga(Atleta atleta,
                                                 boolean iscrizioneAttiva,
                                                 List<ProspettoPagamentiResponse.Colonna> colonne,
                                                 Map<Pagamento.Tipo, Pagamento> pagamenti) {
        List<ProspettoPagamentiResponse.Cella> celle = colonne.stream()
                .map(colonna -> cella(colonna, pagamenti.get(colonna.getTipo())))
                .toList();

        ProspettoPagamentiResponse.Riga riga = new ProspettoPagamentiResponse.Riga();
        riga.setAtletaId(atleta.getId());
        riga.setNome(atleta.getNome());
        riga.setCognome(atleta.getCognome());
        riga.setNominativo(AtletaResponse.nominativo(atleta));
        riga.setCintura(atleta.getCintura());
        riga.setIscrizioneAttiva(iscrizioneAttiva);
        riga.setCelle(celle);
        riga.setTotale(Aggregazioni.arrotonda(celle.stream()
                .filter(c -> c.getStato() == ProspettoPagamentiResponse.StatoCella.SALDATA)
                .mapToDouble(c -> c.getImporto() == null ? 0d : c.getImporto())
                .sum()));
        return riga;
    }

    private ProspettoPagamentiResponse.Cella cella(ProspettoPagamentiResponse.Colonna colonna, Pagamento pagamento) {
        ProspettoPagamentiResponse.Cella cella = new ProspettoPagamentiResponse.Cella();
        cella.setTipo(colonna.getTipo());
        cella.setTipoLabel(colonna.getLabel());
        cella.setImportoSuggerito(colonna.getImportoSuggerito());

        ProspettoPagamentiResponse.StatoCella stato;
        if (pagamento == null) {
            stato = ProspettoPagamentiResponse.StatoCella.VUOTA;
        } else {
            stato = pagamento.isPagato()
                    ? ProspettoPagamentiResponse.StatoCella.SALDATA
                    : ProspettoPagamentiResponse.StatoCella.DA_SALDARE;
            cella.setPagamentoId(pagamento.getId());
            cella.setImporto(pagamento.getImporto());
            cella.setDataPagamento(pagamento.getDataPagamento());
            cella.setMetodo(pagamento.getMetodo());
            cella.setMetodoLabel(Pagamento.Metodo.labelDi(
                    pagamento.getMetodo() == null ? null : pagamento.getMetodo().name()));
            cella.setNote(pagamento.getNote());
            cella.setImportoDiversoDaQuota(importoDiversoDaQuota(
                    pagamento.getImporto(), colonna.getImportoSuggerito()));
        }
        cella.setStato(stato);
        cella.setStatoLabel(stato.getLabel());
        return cella;
    }

    private boolean importoDiversoDaQuota(Double importo, Double quota) {
        return importo != null && quota != null && Double.compare(importo, quota) != 0;
    }

    private ProspettoPagamentiResponse.Riepilogo riepilogo(List<ProspettoPagamentiResponse.Riga> righe,
                                                           int numeroColonne,
                                                           List<Pagamento> pagamenti) {
        double atteso = pagamenti.stream().mapToDouble(this::importo).sum();
        double incassato = pagamenti.stream().filter(Pagamento::isPagato).mapToDouble(this::importo).sum();

        ProspettoPagamentiResponse.Riepilogo riepilogo = new ProspettoPagamentiResponse.Riepilogo();
        riepilogo.setAtteso(Aggregazioni.arrotonda(atteso));
        riepilogo.setIncassato(Aggregazioni.arrotonda(incassato));
        riepilogo.setDaIncassare(Aggregazioni.arrotonda(atteso - incassato));
        riepilogo.setCelleTotali((long) righe.size() * numeroColonne);
        riepilogo.setCelleCompilate(conta(righe, ProspettoPagamentiResponse.StatoCella.VUOTA, false));
        riepilogo.setCelleSaldate(conta(righe, ProspettoPagamentiResponse.StatoCella.SALDATA, true));
        return riepilogo;
    }

    private long conta(List<ProspettoPagamentiResponse.Riga> righe,
                       ProspettoPagamentiResponse.StatoCella stato,
                       boolean uguale) {
        return righe.stream()
                .flatMap(riga -> riga.getCelle().stream())
                .filter(cella -> (cella.getStato() == stato) == uguale)
                .count();
    }

    private double importo(Pagamento pagamento) {
        return pagamento.getImporto() == null ? 0d : pagamento.getImporto();
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
                Stagioni.normalizza(request.getStagione()),
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

    private void mapToEntity(Pagamento pagamento, PagamentoRequest request) {
        Atleta atleta = atletaRepository.findById(request.getAtletaId())
                .orElseThrow(() -> new EntityNotFoundException(ATLETA_NOT_FOUND + request.getAtletaId()));
        pagamento.setAtleta(atleta);
        pagamento.setTipo(request.getTipo());
        pagamento.setStagione(Stagioni.normalizza(request.getStagione()));

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
