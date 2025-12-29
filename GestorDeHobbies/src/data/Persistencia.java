/*
 * Propósito geral: fornece mecanismos simples para persistir e recuperar a instância
 * principal de dados da aplicação (`AppData`) num ficheiro binário.
 * Observações: assume que existe a pasta "appData" na raiz do projeto; caso contrário
 * a escrita falha explicitamente com uma exceção, enquanto a leitura cria dados em branco.
 */
package data;

import models.AppData;

import java.io.*;

public final class Persistencia {

    private static final String PASTA_APPDATA = "appData";
    private static final String FICHEIRO_USERS = "users.dat";

    private Persistencia() { }

    // Constrói a referência para o ficheiro de dados dos utilizadores
    private static File getFicheiroUsers() {
        return new File(PASTA_APPDATA, FICHEIRO_USERS);
    }

    // Grava o objeto AppData no ficheiro binário, exigindo que a pasta exista
    public static void gravar(AppData dados) throws IOException {
        File f = getFicheiroUsers();

        File dir = f.getParentFile();
        if (dir == null || !dir.exists()) {
            throw new IOException("Pasta 'appdata' não encontrada. "
                    + "Cria a pasta 'appdata' na raiz do projeto.");
        }

        // Usa ObjectOutputStream para serializar todo o estado de AppData
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(dados);
        }
    }

    // Carrega o objeto AppData do ficheiro; devolve instância vazia se faltar ou falhar
    public static AppData carregar() {
        File f = getFicheiroUsers();
        if (!f.exists()) {
            return new AppData();
        }

        // Tenta desserializar o ficheiro; em qualquer falha cria dados novos e regista o erro
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (AppData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new AppData();
        }
    }
}
