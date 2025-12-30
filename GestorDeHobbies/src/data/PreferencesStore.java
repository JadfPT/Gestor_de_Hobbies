/*
 * Propósito geral: persiste e carrega preferências globais da aplicação para que
 * as páginas de login/registo e o resto da UI usem o último tema e formatos antes
 * de um utilizador iniciar sessão.
 * Observações: guarda as preferências em appData/prefs.properties; tenta criar a
 * pasta appData se não existir ao gravar; falhas de leitura/escrita são ignoradas
 * para não bloquear a aplicação.
 */
package data;

import ui.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class PreferencesStore {

    // Pasta e nome do ficheiro onde ficam as preferências de UI persistidas
    private static final String PASTA_APPDATA = "appData";
    private static final String FICHEIRO_PREFS = "prefs.properties";

    private PreferencesStore() {}

    // Construção centralizada do caminho para reutilização coerente em leitura/escrita
    private static File getPrefsFile() {
        return new File(PASTA_APPDATA, FICHEIRO_PREFS);
    }

    // Carrega o ficheiro de preferências, aplicando cada valor ao App; se algo falhar, mantém defaults
    public static void loadAppPrefs() {
        File f = getPrefsFile();
            if (!f.exists()) {
                // First run: ensure app defaults are light/standard
                try {
                    App.setDarkModeEnabled(false);
                    App.setUse24HourTime(true);
                    App.setDateFormatPattern("yyyy-MM-dd");
                    App.setChartColor("#f97316");
                } catch (Exception ignored) {}
                return; // use defaults
            }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(f)) {
            p.load(in);
        } catch (IOException ignored) {
            return;
        }

        // Extrai cada propriedade com fallback para valores por defeito já usados pela app
        String dark = p.getProperty("darkMode", "false");
        String time24 = p.getProperty("use24HourTime", "true");
        String dateFmt = p.getProperty("dateFormat", App.getDateFormatPattern());
        String chartCol = p.getProperty("chartColor", App.getChartColor());

        // Aplica ao App; erros são ignorados para evitar bloquear o arranque por configs corruptas
        try {
            App.setDarkModeEnabled(Boolean.parseBoolean(dark));
            App.setUse24HourTime(Boolean.parseBoolean(time24));
            if (dateFmt != null && !dateFmt.isBlank()) App.setDateFormatPattern(dateFmt);
            if (chartCol != null && !chartCol.isBlank()) App.setChartColor(chartCol);
        } catch (Exception ignored) {}
    }

    // Serializa as preferências atuais do App para o ficheiro de propriedades
    public static void saveAppPrefs() {
        Properties p = new Properties();
        p.setProperty("darkMode", Boolean.toString(App.isDarkModeEnabled()));
        p.setProperty("use24HourTime", Boolean.toString(App.isUse24HourTime()));
        p.setProperty("dateFormat", App.getDateFormatPattern());
        p.setProperty("chartColor", App.getChartColor());

        File f = getPrefsFile();
        File dir = f.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs(); // tenta criar appData se estiver em falta
        }
        try (FileOutputStream out = new FileOutputStream(f)) {
            p.store(out, "Gestor de Hobbies - App Preferences"); // escreve em formato .properties
        } catch (IOException ignored) {}
    }
}