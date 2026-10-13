package net.arisnight.service;

import javafx.animation.PauseTransition;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;

/** Копирование секретов с очисткой буфера по таймеру (только если там всё ещё наше значение). */
public final class ClipboardService {
    private static PauseTransition timer;

    private ClipboardService() {}

    public static void copySecret(String text, Duration clearAfter) {
        Clipboard cb = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        cb.setContent(content);

        if (timer != null) timer.stop();
        timer = new PauseTransition(clearAfter);
        timer.setOnFinished(e -> {
            if (text.equals(cb.getString())) cb.clear();
        });
        timer.play();
    }
}