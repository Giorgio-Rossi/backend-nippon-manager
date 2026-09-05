package com.projectstarter.starter.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "corsi")
public class Corso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @Column(length = 150)
    private String luogo;

    @Column(name = "data_inizio")
    private LocalDate dataInizio;

    @Column(name = "data_fine")
    private LocalDate dataFine;

    @Column(name = "quota_mensile")
    private Double quotaMensile;

    /** Tessera associativa annuale, importo suggerito nel prospetto pagamenti. */
    @Column(name = "quota_tessera")
    private Double quotaTessera;

    /** Importo suggerito di ciascuna delle due rate stagionali. */
    @Column(name = "quota_rata")
    private Double quotaRata;

    @Column(nullable = false)
    private Boolean attivo = true;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "corso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CorsoOrario> orari = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (attivo == null) attivo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addOrario(CorsoOrario orario) {
        orario.setCorso(this);
        this.orari.add(orario);
    }
}
