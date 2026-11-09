package com.mar.trn.page;

import com.mar.AppConfig;
import lombok.Getter;
import org.jline.terminal.Terminal;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingsPage implements Page {

    @Getter
    private final Tab tab;

    private final AtomicInteger speed = new AtomicInteger(1000);
    private final String torrentsPath;
    private final String downloadsPath;

    public SettingsPage(int order, AppConfig appConfig) {
        this.tab = new Tab("Settings", this, order);
        this.torrentsPath = appConfig.getTorrentsPath().toString();
        this.downloadsPath = appConfig.getDownloadsPath().toString();
    }

    @Override
    public Callable<Boolean> buttonEvent(char b) {
        return () -> {
            speed.set(new Random().nextInt(1000, 9999));
            return true;
        };
    }

    @Override
    public void render(Terminal terminal) {
        terminal.writer().printf(" Configuration:\033[K\n");
        terminal.writer().printf("  > Torrents Directory: %s\033[K\n", torrentsPath);
        terminal.writer().printf("  > Download Directory: %s\033[K\n", downloadsPath);
        terminal.writer().printf("  > Max Active Torrents: 5\033[K\n");
        terminal.writer().printf("  > Speed Limit: %d\033[K\n", speed.get());
    }
}
