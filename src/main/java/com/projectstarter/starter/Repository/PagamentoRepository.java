package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByAtletaIdOrderByStagioneDescTipoAsc(Long atletaId);

    List<Pagamento> findByAtletaIdAndStagioneAndTipo(Long atletaId, String stagione, Pagamento.Tipo tipo);

    List<Pagamento> findByCorsoId(Long corsoId);

    /** Righe del prospetto: le rate del corso piu le tessere (che non hanno corso). */
    @Query("SELECT p FROM Pagamento p WHERE p.stagione = :stagione AND p.atleta.id IN :atletaIds "
            + "AND (p.corso.id = :corsoId OR p.corso IS NULL)")
    List<Pagamento> findProspetto(@Param("corsoId") Long corsoId,
                                  @Param("stagione") String stagione,
                                  @Param("atletaIds") Collection<Long> atletaIds);

    @Query("SELECT DISTINCT p.stagione FROM Pagamento p ORDER BY p.stagione DESC")
    List<String> findStagioni();
}
