package net.arisnight.model;

import java.time.LocalDate;
import java.util.List;

/** Демо-данные для макета. Заменить загрузкой из зашифрованного хранилища. */
public final class DemoData {
    private DemoData() {}

    public static List<Entry> entries() {
        return List.of(
                new Entry("GitHub", "dev@keystash.dev", "kT9#vQ2$mXe7!wLb", "github.com", "Работа", true,
                        List.of("2fa", "серверы"), 92, "Рабочий аккаунт. Коды восстановления лежат в сейфе.",
                        LocalDate.of(2025, 3, 12), LocalDate.of(2026, 10, 3)),
                new Entry("Telegram", "@arisnyte", "summer-lamp-orbit-cedar", "web.telegram.org", "Соцсети", false,
                        List.of("семья"), 74, "",
                        LocalDate.of(2024, 2, 20), LocalDate.of(2026, 7, 9)),
                new Entry("Hetzner Cloud", "admin@keystash.dev", "Rm2$yN8!cQw4#Pk7", "console.hetzner.cloud", "Работа", true,
                        List.of("серверы", "2fa"), 97, "Сервер CI. Ключ SSH хранится отдельно.",
                        LocalDate.of(2025, 11, 14), LocalDate.of(2026, 9, 28)),
                new Entry("Steam", "WailNx", "Pd6#hT1@zXm9$Wf3", "store.steampowered.com", "Личное", false,
                        List.of("игры"), 90, "",
                        LocalDate.of(2022, 3, 8), LocalDate.of(2026, 5, 11)));
    }
}