/*
 * Propósito geral: gerir a lista de hobbies de um utilizador, permitindo adicionar,
 * remover e procurar por nome, preparado para ser serializado com os restantes dados.
 * Observações: expõe uma vista imutável da lista para evitar alterações externas;
 * a pesquisa ignora capitalização.
 */
package services;

import models.Hobby;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GestorHobbies implements Serializable {
    
    // Lista interna mutável de hobbies
    private final List<Hobby> hobbies = new ArrayList<>();

    // Exposição somente leitura da lista para consumidores
    public List<Hobby> getHobbies() {
        return Collections.unmodifiableList(hobbies);
    }

    // Adiciona um hobby à coleção
    public void adicionarHobby(Hobby hobby) {
        hobbies.add(hobby);
    }

    // Remove um hobby, se presente
    public void removerHobby(Hobby hobby) {
        hobbies.remove(hobby);
    }

    // Procura um hobby pelo nome, ignorando maiúsculas/minúsculas
    public Hobby procurarPorNome(String nome) {
        return hobbies.stream()
                .filter(h -> h.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }
}
