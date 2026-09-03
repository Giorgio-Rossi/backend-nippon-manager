package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.CorsoIscrizione;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
