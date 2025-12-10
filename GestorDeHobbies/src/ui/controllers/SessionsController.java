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
import models.Sessao;
import service.AppState;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class SessionsController {

    @FXML
    private TableView<Sessao> tblSessoes;

    @FXML
    private TableColumn<Sessao, String> colHobby;

    @FXML
    private TableColumn<Sessao, LocalDate> colData;

    @FXML
    private TableColumn<Sessao, LocalTime> colHora;

    @FXML
    private TableColumn<Sessao, Integer> colDuracao;

    @FXML
    private TableColumn<Sessao, String> colNotas;

    @FXML
    private TextField txtPesquisar;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnApagar;

    private final ObservableList<Sessao> dados = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colHobby.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getHobby().getNome()));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colDuracao.setCellValueFactory(new PropertyValueFactory<>("duracaoMinutos"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));

        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            dados.setAll(user.getSessoes());
        }

        tblSessoes.setItems(dados);

        btnEditar.setDisable(true);
        btnApagar.setDisable(true);

        tblSessoes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean has = newSel != null;
            btnEditar.setDisable(!has);
            btnApagar.setDisable(!has);
        });
    }

    @FXML
    private void onNovaSessao() {
        abrirDialogSessao(null);
    }

    @FXML
    private void onEditarSessao() {
        Sessao selecionada = tblSessoes.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            abrirDialogSessao(selecionada);
        }
    }

    @FXML
    private void onApagarSessao() {
        Sessao selecionada = tblSessoes.getSelectionModel().getSelectedItem();
        if (selecionada == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Apagar sessão");
        alert.setHeaderText("Tens a certeza que queres apagar esta sessão?");
        alert.setContentText("Esta ação não pode ser desfeita.");
        alert.initOwner(tblSessoes.getScene().getWindow());

        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dados.remove(selecionada);
            var user = AppState.getInstance().getCurrentUser();
            if (user != null) {
                user.removerSessao(selecionada);
                AppState.getInstance().guardar();
            }
        }
    }

    private void abrirDialogSessao(Sessao aEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/AddSessionView.fxml"));
            Parent root = loader.load();

            AddSessionController controller = loader.getController();
            controller.setSessionsController(this);
            if (aEditar != null) {
                controller.setSessaoExistente(aEditar);
            }

            Stage dialog = new Stage();
            dialog.initOwner(tblSessoes.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(aEditar == null ? "Nova sessão" : "Editar sessão");
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void adicionarSessao(Sessao sessao) {
        dados.add(sessao);
        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            user.registarSessao(sessao);
            AppState.getInstance().guardar();
        }
    }

    public void sessaoAtualizada() {
        tblSessoes.refresh();
        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            AppState.getInstance().guardar();
        }
    }
}
