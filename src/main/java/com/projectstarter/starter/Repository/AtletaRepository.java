package com.projectstarter.starter.Repository;

import com.projectstarter.starter.Entity.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AtletaRepository extends JpaRepository<Atleta, Long> {

    List<Atleta> findByAttivoTrue();

    List<Atleta> findByAttivoFalse();

    List<Atleta> findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(String nome, String cognome);

    List<Atleta> findByDataScadenzaCertificatoBefore(LocalDate data);
}
