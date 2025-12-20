/*    */
/*    */ 
/*    */ import javafx.beans.property.IntegerProperty;
/*    */ import javafx.beans.property.SimpleIntegerProperty;
/*    */ import javafx.beans.property.SimpleStringProperty;
/*    */ import javafx.beans.property.StringProperty;
/*    */ 
/*    */ public class Ingrediencia {
/*    */   private final IntegerProperty id;
/*    */   
/*    */   private final StringProperty nazov;
/*    */   
/*    */   public Ingrediencia(int id, String nazov) {
/* 19 */     this.id = (IntegerProperty)new SimpleIntegerProperty(id);
/* 20 */     this.nazov = (StringProperty)new SimpleStringProperty(nazov);
/*    */   }
/*    */   
/*    */   public IntegerProperty idProperty() {
/* 23 */     return this.id;
/*    */   }
/*    */   
/*    */   public StringProperty nazovProperty() {
/* 25 */     return this.nazov;
/*    */   }
/*    */   
/*    */   public int getId() {
/* 28 */     return this.id.get();
/*    */   }
/*    */   
/*    */   public String getNazov() {
/* 29 */     return (String)this.nazov.get();
/*    */   }
/*    */   
/*    */   public String toString() {
/* 32 */     return getNazov();
/*    */   }
/*    */ }


/* Location:              E:\Receptar\receptar.jar!\receptar\Ingrediencia.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */