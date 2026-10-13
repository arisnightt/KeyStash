package net.arisnight.ui.dialog;

import net.arisnight.generator.PasswordGenerator;
import net.arisnight.generator.PasswordGenerator.Options;
import net.arisnight.generator.PasswordGenerator.Result;
import net.arisnight.ui.util.Icons;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

/** Окно генератора паролей (аналог диалога из HTML-макета). */
public final class GeneratorDialog {

    private final TextFlow out = new TextFlow();
    private final Label ent = new Label();
    private final Label lbl = new Label();
    private final Slider len = new Slider(4, 128, 20);
    private final Label lenVal = new Label("20");
    private final Label lenLbl = new Label("Длина");
    private final CheckBox lo = new CheckBox("a–z строчные");
    private final CheckBox up = new CheckBox("A–Z прописные");
    private final CheckBox di = new CheckBox("0–9 цифры");
    private final CheckBox sy = new CheckBox("!@# спецсимволы");
    private final CheckBox ex = new CheckBox("Исключить похожие символы (0 O l 1 I)");
    private final ToggleButton modePwd = new ToggleButton("Пароль");
    private final ToggleButton modePhrase = new ToggleButton("Парольная фраза");
    private final Consumer<String> onCopy;
    private String current = "";

    public static void show(Window owner, boolean dark, Consumer<String> onCopy) {
        new GeneratorDialog(owner, dark, onCopy);
    }

    private GeneratorDialog(Window owner, boolean dark, Consumer<String> onCopy) {
        this.onCopy = onCopy;
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Генератор паролей");
        stage.setResizable(false);

        // результат
        out.getStyleClass().add("gen-out");
        out.setPrefWidth(300);
        out.setMaxWidth(Double.MAX_VALUE);
        out.setMinHeight(24);
        HBox.setHgrow(out, Priority.ALWAYS);
        Button re = icon("gen");
        re.setOnAction(e -> generate());
        Button cp = icon("copy");
        cp.setOnAction(e -> onCopy.accept(current));
        HBox outBox = new HBox(4, out, re, cp);
        outBox.setAlignment(Pos.CENTER_LEFT);
        outBox.getStyleClass().add("out");

        // энтропия
        ent.getStyleClass().add("muted-s");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox entRow = new HBox(ent, sp, lbl);
        entRow.setPadding(new Insets(2, 2, 6, 2));

        // режимы
        ToggleGroup g = new ToggleGroup();
        modePwd.setToggleGroup(g);
        modePhrase.setToggleGroup(g);
        modePwd.setSelected(true);
        for (ToggleButton t : new ToggleButton[]{modePwd, modePhrase}) {
            t.getStyleClass().add("seg-btn");
            t.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(t, Priority.ALWAYS);
        }
        g.selectedToggleProperty().addListener((o, a, b) -> {
            if (b == null) a.setSelected(true);
        });
        HBox seg = new HBox(modePwd, modePhrase);
        seg.getStyleClass().add("seg");

        // длина
        len.setBlockIncrement(1);
        len.setMajorTickUnit(1);
        len.setMinorTickCount(0);
        len.setSnapToTicks(true);
        lenLbl.getStyleClass().add("muted-s-14");
        lenLbl.setMinWidth(60);
        lenVal.setMinWidth(36);
        lenVal.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(len, Priority.ALWAYS);
        HBox lenRow = new HBox(12, lenLbl, len, lenVal);
        lenRow.setAlignment(Pos.CENTER_LEFT);

        // чекбоксы
        for (CheckBox c : new CheckBox[]{lo, up, di, sy, ex}) {
            c.setSelected(true);
            c.selectedProperty().addListener((o, a, b) -> generate());
        }
        GridPane opts = new GridPane();
        opts.setHgap(16);
        opts.setVgap(8);
        opts.add(lo, 0, 0);
        opts.add(up, 1, 0);
        opts.add(di, 0, 1);
        opts.add(sy, 1, 1);
        opts.add(ex, 0, 2, 2, 1);
        opts.visibleProperty().bind(modePhrase.selectedProperty().not());
        opts.managedProperty().bind(opts.visibleProperty());

        modePhrase.selectedProperty().addListener((o, a, phrase) -> {
            if (phrase) {
                len.setMin(3);
                len.setMax(10);
                len.setValue(5);
                lenLbl.setText("Слов");
            } else {
                len.setMax(128);
                len.setMin(4);
                len.setValue(20);
                lenLbl.setText("Длина");
            }
            lenVal.setText(String.valueOf((int) len.getValue()));
            generate();
        });
        len.valueProperty().addListener((o, a, b) -> {
            lenVal.setText(String.valueOf((int) len.getValue()));
            generate();
        });

        Label title = new Label("Генератор паролей");
        title.getStyleClass().add("h3");
        VBox content = new VBox(12, title, outBox, entRow, seg, lenRow, opts);
        content.setPadding(new Insets(24));

        // подвал
        Button close = new Button("Закрыть");
        close.getStyleClass().add("ks-btn");
        close.setOnAction(e -> stage.close());
        Button use = new Button("Использовать");
        use.getStyleClass().addAll("ks-btn", "ks-btn-pri");
        use.setOnAction(e -> {
            onCopy.accept(current);
            stage.close();
        });
        for (Button b : new Button[]{close, use}) {
            b.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(b, Priority.ALWAYS);
        }
        HBox footer = new HBox(8, close, use);
        footer.getStyleClass().add("dlg-footer");

        VBox root = new VBox(content, footer);
        root.setPrefWidth(480);
        if (dark) root.getStyleClass().add("dark");

        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(owner.getScene().getStylesheets());
        stage.setScene(scene);
        generate();
        stage.show();
    }

    private static Button icon(String name) {
        Button b = new Button();
        b.setGraphic(Icons.get(name));
        b.getStyleClass().addAll("ks-btn", "ks-btn-ic", "ks-btn-sub");
        return b;
    }

    private void generate() {
        out.getChildren().clear();
        try {
            int n = (int) len.getValue();
            Result r = modePhrase.isSelected()
                    ? PasswordGenerator.passphrase(n, "-")
                    : PasswordGenerator.password(new Options(n, lo.isSelected(), up.isSelected(),
                    di.isSelected(), sy.isSelected(), ex.isSelected()));
            current = r.value();
            for (char c : current.toCharArray()) {
                Text t = new Text(String.valueOf(c));
                t.getStyleClass().addAll("mono", "gen-ch");
                if (Character.isDigit(c)) t.getStyleClass().add("digit");
                else if (!Character.isLetter(c)) t.getStyleClass().add("sym");
                out.getChildren().add(t);
            }
            double bits = r.entropyBits();
            ent.setText(Math.round(bits) + " бит энтропии");
            lbl.setText(bits >= 100 ? "Отличный" : bits >= 70 ? "Хороший" : bits >= 50 ? "Средний" : "Слабый");
            lbl.setStyle("-fx-font-weight:bold;-fx-text-fill:"
                    + (bits >= 70 ? "-ks-ok" : bits >= 50 ? "-ks-warn" : "-ks-bad") + ";");
        } catch (IllegalArgumentException ex) {
            current = "";
            Text t = new Text(ex.getMessage());
            t.getStyleClass().add("err-text");
            out.getChildren().add(t);
            ent.setText("");
            lbl.setText("");
        }
    }
}