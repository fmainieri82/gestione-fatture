package com.fatture.service;

import com.fatture.model.Fattura;
import com.fatture.repository.FatturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FatturaService {
    
    @Autowired
    private FatturaRepository repository;
    
    public List<Fattura> findAll() {
        return repository.findAll();
    }
    
    public Optional<Fattura> findById(Long id) {
        return repository.findById(id);
    }
    
    public Optional<Fattura> findByNumeroDocumento(String numero) {
        return repository.findByNumeroFattura(numero);
    }
    
    public List<Fattura> findByClienteId(Long clienteId) {
        return repository.findByClienteId(clienteId);
    }
    
    public List<Fattura> findByAnno(int anno) {
        return repository.findByAnno(anno);
    }
    
    public List<Fattura> findByPeriodo(LocalDate inizio, LocalDate fine) {
        return repository.findByDataFatturaBetween(inizio, fine);
    }
    
    public List<Fattura> search(String keyword) {
        return repository.search(keyword);
    }
    
    public Fattura save(Fattura fattura) {
        // Genera numero documento se non presente
        if (fattura.getNumeroDocumento() == null || fattura.getNumeroDocumento().isEmpty()) {
            fattura.setNumeroDocumento(generaNumeroDocumento(fattura));
        }
        
        // Validazione numero duplicato
        if (fattura.getId() == null && repository.existsByNumeroFattura(fattura.getNumeroDocumento())) {
            throw new RuntimeException("Numero documento già esistente");
        }
        
        // Assicura che tutte le voci abbiano il riferimento alla fattura
        // Questo è necessario perché quando arriva dal JSON, le voci non hanno il riferimento
        if (fattura.getVoci() != null) {
            fattura.getVoci().forEach(voce -> {
                if (voce != null) {
                    voce.setFattura(fattura);
                }
            });
        }
        
        return repository.save(fattura);
    }
    
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    public Map<String, Object> getStatistiche() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Fattura> tutte = repository.findAll();
        stats.put("totale", tutte.size());
        
        BigDecimal fatturatoTotale = tutte.stream()
            .map(Fattura::getTotaleDocumento)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("fatturatoTotale", fatturatoTotale);
        
        int anno = LocalDate.now().getYear();
        List<Fattura> annoCorrente = repository.findByAnno(anno);
        stats.put("fattureAnnoCorrente", annoCorrente.size());
        
        BigDecimal fatturatoAnno = annoCorrente.stream()
            .map(Fattura::getTotaleDocumento)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("fatturatoAnnoCorrente", fatturatoAnno);
        
        return stats;
    }
    
    private String generaNumeroDocumento(Fattura fattura) {
        int anno = fattura.getDataDocumento().getYear();
        List<Fattura> fattureAnno = repository.findByAnno(anno);
        
        int progressivo = fattureAnno.size() + 1;
        
        // Formato: 1/2024P (P=Preventivo, F=Fattura, O=Ordine, D=DDT)
        return String.format("%d/%d%s", 
            progressivo, 
            anno,
            fattura.getTipoDocumento().name().substring(0, 1));
    }
}
