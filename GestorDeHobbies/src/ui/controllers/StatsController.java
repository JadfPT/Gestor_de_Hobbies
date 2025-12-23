package ui.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.Hobby;
import models.Sessao;
import models.User;
import services.AppState;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsController {

    @FXML
    private Label lblResumo;

    @FXML
    private Label lblTotalSessoes;

    @FXML
    private Label lblTotalMinutos;

    @FXML
    private Label lblDuracaoMedia;

    @FXML
    private Label lblHobbyTop;

    @FXML
    private BarChart<String, Number> chartSessoesPorHobby;

    @FXML
    private BarChart<String, Number> chartTempoPorHobby;

    @FXML
    private TableView<Sessao> tblRecentes;

    @FXML
    private TableColumn<Sessao, String> colHobby;

    @FXML
    private TableColumn<Sessao, String> colData;

    @FXML
    private TableColumn<Sessao, Integer> colMinutos;

    private final DateTimeFormatter fmtData = DateTimeFormatter.ofPattern("dd/MM");

    @FXML
    private void initialize() {
        User user = AppState.getInstance().getCurrentUser();
        if (user == null) {
            lblResumo.setText("Nenhum utilizador autenticado.");
            setZeroStats();
            return;
        }

        List<Sessao> sessoes = user.getSessoes();
        if (sessoes.isEmpty()) {
            lblResumo.setText("Ainda não tens sessões registadas.");
            setZeroStats();
            return;
        }

        int totalSessoes = sessoes.size();
        int totalMinutos = sessoes.stream()
                .mapToInt(Sessao::getDuracaoMinutos)
                .sum();
        int media = Math.round((float) totalMinutos / totalSessoes);

        Map<Hobby, Long> porHobby = sessoes.stream()
                .collect(Collectors.groupingBy(Sessao::getHobby, Collectors.counting()));

        Map<Hobby, Integer> minutosPorHobby = sessoes.stream()
                .collect(Collectors.groupingBy(
                        Sessao::getHobby,
                        Collectors.summingInt(Sessao::getDuracaoMinutos)
                ));

        Hobby hobbyTop = porHobby.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        lblResumo.setText("Resumo das tuas atividades.");
        lblTotalSessoes.setText(String.valueOf(totalSessoes));
        lblTotalMinutos.setText(totalMinutos + " min");
        lblDuracaoMedia.setText(media + " min");

        if (hobbyTop != null) {
            lblHobbyTop.setText(hobbyTop.getNome());
        } else {
            lblHobbyTop.setText("—");
        }

        XYChart.Series<String, Number> serieSessoes = new XYChart.Series<>();
        porHobby.forEach((hobby, count) ->
                serieSessoes.getData().add(new XYChart.Data<>(hobby.getNome(), count))
        );
        chartSessoesPorHobby.getData().setAll(serieSessoes);

        XYChart.Series<String, Number> serieTempo = new XYChart.Series<>();
        minutosPorHobby.forEach((hobby, minutos) ->
                serieTempo.getData().add(new XYChart.Data<>(hobby.getNome(), minutos))
        );
        chartTempoPorHobby.getData().setAll(serieTempo);

        colHobby.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getHobby().getNome()));
        colData.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getData().format(fmtData)));
        colMinutos.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getDuracaoMinutos()).asObject());

        List<Sessao> ultimas = sessoes.stream()
                .sorted(Comparator
                        .comparing(Sessao::getData)
                        .thenComparing(Sessao::getHora)
                        .reversed())
                .limit(5)
                .collect(Collectors.toList());

        tblRecentes.setItems(FXCollections.observableArrayList(ultimas));
    }

    private void setZeroStats() {
        lblTotalSessoes.setText("0");
        lblTotalMinutos.setText("0 min");
        lblDuracaoMedia.setText("0 min");
        lblHobbyTop.setText("—");
        if (chartSessoesPorHobby != null) {
            chartSessoesPorHobby.getData().clear();
        }
        if (chartTempoPorHobby != null) {
            chartTempoPorHobby.getData().clear();
        }
        if (tblRecentes != null) {
            tblRecentes.getItems().clear();
        }
    }
}
