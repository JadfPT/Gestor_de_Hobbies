package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.AppState;
import ui.App;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirm;

    @FXML
    private Label lblMensagem;

    @FXML
    private void onCreateAccount() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();
        String confirm = txtConfirm.getText();

        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            lblMensagem.setText("Preenche todos os campos.");
            return;
        }

        if (!pass.equals(confirm)) {
            lblMensagem.setText("As passwords não coincidem.");
            return;
        }

        boolean ok = AppState.getInstance().registar(user, pass);
        if (!ok) {
            lblMensagem.setText("Já existe um utilizador com esse nome.");
            return;
        }

        lblMensagem.setText("Conta criada com sucesso! A entrar...");

        try {
            App.setRoot("views/MainView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            lblMensagem.setText("Conta criada, mas falhou ao abrir a aplicação.");
        }
    }

    @FXML
    private void onBackToLogin() {
        try {
            App.setRoot("views/LoginView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            lblMensagem.setText("Erro ao voltar ao login.");
        }
    }
}
