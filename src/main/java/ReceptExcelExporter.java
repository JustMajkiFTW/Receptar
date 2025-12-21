import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Trieda zabezpečujúca export konkrétneho receptu do formátu Microsoft Excel (.xlsx).
 * Využíva knižnicu Apache POI pre prácu s tabuľkovými dokumentmi.
 */
public class ReceptExcelExporter {

    /** Pripojenie k databáze pre načítanie ingrediencií */
    private final DBConnect db = new DBConnect();

    /**
     * Vygeneruje Excel súbor pre zadaný recept.
     * Súbor obsahuje základné informácie, postup, zoznam ingrediencií a obrázok.
     * * @param recept Objekt receptu, ktorý sa má exportovať.
     */
    public void export(Recept recept) {
        if (recept == null) {
            System.out.println("Žiadny recept na export!");
            return;
        }

        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Recept");

            // --- Definícia štýlov ---

            // Štýl pre hlavičky sekcií
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 14);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Štýl pre hlavný názov receptu
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 18);
            titleStyle.setFont(titleFont);

            int rowNum = 0;

            // --- Názov receptu ---
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(recept.getNazov());
            titleCell.setCellStyle(titleStyle);
            // Zlúčenie buniek pre nadpis (stĺpce A až E)
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            rowNum++; // Vynechaný riadok

            // --- Základné informácie o recepte ---
            Row infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Čas prípravy:");
            infoRow.createCell(1).setCellValue(recept.getCasPripravy() + " min");

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Počet porcií:");
            infoRow.createCell(1).setCellValue(String.valueOf(recept.getPocetPorcii()));

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Kategória:");
            infoRow.createCell(1).setCellValue(recept.getKategoriaNazov());

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Autor:");
            infoRow.createCell(1).setCellValue(recept.getAutor());

            rowNum++; // Vynechaný riadok

            // --- Postup prípravy ---
            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true); // Povolenie zalamovania textu
            wrapStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);

            Row postupRow = sheet.createRow(rowNum++);
            postupRow.createCell(0).setCellValue("Postup:");

            Cell postupCell = postupRow.createCell(1);
            postupCell.setCellValue(recept.getPostup());
            postupCell.setCellStyle(wrapStyle);

            // Nastavenie fixnej šírky pre stĺpec s postupom a automatická výška riadku
            sheet.setColumnWidth(1, 15000);
            postupRow.setHeight((short)-1);

            // --- Vloženie obrázka z webovej adresy ---
            String cestaZDatabazy = recept.getObrazokCesta();
            if (cestaZDatabazy != null && !cestaZDatabazy.trim().isEmpty()) {
                String fullUrl;
                if (cestaZDatabazy.startsWith("http")) {
                    fullUrl = cestaZDatabazy;
                } else {
                    fullUrl = "https://cambalik.eu/ReceptyApp/" + recept.getReceptId() + "/" + cestaZDatabazy;
                }

                byte[] imageBytes = downloadImageFromHTTP(fullUrl);

                if (imageBytes != null && imageBytes.length > 0) {
                    // Určenie formátu obrázka
                    int pictureType = fullUrl.toLowerCase().endsWith(".png")
                            ? Workbook.PICTURE_TYPE_PNG
                            : Workbook.PICTURE_TYPE_JPEG;

                    int pictureIdx = workbook.addPicture(imageBytes, pictureType);
                    CreationHelper helper = workbook.getCreationHelper();
                    Drawing<?> drawing = sheet.createDrawingPatriarch();

                    // Umiestnenie obrázka (stĺpec D, riadok 3)
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(3);
                    anchor.setRow1(2);

                    Picture pict = drawing.createPicture(anchor, pictureIdx);
                    pict.resize(0.6); // Zmenšenie mierky obrázka
                }
            }

            // Odsadenie pre ingrediencie, aby sa neprekrývali s obrázkom
            rowNum += 6;

            // --- Zoznam ingrediencií ---
            Row ingHeader = sheet.createRow(rowNum++);
            ingHeader.createCell(0).setCellValue("Ingrediencie");
            ingHeader.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

            // Načítanie ingrediencií z DB a zápis do riadkov
            List<IngredienciaMnozstvo> ingrediencie = db.nacitajIngredienciePreRecept(recept.getReceptId());
            for (IngredienciaMnozstvo ing : ingrediencie) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(ing.getMnozstvo() + " " + ing.getJednotka());
                r.createCell(1).setCellValue(ing.getNazovIngrediencie());
            }

            // Prispôsobenie šírky stĺpcov podľa obsahu
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // --- Uloženie výsledného súboru ---
            String fileName = "Recept_" + recept.getNazov().replaceAll("[\\\\/:*?\"<>|]", "_") + ".xlsx";
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                workbook.write(fos);
                System.out.println("Export úspešný: " + fileName);
            }

        } catch (Exception ex) {
            System.err.println("Chyba pri exporte: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Stiahne binárne dáta obrázka z HTTP adresy.
     * * @param urlPath Kompletná URL adresa k obrázku.
     * @return Pole bajtov obrázka alebo null v prípade chyby.
     */
    private byte[] downloadImageFromHTTP(String urlPath) {
        try {
            System.out.println("Sťahujem obrázok: " + urlPath);
            URL url = new URL(urlPath);
            try (InputStream is = url.openStream()) {
                return IOUtils.toByteArray(is);
            }
        } catch (Exception e) {
            System.err.println("Obrázok sa nepodarilo stiahnuť z URL: " + urlPath);
            return null;
        }
    }
}