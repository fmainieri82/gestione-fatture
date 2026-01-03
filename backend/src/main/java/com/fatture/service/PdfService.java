package com.fatture.service;

import com.fatture.model.Cliente;
import com.fatture.model.Fattura;
import com.fatture.model.VoceFattura;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {
    
    @Value("${app.output.path:fatture/}")
    private String outputPath;
    
    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_HEADER = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font FONT_SMALL = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
    
    // Colori personalizzati
    private static final BaseColor GRAY_BACKGROUND = new BaseColor(240, 240, 240); // Grigio chiaro per i campi
    private static final BaseColor GRAY_BORDER = new BaseColor(128, 128, 128); // Grigio per i bordi
    private static final BaseColor LIGHT_GRAY_BORDER = new BaseColor(200, 200, 200); // Grigio più chiaro per i bordi delle merci
    private static final BaseColor DARK_GRAY_BACKGROUND = new BaseColor(220, 220, 220); // Grigio più scuro per header
    private static final BaseColor NOTE_BACKGROUND = new BaseColor(250, 250, 250); // Grigio molto chiaro per le note
    
    private final DecimalFormat currencyFormat;
    private final DateTimeFormatter dateFormatter;
    
    public PdfService() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ITALY);
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        currencyFormat = new DecimalFormat("#,##0.00", symbols);
        dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }
    
    public String generaFatturaPdf(Fattura fattura) throws Exception {
        // Crea directory se non esiste
        Path dirPath = Paths.get(outputPath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        // Nome file
        String fileName = String.format("%s_%s.pdf", 
            fattura.getTipoDocumento().name(),
            fattura.getNumeroDocumento().replace("/", "_"));
        String filePath = outputPath + fileName;
        
        // Crea documento
        Document document = new Document(PageSize.A4, 30, 30, 30, 50);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
        
        document.open();
        
        // Header azienda
        aggiungiHeaderAzienda(document, fattura);
        
        // Titolo documento
        aggiungiTitoloDocumento(document, fattura);
        
        // Dati cliente e intestazione
        aggiungiDatiCliente(document, fattura);
        
        // Tabella informazioni documento
        aggiungiInfoDocumento(document, fattura);
        
        // Tabella voci
        aggiungiTabellaVoci(document, fattura);
        
        // Note e condizioni (prima dei totali)
        aggiungiNoteCondizioni(document, fattura);
        
        // Totali con pagamento (in fondo alla pagina)
        aggiungiTotali(document, writer, fattura);
        
        // Footer
        aggiungiFooter(document, fattura);
        
        document.close();
        
        return filePath;
    }
    
    public String generaFatturaPdfConSopralluogo(Fattura fattura) throws Exception {
        // Crea directory se non esiste
        Path dirPath = Paths.get(outputPath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        // Nome file
        String fileName = String.format("%s_%s_SOPRALLUOGO.pdf", 
            fattura.getTipoDocumento().name(),
            fattura.getNumeroDocumento().replace("/", "_"));
        String filePath = outputPath + fileName;
        
        // Crea documento con margine inferiore aumentato per spazio in fondo
        Document document = new Document(PageSize.A4, 30, 30, 30, 50);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
        
        document.open();
        
        // Aggiungi contenuto (stesso del PDF normale)
        aggiungiHeaderAzienda(document, fattura);
        aggiungiTitoloDocumento(document, fattura);
        aggiungiDatiCliente(document, fattura);
        aggiungiInfoDocumento(document, fattura);
        aggiungiTabellaVociConSopralluogo(document, fattura);
        aggiungiTotali(document, writer, fattura);
        
        document.close();
        
        return filePath;
    }
    
    private void aggiungiHeaderAzienda(Document document, Fattura fattura) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 70});
        table.setLockedWidth(false); // Permette il ridimensionamento automatico
        
        // Logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        logoCell.setPadding(10);
        logoCell.setVerticalAlignment(Element.ALIGN_TOP);
        logoCell.setFixedHeight(60); // Altezza fissa per la cella
        
        try {
            // Carica il logo dal classpath
            Image logo = getImageFromResource("logo.png");
            // Ridimensiona il logo mantenendo le proporzioni (max altezza 60px, max larghezza 200px)
            logo.scaleToFit(200, 60);
            logo.setAlignment(Image.ALIGN_LEFT);
            logoCell.addElement(logo);
        } catch (Exception e) {
            // Fallback in caso di errore nel caricamento del logo
            logoCell.addElement(new Phrase("LOGO", FONT_TITLE));
        }
        
        table.addCell(logoCell);
        
        // Dati azienda
        String datiAzienda = String.format(
            "%s\nSede legale: %s\nSede Operativa: %s\nP.IVA/C.F.: %s\nCodice Univoco: %s\nIBAN: %s\nTelefono: %s\nE-mail: %s",
            fattura.getRagioneSocialeEmittente(),
            fattura.getSedeLegaleEmittente(),
            fattura.getSedeOperativaEmittente(),
            fattura.getPartitaIvaEmittente(),
            fattura.getCodiceUnivocoEmittente(),
            fattura.getIbanEmittente(),
            fattura.getTelefonoEmittente(),
            fattura.getEmailEmittente()
        );
        
        PdfPCell datiCell = new PdfPCell(new Phrase(datiAzienda, FONT_SMALL));
        datiCell.setBorder(Rectangle.NO_BORDER);
        datiCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        datiCell.setPadding(10);
        table.addCell(datiCell);
        
        document.add(table);
        document.add(new Paragraph(" "));
    }
    
    private void aggiungiTitoloDocumento(Document document, Fattura fattura) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setLockedWidth(false);
        
        // Titolo
        PdfPCell titleCell = new PdfPCell(new Phrase(fattura.getTipoDocumento().getDescrizione(), FONT_TITLE));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        titleCell.setPadding(5);
        table.addCell(titleCell);
        
        document.add(table);
        document.add(new Paragraph(" "));
    }
    
    private void aggiungiDatiCliente(Document document, Fattura fattura) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50, 50});
        
        Cliente cliente = fattura.getCliente();
        
        // Sede di consegna
        String sedeConsegna = fattura.getSedeConsegnaIndirizzo() != null 
            ? String.format("%s\n%s %s (%s) - Italia", 
                fattura.getSedeConsegnaIndirizzo(),
                fattura.getSedeConsegnaCap(),
                fattura.getSedeConsegnaCitta(),
                fattura.getSedeConsegnaProvincia())
            : "-";
        
        PdfPCell sedeCell = new PdfPCell();
        sedeCell.addElement(new Phrase("Sede di consegna", FONT_HEADER));
        sedeCell.addElement(new Phrase(sedeConsegna, FONT_NORMAL));
        table.addCell(sedeCell);
        
        // Intestatario documento
        String intestatario = String.format("%s\n%s %s (%s) - Italia",
            cliente.getRagioneSociale(),
            cliente.getCap(),
            cliente.getCitta(),
            cliente.getProvincia());
        
        PdfPCell intestatarioCell = new PdfPCell();
        intestatarioCell.addElement(new Phrase("Intestatario documento", FONT_HEADER));
        intestatarioCell.addElement(new Phrase(intestatario, FONT_NORMAL));
        table.addCell(intestatarioCell);
        
        document.add(table);
        document.add(new Paragraph(" "));
    }
    
    private void aggiungiInfoDocumento(Document document, Fattura fattura) throws DocumentException {
        Cliente cliente = fattura.getCliente();
        
        // Prima riga: Codice fiscale, Tipo documento, Numero documento, Data doc. (rimosso Codice cliente perché ridondante con Intestatario documento)
        PdfPTable table1 = new PdfPTable(4);
        table1.setWidthPercentage(100);
        table1.setWidths(new float[]{20, 30, 30, 20});
        table1.setLockedWidth(false);
        
        // Label sopra, valore sotto nella stessa cella
        addCellLabelValueVertical(table1, "Codice fiscale", cliente.getCodiceFiscale() != null ? cliente.getCodiceFiscale() : "");
        addCellLabelValueVertical(table1, "Tipo documento", fattura.getTipoDocumento().getDescrizione());
        addCellLabelValueVertical(table1, "Numero documento", fattura.getNumeroDocumento());
        addCellLabelValueVertical(table1, "Data doc.", fattura.getDataDocumento().format(dateFormatter));
        
        document.add(table1);
        
        // Seconda riga: Vostra banca, Nostra banca, BIC, Pagina (rimossa riga Valuta/Agente/Del perché ridondante)
        PdfPTable table3 = new PdfPTable(4);
        table3.setWidthPercentage(100);
        table3.setWidths(new float[]{25, 50, 15, 10});
        table3.setLockedWidth(false);
        
        // Label sopra, valore sotto nella stessa cella
        addCellLabelValueVertical(table3, "Vostra banca", "");
        String nostraBanca = fattura.getIbanEmittente() != null ? fattura.getIbanEmittente() : "";
        String bancaCompleta = fattura.getRagioneSocialeEmittente() != null ? fattura.getRagioneSocialeEmittente() + " " : "";
        bancaCompleta += nostraBanca;
        addCellLabelValueVertical(table3, "Nostra banca", bancaCompleta);
        // Estrai BIC dall'IBAN se presente (formato standard: ITXX XXXX XXXX...)
        String bic = "ROMAITRRXXX"; // Default
        if (nostraBanca.length() > 8) {
            bic = nostraBanca.substring(4, 8) + "ITRRXXX";
        }
        addCellLabelValueVertical(table3, "BIC", bic);
        addCellLabelValueVertical(table3, "Pagina", "1");
        
        document.add(table3);
        document.add(new Paragraph(" "));
    }
    
    private void aggiungiTabellaVoci(Document document, Fattura fattura) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 8, 8, 12, 12, 8, 22});
        
            // Headers con solo bordi orizzontali
            PdfPCell header1 = new PdfPCell(new Phrase("Descrizione merce o servizio", FONT_SMALL));
            styleHeaderCellNoVerticalBorders(header1);
            header1.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(header1);
            
            PdfPCell header2 = new PdfPCell(new Phrase("U.M.", FONT_SMALL));
            styleHeaderCellNoVerticalBorders(header2);
            header2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(header2);
            
            PdfPCell header3 = new PdfPCell(new Phrase("Quantità", FONT_SMALL));
            styleHeaderCellNoVerticalBorders(header3);
            header3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(header3);
            
            PdfPCell header4 = new PdfPCell(new Phrase("Prezzo", FONT_SMALL));
            styleHeaderCellNoVerticalBorders(header4);
            header4.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(header4);
            
            PdfPCell header5 = new PdfPCell(new Phrase("Importo", FONT_SMALL));
            styleHeaderCellNoVerticalBorders(header5);
            header5.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(header5);
            
            PdfPCell header6 = new PdfPCell(new Phrase("C.I.", FONT_SMALL));
            styleHeaderCellNoVerticalBorders(header6);
            header6.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(header6);
            
            PdfPCell header7 = new PdfPCell(new Phrase("Evasione", FONT_SMALL));
            styleHeaderCellNoVerticalBorders(header7);
            header7.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(header7);
        
        // Voci
        String dataEvasione = fattura.getDataDocumento() != null 
            ? fattura.getDataDocumento().format(dateFormatter) 
            : "";
        
        for (VoceFattura voce : fattura.getVoci()) {
            boolean hasNote = voce.getNote() != null && !voce.getNote().trim().isEmpty();
            
            // Descrizione (può essere multi-linea)
            String descrizioneCompleta = voce.getDescrizione();
            if (voce.getDettagliTecnici() != null && !voce.getDettagliTecnici().isEmpty()) {
                descrizioneCompleta += "\n" + voce.getDettagliTecnici();
            }
            
            PdfPCell descCell = new PdfPCell(new Phrase(descrizioneCompleta, FONT_SMALL));
            descCell.setPadding(5);
            descCell.setBorderWidthTop(1.0f);
            descCell.setBorderWidthBottom(hasNote ? 0 : 1.0f); // Nessun bordo inferiore se ci sono note
            descCell.setBorderWidthLeft(0);
            descCell.setBorderWidthRight(0);
            descCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote) {
                descCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            descCell.setBackgroundColor(BaseColor.WHITE);
            table.addCell(descCell);
            
            // Celle dati con solo bordi orizzontali e grigio più chiaro
            PdfPCell umCell = new PdfPCell(new Phrase(voce.getUnitaMisura(), FONT_SMALL));
            umCell.setPadding(5);
            umCell.setBorderWidthTop(1.0f);
            umCell.setBorderWidthBottom(hasNote ? 0 : 1.0f);
            umCell.setBorderWidthLeft(0);
            umCell.setBorderWidthRight(0);
            umCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote) {
                umCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            umCell.setBackgroundColor(BaseColor.WHITE);
            umCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(umCell);
            
            PdfPCell qtyCell = new PdfPCell(new Phrase(formatNumber(voce.getQuantita()), FONT_SMALL));
            qtyCell.setPadding(5);
            qtyCell.setBorderWidthTop(1.0f);
            qtyCell.setBorderWidthBottom(hasNote ? 0 : 1.0f);
            qtyCell.setBorderWidthLeft(0);
            qtyCell.setBorderWidthRight(0);
            qtyCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote) {
                qtyCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            qtyCell.setBackgroundColor(BaseColor.WHITE);
            qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(qtyCell);
            
            PdfPCell priceCell = new PdfPCell(new Phrase(formatCurrency(voce.getPrezzoUnitario()), FONT_SMALL));
            priceCell.setPadding(5);
            priceCell.setBorderWidthTop(1.0f);
            priceCell.setBorderWidthBottom(hasNote ? 0 : 1.0f);
            priceCell.setBorderWidthLeft(0);
            priceCell.setBorderWidthRight(0);
            priceCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote) {
                priceCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            priceCell.setBackgroundColor(BaseColor.WHITE);
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(priceCell);
            
            PdfPCell importoCell = new PdfPCell(new Phrase(formatCurrency(voce.getImporto()), FONT_SMALL));
            importoCell.setPadding(5);
            importoCell.setBorderWidthTop(1.0f);
            importoCell.setBorderWidthBottom(hasNote ? 0 : 1.0f);
            importoCell.setBorderWidthLeft(0);
            importoCell.setBorderWidthRight(0);
            importoCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote) {
                importoCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            importoCell.setBackgroundColor(BaseColor.WHITE);
            importoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(importoCell);
            
            PdfPCell ivaCell = new PdfPCell(new Phrase(voce.getCodiceIva() != null ? voce.getCodiceIva().toString() : "22", FONT_SMALL));
            ivaCell.setPadding(5);
            ivaCell.setBorderWidthTop(1.0f);
            ivaCell.setBorderWidthBottom(hasNote ? 0 : 1.0f);
            ivaCell.setBorderWidthLeft(0);
            ivaCell.setBorderWidthRight(0);
            ivaCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote) {
                ivaCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            ivaCell.setBackgroundColor(BaseColor.WHITE);
            ivaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(ivaCell);
            
            PdfPCell evasioneCell = new PdfPCell(new Phrase(dataEvasione, FONT_SMALL));
            evasioneCell.setPadding(5);
            evasioneCell.setBorderWidthTop(1.0f);
            evasioneCell.setBorderWidthBottom(hasNote ? 0 : 1.0f);
            evasioneCell.setBorderWidthLeft(0);
            evasioneCell.setBorderWidthRight(0);
            evasioneCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote) {
                evasioneCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            evasioneCell.setBackgroundColor(BaseColor.WHITE);
            evasioneCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(evasioneCell);
            
            // Aggiungi riga note se presenti (occupa tutte le colonne)
            if (hasNote) {
                PdfPCell noteCell = new PdfPCell(new Phrase("NOTE:\n" + voce.getNote() + "\n", FONT_SMALL));
                noteCell.setPadding(5);
                noteCell.setPaddingLeft(20); // Rientro a sinistra per indicare che appartiene alla riga precedente
                noteCell.setBorder(Rectangle.NO_BORDER); // Nessun bordo
                noteCell.setBackgroundColor(NOTE_BACKGROUND); // Sfondo leggermente grigio per distinguerle
                noteCell.setColspan(7); // Occupa tutte le 7 colonne
                table.addCell(noteCell);
            }
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
    }
    
    private void aggiungiTabellaVociConSopralluogo(Document document, Fattura fattura) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 8, 8, 12, 12, 8, 22});
        table.setLockedWidth(false);

        // Headers
        PdfPCell header1 = new PdfPCell(new Phrase("Descrizione merce o servizio", FONT_SMALL));
        styleHeaderCellNoVerticalBorders(header1);
        header1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(header1);
        
        PdfPCell header2 = new PdfPCell(new Phrase("U.M.", FONT_SMALL));
        styleHeaderCellNoVerticalBorders(header2);
        header2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(header2);
        
        PdfPCell header3 = new PdfPCell(new Phrase("Quantità", FONT_SMALL));
        styleHeaderCellNoVerticalBorders(header3);
        header3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(header3);
        
        PdfPCell header4 = new PdfPCell(new Phrase("Prezzo", FONT_SMALL));
        styleHeaderCellNoVerticalBorders(header4);
        header4.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(header4);
        
        PdfPCell header5 = new PdfPCell(new Phrase("Importo", FONT_SMALL));
        styleHeaderCellNoVerticalBorders(header5);
        header5.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(header5);
        
        PdfPCell header6 = new PdfPCell(new Phrase("C.I.", FONT_SMALL));
        styleHeaderCellNoVerticalBorders(header6);
        header6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(header6);
        
        PdfPCell header7 = new PdfPCell(new Phrase("Evasione", FONT_SMALL));
        styleHeaderCellNoVerticalBorders(header7);
        header7.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(header7);
    
        // Voci
        String dataEvasione = fattura.getDataDocumento() != null 
            ? fattura.getDataDocumento().format(dateFormatter) 
            : "";
        
        for (VoceFattura voce : fattura.getVoci()) {
            boolean hasNote = voce.getNote() != null && !voce.getNote().trim().isEmpty();
            boolean hasSopralluogo = hasDatiSopralluogo(voce);
            
            // Descrizione (può essere multi-linea)
            String descrizioneCompleta = voce.getDescrizione();
            if (voce.getDettagliTecnici() != null && !voce.getDettagliTecnici().isEmpty()) {
                descrizioneCompleta += "\n" + voce.getDettagliTecnici();
            }
            
            PdfPCell descCell = new PdfPCell(new Phrase(descrizioneCompleta, FONT_SMALL));
            descCell.setPadding(5);
            descCell.setBorderWidthTop(1.0f);
            descCell.setBorderWidthBottom((hasNote || hasSopralluogo) ? 0 : 1.0f);
            descCell.setBorderWidthLeft(0);
            descCell.setBorderWidthRight(0);
            descCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote && !hasSopralluogo) {
                descCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            descCell.setBackgroundColor(BaseColor.WHITE);
            table.addCell(descCell);
            
            // Celle dati
            PdfPCell umCell = new PdfPCell(new Phrase(voce.getUnitaMisura(), FONT_SMALL));
            umCell.setPadding(5);
            umCell.setBorderWidthTop(1.0f);
            umCell.setBorderWidthBottom((hasNote || hasSopralluogo) ? 0 : 1.0f);
            umCell.setBorderWidthLeft(0);
            umCell.setBorderWidthRight(0);
            umCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote && !hasSopralluogo) {
                umCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            umCell.setBackgroundColor(BaseColor.WHITE);
            umCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(umCell);
            
            PdfPCell qtyCell = new PdfPCell(new Phrase(formatNumber(voce.getQuantita()), FONT_SMALL));
            qtyCell.setPadding(5);
            qtyCell.setBorderWidthTop(1.0f);
            qtyCell.setBorderWidthBottom((hasNote || hasSopralluogo) ? 0 : 1.0f);
            qtyCell.setBorderWidthLeft(0);
            qtyCell.setBorderWidthRight(0);
            qtyCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote && !hasSopralluogo) {
                qtyCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            qtyCell.setBackgroundColor(BaseColor.WHITE);
            qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(qtyCell);
            
            PdfPCell priceCell = new PdfPCell(new Phrase(formatCurrency(voce.getPrezzoUnitario()), FONT_SMALL));
            priceCell.setPadding(5);
            priceCell.setBorderWidthTop(1.0f);
            priceCell.setBorderWidthBottom((hasNote || hasSopralluogo) ? 0 : 1.0f);
            priceCell.setBorderWidthLeft(0);
            priceCell.setBorderWidthRight(0);
            priceCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote && !hasSopralluogo) {
                priceCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            priceCell.setBackgroundColor(BaseColor.WHITE);
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(priceCell);
            
            PdfPCell importoCell = new PdfPCell(new Phrase(formatCurrency(voce.getImporto()), FONT_SMALL));
            importoCell.setPadding(5);
            importoCell.setBorderWidthTop(1.0f);
            importoCell.setBorderWidthBottom((hasNote || hasSopralluogo) ? 0 : 1.0f);
            importoCell.setBorderWidthLeft(0);
            importoCell.setBorderWidthRight(0);
            importoCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote && !hasSopralluogo) {
                importoCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            importoCell.setBackgroundColor(BaseColor.WHITE);
            importoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(importoCell);
            
            PdfPCell ivaCell = new PdfPCell(new Phrase(voce.getCodiceIva() != null ? voce.getCodiceIva().toString() : "22", FONT_SMALL));
            ivaCell.setPadding(5);
            ivaCell.setBorderWidthTop(1.0f);
            ivaCell.setBorderWidthBottom((hasNote || hasSopralluogo) ? 0 : 1.0f);
            ivaCell.setBorderWidthLeft(0);
            ivaCell.setBorderWidthRight(0);
            ivaCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote && !hasSopralluogo) {
                ivaCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            ivaCell.setBackgroundColor(BaseColor.WHITE);
            ivaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(ivaCell);
            
            PdfPCell evasioneCell = new PdfPCell(new Phrase(dataEvasione, FONT_SMALL));
            evasioneCell.setPadding(5);
            evasioneCell.setBorderWidthTop(1.0f);
            evasioneCell.setBorderWidthBottom((hasNote || hasSopralluogo) ? 0 : 1.0f);
            evasioneCell.setBorderWidthLeft(0);
            evasioneCell.setBorderWidthRight(0);
            evasioneCell.setBorderColorTop(LIGHT_GRAY_BORDER);
            if (!hasNote && !hasSopralluogo) {
                evasioneCell.setBorderColorBottom(LIGHT_GRAY_BORDER);
            }
            evasioneCell.setBackgroundColor(BaseColor.WHITE);
            evasioneCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(evasioneCell);
            
            // Aggiungi riga note se presenti
            if (hasNote) {
                PdfPCell noteCell = new PdfPCell(new Phrase("NOTE:\n" + voce.getNote() + "\n", FONT_SMALL));
                noteCell.setPadding(5);
                noteCell.setPaddingLeft(20);
                noteCell.setBorder(Rectangle.NO_BORDER);
                noteCell.setBackgroundColor(NOTE_BACKGROUND);
                noteCell.setColspan(7);
                table.addCell(noteCell);
            }
            
            // Aggiungi riga scheda di sopralluogo se presente
            if (hasSopralluogo) {
                String sopralluogoText = buildSopralluogoText(voce);
                PdfPCell sopralluogoCell = new PdfPCell(new Phrase("SCHEDA DI SOPRALLUOGO:\n" + sopralluogoText + "\n", FONT_SMALL));
                sopralluogoCell.setPadding(5);
                sopralluogoCell.setPaddingLeft(20);
                sopralluogoCell.setBorder(Rectangle.NO_BORDER);
                sopralluogoCell.setBackgroundColor(NOTE_BACKGROUND);
                sopralluogoCell.setColspan(7);
                table.addCell(sopralluogoCell);
            }
        }
        
        document.add(table);
        document.add(new Paragraph(" "));
    }
    
    private boolean hasDatiSopralluogo(VoceFattura voce) {
        return voce.getCerniera() != null || voce.getPompaScarico() != null || 
               voce.getTensione() != null || voce.getAllacciDistanti() != null ||
               voce.getRuote() != null || voce.getSmaltimento() != null ||
               voce.getNecessarioSopralluogo() != null || voce.getAddolcitoreCorrente() != null ||
               voce.getPassaggioCm() != null || voce.getScale() != null ||
               voce.getMacchinaDaSmontare() != null || voce.getMisure() != null ||
               voce.getGas() != null || voce.getGasDistanzaCm() != null ||
               voce.getParcheggio() != null || voce.getGiornoOraConsegna() != null;
    }
    
    private String buildSopralluogoText(VoceFattura voce) {
        StringBuilder sb = new StringBuilder();
        
        if (voce.getCerniera() != null && !voce.getCerniera().isEmpty()) {
            sb.append("Cerniera: ").append(voce.getCerniera()).append(" | ");
        }
        if (voce.getPompaScarico() != null) {
            sb.append("Pompa scarico: ").append(voce.getPompaScarico() ? "SI" : "NO").append(" | ");
        }
        if (voce.getTensione() != null && !voce.getTensione().isEmpty()) {
            sb.append("Tensione: ").append(voce.getTensione()).append(" | ");
        }
        if (voce.getAllacciDistanti() != null) {
            sb.append("Allacci distanti: ").append(voce.getAllacciDistanti() ? "SI" : "NO").append(" | ");
        }
        if (voce.getRuote() != null) {
            sb.append("Ruote: ").append(voce.getRuote() ? "SI" : "NO").append(" | ");
        }
        if (voce.getSmaltimento() != null) {
            sb.append("Smaltimento: ").append(voce.getSmaltimento() ? "SI" : "NO").append(" | ");
        }
        if (voce.getNecessarioSopralluogo() != null) {
            sb.append("Necessario sopralluogo: ").append(voce.getNecessarioSopralluogo() ? "SI" : "NO").append(" | ");
        }
        if (voce.getAddolcitoreCorrente() != null && !voce.getAddolcitoreCorrente().isEmpty()) {
            sb.append("Addolcitore corrente: ").append(voce.getAddolcitoreCorrente()).append(" | ");
        }
        if (voce.getPassaggioCm() != null) {
            sb.append("Passaggio: ").append(voce.getPassaggioCm()).append(" cm | ");
        }
        if (voce.getScale() != null && !voce.getScale().isEmpty()) {
            sb.append("Scale: ").append(voce.getScale()).append(" | ");
        }
        if (voce.getMacchinaDaSmontare() != null) {
            sb.append("Macchina da smontare: ").append(voce.getMacchinaDaSmontare() ? "SI" : "NO");
            if (voce.getMacchinaDaSmontare() && voce.getMisure() != null && !voce.getMisure().isEmpty()) {
                sb.append(" - Misure: ").append(voce.getMisure());
            }
            sb.append(" | ");
        }
        if (voce.getGas() != null && !voce.getGas().isEmpty()) {
            sb.append("Gas: ").append(voce.getGas());
            if (voce.getGasDistanzaCm() != null) {
                sb.append(" (distanza: ").append(voce.getGasDistanzaCm()).append(" cm)");
            }
            sb.append(" | ");
        }
        if (voce.getParcheggio() != null) {
            sb.append("Parcheggio: ").append(voce.getParcheggio() ? "SI" : "NO").append(" | ");
        }
        if (voce.getGiornoOraConsegna() != null && !voce.getGiornoOraConsegna().isEmpty()) {
            sb.append("Giorno e ora di consegna: ").append(voce.getGiornoOraConsegna());
        }
        
        // Rimuovi l'ultimo " | " se presente
        String result = sb.toString();
        if (result.endsWith(" | ")) {
            result = result.substring(0, result.length() - 3);
        }
        
        return result;
    }
    
    private void aggiungiTotali(Document document, PdfWriter writer, Fattura fattura) throws DocumentException {
        // Container dei totali va in fondo alla prima pagina usando posizionamento assoluto
        
        // Assicura che i valori non siano null
        BigDecimal totaleRighe = fattura.getTotaleRighe() != null ? fattura.getTotaleRighe() : BigDecimal.ZERO;
        BigDecimal scontiMaggiori = fattura.getScontiMaggiori() != null ? fattura.getScontiMaggiori() : BigDecimal.ZERO;
        BigDecimal importoIva = fattura.getImportoIva() != null ? fattura.getImportoIva() : BigDecimal.ZERO;
        BigDecimal speseTrasporto = fattura.getSpeseTrasporto() != null ? fattura.getSpeseTrasporto() : BigDecimal.ZERO;
        BigDecimal totaleDocumento = fattura.getTotaleDocumento() != null ? fattura.getTotaleDocumento() : BigDecimal.ZERO;
        
        // Calcola Imponibile Scontato
        BigDecimal imponibileScontato = totaleRighe.subtract(scontiMaggiori);
        
        // TABELLA CONTENITORE: unico riquadro per tutta la sezione finale
        PdfPTable containerTable = new PdfPTable(1);
        containerTable.setWidthPercentage(100);
        containerTable.setLockedWidth(false);
        containerTable.setSpacingBefore(0);
        containerTable.setSpacingAfter(0);
        
        // Cella contenitore con bordi esterni
        PdfPCell containerCell = new PdfPCell();
        containerCell.setBorderWidth(1.5f);
        containerCell.setBorderColor(GRAY_BORDER);
        containerCell.setPadding(5);
        containerCell.setBackgroundColor(BaseColor.WHITE);
        
        // TABELLA TOTALI: label e importo nella stessa cella (label in alto, valore in basso)
        // Prima riga: Totale righe, Sconti/magg., Imponibile Scontato, I.V.A., Spese Trasporto
        PdfPTable tableTotaliRiga1 = new PdfPTable(5);
        tableTotaliRiga1.setWidthPercentage(100);
        tableTotaliRiga1.setWidths(new float[]{20, 20, 20, 20, 20});
        tableTotaliRiga1.setLockedWidth(false);
        tableTotaliRiga1.setSpacingBefore(0);
        tableTotaliRiga1.setSpacingAfter(0);
        
        // Totale righe (label in alto, valore in basso)
        addCellLabelValueVertical(tableTotaliRiga1, "Totale righe", formatCurrency(totaleRighe));
        
        // Sconti/magg.
        addCellLabelValueVertical(tableTotaliRiga1, "Sconti/magg.", formatCurrency(scontiMaggiori));
        
        // Imponibile Scontato
        addCellLabelValueVertical(tableTotaliRiga1, "Imponibile Scontato", formatCurrency(imponibileScontato));
        
        // I.V.A.
        addCellLabelValueVertical(tableTotaliRiga1, "I.V.A.", formatCurrency(importoIva));
        
        // Spese Trasporto
        addCellLabelValueVertical(tableTotaliRiga1, "Spese Trasporto", formatCurrency(speseTrasporto));
        
        containerCell.addElement(tableTotaliRiga1);
        
        // Seconda riga: Acconto versato, Spese incasso, Spese imballo, Bollo, Ritenuta (con label sopra e valore in basso)
        PdfPTable tableTotaliRiga2 = new PdfPTable(5);
        tableTotaliRiga2.setWidthPercentage(100);
        tableTotaliRiga2.setWidths(new float[]{20, 20, 20, 20, 20});
        tableTotaliRiga2.setLockedWidth(false);
        tableTotaliRiga2.setSpacingBefore(0);
        tableTotaliRiga2.setSpacingAfter(0);
        
        BigDecimal accontoVersato = fattura.getAccontoVersato() != null ? fattura.getAccontoVersato() : BigDecimal.ZERO;
        BigDecimal speseIncasso = fattura.getSpeseIncasso() != null ? fattura.getSpeseIncasso() : BigDecimal.ZERO;
        BigDecimal speseImballo = fattura.getSpeseImballo() != null ? fattura.getSpeseImballo() : BigDecimal.ZERO;
        BigDecimal bollo = fattura.getBollo() != null ? fattura.getBollo() : BigDecimal.ZERO;
        BigDecimal ritenuta = fattura.getRitenuta() != null ? fattura.getRitenuta() : BigDecimal.ZERO;
        
        addCellLabelValueVertical(tableTotaliRiga2, "Acconto versato", formatCurrency(accontoVersato));
        addCellLabelValueVertical(tableTotaliRiga2, "Spese incasso", formatCurrency(speseIncasso));
        addCellLabelValueVertical(tableTotaliRiga2, "Spese imballo", formatCurrency(speseImballo));
        addCellLabelValueVertical(tableTotaliRiga2, "Bollo", formatCurrency(bollo));
        addCellLabelValueVertical(tableTotaliRiga2, "Ritenuta", formatCurrency(ritenuta));
        
        containerCell.addElement(tableTotaliRiga2);
        
        // Riga separata per Totale documento € allineato a destra (cella larga metà riga)
        PdfPTable tableTotaliRigaTotale = new PdfPTable(2);
        tableTotaliRigaTotale.setWidthPercentage(100);
        tableTotaliRigaTotale.setWidths(new float[]{50, 50}); // Metà riga vuota, metà con totale
        tableTotaliRigaTotale.setLockedWidth(false);
        tableTotaliRigaTotale.setSpacingBefore(0);
        tableTotaliRigaTotale.setSpacingAfter(10);
        
        // Cella vuota a sinistra
        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorderWidth(1.5f);
        emptyCell.setBorderColor(GRAY_BORDER);
        emptyCell.setBackgroundColor(BaseColor.WHITE);
        emptyCell.setPadding(2);
        tableTotaliRigaTotale.addCell(emptyCell);
        
        // Cella con Totale documento € allineato a destra
        Font fontTotale = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        PdfPCell totaleCell = new PdfPCell();
        totaleCell.setBorderWidth(1.5f);
        totaleCell.setBorderColor(GRAY_BORDER);
        totaleCell.setBackgroundColor(DARK_GRAY_BACKGROUND);
        totaleCell.setPadding(2);
        totaleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        // Crea una tabella interna per label e valore sulla stessa riga
        PdfPTable innerTable = new PdfPTable(2);
        innerTable.setWidthPercentage(100);
        innerTable.setWidths(new float[]{70, 30});
        innerTable.setLockedWidth(false);
        
        PdfPCell labelInner = new PdfPCell(new Phrase("Totale documento €", fontTotale));
        labelInner.setBorder(Rectangle.NO_BORDER);
        labelInner.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelInner.setPadding(0);
        innerTable.addCell(labelInner);
        
        PdfPCell valueInner = new PdfPCell(new Phrase(formatCurrency(totaleDocumento), fontTotale));
        valueInner.setBorder(Rectangle.NO_BORDER);
        valueInner.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueInner.setPadding(0);
        innerTable.addCell(valueInner);
        
        totaleCell.addElement(innerTable);
        tableTotaliRigaTotale.addCell(totaleCell);
        
        containerCell.addElement(tableTotaliRigaTotale);
        
        // TERZA SEZIONE: Tabella pagamento e accettazione (dentro il container)
        PdfPTable tablePagamento = new PdfPTable(4);
        tablePagamento.setWidthPercentage(100);
        tablePagamento.setWidths(new float[]{20, 20, 20, 40}); // Firma più grande (40%)
        tablePagamento.setLockedWidth(false);
        tablePagamento.setSpacingBefore(0);
        tablePagamento.setSpacingAfter(10);
        
        // Header con sfondo grigio scuro
        PdfPCell header1 = new PdfPCell(new Phrase("Tipo pagamento", FONT_SMALL));
        styleHeaderCell(header1);
        tablePagamento.addCell(header1);
        
        PdfPCell header2 = new PdfPCell(new Phrase("Scadenza", FONT_SMALL));
        styleHeaderCell(header2);
        tablePagamento.addCell(header2);
        
        PdfPCell header3 = new PdfPCell(new Phrase("Importo scadenza", FONT_SMALL));
        styleHeaderCell(header3);
        tablePagamento.addCell(header3);
        
        PdfPCell header4 = new PdfPCell(new Phrase("Firma Per Accettazione", FONT_SMALL));
        styleHeaderCell(header4);
        tablePagamento.addCell(header4);
        
        // Dati - Usa i nuovi campi se disponibili, altrimenti fallback alle note
        String tipoPagamento = fattura.getTipoPagamento() != null && !fattura.getTipoPagamento().isEmpty()
            ? fattura.getTipoPagamento()
            : (fattura.getNote() != null && fattura.getNote().contains("Bonifico") 
                ? "Bonifico Bancario" 
                : (fattura.getNote() != null && fattura.getNote().contains("PAGAMENTO") 
                    ? fattura.getNote() 
                    : "DA CONVENIRE"));
        
        String scadenza = fattura.getScadenzaPagamento() != null 
            ? fattura.getScadenzaPagamento().format(dateFormatter) 
            : (fattura.getDataDocumento() != null 
                ? fattura.getDataDocumento().format(dateFormatter) 
                : "");
        
        // Usa importo scadenza se disponibile, altrimenti totale documento
        String importoScadenzaText;
        if (fattura.getImportoScadenza() != null) {
            importoScadenzaText = formatCurrency(fattura.getImportoScadenza());
        } else {
            importoScadenzaText = formatCurrency(totaleDocumento);
        }
        
        PdfPCell cell1 = new PdfPCell(new Phrase(tipoPagamento, FONT_SMALL));
        cell1.setPadding(5);
        cell1.setBorderWidth(1.5f);
        cell1.setBorderColor(GRAY_BORDER);
        cell1.setBackgroundColor(BaseColor.WHITE);
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablePagamento.addCell(cell1);
        
        PdfPCell cell2 = new PdfPCell(new Phrase(scadenza, FONT_SMALL));
        cell2.setPadding(5);
        cell2.setBorderWidth(1.5f);
        cell2.setBorderColor(GRAY_BORDER);
        cell2.setBackgroundColor(BaseColor.WHITE);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablePagamento.addCell(cell2);
        
        PdfPCell cell3 = new PdfPCell(new Phrase(importoScadenzaText, FONT_SMALL));
        cell3.setPadding(5);
        cell3.setBorderWidth(1.5f);
        cell3.setBorderColor(GRAY_BORDER);
        cell3.setBackgroundColor(BaseColor.WHITE);
        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablePagamento.addCell(cell3);
        
        // Cella firma più stretta (ridotta da 80 a 50, padding ridotto)
        PdfPCell cell4 = new PdfPCell(new Phrase("", FONT_SMALL));
        cell4.setPadding(15); // Padding ridotto (era 25)
        cell4.setFixedHeight(50); // Altezza ridotta (era 80) - una riga in meno
        cell4.setBorderWidth(1.5f);
        cell4.setBorderColor(GRAY_BORDER);
        cell4.setBackgroundColor(BaseColor.WHITE); // Sfondo bianco per la firma
        cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tablePagamento.addCell(cell4);
        
        containerCell.addElement(tablePagamento);
        
        // QUARTA SEZIONE: Tabella spedizione - struttura come nell'immagine
        PdfPTable tableSpedizione = new PdfPTable(2);
        tableSpedizione.setWidthPercentage(100);
        tableSpedizione.setWidths(new float[]{50, 50});
        tableSpedizione.setLockedWidth(false);
        tableSpedizione.setSpacingBefore(0);
        tableSpedizione.setSpacingAfter(0);
        
        // Prima riga: Modalità di spedizione (label a sinistra, valore a destra)
        PdfPCell label1 = new PdfPCell(new Phrase("Modalità di spedizione", FONT_SMALL));
        label1.setPadding(5);
        label1.setBorderWidth(1.5f);
        label1.setBorderColor(GRAY_BORDER);
        label1.setBackgroundColor(BaseColor.WHITE); // Label con sfondo bianco
        label1.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableSpedizione.addCell(label1);
        
        PdfPCell value1 = new PdfPCell(new Phrase(
            fattura.getModalitaSpedizione() != null ? fattura.getModalitaSpedizione() : "", 
            FONT_SMALL));
        value1.setPadding(5);
        value1.setBorderWidth(1.5f);
        value1.setBorderColor(GRAY_BORDER);
        value1.setBackgroundColor(BaseColor.WHITE); // Sfondo bianco per i dati
        value1.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableSpedizione.addCell(value1);
        
        // Seconda riga: Porto (label a sinistra, valore a destra)
        PdfPCell label2 = new PdfPCell(new Phrase("Porto", FONT_SMALL));
        label2.setPadding(5);
        label2.setBorderWidth(1.5f);
        label2.setBorderColor(GRAY_BORDER);
        label2.setBackgroundColor(BaseColor.WHITE); // Label con sfondo bianco
        label2.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableSpedizione.addCell(label2);
        
        PdfPCell value2 = new PdfPCell(new Phrase(
            fattura.getPorto() != null ? fattura.getPorto() : "", 
            FONT_SMALL));
        value2.setPadding(5);
        value2.setBorderWidth(1.5f);
        value2.setBorderColor(GRAY_BORDER);
        value2.setBackgroundColor(BaseColor.WHITE); // Sfondo bianco per i dati
        value2.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableSpedizione.addCell(value2);
        
        // Terza riga: Condizione di consegna (label a sinistra, valore a destra)
        PdfPCell label3 = new PdfPCell(new Phrase("Condizione di consegna", FONT_SMALL));
        label3.setPadding(5);
        label3.setBorderWidth(1.5f);
        label3.setBorderColor(GRAY_BORDER);
        label3.setBackgroundColor(BaseColor.WHITE); // Label con sfondo bianco
        label3.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableSpedizione.addCell(label3);
        
        PdfPCell value3 = new PdfPCell(new Phrase(
            fattura.getCondizioneConsegna() != null ? fattura.getCondizioneConsegna() : "Franco Fabbrica (EXW)", 
            FONT_SMALL));
        value3.setPadding(5);
        value3.setBorderWidth(1.5f);
        value3.setBorderColor(GRAY_BORDER);
        value3.setBackgroundColor(BaseColor.WHITE); // Sfondo bianco per i dati
        value3.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableSpedizione.addCell(value3);
        
        containerCell.addElement(tableSpedizione);
        
        // Aggiungi la cella contenitore alla tabella
        containerTable.addCell(containerCell);
        
        // Posiziona la tabella in fondo alla pagina
        // Calcola lo spazio rimanente e aggiungi spazio vuoto per spingere la tabella in fondo
        float currentY = writer.getVerticalPosition(false);
        float pageHeight = document.getPageSize().getHeight();
        float topMargin = document.topMargin();
        float bottomMargin = document.bottomMargin();
        
        // Stima l'altezza della tabella (può essere regolata in base al contenuto)
        float estimatedTableHeight = 350f;
        
        // Calcola la posizione Y target (in fondo alla pagina)
        float targetY = bottomMargin + estimatedTableHeight;
        
        // Calcola lo spazio da aggiungere per spingere la tabella in fondo
        float availableHeight = pageHeight - topMargin - bottomMargin;
        float remainingSpace = currentY - targetY;
        
        // Se c'è spazio sufficiente, aggiungi uno spazio vuoto prima della tabella
        if (remainingSpace > 10 && remainingSpace < availableHeight) {
            Paragraph spacer = new Paragraph();
            spacer.setSpacingBefore(remainingSpace);
            document.add(spacer);
        }
        
        // Aggiungi la tabella normalmente (ora sarà in fondo grazie allo spazio vuoto)
        document.add(containerTable);
    }
    
    private void styleHeaderCell(PdfPCell cell) {
        cell.setPadding(5);
        cell.setBorderWidth(1.5f);
        cell.setBorderColor(GRAY_BORDER);
        cell.setBackgroundColor(DARK_GRAY_BACKGROUND);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    }
    
    private void styleDataCell(PdfPCell cell) {
        cell.setPadding(5);
        cell.setBorderWidth(1.5f);
        cell.setBorderColor(GRAY_BORDER);
        cell.setBackgroundColor(GRAY_BACKGROUND);
    }
    
    private void aggiungiNoteCondizioni(Document document, Fattura fattura) throws DocumentException {
        if (fattura.getNote() != null && !fattura.getNote().isEmpty()) {
            Paragraph note = new Paragraph("Note:", FONT_HEADER);
            document.add(note);
            Paragraph noteText = new Paragraph(fattura.getNote(), FONT_SMALL);
            document.add(noteText);
            document.add(new Paragraph(" "));
        }
    }
    
    private void aggiungiFooter(Document document, Fattura fattura) throws DocumentException {
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph(
            String.format("Elaborato da: %s - p.iva %s - %s",
                fattura.getRagioneSocialeEmittente(),
                fattura.getPartitaIvaEmittente(),
                fattura.getSedeLegaleEmittente()),
            FONT_SMALL
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }
    
    private void addTotaleRowStyled(PdfPTable table, String label, BigDecimal value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_NORMAL));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(5);
        labelCell.setBorderWidth(1.5f);
        labelCell.setBorderColor(GRAY_BORDER);
        labelCell.setBackgroundColor(GRAY_BACKGROUND);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(formatCurrency(value), FONT_NORMAL));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        valueCell.setBorderWidth(1.5f);
        valueCell.setBorderColor(GRAY_BORDER);
        valueCell.setBackgroundColor(GRAY_BACKGROUND);
        table.addCell(valueCell);
    }
    
    private void addTotaleRowCompact(PdfPTable table, String label, BigDecimal value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_NORMAL));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Label allineate a sinistra
        labelCell.setPadding(3); // Padding ridotto per compattezza
        labelCell.setBorderWidth(1.5f);
        labelCell.setBorderColor(GRAY_BORDER);
        labelCell.setBackgroundColor(BaseColor.WHITE); // Sfondo bianco per le label
        labelCell.setMinimumHeight(0); // Altezza minima ridotta
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(formatCurrency(value), FONT_NORMAL));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT); // Valori allineati a destra
        valueCell.setPadding(3); // Padding ridotto per compattezza
        valueCell.setBorderWidth(1.5f);
        valueCell.setBorderColor(GRAY_BORDER);
        valueCell.setBackgroundColor(BaseColor.WHITE); // Sfondo bianco per i dati
        valueCell.setMinimumHeight(0); // Altezza minima ridotta
        table.addCell(valueCell);
    }
    
    private void addCellToTable(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorderWidth(1.5f); // Bordi più spessi
        cell.setBorderColor(GRAY_BORDER); // Colore grigio per i bordi
        if (isHeader) {
            cell.setBackgroundColor(DARK_GRAY_BACKGROUND); // Grigio più scuro per header
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        } else {
            cell.setBackgroundColor(GRAY_BACKGROUND); // Grigio chiaro per i campi dati
        }
        table.addCell(cell);
    }
    
    private void addCellToTableNoVerticalBorders(PdfPTable table, String text, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorderWidthTop(1.0f); // Bordo più sottile
        cell.setBorderWidthBottom(1.0f); // Bordo più sottile
        cell.setBorderWidthLeft(0); // Nessun bordo sinistro
        cell.setBorderWidthRight(0); // Nessun bordo destro
        cell.setBorderColorTop(LIGHT_GRAY_BORDER); // Grigio più chiaro
        cell.setBorderColorBottom(LIGHT_GRAY_BORDER); // Grigio più chiaro
        if (isHeader) {
            cell.setBackgroundColor(DARK_GRAY_BACKGROUND); // Grigio più scuro per header
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        } else {
            cell.setBackgroundColor(BaseColor.WHITE); // Bianco per i dati
        }
        table.addCell(cell);
    }
    
    private void styleHeaderCellNoVerticalBorders(PdfPCell cell) {
        cell.setPadding(5);
        cell.setBorderWidthTop(1.0f);
        cell.setBorderWidthBottom(1.0f);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);
        cell.setBorderColorTop(LIGHT_GRAY_BORDER);
        cell.setBorderColorBottom(LIGHT_GRAY_BORDER);
        cell.setBackgroundColor(DARK_GRAY_BACKGROUND);
    }
    
    private void addCellToTableWhite(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorderWidth(1.5f);
        cell.setBorderColor(GRAY_BORDER);
        cell.setBackgroundColor(BaseColor.WHITE); // Sfondo bianco per i dati
        table.addCell(cell);
    }
    
    private void addCellLabelValue(PdfPTable table, String label, String value) {
        // Cella label con font più piccolo
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_SMALL));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setPadding(2);
        labelCell.setBorderWidth(1.5f);
        labelCell.setBorderColor(GRAY_BORDER);
        labelCell.setBackgroundColor(BaseColor.WHITE);
        labelCell.setMinimumHeight(0);
        table.addCell(labelCell);
        
        // Cella valore con font più piccolo
        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_SMALL));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(2);
        valueCell.setBorderWidth(1.5f);
        valueCell.setBorderColor(GRAY_BORDER);
        valueCell.setBackgroundColor(BaseColor.WHITE);
        valueCell.setMinimumHeight(0);
        table.addCell(valueCell);
    }
    
    private void addCellLabelValueVertical(PdfPTable table, String label, String value) {
        // Cella con label in alto e valore in basso
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(1.5f);
        cell.setBorderColor(GRAY_BORDER);
        cell.setBackgroundColor(BaseColor.WHITE);
        cell.setPadding(2);
        cell.setMinimumHeight(0);
        
        // Label in alto
        Paragraph labelPara = new Paragraph(label, FONT_SMALL);
        labelPara.setAlignment(Element.ALIGN_LEFT);
        cell.addElement(labelPara);
        
        // Valore in basso
        Paragraph valuePara = new Paragraph(value, FONT_SMALL);
        valuePara.setAlignment(Element.ALIGN_RIGHT);
        cell.addElement(valuePara);
        
        table.addCell(cell);
    }
    
    private String formatCurrency(BigDecimal value) {
        return value != null ? currencyFormat.format(value) : "0,00";
    }
    
    private String formatNumber(BigDecimal value) {
        return value != null ? currencyFormat.format(value) : "0,00";
    }
    
    private static Image getImageFromResource(String imagePath) throws IOException, BadElementException {
        ClassPathResource classPathResource = new ClassPathResource(imagePath);
        if (classPathResource.exists()) {
            return Image.getInstance(classPathResource.getURL());
        } else {
            throw new IOException("Impossibile trovare l'immagine nel resource.");
        }
    }
}
