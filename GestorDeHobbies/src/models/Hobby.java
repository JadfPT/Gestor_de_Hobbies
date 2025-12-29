/*
 * Propósito geral: representa um hobby com nome, categoria e descrição, pronto
 * para ser serializado e guardado com o resto dos dados da aplicação.
 * Observações: campos simples com getters/setters; toString devolve o nome para
 * exibição fácil em listas/combos.
 */
package models;

import java.io.Serializable;

public class Hobby implements Serializable {

    // Atributos principais do hobby
    private String nome;
    private Categoria categoria;
    private String descricao;

    // Construtor que inicializa todos os campos obrigatórios
    public Hobby(String nome, Categoria categoria, String descricao) {
        this.nome = nome;
        this.categoria = categoria;
        this.descricao = descricao;
    }

    // Getters e setters padrão
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

    // Facilita a apresentação em componentes de UI
    @Override
    public String toString() {
        return nome;
    }
}
