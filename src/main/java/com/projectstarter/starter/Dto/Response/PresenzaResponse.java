package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.Presenza;
import lombok.Data;

@Data
public class PresenzaResponse {

    private Long id;
    private Long lezioneId;
    private Long atletaId;
    private Presenza.Stato stato;
    private String note;

    public static PresenzaResponse from(Presenza presenza) {
        PresenzaResponse response = new PresenzaResponse();
        response.setId(presenza.getId());
        response.setLezioneId(presenza.getLezione().getId());
        response.setAtletaId(presenza.getAtleta().getId());
        response.setStato(presenza.getStato());
        response.setNote(presenza.getNote());
        return response;
    }
}
