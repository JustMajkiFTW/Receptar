/**
 * Spúšťacia trieda (Launcher) aplikácie.
 * Táto trieda slúži ako vstupný bod (entry point) do programu.
 * * Poznámka: Samostatný Launcher je v JavaFX často nevyhnutný pre správne
 * spustenie zkompilovaného JAR súboru, aby sa predišlo problémom s hľadaním
 * modulov JavaFX runtime.
 */
public class Launcher {

    /**
     * Hlavná metóda, ktorá inicializuje celý program.
     * Deleguje spustenie na triedu LoginApp, ktorá spravuje životný cyklus JavaFX aplikácie.
     * * @param args Argumenty príkazového riadka odovzdané pri spustení.
     */
    public static void main(String[] args) {
        // Volá statickú metódu main hlavnej triedy LoginApp, ktorá extenduje Application
        LoginApp.main(args);
    }
}