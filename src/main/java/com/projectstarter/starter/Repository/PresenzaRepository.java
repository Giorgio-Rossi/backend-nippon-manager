package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.Presenza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresenzaRepository extends JpaRepository<Presenza, Long> {

    List<Presenza> findByLezioneId(Long lezioneId);

    Optional<Presenza> findByLezioneIdAndAtletaId(Long lezioneId, Long atletaId);

    void deleteByLezioneId(Long lezioneId);

    @Query("SELECT p FROM Presenza p WHERE p.lezione.corso.id = :corsoId "
            + "AND p.lezione.data BETWEEN :from AND :to")
    List<Presenza> findByCorsoAndPeriodo(@Param("corsoId") Long corsoId,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);

    @Query("SELECT p FROM Presenza p WHERE p.lezione.corso.id = :corsoId")
    List<Presenza> findByCorso(@Param("corsoId") Long corsoId);

    /** Presenze del periodo con lezione e corso gia caricati, per le aggregazioni. */
    @Query("SELECT p FROM Presenza p JOIN FETCH p.lezione l JOIN FETCH l.corso "
            + "WHERE l.data BETWEEN :from AND :to")
    List<Presenza> findPerStatistiche(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT p FROM Presenza p JOIN FETCH p.lezione l JOIN FETCH l.corso c "
            + "WHERE l.data BETWEEN :from AND :to AND c.id = :corsoId")
    List<Presenza> findPerStatistiche(@Param("from") LocalDate from,
                                      @Param("to") LocalDate to,
                                      @Param("corsoId") Long corsoId);
}
