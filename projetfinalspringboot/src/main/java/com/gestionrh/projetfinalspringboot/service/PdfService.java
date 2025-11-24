package com.gestionrh.projetfinalspringboot.service;

import com.gestionrh.projetfinalspringboot.model.entity.FichePaie;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {
    
    @SneakyThrows
    public byte[] generateFichePaiePdf(FichePaie fiche) {
        // Configuration du format de nombre français
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        
        // Création du document PDF (marges réduites pour tenir sur 1 page)
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Couleurs professionnelles
        BaseColor darkBlue = new BaseColor(31, 56, 100);  // Bleu foncé professionnel
        BaseColor lightGray = new BaseColor(240, 240, 240);
        BaseColor mediumGray = new BaseColor(200, 200, 200);
        BaseColor darkGray = new BaseColor(80, 80, 80);
        
        // Polices
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, darkBlue);
        Font smallBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);
        Font smallNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, darkGray);
        Font mediumBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
        Font mediumNormal = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        Font headerWhite = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
        Font netFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
        
        // ============= EN-TÊTE =============
        Paragraph header = new Paragraph("BULLETIN DE PAIE", titleFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(5);
        document.add(header);
        
        Paragraph period = new Paragraph("Période : " + String.format("%02d/%d", fiche.getMois(), fiche.getAnnee()), 
            FontFactory.getFont(FontFactory.HELVETICA, 10, darkGray));
        period.setAlignment(Element.ALIGN_CENTER);
        period.setSpacingAfter(10);
        document.add(period);
        
        // ============= EMPLOYEUR / SALARIÉ =============
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});
        infoTable.setSpacingAfter(8);
        
        // EMPLOYEUR
        PdfPCell employerCell = new PdfPCell();
        employerCell.setBorder(Rectangle.BOX);
        employerCell.setBorderColor(mediumGray);
        employerCell.setPadding(8);
        employerCell.addElement(new Paragraph("EMPLOYEUR", smallBold));
        employerCell.addElement(new Paragraph("RH ÉLÉGANCE", mediumBold));
        employerCell.addElement(new Paragraph("123 Avenue des Champs-Élysées", smallNormal));
        employerCell.addElement(new Paragraph("75008 Paris, France", smallNormal));
        employerCell.addElement(new Paragraph("SIRET: 123 456 789 00012", smallNormal));
        infoTable.addCell(employerCell);
        
        // SALARIÉ
        PdfPCell employeeCell = new PdfPCell();
        employeeCell.setBorder(Rectangle.BOX);
        employeeCell.setBorderColor(mediumGray);
        employeeCell.setPadding(8);
        employeeCell.addElement(new Paragraph("SALARIÉ", smallBold));
        employeeCell.addElement(new Paragraph(
            fiche.getEmploye().getPrenom() + " " + fiche.getEmploye().getNom().toUpperCase(), mediumBold));
        employeeCell.addElement(new Paragraph("Matricule: " + fiche.getEmploye().getMatricule(), smallNormal));
        employeeCell.addElement(new Paragraph("Poste: " + fiche.getEmploye().getPoste(), smallNormal));
        String departementNom = fiche.getEmploye().getDepartement() != null 
            ? fiche.getEmploye().getDepartement().getNom() 
            : "Non assigné";
        employeeCell.addElement(new Paragraph("Département: " + departementNom, smallNormal));
        employeeCell.addElement(new Paragraph("Grade: " + fiche.getEmploye().getGrade(), smallNormal));
        infoTable.addCell(employeeCell);
        
        document.add(infoTable);
        
        // ============= TABLEAU DES RUBRIQUES =============
        PdfPTable salaryTable = new PdfPTable(4);
        salaryTable.setWidthPercentage(100);
        salaryTable.setWidths(new float[]{3.5f, 1.2f, 1.2f, 1.5f});
        salaryTable.setSpacingAfter(5);
        
        // EN-TÊTE TABLEAU
        addTableHeader(salaryTable, "LIBELLÉ", headerWhite, darkBlue);
        addTableHeader(salaryTable, "BASE", headerWhite, darkBlue);
        addTableHeader(salaryTable, "TAUX", headerWhite, darkBlue);
        addTableHeader(salaryTable, "MONTANT", headerWhite, darkBlue);
        
        // === RÉMUNÉRATION BRUTE ===
        addSectionHeader(salaryTable, "RÉMUNÉRATION BRUTE", 4);
        
        addTableRow(salaryTable, "Salaire de base", "151,67 h", "", df.format(fiche.getSalaireBase()) + " €", false);
        
        // Primes conditionnelles
        if (fiche.getPrimePerformance() != null && fiche.getPrimePerformance().compareTo(BigDecimal.ZERO) > 0) {
            addTableRow(salaryTable, "Prime de performance", "", "", df.format(fiche.getPrimePerformance()) + " €", false);
        }
        if (fiche.getPrimeAnciennete() != null && fiche.getPrimeAnciennete().compareTo(BigDecimal.ZERO) > 0) {
            addTableRow(salaryTable, "Prime d'ancienneté", "", "", df.format(fiche.getPrimeAnciennete()) + " €", false);
        }
        if (fiche.getAutresPrimes() != null && fiche.getAutresPrimes().compareTo(BigDecimal.ZERO) > 0) {
            addTableRow(salaryTable, "Autres primes", "", "", df.format(fiche.getAutresPrimes()) + " €", false);
        }
        
        addTableRow(salaryTable, "TOTAL BRUT", "", "", df.format(fiche.calculerSalaireBrut()) + " €", true);
        
        // === COTISATIONS ET CONTRIBUTIONS ===
        addSectionHeader(salaryTable, "COTISATIONS ET CONTRIBUTIONS", 4);
        
        if (fiche.getCotisationsSociales() != null && fiche.getCotisationsSociales().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tauxSecu = fiche.getCotisationsSociales()
                .divide(fiche.calculerSalaireBrut(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            
            addTableRow(salaryTable, "Sécurité sociale", df.format(fiche.calculerSalaireBrut()) + " €", 
                String.format("%.2f %%", tauxSecu.doubleValue()).replace('.', ','), 
                df.format(fiche.getCotisationsSociales()) + " €", false);
        }
        
        if (fiche.getImpots() != null && fiche.getImpots().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tauxImpot = fiche.getImpots()
                .divide(fiche.calculerSalaireBrut(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            
            addTableRow(salaryTable, "CSG / CRDS", df.format(fiche.calculerSalaireBrut()) + " €", 
                String.format("%.2f %%", tauxImpot.doubleValue()).replace('.', ','), 
                df.format(fiche.getImpots()) + " €", false);
        }
        
        addTableRow(salaryTable, "TOTAL COTISATIONS", "", "", df.format(fiche.calculerTotalDeductions()) + " €", true);
        
        document.add(salaryTable);
        
        // ============= NET À PAYER =============
        PdfPTable netTable = new PdfPTable(2);
        netTable.setWidthPercentage(100);
        netTable.setSpacingBefore(10);
        netTable.setSpacingAfter(10);
        
        PdfPCell netLabelCell = new PdfPCell(new Phrase("NET À PAYER", netFont));
        netLabelCell.setBorder(Rectangle.NO_BORDER);
        netLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        netLabelCell.setPadding(10);
        netLabelCell.setBackgroundColor(lightGray);
        netTable.addCell(netLabelCell);
        
        PdfPCell netAmountCell = new PdfPCell(new Phrase(df.format(fiche.calculerNetAPayer()) + " €", netFont));
        netAmountCell.setBorder(Rectangle.NO_BORDER);
        netAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        netAmountCell.setPadding(10);
        netAmountCell.setBackgroundColor(lightGray);
        netTable.addCell(netAmountCell);
        
        document.add(netTable);
        
        // ============= BAS DE PAGE =============
        String dateCreation = fiche.getDateCreation() != null ? 
            fiche.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        
        Paragraph footer = new Paragraph("Bulletin émis le " + dateCreation + 
            " - N° " + fiche.getId() + " - Document à conserver sans limitation de durée", 
            FontFactory.getFont(FontFactory.HELVETICA, 7, darkGray));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(15);
        document.add(footer);
        
        document.close();
        
        return baos.toByteArray();
    }
    
    /**
     * Ajoute un en-tête de tableau
     */
    private void addTableHeader(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }
    
    /**
     * Ajoute un en-tête de section dans le tableau
     */
    private void addSectionHeader(PdfPTable table, String text, int colspan) {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, sectionFont));
        cell.setColspan(colspan);
        cell.setBackgroundColor(new BaseColor(100, 100, 100));
        cell.setPadding(5);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }
    
    /**
     * Ajoute une ligne au tableau des salaires
     */
    private void addTableRow(PdfPTable table, String libelle, String base, String taux, String montant, boolean isBold) {
        Font cellFont = isBold ? 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK) :
            FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
        
        BaseColor bgColor = isBold ? new BaseColor(240, 240, 240) : BaseColor.WHITE;
        
        // Libellé
        PdfPCell c1 = new PdfPCell(new Phrase(libelle, cellFont));
        c1.setPadding(5);
        c1.setBackgroundColor(bgColor);
        c1.setBorder(Rectangle.BOTTOM);
        c1.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(c1);
        
        // Base
        PdfPCell c2 = new PdfPCell(new Phrase(base, cellFont));
        c2.setPadding(5);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBackgroundColor(bgColor);
        c2.setBorder(Rectangle.BOTTOM);
        c2.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(c2);
        
        // Taux
        PdfPCell c3 = new PdfPCell(new Phrase(taux, cellFont));
        c3.setPadding(5);
        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c3.setBackgroundColor(bgColor);
        c3.setBorder(Rectangle.BOTTOM);
        c3.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(c3);
        
        // Montant
        PdfPCell c4 = new PdfPCell(new Phrase(montant, cellFont));
        c4.setPadding(5);
        c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c4.setBackgroundColor(bgColor);
        c4.setBorder(Rectangle.BOTTOM);
        c4.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(c4);
    }
}
