package services;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;
import models.Hobby;
import models.Sessao;
import models.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class ExportService {

    private ExportService() {}

    // =========================
    // TXT (human friendly)
    // =========================
    public static void exportUserDataTxt(User u, File outFile) throws IOException {
        if (u == null) throw new IllegalArgumentException("User null");
        if (outFile == null) throw new IllegalArgumentException("File null");

        String report = buildTxtReport(u);
        Files.writeString(outFile.toPath(), report, StandardCharsets.UTF_8);
    }

    private static String buildTxtReport(User u) {
        List<Hobby> hobbies = safeList(u.getHobbies());
        List<Sessao> sessoes = safeList(u.getSessoes());

        int totalSessoes = sessoes.size();
        int totalMinutos = sessoes.stream().mapToInt(Sessao::getDuracaoMinutos).sum();
        int media = totalSessoes == 0 ? 0 : Math.round((float) totalMinutos / totalSessoes);

        Map<String, Long> sessoesPorHobby = sessoes.stream()
                .collect(Collectors.groupingBy(ExportService::safeHobbyName, Collectors.counting()));

        Map<String, Integer> minutosPorHobby = sessoes.stream()
                .collect(Collectors.groupingBy(ExportService::safeHobbyName, Collectors.summingInt(Sessao::getDuracaoMinutos)));

        String hobbyTop = sessoesPorHobby.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

        List<Sessao> ord = sessoes.stream()
                .sorted(Comparator.comparing(Sessao::getData).thenComparing(Sessao::getHora).reversed())
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("EXPORT DE DADOS — GESTOR DE HOBBIES\n");
        sb.append("==================================\n");
        sb.append("Utilizador      : ").append(safe(u.getUsername())).append("\n");
        sb.append("Data do export  : ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append("Total de sessões: ").append(totalSessoes).append("\n");
        sb.append("Tempo total     : ").append(totalMinutos).append(" min\n");
        sb.append("Duração média   : ").append(media).append(" min\n");
        sb.append("Hobby mais usado: ").append(hobbyTop).append("\n\n");

        sb.append("HOBBIES (").append(hobbies.size()).append(")\n");
        sb.append("----------------\n");
        if (hobbies.isEmpty()) {
            sb.append("(sem hobbies)\n");
        } else {
            for (Hobby h : hobbies) {
                sb.append("• ").append(safe(h.getNome()))
                        .append(" [").append(String.valueOf(h.getCategoria())).append("]");
                String desc = safe(h.getDescricao());
                if (!desc.isBlank()) sb.append(" — ").append(desc);
                sb.append("\n");
            }
        }
        sb.append("\n");

        sb.append("SESSÕES (").append(ord.size()).append(")\n");
        sb.append("----------------\n");
        if (ord.isEmpty()) {
            sb.append("(sem sessões)\n");
        } else {
            for (Sessao s : ord) {
                sb.append("• ")
                        .append(s.getData().format(df)).append(" ")
                        .append(s.getHora().format(tf)).append("  |  ")
                        .append(safeHobbyName(s)).append("  |  ")
                        .append(s.getDuracaoMinutos()).append(" min");

                String notas = safe(s.getNotas());
                if (!notas.isBlank()) sb.append("  |  ").append(notas);
                sb.append("\n");
            }
        }
        sb.append("\n");

        sb.append("ESTATÍSTICAS POR HOBBY\n");
        sb.append("----------------------\n");

        sb.append("Sessões por hobby:\n");
        if (sessoesPorHobby.isEmpty()) sb.append("(sem dados)\n");
        else {
            sessoesPorHobby.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> sb.append("• ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));
        }

        sb.append("\nTempo por hobby:\n");
        if (minutosPorHobby.isEmpty()) sb.append("(sem dados)\n");
        else {
            minutosPorHobby.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(e -> sb.append("• ").append(e.getKey()).append(": ").append(e.getValue()).append(" min\n"));
        }

        return sb.toString();
    }

    // =========================
    // PDF (better look + charts + tables + pagination)
    // =========================
    public static void exportUserDataPdf(User u, File outFile) throws IOException {
        if (u == null) throw new IllegalArgumentException("User null");
        if (outFile == null) throw new IllegalArgumentException("File null");

        List<Hobby> hobbies = safeList(u.getHobbies());
        List<Sessao> sessoes = safeList(u.getSessoes());

        int totalSessoes = sessoes.size();
        int totalMinutos = sessoes.stream().mapToInt(Sessao::getDuracaoMinutos).sum();
        int media = totalSessoes == 0 ? 0 : Math.round((float) totalMinutos / totalSessoes);

        Map<String, Long> sessoesPorHobby = sessoes.stream()
                .collect(Collectors.groupingBy(ExportService::safeHobbyName, Collectors.counting()));

        Map<String, Integer> minutosPorHobby = sessoes.stream()
                .collect(Collectors.groupingBy(ExportService::safeHobbyName, Collectors.summingInt(Sessao::getDuracaoMinutos)));

        String hobbyTop = sessoesPorHobby.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        BufferedImage imgSessoes = chartToImage(buildBarChartCount("Sessões por hobby", "Hobby", "Sessões", sessoesPorHobby));
        BufferedImage imgTempo   = chartToImage(buildBarChartInt("Tempo total por hobby", "Hobby", "Minutos", minutosPorHobby));

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        String exportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        List<Sessao> sessoesOrd = sessoes.stream()
                .sorted(Comparator.comparing(Sessao::getData).thenComparing(Sessao::getHora).reversed())
                .toList();

        // Soft colors
        java.awt.Color COLOR_DARK = new java.awt.Color(15, 23, 42);
        java.awt.Color COLOR_GRAY = new java.awt.Color(100, 116, 139);
        java.awt.Color COLOR_CARD = new java.awt.Color(241, 245, 249);
        java.awt.Color COLOR_BORDER = new java.awt.Color(226, 232, 240);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 42f;
            float pageW = page.getMediaBox().getWidth();
            float pageH = page.getMediaBox().getHeight();
            float MIN_Y = 60f;

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = pageH - margin;

            // ===== HEADER =====
            y = drawTitle(cs, margin, y, "Export de Dados — Gestor de Hobbies");
            y -= 6;

            y = drawSmallLine(cs, margin, y, "Utilizador: " + safe(u.getUsername()), COLOR_DARK, 12);
            y = drawSmallLine(cs, margin, y, "Data do export: " + exportDate, COLOR_GRAY, 10);

            y -= 14;

            // ===== STATS CARDS =====
            float cardH = 52f;
            float gap = 10f;
            float cardW = (pageW - 2 * margin - 3 * gap) / 4f;

            y = drawStatCards(cs, margin, y, cardW, cardH, gap,
                    COLOR_CARD, COLOR_BORDER, COLOR_DARK, COLOR_GRAY,
                    String.valueOf(totalSessoes), "Sessões",
                    String.valueOf(totalMinutos), "Minutos totais",
                    String.valueOf(media), "Duração média",
                    hobbyTop, "Hobby mais usado");

            y -= 18;

            // ===== CHARTS =====
            PDImageXObject xImg1 = LosslessFactory.createFromImage(doc, imgSessoes);
            PDImageXObject xImg2 = LosslessFactory.createFromImage(doc, imgTempo);

            float usableW = pageW - 2 * margin;
            float chartW = (usableW - 14) / 2f;
            float chartH = 175;

            cs.drawImage(xImg1, margin, y - chartH, chartW, chartH);
            cs.drawImage(xImg2, margin + chartW + 14, y - chartH, chartW, chartH);

            y -= (chartH + 18);

            // ===== HOBBIES TABLE =====
            if (y < MIN_Y + 120) {
                cs.close();
                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                cs = new PDPageContentStream(doc, page);
                y = page.getMediaBox().getHeight() - margin;
            }

            y = drawSectionHeader(cs, margin, y, "Hobbies (" + hobbies.size() + ")");
            y -= 8;

            float[] hw = new float[] { 180, 110, (pageW - 2*margin) - 180 - 110 };
            y = drawTableHeader(cs, margin, y, hw, COLOR_DARK, COLOR_CARD, COLOR_BORDER,
                    new String[] {"Nome", "Categoria", "Descrição"});

            for (Hobby h : hobbies) {
                if (y < MIN_Y + 40) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - margin;

                    y = drawSectionHeader(cs, margin, y, "Hobbies (continuação)");
                    y -= 8;
                    y = drawTableHeader(cs, margin, y, hw, COLOR_DARK, COLOR_CARD, COLOR_BORDER,
                            new String[] {"Nome", "Categoria", "Descrição"});
                }

                String nome = safe(h.getNome());
                String cat = String.valueOf(h.getCategoria());
                String desc = safe(h.getDescricao());

                y = drawTableRow(cs, margin, y, hw, COLOR_DARK, COLOR_BORDER,
                        new String[] { nome, cat, desc });
            }

            y -= 24;

            // ===== SESSIONS TABLE =====
            if (y < MIN_Y + 120) {
                cs.close();
                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                cs = new PDPageContentStream(doc, page);
                y = page.getMediaBox().getHeight() - margin;
            }

            y = drawSectionHeader(cs, margin, y, "Sessões (" + sessoesOrd.size() + ")");
            y -= 8;

            float[] sw = new float[] { 120, 80, 60, 50, (pageW - 2*margin) - 120 - 80 - 60 - 50 };
            y = drawTableHeader(cs, margin, y, sw, COLOR_DARK, COLOR_CARD, COLOR_BORDER,
                    new String[] {"Hobby", "Data", "Hora", "Min", "Notas"});

            for (Sessao s : sessoesOrd) {
                if (y < MIN_Y + 40) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - margin;

                    y = drawSectionHeader(cs, margin, y, "Sessões (continuação)");
                    y -= 8;
                    y = drawTableHeader(cs, margin, y, sw, COLOR_DARK, COLOR_CARD, COLOR_BORDER,
                            new String[] {"Hobby", "Data", "Hora", "Min", "Notas"});
                }

                String hobby = safeHobbyName(s);
                String data = s.getData().format(df);
                String hora = s.getHora().format(tf);
                String mins = String.valueOf(s.getDuracaoMinutos());
                String notas = safe(s.getNotas());

                y = drawTableRow(cs, margin, y, sw, COLOR_DARK, COLOR_BORDER,
                        new String[] { hobby, data, hora, mins, notas });
            }

            cs.close();
            doc.save(outFile);
        }
    }

    // =========================
    // Charts (JavaFX) -> image (crisp)
    // =========================

    private static BarChart<String, Number> buildBarChartCount(String title, String xLabel, String yLabel, Map<String, Long> data) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        x.setLabel(xLabel);
        y.setLabel(yLabel);

        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        data.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        chart.getData().add(s);

        return chart;
    }

    private static BarChart<String, Number> buildBarChartInt(String title, String xLabel, String yLabel, Map<String, Integer> data) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        x.setLabel(xLabel);
        y.setLabel(yLabel);

        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        data.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        chart.getData().add(s);

        return chart;
    }

    private static BufferedImage chartToImage(BarChart<String, Number> chart) {
        // Fixed size so it doesn't render tiny
        chart.setMinSize(800, 420);
        chart.setPrefSize(800, 420);
        chart.setMaxSize(800, 420);

        Group root = new Group(chart);
        Scene scene = new Scene(root);

        // Apply CSS/layout
        root.applyCss();
        root.layout();
        chart.applyCss();
        chart.layout();

        SnapshotParameters sp = new SnapshotParameters();
        sp.setTransform(Transform.scale(2, 2)); // crisp

        WritableImage fxImg = chart.snapshot(sp, null);
        return SwingFXUtils.fromFXImage(fxImg, null);
    }

    // =========================
    // PDF drawing helpers
    // =========================

    private static float drawTitle(PDPageContentStream cs, float x, float y, String title) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
        cs.newLineAtOffset(x, y);
        cs.showText(title);
        cs.endText();
        return y - 26;
    }

    private static float drawSmallLine(PDPageContentStream cs, float x, float y, String text, java.awt.Color c, int size) throws IOException {
        cs.beginText();
        cs.setNonStrokingColor(c);
        cs.setFont(PDType1Font.HELVETICA, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        cs.setNonStrokingColor(java.awt.Color.BLACK);
        return y - (size + 4);
    }

    private static float drawSectionHeader(PDPageContentStream cs, float x, float y, String title) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(x, y);
        cs.showText(title);
        cs.endText();
        return y - 16;
    }

    private static float drawStatCards(
            PDPageContentStream cs,
            float x, float y,
            float cardW, float cardH, float gap,
            java.awt.Color cardBg, java.awt.Color border,
            java.awt.Color titleColor, java.awt.Color subColor,
            String v1, String l1,
            String v2, String l2,
            String v3, String l3,
            String v4, String l4) throws IOException {

        String[] vals = {v1, v2, v3, v4};
        String[] labs = {l1, l2, l3, l4};

        for (int i = 0; i < 4; i++) {
            float cx = x + i * (cardW + gap);
            float cy = y - cardH;

            cs.setNonStrokingColor(cardBg);
            cs.addRect(cx, cy, cardW, cardH);
            cs.fill();

            cs.setStrokingColor(border);
            cs.addRect(cx, cy, cardW, cardH);
            cs.stroke();

            cs.beginText();
            cs.setNonStrokingColor(titleColor);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(cx + 10, cy + 30);
            cs.showText(trimTo(vals[i], 18));
            cs.endText();

            cs.beginText();
            cs.setNonStrokingColor(subColor);
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.newLineAtOffset(cx + 10, cy + 12);
            cs.showText(trimTo(labs[i], 22));
            cs.endText();

            cs.setNonStrokingColor(java.awt.Color.BLACK);
            cs.setStrokingColor(java.awt.Color.BLACK);
        }

        return y - cardH;
    }

    private static float drawTableHeader(
            PDPageContentStream cs,
            float x, float y,
            float[] widths,
            java.awt.Color textColor,
            java.awt.Color bg,
            java.awt.Color border,
            String[] cols) throws IOException {

        float rowH = 18f;
        float cy = y - rowH;

        cs.setNonStrokingColor(bg);
        cs.addRect(x, cy, sum(widths), rowH);
        cs.fill();

        cs.setStrokingColor(border);
        cs.addRect(x, cy, sum(widths), rowH);
        cs.stroke();

        cs.setNonStrokingColor(textColor);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10);

        float cx = x;
        for (int i = 0; i < cols.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(cx + 6, cy + 6);
            cs.showText(trimTo(cols[i], (int)(widths[i] / 5)));
            cs.endText();
            cx += widths[i];
        }

        cs.setNonStrokingColor(java.awt.Color.BLACK);
        return cy;
    }

    private static float drawTableRow(
            PDPageContentStream cs,
            float x, float y,
            float[] widths,
            java.awt.Color textColor,
            java.awt.Color border,
            String[] values) throws IOException {

        float rowH = 18f;
        float cy = y - rowH;

        cs.setStrokingColor(border);
        cs.addRect(x, cy, sum(widths), rowH);
        cs.stroke();

        // vertical lines
        float cx = x;
        for (float w : widths) {
            cs.moveTo(cx, cy);
            cs.lineTo(cx, cy + rowH);
            cs.stroke();
            cx += w;
        }
        cs.moveTo(x + sum(widths), cy);
        cs.lineTo(x + sum(widths), cy + rowH);
        cs.stroke();

        cs.setNonStrokingColor(textColor);
        cs.setFont(PDType1Font.HELVETICA, 9);

        cx = x;
        for (int i = 0; i < values.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(cx + 6, cy + 6);
            cs.showText(trimTo(values[i], (int)(widths[i] / 5)));
            cs.endText();
            cx += widths[i];
        }

        cs.setNonStrokingColor(java.awt.Color.BLACK);
        cs.setStrokingColor(java.awt.Color.BLACK);
        return cy;
    }

    private static float sum(float[] a) {
        float s = 0;
        for (float v : a) s += v;
        return s;
    }

    private static String trimTo(String s, int maxChars) {
        if (s == null) return "";
        String t = s.replace("\n", " ").replace("\r", " ").trim();
        if (t.length() <= maxChars) return t;
        if (maxChars <= 3) return t.substring(0, Math.max(0, maxChars));
        return t.substring(0, maxChars - 3) + "...";
    }

    // =========================
    // general helpers
    // =========================

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String safeHobbyName(Sessao s) {
        try {
            if (s.getHobby() != null && s.getHobby().getNome() != null) return s.getHobby().getNome();
        } catch (Exception ignored) {}
        return "Desconhecido";
    }
}
