/*
 * Propósito geral: gerir o estado global da aplicação (dados persistidos e utilizador
 * autenticado), fornecendo operações de login, registo e gravação em disco.
 * Observações: singleton simples; carrega dados na inicialização; aplica preferências
 * do utilizador no login; falhas de persistência são registadas mas não propagadas.
 */
package services;

import data.Persistencia;
import models.AppData;
import models.User;

public class AppState {

    // Instância única (singleton) para partilhar o estado em toda a aplicação
    private static final AppState INSTANCE = new AppState();

    // Dados persistidos e utilizador atualmente autenticado
    private AppData dados;
    private User currentUser;

    // Construtor privado carrega dados do disco ao arrancar
    private AppState() {
        dados = Persistencia.carregar();
    }

    // Acesso à instância singleton
    public static AppState getInstance() {
        return INSTANCE;
    }

    // Obtém o utilizador atualmente autenticado
    public User getCurrentUser() {
        return currentUser;
    }

    // Tenta autenticar: valida password e aplica preferências guardadas do utilizador
    public boolean login(String username, String password) {
        User u = dados.getUser(username);
        if (u != null && u.getPassword().equals(password)) {
            currentUser = u;
            try {
                ui.App.setDarkModeEnabled(u.isPrefDarkMode());
                ui.App.setUse24HourTime(u.isPrefUse24HourTime());
                String fmt = (u.getPrefDateFormat() != null && !u.getPrefDateFormat().isBlank()) ? u.getPrefDateFormat() : ui.App.getDateFormatPattern();
                ui.App.setDateFormatPattern(fmt);
                String color = (u.getPrefChartColor() != null && !u.getPrefChartColor().isBlank()) ? u.getPrefChartColor() : ui.App.getChartColor();
                ui.App.setChartColor(color);
            } catch (Exception ignored) {} // mantém a sessão mesmo que preferências falhem
            return true;
        }
        return false;
    }

    // Regista um novo utilizador se o username não existir; inicializa preferências atuais da app
    public boolean registar(String username, String password) {
        if (dados.exists(username)) {
            return false;
        }

        User u = new User(username, password);

        // Copia as preferências globais atuais como ponto de partida
        u.setPrefDarkMode(ui.App.isDarkModeEnabled());
        u.setPrefUse24HourTime(ui.App.isUse24HourTime());
        u.setPrefDateFormat(ui.App.getDateFormatPattern());
        u.setPrefChartColor(ui.App.getChartColor());

        dados.addUser(u);
        currentUser = u;
        guardar();
        return true;
    }

    // Persiste o estado atual em disco
    public void guardar() {
        try {
            Persistencia.gravar(dados);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
