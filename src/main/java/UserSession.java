public class UserSession {
    private static UserSession instance;
    private int userId;
    private String meno;
    private String rola; // "admin" alebo "user"

    private UserSession(int userId, String meno, String rola) {
        this.userId = userId;
        this.meno = meno;
        this.rola = rola;
    }

    public static void login(int userId, String meno, String rola) {
        instance = new UserSession(userId, meno, rola);
    }

    public static void logout() {
        instance = null;
    }

    public static UserSession getInstance() {
        return instance;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(rola);
    }

    public String getMeno() {
        return meno;
    }

    public int getUserId() { return userId; }
}