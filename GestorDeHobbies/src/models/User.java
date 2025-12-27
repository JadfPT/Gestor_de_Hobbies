package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    private final List<Hobby> hobbies = new ArrayList<>();
    private final List<Sessao> sessoes = new ArrayList<>();

    // Preferências do utilizador
    private boolean prefDarkMode = false;
    private boolean prefUse24HourTime = true;
    private String prefDateFormat = "yyyy-MM-dd";
    private String prefChartColor = "#f97316";

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Hobby> getHobbies() {
        return Collections.unmodifiableList(hobbies);
    }

    public void adicionarHobby(Hobby hobby) {
        if (hobby != null) {
            hobbies.add(hobby);
        }
    }

    public void removerHobby(Hobby hobby) {
        hobbies.remove(hobby);
    }

    public List<Sessao> getSessoes() {
        return Collections.unmodifiableList(sessoes);
    }

    public void registarSessao(Sessao sessao) {
        if (sessao != null) {
            sessoes.add(sessao);
        }
    }

    public void removerSessao(Sessao sessao) {
        sessoes.remove(sessao);
    }

    // Preferências
    public boolean isPrefDarkMode() { return prefDarkMode; }
    public void setPrefDarkMode(boolean prefDarkMode) { this.prefDarkMode = prefDarkMode; }

    public boolean isPrefUse24HourTime() { return prefUse24HourTime; }
    public void setPrefUse24HourTime(boolean prefUse24HourTime) { this.prefUse24HourTime = prefUse24HourTime; }

    public String getPrefDateFormat() { return prefDateFormat; }
    public void setPrefDateFormat(String prefDateFormat) { this.prefDateFormat = prefDateFormat; }

    public String getPrefChartColor() { return prefChartColor; }
    public void setPrefChartColor(String prefChartColor) { this.prefChartColor = prefChartColor; }

    @Override
    public String toString() {
        return username;
    }
}
