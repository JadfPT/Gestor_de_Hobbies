package services;

import data.Persistencia;
import models.AppData;
import models.Hobby;
import models.User;

public class AppState {

    private static final AppState INSTANCE = new AppState();

    private AppData dados;
    private User currentUser;

    private AppState() {
        dados = Persistencia.carregar();
    }

    public static AppState getInstance() {
        return INSTANCE;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean login(String username, String password) {
        User u = dados.getUser(username);
        if (u != null && u.getPassword().equals(password)) {
            currentUser = u;
            // Aplicar preferências guardadas do utilizador
            try {
                ui.App.setDarkModeEnabled(u.isPrefDarkMode());
                ui.App.setUse24HourTime(u.isPrefUse24HourTime());
                String fmt = (u.getPrefDateFormat() != null && !u.getPrefDateFormat().isBlank()) ? u.getPrefDateFormat() : ui.App.getDateFormatPattern();
                ui.App.setDateFormatPattern(fmt);
                String color = (u.getPrefChartColor() != null && !u.getPrefChartColor().isBlank()) ? u.getPrefChartColor() : ui.App.getChartColor();
                ui.App.setChartColor(color);
            } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

public boolean registar(String username, String password) {
    if (dados.exists(username)) {
        return false;
    }

    User u = new User(username, password);

    // Antes: adicionar hobbies pré-definidos aqui
    // for (Hobby h : PredefinedHobbies.getDefaults()) {
    //     u.adicionarHobby(h);
    // }

    // Inicializar preferências com as atuais da app
    u.setPrefDarkMode(ui.App.isDarkModeEnabled());
    u.setPrefUse24HourTime(ui.App.isUse24HourTime());
    u.setPrefDateFormat(ui.App.getDateFormatPattern());
    u.setPrefChartColor(ui.App.getChartColor());

    dados.addUser(u);
    currentUser = u;
    guardar();
    return true;
}


    public void guardar() {
        try {
            Persistencia.gravar(dados);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
