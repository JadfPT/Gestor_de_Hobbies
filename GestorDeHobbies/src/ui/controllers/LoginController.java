/*
 * Propósito geral: controlar o ecrã de autenticação, validando credenciais e
 * permitindo navegação para registo ou página principal após login bem-sucedido.
 * Observações: obtém utilizador/password do formulário; comunica com AppState para
 * autenticar; mostra mensagens de erro inline no ecrã.
 */
package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.AppState;
import ui.App;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensagem;

    // Valida e tenta autenticar o utilizador
    @FXML
    private void onLogin() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMensagem.setText("Preenche utilizador e password.");
            return;
        }

        // Tenta login através do AppState
        boolean ok = AppState.getInstance().login(user, pass);
        if (ok) {
            try {
                // Sucesso: navega para MainView
                App.setRoot("views/MainView.fxml");
            } catch (IOException e) {
                e.printStackTrace();
                lblMensagem.setText("Erro ao abrir a aplicação.");
            }
        } else {
            // Credenciais inválidas
            lblMensagem.setText("Dados de login incorretos.");
        }
    }

    // Navega para a página de registo
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
