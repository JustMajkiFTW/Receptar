import com.google.gson.JsonArray;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Modelová trieda reprezentujúca entitu Ingrediencia.
 * Používa JavaFX Properties, čo umožňuje sledovanie zmien a binding v UI komponentoch.
 */
public class Ingrediencia {

    public double getCaloriesFromInternet(String ingrediencia, double gramy) {
        // SEM VLOŽ SVOJ KĽÚČ, KTORÝ TI PRIŠIEL MAILOM
        String API_KEY = "gFhumwigl9ZLEbjh2HDgebbeYk9J1By6c2xBg2Ft";

        // USDA vyžaduje kódovanie názvu pre URL (napr. medzery na %20)
        String query = ingrediencia.replace(" ", "%20");
        String url = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key="
                + API_KEY + "&query=" + query + "&pageSize=1";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray foods = jsonResponse.getAsJsonArray("foods");

            if (foods != null && foods.size() > 0) {
                JsonObject firstFood = foods.get(0).getAsJsonObject();
                JsonArray foodNutrients = firstFood.getAsJsonArray("foodNutrients");

                // Hľadáme nutrient, ktorý má v názve "Energy" a jednotku "KCAL"
                for (int i = 0; i < foodNutrients.size(); i++) {
                    JsonObject nutrient = foodNutrients.get(i).getAsJsonObject();
                    String name = nutrient.get("nutrientName").getAsString();
                    String unit = nutrient.get("unitName").getAsString();

                    if (name.contains("Energy") && unit.equalsIgnoreCase("KCAL")) {
                        double kcalPer100g = nutrient.get("value").getAsDouble();
                        return (kcalPer100g / 100.0) * gramy;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Chyba pri komunikácii s USDA: " + e.getMessage());
        }
        return 0.0;
    }

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