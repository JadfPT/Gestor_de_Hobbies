package ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.Hobby;
import models.Sessao;
import models.User;
import services.AppState;
import services.ExportService;
import ui.App;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class SettingsController {

    @FXML
    private Label lblUsername;

    @FXML
    private PasswordField pfCurrent;
    @FXML
    private PasswordField pfNew;
    @FXML
    private PasswordField pfConfirm;
    @FXML
    private Label lblPassMsg;

    @FXML
    private TextField txtDeleteConfirm;
    @FXML
    private Button btnDeleteAccount;
    @FXML
    private Label lblDeleteMsg;

    @FXML
    private TextField txtResetConfirm;
    @FXML
    private Button btnResetData;
    @FXML
    private Label lblResetMsg;

    @FXML
    private Button btnExportData;

    @FXML
    private CheckBox chkDarkMode;
    @FXML
    private Label lblThemeMsg;

    @FXML
    private Label lblGlobalMsg;

    @FXML
    private ComboBox<String> cmbTimeFormat;
    @FXML
    private ComboBox<String> cmbDateFormat;

    @FXML
    private ColorPicker colorChart;

    // =========================
    // Inicialização
    // =========================
    @FXML
    public void initialize() {
        User u = AppState.getInstance().getCurrentUser();
        lblUsername.setText(u != null ? u.getUsername() : "(sem sessão)");

        txtDeleteConfirm.textProperty().addListener((obs, o, n) -> {
            boolean ok = "APAGAR".equalsIgnoreCase(n.trim());
            btnDeleteAccount.setDisable(!ok);
            lblDeleteMsg.setText("");
        });

        txtResetConfirm.textProperty().addListener((obs, o, n) -> {
            boolean ok = "LIMPAR".equalsIgnoreCase(n.trim());
            btnResetData.setDisable(!ok);
            lblResetMsg.setText("");
        });

        Platform.runLater(() -> chkDarkMode.setSelected(App.isDarkModeEnabled()));

        if (cmbTimeFormat != null) {
            cmbTimeFormat.getItems().setAll("24h", "12h");
            cmbTimeFormat.setValue(App.isUse24HourTime() ? "24h" : "12h");
            cmbTimeFormat.setOnAction(e -> onChangeTimeFormat());
        }

        if (cmbDateFormat != null) {
            cmbDateFormat.getItems().setAll("yyyy-MM-dd", "MM-dd-yyyy", "dd-MM-yyyy");
            cmbDateFormat.setValue(App.getDateFormatPattern());
            cmbDateFormat.setOnAction(e -> onChangeDateFormat());
        }

        if (colorChart != null) {
            colorChart.setOnAction(e -> onChangeChartColor());
            try {
                colorChart.setValue(javafx.scene.paint.Color.web(App.getChartColor()));
            } catch (Exception ignored) {
            }
        }
    }

    // =========================
    // Password
    // =========================
    @FXML
    private void onChangePassword() {
        lblPassMsg.setText("");

        User u = AppState.getInstance().getCurrentUser();
        if (u == null) {
            lblPassMsg.setText("Sem utilizador autenticado.");
            return;
        }

        String current = pfCurrent.getText();
        String n1 = pfNew.getText();
        String n2 = pfConfirm.getText();

        if (current.isBlank() || n1.isBlank() || n2.isBlank()) {
            lblPassMsg.setText("Preenche todos os campos.");
            return;
        }

        if (!u.getPassword().equals(current)) {
            lblPassMsg.setText("Password atual incorreta.");
            return;
        }

        if (!n1.equals(n2)) {
            lblPassMsg.setText("A confirmação não coincide.");
            return;
        }

        if (n1.length() < 4) {
            lblPassMsg.setText("A nova password deve ter pelo menos 4 caracteres.");
            return;
        }

        u.setPassword(n1);
        AppState.getInstance().guardar();

        pfCurrent.clear();
        pfNew.clear();
        pfConfirm.clear();

        lblPassMsg.setText("Password alterada com sucesso.");
    }

    // =========================
    // Tema e preferências
    // =========================
    @FXML
    private void onToggleTheme() {
        lblThemeMsg.setText("");

        Scene scene = getSceneSafe();
        if (scene == null) {
            lblThemeMsg.setText("Scene ainda não está pronta.");
            return;
        }

        boolean enable = chkDarkMode.isSelected();
        App.setDarkModeEnabled(enable);

        var u = AppState.getInstance().getCurrentUser();
        if (u != null) {
            u.setPrefDarkMode(enable);
            AppState.getInstance().guardar();
        }

        data.PreferencesStore.saveAppPrefs();
    }

    @FXML
    private void onChangeTimeFormat() {
        String sel = cmbTimeFormat.getValue();
        App.setUse24HourTime("24h".equals(sel));

        var u = AppState.getInstance().getCurrentUser();
        if (u != null) {
            u.setPrefUse24HourTime(App.isUse24HourTime());
            AppState.getInstance().guardar();
        }

        data.PreferencesStore.saveAppPrefs();
    }

    @FXML
    private void onChangeDateFormat() {
        String fmt = cmbDateFormat.getValue();
        App.setDateFormatPattern(fmt);

        var u = AppState.getInstance().getCurrentUser();
        if (u != null) {
            u.setPrefDateFormat(fmt);
            AppState.getInstance().guardar();
        }

        data.PreferencesStore.saveAppPrefs();
    }

    @FXML
    private void onChangeChartColor() {
        var c = colorChart.getValue();
        if (c != null) {
            String hex = String.format("#%02X%02X%02X",
                    (int) (c.getRed() * 255),
                    (int) (c.getGreen() * 255),
                    (int) (c.getBlue() * 255));

            App.setChartColor(hex);

            var u = AppState.getInstance().getCurrentUser();
            if (u != null) {
                u.setPrefChartColor(hex);
                AppState.getInstance().guardar();
            }

            data.PreferencesStore.saveAppPrefs();
        }
    }

    // =========================
    // Logout / Reset
    // =========================
    @FXML
    private void onLogout() {
        Optional<ButtonType> res = confirm(
                "Terminar sessão",
                "Queres terminar a sessão?",
                "Vais voltar ao ecrã de login."
        );

        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return;
        }

        try {
            App.setRoot("views/LoginView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            lblGlobalMsg.setText("Erro ao terminar sessão.");
        }
    }

    @FXML
    private void onDeleteAccount() {
        lblDeleteMsg.setText("Ainda não implementado.");
    }

    @FXML
    private void onResetData() {
        lblResetMsg.setText("");

        User u = AppState.getInstance().getCurrentUser();
        if (u == null) {
            lblResetMsg.setText("Sem utilizador autenticado.");
            return;
        }

        Optional<ButtonType> res = confirm(
                "Limpar dados",
                "Isto vai apagar TODOS os hobbies e sessões.",
                "Esta ação é irreversível."
        );
        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return;
        }

        int h = u.getHobbies().size();
        int s = u.getSessoes().size();

        for (Hobby hb : new ArrayList<>(u.getHobbies())) {
            u.removerHobby(hb);
        }
        for (Sessao se : new ArrayList<>(u.getSessoes())) {
            u.removerSessao(se);
        }

        AppState.getInstance().guardar();

        txtResetConfirm.clear();
        btnResetData.setDisable(true);

        lblResetMsg.setText("OK: removidos " + h + " hobbies e " + s + " sessões.");
    }

    // =========================
    // EXPORTAÇÃO (DIALOG BONITO)
    // =========================
    @FXML
    private void onExportData() {
        lblResetMsg.setText("");

        User u = AppState.getInstance().getCurrentUser();
        if (u == null) {
            lblResetMsg.setText("Sem utilizador autenticado.");
            return;
        }

        Optional<String> formatoOpt = escolherFormatoExportacao();
        if (formatoOpt.isEmpty()) {
            return;
        }

        String formato = formatoOpt.get(); // TXT | PDF

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar exportação");

        String safeUser = (u.getUsername() == null || u.getUsername().isBlank())
                ? "user"
                : u.getUsername().trim();

        if ("PDF".equals(formato)) {
            fc.setInitialFileName("export_" + safeUser + ".pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        } else {
            fc.setInitialFileName("export_" + safeUser + ".txt");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto (*.txt)", "*.txt"));
        }

        Scene scene = getSceneSafe();
        if (scene == null || scene.getWindow() == null) {
            lblResetMsg.setText("Não foi possível abrir o selector de ficheiros.");
            return;
        }

        File destino = fc.showSaveDialog(scene.getWindow());
        if (destino == null) {
            return;
        }

        try {
            if ("PDF".equals(formato)) {
                ExportService.exportUserDataPdf(u, destino);
            } else {
                ExportService.exportUserDataTxt(u, destino);
            }
            lblResetMsg.setText("Export concluído: " + destino.getName());
        } catch (Exception e) {
            e.printStackTrace();
            lblResetMsg.setText("Falha ao exportar dados.");
        }
    }

    // ===== Dialog custom (herda CSS da Scene) =====
    private Optional<String> escolherFormatoExportacao() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Exportar dados");

        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnOk = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCancelar, btnOk);

        // --- UI (card) ---
        Label title = new Label("Escolhe o formato de exportação");
        title.getStyleClass().add("export-title");

        Label sub = new Label("Exporta os teus dados em TXT (texto) ou PDF (relatório).");
        sub.getStyleClass().add("export-sub");

        ComboBox<String> cmb = new ComboBox<>();
        cmb.getItems().addAll("TXT", "PDF");
        cmb.getSelectionModel().selectFirst();

        // Apply inline styles to title and combo so light mode matches dark appearance
        boolean darkMode = App.isDarkModeEnabled();
        if (darkMode) {
            title.setStyle("-fx-font-size:22px; -fx-font-weight:800; -fx-text-fill:#e5e7eb;");
            cmb.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-border-color: rgba(255,255,255,0.12); -fx-text-fill: #e5e7eb; -fx-background-radius:12; -fx-padding:8 12; -fx-pref-height:36;");
        } else {
            title.setStyle("-fx-font-size:22px; -fx-font-weight:800; -fx-text-fill:#0f172a;");
            cmb.setStyle("-fx-background-color: rgba(15,23,42,0.04); -fx-border-color: rgba(15,23,42,0.06); -fx-text-fill: #0f172a; -fx-background-radius:12; -fx-padding:8 12; -fx-pref-height:36;");
        }

        Label lbl = new Label("Formato:");
        HBox row = new HBox(12, lbl, cmb);
        row.setStyle("-fx-alignment: center-left;");

        VBox card = new VBox(14, title, sub, row);
        card.getStyleClass().add("export-card");

        dialog.getDialogPane().setContent(card);

        // --- CSS: herdar da Scene principal ---
        Scene scene = getSceneSafe();
        if (scene != null) {
            dialog.getDialogPane().getStylesheets().addAll(scene.getStylesheets());
        }

        // classes para o CSS funcionar (light e dark)
        dialog.getDialogPane().getStyleClass().add("export-dialog");
        if (App.isDarkModeEnabled()) {
            dialog.getDialogPane().getStyleClass().add("dark");
        }

        // Force a slightly larger dialog width and keep title/subtitle on a single line
        dialog.getDialogPane().setPrefWidth(400);
        title.setWrapText(false);
        sub.setWrapText(false);

        // desativar OK se não houver escolha (por segurança)
        Node okBtn = dialog.getDialogPane().lookupButton(btnOk);
        okBtn.disableProperty().bind(cmb.valueProperty().isNull());
        // lookup the cancel button and add explicit style classes so CSS applies reliably
        Node cancelBtn = dialog.getDialogPane().lookupButton(btnCancelar);
            if (okBtn != null) {
                okBtn.getStyleClass().add("export-primary-button");
                boolean dark = App.isDarkModeEnabled();
                    String primaryStyle = dark
                        ? "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #60a5fa, #3b82f6); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 18;"
                        : "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3b82f6, #2563eb); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 18;";
                okBtn.setStyle(primaryStyle);
            }
            if (cancelBtn != null) {
                cancelBtn.getStyleClass().add("export-cancel-button");
                boolean dark = App.isDarkModeEnabled();
                String cancelStyle = dark
                        ? "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #e5e7eb; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width:1; -fx-background-radius:12; -fx-padding:8 16;"
                        : "-fx-background-color: rgba(15,23,42,0.04); -fx-text-fill: #0f172a; -fx-border-color: rgba(15,23,42,0.06); -fx-border-width:1; -fx-background-radius:12; -fx-padding:8 16;";
                cancelBtn.setStyle(cancelStyle);
            }

        dialog.setResultConverter(bt -> bt == btnOk ? cmb.getValue() : null);
        return dialog.showAndWait();
    }

    // =========================
    // Helpers
    // =========================
    private Scene getSceneSafe() {
        return lblUsername != null ? lblUsername.getScene() : null;
    }

    private Optional<ButtonType> confirm(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

        DialogPane pane = a.getDialogPane();
        if (pane != null && App.isDarkModeEnabled()) {
            pane.getStyleClass().add("dark");
        }
        return a.showAndWait();
    }
}
