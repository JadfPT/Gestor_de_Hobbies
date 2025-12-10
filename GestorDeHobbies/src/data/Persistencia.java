/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;

import services.GestorHobbies;

import java.io.*;

/**
 *
 * @author João
 */
public class Persistencia {
    
    private Persistencia() {
    }

    public static void gravar(String ficheiro, GestorHobbies gestor) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ficheiro))) {
            oos.writeObject(gestor);
        }
    }

    public static GestorHobbies carregar(String ficheiro)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ficheiro))) {
            return (GestorHobbies) ois.readObject();
        }
    }
    
}
