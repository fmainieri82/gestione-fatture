package com.fatture.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fatture")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fattura {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Numero fattura obbligatorio")
    @Column(nullable = false, unique = true)
    private String numeroFattura;
    
    @NotNull(message = "Data fattura obbligatoria")
    @Column(nullable = false)
    private LocalDate dataFattura;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @OneToMany(mappedBy = "fattura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoceFattura> voci = new ArrayList<>();
    
    @Column(precision = 10, scale = 2)
    private BigDecimal imponibile;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal aliquotaIva; // Percentuale IVA (es. 22.00)
    
    @Column(precision = 10, scale = 2)
    private BigDecimal importoIva;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totale;
    
    private String note;
    
    @Column(name = "file_path")
    private String filePath; // Percorso del file DOCX generato
    
    @Column(name = "file_pdf_path")
    private String filePdfPath; // Percorso del file PDF generato
    
    @Enumerated(EnumType.STRING)
    private StatoFattura stato = StatoFattura.BOZZA;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento")
    private TipoDocumento tipoDocumento = TipoDocumento.FATTURA;
    
    // Dati emittente
    @Column(name = "ragione_sociale_emittente")
    private String ragioneSocialeEmittente;
    
    @Column(name = "sede_legale_emittente")
    private String sedeLegaleEmittente;
    
    @Column(name = "sede_operativa_emittente")
    private String sedeOperativaEmittente;
    
    @Column(name = "partita_iva_emittente")
    private String partitaIvaEmittente;
    
    @Column(name = "codice_univoco_emittente")
    private String codiceUnivocoEmittente;
    
    @Column(name = "iban_emittente")
    private String ibanEmittente;
    
    @Column(name = "telefono_emittente")
    private String telefonoEmittente;
    
    @Column(name = "email_emittente")
    private String emailEmittente;
    
    // Sede di consegna
    @Column(name = "sede_consegna_indirizzo")
    private String sedeConsegnaIndirizzo;
    
    @Column(name = "sede_consegna_cap")
    private String sedeConsegnaCap;
    
    @Column(name = "sede_consegna_citta")
    private String sedeConsegnaCitta;
    
    @Column(name = "sede_consegna_provincia")
    private String sedeConsegnaProvincia;
    
    // Sconti e spese
    @Column(name = "totale_righe", precision = 10, scale = 2)
    private BigDecimal totaleRighe;
    
    @Column(name = "sconti_maggiori", precision = 10, scale = 2)
    private BigDecimal scontiMaggiori;
    
    @Column(name = "spese_trasporto", precision = 10, scale = 2)
    private BigDecimal speseTrasporto;
    
    // Modalità spedizione
    @Column(name = "modalita_spedizione")
    private String modalitaSpedizione;
    
    @Column(name = "porto")
    private String porto;
    
    @Column(name = "condizione_consegna")
    private String condizioneConsegna;
    
    @Column(updatable = false)
    private LocalDateTime creatoIl;
    
    private LocalDateTime modificatoIl;
    
    public enum StatoFattura {
        BOZZA,
        EMESSA,
        PAGATA,
        ANNULLATA
    }
    
    public enum TipoDocumento {
        PREVENTIVO("Preventivo"),
        FATTURA("Fattura"),
        ORDINE("Ordine"),
        DDT("DDT");
        
        private final String descrizione;
        
        TipoDocumento(String descrizione) {
            this.descrizione = descrizione;
        }
        
        public String getDescrizione() {
            return descrizione;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        creatoIl = LocalDateTime.now();
        modificatoIl = LocalDateTime.now();
        // Inizializza voci se null
        if (voci == null) {
            voci = new ArrayList<>();
        }
        calcolaTotali();
    }
    
    @PreUpdate
    protected void onUpdate() {
        modificatoIl = LocalDateTime.now();
        calcolaTotali();
    }
    
    public void calcolaTotali() {
        // Inizializza sempre i valori per evitare null
        if (voci == null) {
            voci = new ArrayList<>();
        }
        
        if (!voci.isEmpty()) {
            totaleRighe = voci.stream()
                .filter(v -> v != null && v.getQuantita() != null && v.getPrezzoUnitario() != null)
                .map(v -> v.getQuantita().multiply(v.getPrezzoUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (totaleRighe == null) {
                totaleRighe = BigDecimal.ZERO;
            }
            
            // Calcola imponibile dopo sconti
            imponibile = totaleRighe;
            if (scontiMaggiori != null && scontiMaggiori.compareTo(BigDecimal.ZERO) > 0) {
                imponibile = imponibile.subtract(scontiMaggiori);
            }
            
            if (imponibile == null) {
                imponibile = BigDecimal.ZERO;
            }
            
            if (aliquotaIva != null) {
                importoIva = imponibile.multiply(aliquotaIva)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            } else {
                importoIva = BigDecimal.ZERO;
            }
            
            totale = imponibile.add(importoIva);
            if (speseTrasporto != null && speseTrasporto.compareTo(BigDecimal.ZERO) > 0) {
                totale = totale.add(speseTrasporto);
            }
        } else {
            totaleRighe = BigDecimal.ZERO;
            imponibile = BigDecimal.ZERO;
            importoIva = BigDecimal.ZERO;
            totale = BigDecimal.ZERO;
        }
        
        // Assicura che tutti i BigDecimal siano inizializzati
        if (totaleRighe == null) totaleRighe = BigDecimal.ZERO;
        if (imponibile == null) imponibile = BigDecimal.ZERO;
        if (importoIva == null) importoIva = BigDecimal.ZERO;
        if (totale == null) totale = BigDecimal.ZERO;
        if (scontiMaggiori == null) scontiMaggiori = BigDecimal.ZERO;
        if (speseTrasporto == null) speseTrasporto = BigDecimal.ZERO;
    }
    
    // Metodi alias per compatibilità
    public String getNumeroDocumento() {
        return numeroFattura;
    }
    
    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroFattura = numeroDocumento;
    }
    
    public LocalDate getDataDocumento() {
        return dataFattura;
    }
    
    public void setDataDocumento(LocalDate dataDocumento) {
        this.dataFattura = dataDocumento;
    }
    
    public BigDecimal getTotaleDocumento() {
        return totale;
    }
    
    public void addVoce(VoceFattura voce) {
        voci.add(voce);
        voce.setFattura(this);
    }
    
    public void removeVoce(VoceFattura voce) {
        voci.remove(voce);
        voce.setFattura(null);
    }
}
