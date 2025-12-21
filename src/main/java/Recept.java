import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelová trieda reprezentujúca komplexný Recept.
 * Obsahuje všetky informácie o recepte vrátane autora, kategórie a cesty k obrázku.
 * Implementácia využíva JavaFX Properties pre jednoduchú integráciu s UI.
 */
public class Recept {

    private final IntegerProperty receptId = new SimpleIntegerProperty();
    private final StringProperty nazov = new SimpleStringProperty();
    private final StringProperty postup = new SimpleStringProperty();
    private final IntegerProperty casPripravy = new SimpleIntegerProperty();
    private final IntegerProperty pocetPorcii = new SimpleIntegerProperty();
    private StringProperty obrazokCesta = new SimpleStringProperty();
    private final IntegerProperty kategoriaId = new SimpleIntegerProperty();
    private final StringProperty kategoriaNazov = new SimpleStringProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty autor = new SimpleStringProperty();

    /**
     * Konštruktor pre vytvorenie úplnej inštancie receptu.
     * * @param receptId       Jedinečné ID receptu
     * @param nazov          Názov jedla
     * @param postup         Textový opis prípravy
     * @param casPripravy    Čas v minútach
     * @param pocetPorcii    Pre koľko osôb je recept určený
     * @param cesta          Cesta k súboru obrázka
     * @param kategoriaId    ID prislúchajúcej kategórie
     * @param kategoriaNazov Slovný názov kategórie
     * @param userId         ID používateľa, ktorý recept pridal
     * @param autor          Meno autora receptu
     */
    public Recept(int receptId, String nazov, String postup, int casPripravy, int pocetPorcii,
                  String cesta, int kategoriaId, String kategoriaNazov, int userId, String autor) {
        this.receptId.set(receptId);
        this.nazov.set(nazov);
        this.postup.set(postup);
        this.casPripravy.set(casPripravy);
        this.pocetPorcii.set(pocetPorcii);
        this.obrazokCesta.set(cesta);
        this.kategoriaId.set(kategoriaId);
        this.kategoriaNazov.set(kategoriaNazov);
        this.userId.set(userId);
        this.autor.set(autor);
    }

    // --- Property metódy pre JavaFX Binding ---

    public IntegerProperty receptIdProperty() { return this.receptId; }
    public StringProperty nazovProperty() { return this.nazov; }
    public StringProperty postupProperty() { return this.postup; }
    public IntegerProperty casPripravyProperty() { return this.casPripravy; }
    public IntegerProperty pocetPorciiProperty() { return this.pocetPorcii; }
    public StringProperty obrazokCestaProperty() { return this.obrazokCesta; }
    public IntegerProperty kategoriaIdProperty() { return this.kategoriaId; }
    public StringProperty kategoriaNazovProperty() { return this.kategoriaNazov; }
    public IntegerProperty userIdProperty() { return this.userId; }
    public StringProperty autorProperty() { return this.autor; }

    // --- Klasické Gettery ---

    public int getReceptId() { return this.receptId.get(); }
    public String getNazov() { return this.nazov.get(); }
    public String getPostup() { return this.postup.get(); }
    public int getCasPripravy() { return this.casPripravy.get(); }
    public int getPocetPorcii() { return this.pocetPorcii.get(); }
    public String getObrazokCesta() { return this.obrazokCesta.get(); }
    public int getKategoriaId() { return this.kategoriaId.get(); }
    public String getKategoriaNazov() { return this.kategoriaNazov.get(); }
    public String getAutor() { return this.autor.get(); }

    /**
     * Nastaví novú cestu k obrázku receptu.
     * @param obrazokCesta Cesta k súboru
     */
    public void setObrazokCesta(String obrazokCesta) {
        this.obrazokCesta.set(obrazokCesta);
    }

    /**
     * Vráti textovú reprezentáciu receptu (názov).
     * @return Názov receptu
     */
    @Override
    public String toString() {
        return this.nazov.get();
    }
}