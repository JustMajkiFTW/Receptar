/**
 * Modelová trieda reprezentujúca kategóriu receptov.
 * Slúži na klasifikáciu receptov do skupín pre lepšiu prehľadnosť v systéme.
 */
public class Kategoria {

    /** Unikátny identifikátor kategórie v databáze */
    private final int id;

    /** Názov kategórie */
    private final String nazov;

    /**
     * Konštruktor pre vytvorenie novej inštancie kategórie.
     * * @param id    Jedinečné ID kategórie
     * @param nazov Slovný názov kategórie
     */
    public Kategoria(int id, String nazov) {
        this.id = id;
        this.nazov = nazov;
    }

    /**
     * Získa identifikátor kategórie.
     * * @return Celé číslo (ID)
     */
    public int getId() {
        return this.id;
    }

    /**
     * Získa názov kategórie.
     * * @return String s názvom kategórie
     */
    public String getNazov() {
        return this.nazov;
    }

    /**
     * Prekrytá metóda toString, ktorá vracia názov kategórie.
     * Táto implementácia umožňuje priame zobrazenie názvu v komponentoch
     * ako ComboBox alebo ListView bez nutnosti definovať vlastný CellFactory.
     * * @return Názov kategórie
     */
    @Override
    public String toString() {
        return this.nazov;
    }
}