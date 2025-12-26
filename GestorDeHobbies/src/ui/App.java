package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;
    private static boolean darkModeEnabled = false;

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

        applyThemeToScene();
    }

    public static boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public static void setDarkModeEnabled(boolean enabled) {
        darkModeEnabled = enabled;
        applyThemeToScene();
    }

    private static void applyThemeToScene() {
        if (primaryStage == null) return;
        Scene scene = primaryStage.getScene();
        if (scene == null) return;
        Parent root = scene.getRoot();
        if (root == null) return;

        if (darkModeEnabled) {
            if (!root.getStyleClass().contains("dark")) root.getStyleClass().add("dark");
        } else {
            root.getStyleClass().remove("dark");
        }
    }

    public static void setRoot(String fxml) throws IOException {
        Parent root = loadFXML(fxml);
        primaryStage.getScene().setRoot(root);
        applyThemeToScene();
    }

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
        return loader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}