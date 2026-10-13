package net.arisnight.ui.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.arisnight.generator.PasswordGenerator;
import net.arisnight.generator.PasswordGenerator.Options;
import net.arisnight.generator.StrengthEstimator;
import net.arisnight.model.Entry;
import net.arisnight.ui.util.Icons;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Окно создания новой записи. Возвращает запись или пусто, если пользователь отменил. */
public final class EntryDialog {

    private static final String NO_CATEGORY = "Без категории";

    private final TextField name = new TextField();
    private final TextField login = new TextField();
    private final PasswordField pass = new PasswordField();
    private final TextField passPlain = new TextField();
    private final TextField url = new TextField();
    private final ComboBox<String> category = new ComboBox<>();
    private final TextField tags = new TextField();
    private final CheckBox fav = new CheckBox("Добавить в избранное");
    private final TextArea note = new TextArea();
    private final Label error = new Label();
    private final Label strengthLbl = new Label();
    private Entry result;

    public static Optional<Entry> show(Window owner, boolean dark, List<String> categories) {
        return Optional.ofNullable(new EntryDialog(owner, dark, categories).result);
    }

    private EntryDialog(Window owner, boolean dark, List<String> categories) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Новая запись");
        // окно не выше 85% рабочей области экрана, иначе кнопки могут уйти за его край
        stage.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight() * 0.85);

        name.setPromptText("Например, GitHub");
        login.setPromptText("Логин или e-mail");
        url.setPromptText("example.com");
        tags.setPromptText("через запятую: 2fa, почта");
        note.setPromptText("Необязательно");
        note.setPrefRowCount(2);
        note.setWrapText(true);
        note.getStyleClass().add("ks-area");
        for (TextField f : new TextField[]{name, login, pass, passPlain, url, tags}) f.getStyleClass().add("ks-input");
        pass.getStyleClass().add("mono");
        passPlain.getStyleClass().add("mono");

        // пароль: скрытое и видимое поля синхронизированы
        passPlain.textProperty().bindBidirectional(pass.textProperty());
        setShown(false);
        StackPane passStack = new StackPane(pass, passPlain);
        HBox.setHgrow(passStack, Priority.ALWAYS);
        Button eye = iconBtn("eye");
        eye.setOnAction(e -> setShown(!passPlain.isVisible()));
        Button gen = iconBtn("gen");
        gen.setOnAction(e -> {
            pass.setText(PasswordGenerator.password(new Options(20, true, true, true, true, true)).value());
            setShown(true);
        });
        HBox passRow = new HBox(4, passStack, eye, gen);
        passRow.setAlignment(Pos.CENTER_LEFT);
        strengthLbl.getStyleClass().add("muted-s");
        pass.textProperty().addListener((o, a, b) -> updateStrength());

        category.setEditable(true);
        category.getItems().addAll(categories);
        category.getEditor().setPromptText(NO_CATEGORY);
        category.getStyleClass().add("ks-input-combo");
        category.setMaxWidth(Double.MAX_VALUE);

        error.getStyleClass().add("err-label");
        error.visibleProperty().bind(error.textProperty().isNotEmpty());
        error.managedProperty().bind(error.visibleProperty());

        Label title = new Label("Новая запись");
        title.getStyleClass().add("h3");
        VBox form = new VBox(12, title,
                block("Название *", name),
                block("Логин", login),
                block("Пароль", new VBox(4, passRow, strengthLbl)),
                block("Адрес", url),
                block("Категория", category),
                block("Теги", tags),
                fav,
                block("Заметки", note),
                error);
        form.setPadding(new Insets(24));

        Button cancel = new Button("Отмена");
        cancel.getStyleClass().add("ks-btn");
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> stage.close());
        Button save = new Button("Сохранить");
        save.getStyleClass().addAll("ks-btn", "ks-btn-pri");
        save.setDefaultButton(true);
        save.setOnAction(e -> save(stage));
        for (Button b : new Button[]{cancel, save}) {
            b.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(b, Priority.ALWAYS);
        }
        HBox footer = new HBox(8, cancel, save);
        footer.getStyleClass().add("dlg-footer");

        // форма прокручивается, а кнопки внизу всегда остаются на виду
        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("ks-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox root = new VBox(scroll, footer);
        root.setPrefWidth(480);
        if (dark) root.getStyleClass().add("dark");
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) stage.close();
        });

        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(owner.getScene().getStylesheets());
        stage.setScene(scene);
        Platform.runLater(name::requestFocus);
        stage.showAndWait();
    }

    private void save(Stage stage) {
        String n = name.getText().trim();
        if (n.isEmpty()) {
            error.setText("Введите название записи");
            name.requestFocus();
            return;
        }
        String cat = category.getEditor().getText().trim();
        if (cat.isEmpty()) cat = NO_CATEGORY;
        List<String> tg = Arrays.stream(tags.getText().split("[,;]"))
                .map(String::trim)
                .map(t -> t.startsWith("#") ? t.substring(1).trim() : t)
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();
        String pw = pass.getText();
        LocalDate today = LocalDate.now();
        result = new Entry(n, login.getText().trim(), pw, url.getText().trim(), cat, fav.isSelected(), tg,
                StrengthEstimator.score(pw), note.getText().trim(), today, today);
        stage.close();
    }

    private void setShown(boolean shown) {
        passPlain.setVisible(shown);
        passPlain.setManaged(shown);
        pass.setVisible(!shown);
        pass.setManaged(!shown);
    }

    private void updateStrength() {
        String pw = pass.getText();
        strengthLbl.setText(pw.isEmpty() ? "" : "Надёжность: " + StrengthEstimator.label(StrengthEstimator.score(pw)));
    }

    private static VBox block(String label, Node input) {
        Label l = new Label(label);
        l.getStyleClass().add("muted-s");
        return new VBox(4, l, input);
    }

    private static Button iconBtn(String icon) {
        Button b = new Button();
        b.setGraphic(Icons.get(icon));
        b.getStyleClass().addAll("ks-btn", "ks-btn-ic", "ks-btn-sub");
        return b;
    }
}