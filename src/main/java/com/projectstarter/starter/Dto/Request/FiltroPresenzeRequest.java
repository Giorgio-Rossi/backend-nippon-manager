package com.projectstarter.starter.Dto.Request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Filtri delle statistiche presenze, legati dai parametri di query. Raggrupparli
 * evita una firma di sette argomenti e rende indolore aggiungerne altri.
 */
@Data
public class FiltroPresenzeRequest {

    /** Nullo per aggregare tutti i corsi. */
    private Long corsoId;

    /** Formato "2026/2027"; in mancanza si usa la stagione corrente. */
    private String stagione;

    /** Nome di {@code Periodi.Preset}; in mancanza vale l'intera stagione. */
    private String periodo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    /**
     * Giorno della settimana in ISO-8601 (1 = lunedi ... 7 = domenica): restringe
     * i conteggi alle sole lezioni tenute in quel giorno. Nullo per tutti i giorni.
     */
    private Integer giorno;

    /** Filtro sul nominativo, applicato alla sola classifica atleti. */
    private String q;

    /** Nome di {@code Statistiche.Ordine}; in mancanza dal piu presente. */
    private String ordine;
}
