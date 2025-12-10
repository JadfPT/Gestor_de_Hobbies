package models;

import java.io.Serializable;

public class Hobby implements Serializable {

    private String nome;
    private Categoria categoria;
    private String descricao;

    public Hobby(String nome, Categoria categoria, String descricao) {
        this.nome = nome;
        this.categoria = categoria;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return nome;
    }
}
