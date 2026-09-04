package com.projectstarter.starter.Dto.Response;

import lombok.Data;

import java.util.List;

/** Esito della generazione lezioni, con il messaggio gia pronto per l'utente. */
@Data
public class GenerazioneLezioniResponse {

    private int generate;
    private String messaggio;
    private List<LezioneResponse> lezioni;

    public static GenerazioneLezioniResponse di(List<LezioneResponse> lezioni) {
        GenerazioneLezioniResponse response = new GenerazioneLezioniResponse();
        response.setGenerate(lezioni.size());
        response.setLezioni(lezioni);
        response.setMessaggio(lezioni.isEmpty()
                ? "Nessuna nuova lezione da generare per questo periodo."
                : lezioni.size() + (lezioni.size() == 1 ? " lezione generata." : " lezioni generate."));
        return response;
    }
}
