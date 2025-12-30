/*
 * Propósito geral: gerir a lista de hobbies do utilizador, permitindo criar,
 * editar, apagar e filtrar entradas, mantendo tudo sincronizado com o estado
 * global da aplicação.
 * Observações: usa FilteredList para pesquisa reativa; dialogs são modais e
 * notificam este controlador por callbacks; persiste alterações após cada ação.
 */
package ui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.App;
import models.Categoria;
import models.Hobby;
import services.AppState;

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

    // Dados base e versão filtrada usados pela tabela
    private final ObservableList<Hobby> dados = FXCollections.observableArrayList();
    private FilteredList<Hobby> filtrado;

    // Configura colunas, carrega dados do utilizador e ativa pesquisa/botões
    @FXML
    private void initialize() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            dados.setAll(user.getHobbies());
        }

        filtrado = new FilteredList<>(dados, h -> true);
        tblHobbies.setItems(filtrado);

        // Pesquisa por nome, descrição ou categoria
        if (txtPesquisar != null) {
            txtPesquisar.textProperty().addListener((obs, oldV, newV) -> {
                String q = newV == null ? "" : newV.trim().toLowerCase();
                filtrado.setPredicate(h -> {
                    if (q.isEmpty()) return true;
                    boolean nome = h.getNome() != null && h.getNome().toLowerCase().contains(q);
                    boolean desc = h.getDescricao() != null && h.getDescricao().toLowerCase().contains(q);
                    boolean cat = h.getCategoria() != null && h.getCategoria().name().toLowerCase().contains(q);
                    return nome || desc || cat;
                });
            });
        }

        btnEditar.setDisable(true);
        btnApagar.setDisable(true);

        tblHobbies.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean has = newSel != null;
            btnEditar.setDisable(!has);
            btnApagar.setDisable(!has);
        });
    }

    // Abre diálogo para criar um novo hobby
    @FXML
    private void onNovoHobby() {
        abrirDialogHobby(null);
    }

    // Abre diálogo para editar o hobby selecionado
    @FXML
    private void onEditarHobby() {
        Hobby selecionado = tblHobbies.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            abrirDialogHobby(selecionado);
        }
    }

    // Pede confirmação e remove o hobby selecionado
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

    // Carrega o diálogo de hobby, injeta callbacks e mostra modal
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
            Scene dialogScene = new Scene(root);
            dialog.setScene(dialogScene);
            dialog.setResizable(false);
            if (App.getAppIcon() != null) dialog.getIcons().add(App.getAppIcon());
            // Apply current theme to the dialog so it matches user preferences
            ui.App.applyThemeToScene(dialogScene);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Callback quando um novo hobby é criado
    public void adicionarHobby(Hobby hobby) {
        dados.add(hobby);

        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            user.adicionarHobby(hobby);
            AppState.getInstance().guardar();
        }
    }

    // Callback após edição: refresca tabela e persiste
    public void hobbyAtualizado() {
        tblHobbies.refresh();
        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            AppState.getInstance().guardar();
        }
    }
}
