package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.Corso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorsoRepository extends JpaRepository<Corso, Long> {

    long countByAttivoTrue();

    /**
     * Elenco filtrato e ordinato. Parametri nulli valgono come "nessun filtro".
     *
     * @param q gia normalizzato in minuscolo da {@code Ricerca.normalizza}
     */
    @Query("""
            SELECT c FROM Corso c
            WHERE (:attivo IS NULL OR c.attivo = :attivo)
              AND (:q IS NULL
                   OR LOWER(c.nome) LIKE CONCAT('%', :q, '%')
                   OR LOWER(c.luogo) LIKE CONCAT('%', :q, '%'))
            ORDER BY c.nome
            """)
    List<Corso> cerca(@Param("attivo") Boolean attivo, @Param("q") String q);
}
