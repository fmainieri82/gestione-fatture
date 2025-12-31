package com.fatture.repository;

import com.fatture.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByPartitaIva(String partitaIva);
    
    List<Cliente> findByRagioneSocialeContainingIgnoreCase(String ragioneSociale);
    
    boolean existsByPartitaIva(String partitaIva);
}
