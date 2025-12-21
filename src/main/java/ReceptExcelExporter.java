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

public class ReceptExcelExporter {
    private final DBConnect db = new DBConnect();

    public void export(Recept recept) {
        if (recept == null) {
            System.out.println("Žiadny recept na export!");
            return;
        }

        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Recept");

            // Štýly
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 14);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 18);
            titleStyle.setFont(titleFont);

            int rowNum = 0;

            // Názov receptu
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(recept.getNazov());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            rowNum++; // prázdny riadok

            // Informácie
            Row infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Čas prípravy:");
            infoRow.createCell(1).setCellValue(recept.getCasPripravy() + " min");

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Počet porcií:");
            infoRow.createCell(1).setCellValue("" + recept.getPocetPorcii());

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Kategória:");
            infoRow.createCell(1).setCellValue(recept.getKategoriaNazov());

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Autor:");
            infoRow.createCell(1).setCellValue(recept.getAutor());

            rowNum++; // prázdny riadok

            // Postup
            // 1. Vytvorenie štýlu pre zalamovanie textu
            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true); // Toto povolí zalamovanie
            wrapStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP); // Text začne zhora bunky

            // 2. Vytvorenie riadku a buniek
            Row postupRow = sheet.createRow(rowNum++);
            postupRow.createCell(0).setCellValue("Postup:");

            Cell postupCell = postupRow.createCell(1);
            postupCell.setCellValue(recept.getPostup());

// 3. Priradenie štýlu bunke
            postupCell.setCellStyle(wrapStyle);

// 4. Nastavenie šírky stĺpca (stĺpec B má index 1)
            sheet.setColumnWidth(1, 15000);

// 5. Automatické nastavenie výšky riadku (Excel ju prispôsobí podľa množstva textu)
            postupRow.setHeight((short)-1);

            // --- VLOŽENIE OBRÁZKA CEZ HTTP ---
            String cestaZDatabazy = recept.getObrazokCesta();

            if (cestaZDatabazy != null && !cestaZDatabazy.trim().isEmpty()) {
                String fullUrl;

                // Kontrola: Ak už cesta začína na http, nepripájame doménu znova
                if (cestaZDatabazy.startsWith("http")) {
                    fullUrl = cestaZDatabazy;
                } else {
                    // Ak je v databáze len "recept_1.jpg", vtedy ju poskladáme
                    fullUrl = "https://cambalik.eu/ReceptyApp/" + recept.getReceptId() + "/" + cestaZDatabazy;
                }

                byte[] imageBytes = downloadImageFromHTTP(fullUrl);

                if (imageBytes != null && imageBytes.length > 0) {
                    // ... (zvyšok kódu pre vloženie do Excelu ostáva rovnaký) ...
                    int pictureType = fullUrl.toLowerCase().endsWith(".png")
                            ? Workbook.PICTURE_TYPE_PNG
                            : Workbook.PICTURE_TYPE_JPEG;

                    int pictureIdx = workbook.addPicture(imageBytes, pictureType);
                    CreationHelper helper = workbook.getCreationHelper();
                    Drawing<?> drawing = sheet.createDrawingPatriarch();

                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(3);
                    anchor.setRow1(2);

                    Picture pict = drawing.createPicture(anchor, pictureIdx);
                    pict.resize(0.6);
                }
            }

            rowNum += 6; // Priestor pod informáciami, aby sa obrázok neprekrýval s textom dolu

            // Ingrediencie - hlavička
            Row ingHeader = sheet.createRow(rowNum++);
            ingHeader.createCell(0).setCellValue("Ingrediencie");
            ingHeader.getCell(0).setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

            // Zoznam ingrediencií z DB
            List<IngredienciaMnozstvo> ingrediencie = db.nacitajIngredienciePreRecept(recept.getReceptId());
            for (IngredienciaMnozstvo ing : ingrediencie) {
                Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(ing.getMnozstvo() + " " + ing.getJednotka());
                r.createCell(1).setCellValue(ing.getNazovIngrediencie());
            }

            // Automatické prispôsobenie stĺpcov
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // Uloženie súboru
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

    // Pomocná metóda na stiahnutie dát z webovej adresy
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