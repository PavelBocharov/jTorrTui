package com.mar;

import com.mar.trn.page.DownloadingPage;
import com.mar.trn.page.InfoPage;
import com.mar.trn.page.Page;
import com.mar.trn.page.SettingsPage;
import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
public class Main {

    private static Page activePage;
    private static volatile boolean running = true;
    private static List<Page> pages;



    public static void main(String[] args) {
        Properties p = System.getProperties();

        pages = Stream.of(new InfoPage(3), new SettingsPage(2), new DownloadingPage(1))
                .peek(page -> Optional.ofNullable(page.onStart()).ifPresent(Thread::start))
                .sorted().toList();
        activePage = pages.getFirst();

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            terminal.puts(InfoCmp.Capability.enter_ca_mode);
            terminal.puts(InfoCmp.Capability.cursor_invisible);
            terminal.enterRawMode();

            Thread inputThread = new Thread(() -> handleInput(terminal));
            inputThread.setDaemon(true);
            inputThread.start();

            while (running) {
                synchronized (terminal) {
                    renderUi(terminal);
                }
                Thread.sleep(1000);
            }

            // Восстановление терминала при корректном выходе
            restoreTerminal(terminal);

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * Отрисовка интерфейса в зависимости от выбранной вкладки
     */
    private static void renderUi(Terminal terminal) {
        // Прыгаем в левый верхний угол (0,0) без очистки всего экрана
        terminal.writer().print("\033[H");

        // 1. Рисуем шапку
        terminal.writer().printf(" TORRENT JAVA CLIENT v1.0 | Press [Q] to Exit\033[K\n");
        terminal.writer().printf("--------------------------------------------------------------------------------------------\033[K\n");

        // 2. Рисуем вкладки (подсвечиваем выбранную скобками или цветом)
        terminal.writer().print(" ");
        AtomicInteger order = new AtomicInteger(1);
        for (Page page : pages) {
            if (page.equals(activePage)) {
                terminal.writer().printf(" \033[7m (%d) %s \033[0m ", order.getAndIncrement(), page.getTab().title());
            } else {
                terminal.writer().printf("  (%d) %s  ", order.getAndIncrement(), page.getTab().title());
            }
        }
        terminal.writer().print("\033[K\n--------------------------------------------------------------------------------------------\033[K\n");

        // 3. Рисуем контент в зависимости от вкладки
        activePage.render(terminal);

        terminal.writer().print("\033[J");
        terminal.flush();
    }

    /**
     * Обработка нажатий клавиш (стрелки и хоткеи)
     */
    private static void handleInput(Terminal terminal) {
        try {
            while (running) {
                char ch = (char) terminal.reader().read();

                if (ch == 'q' || ch == 'Q') {
                    running = false;
                    break;
                }

                // Быстрые хоткеи по кнопкам 1, 2, ...
                int i = Character.getNumericValue(ch) - 1;
                if (i >= 0 && i < pages.size()) {
                    activePage = pages.get(i);
                }
                activePage.buttonEvent(ch).call();

                synchronized (terminal) {
                    renderUi(terminal);
                }
            }

            // stop all
            for (Page page : pages) {
                page.onStop();
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private static void restoreTerminal(Terminal terminal) {
        try {
            terminal.puts(InfoCmp.Capability.exit_ca_mode);
            terminal.puts(InfoCmp.Capability.cursor_normal);
            terminal.flush();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }
}
