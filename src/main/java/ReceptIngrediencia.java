/*    */
/*    */ 
/*    */ import javafx.beans.property.DoubleProperty;
/*    */ import javafx.beans.property.IntegerProperty;
/*    */ import javafx.beans.property.SimpleDoubleProperty;
/*    */ import javafx.beans.property.SimpleIntegerProperty;
/*    */ import javafx.beans.property.SimpleStringProperty;
/*    */ import javafx.beans.property.StringProperty;
/*    */ 
/*    */ public class ReceptIngrediencia {
/*  6 */   private final IntegerProperty receptIngredienciaId = (IntegerProperty)new SimpleIntegerProperty(this, "receptIngredienciaId");
/*    */   
/*  7 */   private final StringProperty nazovIngrediencie = (StringProperty)new SimpleStringProperty(this, "nazovIngrediencie");
/*    */   
/*  8 */   private final DoubleProperty mnozstvo = (DoubleProperty)new SimpleDoubleProperty(this, "mnozstvo");
/*    */   
/*  9 */   private final StringProperty jednotka = (StringProperty)new SimpleStringProperty(this, "jednotka");
/*    */   
/*    */   public ReceptIngrediencia(int receptIngredienciaId, String nazovIngrediencie, double mnozstvo, String jednotka) {
/* 13 */     this.receptIngredienciaId.set(receptIngredienciaId);
/* 14 */     this.nazovIngrediencie.set(nazovIngrediencie);
/* 15 */     this.mnozstvo.set(mnozstvo);
/* 16 */     this.jednotka.set((jednotka != null) ? jednotka : "");
/*    */   }
/*    */   
/*    */   public int getReceptIngredienciaId() {
/* 21 */     return this.receptIngredienciaId.get();
/*    */   }
/*    */   
/*    */   public IntegerProperty receptIngredienciaIdProperty() {
/* 26 */     return this.receptIngredienciaId;
/*    */   }
/*    */   
/*    */   public String getNazovIngrediencie() {
/* 31 */     return (String)this.nazovIngrediencie.get();
/*    */   }
/*    */   
/*    */   public StringProperty nazovIngrediencieProperty() {
/* 35 */     return this.nazovIngrediencie;
/*    */   }
/*    */   
/*    */   public double getMnozstvo() {
/* 40 */     return this.mnozstvo.get();
/*    */   }
/*    */   
/*    */   public DoubleProperty mnozstvoProperty() {
/* 44 */     return this.mnozstvo;
/*    */   }
/*    */   
/*    */   public String getJednotka() {
/* 49 */     return (String)this.jednotka.get();
/*    */   }
/*    */   
/*    */   public StringProperty jednotkaProperty() {
/* 53 */     return this.jednotka;
/*    */   }
/*    */   
/*    */   public String toString() {
/* 58 */     return "" + this.mnozstvo.get() + " " + this.mnozstvo.get() + " " + (String)this.jednotka.get();
/*    */   }
/*    */ }


/* Location:              E:\Receptar\receptar.jar!\receptar\ReceptIngrediencia.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */