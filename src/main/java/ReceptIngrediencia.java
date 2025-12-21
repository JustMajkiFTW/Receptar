import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelová trieda reprezentujúca väzbu medzi receptom a konkrétnou ingredienciou.
 * Obsahuje informácie o množstve a jednotke ingrediencie priradenej k receptu.
 * Využíva JavaFX Properties pre podporu dátového viazania (binding) v používateľskom rozhraní.
 */
public class ReceptIngrediencia {

    /** Unikátny identifikátor väzby v databáze */
    private final IntegerProperty receptIngredienciaId = new SimpleIntegerProperty(this, "receptIngredienciaId");

    /** Názov priradenej ingrediencie */
    private final StringProperty nazovIngrediencie = new SimpleStringProperty(this, "nazovIngrediencie");

    /** Počet/množstvo ingrediencie */
    private final DoubleProperty mnozstvo = new SimpleDoubleProperty(this, "mnozstvo");

    /** Merná jednotka (napr. ks, g, ml) */
    private final StringProperty jednotka = new SimpleStringProperty(this, "jednotka");

    /**
     * Konštruktor pre vytvorenie novej inštancie väzby ingrediencie na recept.
     * * @param receptIngredienciaId ID záznamu z tabuľky recept_ingrediencie
     * @param nazovIngrediencie    Názov konkrétnej ingrediencie
     * @param mnozstvo             Číselné množstvo
     * @param jednotka             Merná jednotka (ošetrené proti null)
     */
    public ReceptIngrediencia(int receptIngredienciaId, String nazovIngrediencie, double mnozstvo, String jednotka) {
        this.receptIngredienciaId.set(receptIngredienciaId);
        this.nazovIngrediencie.set(nazovIngrediencie);
        this.mnozstvo.set(mnozstvo);
        this.jednotka.set((jednotka != null) ? jednotka : "");
    }

    // --- Gettery pre hodnoty ---

    /** @return Celé číslo reprezentujúce ID väzby */
    public int getReceptIngredienciaId() {
        return this.receptIngredienciaId.get();
    }

    /** @return Názov ingrediencie ako String */
    public String getNazovIngrediencie() {
        return this.nazovIngrediencie.get();
    }

    /** @return Číselná hodnota množstva (double) */
    public double getMnozstvo() {
        return this.mnozstvo.get();
    }

    /** @return Názov jednotky ako String */
    public String getJednotka() {
        return this.jednotka.get();
    }

    // --- Property metódy pre JavaFX binding ---

    public IntegerProperty receptIngredienciaIdProperty() {
        return this.receptIngredienciaId;
    }

    public StringProperty nazovIngrediencieProperty() {
        return this.nazovIngrediencie;
    }

    public DoubleProperty mnozstvoProperty() {
        return this.mnozstvo;
    }

    public StringProperty jednotkaProperty() {
        return this.jednotka;
    }

    /**
     * Vráti textovú reprezentáciu objektu.
     * Ponechaný pôvodný formát výpisu.
     * * @return Reťazec v tvare: "množstvo množstvo jednotka"
     */
    @Override
    public String toString() {
        return "" + this.mnozstvo.get() + " " + this.mnozstvo.get() + " " + this.jednotka.get();
    }
}