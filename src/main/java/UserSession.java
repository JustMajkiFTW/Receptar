/**
 * Trieda UserSession implementuje návrhový vzor Singleton a slúži na ukladanie
 * údajov o aktuálne prihlásenom používateľovi.
 * Umožňuje pristupovať k informáciám o používateľovi (ID, meno, rola) z ktorejkoľvek
 * časti aplikácie počas trvania relácie.
 */
public class UserSession {

    /** Jediná existujúca inštancia aktuálnej relácie */
    private static UserSession instance;

    /** Unikátne ID používateľa z databázy */
    private final int userId;

    /** Prihlasovacie meno používateľa */
    private final String meno;

    /** Rola používateľa určujúca prístupové práva (napr. "admin" alebo "user") */
    private final String rola;

    /**
     * Súkromný konštruktor na vytvorenie inštancie relácie.
     * Prístupný len cez metódu login.
     * * @param userId Identifikátor používateľa
     * @param meno   Meno používateľa
     * @param rola   Pridelená rola
     */
    private UserSession(int userId, String meno, String rola) {
        this.userId = userId;
        this.meno = meno;
        this.rola = rola;
    }

    /**
     * Vytvorí novú reláciu pri úspešnom prihlásení.
     * * @param userId ID používateľa z DB
     * @param meno   Meno používateľa
     * @param rola   Rola (napr. admin/user)
     */
    public static void login(int userId, String meno, String rola) {
        instance = new UserSession(userId, meno, rola);
    }

    /**
     * Ukončí aktuálnu reláciu (odhlási používateľa) nastavením inštancie na null.
     */
    public static void logout() {
        instance = null;
    }

    /**
     * Vráti aktuálne aktívnu inštanciu používateľskej relácie.
     * * @return Objekt UserSession alebo null, ak nie je nikto prihlásený.
     */
    public static UserSession getInstance() {
        return instance;
    }

    /**
     * Overí, či má aktuálne prihlásený používateľ oprávnenia administrátora.
     * * @return true, ak je rola "admin" (ignoruje sa veľkosť písmen), inak false.
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.rola);
    }

    /**
     * Získa meno aktuálne prihláseného používateľa.
     * * @return Meno používateľa
     */
    public String getMeno() {
        return this.meno;
    }

    /**
     * Získa ID aktuálne prihláseného používateľa.
     * * @return Číselný identifikátor používateľa
     */
    public int getUserId() {
        return this.userId;
    }
}