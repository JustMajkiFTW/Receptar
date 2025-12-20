/*    */

/*    */
/*    */ public class IngredienciaMnozstvo {
/*    */   private final String nazov;
/*    */   
/*    */   private final double mnozstvo;
/*    */   
/*    */   private final String jednotka;
/*    */   
/*    */   public IngredienciaMnozstvo(String nazov, double mnozstvo, String jednotka) {
/*  9 */     this.nazov = (nazov != null) ? nazov : "Neznáma ingrediencia";
/* 10 */     this.mnozstvo = mnozstvo;
/* 11 */     this.jednotka = (jednotka != null) ? jednotka : "";
/*    */   }
/*    */   
/*    */   public String getNazov() {
/* 15 */     return this.nazov;
/*    */   }
/*    */   
/*    */   public String getNazovIngrediencie() {
/* 19 */     return this.nazov;
/*    */   }
/*    */   
/*    */   public double getMnozstvo() {
/* 23 */     return this.mnozstvo;
/*    */   }
/*    */   
/*    */   public String getJednotka() {
/* 27 */     return this.jednotka;
/*    */   }
/*    */   
/*    */   public String toString() {
/* 32 */     return String.format("%.2f %s %s", new Object[] { Double.valueOf(this.mnozstvo), this.jednotka, this.nazov }).trim();
/*    */   }
/*    */ }


/* Location:              E:\Receptar\receptar.jar!\receptar\IngredienciaMnozstvo.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */