package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.Lezione;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
