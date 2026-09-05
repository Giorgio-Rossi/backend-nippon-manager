package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.CorsoIscrizione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorsoIscrizioneRepository extends JpaRepository<CorsoIscrizione, Long> {

    List<CorsoIscrizione> findByCorsoId(Long corsoId);

    List<CorsoIscrizione> findByCorsoIdAndAttivoTrue(Long corsoId);

    List<CorsoIscrizione> findByAtletaId(Long atletaId);

    Optional<CorsoIscrizione> findByCorsoIdAndAtletaId(Long corsoId, Long atletaId);

    long countByCorsoIdAndAttivoTrue(Long corsoId);

    void deleteByCorsoId(Long corsoId);

    /** Iscritti attivi di ogni corso, per non interrogare il DB corso per corso. */
    @Query("SELECT i.corso.id AS corsoId, COUNT(i) AS totale FROM CorsoIscrizione i "
            + "WHERE i.attivo = true GROUP BY i.corso.id")
    List<ConteggioIscritti> countIscrittiAttiviPerCorso();

    /** Projection: evita gli {@code Object[]} e i cast che ne derivano. */
    interface ConteggioIscritti {
        Long getCorsoId();

        long getTotale();
    }
}
