package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Sessao implements Serializable {

    private Hobby hobby;
    private LocalDate data;
    private LocalTime hora;
    private int duracaoMinutos;
    private String notas;

    public Sessao(Hobby hobby, LocalDate data, LocalTime hora, int duracaoMinutos, String notas) {
        this.hobby = hobby;
        this.data = data;
        this.hora = hora;
        this.duracaoMinutos = duracaoMinutos;
        this.notas = notas;
    }

    public Hobby getHobby() {
        return hobby;
    }
    
    public void setHobby(Hobby hobby) {
        this.hobby = hobby;
    }

    public LocalDate getData() {
        return data;
    }
    
    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHora() {
        return hora;
    }
    
    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }
    
    public void setDuracaoMinutos(int duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public String getNotas() {
        return notas;
    }
    
    public void setNotas(String notas) {
        this.notas = notas;
    }
}
