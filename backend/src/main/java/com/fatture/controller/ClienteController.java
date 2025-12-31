package com.fatture.controller;

import com.fatture.model.Cliente;
import com.fatture.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clienti")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"})
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;
    
    @GetMapping
    public ResponseEntity<List<Cliente>> getAllClienti() {
        return ResponseEntity.ok(clienteService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        return clienteService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/partita-iva/{partitaIva}")
    public ResponseEntity<Cliente> getClienteByPartitaIva(@PathVariable String partitaIva) {
        return clienteService.findByPartitaIva(partitaIva)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Cliente>> searchClienti(@RequestParam String keyword) {
        return ResponseEntity.ok(clienteService.search(keyword));
    }
    
    @PostMapping
    public ResponseEntity<?> createCliente(@Valid @RequestBody Cliente cliente) {
        try {
            Cliente saved = clienteService.save(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Errore durante il salvataggio: " + e.getMessage()
            ));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCliente(
            @PathVariable Long id,
            @Valid @RequestBody Cliente cliente) {
        try {
            return clienteService.findById(id)
                .map(existing -> {
                    cliente.setId(id);
                    Cliente saved = clienteService.save(cliente);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        return clienteService.findById(id)
            .map(cliente -> {
                clienteService.deleteById(id);
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
