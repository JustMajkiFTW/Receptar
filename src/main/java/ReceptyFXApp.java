import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private final ObservableList<IngredienciaMnozstvo> docasneIngrediencie = FXCollections.observableArrayList();
    private final ObservableList<IngredienciaMnozstvo> ingrediencieReceptu = FXCollections.observableArrayList();
    private TableView<Recept> tableRecepty;
    private TableView<IngredienciaMnozstvo> tableIngrediencie;
    private TableView<IngredienciaMnozstvo> tableDocasne;
    private TextField tfNazov;
    private TextField tfCas;
    private TextField tfPorcie;
    private TextField tfMnozstvo;
    private TextField tfJednotka;
    private TextField tfSearch;
    private ComboBox<Kategoria> cbFilterKategoria;
    private TextArea taPostup;
    private ComboBox<String> cbIngrediencia;
    private ComboBox<Kategoria> cbKategoria;
    private ImageView imgView;
    private Label lblId;
    private Label lblVybrany;
    private String obrazokCesta = "";
    public static String loggedInUser = "";
    private FilteredList<Recept> filteredRecepty;
    private SortedList<Recept> sortedRecepty;
    private static final String FTP_SERVER = "";
    private static final int FTP_PORT = ;
    private static final String FTP_USER = "";
    private static final String FTP_PASSWORD = "";
    private static final String FTP_BASE_DIR = "/public_html/ReceptyApp";
    private static final String PUBLIC_BASE_URL = "https://cambalik.eu/ReceptyApp";

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

    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        System.setProperty("javafx.platform", "win");
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        this.cbKategoria = new ComboBox<>();
        this.cbIngrediencia = new ComboBox<>();
        this.cbKategoria.setItems(FXCollections.observableArrayList(this.db.nacitajVsetkyKategorie()));
        this.cbIngrediencia.setItems(FXCollections.observableArrayList(this.db.nacitajVsetkyNazvyIngrediencii()));
        nacitajRecepty();
        this.cbKategoria.setPrefWidth(300.0);
        this.cbIngrediencia.setPrefWidth(300.0);
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        HBox topBar = new HBox(15.0);
        topBar.setPadding(new Insets(14.0, 25.0, 14.0, 25.0));
        topBar.getStyleClass().add("top-bar-custom");
        HBox leftActions = new HBox(10.0);
        leftActions.setAlignment(Pos.CENTER_LEFT);
        Button btnNovy = createModernButton("Pridať nový recept", "#27ae60");
        btnNovy.setOnAction(e -> ulozRecept(true));
        Button btnUlozit = createModernButton("Uložiť", "#2980b9");
        btnUlozit.setOnAction(e -> ulozRecept(false));
        Button btnZmazat = createModernButton("Zmazať recept", "#e74c3c");
        btnZmazat.setOnAction(e -> zmazRecept());
        if (UserSession.getInstance() != null) {
            boolean admin = UserSession.getInstance().isAdmin();
            btnZmazat.setVisible(admin);
            btnZmazat.setManaged(admin);
        }
        Button btnVycistit = createModernButton("Vyčistiť formulár", "#95a5a6");
        btnVycistit.setOnAction(e -> vycistiFormular());
        Button btnExcel = createModernButton("Exportovať do Excelu", "#f39c12");
        btnExcel.setOnAction(e -> {
            Recept r = tableRecepty.getSelectionModel().getSelectedItem();
            if (r != null) {
                (new ReceptExcelExporter()).export(r);
            } else {
                alert("Chyba", "Vyberte recept zo zoznamu!");
            }
        });
        leftActions.getChildren().addAll(btnNovy, btnUlozit, btnZmazat, btnVycistit, btnExcel);
        HBox rightBox = new HBox(15.0);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        ToggleButton themeBtn = new ToggleButton("Tmavý režim");
        themeBtn.getStyleClass().clear();
//        themeBtn.getStyleClass().setAll("my-switch");
        themeBtn.getStyleClass().addAll("button", "pill", "accent");
        themeBtn.setPadding(new Insets(0, 10, 0, 10));
        themeBtn.setMinHeight(35);
        themeBtn.setText("\uD83C\uDF19"); // Predvolená ikonka
        themeBtn.setOnAction(e -> {
            var styleClasses = root.getStyleClass();
            var btnClasses = themeBtn.getStyleClass();

            // 1. Dôležité: Vymažeme text, aby tam nezostávali divné znaky
            themeBtn.setText("");

            if (themeBtn.isSelected()) {
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                if (!styleClasses.contains("dark-mode")) styleClasses.add("dark-mode");

                btnClasses.remove("accent");
                btnClasses.add("faint");

                // Nastavíme len čistú ikonu mesiaca
                themeBtn.setText("\u2600");
            } else {
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                styleClasses.remove("dark-mode");

                btnClasses.remove("faint");
                btnClasses.add("accent");

                // Nastavíme len čistú ikonu slnka
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
            (new LoginApp()).start(new Stage());
        });
        rightBox.getChildren().addAll(lblUser, btnZmenitHeslo, btnLogout, themeBtn);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(leftActions, spacer, rightBox);
        root.setTop(topBar);
        GridPane main = new GridPane();
        main.setHgap(20.0);
        main.setVgap(15.0);
        main.setPadding(new Insets(20.0));
        VBox left = createLeftPanel();
        VBox center = createCenterPanel(stage);
        VBox right = createRightPanel();
        main.add(left, 0, 0);
        main.add(center, 1, 0);
        main.add(right, 2, 0);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(35.0);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(35.0);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(30.0);
        main.getColumnConstraints().addAll(c1, c2, c3);
        root.setCenter(main);
        Scene scene = new Scene(root);
        stage.setTitle("Správa Receptov");
        stage.setScene(scene);
        stage.setMaximized(false);
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

    private void nacitajRecepty() {
        this.recepty.clear();
        this.recepty.addAll(this.db.nacitajVsetkyRecepty());
    }

    private VBox createLeftPanel() {
        VBox box = new VBox(10.0);
        box.setPadding(new Insets(18.0));
        box.getStyleClass().add("card");
        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");        Label title = new Label("Pridanie receptu");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17.0));
        this.lblVybrany = new Label("Vybraný recept: -");
        this.lblId = new Label();
        this.lblId.setVisible(false);
        this.tfNazov = new TextField();
        this.tfNazov.setPromptText("Názov receptu");
        this.tfCas = new TextField();
        this.tfCas.setPromptText("Čas (min)");
        this.tfCas.setPrefWidth(80.0);
        this.tfPorcie = new TextField();
        this.tfPorcie.setPromptText("Porcie");
        this.tfPorcie.setPrefWidth(80.0);
        HBox casPorcie = new HBox(12.0);
        casPorcie.getChildren().addAll(new Label("Čas prípravy:"), this.tfCas, new Label("Počet porcií:"), this.tfPorcie);
        casPorcie.setAlignment(Pos.CENTER_LEFT);
        this.cbKategoria.setPromptText("Kategória receptu");
        this.taPostup = new TextArea();
        this.taPostup.setPromptText("Napíšte postup prípravy");
        this.taPostup.setWrapText(true);
        this.taPostup.setPrefRowCount(18);
        VBox.setVgrow(this.taPostup, Priority.ALWAYS);
        box.getChildren().addAll(title, this.lblVybrany, this.lblId, new Label("Názov:"), this.tfNazov, casPorcie, new Label("Kategória:"), this.cbKategoria, new Label("Postup prípravy:"), this.taPostup);
        return box;
    }

    private VBox createCenterPanel(Stage stage) {
        VBox box = new VBox(20.0);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20.0));
        box.getStyleClass().add("card");
        box.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 5);");        Label lblImg = new Label("Fotka receptu");
        lblImg.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18.0));
        this.imgView = new ImageView();
        this.imgView.setFitWidth(380.0);
        this.imgView.setFitHeight(280.0);
        this.imgView.setPreserveRatio(true);
        Button btnVybrat = createModernButton("Vybrať obrázok", "#3498db");
        btnVybrat.setOnAction(e -> vyberObrazok(stage));
        Button btnZmazatObrazok = createModernButton("Zmazať obrázok", "#e74c3c");
        btnZmazatObrazok.setOnAction(e -> {
            this.imgView.setImage(null);
            this.obrazokCesta = "";
        });
        if (UserSession.getInstance() != null) {
            boolean admin = UserSession.getInstance().isAdmin();
            btnZmazatObrazok.setVisible(admin);
            btnZmazatObrazok.setManaged(admin);
        }
        HBox imgButtons = new HBox(15.0);
        imgButtons.getChildren().addAll(btnVybrat, btnZmazatObrazok);
        imgButtons.setAlignment(Pos.CENTER);
        VBox topSection = new VBox(15.0);
        topSection.getChildren().addAll(lblImg, this.imgView, imgButtons);
        topSection.setAlignment(Pos.CENTER);
        Separator separator = new Separator();
        separator.getStyleClass().add("my-separator");
        VBox ingBox = new VBox(8.0);
        ingBox.getStyleClass().add("card");
        ingBox.setStyle("-fx-padding: 12; -fx-background-radius: 10;");
        Label lblIng = new Label("Pridať ingredienciu:");
        lblIng.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13.0));
        this.cbIngrediencia.setEditable(true);
        this.cbIngrediencia.setPromptText("Ingrediencia:");
        this.tfMnozstvo = new TextField();
        this.tfMnozstvo.setPromptText("Množstvo");
        this.tfMnozstvo.setPrefWidth(100.0);
        this.tfJednotka = new TextField();
        this.tfJednotka.setPromptText("Jednotka");
        this.tfJednotka.setPrefWidth(80.0);
        HBox mnozstvoBox = new HBox(8.0);
        mnozstvoBox.getChildren().addAll(this.tfMnozstvo, this.tfJednotka);
        Button btnPridatIng = createModernButton("Pridať do receptu", "#27ae60");
        btnPridatIng.setOnAction(e -> pridajDocasnuIngredienciu());
        this.tableDocasne = new TableView<>();
        this.tableDocasne.getStyleClass().add("table-view");
        this.tableDocasne.setItems(this.docasneIngrediencie);
        TableColumn<IngredienciaMnozstvo, String> colIng = new TableColumn<>("Ingrediencia");
        colIng.setCellValueFactory(new PropertyValueFactory<>("nazovIngrediencie"));
        TableColumn<IngredienciaMnozstvo, Double> colMnoz = new TableColumn<>("Množstvo");
        colMnoz.setCellValueFactory(new PropertyValueFactory<>("mnozstvo"));
        TableColumn<IngredienciaMnozstvo, String> colJedn = new TableColumn<>("Jednotka");
        colJedn.setCellValueFactory(new PropertyValueFactory<>("jednotka"));
        TableColumn<IngredienciaMnozstvo, Double> colKcalDocasne = new TableColumn<>("Kalórie (kcal)");
        colKcalDocasne.setCellValueFactory(new PropertyValueFactory<>("kalorie"));
        colKcalDocasne.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1f", item));
            }
        });
        this.tableDocasne.getColumns().addAll(colIng, colMnoz, colJedn, colKcalDocasne);
        this.tableDocasne.setPrefHeight(250.0);
        this.tableDocasne.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ingBox.getChildren().addAll(lblIng, this.cbIngrediencia, mnozstvoBox, btnPridatIng, this.tableDocasne);
        box.getChildren().addAll(topSection, separator, ingBox);
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
        this.tfSearch.setPromptText("Hľadať...");
        this.tfSearch.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());

        this.cbFilterKategoria = new ComboBox<>();
        List<Kategoria> kategorieZDB = this.db.nacitajVsetkyKategorie();
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

        // STĹPCE TABUĽKY RECEPTOV
        TableColumn<Recept, String> colNazov = new TableColumn<>("Názov");
        colNazov.setCellValueFactory(new PropertyValueFactory<>("nazov"));

        TableColumn<Recept, Integer> colCas = new TableColumn<>("Čas");
        colCas.setCellValueFactory(new PropertyValueFactory<>("casPripravy"));

        TableColumn<Recept, Integer> colPorcie = new TableColumn<>("Porcie");
        colPorcie.setCellValueFactory(new PropertyValueFactory<>("pocetPorcii"));

        TableColumn<IngredienciaMnozstvo, Double> colIngKcal = new TableColumn<>("Kcal");
        colIngKcal.setCellValueFactory(new PropertyValueFactory<>("kalorie"));

        TableColumn<Recept, String> colKategoria = new TableColumn<>("Kategória");
        colKategoria.setCellValueFactory(new PropertyValueFactory<>("kategoriaNazov"));

        // NOVO PRIDANÝ STĹPEC AUTOR
        TableColumn<Recept, String> colAutor = new TableColumn<>("Autor");
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));

        // Pridanie stĺpcov do tabuľky (colAutor pridaný na koniec)
        this.tableRecepty.getColumns().addAll(colNazov, colCas, colPorcie, colKategoria, colAutor);

        this.tableRecepty.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.tableRecepty.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                zobrazRecept(newSelection);
            }
        });

        nastylujTabulku(this.tableRecepty);
        VBox.setVgrow(this.tableRecepty, Priority.ALWAYS);

        // TABUĽKA INGREDIENCIÍ
        this.tableIngrediencie = new TableView<>();
        this.tableIngrediencie.getStyleClass().add("table-view");
        this.tableIngrediencie.setItems(this.ingrediencieReceptu);

        TableColumn<IngredienciaMnozstvo, String> colIngNazov = new TableColumn<>("Ingrediencia");
        colIngNazov.setCellValueFactory(new PropertyValueFactory<>("nazovIngrediencie"));

        TableColumn<IngredienciaMnozstvo, Double> colIngMnozstvo = new TableColumn<>("Množstvo");
        colIngMnozstvo.setCellValueFactory(new PropertyValueFactory<>("mnozstvo"));

        TableColumn<IngredienciaMnozstvo, String> colIngJednotka = new TableColumn<>("Jednotka");
        colIngJednotka.setCellValueFactory(new PropertyValueFactory<>("jednotka"));

        this.tableIngrediencie.getColumns().addAll(colIngNazov, colIngMnozstvo, colIngJednotka);
        this.tableIngrediencie.setPrefHeight(200.0);
        this.tableIngrediencie.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        nastylujTabulku(this.tableIngrediencie);

        Button btnOdstranIng = createModernButton("Odstrániť ingredienciu", "#e74c3c");
        btnOdstranIng.setOnAction(e -> odstranIngredienciuZReceptu());

        Label lblIngReceptu = new Label("Ingrediencie receptu:");
        lblIngReceptu.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13.0));

        VBox ingReceptBox = new VBox(8.0);
        ingReceptBox.getChildren().addAll(lblIngReceptu, this.tableIngrediencie, btnOdstranIng);

        box.getChildren().addAll(title, searchBox, this.tableRecepty, ingReceptBox);

        return box;
    }

    private void updateFilter() {
        this.filteredRecepty.setPredicate(recept -> {
            // 1. Filter podľa kategórie
            Kategoria selectedKategoria = this.cbFilterKategoria.getValue();

            // Úprava: Ak je vybraté null ALEBO ID -1, tak kategóriu nefiltrujeme (match je vždy true)
            boolean matchesKategoria = (selectedKategoria == null ||
                    selectedKategoria.getId() == -1 ||
                    recept.getKategoriaId() == selectedKategoria.getId());

            if (!matchesKategoria) {
                return false;
            }

            // 2. Filter podľa textu (vyhľadávanie)
            String searchText = this.tfSearch.getText().toLowerCase().trim();
            if (searchText.isEmpty()) {
                return true;
            }

            // Kontrola názvu
            if (recept.getNazov().toLowerCase().contains(searchText)) {
                return true;
            }
            // Kontrola času
            if (String.valueOf(recept.getCasPripravy()).contains(searchText)) {
                return true;
            }
            // Kontrola porcií
            if (String.valueOf(recept.getPocetPorcii()).contains(searchText)) {
                return true;
            }

            // Kontrola ingrediencií
            // POZOR: Toto volanie db.nacitajIngredienciePreRecept vnútri filtra
            // môže spomaliť UI, ak máte veľa receptov.
            for (IngredienciaMnozstvo ing : this.db.nacitajIngredienciePreRecept(recept.getReceptId())) {
                if (ing.getNazovIngrediencie().toLowerCase().contains(searchText)) {
                    return true;
                }
            }

            return false;
        });
    }

    private void zobrazRecept(Recept r) {
        this.lblVybrany.setText("Vybraný recept: " + r.getNazov() + " (autor: " + r.getAutor() + ")");
        this.lblId.setText(String.valueOf(r.getReceptId()));
        this.tfNazov.setText(r.getNazov());
        this.tfCas.setText(String.valueOf(r.getCasPripravy()));
        this.tfPorcie.setText(String.valueOf(r.getPocetPorcii()));
        this.taPostup.setText(r.getPostup());
        this.cbKategoria.getSelectionModel().select(
                this.cbKategoria.getItems().stream()
                        .filter(k -> k.getId() == r.getKategoriaId())
                        .findFirst()
                        .orElse(null)
        );
        this.obrazokCesta = r.getObrazokCesta();
        nastavObrazokZUrl(this.obrazokCesta);
        this.ingrediencieReceptu.clear();
        this.ingrediencieReceptu.addAll(this.db.nacitajIngredienciePreRecept(r.getReceptId()));
        this.docasneIngrediencie.clear();
        this.docasneIngrediencie.addAll(this.ingrediencieReceptu);
    }

    private void pridajDocasnuIngredienciu() {
        String nazov = this.cbIngrediencia.getEditor().getText().trim();
        String mnoz = this.tfMnozstvo.getText().trim();
        String jedn = this.tfJednotka.getText().trim();
        if (nazov.isEmpty() || mnoz.isEmpty()) {
            alert("Chyba", "Vyplňte ingredienciu a množstvo");
            return;
        }

        try {
            double mnozstvo = Double.parseDouble(mnoz);

            // Vytvoríme inštanciu triedy Ingrediencia, kde máš tú metódu
            Ingrediencia ingServis = new Ingrediencia(0, "");

            // Spustíme sieťovú požiadavku na pozadí
            new Thread(() -> {
                try {
                    // Voláme tvoju metódu z triedy Ingrediencia
                    double vypocitaneKcal = ingServis.getCaloriesFromInternet(nazov, mnozstvo);

                    // Návrat do hlavného grafického vlákna JavaFX
                    Platform.runLater(() -> {
                        // Pridáme do tabuľky objekt aj s vypočítanými kalóriami
                        this.docasneIngrediencie.add(new IngredienciaMnozstvo(nazov, mnozstvo, jedn, vypocitaneKcal));

                        this.cbIngrediencia.getEditor().clear();
                        this.tfMnozstvo.clear();
                        this.tfJednotka.clear();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> alert("Chyba", "Nepodarilo sa získať kalórie."));
                }
            }).start();

        } catch (NumberFormatException ex) {
            alert("Chyba", "Množstvo musí byť číslo");
        }
    }

    private void ulozRecept(boolean novyRecept) {
        if (this.tfNazov.getText().trim().isEmpty()) {
            alert("Chyba", "Zadajte názov receptu!");
            return;
        }
        try {
            int userId = this.db.getUserIdByUsername(loggedInUser);
            int katId = (this.cbKategoria.getValue() != null) ? this.cbKategoria.getValue().getId() : 0;
            String finalObrazokCesta = this.obrazokCesta;
            int receptId;
            if (novyRecept || this.lblId.getText().isEmpty()) {
                receptId = this.db.pridajRecept(
                        this.tfNazov.getText(),
                        this.taPostup.getText(),
                        Integer.parseInt(this.tfCas.getText()),
                        Integer.parseInt(this.tfPorcie.getText()),
                        katId,
                        userId,
                        finalObrazokCesta
                );
                this.lblId.setText(String.valueOf(receptId));
            } else {
                receptId = Integer.parseInt(this.lblId.getText());
                this.db.upravRecept(
                        receptId,
                        this.tfNazov.getText(),
                        this.taPostup.getText(),
                        Integer.parseInt(this.tfCas.getText()),
                        Integer.parseInt(this.tfPorcie.getText()),
                        finalObrazokCesta,
                        katId
                );
            }
            this.db.vymazIngrediencieReceptu(receptId);
            for (IngredienciaMnozstvo ing : this.docasneIngrediencie) {
                this.db.pridajIngredienciuDoReceptu(receptId, ing.getNazovIngrediencie(), ing.getMnozstvo(), ing.getJednotka(), ing.getKalorie());
            }
            nacitajRecepty();
            if (!novyRecept) {
                Recept r = this.recepty.stream().filter(rec -> rec.getReceptId() == receptId).findFirst().orElse(null);
                if (r != null) {
                    r.setObrazokCesta(finalObrazokCesta);
                    this.tableRecepty.getSelectionModel().select(r);
                    zobrazRecept(r);
                }
            }
            alert("Úspech", novyRecept ? "Recept pridaný" : "Recept upravený");
        } catch (NumberFormatException ex) {
            alert("Chyba", "Čas a porcie musia byť čísla");
        } catch (Exception ex) {
            alert("Chyba", "Nepodarilo sa uložiť recept: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void zmazRecept() {
        if (this.lblId.getText().isEmpty()) {
            return;
        }
        int id = Integer.parseInt(this.lblId.getText());
        this.db.zmazRecept(id);
        nacitajRecepty();
        vycistiFormular();
        alert("Úspech", "Recept zmazaný");
    }

    private void odstranIngredienciuZReceptu() {
        IngredienciaMnozstvo vybrana = this.tableIngrediencie.getSelectionModel().getSelectedItem();
        if (vybrana != null && !this.lblId.getText().isEmpty()) {
            int receptId = Integer.parseInt(this.lblId.getText());
            this.db.odstranJednuIngredienciu(receptId, vybrana.getNazovIngrediencie(), vybrana.getMnozstvo(), vybrana.getJednotka());
            this.ingrediencieReceptu.remove(vybrana);
            this.docasneIngrediencie.remove(vybrana);
        }
    }

    private void vycistiFormular() {
        this.lblId.setText("");
        this.lblVybrany.setText("Vybraný recept: -");
        this.tfNazov.clear();
        this.tfCas.clear();
        this.tfPorcie.clear();
        this.taPostup.clear();
        this.cbKategoria.getSelectionModel().clearSelection();
        this.imgView.setImage(null);
        this.obrazokCesta = "";
        this.docasneIngrediencie.clear();
        this.tableRecepty.getSelectionModel().clearSelection();
    }

    private void vyberObrazok(Stage stage) {
        if (this.lblId.getText().isEmpty()) {
            alert("Pozor", "Najprv uložte recept (Pridať nový recept), a potom pridajte obrázok");
            return;
        }
        int receptId = Integer.parseInt(this.lblId.getText());
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Obrázky", ".png", ".jpg", ".jpeg", ".webp", "*.gif"));
        File file = fc.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        this.imgView.setImage(new Image(file.toURI().toString()));
        (new Thread(() -> {
            String fileExtension = getFileExtension(file).toLowerCase();
            String fileName = "recept_" + receptId + fileExtension;
            String remotePath = receptId + "/" + fileName;
            String uploadedUrl = uploadViaFtp(file, remotePath);
            if (uploadedUrl != null) {
                this.db.upravReceptCestaObrazku(receptId, uploadedUrl);
                this.obrazokCesta = uploadedUrl;
                Platform.runLater(() -> alert("Úspech", "Obrázok nahraný!"));
            } else {
                Platform.runLater(() -> alert("Chyba", "Nepodarilo sa nahrať obrázok"));
            }
        })).start();
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
