package com.projectstarter.starter.Dto.Response;

import lombok.Data;

import java.util.List;

/**
 * I dati della home in una sola risposta. Prima il client scaricava tre elenchi
 * interi per contarne le righe: i conteggi arrivano dal database, che li fa in
 * una query invece che trasferendo tutti gli atleti.
 */
@Data
public class DashboardResponse {

    private long atletiTotali;
    private long atletiAttivi;
    private long corsiAttivi;

    /** Finestra di preavviso usata per l'elenco, in giorni. */
    private int giorniPreavviso;
    private long certificatiInScadenza;
    /** Certificati scaduti o in scadenza, i piu urgenti per primi. */
    private List<AtletaResponse> certificati;
}
