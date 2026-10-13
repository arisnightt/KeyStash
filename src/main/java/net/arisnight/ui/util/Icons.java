package net.arisnight.ui.util;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import java.util.Map;

/** Иконки из HTML-макета (сетка 16×16, обводка). Цвет задаётся в style.css. */
public final class Icons {
    private static final Map<String, String> PATHS = Map.ofEntries(
            Map.entry("lock", "M4.5 7h7A1.5 1.5 0 0 1 13 8.5v4A1.5 1.5 0 0 1 11.5 14h-7A1.5 1.5 0 0 1 3 12.5v-4A1.5 1.5 0 0 1 4.5 7z M5 7 V5 a3 3 0 0 1 6 0 v2"),
            Map.entry("all", "M3.5 3h9A1.5 1.5 0 0 1 14 4.5v7A1.5 1.5 0 0 1 12.5 13h-9A1.5 1.5 0 0 1 2 11.5v-7A1.5 1.5 0 0 1 3.5 3z M2 6.5h12"),
            Map.entry("star", "M8 1.8l1.9 4 4.3.5-3.2 3 .9 4.3L8 11.4 4.1 13.6 5 9.3l-3.2-3 4.3-.5z"),
            Map.entry("trash", "M2.5 4h11 M6 4V2.5h4V4 M4 4l.7 9h6.6L12 4"),
            Map.entry("gen", "M13 8 a5 5 0 1 1 -1.5 -3.6 M13 2.5 v2.8 h-2.8"),
            Map.entry("gear", "M10.2 8 a2.2 2.2 0 1 0 -4.4 0 a2.2 2.2 0 1 0 4.4 0 M8 1.5v2 M8 12.5v2 M1.5 8h2 M12.5 8h2 M3.4 3.4l1.4 1.4 M11.2 11.2l1.4 1.4 M3.4 12.6l1.4-1.4 M11.2 4.8l1.4-1.4"),
            Map.entry("search", "M11.5 7 a4.5 4.5 0 1 0 -9 0 a4.5 4.5 0 1 0 9 0 M10.5 10.5L14 14"),
            Map.entry("plus", "M8 2.5v11 M2.5 8h11"),
            Map.entry("copy", "M7 5.5h5.5A1.5 1.5 0 0 1 14 7v5.5a1.5 1.5 0 0 1-1.5 1.5H7a1.5 1.5 0 0 1-1.5-1.5V7A1.5 1.5 0 0 1 7 5.5z M10.5 5.5V4A1.5 1.5 0 0 0 9 2.5H4A1.5 1.5 0 0 0 2.5 4v5A1.5 1.5 0 0 0 4 10.5h1.5"),
            Map.entry("eye", "M1.5 8S4 3.5 8 3.5 14.5 8 14.5 8 12 12.5 8 12.5 1.5 8 1.5 8z M10 8 a2 2 0 1 0 -4 0 a2 2 0 1 0 4 0"),
            Map.entry("edit", "M10.5 3l2.5 2.5L5.5 13 2.5 13.5 3 10.5z"),
            Map.entry("open", "M9 2.5h4.5V7 M13.5 2.5L7 9 M11.5 9.5v3a1 1 0 0 1-1 1h-7a1 1 0 0 1-1-1v-7a1 1 0 0 1 1-1h3"),
            Map.entry("sun", "M11 8 a3 3 0 1 0 -6 0 a3 3 0 1 0 6 0 M8 1.5v1.5 M8 13v1.5 M1.5 8H3 M13 8h1.5 M3.4 3.4l1 1 M11.6 11.6l1 1 M3.4 12.6l1-1 M11.6 4.4l1-1"),
            Map.entry("shield", "M8 1.5l5.5 2v4c0 3.2-2.3 5.6-5.5 7-3.2-1.4-5.5-3.8-5.5-7v-4z M5.6 8l1.7 1.7 3.1-3.4"),
            Map.entry("clock", "M14 8 a6 6 0 1 0 -12 0 a6 6 0 1 0 12 0 M8 4.5V8l2.3 1.5"),
            Map.entry("check", "M14 8 a6 6 0 1 0 -12 0 a6 6 0 1 0 12 0 M5.5 8.2l1.8 1.8 3.2-3.5"),
            Map.entry("key", "M9.6 9 a2.6 2.6 0 1 0 -5.2 0 a2.6 2.6 0 1 0 5.2 0 M9.4 9H15 M13 9v2.5")
    );

    private Icons() {}

    public static Node get(String name) {
        SVGPath path = new SVGPath();
        path.setContent(PATHS.get(name));
        path.getStyleClass().add("icon");
        Pane box = new Pane(path);
        box.getStyleClass().add("ks-icon");
        box.setMinSize(16, 16);
        box.setPrefSize(16, 16);
        box.setMaxSize(16, 16);
        box.setMouseTransparent(true);
        return box;
    }
}