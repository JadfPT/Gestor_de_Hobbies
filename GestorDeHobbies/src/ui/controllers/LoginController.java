package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import service.AppState;
import ui.App;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensagem;

    @FXML
    private void onLogin() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMensagem.setText("Preenche utilizador e password.");
            return;
        }

        boolean ok = AppState.getInstance().login(user, pass);
        if (ok) {
            try {
                App.setRoot("views/MainView.fxml");
            } catch (IOException e) {
                e.printStackTrace();
                lblMensagem.setText("Erro ao abrir a aplicação.");
            }
        } else {
            lblMensagem.setText("Dados de login incorretos.");
        }
    }

    @FXML
    private void onOpenRegister() {
        try {
            App.setRoot("views/RegisterView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            lblMensagem.setText("Erro ao abrir a página de registo.");
        }
    }
}
