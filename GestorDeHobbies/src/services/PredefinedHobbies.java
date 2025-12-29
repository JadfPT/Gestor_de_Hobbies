/*
 * Propósito geral: fornecer uma lista pré-definida de hobbies com nome, categoria
 * e descrição, para popular a aplicação rapidamente ou sugerir opções ao utilizador.
 * Observações: inclui uma entrada "Outro (personalizado)" com descrição vazia para
 * permitir criação livre; enum facilita uso direto em combos/listas.
 */
package services;

import models.Categoria;

// Enum com hobbies pré-configurados
public enum PredefinedHobbies {
    LEITURA("Leitura", Categoria.LEITURA, "Ler livros, mangas, etc."),
    DESENHAR("Desenhar / Pintar", Categoria.ARTES, "Artes visuais, ilustração, pintura."),
    INSTRUMENTO("Tocar um instrumento", Categoria.MUSICA, "Prática de instrumento musical."),
    GAMING("Gaming", Categoria.JOGOS, "Jogos de PC, consola ou mobile."),
    VER_FILMES("Ver filmes e séries", Categoria.FILMES, "Cinema, streaming, séries."),
    OUVIR_MUSICA("Ouvir música", Categoria.MUSICA, "Playlists, álbuns."),
    ANDAR("Andar", Categoria.DESPORTO, "Caminhadas, passeios."),
    CORRER("Correr", Categoria.DESPORTO, "Corrida, jogging."),
    FOTOGRAFIA("Fotografia", Categoria.ARTES, "Fotografia digital ou analógica."),
    COZINHAR("Cozinhar", Categoria.OUTRO, "Experimentar receitas, meal prep."),
    OUTRO_PERSONALIZADO("Outro (personalizado)", Categoria.OUTRO, ""); // permite entrada livre
    
    // Campos de cada hobby pré-definido
    private final String nome;
    private final Categoria categoria;
    private final String descricao;

    // Construtor do enum que preenche os campos
    PredefinedHobbies(String nome, Categoria categoria, String descricao) {
        this.nome = nome;
        this.categoria = categoria;
        this.descricao = descricao;
    }

    // Acessores para nome, categoria e descrição
    public String getNome() {
        return nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    // Facilita apresentação em UI
    @Override
    public String toString() {
        return nome;
    }
}
