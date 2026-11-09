package com.mar;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Getter
public class AppConfig {

    private final Path torrentsPath;
    private final Path downloadsPath;

    public AppConfig(String torrentsPath, String downloadsPath) {
        this.torrentsPath = Path.of(Optional.ofNullable(torrentsPath).orElse("./torrents"));
        this.downloadsPath = Path.of(Optional.ofNullable(downloadsPath).orElse("./downloads"));
    }

}
