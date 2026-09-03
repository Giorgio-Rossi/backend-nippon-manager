package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Atleta;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Il foglio presenze di un corso in un periodo: le colonne (lezioni),
 * le righe (atleti) e le celle gia compilate (presenze).
 */
@Data
public class FoglioPresenzeResponse {

    private Long corsoId;
    private String corsoNome;
    private LocalDate from;
    private LocalDate to;
    private List<LezioneResponse> lezioni;
    private List<Riga> atleti;
    private List<PresenzaResponse> presenze;

    @Data
    public static class Riga {

        private Long atletaId;
        private String nome;
        private String cognome;
        private String cintura;
        /** false se l'atleta e stato disiscritto ma ha presenze storiche nel periodo. */
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
