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
import java.util.List;

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

    /**
     * Le colonne del prospetto pagamenti. L'etichetta sta qui e non nel client
     * perche descrive il dominio: rinominare una quota resta una decisione sola.
     */
    public enum Tipo {

        /** Tessera associativa annuale: una per atleta e stagione, non legata al corso. */
        TESSERA("Tessera associativa", "Tessera", true),
        RATA_1("1ª rata", "1ª rata", false),
        RATA_2("2ª rata", "2ª rata", false),
        /** Voce libera fuori dal prospetto: puo ripetersi nella stessa stagione. */
        ALTRO("Altro", "Altro", false);

        private final String label;
        private final String breve;
        private final boolean annuale;

        Tipo(String label, String breve, boolean annuale) {
            this.label = label;
            this.breve = breve;
            this.annuale = annuale;
        }

        public String getLabel() {
            return label;
        }

        public String getBreve() {
            return breve;
        }

        /** true se la quota vale per l'atleta e non per il singolo corso. */
        public boolean isAnnuale() {
            return annuale;
        }

        /** Le colonne del prospetto, nell'ordine in cui vanno mostrate. */
        public static List<Tipo> colonneProspetto() {
            return List.of(TESSERA, RATA_1, RATA_2);
        }
    }

    public enum Metodo {

        CONTANTI("Contanti"),
        BONIFICO("Bonifico"),
        ALTRO("Altro");

        /** Chiave con cui le statistiche raggruppano gli incassi registrati senza metodo. */
        public static final String NON_SPECIFICATO = "NON_SPECIFICATO";
        private static final String LABEL_NON_SPECIFICATO = "Non specificato";

        private final String label;

        Metodo(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        /** Etichetta a partire dal nome dell'enum, {@link #NON_SPECIFICATO} compreso. */
        public static String labelDi(String nome) {
            return nome == null || NON_SPECIFICATO.equals(nome)
                    ? LABEL_NON_SPECIFICATO
                    : valueOf(nome).getLabel();
        }
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
