package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Atleta;
import lombok.Data;

import java.util.List;

/**
 * Il prospetto pagamenti di un corso per una stagione: una riga per atleta e,
 * come colonne, tessera associativa e le due rate.
 */
@Data
public class ProspettoPagamentiResponse {

    private Long corsoId;
    private String corsoNome;
    private String stagione;
    /** Importi suggeriti impostati sul corso, usati per precompilare le celle. */
    private Double quotaTessera;
    private Double quotaRata;
    private List<Riga> atleti;
    private List<PagamentoResponse> pagamenti;
    private Double totaleIncassato;
    private Double totaleAtteso;

    @Data
    public static class Riga {

        private Long atletaId;
        private String nome;
        private String cognome;
        private String cintura;
        private Boolean iscrizioneAttiva;

        public static Riga from(Atleta atleta, boolean iscrizioneAttiva) {
            Riga riga = new Riga();
            riga.setAtletaId(atleta.getId());
            riga.setNome(atleta.getNome());
            riga.setCognome(atleta.getCognome());
            riga.setCintura(atleta.getCintura());
            riga.setIscrizioneAttiva(iscrizioneAttiva);
            return riga;
        }
    }
}
