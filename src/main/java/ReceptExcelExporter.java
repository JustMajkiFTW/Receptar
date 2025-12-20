/*    */
/*    */ 
/*    */ import java.io.FileOutputStream;
/*    */ import java.util.List;
/*    */ import org.apache.poi.ss.usermodel.Cell;
/*    */ import org.apache.poi.ss.usermodel.CellStyle;
/*    */ import org.apache.poi.ss.usermodel.Font;
/*    */ import org.apache.poi.ss.usermodel.HorizontalAlignment;
/*    */ import org.apache.poi.ss.usermodel.Row;
/*    */ import org.apache.poi.ss.usermodel.Sheet;
/*    */ import org.apache.poi.ss.util.CellRangeAddress;
/*    */ import org.apache.poi.xssf.usermodel.XSSFWorkbook;
/*    */ 
/*    */ public class ReceptExcelExporter {
/* 11 */   private final DBConnect db = new DBConnect();
/*    */   
/*    */   public void export(Recept recept) {
/* 15 */     if (recept == null) {
/* 16 */       System.out.println("Žiadny recept na export!");
/*    */       return;
/*    */     } 
/*    */     try {
/* 20 */       XSSFWorkbook xSSFWorkbook = new XSSFWorkbook();
/*    */       try {
/* 21 */         Sheet sheet = xSSFWorkbook.createSheet("Recept");
/* 24 */         CellStyle headerStyle = xSSFWorkbook.createCellStyle();
/* 25 */         Font font = xSSFWorkbook.createFont();
/* 26 */         font.setBold(true);
/* 27 */         font.setFontHeightInPoints((short)14);
/* 28 */         headerStyle.setFont(font);
/* 29 */         headerStyle.setAlignment(HorizontalAlignment.CENTER);
/* 31 */         CellStyle titleStyle = xSSFWorkbook.createCellStyle();
/* 32 */         Font titleFont = xSSFWorkbook.createFont();
/* 33 */         titleFont.setBold(true);
/* 34 */         titleFont.setFontHeightInPoints((short)18);
/* 35 */         titleStyle.setFont(titleFont);
/* 37 */         int rowNum = 0;
/* 40 */         Row titleRow = sheet.createRow(rowNum++);
/* 41 */         Cell titleCell = titleRow.createCell(0);
/* 42 */         titleCell.setCellValue(recept.getNazov());
/* 43 */         titleCell.setCellStyle(titleStyle);
/* 44 */         sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
/* 46 */         rowNum++;
/* 47 */         Row infoRow = sheet.createRow(rowNum++);
/* 48 */         infoRow.createCell(0).setCellValue("Čas prípravy:");
/* 49 */         infoRow.createCell(1).setCellValue("" + recept.getCasPripravy() + " min");
/* 51 */         infoRow = sheet.createRow(rowNum++);
/* 52 */         infoRow.createCell(0).setCellValue("Počet porcií:");
/* 53 */         infoRow.createCell(1).setCellValue("" + recept.getPocetPorcii());
/* 55 */         infoRow = sheet.createRow(rowNum++);
/* 56 */         infoRow.createCell(0).setCellValue("Kategória:");
/* 57 */         infoRow.createCell(1).setCellValue(recept.getKategoriaNazov());
/* 59 */         infoRow = sheet.createRow(rowNum++);
/* 60 */         infoRow.createCell(0).setCellValue("Autor:");
/* 61 */         infoRow.createCell(1).setCellValue(recept.getAutor());
/* 63 */         rowNum++;
/* 64 */         Row postupRow = sheet.createRow(rowNum++);
/* 65 */         postupRow.createCell(0).setCellValue("Postup:");
/* 66 */         Cell postupCell = postupRow.createCell(1);
/* 67 */         postupCell.setCellValue(recept.getPostup());
/* 68 */         sheet.setColumnWidth(1, 12800);
/* 71 */         rowNum += 2;
/* 72 */         Row ingHeader = sheet.createRow(rowNum++);
/* 73 */         ingHeader.createCell(0).setCellValue("Ingrediencie");
/* 74 */         ingHeader.getCell(0).setCellStyle(headerStyle);
/* 75 */         sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));
/* 77 */         List<IngredienciaMnozstvo> ingrediencie = this.db.nacitajIngredienciePreRecept(recept.getReceptId());
/* 78 */         for (IngredienciaMnozstvo ing : ingrediencie) {
/* 79 */           Row r = sheet.createRow(rowNum++);
/* 80 */           r.createCell(0).setCellValue("" + ing.getMnozstvo() + " " + ing.getMnozstvo());
/* 81 */           r.createCell(1).setCellValue(ing.getNazovIngrediencie());
/*    */         } 
/* 85 */         String fileName = "Recept_" + recept.getNazov().replaceAll("[\\\\/:*?\"<>|]", "_") + ".xlsx";
/* 86 */         FileOutputStream fos = new FileOutputStream(fileName);
/*    */         try {
/* 87 */           xSSFWorkbook.write(fos);
/* 88 */           System.out.println("Exportované: " + fileName);
/* 89 */           fos.close();
/*    */         } catch (Throwable throwable) {
/*    */           try {
/*    */             fos.close();
/*    */           } catch (Throwable throwable1) {
/*    */             throwable.addSuppressed(throwable1);
/*    */           } 
/*    */           throw throwable;
/*    */         } 
/* 91 */         xSSFWorkbook.close();
/*    */       } catch (Throwable throwable) {
/*    */         try {
/*    */           xSSFWorkbook.close();
/*    */         } catch (Throwable throwable1) {
/*    */           throwable.addSuppressed(throwable1);
/*    */         } 
/*    */         throw throwable;
/*    */       } 
/* 91 */     } catch (Exception ex) {
/* 92 */       ex.printStackTrace();
/*    */     } 
/*    */   }
/*    */ }


/* Location:              E:\Receptar\receptar.jar!\receptar\ReceptExcelExporter.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */