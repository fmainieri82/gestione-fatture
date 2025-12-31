package com.fatture.controller;

import com.fatture.model.Fattura;
import com.fatture.service.FatturaService;
import com.fatture.service.PdfService;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fatture")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"})
public class FatturaController {
    
    @Autowired
    private FatturaService fatturaService;
    
    @Autowired
    private PdfService pdfService;
    
    @GetMapping
    public ResponseEntity<List<Fattura>> getAllFatture() {
        return ResponseEntity.ok(fatturaService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Fattura> getFatturaById(@PathVariable Long id) {
        return fatturaService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/numero/{numeroDocumento}")
    public ResponseEntity<Fattura> getFatturaByNumero(@PathVariable String numeroDocumento) {
        return fatturaService.findByNumeroDocumento(numeroDocumento)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Fattura>> getFattureByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(fatturaService.findByClienteId(clienteId));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Fattura>> searchFatture(@RequestParam String keyword) {
        return ResponseEntity.ok(fatturaService.search(keyword));
    }
    
    @GetMapping("/anno/{anno}")
    public ResponseEntity<List<Fattura>> getFattureByAnno(@PathVariable int anno) {
        return ResponseEntity.ok(fatturaService.findByAnno(anno));
    }
    
    @GetMapping("/periodo")
    public ResponseEntity<List<Fattura>> getFattureByPeriodo(
            @RequestParam String dataInizio,
            @RequestParam String dataFine) {
        LocalDate inizio = LocalDate.parse(dataInizio);
        LocalDate fine = LocalDate.parse(dataFine);
        return ResponseEntity.ok(fatturaService.findByPeriodo(inizio, fine));
    }
    
    @PostMapping
    public ResponseEntity<?> createFattura(@Valid @RequestBody Fattura fattura) {
        try {
            Fattura saved = fatturaService.save(fattura);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (ConstraintViolationException e) {
            Map<String, String> errors = new HashMap<>();
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                String field = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                errors.put(field, message);
            }
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Errore di validazione",
                "details", errors
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Errore durante il salvataggio: " + e.getMessage()
            ));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFattura(
            @PathVariable Long id,
            @Valid @RequestBody Fattura fattura) {
        try {
            return fatturaService.findById(id)
                .map(existing -> {
                    fattura.setId(id);
                    Fattura saved = fatturaService.save(fattura);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (ConstraintViolationException e) {
            Map<String, String> errors = new HashMap<>();
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                String field = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                errors.put(field, message);
            }
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Errore di validazione",
                "details", errors
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Errore durante l'aggiornamento: " + e.getMessage()
            ));
        }
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Errore di validazione",
            "details", errors
        ));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFattura(@PathVariable Long id) {
        return fatturaService.findById(id)
            .map(fattura -> {
                fatturaService.deleteById(id);
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Generazione PDF
    @PostMapping("/{id}/genera-pdf")
    public ResponseEntity<Map<String, String>> generaPdf(@PathVariable Long id) {
        try {
            return fatturaService.findById(id)
                .map(fattura -> {
                    try {
                        String pdfPath = pdfService.generaFatturaPdf(fattura);
                        fattura.setFilePdfPath(pdfPath);
                        fatturaService.save(fattura);
                        
                        return ResponseEntity.ok(Map.of(
                            "message", "PDF generato con successo",
                            "filePath", pdfPath
                        ));
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Errore nella generazione del PDF: " + e.getMessage()));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Download PDF
    @GetMapping("/{id}/download-pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) {
        return fatturaService.findById(id)
            .map(fattura -> {
                if (fattura.getFilePdfPath() == null) {
                    return ResponseEntity.notFound().<Resource>build();
                }
                
                File file = new File(fattura.getFilePdfPath());
                if (!file.exists()) {
                    return ResponseEntity.notFound().<Resource>build();
                }
                
                Resource resource = new FileSystemResource(file);
                
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Aggiorna solo lo stato
    @PatchMapping("/{id}/stato")
    public ResponseEntity<?> updateStato(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String nuovoStato = request.get("stato");
            if (nuovoStato == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Campo 'stato' obbligatorio"));
            }
            
            return fatturaService.findById(id)
                .map(fattura -> {
                    try {
                        fattura.setStato(Fattura.StatoFattura.valueOf(nuovoStato));
                        Fattura saved = fatturaService.save(fattura);
                        return ResponseEntity.ok(saved);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "error", "Stato non valido: " + nuovoStato
                        ));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Errore durante l'aggiornamento dello stato: " + e.getMessage()
            ));
        }
    }
    
    // Statistiche
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistiche() {
        return ResponseEntity.ok(fatturaService.getStatistiche());
    }
}
