/*     */
/*     */ 
/*     */ import java.sql.Connection;
/*     */ import java.sql.DriverManager;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import org.mindrot.jbcrypt.BCrypt;
/*     */ 
/*     */ public class DBConnect {
/*     */   private static final String URL = "jdbc:mysql://db.db048.endora.cz:3306/cambalik_eu?useSSL=false&serverTimezone=UTC";
/*     */   
/*     */   private static final String USER = "cambalik_eu";
/*     */   
/*     */   private static final String PASS = "ILoveHugs321";
/*     */   
/*     */   private Connection conn;
/*     */   
/*     */   public DBConnect() {
/*     */     try {
/*  19 */       this.conn = DriverManager.getConnection("jdbc:mysql://db.db048.endora.cz:3306/cambalik_eu?useSSL=false&serverTimezone=UTC", "cambalik_eu", "ILoveHugs321");
/*  20 */     } catch (SQLException e) {
/*  21 */       e.printStackTrace();
/*  22 */       System.exit(1);
/*     */     } 
/*     */   }
/*     */   
/*     */   public Connection getConnection() throws SQLException {
/*  30 */     return DriverManager.getConnection("jdbc:mysql://db.db048.endora.cz:3306/cambalik_eu?useSSL=false&serverTimezone=UTC", "cambalik_eu", "ILoveHugs321");
/*     */   }
/*     */   
/*     */   public boolean login(String username, String password) {
/*  34 */     String sql = "SELECT password FROM users WHERE username = ?";
/*     */     try {
/*  35 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/*  36 */         ps.setString(1, username);
/*  37 */         ResultSet rs = ps.executeQuery();
/*  38 */         if (rs.next()) {
/*  39 */           boolean bool = BCrypt.checkpw(password, rs.getString("password"));
/*  41 */           if (ps != null)
/*  41 */             ps.close(); 
/*     */           return bool;
/*     */         } 
/*  41 */         if (ps != null)
/*  41 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/*  41 */     } catch (SQLException e) {
/*  42 */       e.printStackTrace();
/*     */     } 
/*  44 */     return false;
/*     */   }
/*     */   
/*     */   public boolean register(String username, String password, String email) {
/*  48 */     String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
/*  49 */     String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
/*     */     try {
/*  50 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/*  51 */         ps.setString(1, username);
/*  52 */         ps.setString(2, hashed);
/*  53 */         ps.setString(3, email);
/*  54 */         boolean bool = (ps.executeUpdate() > 0) ? true : false;
/*  55 */         if (ps != null)
/*  55 */           ps.close(); 
/*     */         return bool;
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/*  55 */     } catch (SQLException e) {
/*  56 */       e.printStackTrace();
/*  58 */       return false;
/*     */     } 
/*     */   }
/*     */   
/*     */   public int getUserIdByUsername(String username) {
/*  62 */     String sql = "SELECT id FROM users WHERE username = ?";
/*     */     try {
/*  63 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/*  64 */         ps.setString(1, username);
/*  65 */         ResultSet rs = ps.executeQuery();
/*  66 */         if (rs.next()) {
/*  66 */           int i = rs.getInt("id");
/*  67 */           if (ps != null)
/*  67 */             ps.close(); 
/*     */           return i;
/*     */         } 
/*  67 */         if (ps != null)
/*  67 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/*  67 */     } catch (SQLException e) {
/*  68 */       e.printStackTrace();
/*     */     } 
/*  70 */     return -1;
/*     */   }
/*     */   
/*     */   public String getEmailPodlaId(int userId) {
/*  75 */     String sql = "SELECT email FROM users WHERE id = ?";
/*     */     try {
/*  76 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/*  77 */         ps.setInt(1, userId);
/*  78 */         ResultSet rs = ps.executeQuery();
/*  79 */         if (rs.next()) {
/*  80 */           String str = rs.getString("email");
/*  82 */           if (ps != null)
/*  82 */             ps.close(); 
/*     */           return str;
/*     */         } 
/*  82 */         if (ps != null)
/*  82 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/*  82 */     } catch (SQLException e) {
/*  83 */       e.printStackTrace();
/*     */     } 
/*  85 */     return null;
/*     */   }
/*     */   
/*     */   public boolean changePassword(String username, String oldPassword, String newPassword) {
/*  90 */     String storedHash = null;
/*  93 */     String selectSql = "SELECT password FROM users WHERE username = ?";
/*     */     try {
/*  94 */       PreparedStatement ps = this.conn.prepareStatement(selectSql);
/*     */       try {
/*  95 */         ps.setString(1, username);
/*  96 */         ResultSet rs = ps.executeQuery();
/*  97 */         if (rs.next()) {
/*  98 */           storedHash = rs.getString("password");
/*     */         } else {
/* 100 */           boolean bool = false;
/* 102 */           if (ps != null)
/* 102 */             ps.close(); 
/*     */           return bool;
/*     */         } 
/* 102 */         if (ps != null)
/* 102 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 102 */     } catch (SQLException e) {
/* 103 */       e.printStackTrace();
/* 104 */       return false;
/*     */     } 
/* 108 */     if (!BCrypt.checkpw(oldPassword, storedHash))
/* 109 */       return false; 
/* 113 */     String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
/* 114 */     String updateSql = "UPDATE users SET password = ? WHERE username = ?";
/*     */     try {
/* 115 */       PreparedStatement ps = this.conn.prepareStatement(updateSql);
/*     */       try {
/* 116 */         ps.setString(1, newHash);
/* 117 */         ps.setString(2, username);
/* 118 */         boolean bool = (ps.executeUpdate() > 0) ? true : false;
/* 119 */         if (ps != null)
/* 119 */           ps.close(); 
/*     */         return bool;
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 119 */     } catch (SQLException e) {
/* 120 */       e.printStackTrace();
/* 121 */       return false;
/*     */     } 
/*     */   }
/*     */   
/*     */   public boolean resetujHeslo(String email, String noveHesloHash) {
/* 127 */     String sql = "UPDATE users SET password = ? WHERE email = ? AND email IS NOT NULL";
/*     */     try {
/* 128 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/* 129 */         ps.setString(1, noveHesloHash);
/* 130 */         ps.setString(2, email);
/* 131 */         boolean bool = (ps.executeUpdate() > 0) ? true : false;
/* 132 */         if (ps != null)
/* 132 */           ps.close(); 
/*     */         return bool;
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 132 */     } catch (SQLException e) {
/* 133 */       e.printStackTrace();
/* 134 */       return false;
/*     */     } 
/*     */   }
/*     */   
/*     */   public List<Kategoria> nacitajVsetkyKategorie() {
/* 140 */     List<Kategoria> list = new ArrayList<>();
/* 141 */     String sql = "SELECT kategoria_id, nazov FROM kategoria ORDER BY nazov";
/*     */     try {
/* 142 */       Statement st = this.conn.createStatement();
/*     */       try {
/* 142 */         ResultSet rs = st.executeQuery(sql);
/*     */         try {
/* 143 */           while (rs.next())
/* 144 */             list.add(new Kategoria(rs.getInt("kategoria_id"), rs.getString("nazov"))); 
/* 146 */           if (rs != null)
/* 146 */             rs.close(); 
/*     */         } catch (Throwable throwable) {
/*     */           if (rs != null)
/*     */             try {
/*     */               rs.close();
/*     */             } catch (Throwable throwable1) {
/*     */               throwable.addSuppressed(throwable1);
/*     */             }  
/*     */           throw throwable;
/*     */         } 
/* 146 */         if (st != null)
/* 146 */           st.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (st != null)
/*     */           try {
/*     */             st.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 146 */     } catch (SQLException e) {
/* 147 */       e.printStackTrace();
/*     */     } 
/* 149 */     return list;
/*     */   }
/*     */   
/*     */   public List<String> nacitajVsetkyNazvyIngrediencii() {
/* 154 */     List<String> list = new ArrayList<>();
/* 155 */     String sql = "SELECT nazov FROM ingrediencie ORDER BY nazov";
/*     */     try {
/* 156 */       Statement st = this.conn.createStatement();
/*     */       try {
/* 156 */         ResultSet rs = st.executeQuery(sql);
/*     */         try {
/* 157 */           while (rs.next())
/* 158 */             list.add(rs.getString("nazov")); 
/* 160 */           if (rs != null)
/* 160 */             rs.close(); 
/*     */         } catch (Throwable throwable) {
/*     */           if (rs != null)
/*     */             try {
/*     */               rs.close();
/*     */             } catch (Throwable throwable1) {
/*     */               throwable.addSuppressed(throwable1);
/*     */             }  
/*     */           throw throwable;
/*     */         } 
/* 160 */         if (st != null)
/* 160 */           st.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (st != null)
/*     */           try {
/*     */             st.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 160 */     } catch (SQLException e) {
/* 161 */       e.printStackTrace();
/*     */     } 
/* 163 */     return list;
/*     */   }
/*     */   
/*     */   private int getOrCreateIngredienciaId(String nazov) {
/* 167 */     String sql = "SELECT ingrediencia_id FROM ingrediencie WHERE nazov = ?";
/*     */     try {
/* 168 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/* 169 */         ps.setString(1, nazov);
/* 170 */         ResultSet rs = ps.executeQuery();
/* 171 */         if (rs.next()) {
/* 171 */           int i = rs.getInt("ingrediencia_id");
/* 172 */           if (ps != null)
/* 172 */             ps.close(); 
/*     */           return i;
/*     */         } 
/* 172 */         if (ps != null)
/* 172 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 172 */     } catch (SQLException e) {
/* 173 */       e.printStackTrace();
/*     */     } 
/* 177 */     String insert = "INSERT INTO ingrediencie (nazov) VALUES (?)";
/*     */     try {
/* 178 */       PreparedStatement ps = this.conn.prepareStatement(insert, 1);
/*     */       try {
/* 179 */         ps.setString(1, nazov);
/* 180 */         ps.executeUpdate();
/* 181 */         ResultSet rs = ps.getGeneratedKeys();
/* 182 */         if (rs.next()) {
/* 182 */           int i = rs.getInt(1);
/* 183 */           if (ps != null)
/* 183 */             ps.close(); 
/*     */           return i;
/*     */         } 
/* 183 */         if (ps != null)
/* 183 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 183 */     } catch (SQLException e) {
/* 184 */       e.printStackTrace();
/*     */     } 
/* 186 */     return -1;
/*     */   }
/*     */   
/*     */   public void upravReceptCestaObrazku(int receptId, String obrazokCesta) {
/* 192 */     String sql = "UPDATE recepty SET obrazok_cesta = ? WHERE recept_id = ?";
/*     */     try {
/* 194 */       Connection conn = getConnection();
/*     */       try {
/* 195 */         PreparedStatement stmt = conn.prepareStatement(sql);
/*     */         try {
/* 197 */           stmt.setString(1, obrazokCesta);
/* 198 */           stmt.setInt(2, receptId);
/* 199 */           stmt.executeUpdate();
/* 201 */           if (stmt != null)
/* 201 */             stmt.close(); 
/*     */         } catch (Throwable throwable) {
/*     */           if (stmt != null)
/*     */             try {
/*     */               stmt.close();
/*     */             } catch (Throwable throwable1) {
/*     */               throwable.addSuppressed(throwable1);
/*     */             }  
/*     */           throw throwable;
/*     */         } 
/* 201 */         if (conn != null)
/* 201 */           conn.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (conn != null)
/*     */           try {
/*     */             conn.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 201 */     } catch (SQLException e) {
/* 202 */       System.err.println("Chyba pri aktualizácii cesty obrázku: " + e.getMessage());
/*     */     } 
/*     */   }
/*     */   
/*     */   public String nacitajObrazokCestu(int receptId) {
/* 207 */     String sql = "SELECT obrazok_cesta FROM recepty WHERE recept_id = ?";
/*     */     try {
/* 208 */       Connection conn = getConnection();
/*     */       try {
/* 209 */         PreparedStatement stmt = conn.prepareStatement(sql);
/*     */         try {
/* 210 */           stmt.setInt(1, receptId);
/* 211 */           ResultSet rs = stmt.executeQuery();
/*     */           try {
/* 212 */             if (rs.next()) {
/* 213 */               String str = rs.getString("obrazok_cesta");
/* 215 */               if (rs != null)
/* 215 */                 rs.close(); 
/* 216 */               if (stmt != null)
/* 216 */                 stmt.close(); 
/* 216 */               if (conn != null)
/* 216 */                 conn.close(); 
/*     */               return str;
/*     */             } 
/*     */             if (rs != null)
/*     */               rs.close(); 
/*     */           } catch (Throwable throwable) {
/*     */             if (rs != null)
/*     */               try {
/*     */                 rs.close();
/*     */               } catch (Throwable throwable1) {
/*     */                 throwable.addSuppressed(throwable1);
/*     */               }  
/*     */             throw throwable;
/*     */           } 
/* 216 */           if (stmt != null)
/* 216 */             stmt.close(); 
/*     */         } catch (Throwable throwable) {
/*     */           if (stmt != null)
/*     */             try {
/*     */               stmt.close();
/*     */             } catch (Throwable throwable1) {
/*     */               throwable.addSuppressed(throwable1);
/*     */             }  
/*     */           throw throwable;
/*     */         } 
/* 216 */         if (conn != null)
/* 216 */           conn.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (conn != null)
/*     */           try {
/*     */             conn.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 216 */     } catch (SQLException e) {
/* 217 */       System.err.println("Chyba pri načítaní starej cesty obrázku: " + e.getMessage());
/*     */     } 
/* 219 */     return null;
/*     */   }
/*     */   
/*     */   public List<Recept> nacitajVsetkyRecepty() {
/* 223 */     List<Recept> list = new ArrayList<>();
/* 224 */     String sql = "SELECT r.recept_id, r.nazov, r.postup, r.cas_pripravy, r.pocet_porcii,\n       r.obrazok_cesta, COALESCE(r.kategoria_id, 0) AS kategoria_id,\n       COALESCE(k.nazov, 'Bez kategórie') AS kat_nazov,\n       r.user_id, COALESCE(u.username, 'Neznámy') AS autor\nFROM recepty r\nLEFT JOIN kategoria k ON r.kategoria_id = k.kategoria_id\nLEFT JOIN users u ON r.user_id = u.id\nORDER BY r.nazov\n";
/*     */     try {
/* 234 */       Statement st = this.conn.createStatement();
/*     */       try {
/* 234 */         ResultSet rs = st.executeQuery(sql);
/*     */         try {
/* 235 */           while (rs.next())
/* 236 */             list.add(new Recept(rs
/* 237 */                   .getInt("recept_id"), rs
/* 238 */                   .getString("nazov"), rs
/* 239 */                   .getString("postup"), rs
/* 240 */                   .getInt("cas_pripravy"), rs
/* 241 */                   .getInt("pocet_porcii"), rs
/* 242 */                   .getString("obrazok_cesta"), rs
/* 243 */                   .getInt("kategoria_id"), rs
/* 244 */                   .getString("kat_nazov"), rs
/* 245 */                   .getInt("user_id"), rs
/* 246 */                   .getString("autor"))); 
/* 249 */           if (rs != null)
/* 249 */             rs.close(); 
/*     */         } catch (Throwable throwable) {
/*     */           if (rs != null)
/*     */             try {
/*     */               rs.close();
/*     */             } catch (Throwable throwable1) {
/*     */               throwable.addSuppressed(throwable1);
/*     */             }  
/*     */           throw throwable;
/*     */         } 
/* 249 */         if (st != null)
/* 249 */           st.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (st != null)
/*     */           try {
/*     */             st.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 249 */     } catch (SQLException e) {
/* 250 */       e.printStackTrace();
/*     */     } 
/* 252 */     return list;
/*     */   }
/*     */   
/*     */   public int pridajRecept(String nazov, String postup, int cas, int porcie, int kategoriaId, int userId, String obrazokCesta) {
/* 256 */     String sql = "INSERT INTO recepty (nazov, postup, cas_pripravy, pocet_porcii, kategoria_id, user_id, obrazok_cesta) VALUES (?, ?, ?, ?, ?, ?, ?)";
/*     */     try {
/* 257 */       PreparedStatement ps = this.conn.prepareStatement(sql, 1);
/*     */       try {
/* 258 */         ps.setString(1, nazov);
/* 259 */         ps.setString(2, postup);
/* 260 */         ps.setInt(3, cas);
/* 261 */         ps.setInt(4, porcie);
/* 262 */         ps.setInt(5, ((kategoriaId == 0) ? null : Integer.valueOf(kategoriaId)).intValue());
/* 263 */         ps.setInt(6, userId);
/* 264 */         ps.setString(7, obrazokCesta);
/* 265 */         ps.executeUpdate();
/* 266 */         ResultSet rs = ps.getGeneratedKeys();
/* 267 */         if (rs.next()) {
/* 267 */           int i = rs.getInt(1);
/* 268 */           if (ps != null)
/* 268 */             ps.close(); 
/*     */           return i;
/*     */         } 
/* 268 */         if (ps != null)
/* 268 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 268 */     } catch (SQLException e) {
/* 269 */       e.printStackTrace();
/*     */     } 
/* 271 */     return -1;
/*     */   }
/*     */   
/*     */   public void upravRecept(int id, String nazov, String postup, int cas, int porcie, String obrazokCesta, int kategoriaId) {
/* 275 */     String sql = "UPDATE recepty SET nazov = ?, postup = ?, cas_pripravy = ?, pocet_porcii = ?, obrazok_cesta = ?, kategoria_id = ? WHERE recept_id = ?";
/*     */     try {
/* 276 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/* 277 */         ps.setString(1, nazov);
/* 278 */         ps.setString(2, postup);
/* 279 */         ps.setInt(3, cas);
/* 280 */         ps.setInt(4, porcie);
/* 281 */         ps.setString(5, obrazokCesta);
/* 282 */         ps.setInt(6, ((kategoriaId == 0) ? null : Integer.valueOf(kategoriaId)).intValue());
/* 283 */         ps.setInt(7, id);
/* 284 */         ps.executeUpdate();
/* 285 */         if (ps != null)
/* 285 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 285 */     } catch (SQLException e) {
/* 286 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   public void zmazRecept(int id) {
/* 291 */     String sql = "DELETE FROM recepty WHERE recept_id = ?";
/*     */     try {
/* 292 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/* 293 */         ps.setInt(1, id);
/* 294 */         ps.executeUpdate();
/* 295 */         if (ps != null)
/* 295 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 295 */     } catch (SQLException e) {
/* 296 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   public List<IngredienciaMnozstvo> nacitajIngredienciePreRecept(int receptId) {
/* 302 */     List<IngredienciaMnozstvo> list = new ArrayList<>();
/* 303 */     String sql = "SELECT i.nazov, ri.mnozstvo, ri.jednotka\nFROM recept_ingrediencie ri\nJOIN ingrediencie i ON ri.ingrediencia_id = i.ingrediencia_id\nWHERE ri.recept_id = ?\nORDER BY i.nazov\n";
/*     */     try {
/* 310 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/* 311 */         ps.setInt(1, receptId);
/* 312 */         ResultSet rs = ps.executeQuery();
/* 313 */         while (rs.next())
/* 314 */           list.add(new IngredienciaMnozstvo(rs
/* 315 */                 .getString("nazov"), rs
/* 316 */                 .getDouble("mnozstvo"), rs
/* 317 */                 .getString("jednotka"))); 
/* 320 */         if (ps != null)
/* 320 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 320 */     } catch (SQLException e) {
/* 321 */       e.printStackTrace();
/*     */     } 
/* 323 */     return list;
/*     */   }
/*     */   
/*     */   public void vymazIngrediencieReceptu(int receptId) {
/* 327 */     String sql = "DELETE FROM recept_ingrediencie WHERE recept_id = ?";
/*     */     try {
/* 328 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/* 329 */         ps.setInt(1, receptId);
/* 330 */         ps.executeUpdate();
/* 331 */         if (ps != null)
/* 331 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 331 */     } catch (SQLException e) {
/* 332 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   public void pridajIngredienciuDoReceptu(int receptId, String nazovIngrediencie, double mnozstvo, String jednotka) {
/* 337 */     int ingId = getOrCreateIngredienciaId(nazovIngrediencie);
/* 338 */     String sql = "INSERT INTO recept_ingrediencie (recept_id, ingrediencia_id, mnozstvo, jednotka) VALUES (?, ?, ?, ?)";
/*     */     try {
/* 339 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/* 340 */         ps.setInt(1, receptId);
/* 341 */         ps.setInt(2, ingId);
/* 342 */         ps.setDouble(3, mnozstvo);
/* 343 */         ps.setString(4, jednotka);
/* 344 */         ps.executeUpdate();
/* 345 */         if (ps != null)
/* 345 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 345 */     } catch (SQLException e) {
/* 346 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */   
/*     */   public void odstranJednuIngredienciu(int receptId, String nazov, double mnozstvo, String jednotka) {
/* 351 */     int ingId = getOrCreateIngredienciaId(nazov);
/* 352 */     String sql = "DELETE FROM recept_ingrediencie WHERE recept_id = ? AND ingrediencia_id = ? AND mnozstvo = ? AND jednotka = ?";
/*     */     try {
/* 353 */       PreparedStatement ps = this.conn.prepareStatement(sql);
/*     */       try {
/* 354 */         ps.setInt(1, receptId);
/* 355 */         ps.setInt(2, ingId);
/* 356 */         ps.setDouble(3, mnozstvo);
/* 357 */         ps.setString(4, jednotka);
/* 358 */         ps.executeUpdate();
/* 359 */         if (ps != null)
/* 359 */           ps.close(); 
/*     */       } catch (Throwable throwable) {
/*     */         if (ps != null)
/*     */           try {
/*     */             ps.close();
/*     */           } catch (Throwable throwable1) {
/*     */             throwable.addSuppressed(throwable1);
/*     */           }  
/*     */         throw throwable;
/*     */       } 
/* 359 */     } catch (SQLException e) {
/* 360 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */ }


/* Location:              E:\Receptar\receptar.jar!\receptar\DBConnect.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */