package services;

import models.Categoria;

public enum PredefinedHobbies {
    LEITURA("Leitura", Categoria.LEITURA, "Ler livros, mangas, etc."),
    DESENHAR("Desenhar / Pintar", Categoria.ARTES, "Artes visuais, ilustração, pintura."),
    INSTRUMENTO("Tocar um instrumento", Categoria.MUSICA, "Prática de instrumento musical."),
    GAMING("Gaming", Categoria.JOGOS, "Jogos de PC, consola ou mobile."),
    VER_FILMES("Ver filmes e séries", Categoria.FILMES, "Cinema, streaming, séries."),
    OUVIR_MUSICA("Ouvir música", Categoria.MUSICA, "Playlists, álbuns."),
    ANDAR("Andar a pé", Categoria.DESPORTO, "Caminhadas, passeios."),
    CORRER("Correr", Categoria.DESPORTO, "Corrida, jogging."),
    FOTOGRAFIA("Fotografia", Categoria.ARTES, "Fotografia digital ou analógica."),
    COZINHAR("Cozinhar", Categoria.OUTRO, "Experimentar receitas, meal prep."),
    OUTRO_PERSONALIZADO("Outro (personalizado)", Categoria.OUTRO, ""); // especial
    
    private final String nome;
    private final Categoria categoria;
    private final String descricao;

    PredefinedHobbies(String nome, Categoria categoria, String descricao) {
        this.nome = nome;
        this.categoria = categoria;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return nome;
    }
}
