package com.projectstarter.starter.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Una singola lezione di un corso: viene generata dalle fasce ricorrenti
 * ({@link CorsoOrario}) oppure inserita a mano.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lezioni",
        uniqueConstraints = @UniqueConstraint(columnNames = {"corso_id", "data", "ora_inizio"}))
public class Lezione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "corso_id", nullable = false)
    private Corso corso;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "ora_inizio")
    private LocalTime oraInizio;

    @Column(name = "ora_fine")
    private LocalTime oraFine;

    @Column(length = 150)
    private String sala;

    @Column(nullable = false)
    private Boolean annullata = false;

    @Column(columnDefinition = "TEXT")
    private String note;

    @PrePersist
    protected void onCreate() {
        if (annullata == null) annullata = false;
    }
}
