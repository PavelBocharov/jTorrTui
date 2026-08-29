package com.mar.trn.page;

import org.jline.terminal.Terminal;

import java.util.concurrent.Callable;

public interface Page extends Comparable<Page> {

    Tab getTab();

    default Callable<Boolean> buttonEvent(char b) {
        return () -> {
            // По умолчанию ничего при старте не делается. Нужно переопределить.
            return true;
        };
    }

    void render(Terminal terminal);

    default Thread onStart() {
        return null;
    }

    default void onStop() {

    }

    @Override
    default int compareTo(Page o) {
        return this.getTab().compareTo(o.getTab());
    }
}
