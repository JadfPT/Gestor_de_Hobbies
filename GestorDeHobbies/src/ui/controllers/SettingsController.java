package ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import models.Hobby;
import models.Sessao;
import models.User;
import services.AppState;
import ui.App;

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
    private CheckBox chkDarkMode;

    @FXML
    private Label lblThemeMsg;

    @FXML
    private Label lblGlobalMsg;

    @FXML
    public void initialize() {
        User u = AppState.getInstance().getCurrentUser();
        lblUsername.setText(u != null ? u.getUsername() : "(sem sessão)");

        txtDeleteConfirm.textProperty().addListener((obs, oldV, newV) -> {
            boolean ok = "APAGAR".equalsIgnoreCase(newV.trim());
            btnDeleteAccount.setDisable(!ok);
            lblDeleteMsg.setText("");
        });

        txtResetConfirm.textProperty().addListener((obs, oldV, newV) -> {
            boolean ok = "LIMPAR".equalsIgnoreCase(newV.trim());
            btnResetData.setDisable(!ok);
            lblResetMsg.setText("");
        });

        Platform.runLater(() -> chkDarkMode.setSelected(App.isDarkModeEnabled()));
    }

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

        if (current == null) current = "";
        if (n1 == null) n1 = "";
        if (n2 == null) n2 = "";

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

        lblThemeMsg.setText(enable ? "Modo escuro ativado." : "Modo escuro desativado.");
    }

    @FXML
    private void onLogout() {
        Optional<ButtonType> res = confirm(
                "Terminar sessão",
                "Queres terminar a sessão?",
                "Vais voltar ao ecrã de login."
        );

        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        try {
            App.setRoot("views/LoginView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            lblGlobalMsg.setText("Erro ao terminar sessão.");
        }
    }

    @FXML
    private void onDeleteAccount() {
        lblDeleteMsg.setText("Ainda não implementado: falta remover o utilizador do AppData de forma segura.");
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
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        int hobbiesAntes = u.getHobbies().size();
        int sessoesAntes = u.getSessoes().size();

        for (Hobby h : new ArrayList<>(u.getHobbies())) {
            u.removerHobby(h);
        }
        for (Sessao s : new ArrayList<>(u.getSessoes())) {
            u.removerSessao(s);
        }

        AppState.getInstance().guardar();

        txtResetConfirm.clear();
        btnResetData.setDisable(true);

        lblResetMsg.setText("OK: removidos " + hobbiesAntes + " hobbies e " + sessoesAntes + " sessões.");
    }

    private Scene getSceneSafe() {
        if (lblUsername == null) return null;
        return lblUsername.getScene();
    }

    private Optional<ButtonType> confirm(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

        DialogPane pane = a.getDialogPane();
        if (pane != null) {
            if (App.isDarkModeEnabled() && !pane.getStyleClass().contains("dark")) {
                pane.getStyleClass().add("dark");
            }
        }

        return a.showAndWait();
    }
}