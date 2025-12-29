/*
 * Propósito geral: gerir a lista de sessões do utilizador, permitindo criar,
 * editar, apagar e filtrar entradas, com formatação de datas/horas conforme
 * preferências globais.
 * Observações: usa FilteredList para pesquisa reativa; formatação dinâmica de
 * data/hora respeitando preferências do utilizador; dialogs modais notificam
 * via callbacks; persiste após alterações.
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
import models.Sessao;
import services.AppState;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

    // Dados base e versão filtrada
    private final ObservableList<Sessao> dados = FXCollections.observableArrayList();
    private FilteredList<Sessao> filtrado;

    // Configura colunas, carrega dados, formata data/hora e ativa pesquisa
    @FXML
    private void initialize() {
        // Vincula colunas aos atributos das sessões
        colHobby.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getHobby().getNome()));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colDuracao.setCellValueFactory(new PropertyValueFactory<>("duracaoMinutos"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));

        // Carrega sessões do utilizador
        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            dados.setAll(user.getSessoes());
        }

        filtrado = new FilteredList<>(dados, s -> true);
        tblSessoes.setItems(filtrado);

        // Aplica formatação de datas conforme preferência da app
        if (colData != null) {
            colData.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(ui.App.getDateFormatPattern());
                        setText(item.format(fmt));
                    }
                }
            });
        }

        // Aplica formatação de horas (12h/24h) conforme preferência da app
        if (colHora != null) {
            colHora.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(LocalTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String pattern = ui.App.isUse24HourTime() ? "HH:mm" : "h:mm a";
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern);
                        setText(item.format(fmt));
                    }
                }
            });
        }

        btnEditar.setDisable(true);
        btnApagar.setDisable(true);

        // Habilita botões apenas se houver seleção
        tblSessoes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean has = newSel != null;
            btnEditar.setDisable(!has);
            btnApagar.setDisable(!has);
        });

        // Pesquisa por hobby, notas ou data
        if (txtPesquisar != null) {
            txtPesquisar.textProperty().addListener((obs, ov, nv) -> {
                String q = nv == null ? "" : nv.trim().toLowerCase();
                filtrado.setPredicate(s -> {
                    if (q.isEmpty()) return true;
                    String hobby = s.getHobby() != null && s.getHobby().getNome() != null ? s.getHobby().getNome().toLowerCase() : "";
                    String notas = s.getNotas() != null ? s.getNotas().toLowerCase() : "";
                    String data = s.getData() != null ? s.getData().toString().toLowerCase() : "";
                    return hobby.contains(q) || notas.contains(q) || data.contains(q);
                });
            });
        }
    }

    // Abre diálogo para criar uma nova sessão
    @FXML
    private void onNovaSessao() {
        abrirDialogSessao(null);
    }

    // Abre diálogo para editar a sessão selecionada
    @FXML
    private void onEditarSessao() {
        Sessao selecionada = tblSessoes.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            abrirDialogSessao(selecionada);
        }
    }

    // Pede confirmação e remove a sessão selecionada
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

    // Carrega o diálogo de sessão, injeta callbacks e mostra modal
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
            if (App.getAppIcon() != null) dialog.getIcons().add(App.getAppIcon());
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Callback quando uma nova sessão é criada
    public void adicionarSessao(Sessao sessao) {
        dados.add(sessao);
        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            user.registarSessao(sessao);
            AppState.getInstance().guardar();
        }
    }

    // Callback após edição: refresca tabela e persiste
    public void sessaoAtualizada() {
        tblSessoes.refresh();
        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            AppState.getInstance().guardar();
        }
    }
}

