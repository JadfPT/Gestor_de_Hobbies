/*
 * Propósito geral: gerir o ecrã de definições/preferências do utilizador, incluindo
 * alteração de password, tema dark/light, formatos de data/hora, cor dos gráficos,
 * exportação de dados (TXT/PDF), reset de dados e logout.
 * Observações: valida ações destrutivas (apagar conta, limpar dados) com confirmações;
 * aplica preferências em tempo real (tema, formatos); utiliza dialogs customizados
 * que herdam o CSS da Scene principal para manter consistência visual; persiste
 * preferências no AppState e PreferencesStore.
 */
package ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.Hobby;
import models.Sessao;
import models.User;
import services.AppState;
import services.ExportService;
import ui.App;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class SettingsController {
=
    
    // Identificação do utilizador
    @FXML
    private Label lblUsername;

    // Alteração de password
    @FXML
    private PasswordField pfCurrent;
    @FXML
    private PasswordField pfNew;
    @FXML
    private PasswordField pfConfirm;
    @FXML
    private Label lblPassMsg;

    // Apagar conta (ainda não implementado)
    @FXML
    private TextField txtDeleteConfirm;
    @FXML
    private Button btnDeleteAccount;
    @FXML
    private Label lblDeleteMsg;

    // Limpar todos os dados (hobbies e sessões)
    @FXML
    private TextField txtResetConfirm;
    @FXML
    private Button btnResetData;
    @FXML
    private Label lblResetMsg;

    // Exportação de dados
    @FXML
    private Button btnExportData;

    // Preferências de tema e aparência
    @FXML
    private CheckBox chkDarkMode;
    @FXML
    private Label lblThemeMsg;

    @FXML
    private Label lblGlobalMsg;

    // Preferências de formato de data/hora
    @FXML
    private ComboBox<String> cmbTimeFormat;
    @FXML
    private ComboBox<String> cmbDateFormat;

    // Personalização de cor dos gráficos
    @FXML
    private ColorPicker colorChart;

    /**
     * Inicializa o controlador: carrega preferências do utilizador, configura listeners
     * para validação de ações destrutivas e preenche combos com valores atuais.
     */
    @FXML
    public void initialize() {
        // Mostra username do utilizador atual
        User u = AppState.getInstance().getCurrentUser();
        lblUsername.setText(u != null ? u.getUsername() : "(sem sessão)");

        // Listener para ativar botão de apagar conta apenas quando o texto for "APAGAR"
        txtDeleteConfirm.textProperty().addListener((obs, o, n) -> {
            boolean ok = "APAGAR".equalsIgnoreCase(n.trim());
            btnDeleteAccount.setDisable(!ok);
            lblDeleteMsg.setText("");
        });

        // Listener para ativar botão de reset apenas quando o texto for "LIMPAR"
        txtResetConfirm.textProperty().addListener((obs, o, n) -> {
            boolean ok = "LIMPAR".equalsIgnoreCase(n.trim());
            btnResetData.setDisable(!ok);
            lblResetMsg.setText("");
        });

        // Marca checkbox do dark mode conforme preferência atual (runLater para garantir que UI está pronta)
        Platform.runLater(() -> chkDarkMode.setSelected(App.isDarkModeEnabled()));

        // Inicializa combo de formato de hora (24h ou 12h)
        if (cmbTimeFormat != null) {
            cmbTimeFormat.getItems().setAll("24h", "12h");
            cmbTimeFormat.setValue(App.isUse24HourTime() ? "24h" : "12h");
            cmbTimeFormat.setOnAction(e -> onChangeTimeFormat());
        }

        // Inicializa combo de formato de data (diferentes padrões)
        if (cmbDateFormat != null) {
            cmbDateFormat.getItems().setAll("yyyy-MM-dd", "MM-dd-yyyy", "dd-MM-yyyy");
            cmbDateFormat.setValue(App.getDateFormatPattern());
            cmbDateFormat.setOnAction(e -> onChangeDateFormat());
        }

        // Inicializa color picker com a cor atual dos gráficos
        if (colorChart != null) {
            colorChart.setOnAction(e -> onChangeChartColor());
            try {
                colorChart.setValue(javafx.scene.paint.Color.web(App.getChartColor()));
            } catch (Exception ignored) {
                // Ignora se a cor não for válida
            }
        }
    }


    
    /**
     * Valida e altera a password do utilizador.
     * Validações: campos preenchidos, password atual correta, confirmação coincide, mínimo 4 caracteres.
     */
    @FXML
    private void onChangePassword() {
        lblPassMsg.setText("");

        // Verifica se existe utilizador autenticado
        User u = AppState.getInstance().getCurrentUser();
        if (u == null) {
            lblPassMsg.setText("Sem utilizador autenticado.");
            return;
        }

        // Recolhe dados dos campos
        String current = pfCurrent.getText();
        String n1 = pfNew.getText();
        String n2 = pfConfirm.getText();

        // Validação: campos não podem estar vazios
        if (current.isBlank() || n1.isBlank() || n2.isBlank()) {
            lblPassMsg.setText("Preenche todos os campos.");
            return;
        }

        // Validação: password atual deve estar correta
        if (!u.getPassword().equals(current)) {
            lblPassMsg.setText("Password atual incorreta.");
            return;
        }

        // Validação: nova password e confirmação devem coincidir
        if (!n1.equals(n2)) {
            lblPassMsg.setText("A confirmação não coincide.");
            return;
        }

        // Validação: password deve ter comprimento mínimo
        if (n1.length() < 4) {
            lblPassMsg.setText("A nova password deve ter pelo menos 4 caracteres.");
            return;
        }

        // Atualiza password e guarda estado
        u.setPassword(n1);
        AppState.getInstance().guardar();

        // Limpa campos após sucesso
        pfCurrent.clear();
        pfNew.clear();
        pfConfirm.clear();

        lblPassMsg.setText("Password alterada com sucesso.");
    }


    
    /**
     * Alterna entre modo claro e escuro.
     * Aplica imediatamente e persiste a preferência no utilizador e nas preferências globais.
     */
    @FXML
    private void onToggleTheme() {
        lblThemeMsg.setText("");

        Scene scene = getSceneSafe();
        if (scene == null) {
            lblThemeMsg.setText("Scene ainda não está pronta.");
            return;
        }

        // Aplica tema conforme estado do checkbox
        boolean enable = chkDarkMode.isSelected();
        App.setDarkModeEnabled(enable);

        // Guarda preferência no perfil do utilizador
        var u = AppState.getInstance().getCurrentUser();
        if (u != null) {
            u.setPrefDarkMode(enable);
            AppState.getInstance().guardar();
        }

        // Persiste nas preferências globais da aplicação
        data.PreferencesStore.saveAppPrefs();
    }

    /**
     * Altera formato de hora entre 24h e 12h (AM/PM).
     * Persiste a escolha no utilizador e preferências globais.
     */
    @FXML
    private void onChangeTimeFormat() {
        String sel = cmbTimeFormat.getValue();
        App.setUse24HourTime("24h".equals(sel));

        // Guarda preferência no perfil do utilizador
        var u = AppState.getInstance().getCurrentUser();
        if (u != null) {
            u.setPrefUse24HourTime(App.isUse24HourTime());
            AppState.getInstance().guardar();
        }

        data.PreferencesStore.saveAppPrefs();
    }

    /**
     * Altera o padrão de formato de data (ex: yyyy-MM-dd, dd-MM-yyyy).
     * Persiste a escolha no utilizador e preferências globais.
     */
    @FXML
    private void onChangeDateFormat() {
        String fmt = cmbDateFormat.getValue();
        App.setDateFormatPattern(fmt);

        // Guarda preferência no perfil do utilizador
        var u = AppState.getInstance().getCurrentUser();
        if (u != null) {
            u.setPrefDateFormat(fmt);
            AppState.getInstance().guardar();
        }

        data.PreferencesStore.saveAppPrefs();
    }

    /**
     * Altera a cor usada nos gráficos da aplicação.
     * Converte cor JavaFX para formato hexadecimal e persiste.
     */
    @FXML
    private void onChangeChartColor() {
        var c = colorChart.getValue();
        if (c != null) {
            // Converte Color JavaFX para string hexadecimal (ex: #3B82F6)
            String hex = String.format("#%02X%02X%02X",
                    (int) (c.getRed() * 255),
                    (int) (c.getGreen() * 255),
                    (int) (c.getBlue() * 255));

            App.setChartColor(hex);

            // Guarda preferência no perfil do utilizador
            var u = AppState.getInstance().getCurrentUser();
            if (u != null) {
                u.setPrefChartColor(hex);
                AppState.getInstance().guardar();
            }

            data.PreferencesStore.saveAppPrefs();
        }
    }

    
    /**
     * Termina sessão do utilizador atual e volta ao ecrã de login.
     * Pede confirmação antes de sair.
     */
    @FXML
    private void onLogout() {
        Optional<ButtonType> res = confirm(
                "Terminar sessão",
                "Queres terminar a sessão?",
                "Vais voltar ao ecrã de login."
        );

        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return;
        }

        // Muda para a view de login
        try {
            App.setRoot("views/LoginView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            lblGlobalMsg.setText("Erro ao terminar sessão.");
        }
    }

    /**
     * Apaga conta do utilizador (funcionalidade ainda não implementada).
     */
    @FXML
    private void onDeleteAccount() {
        lblDeleteMsg.setText("Ainda não implementado.");
    }

    /**
     * Limpa TODOS os hobbies e sessões do utilizador.
     * Pede confirmação dupla: dialog + texto "LIMPAR".
     * Ação irreversível.
     */
    @FXML
    private void onResetData() {
        lblResetMsg.setText("");

        User u = AppState.getInstance().getCurrentUser();
        if (u == null) {
            lblResetMsg.setText("Sem utilizador autenticado.");
            return;
        }

        // Pede confirmação ao utilizador
        Optional<ButtonType> res = confirm(
                "Limpar dados",
                "Isto vai apagar TODOS os hobbies e sessões.",
                "Esta ação é irreversível."
        );
        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return;
        }

        // Guarda contagens para feedback
        int h = u.getHobbies().size();
        int s = u.getSessoes().size();

        // Remove todos os hobbies (itera sobre cópia para evitar ConcurrentModificationException)
        for (Hobby hb : new ArrayList<>(u.getHobbies())) {
            u.removerHobby(hb);
        }
        // Remove todas as sessões
        for (Sessao se : new ArrayList<>(u.getSessoes())) {
            u.removerSessao(se);
        }

        // Persiste alterações
        AppState.getInstance().guardar();

        // Limpa campo de confirmação e desativa botão
        txtResetConfirm.clear();
        btnResetData.setDisable(true);

        lblResetMsg.setText("OK: removidos " + h + " hobbies e " + s + " sessões.");
    }


    
    /**
     * Exporta todos os dados do utilizador (hobbies, sessões, estatísticas).
     * Fluxo: escolher formato (dialog custom) → escolher destino (FileChooser) → exportar.
     * Formatos suportados: TXT (texto simples) e PDF (relatório com gráficos).
     */
    @FXML
    private void onExportData() {
        lblResetMsg.setText("");

        User u = AppState.getInstance().getCurrentUser();
        if (u == null) {
            lblResetMsg.setText("Sem utilizador autenticado.");
            return;
        }

        // Passo 1: Utilizador escolhe formato (TXT ou PDF) via dialog customizado
        Optional<String> formatoOpt = escolherFormatoExportacao();
        if (formatoOpt.isEmpty()) {
            return; // Utilizador cancelou
        }

        String formato = formatoOpt.get(); // TXT | PDF

        // Passo 2: Configura FileChooser conforme formato escolhido
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar exportação");

        // Define nome de ficheiro base (usando username)
        String safeUser = (u.getUsername() == null || u.getUsername().isBlank())
                ? "user"
                : u.getUsername().trim();

        // Configura extensão e filtros conforme formato
        if ("PDF".equals(formato)) {
            fc.setInitialFileName("export_" + safeUser + ".pdf");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        } else {
            fc.setInitialFileName("export_" + safeUser + ".txt");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto (*.txt)", "*.txt"));
        }

        // Passo 3: Abre dialog para escolher destino do ficheiro
        Scene scene = getSceneSafe();
        if (scene == null || scene.getWindow() == null) {
            lblResetMsg.setText("Não foi possível abrir o selector de ficheiros.");
            return;
        }

        File destino = fc.showSaveDialog(scene.getWindow());
        if (destino == null) {
            return; // Utilizador cancelou
        }

        // Passo 4: Executa exportação usando ExportService
        try {
            if ("PDF".equals(formato)) {
                ExportService.exportUserDataPdf(u, destino);
            } else {
                ExportService.exportUserDataTxt(u, destino);
            }
            lblResetMsg.setText("Export concluído: " + destino.getName());
        } catch (Exception e) {
            e.printStackTrace();
            lblResetMsg.setText("Falha ao exportar dados.");
        }
    }

    /**
     * Dialog customizado para escolher formato de exportação (TXT ou PDF).
     * Herda CSS da Scene principal para manter consistência visual entre temas claro/escuro.
     * Retorna formato escolhido ou vazio se cancelado.
     */
    private Optional<String> escolherFormatoExportacao() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Exportar dados");

        // Define botões do dialog
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnOk = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCancelar, btnOk);

        //Construção da UI
        Label title = new Label("Escolhe o formato de exportação");
        title.getStyleClass().add("export-title");

        Label sub = new Label("Exporta os teus dados em TXT (texto) ou PDF (relatório).");
        sub.getStyleClass().add("export-sub");

        // Combo com opções de formato
        ComboBox<String> cmb = new ComboBox<>();
        cmb.getItems().addAll("TXT", "PDF");
        cmb.getSelectionModel().selectFirst();

        // Aplica estilos inline conforme tema (dark/light) para garantir aparência consistente
        boolean darkMode = App.isDarkModeEnabled();
        if (darkMode) {
            title.setStyle("-fx-font-size:22px; -fx-font-weight:800; -fx-text-fill:#e5e7eb;");
            cmb.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-border-color: rgba(255,255,255,0.12); -fx-text-fill: #e5e7eb; -fx-background-radius:12; -fx-padding:8 12; -fx-pref-height:36;");
        } else {
            title.setStyle("-fx-font-size:22px; -fx-font-weight:800; -fx-text-fill:#0f172a;");
            cmb.setStyle("-fx-background-color: rgba(15,23,42,0.04); -fx-border-color: rgba(15,23,42,0.06); -fx-text-fill: #0f172a; -fx-background-radius:12; -fx-padding:8 12; -fx-pref-height:36;");
        }

        // Constrói linha com label e combo
        Label lbl = new Label("Formato:");
        HBox row = new HBox(12, lbl, cmb);
        row.setStyle("-fx-alignment: center-left;");

        // Agrupa tudo num card vertical
        VBox card = new VBox(14, title, sub, row);
        card.getStyleClass().add("export-card");

        dialog.getDialogPane().setContent(card);

        // Herda CSS da Scene principal para manter tema consistente
        Scene scene = getSceneSafe();
        if (scene != null) {
            dialog.getDialogPane().getStylesheets().addAll(scene.getStylesheets());
        }

        // Adiciona classes CSS para aplicar estilos (light/dark)
        dialog.getDialogPane().getStyleClass().add("export-dialog");
        if (App.isDarkModeEnabled()) {
            dialog.getDialogPane().getStyleClass().add("dark");
        }

        // Define largura mínima e evita quebra de linha em título/subtítulo
        dialog.getDialogPane().setPrefWidth(400);
        title.setWrapText(false);
        sub.setWrapText(false);

        // Desativa botão OK se nenhum formato estiver selecionado (validação)
        Node okBtn = dialog.getDialogPane().lookupButton(btnOk);
        okBtn.disableProperty().bind(cmb.valueProperty().isNull());
        
        // Aplica estilos personalizados aos botões conforme tema
        Node cancelBtn = dialog.getDialogPane().lookupButton(btnCancelar);
            if (okBtn != null) {
                okBtn.getStyleClass().add("export-primary-button");
                boolean dark = App.isDarkModeEnabled();
                // Botão primário: gradiente azul com texto branco
                    String primaryStyle = dark
                        ? "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #60a5fa, #3b82f6); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 18;"
                        : "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3b82f6, #2563eb); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 18;";
                okBtn.setStyle(primaryStyle);
            }
            if (cancelBtn != null) {
                cancelBtn.getStyleClass().add("export-cancel-button");
                boolean dark = App.isDarkModeEnabled();
                // Botão cancelar: estilo subtil com borda
                String cancelStyle = dark
                        ? "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #e5e7eb; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width:1; -fx-background-radius:12; -fx-padding:8 16;"
                        : "-fx-background-color: rgba(15,23,42,0.04); -fx-text-fill: #0f172a; -fx-border-color: rgba(15,23,42,0.06); -fx-border-width:1; -fx-background-radius:12; -fx-padding:8 16;";
                cancelBtn.setStyle(cancelStyle);
            }

        // Define conversor de resultado: retorna formato se OK, null se cancelar
        dialog.setResultConverter(bt -> bt == btnOk ? cmb.getValue() : null);
        return dialog.showAndWait();
    }

    
    /**
     * Obtém a Scene atual de forma segura (null-safe).
     * Usado para obter Window para FileChoosers e Dialogs.
     */
    private Scene getSceneSafe() {
        return lblUsername != null ? lblUsername.getScene() : null;
    }

    /**
     * Cria dialog de confirmação simples com título, cabeçalho e conteúdo.
     * Aplica tema dark se ativo. Retorna escolha do utilizador (OK ou CANCEL).
     */
    private Optional<ButtonType> confirm(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

        // Aplica tema dark ao dialog se estiver ativo
        DialogPane pane = a.getDialogPane();
        if (pane != null && App.isDarkModeEnabled()) {
            pane.getStyleClass().add("dark");
        }
        return a.showAndWait();
    }
}
