/*
 * Propósito geral: fornecer funcionalidades de exportação de dados do utilizador
 * para formatos TXT e PDF, incluindo estatísticas completas, gráficos visuais e
 * tabelas estruturadas de hobbies e sessões.
 * Observações: classe utilitária (final, não instanciável); gera PDFs com paginação
 * automática, gráficos criados via JavaFX (convertidos em imagens de alta qualidade),
 * design visual moderno com cores suaves; tratamento seguro de dados nulos; suporta
 * codificação UTF-8 para caracteres especiais.
 */
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

    // Construtor privado previne instanciação (classe utilitária)
    private ExportService() {}
 
    /**
     * Exporta os dados do utilizador para um ficheiro TXT formatado.
     * Validação: verifica se utilizador e ficheiro não são nulos.
     * Escrita: utiliza UTF-8 para suportar caracteres especiais.
     */
    public static void exportUserDataTxt(User u, File outFile) throws IOException {
        if (u == null) throw new IllegalArgumentException("User null");
        if (outFile == null) throw new IllegalArgumentException("File null");

        String report = buildTxtReport(u);
        Files.writeString(outFile.toPath(), report, StandardCharsets.UTF_8);
    }

    /**
     * Constrói o relatório TXT completo com todas as secções.
     * Estrutura: cabeçalho → estatísticas gerais → lista hobbies → lista sessões → estatísticas por hobby.
     */
    private static String buildTxtReport(User u) {
        // Extrai listas de forma segura 
        List<Hobby> hobbies = safeList(u.getHobbies());
        List<Sessao> sessoes = safeList(u.getSessoes());

        // Calcula estatísticas globais
        int totalSessoes = sessoes.size();
        int totalMinutos = sessoes.stream().mapToInt(Sessao::getDuracaoMinutos).sum();
        int media = totalSessoes == 0 ? 0 : Math.round((float) totalMinutos / totalSessoes);

        // Agrupa sessões por hobby (conta quantas sessões cada hobby tem)
        Map<String, Long> sessoesPorHobby = sessoes.stream()
                .collect(Collectors.groupingBy(ExportService::safeHobbyName, Collectors.counting()));

        // Soma minutos totais por hobby
        Map<String, Integer> minutosPorHobby = sessoes.stream()
                .collect(Collectors.groupingBy(ExportService::safeHobbyName, Collectors.summingInt(Sessao::getDuracaoMinutos)));

        // Identifica o hobby com mais sessões
        String hobbyTop = sessoesPorHobby.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        // Define formatadores de data e hora para exibição
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

        // Ordena sessões por data/hora (mais recentes primeiro)
        List<Sessao> ord = sessoes.stream()
                .sorted(Comparator.comparing(Sessao::getData).thenComparing(Sessao::getHora).reversed())
                .toList();

        // Constrói o relatório textual secção por secção
        StringBuilder sb = new StringBuilder();
        
        // Cabeçalho com informações do utilizador e data do export
        sb.append("EXPORT DE DADOS — GESTOR DE HOBBIES\n");
        sb.append("==================================\n");
        sb.append("Utilizador      : ").append(safe(u.getUsername())).append("\n");
        sb.append("Data do export  : ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append("Total de sessões: ").append(totalSessoes).append("\n");
        sb.append("Tempo total     : ").append(totalMinutos).append(" min\n");
        sb.append("Duração média   : ").append(media).append(" min\n");
        sb.append("Hobby mais usado: ").append(hobbyTop).append("\n\n");

        // Secção de hobbies: lista todos os hobbies do utilizador
        sb.append("HOBBIES (").append(hobbies.size()).append(")\n");
        sb.append("----------------\n");
        if (hobbies.isEmpty()) {
            sb.append("(sem hobbies)\n");
        } else {
            // Para cada hobby, mostra nome, categoria e descrição (se existir)
            for (Hobby h : hobbies) {
                sb.append("• ").append(safe(h.getNome()))
                        .append(" [").append(String.valueOf(h.getCategoria())).append("]");
                String desc = safe(h.getDescricao());
                if (!desc.isBlank()) sb.append(" — ").append(desc);
                sb.append("\n");
            }
        }
        sb.append("\n");

        // Secção de sessões: lista todas as sessões ordenadas por data/hora
        sb.append("SESSÕES (").append(ord.size()).append(")\n");
        sb.append("----------------\n");
        if (ord.isEmpty()) {
            sb.append("(sem sessões)\n");
        } else {
            // Para cada sessão, mostra data, hora, hobby, duração e notas (se existirem)
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

        // Secção de estatísticas por hobby
        sb.append("ESTATÍSTICAS POR HOBBY\n");
        sb.append("----------------------\n");

        // Subsecção: número de sessões por cada hobby (ordenado do maior para o menor)
        sb.append("Sessões por hobby:\n");
        if (sessoesPorHobby.isEmpty()) sb.append("(sem dados)\n");
        else {
            sessoesPorHobby.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> sb.append("• ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));
        }

        // Subsecção: tempo total (em minutos) por cada hobby (ordenado do maior para o menor)
        sb.append("\nTempo por hobby:\n");
        if (minutosPorHobby.isEmpty()) sb.append("(sem dados)\n");
        else {
            minutosPorHobby.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(e -> sb.append("• ").append(e.getKey()).append(": ").append(e.getValue()).append(" min\n"));
        }

        return sb.toString();
    }
 
    /**
     * Exporta os dados do utilizador para um ficheiro PDF completo.
     * Inclui: cabeçalho, cartões de estatísticas, gráficos visuais, tabelas de hobbies e sessões.
     * Implementa paginação automática quando o conteúdo excede os limites da página.
     */
    public static void exportUserDataPdf(User u, File outFile) throws IOException {
        if (u == null) throw new IllegalArgumentException("User null");
        if (outFile == null) throw new IllegalArgumentException("File null");

        // Extrai e prepara dados do utilizador
        List<Hobby> hobbies = safeList(u.getHobbies());
        List<Sessao> sessoes = safeList(u.getSessoes());

        // Calcula estatísticas globais (mesma lógica do TXT)
        int totalSessoes = sessoes.size();
        int totalMinutos = sessoes.stream().mapToInt(Sessao::getDuracaoMinutos).sum();
        int media = totalSessoes == 0 ? 0 : Math.round((float) totalMinutos / totalSessoes);

        // Agrupamentos para estatísticas
        Map<String, Long> sessoesPorHobby = sessoes.stream()
                .collect(Collectors.groupingBy(ExportService::safeHobbyName, Collectors.counting()));

        Map<String, Integer> minutosPorHobby = sessoes.stream()
                .collect(Collectors.groupingBy(ExportService::safeHobbyName, Collectors.summingInt(Sessao::getDuracaoMinutos)));

        String hobbyTop = sessoesPorHobby.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");

        // Gera gráficos de barras como imagens de alta qualidade
        BufferedImage imgSessoes = chartToImage(buildBarChartCount("Sessões por hobby", "Hobby", "Sessões", sessoesPorHobby));
        BufferedImage imgTempo   = chartToImage(buildBarChartInt("Tempo total por hobby", "Hobby", "Minutos", minutosPorHobby));

        // Formatadores de data/hora
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        String exportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Ordena sessões para exibição (mais recentes primeiro)
        List<Sessao> sessoesOrd = sessoes.stream()
                .sorted(Comparator.comparing(Sessao::getData).thenComparing(Sessao::getHora).reversed())
                .toList();

        // Paleta de cores suaves para design moderno
        java.awt.Color COLOR_DARK = new java.awt.Color(15, 23, 42);     // Texto principal
        java.awt.Color COLOR_GRAY = new java.awt.Color(100, 116, 139);  // Texto secundário
        java.awt.Color COLOR_CARD = new java.awt.Color(241, 245, 249);  // Fundo dos cartões
        java.awt.Color COLOR_BORDER = new java.awt.Color(226, 232, 240); // Bordas

        // Inicia criação do documento PDF
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            // Define margens e limites da página
            float margin = 42f;
            float pageW = page.getMediaBox().getWidth();
            float pageH = page.getMediaBox().getHeight();
            float MIN_Y = 60f; // Limite inferior (rodapé)

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = pageH - margin; // Posição vertical inicial (topo da página)

            // Desenha título principal do documento
            y = drawTitle(cs, margin, y, "Export de Dados — Gestor de Hobbies");
            y -= 6;

            // Informações do utilizador e data (linha principal e secundária)
            y = drawSmallLine(cs, margin, y, "Utilizador: " + safe(u.getUsername()), COLOR_DARK, 12);
            y = drawSmallLine(cs, margin, y, "Data do export: " + exportDate, COLOR_GRAY, 10);

            y -= 14; // Espaçamento antes dos cartões


            // Calcula dimensões dos cartões (4 cartões distribuídos horizontalmente)
            float cardH = 52f;
            float gap = 10f;
            float cardW = (pageW - 2 * margin - 3 * gap) / 4f;

            // Desenha 4 cartões com métricas principais
            y = drawStatCards(cs, margin, y, cardW, cardH, gap,
                    COLOR_CARD, COLOR_BORDER, COLOR_DARK, COLOR_GRAY,
                    String.valueOf(totalSessoes), "Sessões",
                    String.valueOf(totalMinutos), "Minutos totais",
                    String.valueOf(media), "Duração média",
                    hobbyTop, "Hobby mais usado");

            y -= 18; // Espaçamento antes dos gráficos


            // Converte BufferedImages em objetos PDFBox para inserção no PDF
            PDImageXObject xImg1 = LosslessFactory.createFromImage(doc, imgSessoes);
            PDImageXObject xImg2 = LosslessFactory.createFromImage(doc, imgTempo);

            // Calcula dimensões dos gráficos (2 gráficos lado a lado)
            float usableW = pageW - 2 * margin;
            float chartW = (usableW - 14) / 2f;
            float chartH = 175;

            // Desenha ambos os gráficos horizontalmente
            cs.drawImage(xImg1, margin, y - chartH, chartW, chartH);
            cs.drawImage(xImg2, margin + chartW + 14, y - chartH, chartW, chartH);

            y -= (chartH + 18); // Atualiza posição vertical


            // Verifica se há espaço suficiente; se não, cria nova página
            if (y < MIN_Y + 120) {
                cs.close();
                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                cs = new PDPageContentStream(doc, page);
                y = page.getMediaBox().getHeight() - margin;
            }

            // Desenha cabeçalho da secção de hobbies
            y = drawSectionHeader(cs, margin, y, "Hobbies (" + hobbies.size() + ")");
            y -= 8;

            // Define larguras das colunas da tabela (Nome, Categoria, Descrição)
            float[] hw = new float[] { 180, 110, (pageW - 2*margin) - 180 - 110 };
            y = drawTableHeader(cs, margin, y, hw, COLOR_DARK, COLOR_CARD, COLOR_BORDER,
                    new String[] {"Nome", "Categoria", "Descrição"});

            // Itera sobre cada hobby e desenha uma linha na tabela
            for (Hobby h : hobbies) {
                // Paginação: se não há espaço, cria nova página e redesenha cabeçalho
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

                // Extrai dados do hobby de forma segura
                String nome = safe(h.getNome());
                String cat = String.valueOf(h.getCategoria());
                String desc = safe(h.getDescricao());

                // Desenha linha da tabela com os dados do hobby
                y = drawTableRow(cs, margin, y, hw, COLOR_DARK, COLOR_BORDER,
                        new String[] { nome, cat, desc });
            }

            y -= 24; // Espaçamento antes da próxima secção


            // Verifica se há espaço suficiente; se não, cria nova página
            if (y < MIN_Y + 120) {
                cs.close();
                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                cs = new PDPageContentStream(doc, page);
                y = page.getMediaBox().getHeight() - margin;
            }

            // Desenha cabeçalho da secção de sessões
            y = drawSectionHeader(cs, margin, y, "Sessões (" + sessoesOrd.size() + ")");
            y -= 8;

            // Define larguras das colunas da tabela (Hobby, Data, Hora, Minutos, Notas)
            float[] sw = new float[] { 120, 80, 60, 50, (pageW - 2*margin) - 120 - 80 - 60 - 50 };
            y = drawTableHeader(cs, margin, y, sw, COLOR_DARK, COLOR_CARD, COLOR_BORDER,
                    new String[] {"Hobby", "Data", "Hora", "Min", "Notas"});

            // Itera sobre cada sessão e desenha uma linha na tabela
            for (Sessao s : sessoesOrd) {
                // Paginação: se não há espaço, cria nova página e redesenha cabeçalho
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

                // Extrai e formata dados da sessão
                String hobby = safeHobbyName(s);
                String data = s.getData().format(df);
                String hora = s.getHora().format(tf);
                String mins = String.valueOf(s.getDuracaoMinutos());
                String notas = safe(s.getNotas());

                // Desenha linha da tabela com os dados da sessão
                y = drawTableRow(cs, margin, y, sw, COLOR_DARK, COLOR_BORDER,
                        new String[] { hobby, data, hora, mins, notas });
            }

            // Finaliza stream e guarda documento no ficheiro
            cs.close();
            doc.save(outFile);
        }
    }



    /**
     * Constrói gráfico de barras para dados do tipo Long (ex: contagem de sessões).
     * Configura eixos, título, remove legenda e ordena barras por valor decrescente.
     */
    private static BarChart<String, Number> buildBarChartCount(String title, String xLabel, String yLabel, Map<String, Long> data) {
        // Cria eixos do gráfico
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        x.setLabel(xLabel);
        y.setLabel(yLabel);

        // Configura gráfico de barras
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle(title);
        chart.setLegendVisible(false); // Remove legenda (não necessária)
        chart.setAnimated(false);      // Desativa animações (mais rápido)

        // Cria série de dados e ordena por valor (maior para menor)
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        data.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        chart.getData().add(s);

        return chart;
    }

    /**
     * Constrói gráfico de barras para dados do tipo Integer (ex: minutos totais).
     * Idêntico ao buildBarChartCount mas aceita Map<String, Integer>.
     */
    private static BarChart<String, Number> buildBarChartInt(String title, String xLabel, String yLabel, Map<String, Integer> data) {
        // Cria eixos do gráfico
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        x.setLabel(xLabel);
        y.setLabel(yLabel);

        // Configura gráfico de barras
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        // Cria série de dados e ordena por valor (maior para menor)
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        data.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        chart.getData().add(s);

        return chart;
    }

    /**
     * Converte um gráfico JavaFX em BufferedImage de alta qualidade.
     * Aplica escala 2x para melhorar nitidez no PDF final.
     */
    private static BufferedImage chartToImage(BarChart<String, Number> chart) {
        // Define tamanho fixo para garantir renderização adequada
        chart.setMinSize(800, 420);
        chart.setPrefSize(800, 420);
        chart.setMaxSize(800, 420);

        // Cria cena JavaFX para renderização
        Group root = new Group(chart);
        Scene scene = new Scene(root);

        // Aplica CSS e layout antes da captura
        root.applyCss();
        root.layout();
        chart.applyCss();
        chart.layout();

        // Configura parâmetros de snapshot com escala 2x (imagem mais nítida)
        SnapshotParameters sp = new SnapshotParameters();
        sp.setTransform(Transform.scale(2, 2));

        // Captura gráfico como imagem e converte para BufferedImage
        WritableImage fxImg = chart.snapshot(sp, null);
        return SwingFXUtils.fromFXImage(fxImg, null);
    }


    /**
     * Desenha título principal no PDF (fonte grande e bold).
     * Retorna nova posição vertical após o título.
     */
    private static float drawTitle(PDPageContentStream cs, float x, float y, String title) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
        cs.newLineAtOffset(x, y);
        cs.showText(title);
        cs.endText();
        return y - 26; // Retorna posição ajustada (26 pixels abaixo)
    }

    /**
     * Desenha linha de texto pequena (informações secundárias).
     * Permite personalizar cor e tamanho da fonte.
     */
    private static float drawSmallLine(PDPageContentStream cs, float x, float y, String text, java.awt.Color c, int size) throws IOException {
        cs.beginText();
        cs.setNonStrokingColor(c);
        cs.setFont(PDType1Font.HELVETICA, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        cs.setNonStrokingColor(java.awt.Color.BLACK); // Restaura cor padrão
        return y - (size + 4); // Retorna posição ajustada
    }

    /**
     * Desenha cabeçalho de secção (ex: "Hobbies", "Sessões").
     * Fonte média bold para destacar início de nova secção.
     */
    private static float drawSectionHeader(PDPageContentStream cs, float x, float y, String title) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(x, y);
        cs.showText(title);
        cs.endText();
        return y - 16; // Retorna posição ajustada
    }

    /**
     * Desenha 4 cartões de estatísticas horizontalmente.
     * Cada cartão tem: valor grande (título) + descrição pequena (subtítulo).
     * Recebe arrays de valores e labels para os 4 cartões.
     */
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

        // Desenha cada um dos 4 cartões
        for (int i = 0; i < 4; i++) {
            float cx = x + i * (cardW + gap); // Posição horizontal do cartão
            float cy = y - cardH;             // Posição vertical do cartão

            // Desenha fundo do cartão
            cs.setNonStrokingColor(cardBg);
            cs.addRect(cx, cy, cardW, cardH);
            cs.fill();

            // Desenha borda do cartão
            cs.setStrokingColor(border);
            cs.addRect(cx, cy, cardW, cardH);
            cs.stroke();

            // Desenha valor principal (linha superior, fonte grande)
            cs.beginText();
            cs.setNonStrokingColor(titleColor);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(cx + 10, cy + 30);
            cs.showText(trimTo(vals[i], 18)); // Trunca se necessário
            cs.endText();

            // Desenha label/descrição (linha inferior, fonte pequena)
            cs.beginText();
            cs.setNonStrokingColor(subColor);
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.newLineAtOffset(cx + 10, cy + 12);
            cs.showText(trimTo(labs[i], 22)); // Trunca se necessário
            cs.endText();

            // Restaura cores padrão após cada cartão
            cs.setNonStrokingColor(java.awt.Color.BLACK);
            cs.setStrokingColor(java.awt.Color.BLACK);
        }

        return y - cardH; // Retorna posição ajustada (após os cartões)
    }

    /**
     * Desenha cabeçalho de tabela (linha com fundo colorido e texto bold).
     * Recebe larguras de colunas e labels dos cabeçalhos.
     */
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

        // Desenha fundo do cabeçalho
        cs.setNonStrokingColor(bg);
        cs.addRect(x, cy, sum(widths), rowH);
        cs.fill();

        // Desenha borda do cabeçalho
        cs.setStrokingColor(border);
        cs.addRect(x, cy, sum(widths), rowH);
        cs.stroke();

        // Desenha texto de cada coluna
        cs.setNonStrokingColor(textColor);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10);

        float cx = x;
        for (int i = 0; i < cols.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(cx + 6, cy + 6); // Padding interno
            cs.showText(trimTo(cols[i], (int)(widths[i] / 5))); // Trunca conforme largura
            cs.endText();
            cx += widths[i]; // Avança para próxima coluna
        }

        cs.setNonStrokingColor(java.awt.Color.BLACK); // Restaura cor padrão
        return cy; // Retorna posição ajustada
    }

    /**
     * Desenha linha de dados numa tabela.
     * Desenha bordas e linhas verticais entre colunas, depois preenche com texto.
     */
    private static float drawTableRow(
            PDPageContentStream cs,
            float x, float y,
            float[] widths,
            java.awt.Color textColor,
            java.awt.Color border,
            String[] values) throws IOException {

        float rowH = 18f;
        float cy = y - rowH;

        // Desenha borda externa da linha
        cs.setStrokingColor(border);
        cs.addRect(x, cy, sum(widths), rowH);
        cs.stroke();

        // Desenha linhas verticais entre colunas
        float cx = x;
        for (float w : widths) {
            cs.moveTo(cx, cy);
            cs.lineTo(cx, cy + rowH);
            cs.stroke();
            cx += w;
        }
        // Linha vertical final (borda direita)
        cs.moveTo(x + sum(widths), cy);
        cs.lineTo(x + sum(widths), cy + rowH);
        cs.stroke();

        // Preenche cada célula com texto
        cs.setNonStrokingColor(textColor);
        cs.setFont(PDType1Font.HELVETICA, 9);

        cx = x;
        for (int i = 0; i < values.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(cx + 6, cy + 6); // Padding interno
            cs.showText(trimTo(values[i], (int)(widths[i] / 5))); // Trunca conforme largura
            cs.endText();
            cx += widths[i]; // Avança para próxima coluna
        }

        // Restaura cores padrão
        cs.setNonStrokingColor(java.awt.Color.BLACK);
        cs.setStrokingColor(java.awt.Color.BLACK);
        return cy; // Retorna posição ajustada
    }

    /**
     * Soma todos os elementos de um array de floats.
     * Usada para calcular largura total de tabelas.
     */
    private static float sum(float[] a) {
        float s = 0;
        for (float v : a) s += v;
        return s;
    }

    /**
     * Trunca string para caber num número máximo de caracteres.
     * Remove quebras de linha e adiciona "..." se exceder o limite.
     */
    private static String trimTo(String s, int maxChars) {
        if (s == null) return "";
        String t = s.replace("\n", " ").replace("\r", " ").trim();
        if (t.length() <= maxChars) return t;
        if (maxChars <= 3) return t.substring(0, Math.max(0, maxChars));
        return t.substring(0, maxChars - 3) + "...";
    }



    /**
     * Retorna lista vazia se a lista recebida for null.
     * Protege contra NullPointerException em iterações.
     */
    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    /**
     * Retorna string vazia se a string recebida for null.
     * Garante que campos de texto nunca causem NPE.
     */
    private static String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * Extrai nome do hobby de uma sessão de forma segura.
     * Retorna "Desconhecido" se hobby ou nome forem null/inválidos.
     */
    private static String safeHobbyName(Sessao s) {
        try {
            if (s.getHobby() != null && s.getHobby().getNome() != null) return s.getHobby().getNome();
        } catch (Exception ignored) {}
        return "Desconhecido";
    }
}
