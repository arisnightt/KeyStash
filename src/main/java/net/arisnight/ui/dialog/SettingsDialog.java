package net.arisnight.ui.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import net.arisnight.i18n.I18n;
import net.arisnight.service.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.arisnight.i18n.I18n.t;

public final class SettingsDialog {

    private static final Map<String, String> LANGUAGE_NAMES = Map.of(
            "en", "English",
            "ru", "Русский",
            "fr", "Français",
            "de", "Deutsch");

    private final ComboBox<String> language = new ComboBox<>();
    private final ComboBox<Integer> clipboard = new ComboBox<>();

    public static void show(Window owner, boolean dark, Settings settings, Runnable onSaved) {
        new SettingsDialog(owner, dark, settings, onSaved);
    }

    private SettingsDialog(Window owner, boolean dark, Settings settings, Runnable onSaved) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(t("settings.title"));
        stage.setResizable(false);

        List<String> codes = new ArrayList<>();
        codes.add(I18n.SYSTEM);
        codes.addAll(I18n.LANGUAGES);
        language.getItems().addAll(codes);
        language.setConverter(new StringConverter<>() {
            @Override
            public String toString(String code) {
                if (code == null) return "";
                return I18n.SYSTEM.equals(code) ? t("lang.system") : LANGUAGE_NAMES.getOrDefault(code, code);
            }

            @Override
            public String fromString(String s) {
                return null;
            }
        });
        language.setValue(settings.language());
        language.getStyleClass().add("ks-combo");
        language.setMaxWidth(Double.MAX_VALUE);

        for (int s : Settings.CLIPBOARD_CHOICES) clipboard.getItems().add(s);
        if (!clipboard.getItems().contains(settings.clipboardSeconds())) {
            clipboard.getItems().add(settings.clipboardSeconds());
            clipboard.getItems().sort(null);
        }
        clipboard.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer s) {
                return s == null ? "" : t("settings.seconds", s);
            }

            @Override
            public Integer fromString(String s) {
                return null;
            }
        });
        clipboard.setValue(settings.clipboardSeconds());
        clipboard.getStyleClass().add("ks-combo");
        clipboard.setMaxWidth(Double.MAX_VALUE);

        Label hint = new Label(t("settings.clipboardHint"));
        hint.getStyleClass().add("muted");
        hint.setWrapText(true);

        Label title = new Label(t("settings.title"));
        title.getStyleClass().add("h3");
        VBox content = new VBox(12, title,
                block(t("settings.language"), language),
                block(t("settings.clipboard"), new VBox(6, clipboard, hint)));
        content.setPadding(new Insets(24));

        Button cancel = new Button(t("action.cancel"));
        cancel.getStyleClass().add("ks-btn");
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> stage.close());
        Button save = new Button(t("action.save"));
        save.getStyleClass().addAll("ks-btn", "ks-btn-pri");
        save.setDefaultButton(true);
        save.setOnAction(e -> {
            String lang = language.getValue();
            if (lang != null) settings.setLanguage(lang);
            Integer sec = clipboard.getValue();
            if (sec != null) settings.setClipboardSeconds(sec);
            settings.save();
            onSaved.run();
            stage.close();
        });
        for (Button b : new Button[]{cancel, save}) {
            b.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(b, Priority.ALWAYS);
        }
        HBox footer = new HBox(8, cancel, save);
        footer.getStyleClass().add("dlg-footer");
        footer.setAlignment(Pos.CENTER);

        VBox root = new VBox(content, footer);
        root.setPrefWidth(420);
        if (dark) root.getStyleClass().add("dark");
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) stage.close();
        });

        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(owner.getScene().getStylesheets());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static VBox block(String label, Node input) {
        Label l = new Label(label);
        l.getStyleClass().add("muted-s");
        return new VBox(4, l, input);
    }
}