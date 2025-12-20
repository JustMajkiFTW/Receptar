/*    */
/*    */ 
/*    */ import javafx.beans.property.IntegerProperty;
/*    */ import javafx.beans.property.SimpleIntegerProperty;
/*    */ import javafx.beans.property.SimpleStringProperty;
/*    */ import javafx.beans.property.StringProperty;
/*    */ 
/*    */ public class Recept {
/*  9 */   private final IntegerProperty receptId = (IntegerProperty)new SimpleIntegerProperty();
/*    */   
/* 10 */   private final StringProperty nazov = (StringProperty)new SimpleStringProperty();
/*    */   
/* 11 */   private final StringProperty postup = (StringProperty)new SimpleStringProperty();
/*    */   
/* 12 */   private final IntegerProperty casPripravy = (IntegerProperty)new SimpleIntegerProperty();
/*    */   
/* 13 */   private final IntegerProperty pocetPorcii = (IntegerProperty)new SimpleIntegerProperty();
/*    */   
/* 14 */   private StringProperty obrazokCesta = (StringProperty)new SimpleStringProperty();
/*    */   
/* 15 */   private final IntegerProperty kategoriaId = (IntegerProperty)new SimpleIntegerProperty();
/*    */   
/* 16 */   private final StringProperty kategoriaNazov = (StringProperty)new SimpleStringProperty();
/*    */   
/* 17 */   private final IntegerProperty userId = (IntegerProperty)new SimpleIntegerProperty();
/*    */   
/* 18 */   private final StringProperty autor = (StringProperty)new SimpleStringProperty();
/*    */   
/*    */   public Recept(int receptId, String nazov, String postup, int casPripravy, int pocetPorcii, String cesta, int kategoriaId, String kategoriaNazov, int userId, String autor) {
/* 23 */     this.receptId.set(receptId);
/* 24 */     this.nazov.set(nazov);
/* 25 */     this.postup.set(postup);
/* 26 */     this.casPripravy.set(casPripravy);
/* 27 */     this.pocetPorcii.set(pocetPorcii);
/* 28 */     this.obrazokCesta = (StringProperty)new SimpleStringProperty(cesta);
/* 29 */     this.kategoriaId.set(kategoriaId);
/* 30 */     this.kategoriaNazov.set(kategoriaNazov);
/* 31 */     this.userId.set(userId);
/* 32 */     this.autor.set(autor);
/*    */   }
/*    */   
/*    */   public IntegerProperty receptIdProperty() {
/* 36 */     return this.receptId;
/*    */   }
/*    */   
/*    */   public StringProperty nazovProperty() {
/* 37 */     return this.nazov;
/*    */   }
/*    */   
/*    */   public StringProperty postupProperty() {
/* 38 */     return this.postup;
/*    */   }
/*    */   
/*    */   public IntegerProperty casPripravyProperty() {
/* 39 */     return this.casPripravy;
/*    */   }
/*    */   
/*    */   public IntegerProperty pocetPorciiProperty() {
/* 40 */     return this.pocetPorcii;
/*    */   }
/*    */   
/*    */   public StringProperty obrazokCestaProperty() {
/* 41 */     return this.obrazokCesta;
/*    */   }
/*    */   
/*    */   public IntegerProperty kategoriaIdProperty() {
/* 42 */     return this.kategoriaId;
/*    */   }
/*    */   
/*    */   public StringProperty kategoriaNazovProperty() {
/* 43 */     return this.kategoriaNazov;
/*    */   }
/*    */   
/*    */   public IntegerProperty userIdProperty() {
/* 44 */     return this.userId;
/*    */   }
/*    */   
/*    */   public StringProperty autorProperty() {
/* 45 */     return this.autor;
/*    */   }
/*    */   
/*    */   public int getReceptId() {
/* 48 */     return this.receptId.get();
/*    */   }
/*    */   
/*    */   public String getNazov() {
/* 49 */     return (String)this.nazov.get();
/*    */   }
/*    */   
/*    */   public String getPostup() {
/* 50 */     return (String)this.postup.get();
/*    */   }
/*    */   
/*    */   public int getCasPripravy() {
/* 51 */     return this.casPripravy.get();
/*    */   }
/*    */   
/*    */   public int getPocetPorcii() {
/* 52 */     return this.pocetPorcii.get();
/*    */   }
/*    */   
/*    */   public String getObrazokCesta() {
/* 53 */     return (String)this.obrazokCesta.get();
/*    */   }
/*    */   
/*    */   public int getKategoriaId() {
/* 54 */     return this.kategoriaId.get();
/*    */   }
/*    */   
/*    */   public String getKategoriaNazov() {
/* 55 */     return (String)this.kategoriaNazov.get();
/*    */   }
/*    */   
/*    */   public String getAutor() {
/* 56 */     return (String)this.autor.get();
/*    */   }
/*    */   
/*    */   public String toString() {
/* 60 */     return (String)this.nazov.get();
/*    */   }
/*    */   
/*    */   public void setObrazokCesta(String obrazokCesta) {
/* 64 */     this.obrazokCesta.set(obrazokCesta);
/*    */   }
/*    */ }


/* Location:              E:\Receptar\receptar.jar!\receptar\Recept.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */