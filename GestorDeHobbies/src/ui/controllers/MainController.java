package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

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
        loadView("/ui/views/HobbiesView.fxml");
    }

    @FXML
    private void onSessoesClicked() {
        loadView("/ui/views/SessionsView.fxml");
    }

    private void loadView(String resource) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(resource));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            lblStatus.setText("Erro ao carregar a vista.");
        }
    }
}
