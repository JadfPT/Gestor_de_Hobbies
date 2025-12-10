/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;

/**
 *
 * @author João
 */
public class Sessao implements Serializable {
    
    private LocalDate data;
    private Duration duracao;
    private String notas;
    
        public Sessao(LocalDate data, Duration duracao, String notas) {
        this.data = data;
        this.duracao = duracao;
        this.notas = notas;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Duration getDuracao() {
        return duracao;
    }

    public void setDuracao(Duration duracao) {
        this.duracao = duracao;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
