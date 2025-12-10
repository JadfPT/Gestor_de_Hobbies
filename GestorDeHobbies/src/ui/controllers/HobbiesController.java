// src/ui/controllers/HobbiesController.java
package ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Categoria;
import models.Hobby;
import service.AppState;

import java.io.IOException;

public class HobbiesController {

    @FXML
    private TableView<Hobby> tblHobbies;

    @FXML
    private TableColumn<Hobby, String> colNome;

    @FXML
    private TableColumn<Hobby, Categoria> colCategoria;

    @FXML
    private TableColumn<Hobby, String> colDescricao;

    @FXML
    private TextField txtPesquisar;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnApagar;

    private final ObservableList<Hobby> dados = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            dados.setAll(user.getHobbies());
        }

        tblHobbies.setItems(dados);

        // desativa botões se nada estiver selecionado
        btnEditar.setDisable(true);
        btnApagar.setDisable(true);

        tblHobbies.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean has = newSel != null;
            btnEditar.setDisable(!has);
            btnApagar.setDisable(!has);
        });
    }

    @FXML
    private void onNovoHobby() {
        abrirDialogHobby(null);
    }

    @FXML
    private void onEditarHobby() {
        Hobby selecionado = tblHobbies.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            abrirDialogHobby(selecionado);
        }
    }

    @FXML
    private void onApagarHobby() {
        Hobby selecionado = tblHobbies.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Apagar hobby");
        alert.setHeaderText("Tens a certeza que queres apagar este hobby?");
        alert.setContentText("Esta ação não pode ser desfeita.");
        alert.initOwner(tblHobbies.getScene().getWindow());

        var res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            dados.remove(selecionado);

            var user = AppState.getInstance().getCurrentUser();
            if (user != null) {
                user.removerHobby(selecionado);
                AppState.getInstance().guardar();
            }
        }
    }

    private void abrirDialogHobby(Hobby aEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/AddHobbyView.fxml"));
            Parent root = loader.load();

            AddHobbyController controller = loader.getController();
            controller.setHobbiesController(this);
            if (aEditar != null) {
                controller.setHobbyExistente(aEditar);
            }

            Stage dialog = new Stage();
            dialog.initOwner(tblHobbies.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(aEditar == null ? "Novo hobby" : "Editar hobby");
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** chamado quando é criado um hobby novo */
    public void adicionarHobby(Hobby hobby) {
        dados.add(hobby);

        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            user.adicionarHobby(hobby);
            AppState.getInstance().guardar();
        }
    }

    /** chamado quando um hobby existente foi editado no diálogo */
    public void hobbyAtualizado() {
        tblHobbies.refresh();
        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            AppState.getInstance().guardar();
        }
    }
}
