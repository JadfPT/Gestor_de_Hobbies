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
