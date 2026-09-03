package com.projectstarter.starter.Dto.Request;

import com.projectstarter.starter.Entity.Presenza;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Salvataggio in blocco del foglio presenze. Uno {@code stato} nullo
 * cancella la registrazione (cella lasciata vuota).
 */
@Data
public class PresenzeBulkRequest {

    @Valid
    private List<Record> records = new ArrayList<>();

    @Data
    public static class Record {

        @NotNull
        private Long lezioneId;

        @NotNull
        private Long atletaId;

        private Presenza.Stato stato;

        private String note;
    }
}
