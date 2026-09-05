package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Corso;
import com.projectstarter.starter.Entity.CorsoOrario;
import lombok.Data;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Data
public class CorsoResponse {

    private Long id;
    private String nome;
    private String descrizione;
    private String luogo;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private Double quotaMensile;
    private Double quotaTessera;
    private Double quotaRata;
    private Boolean attivo;
    private String note;
    private List<CorsoOrarioResponse> orari;
    private Long numeroIscritti;

    public static CorsoResponse from(Corso corso, Long numeroIscritti) {
        CorsoResponse response = new CorsoResponse();
        response.setId(corso.getId());
        response.setNome(corso.getNome());
        response.setDescrizione(corso.getDescrizione());
        response.setLuogo(corso.getLuogo());
        response.setDataInizio(corso.getDataInizio());
        response.setDataFine(corso.getDataFine());
        response.setQuotaMensile(corso.getQuotaMensile());
        response.setQuotaTessera(corso.getQuotaTessera());
        response.setQuotaRata(corso.getQuotaRata());
        response.setAttivo(corso.getAttivo());
        response.setNote(corso.getNote());
        response.setOrari(corso.getOrari().stream()
                .sorted(Comparator
                        .comparing(CorsoOrario::getGiornoSettimana, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CorsoOrario::getOraInizio, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(CorsoOrarioResponse::from)
                .toList());
        response.setNumeroIscritti(numeroIscritti);
        return response;
    }
}
