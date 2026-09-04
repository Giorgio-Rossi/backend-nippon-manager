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
    private String nominativo;
    private Long corsoId;
    private String corsoNome;
    private Pagamento.Tipo tipo;
    private String tipoLabel;
    private String stagione;
    private Double importo;
    private LocalDate dataPagamento;
    private Pagamento.Metodo metodo;
    private String metodoLabel;
    private String note;
    private Boolean pagato;

    public static PagamentoResponse from(Pagamento pagamento) {
        PagamentoResponse response = new PagamentoResponse();
        response.setId(pagamento.getId());
        response.setAtletaId(pagamento.getAtleta().getId());
        response.setNome(pagamento.getAtleta().getNome());
        response.setCognome(pagamento.getAtleta().getCognome());
        response.setNominativo(AtletaResponse.nominativo(pagamento.getAtleta()));
        if (pagamento.getCorso() != null) {
            response.setCorsoId(pagamento.getCorso().getId());
            response.setCorsoNome(pagamento.getCorso().getNome());
        }
        response.setTipo(pagamento.getTipo());
        response.setTipoLabel(pagamento.getTipo().getLabel());
        response.setStagione(pagamento.getStagione());
        response.setImporto(pagamento.getImporto());
        response.setDataPagamento(pagamento.getDataPagamento());
        response.setMetodo(pagamento.getMetodo());
        response.setMetodoLabel(Pagamento.Metodo.labelDi(
                pagamento.getMetodo() == null ? null : pagamento.getMetodo().name()));
        response.setNote(pagamento.getNote());
        response.setPagato(pagamento.isPagato());
        return response;
    }
}
