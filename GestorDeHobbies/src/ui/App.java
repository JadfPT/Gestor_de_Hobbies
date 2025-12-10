package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("views/MainView.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle("Gestor de Hobbies");
        primaryStage.setScene(scene);

        // tamanho mínimo decente
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        primaryStage.show();

        // abre logo maximizada
        primaryStage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
