package com.fatture.service;

import com.fatture.model.Cliente;
import com.fatture.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    
    @Autowired
    private ClienteRepository repository;
    
    public List<Cliente> findAll() {
        return repository.findAll();
    }
    
    public Optional<Cliente> findById(Long id) {
        return repository.findById(id);
    }
    
    public Optional<Cliente> findByPartitaIva(String partitaIva) {
        return repository.findByPartitaIva(partitaIva);
    }
    
    public List<Cliente> search(String keyword) {
        return repository.findByRagioneSocialeContainingIgnoreCase(keyword);
    }
    
    public Cliente save(Cliente cliente) {
        // Validazione partita IVA duplicata
        if (cliente.getId() == null && repository.existsByPartitaIva(cliente.getPartitaIva())) {
            throw new RuntimeException("Partita IVA già esistente");
        }
        return repository.save(cliente);
    }
    
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
