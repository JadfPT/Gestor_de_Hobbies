package ui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Categoria;
import models.Hobby;

public class HobbiesController {

    @FXML
    private TableView<Hobby> tblHobbies;

    @FXML
    private TableColumn<Hobby, String> colNome;

    @FXML
    private TableColumn<Hobby, Categoria> colCategoria;

    @FXML
    private TableColumn<Hobby, String> colDescricao;

    @FXML
    private TextField txtPesquisar;

    private final ObservableList<Hobby> dados = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        // Dados de exemplo por agora
        dados.addAll(
                new Hobby("Ler fantasia", Categoria.LEITURA, "Livros de fantasia e ficção científica"),
                new Hobby("Gaming", Categoria.JOGOS, "Jogos de estratégia e RPG"),
                new Hobby("Andar de bicicleta", Categoria.DESPORTO, "Passeios ao fim de semana")
        );

        tblHobbies.setItems(dados);
    }
}
