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

    @FXML
    public void initialize() {
        lblStatus.setText("Bem-vindo ao Gestor de Hobbies!");
    }

    @FXML
    public void onHobbiesClicked() {
        loadView("HobbiesView.fxml");
    }

    @FXML
    public void onSessionsClicked() {
        loadView("SessionsView.fxml");
    }

    @FXML
    public void onStatsClicked() {
        loadView("StatsView.fxml");
    }

    @FXML
    public void onSettingsClicked() {
        loadView("SettingsView.fxml");
    }

    private void loadView(String fxmlName) {
        try {
            Node view = FXMLLoader.load(App.class.getResource("views/" + fxmlName));
            contentArea.getChildren().setAll(view);

            if (contentScroll != null) {
                contentScroll.setFitToWidth(true);
                contentScroll.setFitToHeight(false);
                contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                contentScroll.setVvalue(0);
                contentScroll.setHvalue(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (lblStatus != null) {
                lblStatus.setText("Erro ao carregar a vista: " + fxmlName);
            }
        }
    }
}