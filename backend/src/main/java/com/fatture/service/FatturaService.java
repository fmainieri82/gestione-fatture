package com.fatture.service;

import com.fatture.model.Fattura;
import com.fatture.repository.FatturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Transactional
    public Fattura save(Fattura fattura) {
        // Genera numero documento se non presente
        if (fattura.getNumeroDocumento() == null || fattura.getNumeroDocumento().isEmpty()) {
            fattura.setNumeroDocumento(generaNumeroDocumento(fattura));
        }
        
        // Validazione numero duplicato (solo per nuove fatture, non per aggiornamenti)
        if (fattura.getId() == null) {
            // Se il numero generato esiste già, rigenera fino a trovarne uno disponibile
            String numeroOriginale = fattura.getNumeroDocumento();
            int tentativi = 0;
            int maxTentativi = 100; // Limite di sicurezza per evitare loop infiniti
            
            while (repository.existsByNumeroFattura(fattura.getNumeroDocumento()) && tentativi < maxTentativi) {
                // Rigenera il numero incrementando il progressivo
                fattura.setNumeroDocumento(generaNumeroDocumentoIncrementale(fattura, numeroOriginale, tentativi + 1));
                tentativi++;
            }
            
            if (tentativi >= maxTentativi) {
                throw new RuntimeException("Impossibile generare un numero documento univoco dopo " + maxTentativi + " tentativi");
            }
        } else {
            // Per aggiornamenti, verifica che il numero non sia duplicato con altre fatture
            Optional<Fattura> esistente = repository.findByNumeroFattura(fattura.getNumeroDocumento());
            if (esistente.isPresent() && !esistente.get().getId().equals(fattura.getId())) {
                throw new RuntimeException("Numero documento già esistente per un'altra fattura");
            }
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
        int anno = fattura.getDataDocumento() != null 
            ? fattura.getDataDocumento().getYear() 
            : LocalDate.now().getYear();
        
        String tipoSuffisso = fattura.getTipoDocumento().name().substring(0, 1);
        
        // Trova il progressivo massimo per l'anno e tipo documento
        // Cerca tutte le fatture dell'anno che terminano con il suffisso del tipo
        List<Fattura> fattureAnno = repository.findByAnno(anno);
        int progressivoMassimo = 0;
        
        for (Fattura f : fattureAnno) {
            if (f.getNumeroDocumento() != null && f.getNumeroDocumento().endsWith(tipoSuffisso)) {
                try {
                    // Estrai il progressivo dal numero (es: "1/2024P" -> 1)
                    String[] parts = f.getNumeroDocumento().split("/");
                    if (parts.length > 0) {
                        int progressivo = Integer.parseInt(parts[0]);
                        if (progressivo > progressivoMassimo) {
                            progressivoMassimo = progressivo;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignora numeri malformati
                }
            }
        }
        
        int progressivo = progressivoMassimo + 1;
        
        // Formato: 1/2024P (P=Preventivo, F=Fattura, O=Ordine, D=DDT)
        return String.format("%d/%d%s", 
            progressivo, 
            anno,
            tipoSuffisso);
    }
    
    /**
     * Genera un numero documento incrementale quando il numero originale è già esistente
     */
    private String generaNumeroDocumentoIncrementale(Fattura fattura, String numeroBase, int incremento) {
        int anno = fattura.getDataDocumento() != null 
            ? fattura.getDataDocumento().getYear() 
            : LocalDate.now().getYear();
        
        String tipoSuffisso = fattura.getTipoDocumento().name().substring(0, 1);
        
        // Estrai il progressivo dal numero base (es: "1/2024P" -> 1)
        int progressivoBase = 1;
        try {
            String[] parts = numeroBase.split("/");
            if (parts.length > 0) {
                progressivoBase = Integer.parseInt(parts[0]);
            }
        } catch (NumberFormatException e) {
            // Se non riesce a parsare, usa 1 come default
        }
        
        int progressivo = progressivoBase + incremento;
        
        return String.format("%d/%d%s", progressivo, anno, tipoSuffisso);
    }
}
