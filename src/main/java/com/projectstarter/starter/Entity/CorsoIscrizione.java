package com.projectstarter.starter.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

/**
 * Associazione fra un atleta censito e un corso.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "corsi_iscrizioni",
        uniqueConstraints = @UniqueConstraint(columnNames = {"corso_id", "atleta_id"}))
public class CorsoIscrizione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corso_id", nullable = false)
    private Corso corso;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;

    @Column(name = "data_iscrizione")
    private LocalDate dataIscrizione;

    @Column(name = "data_disiscrizione")
    private LocalDate dataDisiscrizione;

    @Column(nullable = false)
    private Boolean attivo = true;

    @Column(columnDefinition = "TEXT")
    private String note;

    @PrePersist
    protected void onCreate() {
        if (attivo == null) attivo = true;
        if (dataIscrizione == null) dataIscrizione = LocalDate.now();
    }
}
