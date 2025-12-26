
import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class LoginApp extends Application {
    private final DBConnect db = new DBConnect();
    private Scene homeScene;
    private Boolean manualDarkMode = null; // null = nebolo manuálne zmenené, true = tmavá, false = svetlá

    private Button createModernButton(String text, String baseColor) {
        Button btn = new Button(text);
        String baseStyle = "-fx-background-color: " + baseColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 11 28; -fx-background-radius: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0.3, 0, 4);";
        String var10000 = baseStyle.replace(baseColor, this.adjustColor(baseColor, 30));
        String hoverStyle = var10000 + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 16, 0.4, 0, 6); -fx-translate-y: -3;";
        btn.setStyle(baseStyle);
        btn.setCursor(Cursor.HAND);
        btn.setOnMouseEntered((e) -> {
            btn.setStyle(hoverStyle);
        });
        btn.setOnMouseExited((e) -> {
            btn.setStyle(baseStyle);
        });
        Platform.runLater(() -> {
            btn.setStyle(baseStyle);
        });
        return btn;
    }

    private String adjustColor(String hex, int amount) {
        if (hex.length() == 7 && hex.startsWith("#")) {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            r = Math.max(0, r - amount);
            g = Math.max(0, g - amount);
            b = Math.max(0, b - amount);
            return String.format("#%02x%02x%02x", r, g, b);
        } else {
            return hex;
        }
    }

    public void start(Stage stage) {
        // Hlavný kontajner pre obsah
        VBox root = new VBox(20.0);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30.0));
        root.getStyleClass().add("login-root"); // Použijeme štýl z CSS namiesto setStyle

        Label title = new Label("Vitajte v Receptári");
        title.getStyleClass().add("login-title");

        Button btnRegister = this.createModernButton("Registrovať sa", "#27ae60");
        btnRegister.setOnAction((e) -> this.showRegister(stage));

        Button btnLogin = this.createModernButton("Prihlásiť sa", "#3498db");
        btnLogin.setOnAction((e) -> this.showLogin(stage));

        root.getChildren().addAll(title, btnRegister, btnLogin);

        // --- MANUÁLNY PREPÍNAČ ---
        Button btnTheme = new Button("🌙"); // Ikona pre začiatok
        btnTheme.setCursor(Cursor.HAND);
        btnTheme.getStyleClass().add("theme-toggle-btn");
        btnTheme.setStyle("-fx-background-radius: 50; -fx-min-width: 40; -fx-min-height: 40;");

        btnTheme.setOnAction(e -> {
            // Zistíme aktuálny stav zo scény
            boolean isCurrentlyDark = stage.getScene().getRoot().getStyleClass().contains("dark-mode");

            // Prepnutie stavu
            manualDarkMode = !isCurrentlyDark;

            // Okamžitá aplikácia na aktuálnu scénu
            if (manualDarkMode) {
                stage.getScene().getRoot().getStyleClass().add("dark-mode");
                btnTheme.setText("☀️");
            } else {
                stage.getScene().getRoot().getStyleClass().remove("dark-mode");
                btnTheme.setText("🌙");
            }
        });

        // StackPane nám dovolí dať tlačidlo do rohu
        StackPane stackPane = new StackPane(root, btnTheme);
        StackPane.setAlignment(btnTheme, Pos.TOP_RIGHT);
        StackPane.setMargin(btnTheme, new Insets(15));

        this.homeScene = new Scene(stackPane, 450.0, 350.0);
        applyTheme(this.homeScene);
        // Ak applyTheme pridala dark-mode, zmeníme ikonu na slnko hneď pri štarte
        if (stackPane.getStyleClass().contains("dark-mode")) {
            btnTheme.setText("☀️");
        }

        stage.setTitle("Prihlásenie do Receptára");
        stage.setScene(this.homeScene);
        stage.show();
    }

    // Nová pomocná metóda na detekciu a aplikáciu témy
    private void applyTheme(Scene scene) {
        String css = getClass().getResource("/styles.css").toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }

        boolean shouldBeDark;

        if (manualDarkMode != null) {
            // Ak už používateľ manuálne prepol tému, použijeme jeho voľbu
            shouldBeDark = manualDarkMode;
        } else {
            // Ak ešte nič neprepol, zistíme to z Windowsu
            shouldBeDark = detectWindowsDarkMode();
        }

        if (shouldBeDark) {
            scene.getRoot().getStyleClass().add("dark-mode");
        } else {
            scene.getRoot().getStyleClass().remove("dark-mode");
        }
    }

    // Pomocná metóda pre čistotu kódu
    private boolean detectWindowsDarkMode() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            try {
                Process process = Runtime.getRuntime().exec("reg query \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize\" /v AppsUseLightTheme");
                java.util.Scanner sc = new java.util.Scanner(process.getInputStream());
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (line.contains("AppsUseLightTheme") && line.contains("0x0")) {
                        return true;
                    }
                }
            } catch (Exception e) { return false; }
        }
        return false;
    }

    static StackPane createPasswordToggle(String promptText) {
        PasswordField passField = new PasswordField();
        passField.setPromptText(promptText);
        passField.setMaxWidth(400.0);
        passField.setMaxWidth(Double.MAX_VALUE);
        passField.setStyle("-fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 14px;");
        TextField textField = new TextField();
        textField.setStyle("-fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 14px;");
        textField.setPromptText(promptText);
        textField.setMaxWidth(400.0);
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setVisible(false);
        CheckBox toggle = new CheckBox();
        StackPane.setAlignment(toggle, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggle, new Insets(0.0, 10.0, 0.0, 0.0));
        passField.textProperty().bindBidirectional(textField.textProperty());
        toggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                textField.setVisible(true);
                passField.setVisible(false);
                textField.requestFocus();
            } else {
                textField.setVisible(false);
                passField.setVisible(true);
                passField.requestFocus();
            }

        });
        StackPane stackPane = new StackPane(new Node[]{textField, passField, toggle});
        stackPane.setMaxWidth(300.0);
        return stackPane;
    }
    private void showLogin(Stage stage) {
        VBox box = new VBox(15.0);
        box.setPadding(new Insets(30.0));
        box.getStyleClass().add("vbox-container"); // Pridaj toto
        box.setAlignment(Pos.CENTER);
        Label lbl = new Label("Prihlásenie");
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Zmenil som txtUser na txtEmail, pretože v DBConnect hľadáš podľa emailu
        TextField txtUser = new TextField();
        txtUser.setPromptText("Používateľské meno"); // Pôvodný text
        txtUser.getStyleClass().add("modern-input"); // Pridaná trieda
        txtUser.setStyle("-fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 14px;");
        txtUser.setMaxWidth(300.0);

        StackPane passwordContainer = createPasswordToggle("Heslo");
        // Oprava: getStyle() vracal polomery a padding, ale nie maxWidth
        passwordContainer.setMaxWidth(300.0);
        passwordContainer.getStyleClass().add("modern-input");

        PasswordField txtPass = (PasswordField)passwordContainer.getChildren().stream()
                .filter(node -> node instanceof PasswordField)
                .findFirst().get();

        Label lblZabudnute = new Label("Zabudnuté heslo?");
        lblZabudnute.setStyle("-fx-text-fill: #3498db; -fx-cursor: hand; -fx-font-size: 13px;");
        lblZabudnute.setOnMouseEntered(e -> lblZabudnute.setUnderline(true));
        lblZabudnute.setOnMouseExited(e -> lblZabudnute.setUnderline(false));
        lblZabudnute.setOnMouseClicked(e -> this.otvorZabudnuteHeslo(stage));

        Button btnSubmit = this.createModernButton("Prihlásiť sa", "#3498db");
        btnSubmit.setDefaultButton(true);
        btnSubmit.setOnAction((e) -> {
            // 1. Zavoláme metódu, ktorá vráti [ID, Meno, Rola]
            String[] udaje = this.db.overPouzivatela(txtUser.getText(), txtPass.getText());

            if (udaje != null) {
                // 2. Naplníme UserSession (udaje[2] je tá rola "admin"/"user")
                UserSession.login(
                        Integer.parseInt(udaje[0]),
                        udaje[1],
                        udaje[2]
                );

                System.out.println("Login úspešný! Rola: " + udaje[2]);

                // 3. Zavrieme login a otvoríme hlavnú app
                stage.close();

                // Tu použi názov svojej hlavnej triedy, predpokladám ReceptarApp
                try {
                    ReceptyFXApp.startWithUser(new Stage(), udaje[1]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                this.alert("Chyba", "Nesprávny email alebo heslo.");
            }
        });

        Button btnBack = this.createModernButton("Späť na úvod", "#95a5a6");
        btnBack.setOnAction((e) -> stage.setScene(this.homeScene));

        box.getChildren().addAll(lbl, txtUser, passwordContainer, lblZabudnute, btnSubmit, btnBack);

        Scene scene = new Scene(box, 450.0, 400.0);
        applyTheme(scene);
        stage.setScene(scene);
    }
//    private void showLogin(Stage stage) {
//        VBox box = new VBox(15.0);
//        box.setPadding(new Insets(30.0));
//        box.setAlignment(Pos.CENTER);
//        box.setStyle("-fx-background-color: white; -fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");
//        Label lbl = new Label("Prihlásenie");
//        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
//        TextField txtUser = new TextField();
//        txtUser.setPromptText("Používateľské meno");
//        txtUser.setStyle("-fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 14px;");
//        txtUser.setMaxWidth(300.0);
//        StackPane passwordContainer = createPasswordToggle("Heslo");
//        passwordContainer.setStyle(txtUser.getStyle());
//        PasswordField txtPass = (PasswordField)passwordContainer.getChildren().stream().filter((node) -> {
//            return node instanceof PasswordField;
//        }).findFirst().get();
//        txtPass.setMaxWidth(300.0);
//        Label lblZabudnute = new Label("Zabudnuté heslo?");
//        lblZabudnute.setStyle("-fx-text-fill: #349 siedzib; -fx-cursor: hand; -fx-font-size: 13px;");
//        lblZabudnute.setOnMouseEntered((e) -> {
//            lblZabudnute.setUnderline(true);
//        });
//        lblZabudnute.setOnMouseExited((e) -> {
//            lblZabudnute.setUnderline(false);
//        });
//        lblZabudnute.setOnMouseClicked((e) -> {
//            this.otvorZabudnuteHeslo(stage);
//        });
//        Button btnSubmit = this.createModernButton("Prihlásiť sa", "#3498db");
//        btnSubmit.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 40; -fx-background-radius: 8;");
//        btnSubmit.setOnAction((e) -> {
//            if (this.db.login(txtUser.getText(), txtPass.getText())) {
//                stage.close();
//                ReceptyFXApp.startWithUser(new Stage(), txtUser.getText());
//            } else {
//                this.alert("Chyba", "Nesprávne meno alebo heslo.");
//            }
//
//        });
//        Button btnBack = this.createModernButton("Späť na úvod", "#636e72");
//        btnBack.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 8;");
//        btnBack.setOnAction((e) -> {
//            stage.setScene(this.homeScene);
//        });
//        box.getChildren().addAll(new Node[]{lbl, txtUser, passwordContainer, lblZabudnute, btnSubmit, btnBack});
//        Scene scene = new Scene(box, 450.0, 350.0);
//        stage.setScene(scene);
//    }

    private void otvorZabudnuteHeslo(Stage parentStage) {
        Stage stage = new Stage();
        stage.initOwner(parentStage);
        stage.setTitle("Obnova hesla");
        VBox box = new VBox(20.0);
        box.setPadding(new Insets(40.0));
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("vbox-container");
        box.setMaxWidth(400.0);
        Label lbl = new Label("Obnova hesla");
        lbl.getStyleClass().add("login-title");
        Label lblInfo = new Label("Zadaj svoj email a pošleme ti nové heslo.");
        lblInfo.setStyle("-fx-text-fill: #636e72; -fx-font-size: 14px;");
        lblInfo.setWrapText(true);
        lblInfo.setTextAlignment(TextAlignment.CENTER);
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Tvoj email");
        txtEmail.setStyle(txtEmail.getStyle());
        txtEmail.setMaxWidth(Double.MAX_VALUE);
        Button btnOdoslat = this.createModernButton(" Poslať nové heslo", "#3498db");
        btnOdoslat.setMaxWidth(Double.MAX_VALUE);
        Button btnSpat = this.createModernButton(" Späť", "#95a5a6");
        btnSpat.setMaxWidth(Double.MAX_VALUE);
        Label lblStatus = new Label();
        lblStatus.setStyle("-fx-font-size: 13px;");
        btnOdoslat.setOnAction((e) -> {
            String email = txtEmail.getText().trim();
            if (!email.isEmpty() && email.contains("@")) {
                btnOdoslat.setDisable(true);
                lblStatus.setText("Posielam nové heslo...");
                lblStatus.setStyle("-fx-text-fill: #3498db;");
                (new Thread(() -> {
                    try {
                        String noveHeslo = this.generateRandomPassword();
                        String hash = BCrypt.hashpw(noveHeslo, BCrypt.gensalt());
                        boolean uspech = this.db.resetujHeslo(email, hash);
                        if (uspech) {
                            EmailSender.sendEmail(email, "Receptár – Nové heslo", "Ahoj!\n\nTvoje nové heslo do Receptára je:\n\n<h2 style=\"color:#27ae60; font-size:28px; letter-spacing:3px; font-family: monospace;\">%s</h2>\n\nPo prihlásení si ho môžeš zmeniť tlačidlom Zmeniť heslo.\nAlebo zapísať niekam :).\n\nTešíme sa na teba!\nTím Receptára\n".formatted(noveHeslo));
                            Platform.runLater(() -> {
                                lblStatus.setText("Nové heslo bolo poslané na email!");
                                lblStatus.setStyle("-fx-text-fill: #27ae60;");
                                btnOdoslat.setText("Hotovo");
                            });
                        } else {
                            Platform.runLater(() -> {
                                lblStatus.setText("Email sa nenašiel v databáze.");
                                lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                                btnOdoslat.setDisable(false);
                            });
                        }
                    } catch (Exception var7) {
                        Platform.runLater(() -> {
                            lblStatus.setText("Chyba pri odosielaní emailu.");
                            lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                            btnOdoslat.setDisable(false);
                        });
                    }

                })).start();
            } else {
                lblStatus.setText("Zadaj platný email!");
                lblStatus.setStyle("-fx-text-fill: #e74c3c;");
            }
        });
        btnSpat.setOnAction((e) -> {
            stage.close();
        });
        box.getChildren().addAll(new Node[]{lbl, lblInfo, txtEmail, btnOdoslat, btnSpat, lblStatus});
        Scene scene = new Scene(box, 480.0, 550.0);
        applyTheme(scene);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(10);

        for(int i = 0; i < 10; ++i) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }

        return sb.toString();
    }

    private void showRegister(Stage stage) {
        VBox box = new VBox(15.0);
        box.setPadding(new Insets(30.0));
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("vbox-container");
        Label lbl = new Label("Registrácia");
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        TextField txtUser = new TextField();
        txtUser.setPromptText("Používateľské meno");
        txtUser.setStyle("-fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 14px;");
        txtUser.setMaxWidth(300.0);
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        txtEmail.setStyle("-fx-background-radius: 25; -fx-padding: 12; -fx-font-size: 14px;");
        txtEmail.setMaxWidth(300.0);
        StackPane passwordContainer = createPasswordToggle("Heslo");
        passwordContainer.setStyle(txtUser.getStyle());
        PasswordField txtPass = (PasswordField)passwordContainer.getChildren().stream().filter((node) -> {
            return node instanceof PasswordField;
        }).findFirst().get();
        txtPass.setMaxWidth(300.0);
        Button btnSubmit = this.createModernButton("Registrovať", "#27ae60");
        btnSubmit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 40; -fx-background-radius: 8;");
        btnSubmit.setOnAction((e) -> {
            String email = txtEmail.getText().trim();
            String meno = txtUser.getText().trim();
            String heslo = txtPass.getText();
            if (email.isEmpty()) {
                email = null;
            }

            if (this.db.register(meno, heslo, email)) {
                this.alert("Úspech", "Registrácia úspešná! Môžete sa prihlásiť.");
                if (email != null && !email.isEmpty() && email.contains("@")) {
                    String finalEmail = email;
                    (new Thread(() -> {
                        try {
                            EmailSender.sendEmail(finalEmail, "Vitaj v Receptári!", "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 30px; background: #f8f9fa; border-radius: 15px; text-align: center;\">\n    <h1 style=\"color: #27ae60;\">Vitaj, %s!</h1>\n    <p>Ďakujeme, že si sa zaregistroval/a v <strong>Receptári</strong>.</p>\n    ...\n</div>\n".formatted(meno));
                        } catch (Exception var3) {
                        }

                    })).start();
                }

                this.showLogin(stage);
            } else {
                this.alert("Chyba", "Registrácia zlyhala – meno alebo email už existuje.");
            }

        });
        Button btnBack = this.createModernButton("Späť na úvod", "#636e72");
        btnBack.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 8;");
        btnBack.setOnAction((e) -> {
            stage.setScene(this.homeScene);
        });
        box.getChildren().addAll(new Node[]{lbl, txtUser, txtEmail, passwordContainer, btnSubmit, btnBack});
        Scene scene = new Scene(box, 450.0, 400.0);
        applyTheme(scene);
        stage.setScene(scene);
    }

    private void alert(String title, String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText((String)null);
        alert.setContentText(msg);
        alert.getButtonTypes().setAll(new ButtonType[]{ButtonType.OK});
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
