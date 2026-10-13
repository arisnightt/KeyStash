package net.arisnight.ui;

import net.arisnight.model.DemoData;
import net.arisnight.model.Entry;
import net.arisnight.service.ClipboardService;
import net.arisnight.service.UrlOpener;
import net.arisnight.ui.dialog.EntryDialog;
import net.arisnight.ui.dialog.GeneratorDialog;
import net.arisnight.ui.util.Icons;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Главное окно: навигация | список | детали + статус-бар. Данные пока демонстрационные. */
public final class MainView extends StackPane {

    private static final String[] PALETTE = {
            "#24292f", "#229ed9", "#d50c2d", "#1b2838", "#0078d4", "#107c10", "#c239b3", "#ca5010"};
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("ru"));
    private static final Duration CLIPBOARD_TTL = Duration.seconds(30);

    private final HostServices host;
    private final List<Entry> all = new ArrayList<>(DemoData.entries());

    private Predicate<Entry> navFilter = e -> true;
    private Entry selected;
    private boolean pwShown;
    private boolean dark;

    private final VBox nav = new VBox(2);
    private Node navHead;
    private final VBox items = new VBox(2);
    private final VBox detail = new VBox(12);
    private final Label listTitle = new Label("Все записи");
    private final TextField search = new TextField();
    private final ComboBox<String> sort = new ComboBox<>();
    private final Label toast = new Label();
    private SequentialTransition toastAnim;

    public MainView(HostServices host) {
        this.host = host;

        BorderPane content = new BorderPane();
        content.setTop(buildTitleBar());
        content.setLeft(buildNav());
        content.setCenter(buildMain());
        content.setBottom(buildStatusBar());

        toast.getStyleClass().add("toast");
        toast.setGraphic(Icons.get("check"));
        toast.setOpacity(0);
        toast.setMouseTransparent(true);
        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 44, 0));

        getChildren().addAll(content, toast);
        refreshList();
    }

    /* ======================= Заголовок ======================= */

    private Node buildTitleBar() {
        StackPane logo = new StackPane(new Rectangle(18, 18) {{
            setArcWidth(8);
            setArcHeight(8);
            getStyleClass().add("logo");
        }}, Icons.get("key"));
        logo.getChildren().get(1).getStyleClass().add("on-accent");
        logo.setMaxSize(18, 18);
        logo.setScaleX(0.9);
        logo.setScaleY(0.9);

        Label name = new Label("KeyStash");
        name.getStyleClass().add("muted-s");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button theme = btn("Тема", "sun", "ks-btn-sub");
        theme.setOnAction(e -> toggleTheme());
        Button lock = btn("Заблокировать", "lock", "ks-btn-sub");
        lock.setOnAction(e -> showToast("Блокировка пока не реализована"));

        HBox bar = new HBox(8, logo, name, spacer, theme, lock);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("titlebar");
        return bar;
    }

    private void toggleTheme() {
        dark = !dark;
        if (dark) getStyleClass().add("dark");
        else getStyleClass().remove("dark");
    }

    /* ======================= Навигация ======================= */

    private Node buildNav() {
        nav.getStyleClass().add("nav");
        nav.setPrefWidth(248);
        nav.setMinWidth(248);
        nav.setMaxWidth(248);

        search.setPromptText("Поиск по записям");
        search.getStyleClass().add("ks-search");
        search.textProperty().addListener((o, a, b) -> refreshList());
        StackPane.setAlignment(search, Pos.CENTER_LEFT);
        Node sIcon = Icons.get("search");
        StackPane.setMargin(sIcon, new Insets(0, 0, 0, 10));
        StackPane searchBox = new StackPane(search, sIcon);
        StackPane.setAlignment(sIcon, Pos.CENTER_LEFT);
        VBox.setMargin(searchBox, new Insets(0, 4, 10, 4));
        navHead = searchBox;
        fillNav();
        return nav;
    }

    /** Пересобирает навигацию (счётчики и список категорий), например после добавления записи. */
    private void fillNav() {
        nav.getChildren().setAll(navHead);
        ToggleGroup group = new ToggleGroup();
        nav.getChildren().add(navItem(group, Icons.get("all"), "Все записи", all.size(), e -> true, true));
        nav.getChildren().add(navItem(group, Icons.get("star"), "Избранное",
                (int) all.stream().filter(Entry::favorite).count(), Entry::favorite, false));

        nav.getChildren().add(header("Категории"));
        for (String c : all.stream().map(Entry::category).distinct().sorted().toList()) {
            Circle dot = new Circle(4);
            dot.setStyle("-fx-fill:" + color(c) + ";");
            HBox.setMargin(dot, new Insets(0, 4, 0, 4));
            int n = (int) all.stream().filter(e -> e.category().equals(c)).count();
            nav.getChildren().add(navItem(group, dot, c, n, e -> e.category().equals(c), false));
        }

        nav.getChildren().add(header("Теги"));
        FlowPane tags = new FlowPane(6, 6);
        tags.setPadding(new Insets(0, 10, 0, 10));
        all.stream().flatMap(e -> e.tags().stream()).distinct().sorted().forEach(t -> {
            Label l = new Label(t);
            l.getStyleClass().add("tag");
            tags.getChildren().add(l);
        });
        nav.getChildren().add(tags);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        nav.getChildren().add(spacer);

        nav.getChildren().add(navAction("trash", "Корзина", -1, () -> showToast("Корзина — в планах (P2)")));
        nav.getChildren().add(navAction("gen", "Генератор паролей", -1, this::openGenerator));
        nav.getChildren().add(navAction("gear", "Настройки", -1, () -> showToast("Настройки — TODO")));
    }

    private ToggleButton navItem(ToggleGroup g, Node icon, String text, int count,
                                 Predicate<Entry> filter, boolean on) {
        ToggleButton tb = new ToggleButton();
        tb.getStyleClass().add("ni");
        tb.setToggleGroup(g);
        tb.setMaxWidth(Double.MAX_VALUE);
        tb.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        tb.setGraphic(navContent(icon, text, count, tb));
        tb.setSelected(on);
        tb.setOnAction(e -> {
            if (!tb.isSelected()) tb.setSelected(true);
            navFilter = filter;
            listTitle.setText(text);
            refreshList();
        });
        return tb;
    }

    private Button navAction(String icon, String text, int count, Runnable action) {
        Button b = new Button();
        b.getStyleClass().add("ni");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        b.setGraphic(navContent(Icons.get(icon), text, count, b));
        b.setOnAction(e -> action.run());
        return b;
    }

    private static HBox navContent(Node icon, String text, int count, Region host) {
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label n = new Label(count < 0 ? "" : String.valueOf(count));
        n.getStyleClass().add("muted");
        HBox box = new HBox(12, icon, new Label(text), sp, n);
        box.setAlignment(Pos.CENTER_LEFT);
        fillWidth(box, host, 24);
        return box;
    }

    private static void fillWidth(Region box, Region host, double pad) {
        box.prefWidthProperty().bind(Bindings.max(0.0, host.widthProperty().subtract(pad)));
    }

    private static Label header(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted");
        l.setPadding(new Insets(14, 10, 6, 10));
        return l;
    }

    private void addEntry() {
        List<String> categories = all.stream().map(Entry::category).distinct().sorted().toList();
        EntryDialog.show(getScene().getWindow(), dark, categories).ifPresent(entry -> {
            all.add(entry);
            // показываем новую запись: сбрасываем фильтры и выбираем её
            navFilter = e -> true;
            listTitle.setText("Все записи");
            selected = entry;
            pwShown = false;
            search.clear();
            fillNav();
            refreshList();
            showToast("Запись «" + entry.name() + "» добавлена");
        });
    }

    private void openGenerator() {
        GeneratorDialog.show(getScene().getWindow(), dark, text -> {
            ClipboardService.copySecret(text, CLIPBOARD_TTL);
            showToast("Пароль скопирован. Буфер очистится через 30 с");
        });
    }

    /* ======================= Список и детали ======================= */

    private Node buildMain() {
        // список
        listTitle.getStyleClass().add("h1");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button add = btn("Добавить", "plus", "ks-btn-pri");
        add.setOnAction(e -> addEntry());
        HBox head = new HBox(8, listTitle, sp, add);
        head.setAlignment(Pos.CENTER_LEFT);
        head.setPadding(new Insets(16, 16, 8, 16));

        sort.getItems().addAll("по названию", "по дате изменения", "по дате создания");
        sort.getSelectionModel().selectFirst();
        sort.getStyleClass().add("ks-combo");
        sort.setOnAction(e -> refreshList());
        Label sl = new Label("Сортировка:");
        sl.getStyleClass().add("muted");
        HBox sortRow = new HBox(6, sl, sort);
        sortRow.setAlignment(Pos.CENTER_LEFT);
        sortRow.setPadding(new Insets(0, 16, 8, 16));

        items.setPadding(new Insets(0, 8, 8, 8));
        ScrollPane listScroll = scroll(items);
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        VBox list = new VBox(head, sortRow, listScroll);
        list.getStyleClass().add("list");
        list.setPrefWidth(340);
        list.setMinWidth(260);
        list.setMaxWidth(340);

        // детали
        detail.getStyleClass().add("detail");
        ScrollPane detailScroll = scroll(detail);
        HBox.setHgrow(detailScroll, Priority.ALWAYS);

        HBox main = new HBox(list, detailScroll);
        main.getStyleClass().add("main");
        return main;
    }

    private static ScrollPane scroll(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.getStyleClass().add("ks-scroll");
        return sp;
    }

    private void refreshList() {
        String q = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        Comparator<Entry> cmp = switch (sort.getSelectionModel().getSelectedIndex()) {
            case 1 -> Comparator.comparing(Entry::modified, Comparator.reverseOrder());
            case 2 -> Comparator.comparing(Entry::created, Comparator.reverseOrder());
            default -> Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER);
        };
        List<Entry> shown = all.stream()
                .filter(navFilter)
                .filter(e -> q.isEmpty() || e.matches(q))
                .sorted(cmp)
                .toList();

        if (selected == null || !shown.contains(selected)) {
            selected = shown.isEmpty() ? null : shown.get(0);
            pwShown = false;
        }
        items.getChildren().setAll(shown.stream().map(this::row).toList());
        refreshDetail();
    }

    private Button row(Entry e) {
        Label name = new Label(e.name());
        name.getStyleClass().add("row-title");
        Label login = new Label(e.login());
        login.getStyleClass().add("muted-s");
        VBox tx = new VBox(name, login);
        HBox.setHgrow(tx, Priority.ALWAYS);

        HBox box = new HBox(12, avatar(e, 36, 8), tx);
        box.setAlignment(Pos.CENTER_LEFT);
        if (e.favorite()) {
            Node star = Icons.get("star");
            star.getStyleClass().add("fav");
            box.getChildren().add(star);
        }

        Button b = new Button();
        b.getStyleClass().add("row");
        if (e == selected) b.getStyleClass().add("selected");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        b.setGraphic(box);
        fillWidth(box, b, 24);
        b.setOnAction(ev -> {
            selected = e;
            pwShown = false;          // при смене записи пароль снова скрыт
            refreshList();
        });
        return b;
    }

    private void refreshDetail() {
        detail.getChildren().clear();
        if (selected == null) {
            Label empty = new Label("Нет записей");
            empty.getStyleClass().add("muted");
            detail.getChildren().add(empty);
            return;
        }
        Entry e = selected;

        // шапка
        Label title = new Label(e.name());
        title.getStyleClass().add("h2");
        String sub = e.category() + (e.tags().isEmpty() ? ""
                : " · " + e.tags().stream().map(t -> "#" + t).collect(Collectors.joining(" ")));
        Label subtitle = new Label(sub);
        subtitle.getStyleClass().add("muted-s");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button fav = iconBtn("star", "Избранное", "ks-btn-sub");
        if (e.favorite()) fav.getGraphic().getStyleClass().add("fav");
        fav.setOnAction(ev -> showToast("Избранное — TODO"));
        Button edit = btn("Изменить", "edit");
        edit.setOnAction(ev -> showToast("Редактирование — TODO"));
        Button del = iconBtn("trash", "Удалить", "ks-btn-sub", "ks-btn-dng");
        del.setOnAction(ev -> showToast("Удаление — TODO"));

        HBox head = new HBox(16, avatar(e, 56, 12), new VBox(title, subtitle), sp, new HBox(4, fav, edit, del));
        head.setAlignment(Pos.CENTER_LEFT);
        head.setPadding(new Insets(0, 0, 8, 0));

        // карточка с полями
        Label loginV = new Label(e.login());
        loginV.setWrapText(true);
        Button copyLogin = iconBtn("copy", "Копировать логин", "ks-btn-sub");
        copyLogin.setOnAction(ev -> copy(e.login(), "Логин"));

        Label pw = new Label(pwShown ? e.password() : "•".repeat(Math.min(e.password().length(), 16)));
        pw.getStyleClass().add("mono");
        pw.setWrapText(true);
        VBox pwBox = new VBox(4, pw, strength(e.strength()));
        Button eye = iconBtn("eye", pwShown ? "Скрыть" : "Показать", "ks-btn-sub");
        eye.setOnAction(ev -> {
            pwShown = !pwShown;
            refreshDetail();
        });
        Button copyPw = iconBtn("copy", "Копировать пароль", "ks-btn-sub");
        copyPw.setOnAction(ev -> copy(e.password(), "Пароль"));

        Hyperlink link = new Hyperlink(e.url());
        link.setOnAction(ev -> openUrl(e.url()));
        Button openBtn = iconBtn("open", "Открыть в браузере", "ks-btn-sub");
        openBtn.setOnAction(ev -> openUrl(e.url()));

        VBox card = new VBox(
                field("Логин", loginV, copyLogin),
                field("Пароль", pwBox, eye, copyPw),
                field("Адрес", link, openBtn));
        card.getStyleClass().add("card");
        for (int i = 1; i < card.getChildren().size(); i++) card.getChildren().get(i).getStyleClass().add("fld-sep");
        detail.getChildren().addAll(head, card);

        if (e.note() != null && !e.note().isBlank()) {
            Label note = new Label(e.note());
            note.getStyleClass().add("muted-s-14");
            note.setWrapText(true);
            VBox noteCard = new VBox(field("Заметки", note));
            noteCard.getStyleClass().add("card");
            detail.getChildren().add(noteCard);
        }

        Label cr = new Label("Создано: " + DATE.format(e.created()));
        Label md = new Label("Изменено: " + DATE.format(e.modified()));
        cr.getStyleClass().add("muted");
        md.getStyleClass().add("muted");
        HBox meta = new HBox(24, cr, md);
        meta.setPadding(new Insets(4, 4, 0, 4));
        detail.getChildren().add(meta);
    }

    private static HBox field(String label, Node value, Node... actions) {
        Label l = new Label(label);
        l.getStyleClass().add("muted-s");
        l.setMinWidth(96);
        l.setPrefWidth(96);
        HBox.setHgrow(value, Priority.ALWAYS);
        HBox acts = new HBox(2, actions);
        acts.setAlignment(Pos.CENTER_RIGHT);
        HBox row = new HBox(12, l, value, acts);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("fld");
        return row;
    }

    private static Node strength(int s) {
        Region fill = new Region();
        fill.getStyleClass().add("bar-fill");
        fill.setMinSize(0, 4);
        fill.setPrefSize(96.0 * s / 100, 4);
        fill.setMaxSize(96.0 * s / 100, 4);
        StackPane bar = new StackPane(fill);
        bar.getStyleClass().add("bar");
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        bar.setMinSize(96, 4);
        bar.setPrefSize(96, 4);
        bar.setMaxSize(96, 4);

        String lbl = s >= 90 ? "Отличный" : s >= 75 ? "Хороший" : s >= 50 ? "Средний" : "Слабый";
        Label t = new Label(lbl);
        t.getStyleClass().add("muted-s");
        HBox box = new HBox(8, bar, t);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /* ======================= Статус-бар ======================= */

    private Node buildStatusBar() {
        Label ok = new Label("Хранилище разблокировано", Icons.get("shield"));
        ok.getStyleClass().add("status-ok");
        // TODO: подключить реальный таймер автоблокировки
        Label lock = new Label("Автоблокировка через 5:00", Icons.get("clock"));
        lock.getStyleClass().add("muted-s");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label algo = new Label("Argon2id · AES-256-GCM");
        algo.getStyleClass().add("muted-s");
        HBox bar = new HBox(20, ok, lock, sp, algo);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("status");
        return bar;
    }

    /* ======================= Утилиты ======================= */

    private void copy(String value, String what) {
        ClipboardService.copySecret(value, CLIPBOARD_TTL);
        showToast(what + " скопирован. Буфер очистится через 30 с");
    }

    private void openUrl(String url) {
        if (!UrlOpener.open(host, url)) showToast("Разрешены только адреса http и https");
    }

    private void showToast(String msg) {
        toast.setText(msg);
        if (toastAnim != null) toastAnim.stop();
        toast.setOpacity(0);
        FadeTransition in = new FadeTransition(Duration.millis(180), toast);
        in.setToValue(1);
        FadeTransition out = new FadeTransition(Duration.millis(300), toast);
        out.setDelay(Duration.seconds(2.2));
        out.setToValue(0);
        toastAnim = new SequentialTransition(in, out);
        toastAnim.play();
    }

    private static Button btn(String text, String icon, String... styles) {
        Button b = new Button(text, Icons.get(icon));
        b.getStyleClass().add("ks-btn");
        b.getStyleClass().addAll(styles);
        return b;
    }

    private static Button iconBtn(String icon, String tip, String... styles) {
        Button b = new Button();
        b.setGraphic(Icons.get(icon));
        b.setTooltip(new Tooltip(tip));
        b.getStyleClass().addAll("ks-btn", "ks-btn-ic");
        b.getStyleClass().addAll(styles);
        return b;
    }

    private static Node avatar(Entry e, double size, double radius) {
        Label l = new Label(e.name().substring(0, 1).toUpperCase());
        l.setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:" + (size * 0.42) + "px;");
        StackPane p = new StackPane(l);
        p.setMinSize(size, size);
        p.setPrefSize(size, size);
        p.setMaxSize(size, size);
        p.setStyle("-fx-background-color:" + color(e.name()) + ";-fx-background-radius:" + radius + ";");
        return p;
    }

    private static String color(String s) {
        return PALETTE[Math.floorMod(s.hashCode(), PALETTE.length)];
    }
}