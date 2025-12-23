import data.Persistencia;
import models.AppData;
import models.Hobby;
import models.User;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class UserInspector {

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        AppData dados = Persistencia.carregar();
        System.out.println("=== UserInspector (appData/users.dat) ===");

        boolean sair = false;
        while (!sair) {
            System.out.println();
            System.out.println("1) Listar utilizadores");
            System.out.println("2) Ver detalhes de um utilizador");
            System.out.println("3) Criar utilizador");
            System.out.println("4) Alterar password");
            System.out.println("5) Apagar utilizador");
            System.out.println("0) Guardar e sair");
            System.out.print("Opcao: ");
            String op = SC.nextLine().trim();

            switch (op) {
                case "1" -> listarUsers(dados);
                case "2" -> verUser(dados);
                case "3" -> criarUser(dados);
                case "4" -> alterarPassword(dados);
                case "5" -> apagarUser(dados);
                case "0" -> sair = true;
                default -> System.out.println("Opcao inválida.");
            }
        }

        try {
            Persistencia.gravar(dados);
            System.out.println("Dados guardados em appData/users.dat.");
        } catch (IOException e) {
            System.out.println("Erro ao gravar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void listarUsers(AppData dados) {
        Map<String, User> mapa = dados.getUsers();
        if (mapa.isEmpty()) {
            System.out.println("(sem utilizadores)");
            return;
        }
        System.out.println("Utilizadores existentes:");
        for (User u : mapa.values()) {
            System.out.println(" - " + u.getUsername());
        }
    }

    private static void verUser(AppData dados) {
        System.out.print("Username: ");
        String username = SC.nextLine().trim();
        User u = dados.getUser(username);
        if (u == null) {
            System.out.println("Utilizador não encontrado.");
            return;
        }

        System.out.println("Username : " + u.getUsername());
        System.out.println("Password : " + u.getPassword());
        System.out.println("Hobbies  :");
        for (Hobby h : u.getHobbies()) {
            System.out.println("   - " + h.getNome() + " (" + h.getCategoria() + ")");
        }
        System.out.println("Numero sessões: " + u.getSessoes().size());
    }

    private static void criarUser(AppData dados) {
        System.out.print("Novo username: ");
        String username = SC.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username vazio.");
            return;
        }
        if (dados.exists(username)) {
            System.out.println("Ja existe utilizador com esse nome.");
            return;
        }
        System.out.print("Password: ");
        String pass = SC.nextLine();

        User u = new User(username, pass);
        dados.addUser(u);
        System.out.println("Utilizador criado (sem hobbies ainda).");
    }

    private static void alterarPassword(AppData dados) {
        System.out.print("Username: ");
        String username = SC.nextLine().trim();
        User u = dados.getUser(username);
        if (u == null) {
            System.out.println("Utilizador não encontrado.");
            return;
        }
        System.out.print("Nova password: ");
        String pass = SC.nextLine();
        u.setPassword(pass);
        System.out.println("Password atualizada.");
    }

    private static void apagarUser(AppData dados) {
        System.out.print("Username a apagar: ");
        String username = SC.nextLine().trim();
        Map<String, User> mapa = dados.getUsers();
        if (mapa.remove(username.toLowerCase()) != null) {
            System.out.println("Utilizador removido.");
        } else {
            System.out.println("Utilizador nao encontrado.");
        }
    }
}
