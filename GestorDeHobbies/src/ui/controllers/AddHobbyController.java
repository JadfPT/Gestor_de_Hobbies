// src/ui/controllers/AddHobbyController.java
package ui.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Categoria;
import models.Hobby;
import services.PredefinedHobbies;

public class AddHobbyController {

    @FXML
    private ComboBox<PredefinedHobbies> cmbSugestao;

    @FXML
    private ComboBox<Categoria> cmbCategoria;

    @FXML
    private TextField txtNome;

    @FXML
    private TextArea txtDescricao;

    @FXML
    private Label lblMensagem;

    private HobbiesController hobbiesController;
    private Hobby hobbyExistente; // null = novo, != null = editar

    public void setHobbiesController(HobbiesController controller) {
        this.hobbiesController = controller;
    }

    public void setHobbyExistente(Hobby hobby) {
        this.hobbyExistente = hobby;

        // preenche campos com os valores atuais
        txtNome.setText(hobby.getNome());
        cmbCategoria.setValue(hobby.getCategoria());
        txtDescricao.setText(hobby.getDescricao());

        // para edição, queremos tudo editável, independentemente da sugestão original
        txtNome.setEditable(true);
        txtNome.setDisable(false);
        cmbCategoria.setDisable(false);
        txtDescricao.setDisable(false);

        // mantemos a combo de sugestões vazia; o user pode escolher outra se quiser
        cmbSugestao.getSelectionModel().clearSelection();
    }

    @FXML
    private void initialize() {
        cmbSugestao.setItems(FXCollections.observableArrayList(PredefinedHobbies.values()));
        cmbCategoria.setItems(FXCollections.observableArrayList(Categoria.values()));

        // estado inicial: tudo editável
        txtNome.setEditable(true);
        txtNome.setDisable(false);
        cmbCategoria.setDisable(false);
        txtDescricao.setDisable(false);
    }

    @FXML
    private void onSugestaoSelecionada() {
        PredefinedHobbies sel = cmbSugestao.getValue();
        if (sel == null) {
            return;
        }

        if (sel == PredefinedHobbies.OUTRO_PERSONALIZADO) {
            // modo livre: user escreve tudo
            txtNome.clear();
            txtDescricao.clear();
            cmbCategoria.setValue(null);

            txtNome.setEditable(true);
            txtNome.setDisable(false);
            cmbCategoria.setDisable(false);
            txtDescricao.setDisable(false);
        } else {
            // sugestão fixa: nome + categoria bloqueados; descrição pode ser ajustada
            txtNome.setText(sel.getNome());
            cmbCategoria.setValue(sel.getCategoria());
            if (!sel.getDescricao().isEmpty()) {
                txtDescricao.setText(sel.getDescricao());
            }

            txtNome.setEditable(false);
            txtNome.setDisable(true);
            cmbCategoria.setDisable(true);
            txtDescricao.setDisable(false);
        }
    }

    @FXML
    private void onGuardar() {
        String nome = txtNome.getText().trim();
        Categoria categoria = cmbCategoria.getValue();
        String descricao = txtDescricao.getText().trim();

        if (nome.isEmpty() || categoria == null) {
            lblMensagem.setText("Preenche pelo menos o nome e a categoria.");
            return;
        }

        if (hobbyExistente != null) {
            // editar
            hobbyExistente.setNome(nome);
            hobbyExistente.setCategoria(categoria);
            hobbyExistente.setDescricao(descricao);

            if (hobbiesController != null) {
                hobbiesController.hobbyAtualizado();
            }
        } else {
            // novo
            Hobby hobby = new Hobby(nome, categoria, descricao);
            if (hobbiesController != null) {
                hobbiesController.adicionarHobby(hobby);
            }
        }

        fecharJanela();
    }

    @FXML
    private void onCancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        stage.close();
    }
}
