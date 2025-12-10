package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import ui.App;

import java.io.IOException;

public class MainController {

    @FXML
    private Label lblStatus;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        lblStatus.setText("Bem-vindo ao Gestor de Hobbies!");
    }

    @FXML
    private void onHobbiesClicked() {
        loadView("HobbiesView.fxml");
    }
    
    @FXML
private void onSessionsClicked() {
    loadView("SessionsView.fxml");
}

    @FXML
    private void onStatsClicked() {
        loadView("StatsView.fxml");
    }

    private void loadView(String fxmlName) {
        try {
            Node view = FXMLLoader.load(App.class.getResource("views/" + fxmlName));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            if (lblStatus != null) {
                lblStatus.setText("Erro ao carregar a vista: " + fxmlName);
            }
        }
    }
}
