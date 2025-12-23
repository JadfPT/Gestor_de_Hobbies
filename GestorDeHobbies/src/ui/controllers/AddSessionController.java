package ui.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Hobby;
import models.Sessao;
import services.AppState;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddSessionController {

    @FXML
    private ComboBox<Hobby> cmbHobby;

    @FXML
    private DatePicker dpDataInicio;

    @FXML
    private DatePicker dpDataFim;

    @FXML
    private TextField txtHoraInicio;

    @FXML
    private TextField txtHoraFim;

    @FXML
    private TextArea txtDetalhes;

    @FXML
    private Label lblMensagem;

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblSubtitulo;

    private SessionsController sessionsController;
    private Sessao sessaoExistente;

    public void setSessionsController(SessionsController controller) {
        this.sessionsController = controller;
    }

    public void setSessaoExistente(Sessao sessao) {
        this.sessaoExistente = sessao;
        if (sessao != null) {
            cmbHobby.setValue(sessao.getHobby());
            dpDataInicio.setValue(sessao.getData());
            txtHoraInicio.setText(formatHora(sessao.getHora()));

            LocalDateTime inicio = LocalDateTime.of(sessao.getData(), sessao.getHora());
            LocalDateTime fim = inicio.plusMinutes(sessao.getDuracaoMinutos());
            dpDataFim.setValue(fim.toLocalDate());
            txtHoraFim.setText(formatHora(fim.toLocalTime()));

            txtDetalhes.setText(sessao.getNotas());

            lblTitulo.setText("Editar sessão");
            lblSubtitulo.setText("Atualiza a informação desta sessão.");
        }
    }

    @FXML
    private void initialize() {
        var user = AppState.getInstance().getCurrentUser();
        if (user != null) {
            cmbHobby.setItems(FXCollections.observableArrayList(user.getHobbies()));
        }
        LocalDate hoje = LocalDate.now();
        dpDataInicio.setValue(hoje);
        dpDataFim.setValue(hoje);

        if (lblTitulo != null) {
            lblTitulo.setText("Nova sessão");
        }
        if (lblSubtitulo != null) {
            lblSubtitulo.setText("Regista uma atividade realizada num dos teus hobbies.");
        }
    }

    @FXML
    private void onGuardar() {
        Hobby hobby = cmbHobby.getValue();
        LocalDate dataIni = dpDataInicio.getValue();
        LocalDate dataFim = dpDataFim.getValue();
        String horaIniStr = txtHoraInicio.getText().trim();
        String horaFimStr = txtHoraFim.getText().trim();
        String detalhes = txtDetalhes.getText().trim();

        if (hobby == null || dataIni == null || dataFim == null ||
                horaIniStr.isEmpty() || horaFimStr.isEmpty()) {
            lblMensagem.setText("Preenche hobby, datas e horas de início e fim.");
            return;
        }

        LocalTime horaIni;
        LocalTime horaFim;
        try {
            horaIni = LocalTime.parse(horaIniStr);
            horaFim = LocalTime.parse(horaFimStr);
        } catch (Exception e) {
            lblMensagem.setText("Hora inválida (usa HH:MM).");
            return;
        }

        LocalDateTime inicio = LocalDateTime.of(dataIni, horaIni);
        LocalDateTime fim = LocalDateTime.of(dataFim, horaFim);

        if (!fim.isAfter(inicio)) {
            lblMensagem.setText("A data/hora de fim tem de ser depois do início.");
            return;
        }

        long duracaoMin = Duration.between(inicio, fim).toMinutes();

        if (sessaoExistente != null) {
            // editar sessão existente
            sessaoExistente.setHobby(hobby);
            sessaoExistente.setData(dataIni);
            sessaoExistente.setHora(horaIni);
            sessaoExistente.setDuracaoMinutos((int) duracaoMin);
            sessaoExistente.setNotas(detalhes);

            if (sessionsController != null) {
                sessionsController.sessaoAtualizada();
            }
        } else {
            // nova sessão
            Sessao sessao = new Sessao(
                    hobby,
                    dataIni,
                    horaIni,
                    (int) duracaoMin,
                    detalhes
            );
            if (sessionsController != null) {
                sessionsController.adicionarSessao(sessao);
            }
        }

        fechar();
    }

    @FXML
    private void onCancelar() {
        fechar();
    }

    private void fechar() {
        Stage stage = (Stage) cmbHobby.getScene().getWindow();
        stage.close();
    }

    private String formatHora(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }
}
