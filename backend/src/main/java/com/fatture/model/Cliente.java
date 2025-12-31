package com.fatture.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "clienti")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Ragione sociale obbligatoria")
    @Column(nullable = false)
    private String ragioneSociale;
    
    @NotBlank(message = "Partita IVA obbligatoria")
    @Column(nullable = false, unique = true, length = 11)
    private String partitaIva;
    
    private String codiceFiscale;
    
    @NotBlank(message = "Indirizzo obbligatorio")
    private String indirizzo;
    
    @NotBlank(message = "CAP obbligatorio")
    private String cap;
    
    @NotBlank(message = "Città obbligatoria")
    private String citta;
    
    @NotBlank(message = "Provincia obbligatoria")
    @Column(length = 2)
    private String provincia;
    
    private String telefono;
    
    @Email(message = "Email non valida")
    private String email;
    
    private String pec;
    
    private String codiceSdi;
    
    @Column(updatable = false)
    private LocalDateTime creatoIl;
    
    private LocalDateTime modificatoIl;
    
    @PrePersist
    protected void onCreate() {
        creatoIl = LocalDateTime.now();
        modificatoIl = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        modificatoIl = LocalDateTime.now();
    }
}
