package com.gestionrh.projetfinalspringboot.util;

import com.gestionrh.projetfinalspringboot.dto.CredentialDto;
import com.gestionrh.projetfinalspringboot.model.entity.Employe;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Utilitaires pour génération de PDF d'identifiants
 */
public class PdfCredentialsUtil {

    private static final BaseColor DARK_BLUE = new BaseColor(31, 56, 100);
    private static final BaseColor GOLD = new BaseColor(197, 165, 114);
    private static final BaseColor LIGHT_GRAY = new BaseColor(245, 245, 245);
    private static final BaseColor YELLOW_BG = new BaseColor(255, 248, 220);
    private static final BaseColor WARNING_BG = new BaseColor(255, 243, 205);
    private static final BaseColor WARNING_BORDER = new BaseColor(255, 165, 0);

    /**
     * Génère un PDF contenant tous les identifiants (tableau)
     */
    public static byte[] generateCredentialsTablePDF(List<CredentialDto> credentials, List<Employe> employes) 
            throws DocumentException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Polices
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, DARK_BLUE);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
        Font codeFont = FontFactory.getFont(FontFactory.COURIER, 10, BaseColor.BLACK);
        
        // Titre
        Paragraph title = new Paragraph("IDENTIFIANTS EMPLOYÉS", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        Paragraph subtitle = new Paragraph("Document confidentiel - À distribuer aux employés", normalFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
        
        // Tableau
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.5f, 2f, 2.5f, 1.5f});
        table.setSpacingBefore(10);
        
        // En-tête
        addTableHeader(table, "EMPLOYÉ", headerFont, DARK_BLUE);
        addTableHeader(table, "MATRICULE", headerFont, DARK_BLUE);
        addTableHeader(table, "IDENTIFIANT", headerFont, GOLD);
        addTableHeader(table, "MOT DE PASSE", headerFont, GOLD);
        
        // Données
        for (CredentialDto cred : credentials) {
            // Trouver l'employé correspondant
            Employe emp = employes.stream()
                .filter(e -> e.getId().equals(cred.getEmployeId()))
                .findFirst()
                .orElse(null);
            
            addTableCell(table, cred.getEmployeNom(), boldFont);
            addTableCell(table, emp != null ? emp.getMatricule() : "-", normalFont);
            addTableCell(table, cred.getUsername(), codeFont);
            addTableCell(table, cred.getPassword(), codeFont);
        }
        
        document.add(table);
        
        // Avertissement
        Paragraph warning = new Paragraph(
            "\n⚠️ IMPORTANT : Ces identifiants doivent être changés lors de la première connexion. " +
            "Conservez ce document en lieu sûr.", 
            FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(255, 0, 0)));
        warning.setSpacingBefore(20);
        document.add(warning);
        
        document.close();
        return baos.toByteArray();
    }

    /**
     * Génère un PDF individuel pour un employé
     */
    public static byte[] generateIndividualCredentialPDF(CredentialDto cred, Employe emp) 
            throws DocumentException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Polices
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, DARK_BLUE);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, new BaseColor(100, 100, 100));
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new BaseColor(80, 80, 80));
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 14, DARK_BLUE);
        Font codeFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 16, DARK_BLUE);
        Font warningFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new BaseColor(255, 0, 0));
        
        // En-tête
        Paragraph title = new Paragraph("IDENTIFIANT EMPLOYÉ", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        document.add(title);
        
        Paragraph subtitle = new Paragraph("Système de Gestion des Ressources Humaines", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(30);
        document.add(subtitle);
        
        // Ligne de séparation
        com.itextpdf.text.pdf.draw.LineSeparator line = new com.itextpdf.text.pdf.draw.LineSeparator();
        line.setLineColor(GOLD);
        line.setLineWidth(2);
        document.add(new Chunk(line));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        
        // Informations de l'employé
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1.2f, 2f});
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(20);
        
        addInfoRow(infoTable, "MATRICULE :", emp.getMatricule(), labelFont, valueFont, LIGHT_GRAY);
        addInfoRow(infoTable, "NOM :", emp.getNom(), labelFont, valueFont, BaseColor.WHITE);
        addInfoRow(infoTable, "PRÉNOM :", emp.getPrenom(), labelFont, valueFont, LIGHT_GRAY);
        addInfoRow(infoTable, "POSTE :", emp.getPoste(), labelFont, valueFont, BaseColor.WHITE);
        if (emp.getDepartement() != null) {
            addInfoRow(infoTable, "DÉPARTEMENT :", emp.getDepartement().getNom(), labelFont, valueFont, LIGHT_GRAY);
        }
        
        document.add(infoTable);
        
        // Section des identifiants
        PdfPTable credTable = new PdfPTable(1);
        credTable.setWidthPercentage(100);
        credTable.setSpacingBefore(20);
        credTable.setSpacingAfter(20);
        
        PdfPCell headerCell = new PdfPCell(new Phrase("VOS IDENTIFIANTS DE CONNEXION", 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE)));
        headerCell.setBackgroundColor(GOLD);
        headerCell.setPadding(15);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setBorder(Rectangle.NO_BORDER);
        credTable.addCell(headerCell);
        
        // Identifiant
        PdfPCell usernameCell = new PdfPCell();
        usernameCell.setBorder(Rectangle.BOX);
        usernameCell.setBorderColor(GOLD);
        usernameCell.setPadding(15);
        usernameCell.setBackgroundColor(new BaseColor(250, 250, 250));
        
        Paragraph userLabel = new Paragraph("Identifiant :", labelFont);
        userLabel.setSpacingAfter(5);
        usernameCell.addElement(userLabel);
        
        Paragraph userValue = new Paragraph(cred.getUsername(), codeFont);
        usernameCell.addElement(userValue);
        credTable.addCell(usernameCell);
        
        // Mot de passe
        PdfPCell passwordCell = new PdfPCell();
        passwordCell.setBorder(Rectangle.BOX);
        passwordCell.setBorderColor(GOLD);
        passwordCell.setPadding(15);
        passwordCell.setBackgroundColor(YELLOW_BG);
        
        Paragraph passLabel = new Paragraph("Mot de passe :", labelFont);
        passLabel.setSpacingAfter(5);
        passwordCell.addElement(passLabel);
        
        Paragraph passValue = new Paragraph(cred.getPassword(), codeFont);
        passwordCell.addElement(passValue);
        credTable.addCell(passwordCell);
        
        document.add(credTable);
        
        // Instructions
        Paragraph instructions = new Paragraph();
        instructions.setSpacingBefore(30);
        instructions.add(new Chunk("INSTRUCTIONS :", 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, DARK_BLUE)));
        instructions.add(new Chunk("\n\n", FontFactory.getFont(FontFactory.HELVETICA, 11)));
        instructions.add(new Chunk("1. ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK)));
        instructions.add(new Chunk("Connectez-vous au système avec vos identifiants ci-dessus\n", 
            FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK)));
        instructions.add(new Chunk("2. ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK)));
        instructions.add(new Chunk("Il est recommandé de changer votre mot de passe lors de la première connexion\n", 
            FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK)));
        instructions.add(new Chunk("3. ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK)));
        instructions.add(new Chunk("Conservez ce document dans un endroit sûr\n", 
            FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK)));
        document.add(instructions);
        
        // Avertissement
        PdfPTable warningTable = new PdfPTable(1);
        warningTable.setWidthPercentage(100);
        warningTable.setSpacingBefore(30);
        
        PdfPCell warningCell = new PdfPCell();
        warningCell.setBackgroundColor(WARNING_BG);
        warningCell.setBorder(Rectangle.LEFT);
        warningCell.setBorderColor(WARNING_BORDER);
        warningCell.setBorderWidth(4);
        warningCell.setPadding(15);
        
        Paragraph warning = new Paragraph();
        warning.add(new Chunk("⚠ CONFIDENTIEL\n", warningFont));
        warning.add(new Chunk(
            "Ce document contient des informations confidentielles. " +
            "Ne le partagez avec personne. En cas de perte, contactez immédiatement le service RH.",
            FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK)));
        warningCell.addElement(warning);
        warningTable.addCell(warningCell);
        
        document.add(warningTable);
        
        document.close();
        return baos.toByteArray();
    }

    private static void addTableHeader(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private static void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(new BaseColor(220, 220, 220));
        table.addCell(cell);
    }

    private static void addInfoRow(PdfPTable table, String label, String value, 
                                   Font labelFont, Font valueFont, BaseColor bgColor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(bgColor);
        labelCell.setPadding(10);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBackgroundColor(bgColor);
        valueCell.setPadding(10);
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }
}
