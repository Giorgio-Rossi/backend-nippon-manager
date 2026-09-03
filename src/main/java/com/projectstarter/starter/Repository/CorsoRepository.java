package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.Corso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorsoRepository extends JpaRepository<Corso, Long> {

    List<Corso> findByAttivoTrue();

    List<Corso> findByAttivoFalse();

    List<Corso> findByNomeContainingIgnoreCase(String nome);
}
