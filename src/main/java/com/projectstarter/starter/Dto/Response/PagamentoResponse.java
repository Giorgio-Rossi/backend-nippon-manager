package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Pagamento;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PagamentoResponse {

    private Long id;
    private Long atletaId;
    private String nome;
    private String cognome;
    private Long corsoId;
    private String corsoNome;
    private Pagamento.Tipo tipo;
    private String stagione;
    private Double importo;
    private LocalDate dataPagamento;
    private Pagamento.Metodo metodo;
    private String note;
    private Boolean pagato;

    public static PagamentoResponse from(Pagamento pagamento) {
        PagamentoResponse response = new PagamentoResponse();
        response.setId(pagamento.getId());
        response.setAtletaId(pagamento.getAtleta().getId());
        response.setNome(pagamento.getAtleta().getNome());
        response.setCognome(pagamento.getAtleta().getCognome());
        if (pagamento.getCorso() != null) {
            response.setCorsoId(pagamento.getCorso().getId());
            response.setCorsoNome(pagamento.getCorso().getNome());
        }
        response.setTipo(pagamento.getTipo());
        response.setStagione(pagamento.getStagione());
        response.setImporto(pagamento.getImporto());
        response.setDataPagamento(pagamento.getDataPagamento());
        response.setMetodo(pagamento.getMetodo());
        response.setNote(pagamento.getNote());
        response.setPagato(pagamento.isPagato());
        return response;
    }
}
