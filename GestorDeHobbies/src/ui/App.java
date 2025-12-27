package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.File;

import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;
    private static boolean darkModeEnabled = false;
    private static boolean use24HourTime = true;
    private static String dateFormatPattern = "yyyy-MM-dd";
    private static String chartColor = "#f97316"; // orange default
    private static final java.util.List<Runnable> chartColorListeners = new java.util.ArrayList<>();
    private static Image appIcon;

    @Override
    public void start(Stage stage) throws Exception {
        // Load persisted app-level preferences before showing any UI
        data.PreferencesStore.loadAppPrefs();
        primaryStage = stage;

        Scene scene = new Scene(loadFXML("views/LoginView.fxml"));
        stage.setTitle("Gestor de Hobbies");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        Image icon = getAppIcon();
        if (icon != null) stage.getIcons().add(icon);
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

    public static boolean isUse24HourTime() {
        return use24HourTime;
    }

    public static void setUse24HourTime(boolean use24) {
        use24HourTime = use24;
    }

    public static String getDateFormatPattern() {
        return dateFormatPattern;
    }

    public static void setDateFormatPattern(String pattern) {
        if (pattern != null && !pattern.isBlank()) {
            dateFormatPattern = pattern;
        }
    }

    public static String getChartColor() {
        return chartColor;
    }

    public static void setChartColor(String colorCss) {
        if (colorCss != null && !colorCss.isBlank()) {
            chartColor = colorCss;
            notifyChartColorChanged();
        }
    }

    public static void addChartColorListener(Runnable listener) {
        if (listener != null && !chartColorListeners.contains(listener)) {
            chartColorListeners.add(listener);
        }
    }

    private static void notifyChartColorChanged() {
        for (Runnable r : chartColorListeners) {
            try { r.run(); } catch (Exception ignored) {}
        }
    }

    private static void applyThemeToScene() {
        if (primaryStage == null) return;
        Scene scene = primaryStage.getScene();
        if (scene == null) return;
        Parent root = scene.getRoot();
        if (root == null) return;

        if (darkModeEnabled) {
            if (!root.getStyleClass().contains("dark")) root.getStyleClass().add("dark");
            String darkCss = App.class.getResource("style/dark.css").toExternalForm();
            if (!scene.getStylesheets().contains(darkCss)) {
                scene.getStylesheets().add(darkCss);
            }
        } else {
            root.getStyleClass().remove("dark");
            String darkCss = App.class.getResource("style/dark.css").toExternalForm();
            scene.getStylesheets().remove(darkCss);
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

    public static Image getAppIcon() {
        if (appIcon != null) return appIcon;
        // Try classpath under ui/icone
        try {
            var url = App.class.getResource("icone/pooicone.png");
            if (url != null) {
                appIcon = new Image(url.toExternalForm());
                return appIcon;
            }
        } catch (Exception ignored) {}
        // Try classpath root /icone
        try {
            var url = App.class.getResource("/icone/pooicone.png");
            if (url != null) {
                appIcon = new Image(url.toExternalForm());
                return appIcon;
            }
        } catch (Exception ignored) {}
        // Fallback to workspace path icone/pooicone.png
        try {
            File f = new File("icone/pooicone.png");
            if (f.exists()) {
                appIcon = new Image(f.toURI().toString());
                return appIcon;
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}