package data;

import models.AppData;

import java.io.*;

public final class Persistencia {

    // Pasta MANUAL que tu crias na raiz do projeto
    private static final String PASTA_APPDATA = "appData";
    private static final String FICHEIRO_USERS = "users.dat";

    private Persistencia() { }

    private static File getFicheiroUsers() {
        // appdata/users.dat (relativo à pasta do projeto)
        return new File(PASTA_APPDATA, FICHEIRO_USERS);
    }

    public static void gravar(AppData dados) throws IOException {
        File f = getFicheiroUsers();

        // se a pasta não existir, damos erro explícito
        File dir = f.getParentFile();
        if (dir == null || !dir.exists()) {
            throw new IOException("Pasta 'appdata' não encontrada. "
                    + "Cria a pasta 'appdata' na raiz do projeto.");
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(dados);
        }
    }

    public static AppData carregar() {
        File f = getFicheiroUsers();
        // se ainda não existir ficheiro de users, começamos vazio
        if (!f.exists()) {
            return new AppData();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (AppData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new AppData();
        }
    }
}
