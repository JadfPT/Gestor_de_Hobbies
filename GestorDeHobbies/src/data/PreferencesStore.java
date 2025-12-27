package data;

import ui.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Simple persistence for app-level preferences so the login/register pages
 * can render with the last chosen theme and other settings before a user logs in.
 */
public final class PreferencesStore {

    private static final String PASTA_APPDATA = "appData";
    private static final String FICHEIRO_PREFS = "prefs.properties";

    private PreferencesStore() {}

    private static File getPrefsFile() {
        return new File(PASTA_APPDATA, FICHEIRO_PREFS);
    }

    public static void loadAppPrefs() {
        File f = getPrefsFile();
        if (!f.exists()) {
            return; // use defaults
        }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(f)) {
            p.load(in);
        } catch (IOException ignored) {
            return;
        }

        String dark = p.getProperty("darkMode", "false");
        String time24 = p.getProperty("use24HourTime", "true");
        String dateFmt = p.getProperty("dateFormat", App.getDateFormatPattern());
        String chartCol = p.getProperty("chartColor", App.getChartColor());

        try {
            App.setDarkModeEnabled(Boolean.parseBoolean(dark));
            App.setUse24HourTime(Boolean.parseBoolean(time24));
            if (dateFmt != null && !dateFmt.isBlank()) App.setDateFormatPattern(dateFmt);
            if (chartCol != null && !chartCol.isBlank()) App.setChartColor(chartCol);
        } catch (Exception ignored) {}
    }

    public static void saveAppPrefs() {
        Properties p = new Properties();
        p.setProperty("darkMode", Boolean.toString(App.isDarkModeEnabled()));
        p.setProperty("use24HourTime", Boolean.toString(App.isUse24HourTime()));
        p.setProperty("dateFormat", App.getDateFormatPattern());
        p.setProperty("chartColor", App.getChartColor());

        File f = getPrefsFile();
        File dir = f.getParentFile();
        if (dir != null && !dir.exists()) {
            // try to create the appData directory if missing
            dir.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(f)) {
            p.store(out, "Gestor de Hobbies - App Preferences");
        } catch (IOException ignored) {}
    }
}