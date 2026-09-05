package com.projectstarter.starter.Service;

import com.projectstarter.starter.Dto.Response.StatisticheIncassiResponse;
import com.projectstarter.starter.Entity.Corso;
import com.projectstarter.starter.Entity.Pagamento;
import com.projectstarter.starter.Repository.CorsoRepository;
import com.projectstarter.starter.Repository.PagamentoRepository;
import com.projectstarter.starter.Dto.Response.AtletaResponse;
import com.projectstarter.starter.Util.Aggregazioni;
import com.projectstarter.starter.Util.Giorni;
import com.projectstarter.starter.Util.Stagioni;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Statistiche degli incassi, di sola lettura: ricalcola tutto dallo storico
 * pagamenti. "Atteso" e la somma degli importi registrati, "incassato" la sola
 * parte con una data di pagamento valorizzata.
 */
@Service
@RequiredArgsConstructor
public class StatisticheIncassiService {

    private final CorsoRepository corsoRepository;
    private final PagamentoRepository pagamentoRepository;

    private static final String CORSO_NOT_FOUND = "Corso non trovato con id: ";
    /** Ambito mostrato quando le statistiche non sono filtrate su un corso. */
    private static final String TUTTI_I_CORSI = "Tutti i corsi";
    /** Riga delle tessere nel dettaglio per corso: non sono legate ad alcun corso. */
    private static final String ETICHETTA_TESSERE = "Tessere associative";
    private static final int MESI_STAGIONE = 12;

    /**
     * @param corsoId se valorizzato considera le sole rate di quel corso: la tessera
     *                associativa e annuale e non e attribuibile a un singolo corso
     */
    public StatisticheIncassiResponse calcola(Long corsoId, String stagione) {
        Corso corso = corsoId == null ? null : getCorso(corsoId);
        String periodo = Stagioni.normalizza(stagione);

        List<Pagamento> pagamenti = corsoId == null
                ? pagamentoRepository.findPerStatistiche(periodo)
                : pagamentoRepository.findPerStatistiche(periodo, corsoId);

        double atteso = pagamenti.stream().mapToDouble(this::importo).sum();
        double incassato = pagamenti.stream().filter(Pagamento::isPagato).mapToDouble(this::importo).sum();
        long saldati = pagamenti.stream().filter(Pagamento::isPagato).count();

        StatisticheIncassiResponse response = new StatisticheIncassiResponse();
        response.setStagione(periodo);
        if (corso != null) {
            response.setCorsoId(corso.getId());
            response.setCorsoNome(corso.getNome());
        }
        response.setAmbito(corso == null ? TUTTI_I_CORSI : corso.getNome());
        response.setTotaleAtteso(Aggregazioni.arrotonda(atteso));
        response.setTotaleIncassato(Aggregazioni.arrotonda(incassato));
        response.setTotaleResiduo(Aggregazioni.arrotonda(atteso - incassato));
        response.setPercentualeIncasso(atteso == 0 ? null : Aggregazioni.arrotonda(incassato / atteso * 100));
        response.setPagamentiTotali(pagamenti.size());
        response.setPagamentiSaldati(saldati);
        response.setPagamentiInSospeso(pagamenti.size() - saldati);
        response.setAtletiCoinvolti(pagamenti.stream().map(p -> p.getAtleta().getId()).distinct().count());
        response.setAtletiMorosi(atletiMorosi(pagamenti));

        response.setPerTipo(perTipo(pagamenti));
        response.setPerMetodo(perMetodo(pagamenti));
        response.setPerCorso(perCorso(pagamenti));
        response.setAndamentoMensile(andamentoMensile(periodo, pagamenti));
        response.setSospesi(sospesi(pagamenti));
        return response;
    }

    private long atletiMorosi(List<Pagamento> pagamenti) {
        Set<Long> morosi = new HashSet<>();
        for (Pagamento pagamento : pagamenti) {
            if (daIncassare(pagamento)) {
                morosi.add(pagamento.getAtleta().getId());
            }
        }
        return morosi.size();
    }

    // ---------- Dettaglio per tipo di quota ----------

    private List<StatisticheIncassiResponse.RigaTipo> perTipo(List<Pagamento> pagamenti) {
        List<StatisticheIncassiResponse.RigaTipo> righe = new ArrayList<>();
        for (Pagamento.Tipo tipo : Pagamento.Tipo.values()) {
            List<Pagamento> gruppo = pagamenti.stream().filter(p -> p.getTipo() == tipo).toList();
            if (gruppo.isEmpty()) {
                // una quota mai registrata non merita una riga a zero
                continue;
            }
            double atteso = gruppo.stream().mapToDouble(this::importo).sum();
            double incassato = gruppo.stream().filter(Pagamento::isPagato).mapToDouble(this::importo).sum();

            StatisticheIncassiResponse.RigaTipo riga = new StatisticheIncassiResponse.RigaTipo();
            riga.setTipo(tipo);
            riga.setTipoLabel(tipo.getLabel());
            riga.setAtteso(Aggregazioni.arrotonda(atteso));
            riga.setIncassato(Aggregazioni.arrotonda(incassato));
            riga.setResiduo(Aggregazioni.arrotonda(atteso - incassato));
            riga.setConteggio(gruppo.size());
            riga.setSaldati(gruppo.stream().filter(Pagamento::isPagato).count());
            righe.add(riga);
        }
        return righe;
    }

    // ---------- Dettaglio per metodo ----------

    private List<StatisticheIncassiResponse.RigaMetodo> perMetodo(List<Pagamento> pagamenti) {
        Map<String, ContatoreIncassi> perMetodo = new LinkedHashMap<>();
        for (Pagamento pagamento : pagamenti) {
            if (!pagamento.isPagato()) {
                continue;
            }
            String metodo = pagamento.getMetodo() == null
                    ? Pagamento.Metodo.NON_SPECIFICATO
                    : pagamento.getMetodo().name();
            perMetodo.computeIfAbsent(metodo, k -> new ContatoreIncassi()).aggiungi(importo(pagamento));
        }

        return perMetodo.entrySet().stream()
                .map(entry -> {
                    StatisticheIncassiResponse.RigaMetodo riga = new StatisticheIncassiResponse.RigaMetodo();
                    riga.setMetodo(entry.getKey());
                    riga.setMetodoLabel(Pagamento.Metodo.labelDi(entry.getKey()));
                    riga.setIncassato(Aggregazioni.arrotonda(entry.getValue().incassato));
                    riga.setConteggio(entry.getValue().conteggio);
                    return riga;
                })
                .sorted(Comparator.comparingDouble(StatisticheIncassiResponse.RigaMetodo::getIncassato).reversed())
                .toList();
    }

    // ---------- Dettaglio per corso ----------

    private List<StatisticheIncassiResponse.RigaCorso> perCorso(List<Pagamento> pagamenti) {
        // chiave 0 = tessere associative, che non sono legate ad alcun corso
        Map<Long, StatisticheIncassiResponse.RigaCorso> righe = new LinkedHashMap<>();

        for (Pagamento pagamento : pagamenti) {
            Corso corso = pagamento.getCorso();
            StatisticheIncassiResponse.RigaCorso riga = righe.computeIfAbsent(
                    corso == null ? 0L : corso.getId(),
                    k -> nuovaRigaCorso(corso));
            riga.setAtteso(riga.getAtteso() + importo(pagamento));
            if (pagamento.isPagato()) {
                riga.setIncassato(riga.getIncassato() + importo(pagamento));
            }
        }

        for (StatisticheIncassiResponse.RigaCorso riga : righe.values()) {
            riga.setResiduo(Aggregazioni.arrotonda(riga.getAtteso() - riga.getIncassato()));
            riga.setAtteso(Aggregazioni.arrotonda(riga.getAtteso()));
            riga.setIncassato(Aggregazioni.arrotonda(riga.getIncassato()));
        }

        return righe.values().stream()
                .sorted(Comparator.comparingDouble(StatisticheIncassiResponse.RigaCorso::getIncassato).reversed())
                .toList();
    }

    private StatisticheIncassiResponse.RigaCorso nuovaRigaCorso(Corso corso) {
        StatisticheIncassiResponse.RigaCorso riga = new StatisticheIncassiResponse.RigaCorso();
        riga.setCorsoId(corso == null ? null : corso.getId());
        riga.setCorsoNome(corso == null ? ETICHETTA_TESSERE : corso.getNome());
        return riga;
    }

    // ---------- Andamento mensile ----------

    private List<StatisticheIncassiResponse.PuntoMensile> andamentoMensile(String stagione, List<Pagamento> pagamenti) {
        Map<YearMonth, ContatoreIncassi> perMese = new LinkedHashMap<>();
        for (YearMonth mese : Aggregazioni.mesi(
                Stagioni.dataInizio(stagione), Stagioni.dataFine(stagione), MESI_STAGIONE)) {
            perMese.put(mese, new ContatoreIncassi());
        }
        for (Pagamento pagamento : pagamenti) {
            if (!pagamento.isPagato()) {
                continue;
            }
            // un incasso registrato fuori dai mesi della stagione finisce comunque nel grafico
            perMese.computeIfAbsent(YearMonth.from(pagamento.getDataPagamento()), k -> new ContatoreIncassi())
                    .aggiungi(importo(pagamento));
        }

        return perMese.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    StatisticheIncassiResponse.PuntoMensile punto = new StatisticheIncassiResponse.PuntoMensile();
                    punto.setMese(entry.getKey().toString());
                    punto.setMeseLabel(Giorni.meseBreve(entry.getKey()));
                    punto.setIncassato(Aggregazioni.arrotonda(entry.getValue().incassato));
                    punto.setConteggio(entry.getValue().conteggio);
                    return punto;
                })
                .toList();
    }

    // ---------- Quote ancora da incassare ----------

    private List<StatisticheIncassiResponse.RigaSospeso> sospesi(List<Pagamento> pagamenti) {
        return pagamenti.stream()
                .filter(this::daIncassare)
                .map(this::rigaSospeso)
                .sorted(Comparator
                        .comparing(StatisticheIncassiResponse.RigaSospeso::getCognome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(StatisticheIncassiResponse.RigaSospeso::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private StatisticheIncassiResponse.RigaSospeso rigaSospeso(Pagamento pagamento) {
        StatisticheIncassiResponse.RigaSospeso riga = new StatisticheIncassiResponse.RigaSospeso();
        riga.setPagamentoId(pagamento.getId());
        riga.setAtletaId(pagamento.getAtleta().getId());
        riga.setNome(pagamento.getAtleta().getNome());
        riga.setCognome(pagamento.getAtleta().getCognome());
        riga.setNominativo(AtletaResponse.nominativo(pagamento.getAtleta()));
        if (pagamento.getCorso() != null) {
            riga.setCorsoId(pagamento.getCorso().getId());
            riga.setCorsoNome(pagamento.getCorso().getNome());
        }
        riga.setTipo(pagamento.getTipo());
        riga.setTipoLabel(pagamento.getTipo().getLabel());
        riga.setImporto(Aggregazioni.arrotonda(importo(pagamento)));
        return riga;
    }

    // ---------- Helpers ----------

    private Corso getCorso(Long id) {
        return corsoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CORSO_NOT_FOUND + id));
    }

    /** Una voce a importo zero non e un credito: resta fuori dai sospesi. */
    private boolean daIncassare(Pagamento pagamento) {
        return !pagamento.isPagato() && importo(pagamento) > 0;
    }

    private double importo(Pagamento pagamento) {
        return pagamento.getImporto() == null ? 0d : pagamento.getImporto();
    }

    private static final class ContatoreIncassi {

        private double incassato;
        private long conteggio;

        void aggiungi(double importo) {
            incassato += importo;
            conteggio++;
        }
    }
}
