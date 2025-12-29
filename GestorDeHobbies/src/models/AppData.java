/*
 * Propósito geral: modelo raiz que encapsula os dados persistidos da aplicação,
 * atualmente apenas os utilizadores registados.
 * Observações: serializável para ser guardado em disco; guarda utilizadores
 * numa HashMap normalizada para lower-case, evitando duplicados por capitalização.
 */
package models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AppData implements Serializable {

    // Mapa de utilizadores indexados pelo username em minúsculas
    private Map<String, User> users = new HashMap<>();

    // Acesso direto ao mapa completo (principalmente para persistência ou iteração)
    public Map<String, User> getUsers() {
        return users;
    }

    // Obtém um utilizador pelo username, ignorando capitalização
    public User getUser(String username) {
        return users.get(username.toLowerCase());
    }

    // Adiciona um utilizador, guardando a chave em minúsculas para consistência
    public void addUser(User user) {
        users.put(user.getUsername().toLowerCase(), user);
    }

    // Verifica existência de username sem distinguir maiúsculas/minúsculas
    public boolean exists(String username) {
        return users.containsKey(username.toLowerCase());
    }
}
