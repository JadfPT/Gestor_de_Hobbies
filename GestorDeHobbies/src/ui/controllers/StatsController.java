package ui.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import ui.App;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.Sessao;
import services.AppState;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private TableColumn<Sessao, LocalDate> colData;

    @FXML
    private TableColumn<Sessao, Integer> colMinutos;

    @FXML
    private void initialize() {
        if (colHobby != null) {
            colHobby.setCellValueFactory(cell -> {
                Sessao s = cell.getValue();
                String nome = (s != null && s.getHobby() != null && s.getHobby().getNome() != null)
                        ? s.getHobby().getNome()
                        : "—";
                return new SimpleStringProperty(nome);
            });
        }

        if (colData != null) {
            colData.setCellValueFactory(cell -> {
                Sessao s = cell.getValue();
                return new SimpleObjectProperty<>(s != null ? s.getData() : null);
            });
        }

        if (colMinutos != null) {
            colMinutos.setCellValueFactory(cell -> {
                Sessao s = cell.getValue();
                int m = (s != null) ? s.getDuracaoMinutos() : 0;
                return new SimpleIntegerProperty(m).asObject();
            });
        }

        if (chartSessoesPorHobby != null) {
            chartSessoesPorHobby.setLegendVisible(false);
            chartSessoesPorHobby.setAnimated(false);
            chartSessoesPorHobby.setCategoryGap(18);
            chartSessoesPorHobby.setBarGap(4);
        }

        if (chartTempoPorHobby != null) {
            chartTempoPorHobby.setLegendVisible(false);
            chartTempoPorHobby.setAnimated(false);
            chartTempoPorHobby.setCategoryGap(18);
            chartTempoPorHobby.setBarGap(4);
        }

        // Listen for chart color preference changes
        App.addChartColorListener(this::applyChartColors);

        refresh();
    }

    public void refresh() {
        var user = AppState.getInstance().getCurrentUser();
        if (user == null) {
            setEmptyUI();
            return;
        }

        List<Sessao> sessoes = new ArrayList<>(user.getSessoes());

        int totalSessoes = sessoes.size();
        int totalMinutos = 0;

        Map<String, Integer> countByHobby = new LinkedHashMap<>();
        Map<String, Integer> minutesByHobby = new LinkedHashMap<>();

        for (Sessao s : sessoes) {
            int dur = (s != null) ? s.getDuracaoMinutos() : 0;
            totalMinutos += Math.max(0, dur);

            String hobby = (s != null && s.getHobby() != null && s.getHobby().getNome() != null)
                    ? s.getHobby().getNome()
                    : "—";

            countByHobby.put(hobby, countByHobby.getOrDefault(hobby, 0) + 1);
            minutesByHobby.put(hobby, minutesByHobby.getOrDefault(hobby, 0) + Math.max(0, dur));
        }

        int duracaoMedia = totalSessoes == 0 ? 0 : (int) Math.round((double) totalMinutos / totalSessoes);

        String hobbyTop = "—";
        if (!countByHobby.isEmpty()) {
            hobbyTop = countByHobby.entrySet().stream()
                    .max((a, b) -> {
                        int cmp = Integer.compare(a.getValue(), b.getValue());
                        if (cmp != 0) return cmp;
                        int ma = minutesByHobby.getOrDefault(a.getKey(), 0);
                        int mb = minutesByHobby.getOrDefault(b.getKey(), 0);
                        cmp = Integer.compare(ma, mb);
                        if (cmp != 0) return cmp;
                        return b.getKey().compareToIgnoreCase(a.getKey());
                    })
                    .map(Map.Entry::getKey)
                    .orElse("—");
        }

        if (lblResumo != null) {
            lblResumo.setText(totalSessoes == 0 ? "Ainda não tens sessões registadas." : "Resumo das tuas atividades.");
        }
        if (lblTotalSessoes != null) lblTotalSessoes.setText(String.valueOf(totalSessoes));
        if (lblTotalMinutos != null) lblTotalMinutos.setText(String.valueOf(totalMinutos));
        if (lblDuracaoMedia != null) lblDuracaoMedia.setText(String.valueOf(duracaoMedia));
        if (lblHobbyTop != null) lblHobbyTop.setText(hobbyTop);

        fillChart(chartSessoesPorHobby, sortAndLimit(countByHobby, 8), "Sessões");
        fillChart(chartTempoPorHobby, sortAndLimit(minutesByHobby, 8), "Minutos");

        if (tblRecentes != null) {
            sessoes.sort(Comparator
                    .comparing(Sessao::getData, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Sessao::getHora, Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed());

            int limit = Math.min(7, sessoes.size());
            tblRecentes.getItems().setAll(sessoes.subList(0, limit));
        }
    }

    private void setEmptyUI() {
        if (lblResumo != null) lblResumo.setText("Sem sessão ativa.");
        if (lblTotalSessoes != null) lblTotalSessoes.setText("0");
        if (lblTotalMinutos != null) lblTotalMinutos.setText("0");
        if (lblDuracaoMedia != null) lblDuracaoMedia.setText("0");
        if (lblHobbyTop != null) lblHobbyTop.setText("—");
        if (chartSessoesPorHobby != null) chartSessoesPorHobby.getData().clear();
        if (chartTempoPorHobby != null) chartTempoPorHobby.getData().clear();
        if (tblRecentes != null) tblRecentes.getItems().clear();
    }

    private Map<String, Integer> sortAndLimit(Map<String, Integer> in, int limit) {
        return in.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private void fillChart(BarChart<String, Number> chart, Map<String, Integer> data, String seriesName) {
        if (chart == null) return;

        chart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        for (var e : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }

        chart.getData().add(series);

        // Apply bar color to each bar after layout
        javafx.application.Platform.runLater(() -> {
            String color = App.getChartColor();
            for (var d : series.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        });
    }

    @FXML
    private void initializeListeners() {
        // Update chart colors immediately when preference changes
        App.addChartColorListener(this::applyChartColors);
    }

    private void applyChartColors() {
        javafx.application.Platform.runLater(() -> {
            String color = App.getChartColor();
            if (chartSessoesPorHobby != null) {
                for (var series : chartSessoesPorHobby.getData()) {
                    for (var d : series.getData()) {
                        if (d.getNode() != null) d.getNode().setStyle("-fx-bar-fill: " + color + ";");
                    }
                }
            }
            if (chartTempoPorHobby != null) {
                for (var series : chartTempoPorHobby.getData()) {
                    for (var d : series.getData()) {
                        if (d.getNode() != null) d.getNode().setStyle("-fx-bar-fill: " + color + ";");
                    }
                }
            }
        });
    }
}