/*
 * Propósito geral: controlar o ecrã de registo de novos utilizadores, validando
 * dados e criando a conta através do AppState.
 * Observações: valida campos vazios, confirma que as passwords coincidem, evita
 * usernames duplicados; mostra mensagens inline no ecrã.
 */
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

    // Valida dados e cria uma nova conta
    @FXML
    private void onCreateAccount() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();
        String confirm = txtConfirm.getText();

        // Valida campos obrigatórios
        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            lblMensagem.setText("Preenche todos os campos.");
            return;
        }

        // Valida que as passwords coincidem
        if (!pass.equals(confirm)) {
            lblMensagem.setText("As passwords não coincidem.");
            return;
        }

        // Tenta criar a conta através do AppState
        boolean ok = AppState.getInstance().registar(user, pass);
        if (!ok) {
            // Utilizador já existe
            lblMensagem.setText("Já existe um utilizador com esse nome.");
            return;
        }

        lblMensagem.setText("Conta criada com sucesso! A entrar...");

        try {
            // Navega para a aplicação principal
            App.setRoot("views/MainView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            lblMensagem.setText("Conta criada, mas falhou ao abrir a aplicação.");
        }
    }

    // Volta ao ecrã de login
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
