/*
 * Propósito geral: gerir a navegação e exibição do conteúdo principal da aplicação,
 * permitindo trocar entre vistas de hobbies, sessões, estatísticas e configurações.
 * Observações: usa StackPane para trocar dinamicamente de vistas; ScrollPane garante
 * scroll apropriado; mensagens de erro no status bar em caso de carregamento falho.
 */
package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import ui.App;

import java.io.IOException;

public class MainController {

    @FXML
    private Label lblStatus;

    @FXML
    private StackPane contentArea;

    @FXML
    private ScrollPane contentScroll;

    // Inicialização: mostra mensagem de boas-vindas
    @FXML
    public void initialize() {
        lblStatus.setText("Bem-vindo ao Gestor de Hobbies!");
    }

    // Carrega a vista de hobbies
    @FXML
    public void onHobbiesClicked() {
        loadView("HobbiesView.fxml");
    }

    // Carrega a vista de sessões
    @FXML
    public void onSessionsClicked() {
        loadView("SessionsView.fxml");
    }

    // Carrega a vista de estatísticas
    @FXML
    public void onStatsClicked() {
        loadView("StatsView.fxml");
    }

    // Carrega a vista de configurações
    @FXML
    public void onSettingsClicked() {
        loadView("SettingsView.fxml");
    }

    // Carrega dinamicamente um ficheiro FXML e substitui o conteúdo
    private void loadView(String fxmlName) {
        try {
            // Carrega o ficheiro FXML
            Node view = FXMLLoader.load(App.class.getResource("views/" + fxmlName));
            contentArea.getChildren().setAll(view);

            // Configura comportamento do scroll
            if (contentScroll != null) {
                contentScroll.setFitToWidth(true);
                contentScroll.setFitToHeight(false);
                contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                // Reseta scroll para o topo
                contentScroll.setVvalue(0);
                contentScroll.setHvalue(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (lblStatus != null) {
                // Mostra erro no status bar
                lblStatus.setText("Erro ao carregar a vista: " + fxmlName);
            }
        }
    }
}