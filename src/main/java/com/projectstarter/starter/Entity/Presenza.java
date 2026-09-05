package com.projectstarter.starter.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Stato di presenza di un atleta a una lezione. Compilata a mano dal foglio presenze.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "presenze",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lezione_id", "atleta_id"}))
public class Presenza {

    /**
     * Lo stato di una cella del foglio. Una cella non compilata non ha una riga
     * a database: e l'assenza di {@code Presenza}, non un terzo valore.
     */
    public enum Stato {

        PRESENTE("Presente", "P"),
        ASSENTE("Assente", "A");

        private final String label;
        private final String sigla;

        Stato(String label, String sigla) {
            this.label = label;
            this.sigla = sigla;
        }

        public String getLabel() {
            return label;
        }

        /** La lettera mostrata nella cella del foglio presenze. */
        public String getSigla() {
            return sigla;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lezione_id", nullable = false)
    private Lezione lezione;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Stato stato;

    @Column(columnDefinition = "TEXT")
    private String note;
}
