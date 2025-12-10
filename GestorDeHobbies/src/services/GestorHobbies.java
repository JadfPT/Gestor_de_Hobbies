/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import models.Hobby;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author João
 */
public class GestorHobbies implements Serializable {
    
    private final List<Hobby> hobbies = new ArrayList<>();

    public List<Hobby> getHobbies() {
        return Collections.unmodifiableList(hobbies);
    }

    public void adicionarHobby(Hobby hobby) {
        hobbies.add(hobby);
    }

    public void removerHobby(Hobby hobby) {
        hobbies.remove(hobby);
    }

    public Hobby procurarPorNome(String nome) {
        return hobbies.stream()
                .filter(h -> h.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }
}
