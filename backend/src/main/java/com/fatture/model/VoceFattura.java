package com.fatture.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "voci_fattura")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoceFattura {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fattura_id", nullable = false)
    @JsonIgnore
    private Fattura fattura;
    
    @NotBlank(message = "Descrizione obbligatoria")
    @Column(nullable = false, length = 1000)
    private String descrizione;
    
    @NotNull(message = "Quantità obbligatoria")
    @Positive(message = "Quantità deve essere positiva")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal quantita;
    
    @Column(length = 10)
    private String unitaMisura = "pz"; // es: pz, kg, ore, ecc.
    
    @NotNull(message = "Prezzo unitario obbligatorio")
    @PositiveOrZero(message = "Prezzo deve essere positivo o zero")
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal prezzoUnitario;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal importo;
    
    @Column(name = "dettagli_tecnici", columnDefinition = "TEXT")
    private String dettagliTecnici;
    
    @Column(name = "codice_iva")
    private Integer codiceIva;
    
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
    
    @PrePersist
    @PreUpdate
    protected void calcolaImporto() {
        if (quantita != null && prezzoUnitario != null) {
            importo = quantita.multiply(prezzoUnitario)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}
