package com.projectstarter.starter.Dto.Request;

import com.projectstarter.starter.Entity.Pagamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PagamentoRequest {

    @NotNull
    private Long atletaId;

    /** Obbligatorio per le rate, deve restare nullo per la tessera associativa. */
    private Long corsoId;

    @NotNull
    private Pagamento.Tipo tipo;

    /** Formato "2026/2027". Se omessa viene usata la stagione corrente. */
    @Size(max = 9)
    private String stagione;

    @PositiveOrZero
    private Double importo;

    private LocalDate dataPagamento;

    private Pagamento.Metodo metodo;

    private String note;
}
