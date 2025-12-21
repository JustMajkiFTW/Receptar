import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelová trieda reprezentujúca entitu Ingrediencia.
 * Používa JavaFX Properties, čo umožňuje sledovanie zmien a binding v UI komponentoch.
 */
public class Ingrediencia {

    /** Unikátny identifikátor ingrediencie */
    private final IntegerProperty id;

    /** Názov ingrediencie */
    private final StringProperty nazov;

    /**
     * Konštruktor pre vytvorenie novej inštancie ingrediencie.
     * * @param id    Jedinečné ID z databázy
     * @param nazov Slovné označenie ingrediencie
     */
    public Ingrediencia(int id, String nazov) {
        this.id = new SimpleIntegerProperty(id);
        this.nazov = new SimpleStringProperty(nazov);
    }

    /**
     * Vráti property objekt pre ID. Používa sa pri JavaFX bindingu.
     * @return IntegerProperty objekt identifikátora
     */
    public IntegerProperty idProperty() {
        return this.id;
    }

    /**
     * Vráti property objekt pre názov. Používa sa pri JavaFX bindingu.
     * @return StringProperty objekt názvu
     */
    public StringProperty nazovProperty() {
        return this.nazov;
    }

    /**
     * Získa číselnú hodnotu ID ingrediencie.
     * @return Celé číslo reprezentujúce ID
     */
    public int getId() {
        return this.id.get();
    }

    /**
     * Získa textovú hodnotu názvu ingrediencie.
     * @return String s názvom ingrediencie
     */
    public String getNazov() {
        return this.nazov.get();
    }

    /**
     * Prekrytá metóda toString, ktorá vracia názov ingrediencie.
     * Toto je užitočné napríklad pri zobrazení v ListView alebo ComboBoxe bez vlastnej bunky (cell factory).
     * * @return Názov ingrediencie
     */
    @Override
    public String toString() {
        return getNazov();
    }
}