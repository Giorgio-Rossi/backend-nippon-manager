package com.projectstarter.starter.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "atleti")
public class Atleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String cognome;

    @Column(name = "data_nascita")
    private LocalDate dataNascita;

    @Column(name = "codice_fiscale", unique = true, length = 16)
    private String codiceFiscale;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String indirizzo;

    @Column(length = 100)
    private String citta;

    @Column(name = "data_iscrizione")
    private LocalDate dataIscrizione;

    @Column(name = "tipo_certificato", length = 50)
    private String tipoCertificato;

    @Column(name = "data_rilascio_certificato")
    private LocalDate dataRilascioCertificato;

    @Column(name = "data_scadenza_certificato")
    private LocalDate dataScadenzaCertificato;

    @Column(nullable = false)
    private Boolean attivo = true;

    @Column(columnDefinition = "TEXT")
    private String note;

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
}
