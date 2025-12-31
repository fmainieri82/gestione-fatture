package com.fatture.repository;

import com.fatture.model.Fattura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FatturaRepository extends JpaRepository<Fattura, Long> {
    
    Optional<Fattura> findByNumeroFattura(String numeroFattura);
    
    List<Fattura> findByClienteId(Long clienteId);
    
    List<Fattura> findByDataFatturaBetween(LocalDate dataInizio, LocalDate dataFine);
    
    List<Fattura> findByStato(Fattura.StatoFattura stato);
    
    @Query("SELECT f FROM Fattura f WHERE YEAR(f.dataFattura) = ?1 ORDER BY f.numeroFattura DESC")
    List<Fattura> findByAnno(int anno);
    
    @Query("SELECT f FROM Fattura f WHERE f.cliente.ragioneSociale LIKE %?1% OR f.numeroFattura LIKE %?1%")
    List<Fattura> search(String keyword);
    
    boolean existsByNumeroFattura(String numeroFattura);
}
