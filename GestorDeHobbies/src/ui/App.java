package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        Scene scene = new Scene(loadFXML("views/LoginView.fxml"));
        stage.setTitle("Gestor de Hobbies");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
        stage.setMaximized(true);
    }

    public static void setRoot(String fxml) throws IOException {
        primaryStage.getScene().setRoot(loadFXML(fxml));
    }

    // <-- AGORA É PUBLIC
    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
        return loader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
