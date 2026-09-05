package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.Lezione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LezioneRepository extends JpaRepository<Lezione, Long> {

    List<Lezione> findByCorsoIdOrderByDataAscOraInizioAsc(Long corsoId);

    List<Lezione> findByCorsoIdAndDataBetweenOrderByDataAscOraInizioAsc(Long corsoId, LocalDate from, LocalDate to);

    Optional<Lezione> findByCorsoIdAndDataAndOraInizio(Long corsoId, LocalDate data, LocalTime oraInizio);

    void deleteByCorsoId(Long corsoId);

    /** Lezioni del periodo con il corso gia caricato, per le aggregazioni. */
    @Query("SELECT l FROM Lezione l JOIN FETCH l.corso WHERE l.data BETWEEN :from AND :to")
    List<Lezione> findPerStatistiche(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT l FROM Lezione l JOIN FETCH l.corso c "
            + "WHERE l.data BETWEEN :from AND :to AND c.id = :corsoId")
    List<Lezione> findPerStatistiche(@Param("from") LocalDate from,
                                     @Param("to") LocalDate to,
                                     @Param("corsoId") Long corsoId);
}
