/*
 * Propósito geral: controlar o formulário de criação/edição de hobbies, incluindo
 * preenchimento a partir de sugestões pré-definidas e devolução do resultado ao
 * controlador principal.
 * Observações: suporta modo edição (hobbyExistente) e modo criação; alterna entre
 * campos editáveis ou bloqueados conforme a sugestão escolhida; mensagens simples
 * de validação no próprio formulário.
 */
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

    // Referência ao controlador pai para notificar alterações
    private HobbiesController hobbiesController;
    // Quando não nulo, o formulário atua em modo edição
    private Hobby hobbyExistente;

    // Injetado pelo controlador pai para callbacks
    public void setHobbiesController(HobbiesController controller) {
        this.hobbiesController = controller;
    }

    // Preenche o formulário com dados de um hobby para edição
    public void setHobbyExistente(Hobby hobby) {
        this.hobbyExistente = hobby;

        txtNome.setText(hobby.getNome());
        cmbCategoria.setValue(hobby.getCategoria());
        txtDescricao.setText(hobby.getDescricao());

        // Garante edição manual quando vem de um existente
        txtNome.setEditable(true);
        txtNome.setDisable(false);
        cmbCategoria.setDisable(false);
        txtDescricao.setDisable(false);

        cmbSugestao.getSelectionModel().clearSelection();
    }

    // Inicialização do formulário: carrega listas e ativa campos
    @FXML
    private void initialize() {
        cmbSugestao.setItems(FXCollections.observableArrayList(PredefinedHobbies.values()));
        cmbCategoria.setItems(FXCollections.observableArrayList(Categoria.values()));

        txtNome.setEditable(true);
        txtNome.setDisable(false);
        cmbCategoria.setDisable(false);
        txtDescricao.setDisable(false);
    }

    // Reage à escolha de uma sugestão, preenchendo ou liberando campos
    @FXML
    private void onSugestaoSelecionada() {
        PredefinedHobbies sel = cmbSugestao.getValue();
        if (sel == null) {
            return;
        }

        if (sel == PredefinedHobbies.OUTRO_PERSONALIZADO) {
            txtNome.clear();
            txtDescricao.clear();
            cmbCategoria.setValue(null);

            // Modo personalizado: tudo editável
            txtNome.setEditable(true);
            txtNome.setDisable(false);
            cmbCategoria.setDisable(false);
            txtDescricao.setDisable(false);
        } else {
            txtNome.setText(sel.getNome());
            cmbCategoria.setValue(sel.getCategoria());
            if (!sel.getDescricao().isEmpty()) {
                txtDescricao.setText(sel.getDescricao());
            }

            // Sugestão fixa: bloqueia nome e categoria, permite editar descrição
            txtNome.setEditable(false);
            txtNome.setDisable(true);
            cmbCategoria.setDisable(true);
            txtDescricao.setDisable(false);
        }
    }

    // Valida e guarda o hobby (novo ou existente), avisando o controlador pai
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
            hobbyExistente.setNome(nome);
            hobbyExistente.setCategoria(categoria);
            hobbyExistente.setDescricao(descricao);

            if (hobbiesController != null) {
                hobbiesController.hobbyAtualizado();
            }
        } else {
            Hobby hobby = new Hobby(nome, categoria, descricao);
            if (hobbiesController != null) {
                hobbiesController.adicionarHobby(hobby);
            }
        }

        fecharJanela();
    }

    // Cancela a operação e fecha a janela
    @FXML
    private void onCancelar() {
        fecharJanela();
    }

    // Encapsula o fecho da janela atual
    private void fecharJanela() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        stage.close();
    }
}
