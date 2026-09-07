package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface AtletaRepository extends JpaRepository<Atleta, Long> {

    long countByAttivoTrue();

    /**
     * Gli atleti gia in archivio tra i codici fiscali indicati. L'import Excel
     * risolve tutte le corrispondenze con una sola query invece di interrogare
     * il database una volta per riga del foglio.
     */
    List<Atleta> findByCodiceFiscaleIn(Collection<String> codiciFiscali);

    /**
     * Elenco filtrato e ordinato: il filtro sta nella query e non nel client,
     * cosi la lista completa non deve attraversare la rete a ogni ricerca.
     * Parametri nulli valgono come "nessun filtro".
     *
     * @param q gia normalizzato in minuscolo da {@code Ricerca.normalizza}
     */
    @Query("""
            SELECT a FROM Atleta a
            WHERE (:attivo IS NULL OR a.attivo = :attivo)
              AND (:q IS NULL
                   OR LOWER(CONCAT(a.cognome, ' ', a.nome)) LIKE CONCAT('%', :q, '%')
                   OR LOWER(CONCAT(a.nome, ' ', a.cognome)) LIKE CONCAT('%', :q, '%')
                   OR LOWER(a.codiceFiscale) LIKE CONCAT('%', :q, '%'))
            ORDER BY a.cognome, a.nome
            """)
    List<Atleta> cerca(@Param("attivo") Boolean attivo, @Param("q") String q);

    /**
     * Atleti attivi non ancora iscritti al corso: la differenza tra insiemi la
     * fa il database, non il client che apre la modale di associazione.
     */
    @Query("""
            SELECT a FROM Atleta a
            WHERE a.attivo = true
              AND a.id NOT IN (
                  SELECT i.atleta.id FROM CorsoIscrizione i
                  WHERE i.corso.id = :corsoId AND i.attivo = true)
              AND (:q IS NULL
                   OR LOWER(CONCAT(a.cognome, ' ', a.nome)) LIKE CONCAT('%', :q, '%')
                   OR LOWER(CONCAT(a.nome, ' ', a.cognome)) LIKE CONCAT('%', :q, '%'))
            ORDER BY a.cognome, a.nome
            """)
    List<Atleta> findIscrivibili(@Param("corsoId") Long corsoId, @Param("q") String q);

    /** Certificati gia scaduti o in scadenza entro la soglia, i piu urgenti per primi. */
    @Query("""
            SELECT a FROM Atleta a
            WHERE a.dataScadenzaCertificato IS NOT NULL
              AND a.dataScadenzaCertificato <= :entro
              AND (:soloAttivi = false OR a.attivo = true)
            ORDER BY a.dataScadenzaCertificato ASC, a.cognome ASC
            """)
    List<Atleta> findCertificatiInScadenza(@Param("entro") LocalDate entro,
                                           @Param("soloAttivi") boolean soloAttivi);
}
