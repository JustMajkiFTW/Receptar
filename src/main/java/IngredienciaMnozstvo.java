/**
 * Modelová trieda reprezentujúca konkrétne množstvo ingrediencie v recepte.
 * Na rozdiel od základnej triedy Ingrediencia obsahuje aj číselný údaj o množstve a mernú jednotku.
 */
public class IngredienciaMnozstvo {

    /** Názov ingrediencie */
    private final String nazov;

    /** Číselná hodnota množstva */
    private final double mnozstvo;

    /** Merná jednotka (napr. g, kg, ks, ml) */
    private final String jednotka;

    /**
     * Konštruktor pre vytvorenie záznamu o množstve ingrediencie.
     * Vykonáva základné ošetrenie vstupov (null safety).
     * * @param nazov    Názov ingrediencie (ak je null, nastaví sa predvolená hodnota)
     * @param mnozstvo Číselné množstvo
     * @param jednotka Merná jednotka (ak je null, nastaví sa prázdny reťazec)
     */
    public IngredienciaMnozstvo(String nazov, double mnozstvo, String jednotka) {
        this.nazov = (nazov != null) ? nazov : "Neznáma ingrediencia";
        this.mnozstvo = mnozstvo;
        this.jednotka = (jednotka != null) ? jednotka : "";
    }

    /**
     * Získa názov ingrediencie.
     * @return String s názvom
     */
    public String getNazov() {
        return this.nazov;
    }

    /**
     * Alias metóda pre získanie názvu ingrediencie (pre kompatibilitu).
     * @return String s názvom
     */
    public String getNazovIngrediencie() {
        return this.nazov;
    }

    /**
     * Získa číselnú hodnotu množstva.
     * @return double hodnota množstva
     */
    public double getMnozstvo() {
        return this.mnozstvo;
    }

    /**
     * Získa mernú jednotku.
     * @return String s jednotkou
     */
    public String getJednotka() {
        return this.jednotka;
    }

    /**
     * Prekrytá metóda toString pre formátovaný výpis ingrediencie.
     * Formátuje množstvo na dve desatinné miesta.
     * * @return Formátovaný reťazec v tvare "množstvo jednotka názov"
     */
    @Override
    public String toString() {
        return String.format("%.2f %s %s", this.mnozstvo, this.jednotka, this.nazov).trim();
    }
}