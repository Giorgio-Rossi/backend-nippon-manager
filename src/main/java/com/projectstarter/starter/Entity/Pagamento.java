package com.projectstarter.starter.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Una voce dello storico pagamenti. Nella stagione sportiva ogni atleta ha
 * la tessera associativa annuale (non legata a un corso) e due rate per corso.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pagamenti")
public class Pagamento {

    public enum Tipo {
        /** Tessera associativa annuale: una per atleta e stagione, non legata al corso. */
        TESSERA,
        RATA_1,
        RATA_2,
        ALTRO
    }

    public enum Metodo {
        CONTANTI,
        BONIFICO,
        ALTRO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;

    /** Nullo per la tessera associativa, che non e legata a un corso. */
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corso_id")
    private Corso corso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    /** Stagione sportiva, formato "2026/2027". */
    @Column(nullable = false, length = 9)
    private String stagione;

    private Double importo;

    /** Valorizzata quando il pagamento e stato incassato. */
    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Metodo metodo;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Un pagamento e saldato quando ha una data di pagamento. */
    public boolean isPagato() {
        return dataPagamento != null;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
