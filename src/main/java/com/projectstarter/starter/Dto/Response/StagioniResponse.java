package com.projectstarter.starter.Dto.Response;

import lombok.Data;

import java.util.List;

/**
 * Le stagioni selezionabili e quella da preselezionare. La stagione corrente
 * la decide il server: dipende da quando inizia l'anno sportivo, non
 * dall'orologio del browser.
 */
@Data
public class StagioniResponse {

    private List<String> stagioni;
    private String corrente;

    public static StagioniResponse di(List<String> stagioni, String corrente) {
        StagioniResponse response = new StagioniResponse();
        response.setStagioni(stagioni);
        // se la corrente non e tra quelle disponibili si ripiega sulla prima
        response.setCorrente(stagioni.contains(corrente)
                ? corrente
                : stagioni.stream().findFirst().orElse(corrente));
        return response;
    }
}
