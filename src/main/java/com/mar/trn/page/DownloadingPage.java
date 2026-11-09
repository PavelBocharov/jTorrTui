package com.mar.trn.page;

import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.runtime.Config;
import bt.torrent.TorrentSessionState;
import com.mar.AppConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jline.terminal.Terminal;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DownloadingPage implements Page {

    @Getter
    private final Tab tab;

    private final Path torrentsPath;
    private final Path downloadsPath;

    private File dir;
    private Storage storage;
    private Config config;
    private BtRuntime runtime;

    private final Set<Pair<String, TorrentSessionState>> torrentState = new CopyOnWriteArraySet<>();

    public DownloadingPage(int order, AppConfig appConfig) {
        this.tab = new Tab("Downloading", this, order);
        this.torrentsPath = appConfig.getTorrentsPath();
        this.downloadsPath = appConfig.getDownloadsPath();
    }

    @Override
    public void render(Terminal terminal) {
        terminal.writer().printf("\033[K %3.3s | %56.56s | %11s | %10s |\033[K\n",
                "#", "Title", "% to end", "Peers"
        );
        AtomicInteger i = new AtomicInteger(1);
        for (Pair<String, TorrentSessionState> pair : torrentState) {
            TorrentSessionState state = pair.value;

            // 2. ЗАЩИТА ОТ NaN (деления на 0):
            double progress = 0.0;
            if (state.getPiecesTotal() > 0) {
                progress = 100 - (double) state.getPiecesComplete() / state.getPiecesTotal() * 100;
            }

            double downloadedMb = (double) state.getDownloaded() / (1024 * 1024);

            terminal.writer().printf("\033[K %3d | %56.56s | %10.2f%% | %10d |\033[K\n",
//            terminal.writer().printf("\033[K %3d | %40.40s | %10.2f%% | %10.10s | %10.10s | %10d |\033[K\n",
                    i.getAndIncrement(),
                    pair.key,
                    progress,
//                    FileUtils.byteCountToDisplaySize(state.getPiecesComplete() * ),
//                    FileUtils.byteCountToDisplaySize(state.getPiecesTotal()),
                    state.getConnectedPeers().size()
            );
        }
    }

    @SneakyThrows
    @Override
    public Thread onStart() {
        dir = torrentsPath.toFile();
        if (!dir.exists()) {
            FileUtils.forceMkdir(dir);
        }

        File downloadDir = downloadsPath.toFile();
        if (!downloadDir.exists()) {
            FileUtils.forceMkdir(downloadDir);
        }
        storage = new FileSystemStorage(downloadDir);

        config = new Config() {
            @Override
            public int getNumOfHashingThreads() {
                return Runtime.getRuntime().availableProcessors();
            }
        };

        runtime = BtRuntime.builder(config)
                .autoLoadModules()
//                .module(dhtModule)
                .build();

        Thread t = new Thread(() -> {
            log.info("Starting DownloadingPage");
            checkTorrentDir();
        });
        t.setDaemon(true);
        return t;
    }

    @SneakyThrows
    private void checkTorrentDir() {
        FileUtils.listFiles(dir, new String[]{"torrent"}, true).parallelStream().forEach(this::startDownload);
    }

    @SneakyThrows
    void startDownload(File torrentFile) {
        log.info("Add torrent file to download list: {}", torrentFile.getName());
        BtClient client = Bt.client(runtime)
                .torrent(torrentFile.toURI().toURL())
                .storage(storage)
                .build();

        client.startAsync(state -> {
            torrentState.add(new Pair(torrentFile.getName(), state));
            if (state.getPiecesRemaining() == 0) {
                log.info("Torrent file is complete. File - {}", torrentFile.getName());
                client.stop();
            }
        }, 1000).join();
    }

    @Override
    public void onStop() {
        runtime.shutdown();
    }

    record Pair<T, K>(T key, K value) {
    }
}
