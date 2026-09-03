package com.projectstarter.starter.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalTime;

/**
 * Una fascia oraria ricorrente settimanale di un corso
 * (es. Lunedi 18:30-20:00). I giorni sono ISO: 1 = Lunedi ... 7 = Domenica.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "corsi_orari")
public class CorsoOrario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corso_id", nullable = false)
    private Corso corso;

    @Column(name = "giorno_settimana", nullable = false)
    private Integer giornoSettimana;

    @Column(name = "ora_inizio", nullable = false)
    private LocalTime oraInizio;

    @Column(name = "ora_fine", nullable = false)
    private LocalTime oraFine;

    @Column(length = 150)
    private String sala;
}
