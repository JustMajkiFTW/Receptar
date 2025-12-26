import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class ReceptyFXApp extends Application {
    private final DBConnect db = new DBConnect();
    private final ObservableList<Recept> recepty = FXCollections.observableArrayList();
    private final ObservableList<IngredienciaMnozstvo> ingrediencieReceptu = FXCollections.observableArrayList();
    private TableView<Recept> tableRecepty;
    private TableView<IngredienciaMnozstvo> tableIngrediencie;
    private TextField tfSearch;
    private ComboBox<Kategoria> cbFilterKategoria;
    private ImageView imgView;
    private String obrazokCesta = "";
    public static String loggedInUser = "";
    private FilteredList<Recept> filteredRecepty;
    private SortedList<Recept> sortedRecepty;
    private static final String FTP_SERVER = "ftp.cambalik.eu";
    private static final int FTP_PORT = 21;
    private static final String FTP_USER = "recepty.cambalik.eu";
    private static final String FTP_PASSWORD = "ILoveHugs321";
    private static final String FTP_BASE_DIR = "/public_html/ReceptyApp";
    private static final String PUBLIC_BASE_URL = "https://cambalik.eu/ReceptyApp";
    private Label lblVybrany;
    private Label lblKategoriaInfo;
    private Label lblCasPorcieInfo;
    private Label lblPopisInfo;  // ← NOVÉ
    private ListView<String> postupList;  // ← NOVÉ – checklist pre postup

    public static void startWithUser(Stage stage, String username) {
        loggedInUser = username;
        (new ReceptyFXApp()).start(stage);
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

    private Button createModernButton(String text, String baseColor) {
        Button btn = new Button(text);
        String baseStyle = "-fx-background-color: " + baseColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 11 28; -fx-background-radius: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0.3, 0, 4);";
        String hoverStyle = baseStyle + "-fx-background-color: " + adjustColor(baseColor, 30) + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 16, 0.4, 0, 6); -fx-translate-y: -3;";
        btn.setStyle(baseStyle);
        btn.setCursor(Cursor.HAND);
        btn.setOnMouseEntered((e) -> {
            btn.setStyle(hoverStyle);
        });
        btn.setOnMouseExited((e) -> {
            btn.setStyle(baseStyle);
        });
        return btn;
    }

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        System.setProperty("javafx.platform", "win");
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        nacitajRecepty();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // TOP BAR
        HBox topBar = new HBox(15.0);
        topBar.setPadding(new Insets(14.0, 25.0, 14.0, 25.0));
        topBar.getStyleClass().add("top-bar-custom");

        HBox leftActions = new HBox(10.0);
        leftActions.setAlignment(Pos.CENTER_LEFT);

        Button btnNovy = createModernButton("Pridať nový recept", "#27ae60");
        btnNovy.setOnAction(e -> otvorNovyReceptDialog(stage, null));

        Button btnUpravit = createModernButton("Upraviť recept", "#2980b9");
        btnUpravit.setOnAction(e -> {
            Recept selected = tableRecepty.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alert("Chyba", "Najprv vyberte recept na úpravu!");
                return;
            }
            otvorNovyReceptDialog(stage, selected);
        });

        Button btnZmazat = createModernButton("Zmazať recept", "#e74c3c");
        btnZmazat.setOnAction(e -> zmazRecept());

        if (UserSession.getInstance() != null) {
            boolean admin = UserSession.getInstance().isAdmin();
            btnZmazat.setVisible(admin);
            btnZmazat.setManaged(admin);
            btnUpravit.setVisible(admin);
            btnUpravit.setManaged(admin);
        }

        Button btnExcel = createModernButton("Exportovať do Excelu", "#f39c12");
        btnExcel.setOnAction(e -> {
            Recept r = tableRecepty.getSelectionModel().getSelectedItem();
            if (r != null) {
                new ReceptExcelExporter().export(r);
            } else {
                alert("Chyba", "Vyberte recept zo zoznamu!");
            }
        });

        leftActions.getChildren().addAll(btnNovy, btnUpravit, btnZmazat, btnExcel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox rightBox = new HBox(15.0);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        ToggleButton themeBtn = new ToggleButton();
        themeBtn.getStyleClass().addAll("button", "pill", "accent");
        themeBtn.setPadding(new Insets(0, 10, 0, 10));
        themeBtn.setMinHeight(35);
        themeBtn.setText("\uD83C\uDF19");

        themeBtn.setOnAction(e -> {
            var styleClasses = root.getStyleClass();
            if (themeBtn.isSelected()) {
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                if (!styleClasses.contains("dark-mode")) styleClasses.add("dark-mode");
                themeBtn.setText("\u2600");
            } else {
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                styleClasses.remove("dark-mode");
                themeBtn.setText("\uD83C\uDF19");
            }
        });

        Label lblUser = new Label("Prihlásený: " + loggedInUser);
        lblUser.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15.0));
        lblUser.getStyleClass().add("user-label");

        Button btnZmenitHeslo = createModernButton("Zmeniť heslo", "#9b59b6");
        btnZmenitHeslo.setOnAction(e -> showPasswordChangeDialog());

        Button btnLogout = createModernButton("Odhlásiť", "#e74c3c");
        btnLogout.setOnAction(e -> {
            stage.close();
            new LoginApp().start(new Stage());
        });

        rightBox.getChildren().addAll(lblUser, btnZmenitHeslo, btnLogout, themeBtn);

        topBar.getChildren().addAll(leftActions, spacer, rightBox);
        root.setTop(topBar);

        // HLAVNÝ OBSAH
        // ====================== HLAVNÝ OBSAH ======================
        GridPane main = new GridPane();
        main.setHgap(20.0);
        main.setVgap(15.0);
        main.setPadding(new Insets(20.0));

        VBox left = createLeftPanel();
        VBox center = createCenterPanel(stage);
        VBox right = createRightPanel();

// KĽÚČOVÉ: Panely sa budú vertikálne rozťahovať na celú výšku okna
        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);  // všetky riadky (je len jeden) sa rozťahujú
        main.getRowConstraints().add(row);

// Pridáme panely do GridPane
        main.add(left, 0, 0);
        main.add(center, 1, 0);
        main.add(right, 2, 0);

// Šírka panelov (môžeš doladiť podľa potreby)
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(35.0);   // ľavý panel

        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(40.0);   // stredný panel (najširší – postup receptu)

        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(25.0);   // pravý panel (zoznam receptov)

        main.getColumnConstraints().addAll(c1, c2, c3);

        root.setCenter(main);

        Scene scene = new Scene(root);
        stage.setTitle("Správa Receptov");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }



    private void showPasswordChangeDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Zmeniť heslo");
        dialog.setHeaderText("Zadajte aktuálne a nové heslo pre používateľa " + loggedInUser);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(10.0);
        grid.setPadding(new Insets(20.0, 150.0, 10.0, 10.0));
        StackPane oldPassContainer = LoginApp.createPasswordToggle("Aktuálne heslo");
        PasswordField oldPass = (PasswordField) oldPassContainer.getChildren().stream().filter(node -> node instanceof PasswordField).findFirst().orElse(null);
        StackPane newPassContainer = LoginApp.createPasswordToggle("Nové heslo");
        PasswordField newPass = (PasswordField) newPassContainer.getChildren().stream().filter(node -> node instanceof PasswordField).findFirst().orElse(null);
        StackPane confirmPassContainer = LoginApp.createPasswordToggle("Potvrdenie nového hesla");
        PasswordField confirmPass = (PasswordField) confirmPassContainer.getChildren().stream().filter(node -> node instanceof PasswordField).findFirst().orElse(null);
        grid.add(new Label("Aktuálne heslo:"), 0, 0);
        grid.add(oldPassContainer, 1, 0);
        grid.add(new Label("Nové heslo:"), 0, 1);
        grid.add(newPassContainer, 1, 1);
        grid.add(new Label("Potvrďte heslo:"), 0, 2);
        grid.add(confirmPassContainer, 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String stare = oldPass.getText();
                String nove = newPass.getText();
                String potvrd = confirmPass.getText();
                if (!nove.equals(potvrd)) {
                    alert("Chyba", "Nové heslá sa nezhodujú");
                    return null;
                }
                if (nove.trim().isEmpty()) {
                    alert("Chyba", "Nové heslo nemôže byť prázdne");
                    return null;
                }
                boolean uspech = this.db.changePassword(loggedInUser, stare, nove);
                if (uspech) {
                    alert("Úspech", "Heslo bolo zmenené");
                    int userId = this.db.getUserIdByUsername(loggedInUser);
                    String email = this.db.getEmailPodlaId(userId);
                    if (email != null && !email.isEmpty() && email.contains("@")) {
                        String newPasswordMessage = "Vaše heslo bolo zmenené na: " + nove;
                        EmailSender.sendEmail(email, "Zmena hesla v Receptári", newPasswordMessage);
                    } else {
                        System.out.println("Používateľ " + loggedInUser + " nemá vyplnený email, email sa neposlal.");
                    }
                    dialog.close();
                } else {
                    alert("Chyba", "Staré heslo nie je správne");
                }
            }
            return btn;
        });
        dialog.showAndWait();
    }

    private void otvorNovyReceptDialog(Stage parentStage, Recept receptNaUprava) {
        boolean jeUprava = (receptNaUprava != null);

        Stage dialog = new Stage();
        dialog.setTitle(jeUprava ? "Upraviť recept" : "Pridať nový recept");
        dialog.initOwner(parentStage);
        dialog.setResizable(true);
        dialog.setMaximized(true);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // TOP BAR
        HBox dialogTopBar = new HBox(15);
        dialogTopBar.setPadding(new Insets(14, 25, 14, 25));
        dialogTopBar.getStyleClass().add("top-bar-custom");
        dialogTopBar.setAlignment(Pos.CENTER_LEFT);

        Button btnUlozit = createModernButton("Uložiť recept", "#27ae60");
        Button btnZrusit = createModernButton("Zrušiť", "#e74c3c");
        btnZrusit.setOnAction(e -> dialog.close());

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        dialogTopBar.getChildren().addAll(btnUlozit, btnZrusit, topSpacer);
        root.setTop(dialogTopBar);

        // HLAVNÝ OBSAH – VŽDY DVA PANELY
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);

        // ĽAVÝ PANEL – FORMULÁR + POSTUP RECEPTU
        VBox leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(20));
        leftPanel.getStyleClass().add("card"); // Toto prepojí panel s farbou -fx-control-inner-background
        ScrollPane leftScroll = new ScrollPane(leftPanel);
        leftScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        leftScroll.setFitToWidth(true);
        leftScroll.setFitToHeight(true);

        TextField tfNazov = new TextField();
        tfNazov.setPromptText("Názov receptu");

        Label lblPopis = new Label("Popis receptu:");
        lblPopis.getStyleClass().add("dialog-form-label");

        TextArea taPopis = new TextArea();
        taPopis.setPromptText("Krátky popis receptu (napr. 'Rýchly obed pre 4 osoby')");
        taPopis.setWrapText(true);
        taPopis.setPrefRowCount(4);

        HBox pripravaBox = new HBox(15);
        Label lblCasPripravy = new Label("Čas prípravy:");
        lblCasPripravy.getStyleClass().add("dialog-form-label");
        TextField tfCasPripravy = new TextField();
        tfCasPripravy.setPromptText("Príprava (min)");
        tfCasPripravy.setPrefWidth(150);
        tfCasPripravy.getStyleClass().add("dialog-form-field");
        pripravaBox.getChildren().addAll(lblCasPripravy, tfCasPripravy);

        HBox varenieBox = new HBox(15);
        Label lblCasVarenia = new Label("Čas varenia/pečení:");
        lblCasVarenia.getStyleClass().add("dialog-form-label");
        TextField tfCasVarenia = new TextField();
        tfCasVarenia.setPromptText("Varenie/Pečenie (min)");
        tfCasVarenia.setPrefWidth(150);
        tfCasVarenia.getStyleClass().add("dialog-form-field");
        varenieBox.getChildren().addAll(lblCasVarenia, tfCasVarenia);

        HBox porcieBox = new HBox(15);
        Label lblPorcie = new Label("Počet porcií:");
        lblPorcie.getStyleClass().add("dialog-form-label");
        TextField tfPorcie = new TextField();
        tfPorcie.setPromptText("Porcie");
        tfPorcie.setPrefWidth(150);
        tfPorcie.getStyleClass().add("dialog-form-field");
        porcieBox.getChildren().addAll(lblPorcie, tfPorcie);

        ComboBox<Kategoria> cbKategoria = new ComboBox<>();
        cbKategoria.setItems(FXCollections.observableArrayList(db.nacitajVsetkyKategorie()));
        cbKategoria.setPromptText("Vyberte kategóriu");
        cbKategoria.setPrefWidth(300);

        // POSTUP RECEPTU – CHECKLIST
        Label lblPostup = new Label("Postup receptu:");
        lblPostup.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        ObservableList<String> krokyList = FXCollections.observableArrayList();

        ListView<String> postupListEdit = new ListView<>(krokyList);
        postupListEdit.setCellFactory(lv -> new PostupCellEdit(krokyList, postupListEdit));
        VBox.setVgrow(postupListEdit, Priority.ALWAYS);

        HBox postupButtons = new HBox(10);
        Button btnPridatKrok = createModernButton("Pridať krok", "#27ae60");
        btnPridatKrok.setOnAction(e -> {
            krokyList.add("Nový krok – klikni pre editáciu");
            postupListEdit.scrollTo(krokyList.size() - 1);
        });
        postupButtons.getChildren().add(btnPridatKrok);

        leftPanel.getChildren().addAll(
                new Label("Názov receptu:"), tfNazov,
                lblPopis, taPopis,
                pripravaBox, varenieBox, porcieBox,
                new Label("Kategória:"), cbKategoria,
                lblPostup, postupButtons, postupListEdit
        );

        // PRAVÝ PANEL – OBRÁZOK + INGREDIENCIE
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(20));
        rightPanel.getStyleClass().add("card"); // Toto zabezpečí konzistenciu s hlavnou plochou
        ScrollPane rightScroll = new ScrollPane(rightPanel);
        rightScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);

        ImageView dialogImgView = new ImageView();
        dialogImgView.setFitWidth(400);
        dialogImgView.setFitHeight(300);
        dialogImgView.setPreserveRatio(true);
        dialogImgView.setSmooth(true);

        Button btnVybratObrazok = createModernButton("Vybrať obrázok", "#3498db");
        Button btnZmazatObrazok = createModernButton("Zmazať obrázok", "#e74c3c");

        if (UserSession.getInstance() != null) {
            boolean admin = UserSession.getInstance().isAdmin();
            btnZmazatObrazok.setVisible(admin);
            btnZmazatObrazok.setManaged(admin);
        }

        HBox imgButtons = new HBox(15, btnVybratObrazok, btnZmazatObrazok);
        imgButtons.setAlignment(Pos.CENTER);

        Label lblPridatIng = new Label("Pridať ingredienciu:");
        lblPridatIng.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        ComboBox<String> cbIngrediencia = new ComboBox<>();
        cbIngrediencia.setEditable(true);
        cbIngrediencia.setItems(FXCollections.observableArrayList(db.nacitajVsetkyNazvyIngrediencii()));
        cbIngrediencia.setPromptText("Zadajte alebo vyberte ingredienciu");
        cbIngrediencia.setPrefWidth(400);


        HBox mnozstvoJednotkaBox = new HBox(15);
        TextField tfMnozstvo = new TextField();
        tfMnozstvo.setPromptText("Množstvo (napr. 200)");
        tfMnozstvo.setPrefWidth(150);
        TextField tfJednotka = new TextField();
        tfJednotka.setPromptText("Jednotka (g, ks, l...)");
        tfJednotka.setPrefWidth(150);
        mnozstvoJednotkaBox.getChildren().addAll(tfMnozstvo, tfJednotka);

        ObservableList<IngredienciaMnozstvo> docasneIng = FXCollections.observableArrayList();
        TableView<IngredienciaMnozstvo> tableDocasne = new TableView<>(docasneIng);

        Button btnPridatIng = createModernButton("Pridať do receptu", "#27ae60");
        btnPridatIng.setOnAction(e -> {
            String nazov = cbIngrediencia.getValue();
            String mnozstvoStr = tfMnozstvo.getText().trim();
            String jednotka = tfJednotka.getText().trim();

            if (nazov != null && !nazov.isEmpty() && !mnozstvoStr.isEmpty()) {
                try {
                    double mnozstvo = Double.parseDouble(mnozstvoStr.replace(",", "."));

                    // Zavoláme novú metódu z DBConnect
                    double kcalNa100g = db.zistiKaloriePreIngredienciu(nazov);

                    // Výpočet: (množstvo / 100) * kalórie na 100g
                    double vypocitaneKalorie = (mnozstvo / 100.0) * kcalNa100g;

                    // Pridanie do tabuľky (vďaka ObservableList sa tabuľka hneď prekreslí)
                    docasneIng.add(new IngredienciaMnozstvo(nazov, mnozstvo, jednotka, vypocitaneKalorie));

                    // Vyčistenie polí pre ďalší vstup
                    tfMnozstvo.clear();
                    tfJednotka.clear();
                    cbIngrediencia.getEditor().clear();
                    cbIngrediencia.setValue(null);

                } catch (NumberFormatException ex) {
                    alert("Chyba", "Množstvo musí byť číslo (napr. 150 alebo 150.5).");
                }
            } else {
                alert("Chyba", "Vyberte ingredienciu a zadajte množstvo.");
            }
        });

        tableDocasne.getStyleClass().add("table-view");

        TableColumn<IngredienciaMnozstvo, String> colNazov = new TableColumn<>("Ingrediencia");
        colNazov.setCellValueFactory(new PropertyValueFactory<>("nazovIngrediencie"));
        colNazov.setPrefWidth(220);
        TableColumn<IngredienciaMnozstvo, Double> colMnoz = new TableColumn<>("Množstvo");
        colMnoz.setCellValueFactory(new PropertyValueFactory<>("mnozstvo"));
        TableColumn<IngredienciaMnozstvo, String> colJedn = new TableColumn<>("Jednotka");
        colJedn.setCellValueFactory(new PropertyValueFactory<>("jednotka"));
        TableColumn<IngredienciaMnozstvo, Double> colKcal = new TableColumn<>("Kalórie (kcal)");
        colKcal.setCellValueFactory(new PropertyValueFactory<>("kalorie"));
        colKcal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1f", item));
            }
        });

        tableDocasne.getColumns().addAll(colNazov, colMnoz, colJedn, colKcal);
        tableDocasne.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableDocasne, Priority.ALWAYS);

        rightPanel.getChildren().addAll(
                new Label("Obrázok receptu"), dialogImgView, imgButtons,
                lblPridatIng, cbIngrediencia, mnozstvoJednotkaBox, btnPridatIng, tableDocasne
        );

        splitPane.getItems().addAll(leftScroll, rightScroll);

        root.setCenter(splitPane);

        // ====================== TMAVÝ REŽIM – PLNÁ A OKAMŽITÁ SYNCHRONIZÁCIA ======================
        Scene mainScene = parentStage.getScene();
        if (mainScene != null) {
            // 1. Skopírujeme aktuálny user agent stylesheet (PrimerLight/Dark)
            String currentTheme = Application.getUserAgentStylesheet();
            Application.setUserAgentStylesheet(currentTheme);

            // 2. Okamžite pridáme "dark-mode" ak je hlavné okno v tmavom režime
            if (mainScene.getRoot().getStyleClass().contains("dark-mode")) {
                root.getStyleClass().add("dark-mode");
            }

            // 3. Listener na zmenu režimu – aktualizuje dialóg v reálnom čase
            mainScene.getRoot().getStyleClass().addListener((javafx.collections.ListChangeListener<String>) change -> {
                while (change.next()) {
                    if (change.wasAdded() && change.getAddedSubList().contains("dark-mode")) {
                        Platform.runLater(() -> {
                            if (!root.getStyleClass().contains("dark-mode")) {
                                root.getStyleClass().add("dark-mode");
                            }
                        });
                    } else if (change.wasRemoved() && change.getRemoved().contains("dark-mode")) {
                        Platform.runLater(() -> root.getStyleClass().remove("dark-mode"));
                    }
                }
            });
        }

        Scene scene = new Scene(root);

        URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        dialog.setScene(scene);
        dialog.showAndWait();
    }private void nacitajRecepty() {
        this.recepty.clear();
        this.recepty.addAll(this.db.nacitajVsetkyRecepty());
    }

    private VBox createLeftPanel() {
        VBox box = new VBox(15.0);
        box.setPadding(new Insets(18.0));
        box.getStyleClass().add("card");
        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

        // Nadpis "Recept:"
        Label title = new Label("Recept:");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20.0));
        title.getStyleClass().add("recipe-title-label");  // ← PRIDAŤ

        // Názov vybraného receptu (presunutý sem)
        this.lblVybrany = new Label("-");
        this.lblVybrany.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18.0));
        this.lblVybrany.setStyle("-fx-text-fill: #34495e;");
        this.lblVybrany.setWrapText(true);
        this.lblVybrany.getStyleClass().add("recipe-name-label");  // ← PRIDAŤ

        // Fotka
        this.imgView = new ImageView();
        this.imgView.setFitWidth(380.0);
        this.imgView.setFitHeight(300.0);
        this.imgView.setPreserveRatio(true);
        this.imgView.setSmooth(true);

        // Tabuľka ingrediencií
        this.tableIngrediencie = new TableView<>();
        this.tableIngrediencie.getStyleClass().add("table-view");
        this.tableIngrediencie.setItems(this.ingrediencieReceptu);

        TableColumn<IngredienciaMnozstvo, String> colIngNazov = new TableColumn<>("Ingrediencia");
        colIngNazov.setCellValueFactory(new PropertyValueFactory<>("nazovIngrediencie"));
        colIngNazov.setPrefWidth(200);

        TableColumn<IngredienciaMnozstvo, Double> colIngMnozstvo = new TableColumn<>("Množstvo");
        colIngMnozstvo.setCellValueFactory(new PropertyValueFactory<>("mnozstvo"));

        TableColumn<IngredienciaMnozstvo, String> colIngJednotka = new TableColumn<>("Jednotka");
        colIngJednotka.setCellValueFactory(new PropertyValueFactory<>("jednotka"));

        TableColumn<IngredienciaMnozstvo, Double> colKcal = new TableColumn<>("Kalórie (kcal)");
        colKcal.setCellValueFactory(new PropertyValueFactory<>("kalorie"));
        colKcal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1f", item));
            }
        });

        this.tableIngrediencie.getColumns().addAll(colIngNazov, colIngMnozstvo, colIngJednotka, colKcal);
        this.tableIngrediencie.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label lblIng = new Label("Ingrediencie:");
        lblIng.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14.0));

        VBox.setVgrow(this.tableIngrediencie, Priority.ALWAYS);

        // Pridáme v poradí: Recept: → názov receptu → fotka → ingrediencie
        box.getChildren().addAll(title, this.lblVybrany, imgView, lblIng, this.tableIngrediencie);
        return box;
    }

    private VBox createCenterPanel(Stage stage) {
        VBox box = new VBox(20.0);
        box.setPadding(new Insets(20.0));
        box.getStyleClass().add("card");
        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

        // Hlavička s informáciami o receptu
        VBox headerBox = new VBox(8.0);

        Label lblKategoria = new Label();
        lblKategoria.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16.0));
        lblKategoria.setStyle("-fx-text-fill: #27ae60;");
        lblKategoria.getStyleClass().add("recipe-category-label");

        Label lblCasPorcie = new Label();
        lblCasPorcie.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15.0));
        lblCasPorcie.setStyle("-fx-text-fill: #34495e;");
        lblCasPorcie.getStyleClass().add("recipe-time-portions-label");

        // Popis receptu – pridávame sem
        Label lblPopis = new Label();
        lblPopis.setWrapText(true);
        lblPopis.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14.0));
        lblPopis.setStyle("-fx-text-fill: #34495e;");
        lblPopis.getStyleClass().add("recipe-popis-label");

        headerBox.getChildren().addAll(lblKategoria, lblCasPorcie, lblPopis);  // popis medzi kategóriou/časom a postupom

        // Nadpis Postup prípravy
        Label lblPostup = new Label("Postup prípravy");
        lblPostup.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18.0));

        this.postupList = new ListView<>();  // ← String, nie HBox
        this.postupList.setCellFactory(lv -> new PostupCell());
        this.postupList.setFocusTraversable(false);
        this.postupList.getStyleClass().add("postup-list");
        VBox.setVgrow(this.postupList, Priority.ALWAYS);
        this.postupList.setPrefWidth(Region.USE_COMPUTED_SIZE);

        box.getChildren().addAll(headerBox, lblPostup, this.postupList);

        this.lblKategoriaInfo = lblKategoria;
        this.lblCasPorcieInfo = lblCasPorcie;
        this.lblPopisInfo = lblPopis;

        return box;
    }

    private VBox createRightPanel() {
        VBox box = new VBox(10.0);
        box.setPadding(new Insets(18.0));
        box.getStyleClass().add("card");
        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");

        Label title = new Label("Zoznam receptov");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17.0));

        this.tfSearch = new TextField();
        this.tfSearch.setPromptText("Hľadať recept...");
        this.tfSearch.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());

        this.cbFilterKategoria = new ComboBox<>();
        List<Kategoria> kategorieZDB = db.nacitajVsetkyKategorie();
        ObservableList<Kategoria> vsetkyKategorie = FXCollections.observableArrayList();
        vsetkyKategorie.add(new Kategoria(-1, "Všetky kategórie"));
        vsetkyKategorie.addAll(kategorieZDB);
        this.cbFilterKategoria.setItems(vsetkyKategorie);
        this.cbFilterKategoria.setPromptText("Filter podľa kategórie");
        this.cbFilterKategoria.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());

        HBox searchBox = new HBox(10.0);
        searchBox.getChildren().addAll(this.tfSearch, this.cbFilterKategoria);
        HBox.setHgrow(this.tfSearch, Priority.ALWAYS);

        this.tableRecepty = new TableView<>();
        this.filteredRecepty = new FilteredList<>(this.recepty);
        this.sortedRecepty = new SortedList<>(this.filteredRecepty);
        this.sortedRecepty.comparatorProperty().bind(this.tableRecepty.comparatorProperty());
        this.tableRecepty.setItems(this.sortedRecepty);

        TableColumn<Recept, String> colNazov = new TableColumn<>("Názov");
        colNazov.setCellValueFactory(new PropertyValueFactory<>("nazov"));
        colNazov.setPrefWidth(250);

        TableColumn<Recept, Integer> colCas = new TableColumn<>("Čas pripravy (min)");
        colCas.setCellValueFactory(new PropertyValueFactory<>("casPripravy"));

        TableColumn<Recept, Integer> colVarenie = new TableColumn<>("Čas varenia/pečenia (min)");
        colVarenie.setCellValueFactory(new PropertyValueFactory<>("casVarenia"));
        colVarenie.setPrefWidth(100);

        TableColumn<Recept, Integer> colPorcie = new TableColumn<>("Porcie");
        colPorcie.setCellValueFactory(new PropertyValueFactory<>("pocetPorcii"));

        TableColumn<Recept, String> colKategoria = new TableColumn<>("Kategória");
        colKategoria.setCellValueFactory(new PropertyValueFactory<>("kategoriaNazov"));

        TableColumn<Recept, String> colAutor = new TableColumn<>("Autor");
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));

        this.tableRecepty.getColumns().addAll(colNazov, colCas,colVarenie, colPorcie, colKategoria, colAutor);
        this.tableRecepty.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        this.tableRecepty.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if (newSel != null) {
                zobrazRecept(newSel);
            } else {
                vycistiDetail();
            }
        });

        nastylujTabulku(this.tableRecepty);
        VBox.setVgrow(this.tableRecepty, Priority.ALWAYS);

        box.getChildren().addAll(title, searchBox, this.tableRecepty);
        return box;
    }

    private void vycistiDetail() {
        this.lblVybrany.setText("-");
        this.imgView.setImage(null);
        this.ingrediencieReceptu.clear();
        this.lblPopisInfo.setText("");
        this.lblKategoriaInfo.setText("");
        this.lblCasPorcieInfo.setText("");
        this.postupList.getItems().clear();
    }

    private void updateFilter() {
        this.filteredRecepty.setPredicate(recept -> {
            Kategoria selectedKategoria = this.cbFilterKategoria.getValue();
            boolean matchesKategoria = (selectedKategoria == null || selectedKategoria.getId() == -1 || recept.getKategoriaId() == selectedKategoria.getId());
            if (!matchesKategoria) return false;

            String searchText = this.tfSearch.getText().toLowerCase().trim();
            if (searchText.isEmpty()) return true;

            if (recept.getNazov().toLowerCase().contains(searchText)) return true;
            if (String.valueOf(recept.getCasPripravy()).contains(searchText)) return true;
            if (String.valueOf(recept.getPocetPorcii()).contains(searchText)) return true;

            for (IngredienciaMnozstvo ing : db.nacitajIngredienciePreRecept(recept.getReceptId())) {
                if (ing.getNazovIngrediencie().toLowerCase().contains(searchText)) return true;
            }

            return false;
        });
    }

    private void zobrazRecept(Recept r) {
        this.lblVybrany.setText(r.getNazov() + " (autor: " + r.getAutor() + ")");

        this.obrazokCesta = r.getObrazokCesta();
        nastavObrazokZUrl(this.obrazokCesta);

        this.ingrediencieReceptu.clear();
        this.ingrediencieReceptu.addAll(db.nacitajIngredienciePreRecept(r.getReceptId()));

        this.lblKategoriaInfo.setText("\uD83C\uDFF7\uFE0F Kategória: " + r.getKategoriaNazov());

        String casText = "\uD83D\uDD52 Čas prípravy: " + r.getCasPripravy() + " min";
        String varenieText = "\uD83C\uDF72 Čas varenia/pečení: " + r.getCasVarenia() + " min";
        String porcieText = "\uD83C\uDF7D\uFE0F Počet porcií: " + r.getPocetPorcii();

        this.lblCasPorcieInfo.setText(casText + "   |   " + varenieText + "   |   " + porcieText);

        this.lblPopisInfo.setText(r.getPopis() != null ? r.getPopis() : "");

        this.postupList.getItems().clear();
        if (r.getPostup() != null && !r.getPostup().trim().isEmpty()) {
            String[] kroky = r.getPostup().split("\n");
            for (String krok : kroky) {
                String trimmed = krok.trim();
                if (!trimmed.isEmpty()) {
                    this.postupList.getItems().add(trimmed);
                }
            }
        }
    }


    private void zmazRecept() {
        Recept selected = tableRecepty.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Chyba", "Najprv vyberte recept na zmazanie!");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setHeaderText("Naozaj chcete zmazať recept \"" + selected.getNazov() + "\"?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            db.zmazRecept(selected.getReceptId());
            nacitajRecepty();
            vycistiDetail();
            alert("Úspech", "Recept zmazaný");
        }
    }

    private String uploadViaFtp(File localFile, String remotePath) {
        FTPClient ftp = new FTPClient();
        String part = null;
        try {
            System.out.println("Pripojujem sa k FTP: " + FTP_SERVER + ":" + FTP_PORT);
            ftp.connect(FTP_SERVER, FTP_PORT);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                System.err.println("FTP server odmietol spojenie! Kód: " + reply);
                return null;
            }
            if (!ftp.login(FTP_USER, FTP_PASSWORD)) {
                System.err.println("Chyba prihlasovania na FTP!");
                return null;
            }
            System.out.println("Prihlásený na FTP");
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            String[] parts = FTP_BASE_DIR.split("/");
            String path = "";
            for (String p : parts) {
                if (!p.isEmpty()) {
                    path += "/" + p;
                    if (!ftp.changeWorkingDirectory(path)) {
                        System.out.println("Vytváram priečinok: " + path);
                        if (!ftp.makeDirectory(path)) {
                            System.err.println("Nepodarilo sa vytvoriť priečinok: " + path);
                            return null;
                        }
                        ftp.changeWorkingDirectory(path);
                    }
                }
            }
            String receptId = remotePath.substring(0, remotePath.indexOf("/"));
            if (!ftp.changeWorkingDirectory(receptId)) {
                System.out.println("Vytváram priečinok pre recept: " + receptId);
                if (!ftp.makeDirectory(receptId)) {
                    System.err.println("Nepodarilo sa vytvoriť priečinok receptu: " + receptId);
                    return null;
                }
                ftp.changeWorkingDirectory(receptId);
            }
            String fileName = remotePath.substring(remotePath.indexOf("/") + 1);
            System.out.println("Nahrávam súbor: " + fileName);
            FileInputStream fis = new FileInputStream(localFile);
            try {
                if (!ftp.storeFile(fileName, fis)) {
                    System.err.println("Nahrávanie zlyhalo! Reply: " + ftp.getReplyString());
                    fis.close();
                    return null;
                }
            } catch (Throwable var14) {
                fis.close();
                throw var14;
            } finally {
                fis.close();
            }
            System.out.println("Súbor úspešne nahraný!");
            ftp.logout();
            ftp.disconnect();
            String finalUrl = PUBLIC_BASE_URL + "/" + receptId + "/" + fileName;
            System.out.println("Obrázok dostupný na: " + finalUrl);
            return finalUrl;
        } catch (Exception var15) {
            System.err.println("VÝNIMKA pri FTP nahrávaní:");
            var15.printStackTrace();
            Platform.runLater(() -> this.alert("FTP Chyba", "Detail: " + var15.getMessage()));
            try {
                if (ftp.isConnected()) {
                    ftp.disconnect();
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return lastIndex == -1 ? "" : name.substring(lastIndex).toLowerCase();
    }

    private void nastavObrazokZUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            new Thread(() -> {
                try {
                    URL u = new URL(url.trim());
                    try (InputStream in = u.openStream()) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        byte[] imageData = baos.toByteArray();
                        Platform.runLater(() -> {
                            Image image = new Image(new ByteArrayInputStream(imageData));
                            this.imgView.setImage(image);
                            System.out.println("Obrázok úspešne načítaný: " + url);
                        });
                    }
                } catch (Exception var10) {
                    System.err.println("Nepodarilo sa načítať obrázok z URL: " + url);
                    var10.printStackTrace();
                    Platform.runLater(() -> this.imgView.setImage(null));
                }
            }).start();
        } else {
            this.imgView.setImage(null);
        }
    }

    // Custom cell pre postup receptu – zabezpečí zalomenie a checkbox
    private static class PostupCell extends ListCell<String> {
        private final CheckBox checkBox = new CheckBox();
        private final Label label = new Label();
        private final HBox hbox = new HBox(15, checkBox, label);

        public PostupCell() {
            super();
            label.setWrapText(true); // Povolí zalamovanie textu
            label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15.0));

            // Zabezpečí, aby label nevyužíval elipsu (...) ale text zalomil
            label.setMinHeight(Region.USE_PREF_SIZE);

            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setPadding(new Insets(8, 5, 8, 5));
            HBox.setHgrow(label, Priority.ALWAYS);

            // KĽÚČOVÁ ČASŤ:
            // Musíme nabindovať preferovanú šírku labelu na šírku ListView (mínus miesto pre checkbox)
            // Používame šírku samotnej bunky (this.widthProperty())
            label.prefWidthProperty().bind(widthProperty().subtract(70));
            label.maxWidthProperty().bind(widthProperty().subtract(70));

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                int index = getIndex() + 1;
                label.setText(index + ". " + item);
                setGraphic(hbox);
            }
        }
    }

    private static class PostupCellEdit extends ListCell<String> {
        private final TextField textField = new TextField();
        private final Label numberLabel = new Label();
        private final HBox hbox = new HBox(10, numberLabel, textField);

        public PostupCellEdit(ObservableList<String> list, ListView<String> listView) {
            textField.setPromptText("Klikni pre editáciu kroku");
            textField.setOnAction(e -> commitEdit(textField.getText()));
            textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    commitEdit(textField.getText());
                }
            });

            // Mazanie kroku dvojklikom
            setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && getItem() != null) {
                    list.remove(getIndex());
                }
            });

            hbox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(textField, Priority.ALWAYS);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                numberLabel.setText((getIndex() + 1) + ".");
                numberLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
                textField.setText(item);
                setGraphic(hbox);
            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
            textField.setText(getItem());
            textField.requestFocus();
            textField.selectAll();
        }

        @Override
        public void commitEdit(String newValue) {
            super.commitEdit(newValue);
            getListView().getItems().set(getIndex(), newValue.isEmpty() ? "Prázdny krok" : newValue);
        }
    }
    private void alert(String title, String msg) {
        Alert a = new Alert(AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void nastylujTabulku(TableView<?> table) {
        if (table == null) {
            System.out.println("Tabuľka je null – preskočené štýlovanie");
        } else {
            table.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4);");
            table.setFixedCellSize(45.0);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}