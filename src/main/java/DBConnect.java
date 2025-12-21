import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Trieda DBConnect zabezpečuje pripojenie k MySQL databáze a poskytuje 
 * metódy pre manipuláciu s údajmi o používateľoch, receptoch a ingredienciách.
 */
public class DBConnect {

    /** URL adresa pre pripojenie k databáze */
    private static final String URL = "jdbc:mysql://db.db048.endora.cz:3306/cambalik_eu?useSSL=false&serverTimezone=UTC";

    /** Prihlasovacie meno do databázy */
    private static final String USER = "cambalik_eu";

    /** Heslo pre prístup k databáze */
    private static final String PASS = "ILoveHugs321";

    /** Aktuálna inštancia spojenia */
    private Connection conn;

    /**
     * Konštruktor triedy, ktorý pri inicializácii vytvorí spojenie s databázou.
     * V prípade chyby pripojenia ukončí aplikáciu.
     */
    public DBConnect() {
        try {
            this.conn = DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Vytvorí a vráti nové spojenie s databázou.
     * @return Connection objekt
     * @throws SQLException ak nastane chyba pri pripájaní
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * Overí prihlasovacie údaje používateľa.
     * @param username Používateľské meno
     * @param password Heslo v čistom texte
     * @return true, ak sú údaje správne, inak false
     */
    public boolean login(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return BCrypt.checkpw(password, rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Overí používateľa a vráti jeho základné údaje.
     * @param email Používateľské meno (alebo email)
     * @param heslo Heslo v čistom texte
     * @return Pole Stringov [ID, Username, Rola] alebo null, ak overenie zlyhalo
     */
    public String[] overPouzivatela(String email, String heslo) {
        String sql = "SELECT id, username, password, rola FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password");
                    if (BCrypt.checkpw(heslo, hash)) {
                        return new String[]{
                                String.valueOf(rs.getInt("id")),
                                rs.getString("username"),
                                rs.getString("rola")
                        };
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Zaregistruje nového používateľa do systému.
     * @param username Používateľské meno
     * @param password Heslo v čistom texte (bude zahashované)
     * @param email Emailová adresa
     * @return true, ak bola registrácia úspešná
     */
    public boolean register(String username, String password, String email) {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashed);
            ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Získa ID používateľa podľa jeho mena.
     * @param username Meno používateľa
     * @return ID používateľa, alebo -1 ak sa nenašiel
     */
    public int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Získa emailovú adresu používateľa podľa jeho ID.
     * @param userId ID používateľa
     * @return Emailová adresa alebo null
     */
    public String getEmailPodlaId(int userId) {
        String sql = "SELECT email FROM users WHERE id = ?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Zmení heslo používateľa po overení starého hesla.
     * @param username Meno používateľa
     * @param oldPassword Staré heslo
     * @param newPassword Nové heslo
     * @return true, ak bola zmena úspešná
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        String storedHash = null;
        String selectSql = "SELECT password FROM users WHERE username = ?";

        try (PreparedStatement ps = this.conn.prepareStatement(selectSql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    storedHash = rs.getString("password");
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        if (!BCrypt.checkpw(oldPassword, storedHash)) {
            return false;
        }

        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String updateSql = "UPDATE users SET password = ? WHERE username = ?";
        try (PreparedStatement ps = this.conn.prepareStatement(updateSql)) {
            ps.setString(1, newHash);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Resetuje heslo používateľa na základe emailu.
     * @param email Email používateľa
     * @param noveHesloHash Už zahashované nové heslo
     * @return true, ak bola operácia úspešná
     */
    public boolean resetujHeslo(String email, String noveHesloHash) {
        String sql = "UPDATE users SET password = ? WHERE email = ? AND email IS NOT NULL";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setString(1, noveHesloHash);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Načíta zoznam všetkých kategórií z databázy.
     * @return Zoznam objektov Kategoria
     */
    public List<Kategoria> nacitajVsetkyKategorie() {
        List<Kategoria> list = new ArrayList<>();
        String sql = "SELECT kategoria_id, nazov FROM kategoria ORDER BY nazov";
        try (Statement st = this.conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Kategoria(rs.getInt("kategoria_id"), rs.getString("nazov")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Načíta názvy všetkých dostupných ingrediencií.
     * @return Zoznam názvov ingrediencií
     */
    public List<String> nacitajVsetkyNazvyIngrediencii() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT nazov FROM ingrediencie ORDER BY nazov";
        try (Statement st = this.conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("nazov"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Pomocná metóda, ktorá vráti ID ingrediencie podľa názvu.
     * Ak ingrediencia neexistuje, vytvorí ju.
     * @param nazov Názov ingrediencie
     * @return ID ingrediencie alebo -1 pri chybe
     */
    private int getOrCreateIngredienciaId(String nazov) {
        String sql = "SELECT ingrediencia_id FROM ingrediencie WHERE nazov = ?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setString(1, nazov);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ingrediencia_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insert = "INSERT INTO ingrediencie (nazov) VALUES (?)";
        try (PreparedStatement ps = this.conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nazov);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Aktualizuje cestu k obrázku receptu.
     * @param receptId ID receptu
     * @param obrazokCesta Nová cesta k súboru obrázku
     */
    public void upravReceptCestaObrazku(int receptId, String obrazokCesta) {
        String sql = "UPDATE recepty SET obrazok_cesta = ? WHERE recept_id = ?";
        try (Connection localConn = getConnection();
             PreparedStatement stmt = localConn.prepareStatement(sql)) {
            stmt.setString(1, obrazokCesta);
            stmt.setInt(2, receptId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Chyba pri aktualizácii cesty obrázku: " + e.getMessage());
        }
    }

    /**
     * Získa cestu k obrázku pre konkrétny recept.
     * @param receptId ID receptu
     * @return Cesta k obrázku alebo null
     */
    public String nacitajObrazokCestu(int receptId) {
        String sql = "SELECT obrazok_cesta FROM recepty WHERE recept_id = ?";
        try (Connection localConn = getConnection();
             PreparedStatement stmt = localConn.prepareStatement(sql)) {
            stmt.setInt(1, receptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("obrazok_cesta");
                }
            }
        } catch (SQLException e) {
            System.err.println("Chyba pri načítaní starej cesty obrázku: " + e.getMessage());
        }
        return null;
    }

    /**
     * Načíta všetky recepty z databázy vrátane kategórie a autora.
     * @return Zoznam objektov Recept
     */
    public List<Recept> nacitajVsetkyRecepty() {
        List<Recept> list = new ArrayList<>();
        String sql = "SELECT r.recept_id, r.nazov, r.postup, r.cas_pripravy, r.pocet_porcii, " +
                "r.obrazok_cesta, COALESCE(r.kategoria_id, 0) AS kategoria_id, " +
                "COALESCE(k.nazov, 'Bez kategórie') AS kat_nazov, " +
                "r.user_id, COALESCE(u.username, 'Neznámy') AS autor " +
                "FROM recepty r " +
                "LEFT JOIN kategoria k ON r.kategoria_id = k.kategoria_id " +
                "LEFT JOIN users u ON r.user_id = u.id " +
                "ORDER BY r.nazov";
        try (Statement st = this.conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Recept(
                        rs.getInt("recept_id"),
                        rs.getString("nazov"),
                        rs.getString("postup"),
                        rs.getInt("cas_pripravy"),
                        rs.getInt("pocet_porcii"),
                        rs.getString("obrazok_cesta"),
                        rs.getInt("kategoria_id"),
                        rs.getString("kat_nazov"),
                        rs.getInt("user_id"),
                        rs.getString("author")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Pridá nový recept do databázy.
     * @return ID novovytvoreného receptu alebo -1 pri chybe
     */
    public int pridajRecept(String nazov, String postup, int cas, int porcie, int kategoriaId, int userId, String obrazokCesta) {
        String sql = "INSERT INTO recepty (nazov, postup, cas_pripravy, pocet_porcii, kategoria_id, user_id, obrazok_cesta) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nazov);
            ps.setString(2, postup);
            ps.setInt(3, cas);
            ps.setInt(4, porcie);
            if (kategoriaId == 0) ps.setNull(5, java.sql.Types.INTEGER); else ps.setInt(5, kategoriaId);
            ps.setInt(6, userId);
            ps.setString(7, obrazokCesta);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Upraví existujúci recept.
     */
    public void upravRecept(int id, String nazov, String postup, int cas, int porcie, String obrazokCesta, int kategoriaId) {
        String sql = "UPDATE recepty SET nazov = ?, postup = ?, cas_pripravy = ?, pocet_porcii = ?, obrazok_cesta = ?, kategoria_id = ? WHERE recept_id = ?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setString(1, nazov);
            ps.setString(2, postup);
            ps.setInt(3, cas);
            ps.setInt(4, porcie);
            ps.setString(5, obrazokCesta);
            if (kategoriaId == 0) ps.setNull(6, java.sql.Types.INTEGER); else ps.setInt(6, kategoriaId);
            ps.setInt(7, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Vymaže recept z databázy podľa ID.
     * @param id ID receptu na zmazanie
     */
    public void zmazRecept(int id) {
        String sql = "DELETE FROM recepty WHERE recept_id = ?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Načíta ingrediencie prislúchajúce konkrétnemu receptu.
     * @param receptId ID receptu
     * @return Zoznam ingrediencií s ich množstvami
     */
    public List<IngredienciaMnozstvo> nacitajIngredienciePreRecept(int receptId) {
        List<IngredienciaMnozstvo> list = new ArrayList<>();
        String sql = "SELECT i.nazov, ri.mnozstvo, ri.jednotka " +
                "FROM recept_ingrediencie ri " +
                "JOIN ingrediencie i ON ri.ingrediencia_id = i.ingrediencia_id " +
                "WHERE ri.recept_id = ? " +
                "ORDER BY i.nazov";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setInt(1, receptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new IngredienciaMnozstvo(
                            rs.getString("nazov"),
                            rs.getDouble("mnozstvo"),
                            rs.getString("jednotka")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Odstráni všetky väzby ingrediencií pre daný recept.
     * @param receptId ID receptu
     */
    public void vymazIngrediencieReceptu(int receptId) {
        String sql = "DELETE FROM recept_ingrediencie WHERE recept_id = ?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setInt(1, receptId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pridá ingredienciu k receptu (vytvorí záznam v prepájacej tabuľke).
     */
    public void pridajIngredienciuDoReceptu(int receptId, String nazovIngrediencie, double mnozstvo, String jednotka) {
        int ingId = getOrCreateIngredienciaId(nazovIngrediencie);
        String sql = "INSERT INTO recept_ingrediencie (recept_id, ingrediencia_id, mnozstvo, jednotka) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setInt(1, receptId);
            ps.setInt(2, ingId);
            ps.setDouble(3, mnozstvo);
            ps.setString(4, jednotka);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Odstráni jednu konkrétnu ingredienciu z receptu na základe presnej zhody údajov.
     */
    public void odstranJednuIngredienciu(int receptId, String nazov, double mnozstvo, String jednotka) {
        int ingId = getOrCreateIngredienciaId(nazov);
        String sql = "DELETE FROM recept_ingrediencie WHERE recept_id = ? AND ingrediencia_id = ? AND mnozstvo = ? AND jednotka = ?";
        try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
            ps.setInt(1, receptId);
            ps.setInt(2, ingId);
            ps.setDouble(3, mnozstvo);
            ps.setString(4, jednotka);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}