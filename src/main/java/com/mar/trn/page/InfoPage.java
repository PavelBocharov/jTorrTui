package com.mar.trn.page;

import lombok.Getter;
import org.jline.terminal.Terminal;

import java.util.concurrent.Callable;

public class InfoPage implements Page {

    @Getter
    private final Tab tab;

    public InfoPage(int order) {
        this.tab = new Tab("Info", this, order);
    }

    @Override
    public void render(Terminal terminal) {
        terminal.writer().printf("For download torrent, add '.torrent' file in directory './torrent'.\033[K\n");
        terminal.writer().printf("All downloading files in './download'.\033[K\n");
    }
}
