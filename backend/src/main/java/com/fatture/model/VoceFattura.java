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
    
    // Scheda di sopralluogo
    @Column(name = "cerniera", length = 10)
    private String cerniera; // DX, SX
    
    @Column(name = "pompa_scarico")
    private Boolean pompaScarico; // true = SI, false = NO
    
    @Column(name = "tensione", length = 10)
    private String tensione; // 220M, 220T, 380T
    
    @Column(name = "allacci_distanti")
    private Boolean allacciDistanti; // true = SI, false = NO
    
    @Column(name = "ruote")
    private Boolean ruote; // true = SI, false = NO
    
    @Column(name = "smaltimento")
    private Boolean smaltimento; // true = SI, false = NO
    
    @Column(name = "necessario_sopralluogo")
    private Boolean necessarioSopralluogo; // true = SI, false = NO
    
    @Column(name = "addolcitore_corrente", length = 20)
    private String addolcitoreCorrente; // Automatico, Normale
    
    @Column(name = "passaggio_cm")
    private Integer passaggioCm;
    
    @Column(name = "scale", columnDefinition = "TEXT")
    private String scale; // testo libero
    
    @Column(name = "macchina_da_smontare")
    private Boolean macchinaDaSmontare; // true = SI, false = NO
    
    @Column(name = "misure", columnDefinition = "TEXT")
    private String misure; // testo libero (se macchina da smontare = SI)
    
    @Column(name = "gas", length = 10)
    private String gas; // Metano, GPL
    
    @Column(name = "gas_distanza_cm")
    private Integer gasDistanzaCm;
    
    @Column(name = "parcheggio")
    private Boolean parcheggio; // true = SI, false = NO
    
    @Column(name = "giorno_ora_consegna")
    private String giornoOraConsegna; // data e ora
    
    @PrePersist
    @PreUpdate
    protected void calcolaImporto() {
        if (quantita != null && prezzoUnitario != null) {
            importo = quantita.multiply(prezzoUnitario)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}
