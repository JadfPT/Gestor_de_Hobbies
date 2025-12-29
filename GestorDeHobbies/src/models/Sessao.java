/*
 * Propósito geral: representa uma sessão de prática de um hobby, com data, hora,
 * duração e notas, pronta a ser serializada com os restantes dados.
 * Observações: todos os campos são mutáveis via getters/setters; usa tipos java.time
 * para data e hora e guarda a duração em minutos.
 */
package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Sessao implements Serializable {

    // Dados principais da sessão
    private Hobby hobby;
    private LocalDate data;
    private LocalTime hora;
    private int duracaoMinutos;
    private String notas;

    // Construtor completo para inicializar todos os campos
    public Sessao(Hobby hobby, LocalDate data, LocalTime hora, int duracaoMinutos, String notas) {
        this.hobby = hobby;
        this.data = data;
        this.hora = hora;
        this.duracaoMinutos = duracaoMinutos;
        this.notas = notas;
    }

    // Getters e setters para permitir edição posterior
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
